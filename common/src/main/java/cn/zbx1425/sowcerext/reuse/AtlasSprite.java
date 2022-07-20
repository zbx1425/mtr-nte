package cn.zbx1425.sowcerext.reuse;

import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcerext.util.Logging;
import net.minecraft.resources.ResourceLocation;

public class AtlasSprite {

    public ResourceLocation sheet;
    public int sheetWidth, sheetHeight;
    public int frameX, frameY, frameWidth, frameHeight;
    public int spriteX, spriteY, spriteWidth, spriteHeight;
    public int sourceWidth, sourceHeight;
    public boolean rotated; // TODO

    public void applyToMesh(RawMesh mesh) {
        boolean uvBleeding = false;
        for (Vertex vertex : mesh.vertices) {
            vertex.u = mapRange(vertex.u, (float)spriteX / sourceWidth, (float)(spriteX + spriteWidth) / sourceWidth, 0, 1);
            vertex.v = mapRange(vertex.v, (float)spriteY / spriteHeight, (float)(spriteY + spriteHeight) / sourceHeight, 0, 1);
            if (vertex.u < 0 || vertex.u > 1 || vertex.v < 0 || vertex.v > 1) uvBleeding = true;
            vertex.u = mapRange(vertex.u, 0, 1, (float)frameX / sheetWidth, (float)(frameX + frameWidth) / sheetWidth);
            vertex.v = mapRange(vertex.v, 0, 1, (float)frameY / sheetHeight, (float)(frameY + frameHeight) / sheetHeight);
        }
        if (uvBleeding) {
            Logging.LOGGER.warn("UV bleeding into adjacent sprite in " + mesh.materialProp.texture);
        }
        mesh.materialProp.texture = sheet;
    }

    private static float mapRange(float x, float in_min, float in_max, float out_min, float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public AtlasSprite(ResourceLocation sheet,
                       int sheetWidth, int sheetHeight,
                       int frameX, int frameY, int frameWidth, int frameHeight,
                       int spriteX, int spriteY, int spriteWidth, int spriteHeight,
                       int sourceWidth, int sourceHeight,
                       boolean rotated) {
        this.sheet = sheet;
        this.sheetWidth = sheetWidth;
        this.sheetHeight = sheetHeight;
        this.frameX = frameX;
        this.frameY = frameY;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.spriteX = spriteX;
        this.spriteY = spriteY;
        this.spriteWidth = spriteWidth;
        this.spriteHeight = spriteHeight;
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.rotated = rotated;
    }

}
