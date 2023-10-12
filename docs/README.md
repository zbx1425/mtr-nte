# NTE: Nemo's Transit Expansion

本模组 NTE（Nemo's Transit Expansion, 蓝星社交通扩展）包含一些由 [Zbx1425](https://www.zbx1425.cn) 制作的对 [MTR](https://www.curseforge.com/minecraft/mc-mods/minecraft-transit-railway) 模组本体的实验性扩展功能。作为 MTR 开发贡献者之一，作者希望对这些新颖的功能作为独立扩展模组先行发布、展示与实验，并将稳定的部分稍后合并入 MTR 本体中。

 本模组最初在 [Teacon 2022 模组开发茶会](https://www.teacon.cn) 上公开，当时名为蒸汽动力 (MTRSteamLoco)，并荣获 “癫火之王” 荣誉。

<style>
/* This element defines the size the iframe will take.
   In this example we want to have a ratio of 25:14 */
.aspect-ratio {
  position: relative;
  width: 100%;
  height: 0;
  padding-bottom: 56.25%; /* The height of the item will now be 56.25% of the width. */
}

/* Adjust the iframe so it's rendered in the outer-width and outer-height of it's parent */
.aspect-ratio iframe {
  position: absolute;
  width: 100%;
  height: 100%;
  left: 0;
  top: 0;
}
</style>

<div class="aspect-ratio">
<iframe src="//player.bilibili.com/player.html?aid=818254400&bvid=BV1kG4y1G7yx&cid=910363279&page=1" scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> </iframe>
</div>

![Feature Grid](img/featgrid.jpg)



## 特性

NTE 目前包含以下功能：

- 立体轨道
- 新列车：D51 蒸汽机车、北京地铁 DK3，并带有应用新技术制作的车轮与乘务员动画
- OBJ 模型导入支持，协助资源包作者制作应用更精细的模型

- 可选择隐藏自己所乘坐的列车，便于拍摄前方展望影片

详见 [新增特性使用指引](feature.md) 。(**必读 !**)



## 下载

请参见 [下载](download.md)。

本模组当前无服务器侧功能，**只需在客户端安装**。故您只需在自己的设备上安装 NTE，即可在任意服务器与所有单人游戏存档中使用立体轨道、列车隐藏等功能，不需要在服务器一侧额外安装任何组件。

不过，在服务器上使用 NTE 自带的或以资源包导入的含 OBJ 模型的列车时，其他没有安装 NTE 和对应资源包的玩家将不能看到这列车。



## 资源包作者支持

- OBJ 模型导入支持

  通过新技术，NTE 为在 MTR 中使用 OBJ 格式的模型提供了支持。受益于新制的渲染系统，渲染性能更高，使用面数很高的精细模型时也不会造成很多卡顿。详见 [OBJ 模型相关适配](objschem.md) 。
