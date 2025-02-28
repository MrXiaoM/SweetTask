package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.EntityMatcher;

import java.util.Collections;
import java.util.List;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.convert;
import static top.mrxiaom.sweet.taskplugin.utils.Utils.getOrList;

public class TaskKill implements ITask {
    @Override
    public String type() {
        return "kill";
    }
    public static void register() {
        ITask.registerParser("kill", (args, actionTips, warn) -> {
            Pair<List<String>, Integer> pair = getOrList(args, 1);
            List<EntityMatcher> entities = convert(pair, args[1], "实体", EntityMatcher::of, warn);
            if (entities == null) return null;
            int targetIndex = pair == null ? 2 : pair.getValue();
            Integer target = targetIndex < args.length
                    ? Util.parseInt(args[targetIndex]).orElse(null)
                    : null;
            if (target == null) {
                warn.accept("未输入数量");
                return null;
            }
            return new TaskKill(entities, target, actionTips);
        });
    }
    public final List<EntityMatcher> entities;
    public final int target;
    public final String actionTips;

    public TaskKill(List<EntityMatcher> entities, int target, String actionTips) {
        this.entities = Collections.unmodifiableList(entities);
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
