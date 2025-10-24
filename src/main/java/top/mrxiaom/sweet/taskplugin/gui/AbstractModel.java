package top.mrxiaom.sweet.taskplugin.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModel;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.gui.IGuiHolder;
import top.mrxiaom.sweet.taskplugin.database.entry.PlayerCache;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractModel<T, D extends IMenuData> implements IModel {
    private final String id, title;
    private final char[] inventory;
    protected final String permission;
    protected final Map<Character, T> mainIcons;
    private final Map<Character, LoadedIcon> otherIcons;

    public AbstractModel(String id, String title, char[] inventory, String permission, Map<Character, T> mainIcons, Map<Character, LoadedIcon> otherIcons) {
        this.id = id;
        this.title = title;
        this.inventory = inventory;
        this.permission = permission;
        this.mainIcons = mainIcons;
        this.otherIcons = otherIcons;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public char[] inventory() {
        return inventory;
    }

    public boolean hasPermission(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    @Override
    public Map<Character, LoadedIcon> otherIcons() {
        return otherIcons;
    }

    @Nullable
    public LoadedIcon otherIcon(Character id) {
        return id == null ? null : otherIcons.get(id);
    }

    @Nullable
    public T mainIcon(Character id) {
        return id == null ? null : mainIcons.get(id);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes) {
        return applyMainIcon((Menus.Impl<T, D>) instance, player, id, index, appearTimes);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public ItemStack applyMainIcon(IGuiHolder instance, Player player, char id, int index, int appearTimes, AtomicBoolean ignore) {
        return applyMainIcon((Menus.Impl<T, D>) instance, player, id, index, appearTimes, ignore);
    }

    public ItemStack applyMainIcon(Menus.Impl<T, D> instance, Player player, char id, int index, int appearTimes) {
        return null;
    }

    public ItemStack applyMainIcon(Menus.Impl<T, D> instance, Player player, char id, int index, int appearTimes, AtomicBoolean ignore) {
        return this.applyMainIcon(instance, player, id, index, appearTimes);
    }

    public abstract boolean click(
            Menus.Impl<T, D> gui, Player player,
            Object iconObj, ClickType click, int invSlot
    );

    public abstract D createData(Player player, PlayerCache playerCache);
}
