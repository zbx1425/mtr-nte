package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainDrawCalls {

    private final List<ClusterDrawCall>[] carDrawLists;
    private final List<ClusterDrawCall>[] connDrawLists;
    private final List<PlayCarSoundCall>[] carSoundLists;

    @SuppressWarnings("unchecked")
    public TrainDrawCalls(int carCount) {
        carDrawLists = new List[carCount];
        Arrays.setAll(carDrawLists, ignored -> new ArrayList<>());
        connDrawLists = new List[carCount - 1];
        Arrays.setAll(connDrawLists, ignored -> new ArrayList<>());
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

    public void commitConn(int car, DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : connDrawLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
    }

    public void reset() {
        for (List<ClusterDrawCall> list : carDrawLists) list.clear();
        for (List<ClusterDrawCall> list : connDrawLists) list.clear();
        for (List<PlayCarSoundCall> list : carSoundLists) list.clear();
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
