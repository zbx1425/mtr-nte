# JavaScript 数学工具类

此处不再赘述 JavaScript 标准库中的数学工具。而是介绍 NTE 中进行模型渲染变换等所提供的一组工具类。



### Vector3f

三维矢量，也就是一个 (x, y, z) 的坐标。

| 成员                                                | 说明                                                         |
| --------------------------------------------------- | ------------------------------------------------------------ |
| `new Vector3f(x: float, y: float, z: float)`        | 创建一个 Vector3f。                                          |
| `Vector3f.x(): float`                               | 获取它的 X 坐标。Y/Z 同理。                                  |
| `Vector3f.copy(): Vector3f`                         | 复制一个 Vector3f，以便独立地进行一些修改。                  |
| `Vector3f.normalize(): void`                        | 标准化为一个单位矢量，即保持它的方向，但让它的长度变为 1。   |
| `Vector3f.add(x: float, y: float, z: float): void`  | 加上另一个矢量。                                             |
| `Vector3f.add(other: Vector3f): void`               | 加上另一个矢量。                                             |
| `Vector3f.sub(other: Vector3f): void`               | 减去另一个矢量。                                             |
| `Vector3f.mul(x: float, y: float, z: float): void`  | 分别让三个分量与三个数相乘。                                 |
| `Vector3f.mul(n: float): void`                      | 数乘，让三个分量分别与同一个数相乘。                         |
| `Vector3f.rot(axis: Vector3f, rad: float): void`    | 在原点绕一个方向旋转这个矢量。角度采用弧度制，正值为逆时针，axis 需要是单位矢量。 |
| `Vector3f.rotDeg(axis: Vector3f, deg: float): void` | 同上，但是采用角度制。                                       |
| `Vector3f.rotX(rad: float): void`                   | 按照 X 轴旋转，角度采取弧度制。Y/Z 同理。                    |
| `Vector3f.cross(other: Vector3f): void`             | 与另一个矢量计算叉积，然后让自己成为结果。结果会与两个均垂直。 |
| `Vector3f.distance(other: Vector3f): float`         | 到另一个矢量所代表的坐标的距离。                             |
| `Vector3f.distanceSq(other: Vector3f): float`       | 到另一个矢量所代表的坐标的平方。比上一个算起来稍微快一点。   |
| `Vector3f.toBlockPos(): BlockPos`                   | 取整转换成 Minecraft 原版的 BlockPos。                       |
| `static Vector3f.ZERO: Vector3f`                    | 一个零矢量。不要对他做操作。                                 |
| `static Vector3f.XP: Vector3f`                      | (1, 0, 0)。Y/Z 同理。                                        |



### Matrix4f

三维变换矩阵。代表了把一个点对应到另一个点的一种变换关系。例如，我可以说 “我要把这个模型先绕 Y 轴旋转 90 度，再沿 X 轴移动 10 米……”，而这样的一组变换操作就可以用一个矩阵来表示。

需要注意的是，如果按照 “我要把这个模型先绕 Y 轴旋转 90 度，再沿 X 轴移动 10 米……” 的这种思路去想的话，那么会 “先发生” 的变换实际上是更靠后调用的函数。您可以理解成更靠后调用的函数离原始模型更近，而更靠前调用的函数离最终结果更近，所以如果从模型的原始位置出发来想的话是会更早那样变换。

不提供缩放变换。

| 成员                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `new Matrix4f()`                                             | 创建一个 Matrix4f。初始的是一个单位矩阵，它不做任何变换。    |
| `Matrix4f.copy(): Matrix4f`                                  | 复制一个 Matrix4f，以便独立地进行一些修改。                  |
| `Matrix4f.translate(x: float, y: float, z: float): void`     | 增加一个平移 (x,y,z) 的变换。                                |
| `Matrix4f.rotate(axis: Vector3f, rad: float): void`          | 增加一个在原点绕某个方向旋转的变换。角度采用弧度制，正值为逆时针，axis 需要是单位矢量。 |
| `Matrix4f.rotateX(rad: float): void`                         | 按照 X 轴旋转，角度采取弧度制。Y/Z 同理。                    |
| `Matrix4f.multiply(other: Vector3f): void`                   | 右乘另一个变换矩阵，即把那个矩阵的变换接在这个的后面。       |
| `Matrix4f.transform(vec: Vector3f): Vector3f`                | 计算一个坐标按照这个变换进行之后会到哪一个坐标。会返回一个新的 Vector3f，不会动输入值。 |
| `Matrix4f.transform3(vec: Vector3f): Vector3f`               | 同上，但只计入旋转不计入平移。                               |
| `Matrix4f.getTranslationPart(): Vector3f`                    | (0,0,0) 按照这个变换进行之后会到哪一个坐标。                 |
| `Matrix4f.asMoj(): ?`                                        | 转换成 Minecraft 原版所使用的矩阵类型。                      |
| `static Matrix4f.translation(x: float, y: float, z: float): Matrix4f` | 获取一个平移变换矩阵。                                       |



### Matrices

在渲染时会有 “恢复上一步时候的变换状态” 的需要。例如，我会加一个向下的平移变换来渲染转向架，但渲染完之后我要回到平移之前的变换状态来渲染车内其他的部件。

Matrices 实现一个堆栈，可以存储多个变换状态。push 和 pop 是要成对进行的。

| 成员                                                     | 说明                                                         |
| -------------------------------------------------------- | ------------------------------------------------------------ |
| `new Matrices()`                                         | 创建一个 Matrices。初始的只有一项单位矩阵。                  |
| `Matrices.translate(x: float, y: float, z: float): void` | 为当前状态增加一个平移 (x,y,z) 的变换。                      |
| `Matrices.rotate(axis: Vector3f, rad: float): void`      | 为当前状态增加一个在原点绕某个方向旋转的变换。角度采用弧度制，正值为逆时针，axis 需要是单位矢量。 |
| `Matrices.rotateX(rad: float): void`                     | 按照 X 轴旋转，角度采取弧度制。Y/Z 同理。                    |
| `Matrices.last(): Matrix4f`                              | 获取当前状态。                                               |
| `Matrices.pushPose(): void`                              | 将当前状态复制一份压入堆栈。就是把当前状态复制了一份保存起来。 |
| `Matrices.popPose(): void`                               | 弹出堆栈的最后一项。即，把当前状态丢掉，然后恢复出来上一个保存的状态设为当前状态。 |
| `Matrices.popPushPose(): void`                           | 先恢复再保存。                                               |
| `Matrices.clear(): boolean`                              | 现在是否只有一项。                                           |
| `Matrices.setIdentity(): void`                           | 将当前状态重设为单位矩阵。                                   |