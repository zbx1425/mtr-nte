package cn.zbx1425.mtrsteamloco.render.scripting.train;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.client.IDrawing;
import mtr.render.MoreRenderLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainDrawCalls {

    private final List<ClusterDrawCall>[] carDrawLists;
    private final List<ClusterDrawCall>[] connDrawLists;
    private final ResourceLocation[] connStretchTextures;
    private final List<PlayCarSoundCall>[] carSoundLists;

    @SuppressWarnings("unchecked")
    public TrainDrawCalls(int carCount) {
        carDrawLists = new List[carCount];
        Arrays.setAll(carDrawLists, ignored -> new ArrayList<>());
        connDrawLists = new List[carCount - 1];
        Arrays.setAll(connDrawLists, ignored -> new ArrayList<>());
        connStretchTextures = new ResourceLocation[carCount - 1];
        carSoundLists = new List[carCount];
        Arrays.setAll(carSoundLists, ignored -> new ArrayList<>());
    }

    public void addCarModel(int car, ModelCluster model, Matrix4f pose) {
        carDrawLists[car].add(new ClusterDrawCall(model, pose));
    }

    public void addCarSound(int car, SoundEvent sound, Vector3f position, float volume, float pitch) {
        carSoundLists[car].add(new PlayCarSoundCall(sound, position, volume, pitch));
    }

    public void commitCar(int car, DrawScheduler drawScheduler, Matrix4f basePose, Matrix4f worldPose, int light) {
        for (ClusterDrawCall clusterDrawCall : carDrawLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        for (PlayCarSoundCall playCarSoundCall : carSoundLists[car]) {
            Vector3f worldPos = worldPose.transform(playCarSoundCall.position);
            level.playLocalSound(worldPos.x(), worldPos.y(), worldPos.z(),
                    playCarSoundCall.sound, SoundSource.BLOCKS,
                    playCarSoundCall.volume, playCarSoundCall.pitch, false);
        }
    }

    public void addConnModel(int car, ModelCluster model, Matrix4f pose) {
        connDrawLists[car].add(new ClusterDrawCall(model, pose));
    }

    public void drawConnStretchTexture(int car, ResourceLocation texture) {
        connStretchTextures[car] = texture;
    }

    public void commitConn(int car, DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : connDrawLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
    }

    public void commitConnImmediate(int car, PoseStack matrices, MultiBufferSource vertexConsumers, Vec3 prevPos1, Vec3 prevPos2, Vec3 prevPos3, Vec3 prevPos4,
                                    Vec3 thisPos1, Vec3 thisPos2, Vec3 thisPos3, Vec3 thisPos4, int light) {
        if (connStretchTextures[car] != null) {
            VertexConsumer vertexConsumerExterior = vertexConsumers.getBuffer(MoreRenderLayers.getExterior(connStretchTextures[car]));
            drawTexture(matrices, vertexConsumerExterior, thisPos2, prevPos3, prevPos4, thisPos1, 0, 0, 0.5f, 0.5f, light);
            drawTexture(matrices, vertexConsumerExterior, prevPos2, thisPos3, thisPos4, prevPos1, 0, 0, 0.5f, 0.5f, light);
            drawTexture(matrices, vertexConsumerExterior, prevPos3, thisPos2, thisPos3, prevPos2, 0, 0, 0.5f, 0.5f, light);
            drawTexture(matrices, vertexConsumerExterior, prevPos1, thisPos4, thisPos1, prevPos4, 0, 0, 0.5f, 0.5f, light);
            int lightOnLevel = LightTexture.FULL_BRIGHT;
            VertexConsumer vertexConsumerSide = vertexConsumers.getBuffer(MoreRenderLayers.getInterior(connStretchTextures[car]));
            drawTexture(matrices, vertexConsumerSide, thisPos3, prevPos2, prevPos1, thisPos4, 0.5f, 0, 1f, 0.5f, lightOnLevel);
            drawTexture(matrices, vertexConsumerSide, prevPos3, thisPos2, thisPos1, prevPos4, 0.5f, 0, 1f, 0.5f, lightOnLevel);
            drawTexture(matrices, vertexConsumerSide, prevPos2, thisPos3, thisPos2, prevPos3, 0, 0.5f, 0.5f, 1, lightOnLevel);
            drawTexture(matrices, vertexConsumerSide, prevPos4, thisPos1, thisPos4, prevPos1, 0.5f, 0.5f, 1, 1, lightOnLevel);
        }
    }

    private static void drawTexture(PoseStack matrices, VertexConsumer vertexConsumer, Vec3 pos1, Vec3 pos2, Vec3 pos3, Vec3 pos4, float u1, float v1, float u2, float v2, int light) {
        IDrawing.drawTexture(matrices, vertexConsumer,
                (float)pos1.x, (float)pos1.y, (float)pos1.z, (float)pos2.x, (float)pos2.y, (float)pos2.z,
                (float)pos3.x, (float)pos3.y, (float)pos3.z, (float)pos4.x, (float)pos4.y, (float)pos4.z,
                u1, v1, u2, v2, Direction.UP, -1, light);
    }

    public void reset() {
        for (List<ClusterDrawCall> list : carDrawLists) list.clear();
        for (List<ClusterDrawCall> list : connDrawLists) list.clear();
        for (List<PlayCarSoundCall> list : carSoundLists) list.clear();
        Arrays.fill(connStretchTextures, null);
    }

    private static class ClusterDrawCall {
        public ModelCluster model;
        public Matrix4f pose;

        public ClusterDrawCall(ModelCluster model, Matrix4f pose) {
            this.model = model;
            this.pose = pose;
        }
    }

    private static class PlayCarSoundCall {
        public SoundEvent sound;
        public Vector3f position;
        public float volume;
        public float pitch;

        public PlayCarSoundCall(SoundEvent sound, Vector3f position, float volume, float pitch) {
            this.sound = sound;
            this.position = position;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
