# SweetTask

Minecraft 每日任务插件

## 命令

根命令是 `/sweettask`，别名 `/stask`, `/task`, `/st`。  
其中 `<>` 包裹的为必选参数，`[]` 包裹的为可选参数。
| 命令 | 描述 | 权限 |
| --- | --- | --- |
| `/task open <菜单名>` | 打开某个菜单 | 权限在菜单配置中定义 |
| `/task open <菜单名> [玩家]` | 为玩家打开菜单，要求玩家拥有菜单权限 | `sweet.task.open-others` |
| `/task reset <任务名>` | 重置所有玩家的某个任务的进度 | OP/控制台 |
| `/task refresh <玩家>` | 刷新某个玩家的所有任务，不记录刷新次数 | OP/控制台 |
| `/task reload database` | 重载数据库配置，并重新连接数据库 | OP/控制台 |
| `/task reload` | 重载插件配置，不会重连数据库 | OP/控制台 |

## 测试命令

在调试模式下，服务器管理员可以额外使用以下命令。这些命令仅用于测试，它们的提示消息均不可更改，且随时有可能删除或变更。
| 命令 | 描述 |
| --- | --- |
| `/task print <玩家>` | 查看玩家的任务数据 |
| `/task test <任务ID>` | 为自己添加一个任务 |

## 权限
+ `sweet.task.settings.hide-action` 隐藏任务进度 actionbar 消息
