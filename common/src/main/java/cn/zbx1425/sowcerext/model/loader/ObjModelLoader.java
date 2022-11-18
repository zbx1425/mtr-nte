package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.math.Vector3f;
import de.javagl.obj.*;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ObjModelLoader {

    public static RawModel loadModel(ResourceManager resourceManager, ResourceLocation objLocation, @Nullable AtlasManager atlasManager) throws IOException {
        Obj srcObj = ObjReader.read(Utilities.getInputStream(resourceManager.getResource(objLocation)));
        Map<String, Mtl> materials = loadMaterials(resourceManager, srcObj, objLocation);

        RawModel model = loadModel(srcObj, objLocation, materials, atlasManager);
        model.sourceLocation = objLocation;
        return model;
    }

    public static Map<String, RawModel> loadModels(ResourceManager resourceManager, ResourceLocation objLocation, @Nullable AtlasManager atlasManager) throws IOException {
        Obj srcObj = ObjReader.read(Utilities.getInputStream(resourceManager.getResource(objLocation)));
        Map<String, Mtl> materials = loadMaterials(resourceManager, srcObj, objLocation);

        HashMap<String, RawModel> result = new HashMap<>();
        Map<String, Obj> groupObjs = ObjSplitting.splitByGroups(srcObj);
        for (Map.Entry<String, Obj> groupEntry : groupObjs.entrySet()) {
            RawModel model = loadModel(groupEntry.getValue(), objLocation, materials, atlasManager);
            String compliantKey = groupEntry.getKey().toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_");
            model.sourceLocation = new ResourceLocation(objLocation.getNamespace(), objLocation.getPath() + "/" + compliantKey);
            result.put(groupEntry.getKey(), model);
        }
        return result;
    }

    public static Map<String, RawModel> loadExternalModels(String path, @Nullable AtlasManager atlasManager) throws IOException {
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path))) {
            Obj srcObj = ObjReader.read(fis);

            HashMap<String, RawModel> result = new HashMap<>();
            Map<String, Obj> groupObjs = ObjSplitting.splitByGroups(srcObj);
            for (Map.Entry<String, Obj> groupEntry : groupObjs.entrySet()) {
                RawModel model = loadModel(groupEntry.getValue(), null, null, atlasManager);
                String compliantPath = path.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_");
                String compliantKey = groupEntry.getKey().toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9/._-]", "_");
                model.sourceLocation = new ResourceLocation("mtrsteamloco-external", compliantPath + "/" + compliantKey);
                result.put(groupEntry.getKey(), model);
            }
            return result;
        }
    }

    private static RawModel loadModel(Obj srcObj, ResourceLocation objLocation, Map<String, Mtl> materials, @Nullable AtlasManager atlasManager) {
        Map<String, Obj> mtlObjs = ObjSplitting.splitByMaterialGroups(srcObj);
        RawModel model = new RawModel();
        for (Map.Entry<String, Obj> entry : mtlObjs.entrySet()) {
            if (entry.getValue().getNumFaces() == 0) continue;

            ResourceLocation textureLocation = MaterialProp.PLACEHOLDER_TILE_TEXTURE_LOCATION;
            String meshRenderType = "exterior";
            String materialGroupName = entry.getValue().getActivatedMaterialGroupName(entry.getValue().getFace(0));
            if (materialGroupName.contains("#")) {
                meshRenderType = materialGroupName.split("#", 2)[1];
                materialGroupName = materialGroupName.split("#", 2)[0];
            }
            Mtl objMaterial = null;
            if (materials != null && objLocation != null) {
                objMaterial = materials.getOrDefault(materialGroupName, null);
                if (objMaterial != null) {
                    if (!StringUtils.isEmpty(objMaterial.getMapKd())) {
                        textureLocation = ResourceUtil.resolveRelativePath(objLocation, objMaterial.getMapKd(), ".png");
                    }
                }
            } else if (objLocation != null) {
                textureLocation = entry.getKey().equals("_") ? null : ResourceUtil.resolveRelativePath(objLocation, materialGroupName, ".png");
            }
            MaterialProp materialProp = new MaterialProp("", textureLocation);
            if (objMaterial != null) {
                FloatTuple color = objMaterial.getKd();
                materialProp.attrState.setColor((int)(color.getX() * 255), (int)(color.getY() * 255), (int)(color.getZ() * 255), (int)(objMaterial.getD() * 255));
            }

            Obj renderObjMesh = ObjUtils.convertToRenderable(entry.getValue());
            RawMesh mesh = new RawMesh(materialProp);
            mesh.setRenderType(meshRenderType);
            for (int i = 0; i < renderObjMesh.getNumVertices(); ++i) {
                FloatTuple pos, normal, uv;
                pos = renderObjMesh.getVertex(i);
                if (i < renderObjMesh.getNumNormals()) {
                    normal = renderObjMesh.getNormal(i);
                } else {
                    normal = ZeroFloatTuple.ZERO3;
                }
                if (i < renderObjMesh.getNumTexCoords()) {
                    uv = renderObjMesh.getTexCoord(i);
                } else {
                    uv = ZeroFloatTuple.ZERO2;
                }
                Vertex seVertex = new Vertex(
                        new Vector3f(pos.getX(), pos.getY(), pos.getZ()),
                        new Vector3f(normal.getX(), normal.getY(), normal.getZ())
                );
                seVertex.u = uv.getX();
                seVertex.v = uv.getY();
                mesh.vertices.add(seVertex);
            }
            for (int i = 0; i < renderObjMesh.getNumFaces(); ++i) {
                ObjFace face = renderObjMesh.getFace(i);
                mesh.faces.add(new Face(new int[] {face.getVertexIndex(0), face.getVertexIndex(1), face.getVertexIndex(2)}));
            }
            mesh.generateNormals();
            mesh.distinct();
            if (atlasManager != null) atlasManager.applyToMesh(mesh);
            model.append(mesh);
        }
        return model;
    }

    private static Map<String, Mtl> loadMaterials(ResourceManager resourceManager, Obj srcObj, ResourceLocation objLocation) throws IOException {
        Map<String, Mtl> materials = new HashMap<>();
        for (String mtlFileName : srcObj.getMtlFileNames()) {
            List<Mtl> srcMtls = MtlReader.read(Utilities.getInputStream(resourceManager.getResource(ResourceUtil.resolveRelativePath(objLocation, mtlFileName, ".mtl"))));
            for (Mtl mtl : srcMtls) {
                materials.put(mtl.getName(), mtl);
            }
        }
        return materials;
    }

    private static class ZeroFloatTuple implements FloatTuple {

        public static final ZeroFloatTuple ZERO2 = new ZeroFloatTuple(2);
        public static final ZeroFloatTuple ZERO3 = new ZeroFloatTuple(3);

        private final int dimensions;

        public ZeroFloatTuple(int dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public float getX() {
            return 0;
        }
        @Override
        public float getY() {
            return 0;
        }
        @Override
        public float getZ() {
            return 0;
        }
        @Override
        public float getW() {
            return 0;
        }
        @Override
        public float get(int index) {
            return 0;
        }
        @Override
        public int getDimensions() {
            return dimensions;
        }
    }

}