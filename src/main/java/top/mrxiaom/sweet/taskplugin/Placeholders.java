package top.mrxiaom.sweet.taskplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;

public class Placeholders extends PlaceholderExpansion {
    private final SweetTask plugin;
    public Placeholders(SweetTask plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean register() {
        try {
            unregister();
        } catch (Throwable ignored) {
        }
        return super.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("count_daily")) {
            return String.valueOf(TaskManager.inst().getDailyCount(player));
        }
        if (params.equalsIgnoreCase("count_weekly")) {
            return String.valueOf(TaskManager.inst().getWeeklyCount(player));
        }
        if (params.equalsIgnoreCase("count_monthly")) {
            return String.valueOf(TaskManager.inst().getMonthlyCount(player));
        }
        return super.onPlaceholderRequest(player, params);
    }
}
