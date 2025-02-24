package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.convert;
import static top.mrxiaom.sweet.taskplugin.utils.Utils.getOrList;

public class TaskFishing implements ITask {
    @Override
    public String type() {
        return "fishing";
    }
    public static void register() {
        ITask.registerParser("fishing", (args, actionTips, warn) -> {
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
            return new TaskFishing(items, target, actionTips);
        });
    }
    public final List<ItemMatcher> items;
    public final int target;
    public final String actionTips;

    public TaskFishing(List<ItemMatcher> items, int target, String actionTips) {
        this.items = Collections.unmodifiableList(items);
        this.target = target;
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }
}
