package com.example.fayf_android002.RuntimeTest;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.example.fayf_android002.MainActivity;

public class UtilDebug {

    private static final String TAG = "UiViewDebugHelper";



    public static void inspectView() {
        inspectView(null, 0);
    }

    private static void inspectView(View view, int depth) {
        if (view == null) {
            view = MainActivity.getInstance().findViewById(android.R.id.content);
        }

        // Indentation for better readability in logs
        String indent = new String(new char[depth]).replace("\0", "  ");

        // Get view details
        String viewName = view.getClass().getSimpleName();
        int visibility = view.getVisibility();
        String visibilityStatus = getVisibilityStatus(visibility);
        int width = view.getWidth();
        int height = view.getHeight();
        int x = (int) view.getX();
        int y = (int) view.getY();

        // Get the XML-defined name (if available)
        String resourceName = "";
        if (view.getId() != View.NO_ID) {
            try {
                resourceName = view.getResources().getResourceEntryName(view.getId());
            } catch (Exception e) {
                resourceName = "unknown";
            }
        }

        // Log view details
        // Log.d(TAG, indent + "View: " + viewName + ", Name: " + resourceName + ", Visibility: " + visibilityStatus + ", Size: " + width + "x" + height);

        // If the view is a ViewGroup, inspect its children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            Log.d(TAG, indent +  viewName +
                    (resourceName.isEmpty() ? "" : ", " + resourceName) +
                    " (" + childCount + " child)" +
                    ", " + visibilityStatus +
                    " (" + x + ", " + y + ") " + width + "x" + height
            );

            for (int i = 0; i < childCount; i++) {
                inspectView(viewGroup.getChildAt(i), depth + 1);
            }
        } else {
            Log.d(TAG, indent +  viewName +
                    (resourceName.isEmpty() ? "" : ", " + resourceName) +
                    ", " + visibilityStatus +
                    " (" + x + ", " + y + ") " + width + "x" + height
            );

        }
    }

    private static String getVisibilityStatus(int visibility) {
        switch (visibility) {
            case View.VISIBLE:
                return "VISIBLE";
            case View.INVISIBLE:
                return "INVISIBLE";
            case View.GONE:
                return "GONE";
            default:
                return "UNKNOWN";
        }
    }


    public static String getResourceName(View view) {
        if (view.getId() != View.NO_ID) {
            try {
                return view.getResources().getResourceEntryName(view.getId());
            } catch (Exception e) {
                return "unknown";
            }
        }
        return "no_id";
    }

    /*
        Stack trace helper
     */
    public static void logCompactCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String packageName = "com.example.fayf_android002";

        StringBuilder compactStack = new StringBuilder("Compact Call Stack:\n");
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains(packageName) && !element.getMethodName().equals("logCompactCallStack")) {
                compactStack.append("at ").append(element.toString()).append("\n");
            }
        }

        Log.d(TAG, compactStack.toString());
    }

}