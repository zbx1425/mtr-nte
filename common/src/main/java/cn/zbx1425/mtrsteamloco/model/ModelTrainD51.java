package cn.zbx1425.mtrsteamloco.model;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import mtr.model.ModelTrainBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModelTrainD51 extends ModelTrainBase {

    public static Model glModel;
    public static VertArrays glVaos;

    public static void initGlModel(ResourceManager resourceManager) {
        if (glVaos != null) return;

        VertAttrMapping mapping = new VertAttrMapping.Builder()
                .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
                .set(VertAttrType.COLOR, VertAttrSrc.MATERIAL)
                .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
                .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.ENQUEUE)
                .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
                .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.INSTANCE_BUF)
                .build();

        int cubeSize = 20;
        ByteBuffer instanceBuf = MemoryTracker.create(cubeSize * cubeSize * cubeSize * 16 * 4);
        for (int x = -cubeSize / 2; x < cubeSize / 2; ++x) {
            for (int y = 0; y < cubeSize; ++y) {
                for (int z = -cubeSize / 2; z < cubeSize / 2; ++z) {
                    ByteBuffer matrixBuf = ByteBuffer.allocate(16 * 4);
                    matrixBuf.order(ByteOrder.nativeOrder());
                    Matrix4f mat = new Matrix4f();
                    mat.setIdentity();
                    mat.multiplyWithTranslation(x * 4, y * 5, z * 22);
                    mat.store(matrixBuf.asFloatBuffer());
                    matrixBuf.clear();
                    instanceBuf.put(matrixBuf);
                }
            }
        }
        InstanceBuf instanceBufObj = new InstanceBuf(cubeSize * cubeSize * cubeSize);
        instanceBufObj.upload(instanceBuf);

        try {
            Main.LOGGER.info("Uploading VBO");
            glModel = ObjModelLoader.loadModel(resourceManager, new ResourceLocation("mtrsteamloco:models/dk3body.obj")).upload(mapping);
            Main.LOGGER.info("Uploading VAO");
            glVaos = VertArrays.createAll(glModel, mapping, instanceBufObj);
            Main.LOGGER.info("Finish");
        } catch (IOException e) {
            Main.LOGGER.error(e);
        }
    }

    @Override
    protected void render(PoseStack matrices, VertexConsumer vertices, RenderStage renderStage, int light, float doorLeftX, float doorRightX, float doorLeftZ, float doorRightZ, int currentCar, int trainCars, boolean head1IsFront, boolean renderDetails) {
        if (glVaos == null) return;
        if (renderStage == RenderStage.EXTERIOR) {
            final Matrix4f lastPose = matrices.last().pose().copy();
            lastPose.setIdentity();
            // lastPose.multiply(Vector3f.XP.rotation((float) Math.PI));
            // lastPose.multiplyWithTranslation(0, -1, 0);

        }
    }

    @Override
    protected float getDoorAnimationX(float v, boolean b) {
        return 0;
    }

    @Override
    protected float getDoorAnimationZ(float v, boolean b) {
        return 0;
    }
}
