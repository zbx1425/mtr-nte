package cn.zbx1425.sowcer;

import cn.zbx1425.mtrsteamloco.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class ContextCapability {

    public static boolean supportVertexAttribDivisor = false;

    public static int contextVersion = 32;

    public static long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
        long window = 0;
        for (int versionToTry : new int[] {46, 45, 44, 43, 42, 41, 40, 33, 32}) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, versionToTry / 10);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, versionToTry % 10);
            window = GLFW.glfwCreateWindow(width, height, title, monitor, share);
            String glVersionStr = "OpenGL " + versionToTry / 10 + "." + versionToTry % 10;
            if (window != 0) {
                Main.LOGGER.warn(glVersionStr + " is supported.");
                contextVersion = versionToTry;
                break;
            } else {
                Main.LOGGER.warn(glVersionStr + " is not supported.");
            }
        }
        if (window == 0) {
            Main.LOGGER.warn("Cannot create OpenGL context.");
        }
        supportVertexAttribDivisor = (contextVersion >= 33);
        GLFW.glfwSetErrorCallback(callback);
        return window;
    }
}
