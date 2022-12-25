package cn.zbx1425.sowcer;

import cn.zbx1425.mtrsteamloco.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class ContextCapability {

    public static boolean supportVertexAttribDivisor;

    public static int contextMajor = 0;

    public static int contextMinor = 0;

    public static void getAvailableContext() {
        if (contextMajor > 0) return;
        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        long window = 0;
        try {
            /*
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 6);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
            GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
            window = GLFW.glfwCreateWindow(1, 1, "", 0, 0);
            if (window != 0) {
                contextMajor = 4;
                contextMinor = 6;
                supportVertexAttribDivisor = true;
                return;
            }
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);
            window = GLFW.glfwCreateWindow(1, 1, "", 0, 0);
            if (window != 0) {
                contextMajor = 4;
                contextMinor = 5;
                supportVertexAttribDivisor = true;
                return;
            }
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
            window = GLFW.glfwCreateWindow(1, 1, "", 0, 0);
            if (window != 0) {
                contextMajor = 4;
                contextMinor = 1;
                supportVertexAttribDivisor = true;
                return;
            }
            */
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
            window = GLFW.glfwCreateWindow(1, 1, "", 0, 0);
            if (window != 0) {
                contextMajor = 3;
                contextMinor = 3;
                supportVertexAttribDivisor = true;
                return;
            }
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            contextMajor = 3;
            contextMinor = 2;
            supportVertexAttribDivisor = false;
        } catch (Exception e) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
            contextMajor = 3;
            contextMinor = 2;
            supportVertexAttribDivisor = false;
        } finally {
            if (window != 0) {
                GLFW.glfwDestroyWindow(window);
            }
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_TRUE);
            GLFW.glfwSetErrorCallback(callback);
        }
    }
}
