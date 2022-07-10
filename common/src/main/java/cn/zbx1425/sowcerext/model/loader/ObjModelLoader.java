package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.sowcer.batch.MaterialProp;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.mojang.math.Vector3f;
import de.javagl.obj.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class ObjModelLoader {

    public static RawModel loadModel(ResourceManager resourceManager, ResourceLocation objLocation) throws IOException {
        Obj srcObj = ObjReader.read(resourceManager.getResource(objLocation).getInputStream());
        Map<String, Obj> mtlObjs = ObjSplitting.splitByMaterialGroups(srcObj);
        RawModel model = new RawModel();
        for (Map.Entry<String, Obj> entry : mtlObjs.entrySet()) {
            Obj renderObjMesh = ObjUtils.convertToRenderable(entry.getValue());
            MaterialProp materialProp = new MaterialProp("rendertype_entity_cutout",
                    ResourceUtil.resolveRelativePath(objLocation, entry.getKey(), ".png"));

            RawMesh mesh = new RawMesh(materialProp);
            for (int i = 0; i < renderObjMesh.getNumVertices(); ++i) {
                FloatTuple pos, normal, uv;
                pos = renderObjMesh.getVertex(i);
                if (i < renderObjMesh.getNumNormals()) {
                    normal = renderObjMesh.getNormal(i);
                } else {
                    normal = new ZeroFloatTuple(3);
                }
                if (i < renderObjMesh.getNumTexCoords()) {
                    uv = renderObjMesh.getTexCoord(i);
                } else {
                    uv = new ZeroFloatTuple(2);
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
            model.append(mesh);
        }
        return model;
    }

    private static class ZeroFloatTuple implements FloatTuple {

        private int dimensions;

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
