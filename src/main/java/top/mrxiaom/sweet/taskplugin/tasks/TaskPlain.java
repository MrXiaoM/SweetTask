package top.mrxiaom.sweet.taskplugin.tasks;

public class TaskPlain implements ITask {
    public static final String TYPE = "plain";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            return new TaskPlain(actionTips);
        });
    }
    public final String actionTips;

    public TaskPlain(String actionTips) {
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }

    @Override
    public int getTargetValue() {
        return 0;
    }
}
