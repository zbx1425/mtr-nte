# JavaScript 资源载入

NTE 提供了一些方法，用于在 JavaScript 脚本中随意控制载入或者获取资源包内的资源。

写在函数以外的顶层空间内的代码会在资源包加载时运行，可用于加载模型、贴图等资源。推荐将不需要每列车都不同的资源（如模型等）存储在全局变量，避免相同内容每列车都加载一份带来过多内存占用。



### ResourceLocation

Minecraft 采取一个叫做资源位置的东西来标识资源包内的文件。很多函数只接受 `ResourceLocation` 类型的路径，而不接受字符串。

- `static Resources.id(idStr: String): ResourceLocation`

  将一个字符串转为对应的 `ResourceLocation`。如 `Resources.id("mtr:path/absolute.js")`

- `static Resources.idr(relPath: String): ResourceLocation`

  相对于这个 JS 文件的另一个文件的 `ResourceLocation`。不能在函数内使用。如 `Resources.idr("ccc.png")`



### 载入模型

在 NTE 中处理模型的方法是，模型文件首先可以加载为 `RawModel`，接下来可以随意对他进行一些处理，然后要进行一个上传过程得到 `ModelCluster`，最后在渲染时将 `ModelCluster` 交给 NTE 显示。

- `static ModelManager.loadRawModel(Resources.manager(), path: ResourceLocation, null): RawModel`

  将一个模型整体地加载为一个 RawModel。

- `static ModelManager.loadPartedRawModel(Resources.manager(), path: ResourceLocation, null): Map<String, RawModel>`

  将一个模型的每个分组分别加载成各个 RawModel，返回一个 Java Map。

- `static ModelManager.uploadVertArrays(rawModel: RawModel): ModelCluster`

  把一个 RawModel 上传到显存，返回上传好的 ModelCluster。



### 载入 AWT 资源

这些函数加载用于通过 Java AWT 来绘制动态贴图的资源。

- `static Resources.readBufferedImage(path: ResourceLocation): BufferedImage`

  加载一张图片为 BufferedImage。

- `static Resources.readFont(path: ResourceLocation): Font`

  加载一个 TTF 或 OTF 字体为 Font。

- `static Resources.getSystemFont(name: String): Font`

  获取一个系统或者 MTR 内置的字体。

  | 字体名称   | 说明                                                         |
  | ---------- | ------------------------------------------------------------ |
  | Noto Serif | MTR 内置的衬线字体 (类似宋体)。在各种系统上相同。            |
  | Noto Sans  | NTE 内置的无衬线字体 (类似黑体)。在各种系统上相同。          |
  | Serif      | 由 AWT 选择这台计算机上安装的一款衬线字体。在不同的设备上可能不同。 |
  | SansSerif  | 由 AWT 选择这台计算机上安装的一款无衬线字体。在不同的设备上可能不同。 |
  | Monospaced | 由 AWT 选择这台计算机上安装的一款等宽字体。在不同的设备上可能不同。 |

- `static Resources.getFontRenderContext(): FontRenderContext`

  获取一个 AWT FontRenderContext。



### 直接读取资源文件

- `static Resources.readString(location: ResourceLocation): String`

  将一个资源文件的内容作为字符串读出。读取失败时返回 null。




### 杂项

- `static Resources.parseNbtString(nbtStr: String): CompoundTag`

  用来获取 Minecraft 原版的 NBT 类型 CompoundTag。使用类似命令方块中的写法，返回 CompoundTag。



### RawModel

RawModel 提供一些方法来对载入的模型进行处理，就像是直接在 OBJ 源文件里进行了这些修改一样。

| 函数                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `RawModel.append(other: RawModel): void`                     | 将另一个 RawModel 合并到这个 RawModel 里面。                 |
| `RawModel.append(other: RawMesh): void`                      | 将一个 RawMesh 合并到这个 RawModel 里面。                    |
| `RawModel.applyMatrix(transform: atrix4f): void`             | 用一个矩阵来变换模型里的所有顶点。                           |
| `RawModel.applyTranslation(x: float, y: float, z: float): void` | 平移模型里的所有顶点。                                       |
| `RawModel.applyRotation(direction: Vector3f, angle: float): void` | 以原点为中心绕一个轴旋转模型里的所有顶点。角度采用角度制。   |
| `RawModel.applyScale(x: float, y: float, z: float): void`    | 缩放模型。                                                   |
| `RawModel.applyMirror(vx: boolean, vy: boolean, vz: boolean, nx: boolean, ny: boolean, nz: boolean): void` | 镜面翻转模型。<br />六个布尔值，前三个为要否变换顶点，后三个为要否翻转法线方向。 |
| `RawModel.applyUVMirror(u: boolean, v: boolean): void`       | 反转 UV 方向。最终需要 V 正方向向下，所以导入 Blockbench 或 Blender 模型时需 `rawModel.applyUVMirror(false, true)`。 |
| `RawModel.replaceTexture(oldFileName: String, path: ResourceLocation): void` | 把所有文件名为 `oldFileName` 的贴图替换为 `resourceLocation` 所指定的贴图。 |
| `RawModel.replaceAllTexture(path: ResourceLocation): void`   | 把所有贴图替换为 `resourceLocation` 所指定的贴图。           |
| `RawModel.copy(): RawModel`                                  | 复制模型的材质和顶点书韩剧为新模型。                         |
| `RawModel.copyForMaterialChanges(): RawModel`                | 复制模型的材质为新模型，但和原先的模型共用一组顶点数据。     |

如果需要以不同方式修改同一个模型，可能需要复制模型。因为假如 `a` 是一个 RawModel，进行 `b = a` 后两个变量指向的是同一个 RawModel，修改 b 也会影响 a，就丢失了修改前的状态。这时可以使用 `b = a.copy()` 进行复制，来让两者互不影响。

如果不需要修改几何形态，只需要修改材质或替换贴图，可以使用 `b = a.copyForMaterialChanges()`，它只复制材质相关的信息，减少了在这一情况下不必要的复制顶点数据的操作。



### ModelCluster

模型上传之后就不能再修改顶点数据了，不过也还可以替换贴图。因此一个模型需要多次替换贴图时，可以先上传再替换，避免每次都替换后再上传产生的不必要的上传操作。

| 函数                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `ModelCluster.replaceTexture(oldFileName: String, path: ResourceLocation): void` | 把所有文件名为 `oldFileName` 字符串的贴图替换为 `resourceLocation` 所指定的贴图。 |
| `ModelCluster.replaceAllTexture(path: ResourceLocation): void` | 把所有贴图替换为 `resourceLocation` 所指定的贴图。           |
| `ModelCluster.copyForMaterialChanges(): ModelCluster`        | 复制模型的材质为新模型，但和原先的模型共用一组顶点数据。     |

