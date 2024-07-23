# JavaScript 装饰物件相关

NTE 支持通过 JavaScript 控制装饰物件的渲染。但只能选择完全用 JavaScript 控制所有部件的渲染，或不使用 JavaScript 控制渲染。（至少现在是这样的）



## 添加 JavaScript 控制渲染的装饰物件

您可以通过在 `assets/mtrsteamloco/eyecandies` 文件夹内添加 JSON 文件来添加用JavaScript控制渲染的装饰物件。其写法大致如下：

```json
{
    "key1": {
    "name": "name1",
    "scriptFiles": ["mtrsteamloco:eyecandies/script1.js"]
    },
    "key2": {
    "name": "name2",
    "scriptFiles": ["mtrsteamloco:eyecandies/script2.js", "mtrsteamloco:eyecandies/script3.js", "mtrsteamloco:eyecandies/script4.js"]
    }
}
```

其中，`key` 是装饰物件的唯一标识符，`name` 是装饰物件的名称，`scriptFiles` 是装饰物件的 JavaScript 文件列表。注意key只能由小写字母或下划线组成，scriptFiles需要有[]包裹，不含有能有"model"项否则会使用model文件不加载scriptFiles。



## 全局环境

同一个key的装饰物件共享同一个运行环境（即全局变量等）。

写在函数以外的顶层空间内的代码会在资源包加载时运行，可用于加载模型、贴图等资源。推荐将不需要每次都重新加载的的资源（如模型等）存储在全局变量，避免相同内容每放置新放块都加载一份带来过多内存占用。



## 您要定义的函数

您的脚本中应包含以下函数，NTE 会按需调用它们：

```javascript
function create(ctx, state, block) { ... }
function render(ctx, state, block) { ... }
function dispose(ctx, state, block) { ... }
```

| 函数    | 说明                                                         |
| ------- | ------------------------------------------------------------ |
| create  | 在列车最开始被加载时调用，可用于进行一些初始化的操作，例如创建动态贴图。 |
| render  | *大致*每帧调用一次。用于主要的显示逻辑。代码在单独线程上运行以便不拖低 FPS。如果代码耗时太长，就可能实际上好几帧才运行一次。 |
| dispose | 在列车驶出可视范围时调用。可用于释放动态贴图之类的操作。     |

NTE 调用这几个函数时会使用三个参数，稍后介绍其各自的内容。

| 参数 (本文中称呼) | 说明                                                         |
| ----------------- | ------------------------------------------------------------ |
| 第一个 (`ctx`)    | 用于向 NTE 输出要如何渲染列车的相关操作。类型是 TrainScriptContext。 |
| 第二个 (`state`)  | 一个和某一个装饰物件方块关联的 JavaScript 对象。初始值是 `{}`，可随意设置其上的成员，用来存储一些需要每列车都不同的内容。 |
| 第三个 (`block`)  | 用于获取列车的状态。类型是 BlockEyeCandy.BlockEntityEyeCandy。                           |


接下来列出您可以进行的所有渲染控制操作，和可以获取到的所有关于方块的信息。



## EyeCandyScriptContext
调用以下函数可以**控制渲染**。每次 `render` 时都需要为想绘制的模型调用相应的函数，

- `EyeCandyScriptContext.drawModel(model: ModelCluster, poseStack: Matrices): void`
或
- `EyeCandyScriptContext.drawModel(model: DynamicModelHolder, poseStack: Matrices): void`

  要求 NTE 绘制模型。

  `poseStack`：模型放置位置的变换，传入 `null` 表示就放在中心不变换。

调用以下函数可以**播放声音**。只在需要开始播放时调用，重复调用会使多个声音叠加。

- `EyeCandyScriptContext.playSound(sound: ResourceLocation, volume: float, pitch: float): void`

  播放声音。

此外，还有一组函数以 **辅助开发调试**。

- `EyeCandyScriptContext.setDebugInfo(key: String, value: Object)`

  在屏幕左上角输出调试信息。需在设置中开启 “显示JS调试信息” 才会显示。`key` 为值的名称，`value` 为内容（`GraphicsTexture` 类型的会被显示出来，其他的会被转为字符串显示）。



## BlockEyeCandy
| 属性                                          | 说明                                                         |
| --------------------------------------------- | ------------------------------------------------------------ |
|`block.getWorldPos()`:Object|获取方块坐标|
|`block.getWorldPosVector3f():Vector3f|获取方块坐标|
