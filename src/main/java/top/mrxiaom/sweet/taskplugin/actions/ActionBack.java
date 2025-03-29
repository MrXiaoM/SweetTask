package top.mrxiaom.sweet.taskplugin.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.gui.Menus;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ActionBack implements IAction {
    public static final ActionBack INSTANCE = new ActionBack();
    public static final IActionProvider PROVIDER = s -> {
        if (s.equals("[back]") || s.equals("back")) {
            return new ActionBack();
        }
        return null;
    };
    private ActionBack() {}

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        GuiManager guiManager = AbstractPluginHolder.get(GuiManager.class).orElseThrow(IllegalStateException::new);
        IGui gui = guiManager.getOpeningGui(player);
        if (gui instanceof Menus.Impl) {
            ((Menus.Impl<?, ?>) gui).parent.open();
        }
        // TODO: 打开刷新任务菜单
        t(player, "&e暂不支持刷新商店，敬请期待");
    }
}
