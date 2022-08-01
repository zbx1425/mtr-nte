package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RenderConfigForge {

    final ForgeConfigSpec.BooleanValue shaderCompatMode;
    final ForgeConfigSpec.BooleanValue enableRail3D;
    final ForgeConfigSpec.BooleanValue enableRailRender;
    final ForgeConfigSpec.BooleanValue enableTrainRender;

    RenderConfigForge(ForgeConfigSpec.Builder builder) {
        builder.comment("MTR Steam Locomotive Addon").push("mtrsteamloco");
        shaderCompatMode = builder
                .comment("Shader compatibility mode")
                .define("shader_compat_mode", false);
        enableRail3D = builder
                .comment("Enable 3D rail rendering")
                .define("rail_3d", true);
        enableRailRender = builder
                .comment("Enable rail rendering")
                        .define("rail_render", true);
        enableTrainRender = builder
                .comment("Enable train rendering")
                        .define("train_render", true);
        builder.pop();
    }

    public static final RenderConfigForge CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        Pair<RenderConfigForge, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
                .configure(RenderConfigForge::new);
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

    public static void apply() {
        if (CONFIG.shaderCompatMode.get()) {
            RenderUtil.railRenderLevel = CONFIG.enableRailRender.get() ? 1 : 0;
            RenderUtil.trainRenderLevel = CONFIG.enableTrainRender.get() ? 1 : 0;
        } else {
            RenderUtil.railRenderLevel = CONFIG.enableRailRender.get()
                    ? (CONFIG.enableRail3D.get() ? 2 : 1)
                    : 0;
            RenderUtil.trainRenderLevel = CONFIG.enableTrainRender.get() ? 2 : 0;
        }
    }
}
