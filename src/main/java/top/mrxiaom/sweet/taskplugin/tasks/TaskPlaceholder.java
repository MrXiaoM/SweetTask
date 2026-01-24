package top.mrxiaom.sweet.taskplugin.tasks;

import org.bukkit.entity.Player;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;

import java.util.StringJoiner;

public class TaskPlaceholder implements ITask {
    public static final String TYPE = "placeholder";
    @Override
    public String type() {
        return TYPE;
    }
    public static void register() {
        ITask.registerParser(TYPE, (args, actionTips, warn) -> {
            StringJoiner inputText = new StringJoiner(" ");
            for (int i = 1; i < args.length - 1; i++) {
                inputText.add(args[i]);
            }
            Integer target = Util.parseInt(args[args.length - 1]).orElse(null);
            if (target == null) {
                warn.accept("未输入数量");
                return null;
            }
            return new TaskPlaceholder(inputText.toString(), target, actionTips);
        });
    }
    public final String inputText;
    public final int target;
    public final String actionTips;

    public TaskPlaceholder(String inputText, int target, String actionTips) {
        this.inputText = inputText;
        this.target = target;
        this.actionTips = actionTips;
    }

    @Override
    public String actionTips() {
        return actionTips;
    }

    @Override
    public int getValue(Player player, LoadedTask task, TaskCache cache, int index) {
        String str = PAPI.setPlaceholders(player, inputText).trim();
        return Util.parseInt(str).orElse(0);
    }

    @Override
    public int getTargetValue() {
        return target;
    }
}
