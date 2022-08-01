package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ObjModelLoader {

    // No OBJ model is used in TeaconMC, thus OBJ loading library is stripped.
    public static RawModel loadModel(ResourceManager resourceManager, ResourceLocation objLocation, @Nullable AtlasManager atlasManager) throws IOException {
        return new RawModel();
    }

}
