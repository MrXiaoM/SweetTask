package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Util;

public class TaskGainLevel implements ITask {
    public static final String TYPE = "gain_level";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            Integer level = Util.parseInt(args[1]).orElse(null);
            if (level == null) {
                warn.accept("未输入等级");
                return null;
            }
            return new TaskGainLevel(level, actionTips);
        });
    }
    public final int level;
    public final String actionTips;

    public TaskGainLevel(int level, String actionTips) {
        this.level = level;
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }

    @Override
    public int getTargetValue() {
        return level;
    }
}
