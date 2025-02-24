package top.mrxiaom.sweet.taskplugin.utils;

import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.taskplugin.matchers.BlockMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Utils {
    @Nullable
    public static Pair<List<String>, Integer> getOrList(String[] args, int startIndex) {
        List<String> list = new ArrayList<>();
        boolean flag = false;
        int index = startIndex;
        for (int i = startIndex; i < args.length; i++) {
            String s = args[i];
            if (flag) {
                list.add(s);
                index = i + 1;
                break;
            }
            if (s.equals("or")) {
                list.add(",");
                flag = true;
            } else {
                list.add(s);
            }
        }
        if (index == startIndex) return null;
        List<String> split = Util.split(String.join("", list), ',');
        list.clear();
        return Pair.of(split, index);
    }

    public static <T> List<T> convert(Pair<List<String>, Integer> pair, String input, String name, Function<String, T> of, Consumer<String> warn) {
        List<T> list = new ArrayList<>();
        if (pair == null) {
            T entry = of.apply(input);
            if (entry == null) {
                warn.accept("未输入" + name + "列表");
                return null;
            } else {
                list.add(entry);
            }
        }
        if (pair != null) for (String str : pair.getKey()) {
            T entry = of.apply(str);
            if (entry == null) {
                warn.accept("输入的" + name + " " + str + " 格式错误或不存在");
                continue;
            }
            list.add(entry);
        }
        if (list.isEmpty()) {
            warn.accept("未输入" + name + "列表");
            return null;
        }
        return list;
    }
}
