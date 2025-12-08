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
    public boolean persist() {
        return true;
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
            return String.valueOf(TaskManager.inst().typeDaily().getMaxTasksCount(player));
        }
        if (params.equalsIgnoreCase("count_weekly")) {
            return String.valueOf(TaskManager.inst().typeWeekly().getMaxTasksCount(player));
        }
        if (params.equalsIgnoreCase("count_monthly")) {
            return String.valueOf(TaskManager.inst().typeMonthly().getMaxTasksCount(player));
        }
        if (params.equalsIgnoreCase("refresh_count_daily")) {
            return String.valueOf(TaskManager.inst().typeDaily().getMaxRefreshCount(player));
        }
        if (params.equalsIgnoreCase("refresh_count_weekly")) {
            return String.valueOf(TaskManager.inst().typeWeekly().getMaxRefreshCount(player));
        }
        if (params.equalsIgnoreCase("refresh_count_monthly")) {
            return String.valueOf(TaskManager.inst().typeMonthly().getMaxRefreshCount(player));
        }
        return super.onPlaceholderRequest(player, params);
    }
}
