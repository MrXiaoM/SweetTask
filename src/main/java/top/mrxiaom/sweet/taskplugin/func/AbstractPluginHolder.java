package top.mrxiaom.sweet.taskplugin.func;
        
import top.mrxiaom.sweet.taskplugin.SweetTask;

@SuppressWarnings({"unused"})
public abstract class AbstractPluginHolder extends top.mrxiaom.pluginbase.func.AbstractPluginHolder<SweetTask> {
    public AbstractPluginHolder(SweetTask plugin) {
        super(plugin);
    }

    public AbstractPluginHolder(SweetTask plugin, boolean register) {
        super(plugin, register);
    }
}
