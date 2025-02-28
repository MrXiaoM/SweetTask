package top.mrxiaom.sweet.taskplugin.listeners;

import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;

public class TaskWrapper {
    public final LoadedTask task;
    public final ITask subTask;
    public final int index;

    public TaskWrapper(LoadedTask task, ITask subTask, int index) {
        this.task = task;
        this.subTask = subTask;
        this.index = index;
    }
}
