package top.mrxiaom.sweet.taskplugin.tasks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.Collections;
import java.util.List;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.convert;
import static top.mrxiaom.sweet.taskplugin.utils.Utils.getOrList;

public class TaskSubmitItem implements ITask {
    @Override
    public String type() {
        return "submit";
    }
    public static void register() {
        ITask.registerParser("submit", (args, actionTips, warn) -> {
            Pair<List<String>, Integer> pair = getOrList(args, 1);
            List<ItemMatcher> items = convert(pair, args[1], "物品", ItemMatcher::of, warn);
            if (items == null) return null;
            int targetIndex = pair == null ? 2 : pair.getValue();
            Integer target = targetIndex < args.length
                    ? Util.parseInt(args[targetIndex]).orElse(null)
                    : null;
            if (target == null) {
                warn.accept("未输入数量");
                return null;
            }
            return new TaskSubmitItem(items, target, actionTips);
        });
    }
    public final List<ItemMatcher> items;
    public final int target;
    public final String actionTips;

    public TaskSubmitItem(List<ItemMatcher> items, int target, String actionTips) {
        this.items = Collections.unmodifiableList(items);
        this.target = target;
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }

    @Override
    public int getTargetValue() {
        return target;
    }

    @Contract("null -> false")
    public boolean isItemMatch(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        for (ItemMatcher matcher : items) {
            if (matcher.match(item)) return true;
        }
        return false;
    }
}
