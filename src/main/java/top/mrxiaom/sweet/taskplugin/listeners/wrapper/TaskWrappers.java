package top.mrxiaom.sweet.taskplugin.listeners.wrapper;

import java.util.List;

public class TaskWrappers<T> {
    public final T matcher;
    public final List<TaskWrapper> tasks;

    public TaskWrappers(T matcher, List<TaskWrapper> tasks) {
        this.matcher = matcher;
        this.tasks = tasks;
    }
}
