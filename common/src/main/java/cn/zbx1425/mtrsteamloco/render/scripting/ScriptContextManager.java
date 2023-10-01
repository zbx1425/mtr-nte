package cn.zbx1425.mtrsteamloco.render.scripting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ScriptContextManager {

    public static final List<Map.Entry<AbstractScriptContext, ScriptHolder>> livingContexts = new LinkedList<>();

    public static void trackContext(AbstractScriptContext context, ScriptHolder scriptHolder) {
        synchronized (livingContexts) {
            livingContexts.add(Map.entry(context, scriptHolder));
        }
    }

    public static void disposeDeadContexts() {
        synchronized (livingContexts) {
            for (Iterator<Map.Entry<AbstractScriptContext, ScriptHolder>> it = livingContexts.iterator(); it.hasNext(); ) {
                Map.Entry<AbstractScriptContext, ScriptHolder> entry = it.next();
                if (!entry.getKey().isBearerAlive()) {
                    entry.getKey().debugInfo.clear();
                    entry.getValue().callDisposeFunctionAsync(entry.getKey());
                    it.remove();
                }
            }
        }
    }
}
