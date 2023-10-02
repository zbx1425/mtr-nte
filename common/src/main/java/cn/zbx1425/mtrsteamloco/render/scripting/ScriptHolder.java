package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.scripting.util.*;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.integration.RawMeshBuilder;
import mtr.client.ClientData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScriptHolder {

    private static ExecutorService SCRIPT_THREAD = Executors.newSingleThreadExecutor();

    private Scriptable scope;

    public long failTime = 0;
    public Exception failException = null;

    public String name;

    public void load(String name, Map<ResourceLocation, String> scripts) throws Exception {
        this.name = name;
        Context rhinoCtx = Context.enter();
        rhinoCtx.setLanguageVersion(Context.VERSION_ES6);
        try {
            scope = new ImporterTopLevel(rhinoCtx);

            scope.put("include", scope, new NativeJavaMethod(
                    ScriptResourceUtil.class.getMethod("includeScript", Object.class), "includeScript"));
            scope.put("print", scope, new NativeJavaMethod(
                    ScriptResourceUtil.class.getMethod("print", Object[].class), "print"));

            scope.put("ModelManager", scope, Context.toObject(MainClient.modelManager, scope));
            scope.put("Resources", scope, new NativeJavaClass(scope, ScriptResourceUtil.class));
            scope.put("GraphicsTexture", scope, new NativeJavaClass(scope, GraphicsTexture.class));

            scope.put("Timing", scope, new NativeJavaClass(scope, TimingUtil.class));
            scope.put("StateTracker", scope, new NativeJavaClass(scope, StateTracker.class));
            scope.put("CycleTracker", scope, new NativeJavaClass(scope, CycleTracker.class));
            scope.put("RateLimit", scope, new NativeJavaClass(scope, RateLimit.class));
            scope.put("TextUtil", scope, new NativeJavaClass(scope, TextUtil.class));

            scope.put("RawModel", scope, new NativeJavaClass(scope, RawModel.class));
            scope.put("RawMesh", scope, new NativeJavaClass(scope, RawMesh.class));
            scope.put("RawMeshBuilder", scope, new NativeJavaClass(scope, RawMeshBuilder.class));
            scope.put("Matrices", scope, new NativeJavaClass(scope, Matrices.class));
            scope.put("Matrix4f", scope, new NativeJavaClass(scope, Matrix4f.class));
            scope.put("Vector3f", scope, new NativeJavaClass(scope, Vector3f.class));

            scope.put("MTRClientData", scope, new NativeJavaClass(scope, ClientData.class));

            scope.put("MinecraftClient", scope, new NativeJavaClass(scope, MinecraftClientUtil.class));

            try {
                String[] classesToLoad = {
                        "util.AddParticleHelper",
                        "particle.MadParticleOption",
                        "particle.SpriteFrom",
                        "command.inheritable.InheritableBoolean",
                        "particle.ParticleRenderTypes",
                        "particle.ChangeMode"
                };
                for (String classToLoad : classesToLoad) {
                    Class<?> classToLoadClass = Class.forName("cn.ussshenzhou.madparticle." + classToLoad);
                    scope.put(classToLoad.substring(classToLoad.lastIndexOf(".") + 1), scope,
                            new NativeJavaClass(scope, classToLoadClass));
                }
                scope.put("foundMadParticle", scope, true);
            } catch (ClassNotFoundException ignored) {
                // Main.LOGGER.warn("MadParticle", ignored);
                scope.put("foundMadParticle", scope, false);
            }
            scope.put("CompoundTag", scope, new NativeJavaClass(scope, CompoundTag.class));

            rhinoCtx.evaluateString(scope, "\"use strict\"", "", 1, null);

            ScriptResourceUtil.activeContext = rhinoCtx;
            ScriptResourceUtil.activeScope = scope;
            for (Map.Entry<ResourceLocation, String> entry : scripts.entrySet()) {
                ScriptResourceUtil.executeScript(rhinoCtx, scope, entry.getKey(), entry.getValue());
            }
            ScriptResourceUtil.activeContext = null;
            ScriptResourceUtil.activeScope = null;
        } finally {
            Context.exit();
        }
    }

    public Future<?> callFunctionAsync(String function, AbstractScriptContext scriptCtx, Runnable finishCallback) {
        if (duringFailTimeout()) return null;
        failTime = 0;
        return SCRIPT_THREAD.submit(() -> {
            if (Thread.currentThread().isInterrupted()) return;
            Context rhinoCtx = Context.enter();
            if (scriptCtx.state == null) scriptCtx.state = rhinoCtx.newObject(scope);
            try {
                long startTime = System.nanoTime();
                Object jsFunction = scope.get(function, scope);
                if (!(jsFunction instanceof Function && jsFunction != Scriptable.NOT_FOUND)) {
                    jsFunction = scope.get(function + scriptCtx.getContextTypeName(), scope);
                }
                if (!(jsFunction instanceof Function && jsFunction != Scriptable.NOT_FOUND)) return;

                TimingUtil.prepareForScript(scriptCtx);
                Object[] functionParam = { scriptCtx, scriptCtx.state, scriptCtx.getWrapperObject() };
                ((Function)jsFunction).call(rhinoCtx, scope, scope, functionParam);
                if (finishCallback != null) finishCallback.run();
                scriptCtx.lastExecuteDuration = System.nanoTime() - startTime;
            } catch (Exception ex) {
                Main.LOGGER.error("Error in NTE Resource Pack JavaScript", ex);
                failTime = System.currentTimeMillis();
                failException = ex;
            } finally {
                Context.exit();
            }
        });
    }

    public void tryCallRenderFunctionAsync(AbstractScriptContext scriptCtx) {
        if (!(scriptCtx.scriptStatus == null || scriptCtx.scriptStatus.isDone())) return;
        if (scriptCtx.disposed) return;
        if (!scriptCtx.created) {
            ScriptContextManager.trackContext(scriptCtx, this);
            scriptCtx.scriptStatus = callFunctionAsync("create", scriptCtx, () -> {
                scriptCtx.created = true;
            });
        }
        if (scriptCtx.scriptStatus == null || scriptCtx.scriptStatus.isDone()) {
            scriptCtx.scriptStatus = callFunctionAsync("render", scriptCtx, scriptCtx::renderFunctionFinished);
        }
    }

    public void tryCallDisposeFunctionAsync(AbstractScriptContext scriptCtx) {
        if (!(scriptCtx.scriptStatus == null || scriptCtx.scriptStatus.isDone())) return;
        scriptCtx.disposed = true;
        if (scriptCtx.created) {
            scriptCtx.scriptStatus = callFunctionAsync("dispose", scriptCtx, () -> {
                scriptCtx.created = false;
            });
        }
    }

    private boolean duringFailTimeout() {
        return failTime > 0 && (System.currentTimeMillis() - failTime) < 4000;
    }

    public static void resetRunner() {
        SCRIPT_THREAD.shutdownNow();
        SCRIPT_THREAD = Executors.newSingleThreadExecutor();
    }
}
