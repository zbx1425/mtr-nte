package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainDrawCalls {

    private final List<ClusterDrawCall>[] carLists;

    @SuppressWarnings("unchecked")
    public TrainDrawCalls(int carCount) {
        carLists = new List[carCount];
        Arrays.setAll(carLists, ignored -> new ArrayList<>());
    }

    public void enqueue(int car, ModelCluster model, Matrix4f pose) {
        carLists[car].add(new ClusterDrawCall(model, pose));
    }

    public void commit(int car, DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : carLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
    }

    public void reset() {
        for (List<ClusterDrawCall> list : carLists) {
            list.clear();
        }
    }

    private static class ClusterDrawCall {
        public ModelCluster model;
        public Matrix4f pose;

        public ClusterDrawCall(ModelCluster model, Matrix4f pose) {
            this.model = model;
            this.pose = pose;
        }
    }
}
