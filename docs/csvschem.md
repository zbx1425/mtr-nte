## 概述

一个CSV文件允许使用纯文本命令来创造单个物件。该物件可以在线路或列车中使用。文件描述的物件可以包含任意数量的多边形。文件格式允许在CreatMeshBuilder部分中对多个多边形进行分组，并将颜色或纹理（也就是贴图）信息等属性分配给在每个部分中创建的多边形。这允许在同一个CreateMeshBuilder当中创建多个多边形，这些多边形共享公共属性。多边形在这里称为面（Face）。 


该文件是以任意[编码]({{< ref "/information/encodings/_index.md" >}})编码的纯文本文件，但是，带字节顺序标记的UTF-8是更好的选择。数字的[解析方式]({{< ref "/information/numberformats/_index.md" >}}) 是 **宽松的** ，尽管如此，编写时建议您必须写一些 *严格正确的* 数字。文件名是任意的，但必须有扩展名 **.csv** 。该文件将从上到下按照每行进行解析。 

## 语法

文件中的每一行都分为命令名称及其参数。所有命令的语法都是相同的： 

{{% command %}} 
**命令名称** , *参数<sub>1</sub>* , *参数<sub>2</sub>* , *参数<sub>3</sub>* ，...， *参数<sub>n</sub>* 
{{% /command %}} 

*命令名称* 不区分大小写。如果有参数，则 *命令名称* 和 *参数1* 用逗号（U+002C）来分隔。同样，参数也用逗号分隔。*命令名称* 和参数周围，及行的开头和结尾的 [空格]({{< ref "/information/whitespaces/_index.md" >}}) 都会被忽略。仅由空格组成的行或空行也被忽略。

在 *参数<sub>i</sub>* 处留空，也可以省略该参数。在省略时会应用特定的默认值。所有默认值都在下方有列出。

您可以在一行的末尾添加注释。注释由分号（U+003B，英文半角）开始。所有注释在开始解析文件之前就将被自动排除。 

## ■ 3. 可用指令

{{% command %}} 
**CreateMeshBuilder** 
{{% /command %}} 

这个命令标志着新一组面（多边形）的开始。它必须位于以下任何命令之前。在文件中可以根据需要添加任意数量的该指令。后续的所有命令将与前一个CreateMeshBuilder关联。

------

{{% command %}}  
**AddVertex**, *vX*, *vY*, *vZ*, *nX*, *nY*, *nZ*  
{{% /command %}}

{{% command-arguments %}} 
***vX*** ：顶点的x坐标，以米为单位，负值向左，正值向右，默认值为0。   
***vY*** ：顶点的y坐标，以米为单位，负值向下，正值向上，默认值为0。   
***vZ*** ：顶点的z坐标，以米为单位，负值向后，正值向前，默认值为0。   
***nX*** ：顶点法线的x坐标，默认值为0。   
***nY*** ：顶点法线的y坐标，默认值为0。   
***nZ*** ：顶点法线的z坐标，默认值为0。   
{{% /command-arguments %}} 

这个命令将创建一个新顶点，然后可以将此顶点用于AddFace或AddFace2命令来创建面。在CreateMeshBuilder部分中可以根据需要添加任意数量的该指令。但是，给出顶点的顺序对后续的命令很重要。给定的第一个顶点具有索引编号0，后续顶点具有索引1,2,3等等。 

法线是在特定点垂直于面的方向。如果面里的所有顶点具有相同的法线，那么面将看起来平坦。如果使用得当，您可以通过为每个顶点指定不同的法线来造成曲面的错觉 - 跨多个面在所有顶点上使用相同法线除外。 **请尽量使用简单的面和复杂的法线而不是复杂的面来达成曲面或凹凸效果。这能节省性能开支。** 如果全部为0或不给出，法线将被自动计算。 

------

{{% command %}} 
**AddFace** , *v<sub>1</sub>* , *v<sub>2</sub>* , *v<sub>3</sub>* , ..., *v<sub>max</sub>* 
{{% /command %}} 

