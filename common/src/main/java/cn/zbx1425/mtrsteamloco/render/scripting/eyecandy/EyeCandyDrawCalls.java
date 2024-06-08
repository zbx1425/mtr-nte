package cn.zbx1425.mtrsteamloco.render.scripting.eyecandy;

import cn.zbx1425.mtrsteamloco.render.scripting.AbstractDrawCalls;
import cn.zbx1425.mtrsteamloco.render.scripting.util.DynamicModelHolder;
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

public class EyeCandyDrawCalls extends AbstractDrawCalls {

    private final List<ClusterDrawCall> drawList = new ArrayList<>();
    private final List<PlaySoundCall> soundList = new ArrayList<>();

    public void addModel(ModelCluster model, Matrix4f pose) {
        drawList.add(new ClusterDrawCall(model, pose));
    }

    public void addModel(DynamicModelHolder model, Matrix4f pose) {
        drawList.add(new ClusterDrawCall(model, pose));
    }

    public void addSound(SoundEvent sound, float volume, float pitch) {
        soundList.add(new PlaySoundCall(sound, Vector3f.ZERO, volume, pitch));
    }

    public void commit(DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : drawList) {
            clusterDrawCall.commit(drawScheduler, basePose, light);
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        for (PlaySoundCall playSoundCall : soundList) {
            playSoundCall.commit(level, basePose);
        }
    }

    public void reset() {
        drawList.clear();
        soundList.clear();
    }
}
