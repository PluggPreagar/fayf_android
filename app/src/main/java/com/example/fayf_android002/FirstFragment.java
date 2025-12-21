package com.example.fayf_android002;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.RuntimeTest.RuntimeTester;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.UI.MainItemAdapter;
import com.example.fayf_android002.databinding.FragmentFirstBinding;
import com.example.fayf_android002.UI.ButtonTouchable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FirstFragment extends Fragment {

    private final String FIRST_FRAGMENT = "FirstFragment";
    Logger logger = LoggerFactory.getLogger(FirstFragment.class);

    private View view;

    private Map<Integer, BadgeDrawable> badgeMap = new HashMap<>();

    private RecyclerView recyclerView;
    private MainItemAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        logger.info("FirstFragment onCreate() called");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // to enable menu in fragment
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        logger.info("FirstFragment onCreateView() called");
        view = inflater.inflate(R.layout.fragment_first, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MainItemAdapter(getContext(), Entries.getTopicEntries() );
        recyclerView.setAdapter(adapter);

        RuntimeTester.registerFragment(FIRST_FRAGMENT, this, R.id.FirstFragment, view);

        // Navigating from SecondFragment to FirstFragment will not show the Topic Title
        if (Entries.size()<3) { // TODO ignore /_/config topic
            Entries.load_async(requireContext()); // async load entries - will trigger callback to update buttons
        }

        Entries.setOnTopicChangedListener(FIRST_FRAGMENT, entry -> {
            if (!this.isVisible()) {
                // somehow I removed the listener on onDestroyView
                // but it missed the onCreateView call to re-listen
                logger.info("Topic changed callback received, but Fragment not visible, skipping UI update");
                return; //rather than set/reset listener on onResume/onPause
            }
            onTopicChanged(entry);
        });

        Entries.setOnDataChangedListener(FIRST_FRAGMENT, k -> {
            if (!this.isVisible()) {
                // somehow I removed the listener on onDestroyView
                // but it missed the onCreateView call to re-listen
                logger.info("Data changed callback received, but Fragment not visible, skipping UI update");
                return; //rather than set/reset listener on onResume/onPause
            }
            logger.info("Data changed callback received, updating buttons - but keep topic and offset");
            adapter.updateData( Entries.getTopicEntries(), recyclerView );
        });

        // on overscroll the bottom of button list
        // then top entries are out of reach

        // get ButtomList.onOverScrolledListener

        return view;

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        logger.info("FirstFragment onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);
        // this.view.ButtonScrollView.setOnScrollChangeListener(this);
        UtilDebug.inspectView();

    }

    private void onTopicChanged(EntryKey entryKey) {
        logger.info("FirstFragment onTopicChanged() called: {}", null == entryKey ? "NONE" : entryKey.getFullPath());
        adapter.updateData( Entries.getTopicEntries(), recyclerView );
    }

    public void onResume() {
        logger.info("FirstFragment onResume() called");
        super.onResume();

    }

    public void onPause() {
        logger.info("FirstFragment onPause() called");
        super.onPause();
    }

    public void onStart() {
        logger.info("FirstFragment onStart() called");
        super.onStart();
    }

    public void onStop() {
        logger.info("FirstFragment onStop() called");
        super.onStop();
    }

    public void onDestroy() {
        logger.info("FirstFragment onDestroy() called");
        super.onDestroy();
    }

    public void onDetach() {
        logger.info("FirstFragment onDetach() called");
        super.onDetach();
    }

    public void onAttach(@NonNull android.content.Context context) {
        logger.info("FirstFragment onAttach() called");
        super.onAttach(context);
    }

    /*
        Fragment Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
        It can be used to do final initialization once these pieces are in place, such as retrieving views or restoring
        state. It is also useful for fragments that use setRetainInstance(boolean) to retain their instance, as this
        callback tells the fragment when it is fully associated with the new activity instance. This is called after
        onCreateView and before onViewStateRestored(Bundle).
     */
    public void onActivityCreated(Bundle savedInstanceState) {
        logger.info("FirstFragment onActivityCreated() called");
        super.onActivityCreated(savedInstanceState);
    }

    public void onViewStateRestored(Bundle savedInstanceState) {
        logger.info("FirstFragment onViewStateRestored() called");
        super.onViewStateRestored(savedInstanceState);
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        logger.info("FirstFragment onSaveInstanceState() called");
        super.onSaveInstanceState(outState);
    }

    public void onLowMemory() {
        logger.info("FirstFragment onLowMemory() called");
        super.onLowMemory();
    }

    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        logger.info("FirstFragment onConfigurationChanged() called");
        super.onConfigurationChanged(newConfig);
    }

    public void onHiddenChanged(boolean hidden) {
        logger.info("FirstFragment onHiddenChanged() called, hidden: {}", hidden);
        super.onHiddenChanged(hidden);
    }


    public void onInflate(@NonNull android.content.Context context, @NonNull android.util.AttributeSet attrs, Bundle savedInstanceState) {
        logger.info("FirstFragment onInflate() called");
        super.onInflate(context, attrs, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState, android.content.res.Configuration newConfig) {
        logger.info("FirstFragment onViewCreated() called with newConfig");
        super.onViewCreated(view, savedInstanceState);
    }






    /*
        Actions

     */


    public void navigateToEdit(EntryKey entryKey) {
        Entries.setCurrentEntryKey(entryKey);
        try {
            MainActivity.getInstance().switchToInputFragment();
        } catch (Exception ex) {
            // needed due to timing issues
            logger.error("Navigation to SecondFragment failed: {}", ex.getMessage());
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logger.info("FirstFragment onDestroyView() called");
        view = null;
    }


}