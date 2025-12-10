package com.example.fayf_android002.RuntimeTest;

import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RuntimeTester {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeTester.class);
    private static FragmentManager fragmentManager;

    private static java.util.Map<String, FragmentInfo> fragments = new java.util.HashMap<>();

    public static class FragmentInfo {
        public final Fragment fragment;
        public final View view;
        public FragmentInfo(Fragment fragment, View view) {
            this.fragment = fragment;
            this.view = view;
        }
    }


    public void register(Menu menu) {
        // TODO

    }







    public RuntimeTester(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }







    public static void registerFragment(String name, Fragment fragment, int fragmentId, View rootView) {
        fragments.put(String.valueOf(fragment.getId()), new FragmentInfo(fragment, rootView)); // allow lookup by ContainerID as well
        fragments.put(String.valueOf(fragmentId), new FragmentInfo(fragment, rootView));
        fragments.put(name, new FragmentInfo(fragment, rootView));
    }

    public static FragmentInfo findFragmentInfo(int id) {
        return fragments.get(String.valueOf(id));
    }

    public static Fragment findFragment(String name) {
        return Objects.requireNonNull(fragments.get(name)).fragment;
    }

    public static Fragment findFragment(int id) {
        return Objects.requireNonNull(fragments.get(String.valueOf(id))).fragment;
    }

    public static Fragment findFragmentOptional(int id) {
        FragmentInfo fragmentInfo = fragments.get(String.valueOf(id));
        return fragmentInfo != null ? fragmentInfo.fragment : null;
    }




    // Find the FragmentManager for a given fragment ID
    public static FragmentManager findFragmentManagerForFragmentId(FragmentManager fragmentManager, int fragmentId) {
        Fragment fragment = fragmentManager.findFragmentById(fragmentId);
        if (fragment != null) {
            return fragmentManager;
        }

        for (Fragment childFragment : fragmentManager.getFragments()) {
            FragmentManager childFragmentManager = childFragment.getChildFragmentManager();
            FragmentManager result = findFragmentManagerForFragmentId(childFragmentManager, fragmentId);
            if (result != null) {
                return result;
            }
        }

        logger.warn("Fragment with ID {} not found in any FragmentManager.", fragmentId);
        return null;
    }

    // Perform action on a Button by its ID in a specific Fragment
    public void performAction(@IdRes int fragmentId, @IdRes int buttonId) {
        Fragment fragment = findFragment(fragmentId);
        if (fragment != null && fragment.getView() != null) {
            View button = fragment.getView().findViewById(buttonId);
            if (button instanceof Button) {
                button.performClick();
            } else {
                throw new IllegalArgumentException("View with ID " + buttonId + " is not a Button.");
            }
        } else {
            throw new IllegalStateException("Fragment or its view is not available.");
        }
    }

    // check if a Fragment is currently visible
    public boolean isFragmentVisible(@IdRes int fragmentId) {
        Fragment fragment = findFragment(fragmentId);
        return fragment != null && fragment.isVisible();
    }

    // Check if a View is visible on screen
    public boolean isViewVisible(@IdRes int fragmentId, @IdRes int viewId) {
        Fragment fragment = findFragment(fragmentId);
        if (fragment != null && fragment.getView() != null) {
            View view = fragment.getView().findViewById(viewId);
            return view != null && view.getVisibility() == View.VISIBLE;
        }
        return false;
    }

    // Retrieve text from a Button or TextView
    public String getTextFromView(@IdRes int fragmentId, @IdRes int viewId) {
        Fragment fragment = findFragment(fragmentId);
        if (fragment != null && fragment.getView() != null) {
            View view = fragment.getView().findViewById(viewId);
            if (view instanceof Button) {
                return ((Button) view).getText().toString();
            } else if (view instanceof TextView) {
                return ((TextView) view).getText().toString();
            } else {
                logger.warn("View with ID {} is not a Button or TextView.", viewId);
                //throw new IllegalArgumentException("View with ID " + viewId + " is not a Button or TextView.");
            }
        }
        logger.warn("Fragment or its view is not available for fragment ID {}.", fragmentId);
        //throw new IllegalStateException("Fragment or its view is not available.");
        return null;
    }

}