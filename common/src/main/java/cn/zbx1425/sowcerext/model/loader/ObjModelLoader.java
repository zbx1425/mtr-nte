package cn.zbx1425.sowcerext.model.loader;

import cn.zbx1425.sowcer.batch.BatchProp;
import cn.zbx1425.sowcer.model.Mesh;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.object.IndexBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.Face;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.Vertex;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.math.Vector3f;
import de.javagl.obj.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

public class ObjModelLoader {

    public static Model loadModel(ResourceManager resourceManager, ResourceLocation objLocation, VertAttrMapping mapping) throws IOException {
        Obj srcObj = ObjReader.read(resourceManager.getResource(objLocation).getInputStream());
        Map<String, Obj> mtlObjs = ObjSplitting.splitByMaterialGroups(srcObj);
        Model model = new Model();
        for (Map.Entry<String, Obj> entry : mtlObjs.entrySet()) {
            Obj renderObjMesh = ObjUtils.convertToRenderable(entry.getValue());
            String parentDirName = new File(objLocation.getPath()).getParent();
            if (parentDirName == null) parentDirName = "";
            String texFileName = entry.getKey().toLowerCase(Locale.ROOT);
            if (!texFileName.endsWith(".png")) texFileName += ".png";
            BatchProp batchProp = new BatchProp("rendertype_entity_cutout",
                    new ResourceLocation(objLocation.getNamespace(), parentDirName + "/" + texFileName));

            RawMesh mesh = new RawMesh(batchProp);
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
            if (!mesh.checkVertIndex()) throw new IndexOutOfBoundsException("Invalid vertex index in OBJ model.");
            mesh.distinct();
            if (!mesh.checkVertIndex()) throw new AssertionError("Bad VertIndex after mesh distinct");
            if (RawMesh.shouldWriteVertBuf(mapping, VertAttrType.NORMAL)) mesh.generateNormals();
            model.meshList.add(mesh.upload(mapping));
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
