package top.mrxiaom.sweet.taskplugin.tasks;

import java.util.function.Consumer;

public interface ITaskParser {
    ITask parse(String[] args, String actionTips, Consumer<String> warn);
}
