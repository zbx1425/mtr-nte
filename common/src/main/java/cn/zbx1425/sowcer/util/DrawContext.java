package cn.zbx1425.sowcer.util;

public class DrawContext {

    public boolean drawWithBlaze = false;
    public boolean sortTranslucentFaces = false;

    public int drawCallCount = 0;
    public int batchCount = 0;
    public int singleFaceCount = 0;
    public int instancedFaceCount = 0;
    public int blazeFaceCount;

    private int drawCallCountCF = 0;
    private int batchCountCF = 0;
    private int singleFaceCountCF = 0;
    private int instancedFaceCountCF = 0;
    private int blazeFaceCountCF = 0;

    public void resetFrameProfiler() {
        drawCallCount = drawCallCountCF;
        batchCount = batchCountCF;
        singleFaceCount = singleFaceCountCF;
        instancedFaceCount = instancedFaceCountCF;
        blazeFaceCount = blazeFaceCountCF;
        drawCallCountCF = 0;
        batchCountCF = 0;
        singleFaceCountCF = 0;
        instancedFaceCountCF = 0;
        blazeFaceCountCF = 0;
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

    public void recordBlazeAction(int faceCount) {
        blazeFaceCountCF += faceCount;
    }
}
