package cn.zbx1425.mtrsteamloco.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import javax.annotation.Nullable;

public final class ShadersModHandler {
    public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";
    public static final String IRIS_ROOT_PACKAGE = "net.irisshaders.iris";

    private static final boolean isOculusLoaded;
    private static final boolean isOptifineInstalled;
    private static final InternalHandler internalHandler;

    static {
        Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
        Package irisPackage = Package.getPackage(IRIS_ROOT_PACKAGE);
        isOptifineInstalled = optifinePackage != null;
        isOculusLoaded = irisPackage != null;

        // optfine and oculus are assumed to be mutually exclusive

        if (isOptifineInstalled) {
            internalHandler = new Optifine();
        } else if (isOculusLoaded) {
            internalHandler = new Oculus();
        } else {
            internalHandler = new InternalHandler() {};
        }
    }

    public static boolean isOptifineInstalled() {
        return isOptifineInstalled;
    }

    public static boolean isOculusLoaded() {
        return isOculusLoaded;
    }

    public static boolean isShaderPackInUse() {
        return internalHandler.isShaderPackInUse();
    }

    private interface InternalHandler {
        default boolean isShaderPackInUse() {
            return false;
        }
    }

    private static class Oculus implements InternalHandler {
        private final BooleanSupplier shadersEnabledSupplier;

        Oculus() {
            shadersEnabledSupplier = createShadersEnabledSupplier();
        }

        @Override
        public boolean isShaderPackInUse() {
            return shadersEnabledSupplier.getAsBoolean();
        }

        private static BooleanSupplier createShadersEnabledSupplier() {
            try {
                Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Object irisApiInstance = irisApiClass.getMethod("getInstance").invoke(null);
                Method fnIsShaderPackInUse = irisApiClass.getMethod("isShaderPackInUse");
                return () -> {
                    try {
                        return (Boolean)fnIsShaderPackInUse.invoke(null);
                    } catch (Exception ignored) {
                        return false;
                    }
                };
            } catch (Exception ignored) {
                return () -> false;
            }
        }
    }

    private static class Optifine implements InternalHandler {
        private final BooleanSupplier shadersEnabledSupplier;

        Optifine() {
            shadersEnabledSupplier = createShadersEnabledSupplier();
        }

        @Override
        public boolean isShaderPackInUse() {
            return shadersEnabledSupplier.getAsBoolean();
        }

        private static BooleanSupplier createShadersEnabledSupplier() {
            try {
                Class<?> ofShaders = Class.forName("net.optifine.shaders.Shaders");
                Field field = ofShaders.getDeclaredField("shaderPackLoaded");
                field.setAccessible(true);
                return () -> {
                    try {
                        return field.getBoolean(null);
                    } catch (IllegalAccessException ignored) {
                        return false;
                    }
                };
            } catch (Exception ignored) {
                return () -> false;
            }
        }
    }
}