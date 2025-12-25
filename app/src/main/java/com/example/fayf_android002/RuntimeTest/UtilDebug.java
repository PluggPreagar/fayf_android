package com.example.fayf_android002.RuntimeTest;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.*;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.Util;

import static android.view.MotionEvent.*;

public class UtilDebug {

    private static final String TAG = "UtilDebug";

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
                    (resourceName.isEmpty() ? "" : " \"" + resourceName+"\"") +
                    ( view.getId() != View.NO_ID ? " (" + view.getId() + ")" : "") +
                    " (" + childCount + " child)" +
                    ", " + visibilityStatus +
                    " (" + x + ", " + y + ") " + width + "x" + height
            );

            for (int i = 0; i < childCount; i++) {
                inspectView(viewGroup.getChildAt(i), depth + 1);
            }
        } else {
            String textContent = "";
            if (view instanceof TextView){
                textContent = ((TextView) view).getText().toString();
            } else if (view instanceof Button){
                textContent = ((Button) view).getText().toString();
            }

            Log.d(TAG, indent +  viewName +
                    (resourceName.isEmpty() ? "" : " \"" + resourceName+"\"") +
                    ( view.getId() != View.NO_ID ? " (" + view.getId() + ")" : "") +
                    ", " + visibilityStatus +
                    " (" + x + ", " + y + ") " + width + "x" + height
                    + (textContent.isEmpty() ? "" : " \"" + Util.shortenString(textContent, 50) + "\"")
            );

        }
    }

    public static String getVisibilityStatus(int visibility) {
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

    public static String getVisibilityStatus(View view) {
        return getVisibilityStatus(view.getVisibility());
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


    public static MenuItem getMenuItem(int viewId) {
        Menu menu = MainActivity.getInstance().menu;
        return null == menu ? null : menu.findItem(viewId);
    }

    public static View getView(int viewId) {
        return getView(null, viewId);
    }

    public static View getView(View view, int viewId) {
        View matchingView = null;
        if (view == null) {
            // check for menu items
            view = MainActivity.getInstance().findViewById(android.R.id.content);
        }
        // If the view is a ViewGroup, inspect its children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();

            for (int i = 0; i < childCount; i++) {
                matchingView = getView(viewGroup.getChildAt(i), viewId);
                if (matchingView != null)
                    break;
            }
        } else if (view.getId() == viewId) {
                matchingView = view;
        } else if (view instanceof RecyclerView) {
            // get child views of recycler view
            RecyclerView recyclerView = (RecyclerView) view;
            int itemCount = recyclerView.getChildCount();
            for (int i = 0; i < itemCount; i++) {
                matchingView = getView(recyclerView.getChildAt(i), viewId);
                if (matchingView != null)
                    break;
            }
        } else {
            // Log.d(TAG, "getView: not matching view " + getResourceName(view) + " (" + view.getId() + ")");
        }
        return matchingView;
    }



    public static int getView(String text) {
        View view = getView(null, text);
        return null != view ? view.getId() : View.NO_ID;
    }


    public static View getView(View view, String text) {
        View matchingView = null;
        String viewText = null;
        if (view == null) {
            view = MainActivity.getInstance().findViewById(android.R.id.content);
        }
        // If the view is a ViewGroup, inspect its children
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();

            for (int i = 0; i < childCount; i++) {
                matchingView = getView(viewGroup.getChildAt(i), text);
                if (matchingView != null)
                    break;
            }
        } else if (view instanceof android.widget.TextView) {
            TextView textView = (TextView) view;
            viewText = textView.getText().toString();
        } else if (view instanceof android.widget.Button) {
            android.widget.Button button = (android.widget.Button) view;
            viewText = button.getText().toString();
        } else if (view instanceof RecyclerView) {
            // get child views of recycler view
            RecyclerView recyclerView = (RecyclerView) view;
            int itemCount = recyclerView.getChildCount();
            for (int i = 0; i < itemCount; i++) {
                matchingView = getView(recyclerView.getChildAt(i), text);
                if (matchingView != null)
                    break;
            }
        } else {
            // Log.d(TAG, "getView: not matching view " + getResourceName(view) + " (" + view.getId() + ")");
        }
        if (null != viewText &&  viewText.equals(text)){
            Log.d(TAG, "getView: found matching view " + getResourceName(view) + " (" + view.getId() + ") with text \"" + text + "\"");
            matchingView = view;
        }
        return matchingView;
    }




    /*
        Stack trace helper
     */
    public static void logCompactCallStack() {
        logCompactCallStack("Compact Call Stack:");
    }

    public static void logCompactCallStack(String prefix) {
        Log.d(TAG, getCompactCallStack(prefix));
    }

    public static String getCompactCallStack(String prefix) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String packageName = "com.example.fayf_android002";

        StringBuilder compactStack = new StringBuilder(prefix + "\n");
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains(packageName)
                    && !element.getClassName().contains("TextViewAppender") // exclude logging helper methods
                    && !element.getMethodName().equals("logCompactCallStack")) {
                compactStack.append("\tat ").append(element.toString()).append("\n");
            }
        }
        return compactStack.toString();
    }


    public static String eventToStr(MotionEvent event) {
        if (event == null) {
            return "MotionEvent: null";
        }
        return "MotionEvent: ACTION_"
                +((ACTION_DOWN == event.getAction()) ? "DOWN" :
                        ((ACTION_UP == event.getAction()) ? "UP" :
                                ((ACTION_MOVE == event.getAction()) ? "MOVE" :
                                        ((ACTION_CANCEL == event.getAction()) ? "CANCEL" :
                                                Integer.toString(event.getAction()))))
                ) +
                " " + event.getX() +
                ", " + event.getY();
    }

    public static String getBackgroundColorOfButton(View view){
        String colorInfo = "N/A";
        if (view != null) {
            Drawable background = view.getBackground();
            colorInfo = background.toString();
            Log.d(TAG, "Background of view " + getResourceName(view) + " (" + view.getId() + "): " + background.toString());
        } else {
            Log.d(TAG, "View is null.");
        }
        return colorInfo;
    }

    public static void logError(String s, Exception e) {
        Log.e(TAG, s + " Exception: " + e.getMessage());
    }

    /*
        view log helpers
     */

    public static String logName(@IdRes int viewId) {
        View view = UtilDebug.getView(viewId);
        if ( null != view ) {
            return logName(view);
        } else {
            return String.valueOf(viewId );
        }
    }


    public static String logName(View view) {
        return "\""+ UtilDebug.getResourceName(view) + "\" (" + view.getClass().getSimpleName() + " " + view.getId() + " )";
    }
}