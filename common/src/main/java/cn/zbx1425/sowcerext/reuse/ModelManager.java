package cn.zbx1425.sowcerext.reuse;

import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.CsvModelLoader;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.HashMap;

public class ModelManager {

    public HashMap<ResourceLocation, Model> uploadedModels = new HashMap<>();
    public HashMap<ResourceLocation, VertArrays> uploadedVertArrays = new HashMap<>();
    public HashMap<ResourceLocation, RawModel> loadedRawModels = new HashMap<>();

    public int uploadedVertArraysCount = 0;

    public static final VertAttrMapping DEFAULT_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.MATERIAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.ENQUEUE)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.ENQUEUE)
            .build();

    public void clear() {
        for (VertArrays vertArrays : uploadedVertArrays.values()) {
            vertArrays.close();
        }
        for (Model model : uploadedModels.values()) {
            model.close();
        }
        uploadedModels.clear();
        loadedRawModels.clear();
        uploadedVertArraysCount = 0;
    }

    public RawModel loadRawModel(ResourceManager resourceManager, ResourceLocation objLocation, AtlasManager atlasManager) throws IOException {
        if (loadedRawModels.containsKey(objLocation)) return loadedRawModels.get(objLocation);
        String crntStatExt = FilenameUtils.getExtension(objLocation.getPath());
        RawModel result;
        switch (crntStatExt) {
            case "obj":
                result = ObjModelLoader.loadModel(resourceManager, objLocation, atlasManager);
                break;
            case "csv":
                result = CsvModelLoader.loadModel(resourceManager, objLocation, atlasManager);
                break;
            case "animated":
                throw new IllegalArgumentException("ANIMATED model cannot be loaded as RawModel.");
            default:
                throw new IllegalArgumentException("Unknown model format: " + resourceManager);
        };
        loadedRawModels.put(objLocation, result);
        return result;
    }

    public Model uploadModel(RawModel rawModel) {
        if (rawModel.sourceLocation == null) {
            return rawModel.upload(DEFAULT_MAPPING);
        }
        if (uploadedModels.containsKey(rawModel.sourceLocation)) return uploadedModels.get(rawModel.sourceLocation);
        Model result = rawModel.upload(DEFAULT_MAPPING);
        uploadedModels.put(rawModel.sourceLocation, result);
        return result;
    }

    public VertArrays uploadVertArrays(RawModel rawModel) {
        if (rawModel.sourceLocation == null) {
            uploadedVertArraysCount++;
            return VertArrays.createAll(rawModel.upload(DEFAULT_MAPPING), DEFAULT_MAPPING, null);
        }
        if (uploadedVertArrays.containsKey(rawModel.sourceLocation)) return uploadedVertArrays.get(rawModel.sourceLocation);
        uploadedVertArraysCount++;
        VertArrays result = VertArrays.createAll(uploadModel(rawModel), DEFAULT_MAPPING, null);
        uploadedVertArrays.put(rawModel.sourceLocation, result);
        return result;
    }

}
