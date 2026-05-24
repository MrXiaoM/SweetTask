package top.mrxiaom.sweet.taskplugin.matchers;

import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweet.taskplugin.mythic.IMythic;

import java.util.Objects;

public class MythicItemMatcher implements ItemMatcher {
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
