package top.mrxiaom.sweet.taskplugin.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.TaskProcessDatabase;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.gui.Menus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;
import static top.mrxiaom.sweet.taskplugin.commands.CommandMain.startsWith;

public class Debug {
    protected static boolean onCommand(SweetTask plugin, CommandSender sender, String[] args) {
        if (args.length == 2 && "print".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return t(sender, "&e玩家不在线");
            }
            PlayerCache playerCache = plugin.getDatabase().getTasks(player);
            String nextSubmitTime = "&f提交计划时间: &e";
            LocalDateTime nextSubmit = playerCache.nextSubmitTime();
            if (nextSubmit == null) {
                nextSubmitTime += "未计划提交到数据库";
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
                nextSubmitTime = nextSubmit.format(formatter);
                Duration between = Duration.between(LocalDateTime.now(), nextSubmit);
                nextSubmitTime += " &7(还剩 " + between.getSeconds() + "秒)";
            }
            StringBuilder sb = new StringBuilder();
            for (TaskCache cache : playerCache.tasks.values()) {
                sb.append("\n").append("  &e任务 ").append(cache.taskId).append(" 的子任务数据");
                for (Map.Entry<String, Integer> entry : cache.subTaskData.entrySet()) {
                    sb.append("\n").append("    &b").append(entry.getKey()).append(" : &f").append(entry.getValue());
                }
            }
            return t(sender, nextSubmitTime + sb);
        }
        if (args.length == 2 && "test".equalsIgnoreCase(args[0]) && sender instanceof Player) {
            Player player = (Player) sender;
            LoadedTask task = TaskManager.inst().getTask(args[1]);
            if (task == null) {
                return t(sender, "任务不存在");
            }
            TaskProcessDatabase database = plugin.getDatabase();
            database.addTask(player, task, LocalDateTime.now().plusDays(1));
            return t(sender, "已添加任务");
        }
        return false;
    }

    protected static Boolean onTabComplete(CommandSender sender, String[] args, List<String> returnList) {
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("test".equalsIgnoreCase(args[0])) {
                    returnList.addAll(startsWith(TaskManager.inst().getTasksId(), args[1]));
                    return true;
                }
                if ("print".equalsIgnoreCase(args[0])) {
                    return null;
                }
            }
            if ("open".equalsIgnoreCase(args[0])) {
                returnList.addAll(startsWith(Menus.inst().keys(sender), args[1]));
                return true;
            }
        }
        return false;
    }
}
