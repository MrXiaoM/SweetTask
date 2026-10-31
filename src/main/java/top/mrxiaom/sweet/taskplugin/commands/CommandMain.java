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
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.Messages;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;
import top.mrxiaom.sweet.taskplugin.func.AbstractModule;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.gui.AbstractModel;
import top.mrxiaom.sweet.taskplugin.gui.IMenuCondition;
import top.mrxiaom.sweet.taskplugin.gui.Menus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            Menus menus = Menus.inst();
            AbstractModel<?, ?> menu = menus.get(args.length >= 2 ? args[1] : "default");
            if (menu == null) {
                return Messages.Command.open__menu_not_found.tm(sender);
            }
            Player target;
            if (args.length == 3) {
                if (!sender.hasPermission("sweet.task.open-others")) {
                    return Messages.no_permission.tm(sender);
                }
                target = Util.getOnlinePlayer(args[2]).orElse(null);
                if (target == null) {
                    return Messages.player__not_online.tm(sender);
                }
            } else {
                target = sender instanceof Player ? (Player) sender : null;
                if (target == null) {
                    return Messages.player__only.tm(sender);
                }
                if (!menu.hasPermission(target)) {
                    return Messages.no_permission.tm(sender);
                }
            }
            PlayerCache cache = plugin.getDatabase().getTasks(target);
            TaskManager.inst().checkTasksAsync(cache, () -> {
                if (menu instanceof IMenuCondition && !((IMenuCondition) menu).check(target)) return;
                menus.create(target, menu).open();
            });
            return true;
        }
        if (args.length == 2 && "reset".equalsIgnoreCase(args[0]) && sender.isOp()) {
            LoadedTask task = TaskManager.inst().getTask(args[1]);
            if (task == null) {
                return Messages.Command.reset__task_not_found.tm(sender);
            }
            plugin.getDatabase().resetTask(task);
            return Messages.Command.reset__done.tm(sender);
        }
        if (args.length == 2 && "refresh".equalsIgnoreCase(args[0]) && sender.isOp()) {
            Player player = Util.getOnlinePlayer(args[1]).orElse(null);
            if (player == null) {
                return Messages.player__not_online.tm(sender);
            }
            PlayerCache cache = plugin.getDatabase().getTasks(player);
            cache.submitRefresh(null, () -> {});
            return Messages.Command.refresh__done.tm(sender,
                    Pair.of("%player%", player.getName()));
        }
        if (args.length >= 1 && "reload".equalsIgnoreCase(args[0]) && sender.isOp()) {
            if (args.length == 2 && "database".equalsIgnoreCase(args[1])) {
                DatabaseHolder db = plugin.options.database();
                db.reloadConfig();
                db.reconnect();
                return Messages.Command.reload__database.tm(sender);
            }
            plugin.reloadConfig();
            return Messages.Command.reload__done.tm(sender);
        }
        return true;
    }

    private static final List<String> emptyList = Lists.newArrayList();
    private static final List<String> listArg0 = Lists.newArrayList(
            "open");
    private static final List<String> listOpArg0 = Lists.newArrayList(
            "open", "refresh", "reset", "reload");
    private static final List<String> listArg1Reload = Lists.newArrayList("database");
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
            if ("refresh".equalsIgnoreCase(args[0]) && sender.isOp()) {
                return null;
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
        List<String> stringList = new ArrayList<>();
        if (addition != null) {
            for (String str : addition) {
                if (str.toLowerCase().startsWith(s1)) {
                    stringList.add(str);
                }
            }
        }
        for (String str : list) {
            if (str.toLowerCase().startsWith(s1)) {
                stringList.add(str);
            }
        }
        return stringList;
    }
}
