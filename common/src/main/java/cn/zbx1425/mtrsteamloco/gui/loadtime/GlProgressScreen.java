package cn.zbx1425.mtrsteamloco.gui.loadtime;


import cn.zbx1425.mtrsteamloco.BuildConfig;
import cn.zbx1425.mtrsteamloco.Main;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class GlProgressScreen implements ProgressReceiver {

    private final List<String> logs = new ArrayList<>();
    private String primaryInfo = "";
    private float primaryProgress;
    private float secondaryProgress;
    private Exception exception;
    private boolean paused;

    private int logViewOffset = 0;

    @Override
    public void printLog(String line) throws GlHelper.MinecraftStoppingException {
        logs.add(line);
        primaryInfo = line;
        // Main.LOGGER.info(line);

        final int LOG_LINE_HEIGHT = 20;
        float logBegin = 60 + LOG_LINE_HEIGHT * 3 + 40;
        float usableLogHeight = GlHelper.getScaledHeight() - logBegin - 20;
        int logLines = (int) Math.floor(usableLogHeight / LOG_LINE_HEIGHT);
        logViewOffset = Math.max(0, logs.size() - logLines);

        redrawScreen(true);
    }

    @Override
    public void amendLastLog(String postfix) throws GlHelper.MinecraftStoppingException {
        logs.set(logs.size() - 1, logs.get(logs.size() - 1) + postfix);
        redrawScreen(true);
    }

    @Override
    public void setProgress(float primary, float secondary) throws GlHelper.MinecraftStoppingException {
        this.primaryProgress = primary;
        this.secondaryProgress = secondary;
        redrawScreen(true);
    }

    @Override
    public void setSecondaryProgress(float secondary, String textValue) throws GlHelper.MinecraftStoppingException {
        this.secondaryProgress = secondary;
        this.primaryInfo = textValue;
        redrawScreen(true);
    }

    @Override
    public void setException(Exception exception) throws GlHelper.MinecraftStoppingException {
        this.exception = exception;
        this.paused = true;
        Main.LOGGER.error("Loading", exception);
        for (String line : exception.toString().split("\n")) {
            printLog(line);
        }
    }

    public boolean pause(boolean swap) throws GlHelper.MinecraftStoppingException {
        var glfwWindow = Minecraft.getInstance().getWindow().getWindow();
        paused = !InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_RETURN);
        if (paused) {
            final int LOG_LINE_HEIGHT = 20;
            float logBegin = 60 + LOG_LINE_HEIGHT * 3 + 40;
            float usableLogHeight = GlHelper.getScaledHeight() - logBegin - 20;
            int logLines = (int) Math.floor(usableLogHeight / LOG_LINE_HEIGHT);
            int maxLogViewOffset = Math.max(0, logs.size() - logLines);

            if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_HOME)) {
                logViewOffset = 0;
            } else if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_END)) {
                logViewOffset = maxLogViewOffset;
            } else if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_PAGEUP)) {
                logViewOffset = Math.max(0, logViewOffset - logLines);
            } else if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_PAGEDOWN)) {
                logViewOffset = Math.min(maxLogViewOffset, logViewOffset + logLines);
            } else if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_UP)) {
                logViewOffset = Math.max(0, logViewOffset - 1);
            } else if (InputConstants.isKeyDown(glfwWindow, InputConstants.KEY_DOWN)) {
                logViewOffset = Math.min(maxLogViewOffset, logViewOffset + 1);
            }
            redrawScreen(swap);
        }
        return paused;
    }

    public void reset() {
        logs.clear();
        primaryInfo = "";
        primaryProgress = 0;
        secondaryProgress = 0;
        exception = null;
        paused = false;
    }

    public boolean hasException() {
        return exception != null;
    }

    public void redrawScreen(boolean swap) throws GlHelper.MinecraftStoppingException {
        GlHelper.clearScreen(1f, 0f, 1f);
        final int FONT_SIZE = 24;
        final int LINE_HEIGHT = 30;

        GlHelper.begin();
        if (GlHelper.fontAtlas != null) {
            RenderSystem.setShaderTexture(0, 0);
            RenderSystem.setShaderTexture(0, GlHelper.fontAtlas.getId());
        }
        // Minecraft.getInstance().getTextureManager().getTexture(GlHelper.PRELOAD_FONT_TEXTURE).setFilter(true, false);
        GlHelper.drawBlueGradientBackground();
        if (exception == null) {
            /*
            GlHelper.drawShadowString(20, 60, GlHelper.getScaledWidth() - 40, LINE_HEIGHT * 2, FONT_SIZE,
                    String.format("%3d%%\n%3d%%\n", Math.round(primaryProgress * 100), Math.round(secondaryProgress * 100)),
                    0xffdddddd, false, true);
            if (paused) {
                GlHelper.drawShadowString(GlHelper.getScaledWidth() - 240 - 20, 20, 240, 16, 16, "Arrow Keys to Scroll", 0xffdddddd, false, true);
                int backColor = System.currentTimeMillis() % 400 >= 200 ? 0xff27a2fd : 0xFF000000;
                GlHelper.blit(0, 60 + LINE_HEIGHT * 2, GlHelper.getScaledWidth(), LINE_HEIGHT, backColor);
                GlHelper.drawShadowString(20, 60 + LINE_HEIGHT * 2, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, FONT_SIZE,
                        "Press ENTER to proceed.",
                        0xffdddddd, false, true);
            } else {
                GlHelper.drawShadowString(GlHelper.getScaledWidth() - 160 - 20, 20, 160, 16, 16, "MTR-NTE" + BuildConfig.MOD_VERSION, 0xffdddddd, false, true);
                boolean monospace = primaryInfo.length() > 0 && primaryInfo.charAt(0)== ':';
                GlHelper.drawShadowString(20, 60 + LINE_HEIGHT * 2, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, FONT_SIZE,
                        primaryInfo,
                        0xffdddddd, monospace, true);
            }
            float barBegin = 20 + FONT_SIZE * 2 + 20;
            float usableBarWidth = GlHelper.getScaledWidth() - barBegin - 50;


            GlHelper.blit(barBegin, 60 + 3, GlHelper.getScaledWidth() - barBegin - 40, LINE_HEIGHT - 6, 0xFF666666);
            GlHelper.blit(barBegin + 3, 60 + 6, GlHelper.getScaledWidth() - barBegin - 46, LINE_HEIGHT - 12, 0xFF222222);
            GlHelper.blit(barBegin + 5, 60 + 8, usableBarWidth * primaryProgress, LINE_HEIGHT - 16, 0xff9722ff);
            GlHelper.blit(barBegin, 60 + LINE_HEIGHT + 3, GlHelper.getScaledWidth() - barBegin - 40, LINE_HEIGHT - 6, 0xFF666666);
            GlHelper.blit(barBegin + 3, 60 + LINE_HEIGHT + 6, GlHelper.getScaledWidth() - barBegin - 46, LINE_HEIGHT - 12, 0xFF222222);
            GlHelper.blit(barBegin + 5, 60 + LINE_HEIGHT + 8, usableBarWidth * secondaryProgress, LINE_HEIGHT - 16, 0xff27a2fd);
             */
        } else {
            GlHelper.drawShadowString(20, 60, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, FONT_SIZE,
                    "There was an error!",
                    0xFFFF0000, false, true);
            if (paused) {
                GlHelper.drawShadowString(GlHelper.getScaledWidth() - 240 - 20, 20, 240, 16, 16, "Arrow Keys to Scroll", 0xffdddddd, false, true);
                int backColor = System.currentTimeMillis() % 400 >= 200 ? 0xff9722ff : 0xFF000000;
                GlHelper.blit(0, 60 + LINE_HEIGHT, GlHelper.getScaledWidth(), LINE_HEIGHT, backColor);
                GlHelper.drawShadowString(20, 60 + LINE_HEIGHT, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, FONT_SIZE,
                        "Please report. Press ENTER to proceed.",
                        0xffdddddd, false, true);
            } else {
                GlHelper.drawShadowString(20, 60 + LINE_HEIGHT, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, FONT_SIZE,
                        primaryInfo,
                        0xffdddddd, true, true);
            }
        }

        final int LOG_FONT_SIZE = 16;
        final int LOG_LINE_HEIGHT = 20;
        // float logBegin = 60 + LOG_LINE_HEIGHT * 3 + 40;
        float logBegin = 60;
        float usableLogHeight = GlHelper.getScaledHeight() - logBegin - 20;
        for (int i = logViewOffset; i < logs.size(); i++) {
            GlHelper.drawShadowString(20, logBegin + LOG_LINE_HEIGHT * (i - logViewOffset), GlHelper.getScaledWidth() - 40, usableLogHeight, LOG_FONT_SIZE,
                    logs.get(i), 0xFFDDDDDD, false, true);
        }
        GlHelper.end();

        GlHelper.begin();
        GlHelper.drawShadowString(20, 20, GlHelper.getScaledWidth() - 40, LINE_HEIGHT, LINE_HEIGHT,
                "Loading MTR Resources", 0xFFFFFF00, false, true);
        // RenderSystem.setShaderTexture(0, GlHelper.PRELOAD_HEADER_TEXTURE);
        // Minecraft.getInstance().getTextureManager().getTexture(GlHelper.PRELOAD_HEADER_TEXTURE).setFilter(true, false);
        // GlHelper.blit(20, 20, 512, 32, 0, 0, 1, 1, 0xffdddddd);
        GlHelper.end();

        if (swap) {
            GlHelper.swapBuffer();
        }
    }
}