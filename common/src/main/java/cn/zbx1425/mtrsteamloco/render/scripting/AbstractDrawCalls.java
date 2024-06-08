package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.render.scripting.util.DynamicModelHolder;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public abstract class AbstractDrawCalls {

    public static class ClusterDrawCall {
        public ModelCluster model;
        public DynamicModelHolder modelHolder;
        public Matrix4f pose;

        public ClusterDrawCall(ModelCluster model, Matrix4f pose) {
            this.model = model;
            this.pose = pose;
        }

        public ClusterDrawCall(DynamicModelHolder model, Matrix4f pose) {
            this.modelHolder = model;
            this.pose = pose;
        }

        public void commit(DrawScheduler drawScheduler, Matrix4f basePose, int light) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(pose);
            if (model != null) {
                drawScheduler.enqueue(model, finalPose, light);
            } else {
                ModelCluster model = modelHolder.getUploadedModel();
                if (model != null) {
                    drawScheduler.enqueue(model, finalPose, light);
                }
            }
        }
    }

    public static class PlaySoundCall {
        public SoundEvent sound;
        public Vector3f position;
        public float volume;
        public float pitch;

        public PlaySoundCall(SoundEvent sound, Vector3f position, float volume, float pitch) {
            this.sound = sound;
            this.position = position;
            this.volume = volume;
            this.pitch = pitch;
        }

        public void commit(ClientLevel level, Matrix4f worldPose) {
            Vector3f worldPos = worldPose.transform(position);
            level.playLocalSound(worldPos.x(), worldPos.y(), worldPos.z(),
                    sound, SoundSource.BLOCKS,
                    volume, pitch, false);
        }
    }
}