{{% command-arguments %}} 
***v<sub>i</sub>*** ：此数值为将要包含在此面中的顶点索引。允许的数值为0到 *n* -1， *n* 是所使用的AddVertex命令数量。 
{{% /command-arguments %}} 

此命令将创建一个以所有给出v点为顶点的面。i的值（索引值）对应于AddVertex中创建顶点的顺序，因此该命令须在AddVertex命令之后使用。顶点索引出现的顺序很重要，必须以从面的正面来看的顺时针顺序给出。面的背面不可见。在相邻位置出现的顶点的连线不可以是该多边形的对角线。 仅支持凸多边形，凹多边形需要被拆成多个凸多边形。

![](https://s1.ax1x.com/2020/07/21/UIpAgK.png)

------

{{% command %}} 
**AddFace2** , *v<sub>1</sub>* , *v<sub>2</sub>* , *v<sub>3</sub>* , ..., *v<sub>max</sub>* 
{{% /command %}}

{{% command-arguments %}} 
***v<sub>i</sub>*** ：此数值为将要包含在此面中的顶点索引。允许的数值为0到 *n* -1， *n* 是所使用的AddVertex命令数量。 
{{% /command-arguments %}} 

此命令将创建一个以所有给出v点为顶点的面。i的值（索引值）对应于AddVertex中创建顶点的顺序，因此该命令须在AddVertex命令之后使用。顶点索引出现的顺序很重要，必须以从面的正面来看的顺时针顺序给出。在相邻位置出现的顶点的连线不可以是该多边形的对角线。 仅支持凸多边形，凹多边形需要被拆成多个凸多边形。面的两边都可见，但在目前的openBVE版本里背面的光照计算会有错误。

------

{{% command %}} 
**Cube** , *半宽* , *半高* , *半深* 
{{% /command %}} 

{{% command-arguments %}} 
***半宽*** ：一个表示此立方体一半宽度（X轴向，左右）的浮点数，以 **米** 为单位。   
***半高*** ：一个表示此立方体一半高度（Y轴向，上下）的浮点数，以 **米** 为单位。   
***半深*** ：一个表示此立方体一半深度（Z轴向，前后）的浮点数，以 **米** 为单位。   
{{% /command-arguments %}} 

此命令将以原点（0,0,0）为中心创建一个以 *两倍的半宽* ， *两倍的半高* 和 *两倍的半深* 为尺寸的立方体。即，在X轴上它占据 -*半宽* 到 *半宽* 的范围，在Y轴上它占据 -*半高* 到 *半高* 的范围，在Z轴上它占据 -*半深* 到 *半深* 的范围。立方体总有8个顶点和6个面。

{{% notice %}}  

#### 立方体指令表示

Cube命令相当于一系列的AddVertex和AddFace命令，会影响顶点索引，所以在同一CreateMeshBuilder部分中使用其他命令时需要考虑这些命令。[此处]({{< ref "/objects/native/cubecylinder/_index.md" >}})提供了Cube命令的详细信息。

{{% /notice %}}

------

{{% command %}} 
**Cylinder** , *n* , *上底半径* , *下底半径* , *高* 
{{% /command %}} 

{{% command-arguments %}} 
***n*** ：一个整数，表示顶底面正多边形的顶点数。  
***上底半径*** ：顾名思义。以 **米** 为单位。如该值为负数，则上底面将不生成。   
***下底半径*** ：顾名思义。以 **米** 为单位。如该值为负数，则下底面将不生成。   
***高*** ：一个表示该圆柱/圆锥/圆台高度的浮点数，以 **米** 为单位。如为负值，则该截锥体将上下倒转且显示面将朝内。   
{{% /command-arguments %}} 

该命令将创建一个[截锥体](http://en.wikipedia.org/wiki/Frustum)。如果 *上底半径* 和 *下底半径* 是相等的，这个物件将变成[棱柱](http://en.wikipedia.org/wiki/Prism_(geometry))，并可用作近似的圆柱。如果 *下底半径* 或 *上底半径* 为0，这个物件将变成[棱锥](http://en.wikipedia.org/wiki/Pyramid_(geometry))。创建的截椎体将以原点为中心。 即，在X和Z轴上，该截锥体下底面占据 -*下底半径* 到 *下底半径*，顶面占据 -*上底半径* 到 *上底半径*；在Y轴上，该截锥体占据 -½\**高* 到 ½\**高*。译注：以上三个链接需轻功。

当半径的值较小时， 如线杆或扶手，顶点数 *n* 为6或8就足够了。无论 *上底半径* ， *下底半径* 和 *n* 的值如何，该多面体将始终有 2\**n* 个顶点和 *n* +2个面，除非省略上下底面。若 *上底半径* 或 *下底半径* 为负数，则采用其绝对值，同时不创建相应的底面（没有盖儿）。若 *高* 为负数，则上下底面会倒转（上底在下，下底在上），同时所有面都会变为内部可见（默认情况是外部可见） 。

{{% notice %}}

#### 截锥体命令表示

Cylinder命令相当于一系列的AddVertex和AddFace命令，在同一CreateMeshBuilder部分中使用其他命令时需要考虑这些命令。[此处]({{< ref "/objects/native/cubecylinder/_index.md" >}})提供了Cylinder命令的详细信息。

{{% /notice %}}

------

{{% command %}} 
**Translate** , *X* , *Y* , *Z*   
**TranslateAll** , *X* , *Y* , *Z* 
{{% /command %}} 

{{% command-arguments %}} 
***X*** ：一个表示顶点在x轴上移动距离的浮点数，以 **米** 为单位。负值向左平移，正值向右平移。默认值为0。   
***Y*** ：一个表示顶点在y轴上移动距离的浮点数，以 **米** 为单位。负值向下平移，正值向上平移。默认值为0。   
***Z*** ：一个表示顶点在z轴上移动距离的浮点数，以 **米** 为单位。负值向后平移，正值向前平移。默认值为0。   
{{% /command-arguments %}} 

**Translate** 命令将移动从CreateMeshBuilder到Translate之间创建的所有顶点，且后续顶点不受影响。您可以在CreateMeshBuilder部分中根据需要使用不限数量的Translate命令。 **TranslateAll** 不仅影响当前CreateMeshBuilder部分中创建的顶点，还会影响到之前所有CreateMeshBuilder部分中创建的顶点，这对于在文件末尾插入来平移整个物件很有用。

------

{{% command %}} 
**Scale** , *X* , *Y* , *Z*   
**ScaleAll** , *X* , *Y* , *Z* 
{{% /command %}} 

{{% command-arguments %}} 
***X*** ：一个非零浮点数，表示x轴上的缩放比值，默认值为1。   
***Y*** ：一个非零浮点数，表示y轴上的缩放比值，默认值为1。   
***Z*** ：一个非零浮点数，表示z轴上的缩放比值，默认值为1。 
{{% /command-arguments %}} 

**Scale** 命令将缩放从CreateMeshBuilder到Scale之间创建的所有顶点，且后续顶点不受影响。您可以在CreateMeshBuilder部分中根据需要使用不限数量的Scale命令。 **ScaleAll** 不仅影响当前CreateMeshBuilder部分中创建的顶点，还会影响到之前所有CreateMeshBuilder部分中创建的顶点，这对于在文件末尾插入来缩放整个物件很有用。

------

{{% command %}} 
**Rotate** , *X* , *Y* , *Z* , *角度*   
**RotateAll** , *X* , *Y* , *Z* , *角度* 
{{% /command %}}

{{% command-arguments %}} 
***X*** ：旋转轴的x方向。负值指向左侧，正值指向右侧。默认值为0。   
***Y*** ：旋转轴的Y方向。负值指向下边，正值指向上边。默认值为0。   
***Z*** ：旋转轴的Z方向。负值指向后方，正值指向前方。默认值为0。 
{{% /command-arguments %}}

**Rotate** 命令将旋转从CreateMeshBuilder到Rotate之间创建的所有顶点，且后续顶点不受影响。旋转轴通过 *X* ， *Y* 和 *Z* 值指定。旋转将发生在垂直于该轴的平面中。该轴的零向量被视为（1,0,0），所有其他方向都被折算成单位向量。

您可以在CreateMeshBuilder部分中根据需要使用尽可能多的Rotate命令。 **RotateAll** 不仅影响当前CreateMeshBuilder部分中创建的顶点，还会影响到之前所有CreateMeshBuilder部分中创建的顶点，这对于在文件末尾插入来旋转整个物件很有用。 

※由于官方的说明文档原文有些晦涩，就算翻译过来也可能会看不懂，这里译者再开一段来讲讲本人是如何使用Rotate命令的。我倾向于“一对一”式的写法，即一个Rotate命令完成物件在一个坐标轴上的旋转，上文提到的X,Y,Z被我用来标记在该坐标轴上是否做出旋转动作，0代表否，1代表是。然后再用 角度 参数说明旋转的角度，正值为顺时针，负值为逆时针。例如 Rotate,0,1,0,180 表示将该物件以y轴为基准旋转180度。 

------

{{% command %}} 
**Shear** , *dX* , *dY* , *dZ* , *sX* , *sY* , *sZ* , *r*   
**ShearAll** , *dX* , *dY* , *dZ* , *sX* , *sY* , *sZ* , *r* 
{{% /command %}} 

{{% command-arguments %}} 
***dX*** ：向量D的x坐标，默认为0。  
***dY*** ：向量D的y坐标，默认为0。  
***dZ*** ：向量D的z坐标，默认为0。  
***sX*** ：向量S的x坐标，默认为0。  
***sY*** ：向量S的y坐标，默认为0。  
***sZ*** ：向量S的z坐标，默认为0。  
***r*** ：表示矢量移位的比例。默认为0。 
{{% /command-arguments %}} 

**Shear** 命令为当前CreateMeshBuilder部分中到目前为止创建的所有顶点执行[剪切映射](http://en.wikipedia.org/wiki/Shear_mapping)。 **ShearAll** 不仅影响当前CreateMeshBuilder部分中创建的顶点，还会影响到之前所有CreateMeshBuilder部分中创建的顶点，这对于在文件末尾插入来剪切整个物件很有用。 

![illustration_shear](/images/illustration_shear.png)

剪切映射以原点为中心进行。不严谨地说，将物体沿方向D切成平面，然后沿方向S移位。通常，D和S是垂直的。D和S都被折算为单位向量。如果 *r* 为0，则不执行转换。如果D和S垂直，则 *r* 的1值对应45度的斜率。 

------

{{% command %}} 
**Mirror** , *X* , *Y* , *Z*   
**MirrorAll** , *X* , *Y* , *Z* 
{{% /command %}} 

{{% command-arguments %}} 
***X*** ：决定x轴是否被镜像。默认值为0（否）。   
***Y*** ：决定y轴是否被镜像。默认值为0（否）。   
***Z*** ：决定z轴是否被镜像。默认值为0（否）。 
{{% /command-arguments %}} 

**Mirror** 命令将镜像从CreateMeshBuilder到Mirror之间创建的所有顶点，且后续顶点不受影响。 镜像的方向通过 *X* ， *Y* 和 *Z* 值指定。您可以在CreateMeshBuilder部分中根据需要使用任意数量的Mirror命令。

 **MirrorAll** 不仅影响当前CreateMeshBuilder部分中创建的顶点，还会影响到之前所有CreateMeshBuilder部分中创建的顶点，这对于在文件末尾插入来镜像整个物件很有用。 

------

{{% command %}}  
**SetColor**, *Red*, *Green*, *Blue*, *Alpha*  
**SetColorAll**, *Red*, *Green*, *Blue*, *Alpha*  
{{% /command %}}

{{% command-arguments %}} 
***R*** ：该颜色的红色分量，范围为0（黑）~255（红），默认值为0。   
***G*** ：该颜色的绿色分量，范围为0（黑）~255（绿），默认值为0。   
***B*** ：该颜色的蓝色分量，范围为0（黑）~255（蓝），默认值为0。   
***透明度*** ：该颜色的透明度，范围为0（透明）~255（不透明），默认值为255。 
{{% /command-arguments %}} 

The **SetColor** command sets the color for all faces that were already created in the current CreateMeshBuilder section. If no texture is used, the faces will be colored using the color data as specified by *Red*, *Green*and *Blue*. If a texture is used, the pixels in the texture will be multiplied by the color, where multiplying with black results in black and multiplying with white does not change the color of the texture pixels. Values in-between make the texture pixels darker. When lighting is used in the route, the actual color can change depending on the lighting conditions, but will usually become darker.

The **SetColorAll** command sets the color for all faces that were already created in the current CreateMeshBuilder section, and all those created in the previous CreateMeshBuilder sections.

------

{{% command %}} 
**LoadTexture** , *日间材质* , *夜间材质* 
{{% /command %}} 

{{% command-arguments %}} 
***日间材质*** ：将要加载的日间材质的文件路径，相对于CSV文件所在目录。   
***夜间材质*** ：将要加载的夜间材质的文件路径，相对于CSV文件所在目录。 
{{% /command-arguments %}} 

此命令将加载材质并将其用于当前CreateMeshBuilder部分中的所有面。文件路径相对于CSV文件所在路径。您也可以使用支持完整Alpha通道的PNG格式，但请尽量不要使用半透明的PNG，因为很吃性能。没有Alpha通道（全不透明）的材质可以与SetDecalTransparentColor命令配合使用来达到性能更好的透明效果。 

如果使用了 *夜间材质* ，它指定在夜间光照状态（.Brightness 0）下使用的材质，而 *日间材质* 指定在日间光照状态（.Brightness 255）下使用的材质。两个材质会根据光照状态互相混合（.Brightness 1~254），材质也需要以此来进行设计。如果指定了 *夜间材质* ，就必须同时指定 *日间材质* 。如果没有指定 *夜间材质* ，暗光照条件会使日间材质更黑。必须使用SetTextureCoordinates指令设定好材质与各顶点的关系，材质才能被正常显示。 

------

{{% command %}} 
**SetTextureCoordinates** , *顶点索引* , *水平偏移量(U)* , *垂直偏移量(V)* 
{{% /command %}} 

{{% command-arguments %}} 
***顶点索引*** ：这个材质坐标匹配的模型顶点。范围是0到 *n* -1， *n* 为 AddVertex指令创建的顶点数量。   
***水平偏移量(U)*** ：这个材质坐标相对于模型左边缘的位置。一个0~1之间的数字，0代表最左边，1代表最右边。   
***垂直偏移量(V)*** ：这个材质坐标相对于模型上边缘的位置。一个0~1之间的数字，0代表最上边，1代表最下边。 
{{% /command-arguments %}} 

这条指令为 *顶点索引* 指定的顶点匹配一个材质坐标。由于这个索引是要匹配一个已经创建了的顶点的，所以这条指令要放在AddVertex指令的后面。 当 *U* 或 *V* 的值大于0小于1时，如下图所示（应该解释得很清楚了），当指定顶点上的 *U* 或 *V* 值大于1，材质横纵向无限平铺（就是无数张图片组成的一个格子状的二维平面），U值“2”对应在该平面中所有左起第二列图片的右边线，V值“5”对应在该平面中所有第五行图片的下边线，而“2,5”则对应该平面中第二列第五行的那张图片的右下角（也就是前面所述两条垂直直线的交点）。使用大于1的U和V值，您可以将材质以平铺的方式在面上重复多次地贴图。 

![](https://s1.ax1x.com/2020/07/21/U5zEPx.png)
