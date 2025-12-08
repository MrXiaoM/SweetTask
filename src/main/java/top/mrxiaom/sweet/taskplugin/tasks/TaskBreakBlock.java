package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;

import java.util.Collections;
import java.util.List;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.convert;
import static top.mrxiaom.sweet.taskplugin.utils.Utils.getOrList;

public class TaskBreakBlock implements ITask {
    public static final String TYPE = "break";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            Pair<List<String>, Integer> pair = getOrList(args, 1);
            List<BlockMatcher> blocks = convert(pair, args[1], "方块", BlockMatcher::of, warn);
            if (blocks == null) return null;
            int targetIndex = pair == null ? 2 : pair.getValue();
            Integer target = targetIndex < args.length
                    ? Util.parseInt(args[targetIndex]).orElse(null)
                    : null;
            if (target == null) {
                warn.accept("未输入数量");
                return null;
            }
            return new TaskBreakBlock(blocks, target, actionTips);
        });
    }
    public final List<BlockMatcher> blocks;
    public final int target;
    public final String actionTips;

    public TaskBreakBlock(List<BlockMatcher> blocks, int target, String actionTips) {
        this.blocks = Collections.unmodifiableList(blocks);
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
}
