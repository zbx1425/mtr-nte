package cn.zbx1425.mtrsteamloco.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class RenderConfigForge {

    final ForgeConfigSpec.EnumValue<RenderLevel> railRenderLevel;
    final ForgeConfigSpec.EnumValue<RenderLevel> trainRenderLevel;

    public enum RenderLevel {
        NONE("不显示: 不看立省 100% 性能"),
        BLAZE("原版管线: 适配光影, 性能可能不佳, 轨道改为平面",
                "原版管线: 适配光影, 性能可能不佳, 部分半透明效果停用"),
        SOWCER("默认: 不适配光影! 使用立体的轨道模型", "默认: 不适配光影! 启用全部视觉效果")
        ;
        public String descriptionRail, descriptionTrain;
        RenderLevel(String descriptionRail, String descriptionTrain) {
            this.descriptionRail = descriptionRail;
            this.descriptionTrain = descriptionTrain;
        }
        RenderLevel(String description) {
            this.descriptionRail = description;
            this.descriptionTrain = description;
        }

        public String getDescriptionRail() {
            return descriptionRail;
        }

        public String getDescriptionTrain() {
            return descriptionTrain;
        }
    }

    RenderConfigForge(ForgeConfigSpec.Builder builder) {
        builder.comment("MTRSteamLoco").push("mtrsteamloco");
        railRenderLevel = builder
                .comment("轨道渲染方式")
                .defineEnum("rail_render_level", RenderLevel.SOWCER);
        trainRenderLevel = builder
                .comment("列车渲染方式")
                .defineEnum("train_render_level", RenderLevel.SOWCER);
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
}
