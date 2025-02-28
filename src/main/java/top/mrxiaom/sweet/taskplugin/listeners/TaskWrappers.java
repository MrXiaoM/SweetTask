package top.mrxiaom.sweet.taskplugin.listeners;

import java.util.List;

public class TaskWrappers<T> {
    public final T matcher;
    public final List<TaskWrapper> subTasks;

    public TaskWrappers(T matcher, List<TaskWrapper> subTasks) {
        this.matcher = matcher;
        this.subTasks = subTasks;
    }
}
