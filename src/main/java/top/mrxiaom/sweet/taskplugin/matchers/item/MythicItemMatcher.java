package top.mrxiaom.sweet.taskplugin.matchers.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.SweetTask;
import top.mrxiaom.sweet.taskplugin.matchers.ItemMatcher;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.Objects;

public class MythicItemMatcher implements ItemMatcher {
    public static final Provider PROVIDER = (input) -> {
        String lower = input.toLowerCase();
        if (lower.startsWith("mythic:")) {
            IMythic mythic = SweetTask.getInstance().getMythic();
            if (mythic != null) {
                return new MythicItemMatcher(mythic, input.substring(7));
            }
        }
        if (lower.startsWith("mm:")) {
            IMythic mythic = SweetTask.getInstance().getMythic();
            if (mythic != null) {
                return new MythicItemMatcher(mythic, input.substring(3));
            }
        }
        return null;
    };
    private final IMythic mythic;
    private final String mythicId;
    public MythicItemMatcher(IMythic mythic, String mythicId) {
        this.mythic = mythic;
        this.mythicId = mythicId;
    }

    public String getMythicId() {
        return mythicId;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return mythicId.equals(mythic.getMythicId(item));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MythicItemMatcher)) return false;
        MythicItemMatcher that = (MythicItemMatcher) o;
        return Objects.equals(mythicId, that.mythicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mythicId);
    }
}
