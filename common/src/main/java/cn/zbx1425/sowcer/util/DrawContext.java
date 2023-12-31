package cn.zbx1425.sowcer.util;

import cn.zbx1425.sowcer.batch.BatchManager;

import java.util.ArrayList;
import java.util.List;

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

    public List<String> debugInfo = new ArrayList<>();
    private List<String> debugInfoCF = new ArrayList<>();

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
#if DEBUG
        debugInfo = debugInfoCF;
        debugInfoCF = new ArrayList<>();
#endif
    }

    public void recordBatches(int batchCount) {
        batchCountCF += batchCount;
    }

    public void recordDrawCall(BatchManager.RenderCall renderCall) {
        drawCallCountCF++;
        if (renderCall.vertArray.instanceBuf != null) {
            instancedFaceCountCF += renderCall.vertArray.getFaceCount();
        } else {
            singleFaceCountCF += renderCall.vertArray.getFaceCount();
#if DEBUG
            debugInfoCF.add(String.format("%s: %d", renderCall.vertArray.materialProp.toString(), renderCall.vertArray.getFaceCount()));
#endif
        }
    }

    public void recordBlazeAction(int faceCount) {
        blazeFaceCountCF += faceCount;
    }
}
