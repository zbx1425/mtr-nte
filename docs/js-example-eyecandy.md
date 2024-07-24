# 装饰物件渲染案例

以下代码实现了在游戏中加载模型，并为每个物件独立的进行随机更换贴图和缩放模型，最终在绘制该模型的功能。

```javascript

//存储更换的贴图
var textures = [];
textures.push(Resources.idRelative("texture1.png"));
textures.push(Resources.idRelative("texture2.png"));
textures.push(Resources.idRelative("texture3.png"));
textures.push(Resources.idRelative("texture4.png"));

function create(ctx, state, block) {
    // 注意在函数中Resources.idRelative(resources: String)不可用 只能使用 Resources.id(resources: String)
    let rawModel = ModelManager.loadRawModel(Resources.manager(), Resources.id("mtrsteamloco:eyecandies/block.obj"), null);
    //随机获得0-3的索引
    let textureIndex = Math.floor(Math.random()*textures.length);
    //替换模型的贴图
    rawModel.replaceAllTexture(textures[textureIndex]);
    //随机缩放
    let scale = Math.random()*0.4 + 0.8;
    //应用随机缩放
    rawModel.applyScale(scale,scale,scale);
    //创建动态模型
    state.dynamicModelHolder= new DynamicModelHolder();
    state.dynamicModelHolder.uploadLater(rawModel);
    //注意这里DynamicModelHolder.getUploadedModel()不可用,要对它的ModelCluster进行操作需要等下一次主程序调用
}

function render(ctx, state, block) {
    //绘制模型
    ctx.drawModel(state.dynamicModelHolder, null);
}

```
