# 显示屏工具类

对于给一个现有列车添加显示屏的需求，显示屏工具类可以更便利地配置相应的模型和动态贴图。



## DisplayHelper

这个类需要写 `include(Resources.id("mtrsteamloco:scripts/display_helper.js"));` 来导入。

- `new DisplayHelper(cfg: Object)`

  按照一个显示屏位置配置创建一个 DisplayHelper，并生成相应的模型，返回一个只配置了模型的基本 DisplayHelper。必须在函数之外调用。

- `DisplayHelper.create(): DisplayHelper`

  进行创建动态贴图等特定的配置，返回一个完全配置好的 DisplayHelper。可以把结果保存到 `state` 里。
  `new` 出来的 DisplayHelper 只能用于调用 `create` ，`create` 出来的 DisplayHelper 可以用来进行其他操作。

- `DisplayHelper.close(): void`

  `create` 出来的 DisplayHelper 必须在 `dispoe` 时关闭，以释放资源。

- `DisplayHelper.graphics(): Graphics2D`

  获取用来在显示屏上画图的 Java AWT Graphics。坐标原点是包含所有显示屏位置的整张大图的左上角。

- `DisplayHelper.graphicsFor(slotName: String): Graphics2D`

  获取用来在显示屏上画图的 Java AWT Graphics。坐标原点是用于这个显示屏的区域的左上角。
  这其实和 `graphics()` 返回同一个对象，只是自动设置一下变换。

- `DisplayHelper.upload(): void`

  将画好的内容正式上传应用。

- `DisplayHelper.model: ModelCluster`

  包含了所有显示屏的模型。

- `DisplayHelper.texture: GraphicsTexture`

  内部使用的 GraphicsTexture。


  
## 示例代码

下面的部分会介绍代码里各部分的含义。

```javascript
include(Resources.id("mtrsteamloco:scripts/display_helper.js"));

let slotCfg = {
  "version": 1,
  "texSize": [2048, 1024],
  "slots": [
    {
      "name": "lcd_door_left",
      "texArea": [0, 0, 2048, 274],
      "pos": [
        [[-0.59, 2.125, -1.75], [-0.755, 2.03, -1.75], [-0.755, 2.03, -3.25], [-0.59, 2.125, -3.25]]
      ],
      "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
    },
    {
      "name": "lcd_door_right",
      "texArea": [0, 512, 2048, 274],
      "pos": [
        [[0.59, 2.125, -3.25], [0.755, 2.03, -3.25], [0.755, 2.03, -1.75], [0.59, 2.125, -1.75]]
      ],
      "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
    }
  ]
};
var dhBase = new DisplayHelper(slotCfg);

function create(ctx, state, train) {
  state.pisRateLimit = new RateLimit(0.05);
  state.dh = dhBase.create();
}

function dispose(ctx, state, train) {
  state.dh.close();
}

function render(ctx, state, train) {
  if (state.pisRateLimit.shouldUpdate()) {
    let g;
    
    g = state.dh.graphicsFor("lcd_door_left");
    g.setColor(Color.RED);
    g.fillRect(0, 0, 2048, 274);
    // ...
      
    g = state.dh.graphicsFor("lcd_door_right");
    g.setColor(Color.BLUE);
    g.fillRect(0, 0, 2048, 274);
    // ...
    
    state.dh.upload();
  }
  
  for (let i = 0; i < train.trainCars(); i++) {
    ctx.drawCarModel(state.dh.model, i, null);
  }
}

```



## 显示屏位置配置

通过一个数组来指定动态显示屏的设置位置。这是为了使得设置更为灵活，以便便利地增加到已有模型的车辆上，同时这也和 RTM 的方向幕设定较为类似。

```json
{
  "version": 1,
  "texSize": [2048, 1024],
  "slots": [
    {
      "name": "lcd_door_left",
      "texArea": [0, 0, 2048, 274],
      "pos": [
        [[-0.59, 2.125, -1.75], [-0.755, 2.03, -1.75], [-0.755, 2.03, -3.25], [-0.59, 2.125, -3.25]]
      ],
      "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
    },
    {
      "name": "lcd_door_right",
      "texArea": [0, 512, 2048, 274],
      "pos": [
        [[0.59, 2.125, -3.25], [0.755, 2.03, -3.25], [0.755, 2.03, -1.75], [0.59, 2.125, -1.75]]
      ],
      "offsets": [[0, 0, -5], [0, 0, 0], [0, 0, 5], [0, 0, 10]]
    }
  ]
}
```

这个配置里设定了两个显示屏位置。每个显示屏位置使用一种显示内容，可以在下面的 `pos` 中让它在多个屏幕上显示，但是这些屏幕都只能显示相同的内容。

所有屏幕的内容共同画在一张生成的动态贴图上。`texSize` 指定这张动态贴图的尺寸（宽、高）。

- `name`  是这个位置的名称。
- `texArea` 是指在最后的动态贴图里的哪一部分用作这个屏幕的显示内容，分别是 X、Y、宽、高。
- `pos` 是一个三层的数组（注意看括号的数量和分布，避免写错），来写明各个屏幕的位置。每个屏幕只能是矩形，对于每个屏幕，分别按对于其正面来说的左上、左下、右下、右上的顺序给出四个 XYZ 坐标点。坐标原点是列车中心、地板高度，X 正方向向左、Y 正方向向上、Z 正方向向后。
- `offsets` 是一个两层的数组，用于把 `pos` 指定的显示屏复制多份，以在如门上方闪灯图的场景中省字。分别给出要复制出的每份的 XYZ 偏移量。如果没有写 `offsets`，就不会去复制。