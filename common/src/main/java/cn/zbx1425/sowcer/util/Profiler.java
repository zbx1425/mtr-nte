package cn.zbx1425.sowcer.util;

public class Profiler {

    public int drawCallCount = 0;
    public int batchCount = 0;
    public int singleFaceCount = 0;
    public int instancedFaceCount = 0;

    private int drawCallCountCF = 0;
    private int batchCountCF = 0;
    private int singleFaceCountCF = 0;
    private int instancedFaceCountCF = 0;

    public void beginFrame() {
        drawCallCount = drawCallCountCF;
        batchCount = batchCountCF;
        singleFaceCount = singleFaceCountCF;
        instancedFaceCount = instancedFaceCountCF;
        drawCallCountCF = 0;
        batchCountCF = 0;
        singleFaceCountCF = 0;
        instancedFaceCountCF = 0;
    }

    public void recordBatches(int batchCount) {
        batchCountCF += batchCount;
    }

    public void recordDrawCall(int faceCount, boolean instanced) {
        drawCallCountCF++;
        if (instanced) {
            instancedFaceCountCF += faceCount;
        } else {
            singleFaceCountCF += faceCount;
        }
    }
}
