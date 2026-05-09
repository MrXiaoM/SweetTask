package top.mrxiaom.sweet.taskplugin.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.util.jnbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Mythic4 implements IMythic {
    MythicMobs mythic = MythicMobs.inst();

    @Override
    @Nullable
    public String getMobType(LivingEntity entity) {
        ActiveMob mob = mythic.getMobManager().getMythicMobInstance(entity);
        return mob == null ? null : mob.getMobType();
    }

    @Nullable
    @Override
    public ItemStack getItem(String id) {
        return mythic.getItemManager().getItemStack(id);
    }

    @Nullable
    @Override
    public String getMythicId(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() <= 0) {
            return null;
        }
        CompoundTag data = mythic.getVolatileCodeHandler().getItemHandler().getNBTData(item);
        if (data != null && data.containsKey("MYTHIC_TYPE")) {
            return data.getString("MYTHIC_TYPE");
        }
        return null;
    }
}
