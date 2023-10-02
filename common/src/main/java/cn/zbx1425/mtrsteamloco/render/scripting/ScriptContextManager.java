package cn.zbx1425.mtrsteamloco.render.scripting;

import java.util.*;

public class ScriptContextManager {

    public static final Map<AbstractScriptContext, ScriptHolder> livingContexts = new HashMap<>();

    public static void trackContext(AbstractScriptContext context, ScriptHolder scriptHolder) {
        synchronized (livingContexts) {
            livingContexts.put(context, scriptHolder);
        }
    }

    public static void disposeDeadContexts() {
        synchronized (livingContexts) {
            for (Iterator<Map.Entry<AbstractScriptContext, ScriptHolder>> it = livingContexts.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<AbstractScriptContext, ScriptHolder> entry = it.next();
                if (!entry.getKey().isBearerAlive() || entry.getKey().disposed) {
                    if (entry.getKey().created) {
                        entry.getValue().tryCallDisposeFunctionAsync(entry.getKey());
                    } else {
                        it.remove();
                    }
                }
            }
        }
    }
}
