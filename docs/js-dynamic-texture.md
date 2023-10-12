# JavaScript 动态贴图

NTE 提供了一个 GraphicsTexture 类以在模型上使用通过 JS 控制的有动态内容的贴图，以用于 LCD 显示屏、闪灯图、LED 滚动文字等。



### GraphicsTexture

- `new GraphicsTexture(width: int, height: int)`

  创建一个动态贴图，需要在创建时指定宽和高。
  由于不同的列车可能有内容不同的显示屏，在这些场景里就需要把它在 `create` 函数里创建并放进 `state` 里。

- `GraphicsTexture.close(): void`

  释放这张动态贴图所使用的内存。这之后就不能再使用这张贴图了。
  如果是在列车的 `create` 函数里创建的，就需要在 `dispose` 函数里关闭它，否则将持续占用内存而产生内存泄漏。

- `GraphicsTexture.bufferedImage: BufferedImage`

  用于暂存绘图效果的 Java AWT BufferedImage。

- `GraphicsTexture.graphics: Graphics2D`

  这张动态贴图的 Java AWT Graphics。可以在上面调用各种绘制函数，来向 bufferedImage 上画图。

- `GraphicsTexture.upload(): void`

  将 bufferedImage 里的内容正式上传到显存，立刻应用到模型上显示。这个操作可能相对较为拉低 FPS，建议尽量使用 `RateLimit` 减少贴图更新的频率，如显示屏可以只每秒刷新十次，不需要在较远距离下刷新，某些信息不需要一直刷新，等等。

- `GraphicsTexture.identifier: ResourceLocation`

  这张动态贴图的虚拟资源位置。用它来替换模型的贴图。



### 示例

```javascript
importPackage(java.awt);
importPackage(java.awt.geom);

rawTrainModel = ModelManager.loadRawModel(Resources.manager(), Resources.idr("train.obj"), null);
baseTrainModel = ModelManager.uploadVertArrays(rawTrainModel);

function create(ctx, state, train) {
    state.pisTexture = new GraphicsTexture(1024, 256);
	state.trainModel = baseTrainModel.copyForMaterialChanges();
  	state.trainModel.replaceTexture("pis_placeholder.png", state.pisTexture.identifier);
    state.pisRateLimit = new RateLimit(0.1);
}

function dispose(ctx, state, train) {
    state.pisTexture.close();
}

serifFont = Resources.getSystemFont("Noto Serif");

function render(ctx, state, train) {
    if (state.pisRateLimit.shouldUpdate()) {
     	var g = state.pisTexture.graphics;
        g.setColor(Color.WHITE);
        g.clearRect(0, 0, 1024, 256);
        g.setFont(serifFont.deriveFont(Font.BOLD, 32));
        g.setColor(Color.BLACK);
        g.drawString("Hello World!", 10, 40);
        state.pisTexture.upload();
    }
    for (i = 0; i < train.trainCars(); i++) {
        ctx.drawCarModel(state.trainModel, i, null);
    }
}
```



### AWT 相关类

可以使用 Rhino 提供的 importPackage 函数来免去使用 AWT 类时前加 java.awt 的麻烦。

您可以在网上找到一些 AWT 的学习资料，或者查看 JavaDoc 来了解 Graphics2D 上提供了哪些绘图功能。

- JavaDoc: [Graphics](https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics.html) (是 Graphics2D 的基类，也就是它上面的东西也都可以在 GraphicsTexture.graphics 上用)
- JavaDoc: [Graphics2D](https://docs.oracle.com/javase/8/docs/api/java/awt/Graphics2D.html)

您可能会用到的东西包括：

- `static Color.decode`、`Color.WHITE……`、`new Color`
- `Graphics.setColor`、`Graphics.setFont`、`Graphics.setStroke(new BasicStroke(...))`
- `Graphics.drawRect`、`Graphics.fillRect`、`Graphics.drawRoundRect`、`Graphics.fillRoundRect`
- `Graphics.drawImage`、`Graphics.drawString`、`Font.deriveFont`
- `Graphics.setPaint(new GradientPaint(...))`、`Graphics.fill(new Rectangle(...))`
- `Graphics.getTransform`、`Graphics.transform`、`Graphics.setTransform`、`AffineTransform.getTranslateInstance`、`AffineTransform.getRotateInstance`
- `Graphics.setClip`
- `Graphics.getComposite`、`Graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ...))`、`Graphics.setComposite`
