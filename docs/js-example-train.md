# 列车渲染样例

### 基于另一款已有的 MTR 车型

您可以直接搬用一款已有车型的外观，然后用 JS 再额外添加一些新的渲染逻辑。

### 用 JS 自由控制所有渲染过程

以下代码实现了加载 OBJ 模型，按照需要选择其中的分组显示，达到类似于 MTR 原版列车的显示效果。欢迎复制后修改。

它所使用的模型中有 `body`、`head`、`end`、`headlight`、`taillight` 分组，其中 `head`、`headlight`、`taillight` 在 Z- 方向，`end` 在 Z+ 方向；还有 `doorXNZN`、`doorXNZP`、`doorXPZN`、`doorXPZP`、`doorlightXN`、`doorlightXP` 分组是各组门扇和门灯。

```javascript
// 将 train.obj 里的各个分组各自加载，成为一个 RawModel 的 Map
var rawModels = ModelManager.loadPartedRawModel(Resources.manager(), Resources.idRelative("train.obj"), null);
// 上传所有的 RawModel。我这里写了一个 uploadPartedModels 来逐个上传 Map 里的每一个分组模型
var models = uploadPartedModels(rawModels);
// 用这个贴图作为连接处的贴图
var idTexConnector = Resources.idRelative("connector.png");

// 没有需要 createTrain 或 disposeTrain 时处理的逻辑，可以不写

function renderTrain(ctx, state, train) {
  let matrices = new Matrices();
  
  // 依次处理每节车厢
  for (i = 0; i < train.trainCars(); i++) {
    // 按需绘制车头、尾端，以及头灯或尾灯
    matrices.pushPose();
    if (train.trainCars() == 1) { // 总共就一节，显示一个双头车厢
      matrices.rotateY(Math.PI);
      ctx.drawCarModel(models["head"], i, matrices);
      ctx.drawCarModel(train.isReversed() ? models["taillight"] : models["headlight"], i, matrices);
      matrices.popPushPose();
      ctx.drawCarModel(models["head"], i, matrices);
      ctx.drawCarModel(train.isReversed() ? models["headlight"] : models["taillight"], i, matrices);
    } else if (i == 0) { // 是第一节，车头应该在 Z+ 方向，尾端应该在 Z- 方向，所以旋转 180°
      matrices.rotateY(Math.PI);
      ctx.drawCarModel(models["head"], i, matrices);
      ctx.drawCarModel(train.isReversed() ? models["headlight"] : models["taillight"], i, matrices);
      ctx.drawCarModel(models["end"], i, matrices);
      matrices.popPushPose();
    } else if (i == train.trainCars() - 1) { // 是最后一节，车头应该在 Z- 方向，尾端应该在 Z+ 方向，所以不旋转
      ctx.drawCarModel(models["head"], i, matrices);
      ctx.drawCarModel(train.isReversed() ? models["taillight"] : models["headlight"], i, matrices);
      ctx.drawCarModel(models["end"], i, matrices);
    } else { // 是中间车，显示两个尾端
      matrices.rotateY(Math.PI);
      ctx.drawCarModel(models["end"], i, matrices);
      matrices.popPushPose();
      ctx.drawCarModel(models["end"], i, matrices);
    }
    matrices.popPose();
    
    // 绘制车体
    ctx.drawCarModel(models["body"], i, null);
      
    // 绘制车门开启指示灯
    if (train.doorLeftOpen[i] && train.doorValue() > 0) {
        ctx.drawCarModel(models["doorlightXP"], i, null);
    }
    if (train.doorRightOpen[i] && train.doorValue() > 0) {
        ctx.drawCarModel(models["doorlightXN"], i, null);
    }
    
    // 绘制车门
    let doorX = smoothEnds(0, 0.81, 0, 0.5, train.doorValue());
    let doorXP = train.doorLeftOpen[i] ? doorX * 0.81 : 0;
    let doorXN = train.doorRightOpen[i] ? doorX * 0.81 : 0;
    matrices.pushPose();
    matrices.translate(0, 0, -doorXN);
    ctx.drawCarModel(models["doorXNZN"], i, matrices);
    matrices.popPushPose();
    matrices.translate(0, 0, doorXN);
    ctx.drawCarModel(models["doorXNZP"], i, matrices);
    matrices.popPushPose();
    matrices.translate(0, 0, -doorXP);
    ctx.drawCarModel(models["doorXPZN"], i, matrices);
    matrices.popPushPose();
    matrices.translate(0, 0, doorXP);
    ctx.drawCarModel(models["doorXPZP"], i, matrices);
    matrices.popPose();
  }
  
  // 绘制连接处
  for (i = 0; i < train.trainCars() - 1; i++) {
    ctx.drawConnStretchTexture(idTexConnector, i);
  }
}

// 把 loadRawModels 得到的 Map 里的各个内容分别上传
function uploadPartedModels(rawModels) {
  let result = {};
  for (it = rawModels.entrySet().iterator(); it.hasNext(); ) {
    entry = it.next();
    entry.getValue().applyUVMirror(false, true);
    result[entry.getKey()] = ModelManager.uploadVertArrays(entry.getValue());
  }
  return result;
}

// 从 MTR 里面抄的车门缓动
function smoothEnds(startValue, endValue, startTime, endTime, time) {
    if (time < startTime) return startValue;
    if (time > endTime) return endValue;
    let timeChange = endTime - startTime;
    let valueChange = endValue - startValue;
    return valueChange * (1 - Math.cos(Math.PI * (time - startTime) / timeChange)) / 2 + startValue;
}

// 文件结束

```

