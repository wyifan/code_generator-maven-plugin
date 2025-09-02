package com.yifan.code_generator_maven_plugin.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yifan.code_generator_maven_plugin.constant.Constants.MergeStrategy;

public class CommonFunc {
    private CommonFunc() {
    }

    /**
     * 通用方法：合并两个 Map，并指定重复键的合并策略。
     *
     * @param map1          第一个 Map
     * @param map2          第二个 Map
     * @param mergeStrategy 重复键的合并策略，可以选择 KEEP_FIRST 或 KEEP_SECOND
     * @return 合并后的新 Map
     */
    public static <K, V> Map<K, V> mergeMaps(Map<K, V> map1, Map<K, V> map2, MergeStrategy mergeStrategy) {
        // 确保 map1 不为空，否则抛出异常
        if (map1 == null) {
            throw new IllegalArgumentException("map1 cannot be null.");
        }

        // 如果 map2 为空，直接返回 map1 的副本
        if (map2 == null || map2.isEmpty()) {
            return new HashMap<>(map1);
        }

        // 创建一个合并函数，根据指定的策略来决定保留哪个值
        BinaryOperator<V> mergeFunction = (mergeStrategy == MergeStrategy.KEEP_FIRST)
                ? (BinaryOperator<V>) (oldValue, newValue) -> oldValue
                : (BinaryOperator<V>) (oldValue, newValue) -> newValue;

        // 使用 Stream API 高效地合并两个 Map
        return Stream.of(map1, map2)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        mergeFunction // 将合并函数传递给 Collectors.toMap
                ));
    }

    /**
     * 合并两个 List，根据指定的键值提取函数和合并策略处理重复项。
     *
     * @param list1        第一个 List
     * @param list2        第二个 List
     * @param keyExtractor 一个函数，用于从 List 中的对象提取键（如 entityName）
     * @return 合并后的新 List
     */
    public static <T, K> List<T> mergeLists(List<T> list1, List<T> list2, Function<? super T, ? extends K> keyExtractor) {
        // 如果 list2 为空，直接返回 list1 的副本
        if (list2 == null || list2.isEmpty()) {
            return list1;
        }

        // 定义合并函数：保留第二个值（即新值）
        BinaryOperator<T> mergeFunction = (oldValue, newValue) -> newValue;

        // 将两个 List 的元素流合并成一个 Map
        Map<K, T> mergedMap = Stream.of(list1, list2)
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        keyExtractor, // 提取键的函数
                        Function.identity(), // 提取值（就是对象本身）
                        mergeFunction // 冲突时保留第二个
                ));

        // 将 Map 的值转换回 List
        return mergedMap.values().stream().collect(Collectors.toList());
    }

    /**
     * Converts a camelCase string to a snake_case string.
     *
     * @param camelCase The input camelCase string.
     * @return The converted snake_case string.
     */
    public static String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    /**
     * 将一个用下划线或连字符分隔的字符串转换为大驼峰命名法（PascalCase）。
     * <p>例如: "log_common" -> "LogCommon", "my-service" -> "MyService"</p>
     *
     * @param input 原始字符串，例如 "log_common" 或 "my-service"
     * @return 转换后的字符串，例如 "LogCommon"
     */
    public static String convertToPascalCase(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // 使用下划线和连字符作为分隔符
        return Arrays.stream(input.split("[_-]"))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining());
    }
}
