# JavaScript 资源载入

NTE 提供了一些方法，用于在 JavaScript 脚本中随意控制载入或者获取资源包内的资源。

写在函数以外的顶层空间内的代码会在资源包加载时运行，可用于加载模型、贴图等资源。推荐将不需要每列车都不同的资源（如模型等）存储在全局变量，避免相同内容每列车都加载一份带来过多内存占用。



## ResourceLocation

Minecraft 采取一个叫做资源位置的东西来标识资源包内的文件。很多函数只接受 `ResourceLocation` 类型的路径，而不接受字符串。

- `static Resources.id(idStr: String): ResourceLocation`

  将一个字符串转为对应的 `ResourceLocation`。如 `Resources.id("mtr:path/absolute.js")`

- `static Resources.idr(relPath: String): ResourceLocation`
或
- `static Resources.idRelative(relPath: String): ResourceLocation`

  相对于这个 JS 文件的另一个文件的 `ResourceLocation。如 `Resources.idr("ccc.png")`，请注意此函数不能在函数内使用



## 载入 AWT 资源

这些函数加载用于通过 Java AWT 来绘制动态贴图的资源。

- `static Resources.getSystemFont(name: String): Font`

  获取一个系统或者 MTR 内置的字体。

  | 字体名称   | 说明                                                         |
  | ---------- | ------------------------------------------------------------ |
  | Noto Serif | MTR 内置的衬线字体 (类似宋体)。在各种系统上相同。            |
  | Noto Sans  | NTE 内置的无衬线字体 (类似黑体)。在各种系统上相同。          |
  | Serif      | 由 AWT 选择这台计算机上安装的一款衬线字体。在不同的设备上可能不同。 |
  | SansSerif  | 由 AWT 选择这台计算机上安装的一款无衬线字体。在不同的设备上可能不同。 |
  | Monospaced | 由 AWT 选择这台计算机上安装的一款等宽字体。在不同的设备上可能不同。 |

- `static Resources.readBufferedImage(path: ResourceLocation): BufferedImage`

  加载一张图片为 BufferedImage。

- `static Resources.readFont(path: ResourceLocation): Font`

  加载一个 TTF 或 OTF 字体为 Font。

- `static Resources.getFontRenderContext(): FontRenderContext`

  获取一个 AWT FontRenderContext。



## 直接读取资源文件

- `static Resources.readString(location: ResourceLocation): String`

  将一个资源文件的内容作为字符串读出。读取失败时返回 null。




## 杂项

- `static Resources.parseNbtString(nbtStr: String): CompoundTag`

  用来获取 Minecraft 原版的 NBT 类型 CompoundTag。使用类似命令方块中的写法，返回 CompoundTag。

