package top.mrxiaom.sweet.taskplugin.mythic;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
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
}
