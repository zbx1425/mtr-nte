package cn.zbx1425.mtrsteamloco.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

public final class ShadersModHandler {
    public static final String OPTIFINE_ROOT_PACKAGE = "net.optifine";

    private static boolean isOculusLoaded;
    private static boolean isOptifineInstalled;
    private static InternalHandler internalHandler;

    public static void init() {
        Package optifinePackage = Package.getPackage(OPTIFINE_ROOT_PACKAGE);
        isOptifineInstalled = optifinePackage != null;
        try {
            Class<?> ignored = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            isOculusLoaded = true;
        } catch (Exception ignored) {
            isOculusLoaded = false;
        }

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
                        return (Boolean)fnIsShaderPackInUse.invoke(irisApiInstance);
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