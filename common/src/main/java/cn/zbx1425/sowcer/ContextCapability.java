package cn.zbx1425.sowcer;

import cn.zbx1425.mtrsteamloco.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.util.Locale;

public class ContextCapability {

    public static boolean supportVertexAttribDivisor = false;

    public static int contextVersion = 32;

    public static boolean isGL4ES = false;

    public static long createWindow(int width, int height, CharSequence title, long monitor, long share) {
        GLFWErrorCallback callback = GLFW.glfwSetErrorCallback(null);
        long window = 0;
        for (int versionToTry : new int[] {46, 45, 44, 43, 42, 41, 40, 33, 32}) {
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, versionToTry / 10);
            GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, versionToTry % 10);

            MemoryStack stack = MemoryStack.stackGet(); int stackPointer = stack.getPointer();
            try {
                stack.nUTF8(title, true);
                long titleEncoded = stack.getPointerAddress();
                window = GLFW.nglfwCreateWindow(width, height, titleEncoded, monitor, share);
            } finally {
                stack.setPointer(stackPointer);
            }

            if (window != 0) {
                contextVersion = versionToTry;
                break;
            }
        }
        if (window == 0) {
            Main.LOGGER.warn("Cannot create OpenGL context.");
        }
        supportVertexAttribDivisor = (contextVersion >= 33);
        GLFW.glfwSetErrorCallback(callback);
        return window;
    }

    public static void checkContextVersion() {
        contextVersion = GL33.glGetInteger(GL33.GL_MAJOR_VERSION) * 10 + GL33.glGetInteger(GL33.GL_MINOR_VERSION);
        supportVertexAttribDivisor = (contextVersion >= 33);

        String glVersionStr = GL33.glGetString(GL33.GL_VERSION);
        isGL4ES = glVersionStr != null && glVersionStr.toLowerCase(Locale.ROOT).contains("gl4es");
    }
}
