package top.mrxiaom.sweet.taskplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.tasks.ITask;
import top.mrxiaom.sweet.taskplugin.tasks.TaskCrafting;

import java.util.List;
import java.util.Map;

import static top.mrxiaom.sweet.taskplugin.utils.Utils.getListOrEmpty;

@AutoRegister
public class CraftingListener extends AbstractListener<ItemStack, ItemMatcher> {
    public CraftingListener(SweetTask plugin) {
        super(plugin);
    }

    @Override
    protected void handleLoadTask(Map<ItemMatcher, List<TaskWrapper>> map, LoadedTask task, ITask subTask, int index) {
        if (subTask instanceof TaskCrafting) {
            TaskWrapper wrapper = new TaskWrapper(task, subTask, index);
            for (ItemMatcher item : ((TaskCrafting) subTask).items) {
                List<TaskWrapper> list = getListOrEmpty(map, item);
                list.add(wrapper);
            }
        }
    }

    @Override
    protected boolean isNotMatch(ItemMatcher matcher, ItemStack entry) {
        return !matcher.match(entry);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemCraft(CraftItemEvent e) {
        if (e.isCancelled()) return;
        HumanEntity whoClicked = e.getWhoClicked();
        if (!(whoClicked instanceof Player)) return;
        Player player = (Player) whoClicked;
        CraftingInventory craftingTable = e.getInventory();
        ItemStack item = craftingTable.getResult();
        if (!isEmpty(item)) {
            int amount, onceAmount = item.getAmount(); // 一次合成多少个物品
            if (onceAmount == 0) return;
            if (e.isShiftClick()) {
                int canCraftTimes = calcCanCraftTimes(craftingTable);
                int canReceiveAmount = calcCanReceiveAmount(player.getInventory(), item);
                amount = Math.min(canReceiveAmount, canCraftTimes * onceAmount);
            } else {
                amount = onceAmount;
            }
            if (amount > 0) {
                plus(player, item, amount);
            }
        }
    }

    /**
     * 计算工作台的材料可以合成多少次
     */
    private static int calcCanCraftTimes(@NotNull CraftingInventory craftingTable) {
        int canCraftTimes = 64;
        boolean atLeastOnce = false;
        for (ItemStack material : craftingTable.getMatrix()) {
            if (isEmpty(material)) continue;
            atLeastOnce = true;
            int count = material.getAmount();
            if (count < canCraftTimes) { // 最少的材料即为可合成次数
                canCraftTimes = count;
            }
        }
        return atLeastOnce ? canCraftTimes : 0;
    }
    /**
     * 计算背包可以容纳多少个输出物品
     */
    private static int calcCanReceiveAmount(@NotNull PlayerInventory inv, @NotNull ItemStack item) {
        int canReceiveAmount = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack itemStack = inv.getItem(i);
            boolean empty = isEmpty(itemStack);
            if (empty || item.isSimilar(itemStack)) {
                // 空的格子可以容纳 {最大堆叠数量 - 0} 个物品
                // 可以和输出物品堆叠的物品可以容纳 {最大堆叠数量 - 物品数量} 个物品
                canReceiveAmount += item.getType().getMaxStackSize() - (empty ? 0 : itemStack.getAmount());
            }
        }
        return canReceiveAmount;
    }

    @Contract("null -> true")
    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}
