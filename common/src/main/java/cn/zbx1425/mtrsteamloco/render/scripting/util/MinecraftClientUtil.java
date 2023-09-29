package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.sowcer.math.Vector3f;
import net.minecraft.client.Minecraft;

public class MinecraftClientUtil {

    public static boolean worldIsRaining() {
        return Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.isRaining();
    }

    public static boolean worldIsRainingAt(Vector3f pos) {
        return Minecraft.getInstance().level != null
                && Minecraft.getInstance().level.isRainingAt(pos.toBlockPos());
    }

    public static int worldDayTime() {
        return Minecraft.getInstance().level != null
                ? (int) Minecraft.getInstance().level.getDayTime() : 0;
    }
}
