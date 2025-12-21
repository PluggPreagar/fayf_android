package com.example.fayf_android002;

import android.view.MotionEvent;
import com.example.fayf_android002.Entry.EntryStyle;

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

    public static String appendIfFilled(String str, String toAppend) {
        return str != null && !str.isEmpty() ? str + toAppend : "";
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

    public static String getCurrentTimestamp() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }

    public static int parseIntOr(String s, int i) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return i;
        }
    }

    public static String shortenLeft(String str, int maxLength) {
        // cut prefix, remove lower case letters from the left if too long
        if (null == str) {
            return "";
        } else if (str.length() <= maxLength) {
            return str;
        }
        // remove lower case letters from the left until maxLength is reached
        int l = -1;
        while (str.length() > maxLength && l != str.length()) {
            l = str.length();
            str = str.replaceFirst("[a-z0-9_-]|\\.+(\\.)", "$1");
        }
        // if still too long, cut from the left
        if (str.length() > maxLength) {
            str = str.substring(str.length() - maxLength);
        }
        return str;
    }

    public static void postDelayed(Runnable runnable, long delayMillis) {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(runnable, delayMillis);
    }

    public static String convertTime(long buildTime) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = new java.util.Date(buildTime);
        return sdf.format(date);
    }

    public String shortEventString(MotionEvent event) {
        if (null == event) {
            return "null";
        }
        // Action as String
        String actionString;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionString = "DOWN";
                break;
            case MotionEvent.ACTION_UP:
                actionString = "UP";
                break;
            case MotionEvent.ACTION_MOVE:
                actionString = "MOVE";
                break;
            case MotionEvent.ACTION_CANCEL:
                actionString = "CANCEL";
                break;
            default:
                actionString = "OTHER";
                break;
        }
        return actionString + String.format(" (x=%.1f, y=%.1f)", event.getX(), event.getY());
    }

    public Integer parseIntegerOptional(String s, Integer defaultValue) {
        try {
            return Integer.valueOf(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
