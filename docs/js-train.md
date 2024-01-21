# JavaScript 列车相关

NTE 支持通过 JavaScript 控制列车的渲染全过程。既可以完全用 JavaScript 控制所有部件的渲染，也可以在现有的一个列车类型的显示模型的基础上叠加用 JavaScript 控制的显示内容。



### 添加车型

在 `mtr_custom_resources.json` 里使用这样写法即可添加使用 JavaScript 控制渲染的车型。

```json
{
  "custom_trains": {
    "s_train_suspend": {
      "name": "JS Test Train",
      "base_type": "train_9_2",
      "color": "2AF0AD",
      "script_files": [ "mtr:js_train/main.js" ],
      "bve_sound_base_id": "optonix1500"
    }
  }
}
```

可包含的内容和 MTR 原版添加车型时的相同，相当于写一个标准的 MTR 自定义列车，然后再添加 `script_files` 属性。

如果不含有 `base_type` 这个参数，将首先原样显示由其余内容设定的列车外观，然后把 JavaScript 控制的渲染内容叠加在上面。如果含有 `base_type` 这个参数，则会纯粹用 JavaScript 来显示它的外观。

| 属性                   | 说明                                                         |
| ---------------------- | ------------------------------------------------------------ |
| base_type              | 列车的交通类型、长度和宽度。如 `train_19_2`。如果有这个属性，会纯粹用 JavaScript 来显示它的外观，即忽略 `base_train_type`、`model` 等参数；否则会按照`base_train_type`、`model` 等显示一个外观再把 JavaScript 控制的内容叠加在上面。 |
| script_files           | 一个数组，包含要使用的 JS 文件的资源位置。可以使用多个 JS 文件。 |
| script_texts           | 可选，一个数组，包含要运行的一些 JS 纯文本内容（会在 script_files 之前运行）。可以用于需要给多个列车使用同一脚本，而又想设定一些各个列车之间不相同的设定的情况。 |
| has_gangway_connection | 是否可以通过贯通道走到其他车厢。没有这项时采取 base_train_type 的设置。 |
| is_jacobs_bogie        | 是否是铰接转向架。只影响 BVE 列车音效的轮轨声音播放。        |
| bogie_position         | 非铰接的转向架距离车中点的距离。只影响 BVE 列车音效的轮轨声音播放。 |



### 全局环境

同一车型的所有列车共享同一个运行环境（即全局变量等）。

写在函数以外的顶层空间内的代码会在资源包加载时运行，可用于加载模型、贴图等资源。推荐将不需要每列车都不同的资源（如模型等）存储在全局变量，避免相同内容每列车都加载一份带来过多内存占用。



### 您要定义的函数

您的脚本中应包含以下函数，NTE 会按需调用它们：

