package top.mrxiaom.sweet.taskplugin.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGui;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.func.AbstractPluginHolder;
import top.mrxiaom.sweet.taskplugin.gui.AbstractModel;
import top.mrxiaom.sweet.taskplugin.gui.IMenuCondition;
import top.mrxiaom.sweet.taskplugin.gui.Menus;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ActionOpenGui implements IAction {
    public static final IActionProvider PROVIDER = s -> {
        if (s.startsWith("[open]")) {
            return new ActionOpenGui(s.substring(9));
        }
        if (s.startsWith("open:")) {
            return new ActionOpenGui(s.substring(8));
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
        AbstractModel<?, ?> model = menus.get(id);
        if (model != null) {
            if (model instanceof IMenuCondition && !((IMenuCondition) model).check()) return;
            menus.create(parent, player, model).open();
        }
    }
}
