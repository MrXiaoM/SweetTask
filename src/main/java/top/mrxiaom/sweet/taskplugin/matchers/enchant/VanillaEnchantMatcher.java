package top.mrxiaom.sweet.taskplugin.matchers.enchant;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.EnchantMatcher;

import java.util.Map;
import java.util.Objects;

public class VanillaEnchantMatcher implements EnchantMatcher {
    public static final Provider PROVIDER = new Provider() {
        @Override
        public @Nullable EnchantMatcher parse(@NotNull String input) {
            Enchantment enchant = Util.valueOrNull(Enchantment.class, input);
            if (enchant != null) {
                return new VanillaEnchantMatcher(enchant);
            }
            return null;
        }
        @Override
        public int getPriority() {
            return 2000;
        }
    };
    private final Enchantment enchant;

    public VanillaEnchantMatcher(Enchantment enchant) {
        this.enchant = enchant;
    }

    public Enchantment getEnchant() {
        return enchant;
    }

    @Override
    public boolean match(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        Map<Enchantment, Integer> enchantMap = item.getEnchantments();
        int level = enchantMap.getOrDefault(enchant, 0);
        return level > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VanillaEnchantMatcher)) return false;
        VanillaEnchantMatcher that = (VanillaEnchantMatcher) o;
        return Objects.equals(enchant, that.enchant);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enchant);
    }
}
