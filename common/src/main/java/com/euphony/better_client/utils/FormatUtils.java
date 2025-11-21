package com.euphony.better_client.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatUtils {
    private static final Pattern pattern = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");

    public static String format(String template, Map<String, Object> values) {
        Matcher matcher = pattern.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object replacement = values.getOrDefault(key, "{" + key + "}");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(replacement)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
