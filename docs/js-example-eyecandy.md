# 装饰物件渲染案例

以下代码展示了如何用 DynamicModelHolder 加载模型并更换贴图，同时打印当前方块的坐标、在消息栏显示Hellow world!。您可以复制后按需修改。

```javascript
//存储更换的贴图
var texture = Resources.idRelative("block.png");

function create(ctx, state, block) {
    // 注意在函数中Resources.idRelative(resources: String)不可用 只能使用 Resources.id(resources: String)
    let rawModel = ModelManager.loadRawModel(Resources.manager(), Resources.id("mtrsteamloco:eyecandies/block.obj"), null);
    state.dynamicModelHolder= new DynamicModelHolder();
    state.dynamicModelHolder.uploadLater(rawModel);
    //注意这里DynamicModelHolder.getUploadedModel()不可用,要对它的ModelCluster进行操作需要等下一次主程序调用
}

function render(ctx, state, block) {
    //绘制模型
    ctx.drawModel(state.dynamicModelHolder, null);
    //获得ModelCluster
    let model = state.dynamicModelHolder.getUploadedModel();
    //更换贴图
    model.replaceAllTexture(texture);
    //打印坐标
    blockPos = block.getWorldPosVector3f();
    ctx.setDebugInfo("Block at","x:"+blockPos.x()+",y:"+blockPos.y()+",z:"+blockPos.z());
    //打印"Hellow world!"
    MinecraftClient.displayMessage("Hellow world!",true);
}

```