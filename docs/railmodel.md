# 轨道模型

NTE 允许您通过资源包添加更多的轨道模型。

## 使用

手持刷子物品，指向轨道节点，并通过更换视角选中想要编辑的轨道。接下来，按住 Shift 同时右击轨道节点，即可打开选择模型的界面。选择您偏好的模型之后，这个模型的选择会被记录到这个刷子物品的 NBT 数据中，您可接下来直接点击其他轨道节点来批量应用同一设置。

对于方向有左右之分的模型，在双向轨道上将沿刚刚被刷子点击了的节点到另一个节点的顺序放置（因此可通过用刷子点击两端来设置方向）；而单向轨道上刷子点击位置并无影响，只可沿行驶方向放置。


## 制作新轨道

您可以通过在 `assets/mtrsteamloco/rails` 文件夹内添加 JSON 文件来导入模型。其写法大致如下：

```json
{
    "name": "Testing Yellow Stuff",
    "model": "mtrsteamloco:rails/cube_yellow.obj",
    "repeatInterval": 1.0
}
```

在上述例子里，`assets/mtrsteamloco/rails/cube_yellow.json` 文件里有着以上内容，同时 `assets/mtrsteamloco/rails/cube_yellow.obj` 文件是这个轨道的 OBJ 模型。

如上是一个 JSON 文件说明一个轨道的写法。也可以在一个 JSON 文件内说明多个轨道。

```json
{
    "key1": {
        "name": "Name1",
        "model": "mtrsteamloco:rails/model1.obj"
    },
    "key2": {
        "name": "Name2",
        "model": "mtrsteamloco:rails/model2.obj",
        "repeatInterval": 1.0,
        "flipV": true
    }
}
```

其中：

- 在一个 JSON 文件内说明多个轨道的写法中，`key1`、`key2` 等是内部记录这个模型时所使用的名称，可任意选取，不会显示给玩家，但每个必须不同。在一个 JSON 文件说明一个模型的写法中，会把 JSON 文件的文件名用作这一用途。

- `name` 是这个模型显示在选择列表里的名称。

- `model` 是这个模型所使用的 OBJ 文件的位置。

  需要按照 Minecraft 的 “资源位置” 写法转写，即 `assets/mtrsteamloco/rails/teapot.obj` 需写为 `mtrsteamloco:rails/teapot.obj`。

  导入模型、设定贴图、设定渲染批次等的方法参见 [OBJ 模型相关适配](objschem.md) 。
  
  您使用的 OBJ 文件应当符合：相对于轨道的方向，X 轴正方向向右，Y 轴正方向向上，Z 轴正方向向后。每个单位长度为 1m。

  另外也可以使用 BVE CSV 文件，此时 Z 轴正方向向前。

  NTE 在加载模型时会自动把模型绕 (1,1,1) 方向旋转 2°，以减少轨道之间的深度冲突。

以上是必填项。以下是可选项：

- `repeatInterval` 设定在应用到轨道上时两个相邻模型之间的间距，单位是 m，默认为 0.5。

由于 NTE 只是将模型进行移动旋转后放置，而不进行拉伸，因此您的模型中的轨道部分应当比 repeatInterval 略微长出一点，以便在弯道上外侧的轨道不至于断开。另外，不太建议将 repeatInterval 设为 1 以上的值，因为轨道的长度不一定是它的整倍数，这可能致使模型间距不恰当（……事实上，由于圆周率的参与，它也不一定是整数，所以 1/0.5 的设置也会略受到影响）。

- `yOffset` 设定轨道的垂直偏移量。这直接平移模型的原点(旋转中心)，因此或许对于制作悬挂式单轨轨道效果更好。默认为 0。
- `flipV` (V大写)  设为 true 将把 OBJ 模型加载改为适配贴图 V 坐标正方向向上的导出方式的模式。

请注意大括号、中括号、双引号与逗号的使用。同时，所有文件名必须为全英文小写。
