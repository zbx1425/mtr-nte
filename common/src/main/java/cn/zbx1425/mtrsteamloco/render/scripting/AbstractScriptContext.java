package cn.zbx1425.mtrsteamloco.render.scripting;

import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public abstract class AbstractScriptContext {

    public Scriptable state;
    protected boolean created = false;
    public Future<?> scriptStatus;
    public double lastExecuteTime = 0;

    protected boolean disposed = false;

    public long lastExecuteDuration = 0;
    public Map<String, Object> debugInfo = new HashMap<>();

    public abstract void renderFunctionFinished();

    public abstract Object getWrapperObject();

    public abstract boolean isBearerAlive();

    public void setDebugInfo(String key, Object value) {
        debugInfo.put(key, value);
    }

}
