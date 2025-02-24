# 子任务列表

预置可用的子任务列表。

------

## 自定义显示

设置子任务在界面上、ActionBar 上的自定义提示

```
<子任务>; <自定义提示>
```
自定义提示可用变量 `%current%` 和 `%max%` 变量，代表子任务统计数值。

以**破坏方块**子任务举例
```
break DIAMOND_ORE or DEEPSLATE_DIAMOND_ORE 64; &f挖掘钻石矿石 &e%current%&7/&e%max%
```

------

## 破坏方块 TaskBreakBlock

```
break <方块列表> <数量>
```
其中，方块列表可以使用以下几种格式
+ `方块1` - 单个方块
+ `方块1 or 方块2` - 两个方块
+ `方块1, 方块2 or 方块3` - 三个方块或以上，更多方块使用逗号分隔
+ `ANY` - 代表所有类型

方块ID不区分大小写，为了与固定格式作出区别，建议全大写。

破坏方块列表中的任意方块，都会计入这个子任务的统计数值。

示例：挖掘钻石矿石
```
break DIAMOND_ORE or DEEPSLATE_DIAMOND_ORE 64
```

------

## 放置方块 TaskPlaceBlock

```
place <方块列表> <数量>
```
方块列表格式同**破坏方块**子任务。

示例：放置木板
```
place ACACIA_PLANKS, BAMBOO_PLANKS, BIRCH_PLANKS, CHERRY_PLANKS, CRIMSON_PLANKS, DARK_OAK_PLANKS, JUNGLE_PLANKS, MANGROVE_PLANKS, OAK_PLANKS, SPRUCE_PLANKS or WARPED_PLANKS 64
```

------

## 合成物品 TaskCrafting

```
crafting <物品列表> <数量>
```
物品列表格式同**破坏方块**子任务的方块列表。

示例：合成面包
```
crafting BREAD 64
```

------

## 钓鱼 TaskFishing

```
fishing <物品列表> <数量>
```
物品列表格式同**破坏方块**子任务的方块列表。

示例：钓上任何物品
```
fishing ANY 16
```

------

## 提交物品 TaskSubmitItem

```
submit <物品列表> <数量>
```
物品列表格式同**破坏方块**子任务的方块列表。特殊地，你可以使用类似 `mythic:ExampleItem` 的格式要求玩家提交 MythicMobs 物品。

示例：提交 MythicMobs 材料
```
submit mythic:ExampleItem 10
```
示例：提交下界之星或信标
```
submit NETHER_STAR or BEACON 3
```

------

## 击杀生物 TaskKill

```
kill <实体列表> <数量>
```
实体列表格式同**破坏方块**子任务的方块列表。

示例：击杀僵尸或僵尸村民
```
kill ZOMBIE or ZOMBIE_VILLAGER 24
```

可在实体类型前加 `mythic:` 以匹配 MythicMobs 生物。

示例：击杀 SkeletonKnight
```
kill mythic:SkeletonKnight 3
```
