# JavaScript 支持

NTE 支持通过编写 JavaScript 代码来完全自定义地控制渲染和其他功能。本文档列出在 NTE 中使用 JavaScript 时提供的各种函数等。



### 为什么这么麻烦？

本功能的一大目的就是实现列车的 LCD 动态显示屏；然而全世界不同城市和线路的显示屏外观、切换逻辑等千差万别。

如果设计一个简易的格式，就势必无法灵活还原现实中的设计，而受到很多人的要求和抱怨；而如果要设计一个更复杂的某种格式来描绘逻辑，最后就还是要变成一种类似于 JS 的东西，而且可能还不如 JS 好学好用。所以最后决定直接提供一个 JS 运行环境，来允许最大的灵活程度。



### 用什么来编辑 `.js` 文件

任何文本编辑器皆可，例如 Notepad3 或 Notepad++。您可能会喜欢用像 Visual Studio Code 之类的东西。用 IDEA / Visual Studio 大概不是好主意。

本文档假设您对 JavaScript 有一些基本的了解，且不再赘述 JavaScript 的基本语法等方面，您可以通过互联网上的资源来学习 JavaScript。以及，JavaScript 和 Java 没有任何关系。



### 类型标注

众所周知，JS 中的值有不同的类型。调用函数时需要传入对应类型的参数，而它的返回结果也有类型。本文档中将按照类似于 TypeScript 的形式标注所提供函数所需参数和返回值的类型。以下是几个例子：

- `static Resources.id(idStr: String): ResourceLocation`
  - `static` 代表这个函数不需要对象，可以直接调用 `Resources.id("aaa:bbb")`
  - `idStr: String` 代表接受一个字符串类型的参数，它的名字（只供参考用）是 idStr 
  - `: ResourceLocation` 代表调用完之后会返回一个 ResourceLocation 类型的返回值
- `Matrices.rotateX(radian: float): void`
  - 没有 `static` 代表它需要对象，例如 a 是一个 Matrices 类型的对象，可以调用如 `a.rotateX(Math.PI)`
  - `radian: float` 代表接受一个数字类型的参数。虽然 JS 里的数字不区分整数还是浮点数，本文档中还是会区分 `int`、`long`、`float`、`double` 写出以便明确它能不能接受小数部分和精度。
  - `: void` 代表它没有返回值。



### 使用 Java 内置类

Rhino 脚本引擎中可以使用 `java.包名.类名` 的形式使用 Java 标准库里的类。似乎还不能使用 mtr 内的类，或许是因为某些类加载器方面的问题。



### 要使用 let 或 var 声明变量

NTE 开启了 JavaScript 严格模式，不允许不声明直接给变量赋值。要声明全局变量，在函数外使用类似 `var glb;` 或 `var glb = 1;` 的写法声明。要在函数内使用局部变量，使用 `let local;` 或 `let local = 1;`。



### 不要阻塞或无限循环

NTE 会每帧调用一次您所写的函数，然后期望您的函数尽快处理完并返回。所以没有 “停住运行等等到一会儿再做” 这一说，如果要实现类似的效果，您应该记录一下时间，然后在符合时间的那次调用来执行相应的效果。

如果您的代码阻塞或者发生无限循环，将导致所有脚本执行全部卡住，因为 NTE 的脚本是逐个执行的。用 F3+T 可以重置这一情况。



### 使用 Java 类时的差异问题

对于例如字符串的常用功能，Java 和 JavaScript 都各有不同的类的实现，这就导致了有一种 JavaScript 自己的字符串而又有一种 Java 的字符串。而 NTE/MTR 的函数和字段返回的都是 Java 的字符串类而不是 JavaScript 的。Rhino 会自动进行一些转换所以大部分时候可以混着用，但有时就会有问题。

例如，这里是一个在获取字符串长度上，Java 的字符串类要用 `str.length()` 而 JavaScript 的字符串类是 `str.length` 引起的问题：

```javascript
var stationName = train.getThisRoutePlatforms().get(0).station.name;
print(stationName.length); // 不行: stationName 是 Java 的字符串而不是 JavaScript 的字符串
print(stationName.length()); // Java 的字符串类上获取长度的是 length() 函数，而不是 JavaScript 的 length 字段
print((""+stationName).length) // 用 ""+ 可以把它转成一个 JavaScript 的字符串类
```

类似的还有 Java 的列表（`List<XXX>`）。它和 JavaScript 的数组虽然干同一件事但是不同的类型。尝试在上面调用 JavaScript 数组的函数并不行，不过 Rhino 能自动适应从而可以在上面用 `list[0]` 取值或使用 `for (a of list)` 来遍历。



### 支持的 JavaScript 标准部分

Rhino 脚本引擎不支持所有最新的 JavaScript 功能。对于具体支持的功能可参见 [Mozilla 的文档](https://mozilla.github.io/rhino/compat/engines.html)。NTE 使用的是 Rhino 1.7.14 且已开启 VERSION_ES6 标记（即栏目中写 Flag 的相当于 Yes）。



### 错误

当脚本运行出错时，NTE 会在 Minecraft 日志中打出报错信息（以 “Error in NTE Resource Pack JavaScript” 开头），而在游戏内没有信息显示。报错信息中会说明错误发生在哪个脚本文件中的哪一行代码。大多启动器有将日志实时显示在单独窗口的功能。

同时，NTE 会暂停脚本运行 4 秒钟，然后再试运行脚本。



### 运行另一个脚本文件

- `static include(relPath: String): void` 

  载入并运行位置相对于这个 JS 文件的另一个 JS 文件。

- `static include(path: ResourceLocation): void`

  载入并运行资源包中一个位置的 JS 文件。如 `include(Resources.id("mtr:path/absolute.js"))`
