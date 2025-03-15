package top.mrxiaom.sweet.taskplugin.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.List;

public class ActionOpenRefreshTaskGui implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[refresh]")) {
            EnumTaskType type = Util.valueOr(EnumTaskType.class, s.substring(9), null);
            return type == null ? null : new ActionOpenRefreshTaskGui(type);
        }
        if (s.startsWith("refresh:")) {
            EnumTaskType type = Util.valueOr(EnumTaskType.class, s.substring(8), null);
            return type == null ? null : new ActionOpenRefreshTaskGui(type);
        }
        return null;
    };
    public final EnumTaskType type;
    public ActionOpenRefreshTaskGui(EnumTaskType type) {
        this.type = type;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        GuiManager guiManager = AbstractPluginHolder.get(GuiManager.class).orElseThrow(IllegalStateException::new);
        IGui parent = guiManager.getOpeningGui(player);
        // TODO: 打开刷新任务菜单

    }
}