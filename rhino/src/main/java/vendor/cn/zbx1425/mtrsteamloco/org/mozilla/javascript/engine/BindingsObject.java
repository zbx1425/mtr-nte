/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.engine;

import javax.script.Bindings;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Context;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.Scriptable;
import vendor.cn.zbx1425.mtrsteamloco.org.mozilla.javascript.ScriptableObject;

/**
 * This class makes the Bindings object into a Scriptable. That way, we can query and modify the
 * contents of the Bindings on demand.
 */
public class BindingsObject extends ScriptableObject {
    private final Bindings bindings;

    BindingsObject(Bindings bindings) {
        if (bindings == null) {
            throw new IllegalArgumentException("Bindings must not be null");
        }
        this.bindings = bindings;
    }

    @Override
    public String getClassName() {
        return "BindingsObject";
    }

    @Override
    public Object get(String name, Scriptable start) {
        if (!bindings.containsKey(name)) {
            return Scriptable.NOT_FOUND;
        }
        return Context.jsToJava(bindings.get(name), Object.class);
    }

    @Override
    public void put(String name, Scriptable start, Object value) {
        bindings.put(name, Context.javaToJS(value, start));
    }

    @Override
    public void delete(String name) {
        bindings.remove(name);
    }

    @Override
    public boolean has(String name, Scriptable start) {
        return bindings.containsKey(name);
    }

    @Override
    public Object[] getIds() {
        return bindings.keySet().toArray();
    }
}
