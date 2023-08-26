package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.scripting.eyecandy.EyeCandyScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.train.TrainScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.util.*;
import cn.zbx1425.sowcer.math.Matrices;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ScriptHolder {

    private static final ExecutorService SCRIPT_THREAD = Executors.newSingleThreadExecutor();

    private Scriptable scope;

    public boolean isActive = false;
    public double lastExecuteTime = 0;

    public void load(Map<ResourceLocation, String> scripts) {
        Context rhinoCtx = Context.enter();
        try {
            scope = new ImporterTopLevel(rhinoCtx);

            scope.put("include", scope, new NativeJavaMethod(
                    ScriptResourceUtil.class.getMethod("includeScript", Object.class), "includeScript"));

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
            scope.put("Matrices", scope, new NativeJavaClass(scope, Matrices.class));
            scope.put("Matrix4f", scope, new NativeJavaClass(scope, Matrix4f.class));
            scope.put("Vector3f", scope, new NativeJavaClass(scope, Vector3f.class));

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

            ScriptResourceUtil.scriptsToExecute = new ArrayList<>(scripts.entrySet());
            for (int i = 0; i < ScriptResourceUtil.scriptsToExecute.size(); i++) {
                Map.Entry<ResourceLocation, String> entry = ScriptResourceUtil.scriptsToExecute.get(i);
                ScriptResourceUtil.relativeBase = entry.getKey();
                rhinoCtx.evaluateString(scope, entry.getValue(), entry.getKey().toString(), 1, null);
                ScriptResourceUtil.relativeBase = null;
            }

            isActive = true;
        } catch (Exception ex) {
            Main.LOGGER.error("Error in NTE Resource Pack JavaScript", ex);
        } finally {
            Context.exit();
        }
    }

    public Future<?> callTrainFunction(String function, TrainScriptContext trainCtx) {
        if (!isActive) return null;
        return SCRIPT_THREAD.submit(() -> {
            if (Thread.currentThread().isInterrupted()) return;
            Context rhinoCtx = Context.enter();
            if (trainCtx.state == null) trainCtx.state = rhinoCtx.newObject(scope);
            try {
                Object jsFunction = scope.get(function, scope);
                if (jsFunction instanceof Function && jsFunction != Scriptable.NOT_FOUND) {
                    TimingUtil.prepareForScript(this);
                    Object[] functionParam = { trainCtx, trainCtx.state, trainCtx.train, trainCtx.trainExtra };
                    ((Function)jsFunction).call(rhinoCtx, scope, scope, functionParam);
                    trainCtx.scriptFinished();
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Error in NTE Resource Pack JavaScript", ex);
                isActive = false;
            } finally {
                Context.exit();
            }
        });
    }

    public Future<?> callEyeCandyFunction(String function, EyeCandyScriptContext eyeCandyCtx) {
        if (!isActive) return null;
        return SCRIPT_THREAD.submit(() -> {
            if (Thread.currentThread().isInterrupted()) return;
            Context rhinoCtx = Context.enter();
            if (eyeCandyCtx.state == null) eyeCandyCtx.state = rhinoCtx.newObject(scope);
            try {
                Object jsFunction = scope.get(function, scope);
                if (jsFunction instanceof Function && jsFunction != Scriptable.NOT_FOUND) {
                    TimingUtil.prepareForScript(this);
                    Object[] functionParam = { eyeCandyCtx, eyeCandyCtx.state, eyeCandyCtx.entity };
                    ((Function)jsFunction).call(rhinoCtx, scope, scope, functionParam);
                    eyeCandyCtx.scriptFinished();
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Error in NTE Resource Pack JavaScript", ex);
                isActive = false;
            } finally {
                Context.exit();
            }
        });
    }
}
