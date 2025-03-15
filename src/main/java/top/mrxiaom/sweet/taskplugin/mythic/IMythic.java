package top.mrxiaom.sweet.taskplugin.mythic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IMythic {
    @Nullable
    String getMobType(LivingEntity entity);

    @Nullable
    ItemStack getItem(String id);
}
