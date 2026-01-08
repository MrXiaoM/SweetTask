package top.mrxiaom.sweet.taskplugin.commands;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
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
        if (!sender.isOp()) return false;
        if (args.length == 2 && "print".equalsIgnoreCase(args[0])) {
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
                sb.append("\n").append("  &e任务 ").append(cache.taskId).append(" 的任务数据");
                sb.append("\n").append("    &e到期时间: &f").append(cache.expireTime);
                sb.append("\n").append("    &e子任务列表: &7(").append(cache.subTaskData.size()).append(")");
                for (Map.Entry<String, Integer> entry : cache.subTaskData.entrySet()) {
                    sb.append("\n").append("      &b").append(entry.getKey()).append(" : &f").append(entry.getValue());
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
        if (args.length > 0 && "material".equalsIgnoreCase(args[0]) && sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 1 && "look".equalsIgnoreCase(args[1])) {
                RayTraceResult result = player.rayTraceBlocks(4);
                Block block = result == null ? null : result.getHitBlock();
                Material material = block == null ? null : block.getType();
                if (material == null) {
                    return t(sender, "&e你的准心没有指向一个方块");
                }
                player.sendMessage(material.name() + " (" + material.getId() + ")");
                return true;
            } else {
                ItemStack item = player.getItemInHand();
                Material material = item == null ? Material.AIR : item.getType();
                player.sendMessage(material.name() + " (" + material.getId() + ")");
                return true;
            }
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
