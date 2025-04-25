package top.mrxiaom.sweet.taskplugin.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.gui.AbstractModel;
import top.mrxiaom.sweet.taskplugin.gui.IMenuCondition;
import top.mrxiaom.sweet.taskplugin.gui.Menus;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ActionOpenGui implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[open]")) {
            return new ActionOpenGui(s.substring(6));
        }
        if (s.startsWith("open:")) {
            return new ActionOpenGui(s.substring(5));
        }
        return null;
    };
    public final String id;
    public ActionOpenGui(String id) {
        this.id = id;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        IGui parent = GuiManager.inst().getOpeningGui(player);
        Menus menus = Menus.inst();
        AbstractModel<?, ?> menu = menus.get(id);
        if (menu != null) {
            if (menu instanceof IMenuCondition && !((IMenuCondition) menu).check(player)) return;
            menus.create(parent, player, menu).open();
        } else {
            t(player, "&c找不到菜单 " + id);
        }
    }
}
