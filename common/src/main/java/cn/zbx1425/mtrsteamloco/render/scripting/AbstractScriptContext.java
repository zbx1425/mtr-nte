package cn.zbx1425.mtrsteamloco.render.scripting;

import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;

import java.util.concurrent.Future;

public abstract class AbstractScriptContext {

    public Scriptable state;
    protected boolean created = false;
    public Future<?> scriptStatus;
    public double lastExecuteTime = 0;

    public abstract void renderFunctionFinished();

    public abstract Object getWrapperObject();

    public abstract String getContextTypeName();

    public abstract boolean isBearerAlive();

}
