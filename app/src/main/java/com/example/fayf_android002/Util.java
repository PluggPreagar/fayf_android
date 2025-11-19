package com.example.fayf_android002;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

    public static String shortenString(String str, int maxLength) {
        // shorten by keeping the beginning and end, insert "..." if too long
        if (null == str) {
            return "";
        } else if (str.length() <= maxLength) {
            return str;
        } else {
            int partLength = (maxLength - 3) / 2;
            return str.substring(0, partLength) + "..." + str.substring(str.length() - partLength);
        }
    }

    public static String optional(String str, String defaultStr) {
        if (null == str || str.isEmpty()) {
            return defaultStr;
        } else {
            return str;
        }
    }

    public static boolean isFilled(String str) {
        return str != null && !str.isEmpty();
    }




    public static String encodeToUrlParam(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean asBoolean(String str) {
        if (null == str) {
            return false;
        }
        String lower = str.toLowerCase();
        return lower.equalsIgnoreCase("true") || lower.equals("1")
                || lower.equalsIgnoreCase("yes") || lower.equalsIgnoreCase("y")
                || lower.equalsIgnoreCase("on");
    }

}
