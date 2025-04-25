package top.mrxiaom.sweet.taskplugin.commands;
        
import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.DatabaseHolder;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.gui.AbstractModel;
import top.mrxiaom.sweet.taskplugin.gui.IMenuCondition;
import top.mrxiaom.sweet.taskplugin.gui.Menus;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.*;
import java.util.stream.Collectors;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetTask plugin) {
        super(plugin);
        registerCommand("sweettask", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (SweetTask.DEBUG && Debug.onCommand(plugin, sender, args)) return true;
        if (args.length >= 1 && "open".equalsIgnoreCase(args[0])) {
            AbstractModel<?, ?> menu = Menus.inst().get(args.length >= 2 ? args[1] : "default");
            if (menu == null) {
                return t(sender, "菜单不存在");
            }
            Player target;
            if (args.length == 3) {
                if (!sender.hasPermission("sweet.task.open-others")) {
                    return t(sender, "&c你没有进行此操作的权限");
                }
                target = Util.getOnlinePlayer(args[2]).orElse(null);
                if (target == null) {
                    return t(sender, "&e玩家不在线 (或不存在)");
                }
            } else {
                target = sender instanceof Player ? (Player) sender : null;
                if (target == null) {
                    return t(sender, "&e只有玩家才能执行该操作");
                }
                if (!menu.hasPermission(target)) {
                    return t(target, "&c你没有进行此操作的权限");
                }
            }
            PlayerCache cache = plugin.getDatabase().getTasks(target);
            TaskManager.inst().checkTasksAsync(cache, () -> {
                if (menu instanceof IMenuCondition && !((IMenuCondition) menu).check(target)) return;
                Menus.inst().create(target, menu).open();
            });
            return true;
        }
        if (args.length == 2 && "reset".equalsIgnoreCase(args[0]) && sender.isOp()) {
            LoadedTask task = TaskManager.inst().getTask(args[1]);
            if (task == null) {
                return t(sender, "&e任务不存在");
            }
            plugin.getDatabase().resetTask(task);
            return t(sender, "&a刷新完成，详见服务器控制台");
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                DatabaseHolder db = plugin.options.database();
                db.reloadConfig();
                db.reconnect();
                return t(sender, "&a已重新连接数据库");
            }
            plugin.reloadConfig();
            return t(sender, "&a配置文件已重载");
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList(
            "open", "refresh");
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "open", "refresh", "reset", "reload");
    private static final List<String> listArg1Reload = Lists.newArrayList("database");
    private static final List<String> listArg1Refresh = Arrays.stream(EnumTaskType.values())
            .map(Enum::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (SweetTask.DEBUG) {
            List<String> debugList = new ArrayList<>();
            Boolean debug = Debug.onTabComplete(sender, args, debugList);
            if (debug == null) return null;
            if (debug) return debugList;
        }
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("reload".equalsIgnoreCase(args[0])) {
                    return startsWith(listArg1Reload, args[1]);
                }
                if ("reset".equalsIgnoreCase(args[0])) {
                    return startsWith(TaskManager.inst().getTasksId(), args[1]);
                }
            }
            if ("open".equalsIgnoreCase(args[0])) {
                return startsWith(Menus.inst().keys(sender), args[1]);
            }
            if ("refresh".equalsIgnoreCase(args[0])) {
                return startsWith(listArg1Refresh, args[1]);
            }
        }
        if (args.length == 3) {
            if ("open".equalsIgnoreCase(args[0]) && sender.hasPermission("sweet.task.open-others")) {
                return null;
            }
        }
        return emptyList;
    }

    public static List<String> startsWith(Collection<String> list, String s) {
        return startsWith(null, list, s);
    }
    public static List<String> startsWith(String[] addition, Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        if (addition != null) stringList.addAll(0, Lists.newArrayList(addition));
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
