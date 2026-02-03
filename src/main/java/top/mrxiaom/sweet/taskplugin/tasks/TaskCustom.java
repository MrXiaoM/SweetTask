package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Util;

import java.util.StringJoiner;

public class TaskCustom implements ITask {
    public static final String TYPE = "custom";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            StringJoiner key = new StringJoiner(" ");
            for (int i = 1; i < args.length - 1; i++) {
                key.add(args[i]);
            }
            Integer target = Util.parseInt(args[args.length - 1]).orElse(null);
            if (target == null) {
                warn.accept("未输入数量");
                return null;
            }
            return new TaskCustom(key.toString(), target, actionTips);
        });
    }
    public final String key;
    public final int target;
    public final String actionTips;

    public TaskCustom(String key, int target, String actionTips) {
        this.key = key;
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
