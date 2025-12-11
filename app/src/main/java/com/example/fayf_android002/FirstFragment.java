package com.example.fayf_android002;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.RuntimeTest.RuntimeTester;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.UI.CustomOnTouchListener;
import com.example.fayf_android002.databinding.FragmentFirstBinding;
import com.example.fayf_android002.UI.ButtonTouchable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class FirstFragment extends Fragment implements NestedScrollView.OnScrollChangeListener {

    private final String FIRST_FRAGMENT = "FirstFragment";
    Logger logger = LoggerFactory.getLogger(FirstFragment.class);

    private FragmentFirstBinding binding;

    private boolean blockRekursiveScroll = true;
    private NestedScrollView scrollView = null;

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
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        RuntimeTester.registerFragment(FIRST_FRAGMENT, this, R.id.FirstFragment, binding.getRoot());

        // initializeButtons(); // to early here ?

        // Navigating from SecondFragment to FirstFragment will not show the Topic Title
        getMainActivity().updateActionBarTitle();

        if (Entries.size()<3) { // TODO ignore /_/config topic
            /* get Data */
            Entries.load_async(requireContext()); // async load entries - will trigger callback to update buttons
        }

        //((MainActivity) requireActivity()).getFab().setVisibility(View.GONE);
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
            updateButtonsUIThread();
        });

        // on overscroll the bottom of button list
        // then top entries are out of reach

        // get ButtomList.onOverScrolledListener
        /*
        binding.ButtonScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == oldScrollY) {
                return; // no scroll // may happen while WO top-out-of-reach issue
            }
            if (blockRekursiveScroll && 0 == scrollY) {
                blockRekursiveScroll = false;
                return; // skip first call after programmatic scroll
            }
            View view = binding.ButtonScrollView.getChildAt(binding.ButtonScrollView.getChildCount() - 1);
            int diff = (view.getBottom() - (binding.ButtonScrollView.getHeight() + binding.ButtonScrollView.getScrollY()));
            if (diff == 0) {
                // we are at the bottom
                logger.info("ScrollView reached bottom");
                if (true) {
                    logger.info("Scrolling load more entries skipped - TEST");
                }
                if (Entries.changeOffsetBy(5)){
                    // load next 20 entries
                    logger.info("Loading next entries after overscroll at bottom");
                    updateButtonsUIThread();
                } else {
                    logger.info("No more entries to load after overscroll at bottom");
                }
            }
            // if on overscroll at top
            if (binding.ButtonScrollView.getScrollY() == 0) {
                logger.info("ScrollView reached top");
                // we are at the top
                if (Entries.changeOffsetBy(-5)){
                    // load previous 20 entries
                    updateButtonsUIThread();
                    logger.info("Loading previous entries after overscroll at top");
                } else {
                    logger.info("No previous entries to load after overscroll at top");
                }
                // try to fix the top-out-of-reach--after--scrolling-bottom issue
                if (scrollX>0) {
                    binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
                    binding.ButtonScrollView.post(() -> binding.ButtonScrollView.scrollTo(0, 0));
                    // scroll to see first button
                    binding.button1.requestFocus();
                }
            }
        });
        */


        // try to fix the top-out-of-reach--after--scrolling-bottom issue
        //binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
        //binding.ButtonScrollView.post(() -> binding.ButtonScrollView.scrollTo(0, 0));
        // scroll to see first button
        //binding.button1.requestFocus();

        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        logger.info("FirstFragment onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);
        binding.ButtonScrollView.setOnScrollChangeListener(this);
        UtilDebug.inspectView();

/*
        binding.button1.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        if (getArguments() != null) {
            String entryFullPath = getArguments().getString("entryFullPath", "");
            // fullPath = topic / nodeId
            String topic = Entry.getTopicFromFullPath(entryFullPath);
            logger.info("Received argument entryFullPath: {}, setting topic to: {}", entryFullPath, topic);
        }

        if (0 == Entries.size()) {
            logger.info("Entries not loaded yet, will update buttons after loading");
        } else {
            logger.info("Entries already loaded ({} entries), updating buttons", Entries.size());
            updateButtonsUIThread();
        }
*/
        // binding.button1.requestFocus();
    }

    private void onTopicChanged(EntryKey entryKey) {
        logger.info("FirstFragment onTopicChanged() called: {}", null == entryKey ? "NONE" : entryKey.getFullPath());
        updateButtonsUIThread();
        // scroll to top, show appbar
        binding.ButtonScrollView.scrollTo(0, 0);
        MainActivity.getInstance().runOnUiThread(() -> {
            // ensure scroll to top and appbar visible
            binding.ButtonScrollView.fullScroll(View.FOCUS_UP);
            binding.ButtonScrollView.scrollTo(0, 0);
            AppBarLayout appBarLayout =  MainActivity.getInstance().findViewById(R.id.appbar);
            appBarLayout.setExpanded(true, true); // Scrolls the toolbar into viewc
        });
        binding.ButtonScrollView.post(() ->
                {
                    /*
                    logger.debug("Scrolling to top after topic change");
                    binding.ButtonScrollView.fullScroll(View.FOCUS_UP);
                    binding.ButtonScrollView.scrollTo(0, 0);

                     */
                } // scroll to top
        );
    }

    public void onResume() {
        logger.info("FirstFragment onResume() called");
        super.onResume();
        // called when getting from InputFragment back to FirstFragment
        updateButtonsUIThread(); // in case entries changed while in background

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
            B U T T O N S
     */

    private MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }


    // add 10 Buttons dynamically to ButtonList
    public void initializeButtons() {
        ViewGroup buttonList = getMainActivity().findViewById(R.id.ButtonList);
        // TODO -
        if (buttonList == null) {
            logger.error("ButtonList ViewGroup not found in MainActivity");
            return;
        }
        if (false) {
            logger.info("initializeButtons() skipped - already initialized");
            binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
            return;
        }
        logger.info("Initializing buttons in ButtonList {} ", buttonList.getChildCount());
        //buttonList.removeAllViews(); // clear existing buttons
        /*
                app:layout_constraintTop_toBottomOf="@id/button2"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                app:layout_constraintStart_toStartOf="parent"
                app:layout_constraintEnd_toEndOf="parent"
         */
        int uniqueIdBase = 1000; // base for unique IDs
        for (int i = buttonList.getChildCount(); i < 20 ; i++) {
            Button button = (Button) LayoutInflater.from(requireContext()).inflate(R.layout.button_clone, null);
            buttonList.addView(button);
            /*
            Button button = new Button(getActivity());
            button.setId(uniqueIdBase + i); // set unique ID
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            buttonList.addView(button);

             */
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone((ConstraintLayout) buttonList);
            if (i == 0) {
                constraintSet.connect(button.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16);
            } else {
                constraintSet.connect(button.getId(), ConstraintSet.TOP, buttonList.getChildAt(i - 1).getId(), ConstraintSet.BOTTOM, 16);
            }
            constraintSet.connect(button.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 16);
            constraintSet.connect(button.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16);
            constraintSet.applyTo((ConstraintLayout) buttonList);


            //
            button.setText("Button " + (i + 1));
        }

        for (int i = 0; i < buttonList.getChildCount(); i++) {
            View button = buttonList.getChildAt(i);
            if (button instanceof Button) {

            }
        }
        //binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
        logger.info("Initialized buttons in ButtonList, total count: {} ", buttonList.getChildCount());
    }



    public void updateButtonsUIThread() {
        requireActivity().runOnUiThread(() -> {
            initializeButtons();
            logger.info("Render in UI-Thread ({} entries)", Entries.size());
            updateButtons(); // update buttons after entries loaded
        });
    }

    public void updateButtons() {
        updateButtons( Entries.getOffset());
    }

    public void updateButtons( int offset) {
        int limit = 20; // buttonList.getChildCount();
        logger.info("Updating buttons for topic: {} , offset: {}, limit: {} max: {} "
                , Entries.toString(Entries.getCurrentEntryKey())
                , offset, limit, Entries.getCurrentTopicSize());
        UtilDebug.logCompactCallStack("FirstFragment.updateButtons(" + offset + ")");
        ViewGroup buttonList = getMainActivity().findViewById(R.id.ButtonList);
        if (buttonList == null) {
            logger.error("ButtonList ViewGroup not found in MainActivity");
            return;
        }
        if (null == binding || null == binding.ButtonScrollView) {
            logger.error("Binding or ButtonScrollView is null, cannot update buttons");
            return;
        }
        if (false) {
            logger.info("updateButtons() skipped - TEST");
            return;
        }
        String topic = Entries.getCurrentEntryKey().getFullPath();
        Iterator<Map.Entry<String, Entry>> entriesIterator = Entries.getEntriesIterator( offset);
        int idx = 0;
        if (!entriesIterator.hasNext()) {
            logger.info("No entries found for topic: {}, offset: {}", topic, offset);
            //Entries.upOneTopicLevel(); // will trigger callback to update buttons again --> RECURSIVE LOOP !!
            if (!topic.equals(EntryTree.ROOT_ENTRY_KEY.getFullPath())) {
                // Toast message - suppressed to avoid spam on startup
                Toast.makeText(getActivity(), "No entries found for the selected topic.", Toast.LENGTH_SHORT).show();
            }
        } else {

            while (entriesIterator.hasNext() && limit > 0) {
                Map.Entry<String, Entry> e = entriesIterator.next();
                String nodeId = e.getKey();
                Entry entry = e.getValue();
                logger.info("Entry: {}", entry.content);
                // KLUDGE  iterate over Buttons in ButtonList

                // iterate over Buttons in ButtonList
                View button = buttonList.getChildAt(idx);
                if (button instanceof ButtonTouchable) {

                    ButtonTouchable btn = (ButtonTouchable) button;
                    btn.setEntry(new EntryKey( topic, nodeId), this); // set entry and fragment reference, allow runtime test

                    if (topic.startsWith(Config.CONFIG_PATH)) {
                        btn.setText("" + Config.DisplayName( nodeId ) + ": " + entry.content);
                    } else {
                        btn.setText(entry.content);
                    }
                    if (Entries.isTopic(new EntryKey( topic, nodeId))) {
                        btn.setContentDescription("Topic: " + entry.content);
                        btn.setTextAppearance(R.style.TopicButtonStyle);
                        btn.setIconResource(R.drawable.baseline_chevron_right_24);
                    } else {
                        btn.setContentDescription("Note: " + entry.content);
                        btn.setTextAppearance(R.style.NoteButtonStyle);
                        btn.setIconResource(R.drawable.ic_baseline_note_24);
                    }

                    btn.setVisibility(View.VISIBLE);
                    // make button height larger
                    MaterialButton btn_m = (MaterialButton) button;
                    btn_m.setInsetTop( 0);
                    btn_m.setInsetBottom(0);
                    // compensate with margins
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
                    params.topMargin = 10;
                    params.bottomMargin = 10;
                    button.setLayoutParams(params);

                    btn.setOnTouchListener(new CustomOnTouchListener(this) {

                        @Override
                        public void onClick() {
                            //Toast.makeText(getActivity(), "Click: " + entry.content, Toast.LENGTH_SHORT).show();
                            btn.performClick(); // Fragment calls updateButtonsUIThread() after changing topic
                            // updateButtonsUIThread(); // TODO should be called from Entries after topic change
                        }
                        @Override
                        public void onLongClick() {
                            //Toast.makeText(getActivity(), "LongClick: " + entry.content, Toast.LENGTH_SHORT).show();
                            btn.performLongClick();
                        }

                        @Override
                        public void onSwipeRight() {
                            MainActivity.notifyUser("Up(" + entry.rank + ") ");
                            //Entries.setTopicEntry(entry); // set topic to this entry
                            Entries.voteUp(btn.getEntryKey());
                        }
                        @Override
                        public void onSwipeLeft() {
                            MainActivity.notifyUser("Down(" + entry.rank + ") ");
                            //navigateToEdit(entry); // navigate to edit this entry
                            Entries.voteDown(btn.getEntryKey());
                        }


                    });


                } else {
                    logger.warn("Button at index {} is not ButtonTouchable, but {}", idx, button.getClass().getName());
                }

                limit--;
                idx++;
            }
        }

        logger.info("{} Buttons updated, hiding remaining buttons if any", idx);
        while (limit > 0) {
            if (idx >= buttonList.getChildCount() || buttonList.getChildAt(idx) == null) {
                logger.warn("No more buttons available in ButtonList to hide (idx: {}, childCount: {})", idx, buttonList.getChildCount());
                break;
            }
            buttonList.getChildAt(idx).setVisibility(View.GONE);
            limit--;
            idx++;
        }


        //
        // getMainActivity().getSupportActionBar().setTitle("FAYF - " + topic);
        if (null != binding.ButtonScrollView){
            binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
        }
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
        binding = null;
    }

    /*
            S C R O L L V I E W
     */

    // prevent self bounce

    // 0 == no scroll in progress
    // t = timestamp for reset
    public static long scrollingInProgress = 0;

    public static boolean isScrollingInProgress() {
        if (0 == scrollingInProgress) {
            return false;
        }
        if (System.currentTimeMillis() > scrollingInProgress) {
            scrollingInProgress = 0;
            return false;
        }
        return true;
    }

    @Override
    public void onScrollChange(@NonNull @NotNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (isScrollingInProgress()){ // KLUDGE
            logger.warn("onScrollChange recursionGuard active, skipping");
            UtilDebug.logCompactCallStack("FirstFragment.onScrollChange() recursionGuard");
            return;
        }
        // Detect scrollview top and bottom
        View view = v.getChildAt(v.getChildCount() - 1);
        int topDetector = v.getScrollY();
        int bottomDetector = view.getBottom() - (v.getHeight() + v.getScrollY());
        int offsetMove = 0;
        if(topDetector <= 0){
            // v.scrollTo(0,v.getBottom()/2);
            //Toast.makeText( MainActivity.getInstance(),"Scroll View top reached",Toast.LENGTH_SHORT).show();
            logger.info("ScrollView reached top");
            offsetMove = -1;
        }
        if(bottomDetector == 0 ){
            //v.scrollTo(0,v.getBottom()/2);
            //Toast.makeText( MainActivity.getInstance(),"Scroll View bottom reached",Toast.LENGTH_SHORT).show();
            logger.info("ScrollView reached bottom");
            offsetMove = 1;
        }
        if (offsetMove != 0) {
            UtilDebug.logCompactCallStack("FirstFragment.onScrollChange()");
            offsetMove *= 10; // load 5 entries
            if (Entries.changeOffsetBy(offsetMove)) {
                logger.info("Loading more entries after overscroll at {} (offset: {}: {} of {})",
                        offsetMove > 0 ? "bottom" : "top",
                        offsetMove, Entries.getOffset(), Entries.getCurrentTopicSize());
                if (!isScrollingInProgress()){
                    scrollingInProgress = System.currentTimeMillis() + 500; // 500ms guard
                    updateButtonsUIThread();
                    // scroll to opposite end to prevent self bounce
                    v.scrollTo(0, offsetMove > 0 ? 1 : v.getBottom() - v.getHeight());
                } // prevent self bounce
            } else {
                logger.info("No more entries to load after overscroll at {} (offset {}: {} of {})",
                        offsetMove > 0 ? "bottom" : "top",
                        offsetMove, Entries.getOffset(), Entries.getCurrentTopicSize());
            }
        } // move
    } // onScrollChange

}