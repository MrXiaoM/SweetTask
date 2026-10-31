package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.pluginbase.utils.Util;

public class TaskGainExp implements ITask {
    public static final String TYPE = "gain_exp";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            Integer exp = Util.parseInt(args[1]).orElse(null);
            if (exp == null) {
                warn.accept("未输入经验值");
                return null;
            }
            return new TaskGainExp(exp, actionTips);
        });
    }
    public final int exp;
    public final String actionTips;

    public TaskGainExp(int exp, String actionTips) {
        this.exp = exp;
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }

    @Override
    public int getTargetValue() {
        return exp;
    }
}
