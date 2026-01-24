package top.mrxiaom.sweet.taskplugin.tasks;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.taskplugin.database.entry.TaskCache;
import top.mrxiaom.sweet.taskplugin.func.TaskManager;
import top.mrxiaom.sweet.taskplugin.func.entry.LoadedTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface ITask {
    /**
     * 该任务的类型
     */
    String type();

    /**
     * 该任务在 ActionBar 显示的消息提示
     */
    String actionTips();

    /**
     * 获取该任务当前进度
     */
    default int getValue(Player player, LoadedTask task, TaskCache cache, int index) {
        return cache.get(index, type());
    }

    /**
     * 该任务的目标进度数值
     */
    int getTargetValue();

    /**
     * 获取替换变量列表
     * @param data 目标的当前进度数值
     */
    default List<Pair<String, Object>> actionReplacements(int data) {
        int target = getTargetValue();
        List<Pair<String, Object>> replacements = new ArrayList<>();
        replacements.add(Pair.of("%current%", Math.min(data, target)));
        replacements.add(Pair.of("%max%", target));
        return replacements;
    }

    /**
     * 加载一个子任务目标
     * @param s 子任务字符串配置
     * @return 如果找不到任务，则返回<code>null</code>
     */
    @Nullable
    static ITask load(@NotNull String s) {
        return load(null, null, s);
    }

    /**
     * 加载一个子任务目标
     * @param parent 任务管理器，在出现问题时用于日志显示，使用<code>null</code>则不显示日志
     * @param parentTaskId 父任务ID，作用于<code>parent</code>相同
     * @param s 子任务字符串配置
     * @return 如果找不到任务，则返回<code>null</code>
     */
    @Nullable
    static ITask load(@Nullable TaskManager parent, @Nullable String parentTaskId, @NotNull String s) {
        String[] args;
        String actionTips;
        // 分号后面的为 actionbar 提示，其余部分使用 空格 隔开作为参数
        if (s.contains(";")) {
            int index = s.lastIndexOf(';');
            actionTips = s.substring(index + 1).trim();
            args = s.substring(0, index).trim().split(" ");
        } else {
            actionTips = "";
            args = s.split(" ");
        }
        // 加载子任务
        String id = args[0];
        ITaskParser parser = Internal.parsers.get(id);
        if (parser != null) {
            return parser.parse(args, actionTips, warnMsg -> {
                if (parent != null && parentTaskId != null) {
                    parent.warn("[任务][" + parentTaskId + "][" + id + "] " + warnMsg);
                }
            });
        }
        if (parent != null && parentTaskId != null) {
            parent.warn("[任务][" + parentTaskId + "] 找不到可用的 " + id + " 子任务");
        }
        return null;
    }
    static void registerParser(String id, ITaskParser parser) {
        Internal.parsers.put(id, parser);
    }
    static void unregisterParser(String id) {
        Internal.parsers.remove(id);
    }

    class Internal {
        private static final Map<String, ITaskParser> parsers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }
}
