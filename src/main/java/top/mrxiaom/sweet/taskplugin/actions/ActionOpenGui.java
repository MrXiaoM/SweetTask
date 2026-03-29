package top.mrxiaom.sweet.taskplugin.actions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IActionProvider;
import top.mrxiaom.pluginbase.func.GuiManager;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.gui.AbstractModel;
import top.mrxiaom.sweet.taskplugin.gui.IMenuCondition;
import top.mrxiaom.sweet.taskplugin.gui.Menus;

import java.util.List;

import static top.mrxiaom.pluginbase.func.AbstractPluginHolder.t;

public class ActionOpenGui implements IAction {
    public static final IActionProvider PROVIDER = input -> {
        if (input instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) input;
            if ("open".equals(section.getString("type"))) {
                String menu = section.getString("menu");
                if (menu != null) {
                    return new ActionOpenGui(menu);
                }
            }
        } else {
            String s = String.valueOf(input);
            if (s.startsWith("[open]")) {
                return new ActionOpenGui(s.substring(6));
            }
            if (s.startsWith("open:")) {
                return new ActionOpenGui(s.substring(5));
            }
        }
        return null;
    };
    public final String id;
    public ActionOpenGui(String id) {
        this.id = id;
    }

    @Override
    public void run(Player player, @Nullable List<Pair<String, Object>> replacements) {
        IGuiHolder parent = GuiManager.inst().getOpeningGui(player);
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
