package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.mtrsteamloco.BuildConfig;
import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcerext.reuse.AtlasManager;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import de.javagl.obj.*;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ObjModelLoader {

    public static RawModel loadModel(ResourceManager resourceManager, ResourceLocation objLocation, AtlasManager atlasManager) throws IOException {
        Obj srcObj = ObjReader.read(Utilities.getInputStream(resourceManager.getResource(objLocation)));
        Map<String, Mtl> materials = loadMaterials(resourceManager, srcObj, objLocation);

        RawModel model = loadModel(srcObj, objLocation, materials, atlasManager);
        model.sourceLocation = objLocation;
        return model;
    }

    public static Map<String, RawModel> loadModels(ResourceManager resourceManager, ResourceLocation objLocation, AtlasManager atlasManager) throws IOException {
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

    public static Map<String, RawModel> loadExternalModels(String path, AtlasManager atlasManager) throws IOException {
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

    private static RawModel loadModel(Obj srcObj, ResourceLocation objLocation, Map<String, Mtl> materials, AtlasManager atlasManager) {
        Map<String, Obj> mtlObjs = ObjSplitting.splitByMaterialGroups(srcObj);
        RawModel model = new RawModel();
        for (Map.Entry<String, Obj> entry : mtlObjs.entrySet()) {
            if (entry.getValue().getNumFaces() == 0) continue;

            Map<String, String> materialOptions = splitMaterialOptions(entry.getKey());
            String materialGroupName = materialOptions.get("");
            String meshRenderType = materialOptions.getOrDefault("#", "exterior").toLowerCase(Locale.ROOT);
            boolean flipV = materialOptions.getOrDefault("flipv", "0").equals("1");
            MaterialProp materialProp = new MaterialProp();

            if ((materials != null && materials.size() > 0) && objLocation != null) {
                Mtl objMaterial = materials.getOrDefault(entry.getKey(), null);
                if (objMaterial != null) {
                    if (!StringUtils.isEmpty(objMaterial.getMapKd())) {
                        materialProp.texture = ResourceUtil.resolveRelativePath(objLocation, objMaterial.getMapKd(), ".png");
                    }
                    FloatTuple color = objMaterial.getKd();
                    materialProp.attrState.setColor((int)(color.getX() * 255), (int)(color.getY() * 255), (int)(color.getZ() * 255), (int)(objMaterial.getD() * 255));
                }
            } else if (objLocation != null) {
                materialProp.texture = materialGroupName.equals("_") ? null : ResourceUtil.resolveRelativePath(objLocation, materialGroupName, ".png");
                materialProp.attrState.setColor(255, 255, 255, 255);
            } else {
                materialProp.texture = null;
                materialProp.attrState.setColor(255, 255, 255, 255);
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
                seVertex.v = flipV ? 1 - uv.getY() : uv.getY();
                mesh.vertices.add(seVertex);
            }
            for (int i = 0; i < renderObjMesh.getNumFaces(); ++i) {
                ObjFace face = renderObjMesh.getFace(i);
                mesh.faces.add(new Face(new int[] {face.getVertexIndex(0), face.getVertexIndex(1), face.getVertexIndex(2)}));
            }
            if (atlasManager != null) atlasManager.applyToMesh(mesh);
            mesh.validateVertIndex();
            model.append(mesh);
        }

        model.generateNormals();
        model.distinct();
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

    private static Map<String, String> splitMaterialOptions(String src) {
        HashMap<String, String> result = new HashMap<>();
        String[] majorParts = src.split("#", 2);
        result.put("", majorParts[0]);
        if (majorParts.length > 1) {
            for (String minorPart : majorParts[1].split(",")) {
                String[] tokens = minorPart.split("=", 2);
                if (tokens.length > 1) {
                    result.put(tokens[0], tokens[1]);
                } else if (!result.containsKey("#")) {
                    result.put("#", tokens[0]);
                } else {
                    result.put(tokens[0].toLowerCase(Locale.ROOT), "1");
                }
            }
        }
        return result;
    }


    public static void saveModels(Map<String, RawModel> models, Path objOutputFile, Path mtlOutputFile, boolean withNormal) throws IOException {
        String mtlFileName = mtlOutputFile.getFileName().toString();
        try (PrintWriter objFile = new PrintWriter(Files.newOutputStream(objOutputFile));
             PrintWriter mtlFile = new PrintWriter(Files.newOutputStream(mtlOutputFile))) {
            objFile.println("# Generated by MTR-NTE " + BuildConfig.MOD_VERSION);
            mtlFile.println("# Generated by MTR-NTE " + BuildConfig.MOD_VERSION);
            Set<String> writtenMaterials = new HashSet<>();
            int vertOffset = 1;
            objFile.println("mtllib " + mtlFileName);
            for (Map.Entry<String, RawModel> groupEntry : models.entrySet()) {
                objFile.println("g " + groupEntry.getKey());
                for (Map.Entry<MaterialProp, RawMesh> matEntry : groupEntry.getValue().meshList.entrySet()) {
                    String textureName = matEntry.getKey().texture == null ? "_"
                            : FilenameUtils.getBaseName(matEntry.getKey().texture.getPath());
                    String renderType;
                    switch (matEntry.getKey().shaderName) {
                        case "rendertype_entity_cutout" ->
                                renderType = matEntry.getKey().attrState.lightmapUV != null
                                        ? "interior" : "exterior";
                        case "rendertype_entity_translucent_cull" ->
                                renderType = matEntry.getKey().attrState.lightmapUV != null
                                        ? "interiortranslucent" : "exteriortranslucent";
                        case "rendertype_beacon_beam" ->
                                renderType = matEntry.getKey().translucent
                                        ? "lighttranslucent" : "light";
                        default -> renderType = "exterior";
                    }

                    if (!writtenMaterials.contains(textureName + "#" + renderType)) {
                        writtenMaterials.add(textureName + "#" + renderType);
                        mtlFile.println("newmtl " + textureName + "#" + renderType);
                        mtlFile.println("Kd 1.0 1.0 1.0");
                        mtlFile.println("map_Kd " + textureName + ".png");
                    }

                    objFile.println("usemtl " + textureName + "#" + renderType);
                    RawMesh mesh = matEntry.getValue();
                    for (Vertex vertex : mesh.vertices) {
                        objFile.printf("v %f %f %f\n", vertex.position.x(), vertex.position.y(), vertex.position.z());
                        if (withNormal) {
                            objFile.printf("vn %f %f %f\n", vertex.normal.x(), vertex.normal.y(), vertex.normal.z());
                        }
                        objFile.printf("vt %f %f\n", vertex.u, 1 - vertex.v);
                    }
                    for (Face face : mesh.faces) {
                        if (withNormal) {
                            objFile.print("f");
                            for (int vertex : face.vertices) {
                                objFile.printf(" %d/%d/%d", vertex + vertOffset, vertex + vertOffset, vertex + vertOffset);
                            }
                            objFile.println();
                        } else {
                            objFile.print("f");
                            for (int vertex : face.vertices) {
                                objFile.printf(" %d/%d", vertex + vertOffset, vertex + vertOffset);
                            }
                            objFile.println();
                        }
                    }
                    vertOffset += mesh.vertices.size();
                }
            }
        }
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