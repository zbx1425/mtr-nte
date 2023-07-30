package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.reuse.DrawScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainDrawCalls {

    private final List<ClusterDrawCall>[] carLists;
    private final List<ClusterDrawCall>[] connectionLists;

    @SuppressWarnings("unchecked")
    public TrainDrawCalls(int carCount) {
        carLists = new List[carCount];
        Arrays.setAll(carLists, ignored -> new ArrayList<>());
        connectionLists = new List[carCount - 1];
        Arrays.setAll(connectionLists, ignored -> new ArrayList<>());
    }

    public void enqueueCar(int car, ModelCluster model, Matrix4f pose) {
        carLists[car].add(new ClusterDrawCall(model, pose));
    }

    public void commitCar(int car, DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : carLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
    }

    public void enqueueConnection(int car, ModelCluster model, Matrix4f pose) {
        connectionLists[car].add(new ClusterDrawCall(model, pose));
    }

    public void commitConnection(int car, DrawScheduler drawScheduler, Matrix4f basePose, int light) {
        for (ClusterDrawCall clusterDrawCall : connectionLists[car]) {
            Matrix4f finalPose = basePose.copy();
            finalPose.multiply(clusterDrawCall.pose);
            drawScheduler.enqueue(clusterDrawCall.model, finalPose, light);
        }
    }

    public void reset() {
        for (List<ClusterDrawCall> list : carLists) list.clear();
        for (List<ClusterDrawCall> list : connectionLists) list.clear();
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
