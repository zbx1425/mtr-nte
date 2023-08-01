package cn.zbx1425.mtrsteamloco.render.scripting.eyecandy;

import cn.zbx1425.mtrsteamloco.render.scripting.train.TrainDrawCalls;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

public class EyeCandyDrawCalls {

    private final List<ClusterDrawCall> drawList = new ArrayList<>();
    private final List<PlaySoundCall> soundList = new ArrayList<>();

    public void addModel(ModelCluster model, Matrix4f pose) {
        drawList.add(new ClusterDrawCall(model, pose));
    }

    public void addSound(SoundEvent sound, float volume, float pitch) {
        soundList.add(new PlaySoundCall(sound, volume, pitch));
    }

    public void commit(DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : drawList) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        for (PlaySoundCall playSoundCall : soundList) {
            Vector3f worldPos = basePose.transform(Vector3f.ZERO);
            level.playLocalSound(worldPos.x(), worldPos.y(), worldPos.z(),
                    playSoundCall.sound, SoundSource.BLOCKS,
                    playSoundCall.volume, playSoundCall.pitch, false);
        }
    }

    public void reset() {
        drawList.clear();
        soundList.clear();
    }

    private static class ClusterDrawCall {
        public ModelCluster model;
        public Matrix4f pose;

        public ClusterDrawCall(ModelCluster model, Matrix4f pose) {
            this.model = model;
            this.pose = pose;
        }
    }

    private static class PlaySoundCall {
        public SoundEvent sound;
        public float volume;
        public float pitch;

        public PlaySoundCall(SoundEvent sound, float volume, float pitch) {
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
