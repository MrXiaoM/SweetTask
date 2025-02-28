package top.mrxiaom.sweet.taskplugin.tasks;

import top.mrxiaom.sweet.taskplugin.func.TaskManager;

import java.util.Map;
import java.util.TreeMap;

public interface ITask {
    String type();
    String actionTips();
    int getTargetValue();

    static ITask load(TaskManager parent, String parentTaskId, String s) {
        String[] args;
        String actionTips;
        if (s.contains(";")) {
            int index = s.lastIndexOf(';');
            actionTips = s.substring(index + 1).trim();
            args = s.substring(0, index).trim().split(" ");
        } else {
            actionTips = "";
            args = s.split(" ");
        }
        // 加载子任务
        String id = args[0];
        ITaskParser parser = Internal.parsers.get(id);
        if (parser != null) {
            return parser.parse(args, actionTips,
                    msg -> parent.warn("[任务][" + parentTaskId + "][" + id + "] " + msg));
        }
        parent.warn("[任务][" + parentTaskId + "] 找不到可用的 " + id + " 子任务");
        return null;
    }
    static void registerParser(String id, ITaskParser parser) {
        Internal.parsers.put(id, parser);
    }

    class Internal {
        static Map<String, ITaskParser> parsers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }
}
