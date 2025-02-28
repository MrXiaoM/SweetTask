package top.mrxiaom.sweet.taskplugin.func;

import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.sweet.taskplugin.SweetTask;

public abstract class AbstractGuisModule<M extends IModel> extends top.mrxiaom.pluginbase.func.AbstractGuisModule<SweetTask, M> {
    public AbstractGuisModule(BukkitPlugin plugin, String warningPrefix) {
        super(plugin, warningPrefix);
    }
}
