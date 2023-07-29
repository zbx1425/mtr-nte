package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.Main;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TrainTypeScriptContext {

    private final ExecutorService SCRIPT_THREAD = Executors.newSingleThreadExecutor();

    private Scriptable scope;

    public void load(String[] scripts) {
        Context rhinoCtx = Context.enter();
        try {
            scope = rhinoCtx.initStandardObjects();
            rhinoCtx.evaluateString(scope, """
                    importPackage(Packages.cn.zbx1425.mtrsteamloco);
                    importPackage(Packages.cn.zbx1425.sowcerext.model);
                    importPackage(Packages.cn.zbx1425.sowcer.math);
                    importPackage(Packages.cn.zbx1425.mtrsteamloco.scripting);
                    """,
                    "<NTE>", 1, null);
            for (String script : scripts) {
                rhinoCtx.evaluateString(scope, script, "<NTE>", 1, null);
            }
        } catch (Exception ex) {
            Main.LOGGER.error("Script", ex);
        } finally {
            Context.exit();
        }
    }

    public Future<?> callCreateTrain(TrainScriptContext trainCtx) {
        return SCRIPT_THREAD.submit(() -> {
            if (Thread.currentThread().isInterrupted()) return;

            Context rhinoCtx = Context.enter();
            try {
                Object createFunction = scope.get("createTrain", scope);
                if (createFunction instanceof Function && createFunction != Scriptable.NOT_FOUND) {
                    Object[] functionParam = { trainCtx, trainCtx.train };
                    ((Function)createFunction).call(rhinoCtx, scope, scope, functionParam);
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Script", ex);
            } finally {
                Context.exit();
            }
        });
    }

    public Future<?> callRenderTrain(TrainScriptContext trainCtx) {
        return SCRIPT_THREAD.submit(() -> {
            if (Thread.currentThread().isInterrupted()) return;

            Context rhinoCtx = Context.enter();
            try {
                Object renderFunction = scope.get("renderTrain", scope);
                if (renderFunction instanceof Function && renderFunction != Scriptable.NOT_FOUND) {
                    Object[] functionParam = { trainCtx, trainCtx.train };
                    ((Function)renderFunction).call(rhinoCtx, scope, scope, functionParam);
                }
                trainCtx.scriptFinished();
            } catch (Exception ex) {
                Main.LOGGER.error("Script", ex);
            } finally {
                Context.exit();
            }
        });
    }
}
