package top.mrxiaom.sweet.taskplugin.mythic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class Mythic5 implements IMythic {
    MythicBukkit mythic = MythicBukkit.inst();

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
