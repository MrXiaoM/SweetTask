package top.mrxiaom.sweet.taskplugin;

import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

@Language(prefix="messages.")
public class Messages {

    public static final Message player__not_online = field("&e玩家不在线 (或不存在)");
    public static final Message player__only = field("只有玩家可以执行该命令");
    public static final Message no_permission = field("&c你没有执行此操作的权限");

    @Language(prefix="messages.command.")
    public static class Command {
        public static final Message open__menu_not_found = field("&e菜单不存在");
        public static final Message reset__task_not_found = field("&e任务不存在");
        public static final Message reset__done = field("&a刷新完成，详见服务器控制台");
        public static final Message refresh__done = field("&a已刷新玩家 &e%player% &a 的任务");
        public static final Message reload__database = field("&a已重新连接数据库");
        public static final Message reload__done = field("&a配置文件已重载");
    }

    @Language(prefix="messages.menu.")
    public static class Menu {
        public static final Message task__submit__done = field("&a已提交物品到任务");
        public static final Message task__submit__not_yet = field("&e任务未完成");
        public static final Message refresh__deny_done = field("&e你已经完成过任务了，不可刷新");
        public static final Message refresh__deny_count = field("&a你的刷新次数已耗尽");
        public static final Message back__deny = field("&c当前不能返回");
        public static final Message open__not_found = field("&c找不到菜单&e %menu%");
    }
}