```javascript
function create(ctx, state, train) { ... }
function render(ctx, state, train) { ... }
function dispose(ctx, state, train) { ... }
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
| 第二个 (`state`)  | 一个和某列车关联的 JavaScript 对象。初始值是 `{}`，可随意设置其上的成员，用来存储一些需要每列车都不同的内容。 |
| 第三个 (`train`)  | 用于获取列车的状态。类型是 Train。                           |

接下来列出您可以进行的所有渲染控制操作，和可以获取到的所有关于列车的信息。



### TrainScriptContext
调用以下函数可以**控制渲染**。每次 `renderTrain` 时都需要为想绘制的模型调用相应的函数，

- `TrainScriptContext.drawCarModel(model: ModelCluster, carIndex: int, poseStack: Matrices): void`

  要求 NTE 在一节车厢处绘制模型。

  `carIndex`：模型放置位置相对于哪节车厢的中心（`0` 为相对于出库方向的第一节，`train.trainCars() - 1` 为最后一节）。原点位置是列车的中心，离地高 1m 处。

  `poseStack`：模型放置位置的变换，传入 `null` 表示就放在中心不变换。

- `TrainScriptContext.drawConnModel(model: ModelCluster, carIndex: int, poseStack: Matrices): void`

  要求 NTE 在车厢连接处绘制模型。

  `carIndex`：模型放置位置相对于第几个连接处（`0` 为相对于出库方向的第 1 和 2 节之间，`train.trainCars() - 2` 为最后一个）原点位置是连接处的中心，离地高 1m 处。

  `poseStack`：同上。

- `TrainScriptContext.drawConnStretchTexture(location: ResourceLocation, carIndex: int): void`

  要求 NTE 在车厢连接处绘制与 MTR 原版相同的可拉伸折蓬。为节省性能，要输入的图片是 MTR 原版的四张图片合并成一张。其中左上四分之一是外侧，右上四分之一是内部侧面，左下四分之一是内部顶面，右下四分之一是内部底面。

调用以下函数可以**播放声音**。只在需要开始播放时调用，重复调用会使多个声音叠加。

- `TrainScriptContext.playCarSound(sound: ResourceLocation, carIndex: int, x: float, y: float, z: float, volume: float, pitch: float): void`

  播放声音。可设定声源位置，能被所有附近玩家听到。

- `TrainScriptContext.playAnnSound(sound: ResourceLocation, volume: float, pitch: float): void`

  播放广播声音。只能被当前在车上的玩家听到。

此外，还有一组函数以 **辅助开发调试**。

- `TrainScriptContext.setDebugInfo(key: String, value: Object)`

  在屏幕左上角输出调试信息。需在设置中开启 “显示JS调试信息” 才会显示。`key` 为值的名称，`value` 为内容（`GraphicsTexture` 类型的会被显示出来，其他的会被转为字符串显示）。




### Train

| 属性                                          | 说明                                                         |
| --------------------------------------------- | ------------------------------------------------------------ |
| `train.shouldRender(): boolean`               | 现在是否应该显示这列车。在打开 “隐藏正在乘坐的列车” 时，JS 脚本仍然会照常运行，以保证如广播的功能还可以照常运作。此时这个函数会返回 `false` 以便关闭如粒子效果的功能。注意不需要用这个来判断停止 `drawCarModel`，NTE 会自动让它调用也没有效果。 |
| `train.shouldRenderDetail(): boolean`         | 列车是否在细节显示范围（32 格）以内。推荐在它为 `false` 的时候停止处理如广播、显示屏、细节模型等来节省性能。 |
| `train.trainTypeId(): String`                 | 车型 ID。                                                    |
| `train.baseTrainType(): String`               | 基于的车型 ID。                                              |
| `train.id(): long`                            | 这辆车在 MTR 内部的唯一编号，是一个随机的 64 位整数。<br />在 JavaScript 里可能因为有效数字位数不够，末尾的几位变成 0？ |
| `train.transportMode(): TransportMode`        | 交通方式种类。                                               |
| `train.spacing(): int`                        | 每节车的长度 + 1。                                           |
| `train.width(): int`                          | 宽度。由于玩家也有宽度，3 格宽的车的这一属性是 2。           |
| `train.trainCars(): int`                      | 车厢数。                                                     |
| `train.accelerationConstant(): float`         | 加速度。单位是 m/tick/tick，即 *1/400 m/s。                  |
| `train.manualAllowed(): boolean`              | 是否可以人工控制。                                           |
| `train.maxManualSpeed(): int`                 | 人工控制时的最大速度。是和轨道类型从低到高的顺序对应的。     |
| `train.manualToAutomaticTime(): int`          | 无人驾驶后转回自动控制的时间。                               |
| `train.path(): List<PathData>`                | 行驶路径，即要经过的每段轨道的列表。每一项是一个 `PathData`。 |
| `train.railProgress(): double`                | 从车库开出的距离。                                           |
| `train.speed(): float`                        | 速度。单位是 m/tick，即 *1/20 m/s。                          |
| `train.doorValue(): float`                    | 车门开度。关门为 0，全开为 1，开门时增加关门时减少。         |
| `train.doorValue(): float`                    | 车门开度。关门为 0，全开为 1，开门时增加关门时减少。         |
| `train.isDoorOpening(): boolean`              | 是否正在开门。                                               |
| `train.doorLeftOpen[carIndex: int]: boolean`  | 某一车厢的左侧车门是否可以打开。                             |
| `train.doorRightOpen[carIndex: int]: boolean` | 某一车厢的右侧车门是否可以打开。                             |
| `train.isCurrentlyManual(): boolean`          | 是否正在被人工控制。                                         |
| `train.isReversed(): boolean`                 | 是否已折返，即现在 1 号车是车尾。                            |
| `train.isOnRoute(): boolean`                  | 是否已出库。                                                 |

- `train.getRailProgress(car: int): double`

  相对于当前行驶方向的车头，从 0 开始计第 `car` 车前方的从车库开出的距离。如 `getRailProgress(1)` 是 1/2 车连接处的位置。

- `train.getRailIndex(railProgress: double, roundDown: boolean): int`

  计算某个 `railProgress` 是位于在 `path()` 中的第几项的这条轨道上的。如果这个位置正位于两条轨道交界，`roundDown` 为 `true` 时返回更靠车尾方向的一条，否则返回更靠车头方向的一条。

- `train.getRailSpeed(railIndex: int): float`

  `path()` 中的第 `railIndex` 项的这条轨道可通行的最高速度。它能正确处理这段轨道是站台时列车不应该进站还加速的情况。单位是 m/tick，即 *1/20 m/s。

- `train.getAllPlatforms(): List<PlatformInfo>`

  列车指令里所有线路行经的所有站台的列表。每一项是一个 `PlatformInfo`。在站台上折返时只算作一项。

- `train.getAllPlatformsNextIndex(): int`

  列车下一站（这个值在列车出站时更新，所以停在站台时则是这一站）要停靠的站台是在 `getAllPlatforms()` 的列表里的第几项。
  0 代表刚刚出库正前往第一个站台，等于 `getAllPlatforms().size()` 代表正在回库。注意因此它并不一定在 `getAllPlatforms()` 的索引范围内，需要判断。

- `train.getThisRoutePlatforms(): List<PlatformInfo>`

  列车现在行驶的线路行经的所有站台的列表。每一项是一个 `PlatformInfo`。

- `train.getThisRoutePlatformsNextIndex(): int`

  列车下一站要停靠的站台是在 `getThisRoutePlatforms()`的列表里的第几项。
  0 正前往第一个站台，等于 `getThisRoutePlatforms().size()` 代表正在前往下一条线路起点或回库。注意因此它并不一定在 `getThisRoutePlatforms()` 的索引范围内，需要判断。



### PathData

| 属性                                  | 说明                                                         |
| ------------------------------------- | ------------------------------------------------------------ |
| `PathData.rail: Rail`                 | 这段轨道；详情见 MTR 源码 `Rail.java`。                      |
| `PathData.rail.railType: RailType`    | 这段轨道的轨道类型（木、铁……）；详情见 MTR 源码 `RailType.java`。 |
| `PathData.rail.getModelKey(): String` | 这段轨道所使用的 NTE 轨道模型。未设置为空字符串，隐藏为 `"null"`。 |
| `PathData.dwellTime: int`             | 如果这段轨道是站台的话，列车要停车的时间。不是站台时为 0。单位是 *0.5s。 |



### PlatformInfo

| 属性                                           | 说明                                                         |
| ---------------------------------------------- | ------------------------------------------------------------ |
| `platformInfo.route: Route`                    | 这个站台所属的路线。在站台上折返时，是折返后的路线。详情见 MTR 源码 `Route.java`。 |
| `PlatformInfo.route.name: String`              | 这个站台所属的路线的名称。                                   |
| `PlatformInfo.station: Station`                | 这个站台所属的车站。详情见 MTR 源码 `Station.java`。         |
| `PlatformInfo.station.name: String`            | 这个站台所属的车站的名称。                                   |
| `PlatformInfo.platform: Platform`              | 这个站台。详情见 MTR 源码 `Platform.java`。                  |
| `PlatformInfo.platform.name: String`           | 这个站台的名称。                                             |
| `PlatformInfo.platform.dwellTime: int`         | 这个站台的停车时间，单位是 Tick (1/20 s)。                   |
| `PlatformInfo.destinationStation: Station`     | 这个站台所属的路线的终点站。详情见 MTR 源码 `Station.java`。 |
| `PlatformInfo.destinationStation.name: String` | 这个站台所属的路线的终点站的名称。                           |
| `PlatformInfo.destinationName: String`         | 这个站台的终点名称（考虑自定义终点名称）                     |
| `PlatformInfo.distance: double`                | 这个站台末端所在位置从车库开出的距离，可以拿来和 railProgress 比较。 |
| `PlatformInfo.reverseAtPlatform: boolean`      | 列车是否会在这个站台上折返。                                 |

