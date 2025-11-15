package com.example.fayf_android002;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fayf_android002.databinding.FragmentFirstBinding;
import com.google.android.material.button.MaterialButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class FirstFragment extends Fragment {

    Logger logger = LoggerFactory.getLogger(FirstFragment.class);

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        logger.info("FirstFragment onCreateView() called");
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        RuntimeTester.registerFragment("FirstFragment", this, binding.getRoot());

        // initializeButtons(); // to early here ?

        // Navigating from SecondFragment to FirstFragment will not show the Topic Title
        getMainActivity().updateActionBarTitle(null);

        if (0 == Entries.getInstance().getEntryTree().size()) {

            /* get Data */
            Entries.setOnEntriesLoadedListener(loadedEntries -> requireActivity().runOnUiThread(() -> {
                        logger.info("Entries loaded callback received ({} entries)", Entries.getInstance().getEntryTree().size());
                        updateButtons( ); // update buttons after entries loaded
                    }
            ));
            Entries.load_async(requireContext()); // async load entries
        } else {
            updateButtons( ); // update buttons if entries already loaded
        }

        //((MainActivity) requireActivity()).getFab().setVisibility(View.GONE);
        Entries.setOnTopicChangedListener("FirstFragment", entry -> {
            logger.info("Topic changed callback received: {}", entry.getFullPath());
            updateButtonsUIThread();
        });

        // on overscroll the bottom of button list
        // then top entries are out of reach

        // get ButtomList.onOverScrolledListener
        binding.ButtonScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            View view = binding.ButtonScrollView.getChildAt(binding.ButtonScrollView.getChildCount() - 1);
            int diff = (view.getBottom() - (binding.ButtonScrollView.getHeight() + binding.ButtonScrollView.getScrollY()));
            if (diff == 0) {
                // we are at the bottom
                logger.info("ScrollView reached bottom");
                if (true) {
                    logger.info("Scrolling load more entries skipped - TEST");
                }
                if (Entries.incrementOffset(20)){
                    // load next 20 entries
                    updateButtonsUIThread();
                };
            }
        });
        // try to fix the top-out-of-reach--after--scrolling-botton issue
        binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
        binding.ButtonScrollView.post(() -> binding.ButtonScrollView.scrollTo(0, 0));
        // scroll to see first button
        // binding.button1.

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        logger.info("FirstFragment onViewCreated() called");
        super.onViewCreated(view, savedInstanceState);

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

        if (0 == Entries.getInstance().getEntryTree().size()) {
            logger.info("Entries not loaded yet, will update buttons after loading");
        } else {
            logger.info("Entries already loaded ({} entries), updating buttons", Entries.getInstance().getEntryTree().size());
            updateButtonsUIThread();
        }
*/
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

    public void onCreate(Bundle savedInstanceState) {
        logger.info("FirstFragment onCreate() called");
        super.onCreate(savedInstanceState);
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
        if (true) {
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
        binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
        logger.info("Initialized buttons in ButtonList, total count: {} ", buttonList.getChildCount());
    }


    public void setTopic(Entry entry) {
        Entries.setTopicEntry(entry); // e.g. not valid for leaf entries
        // button will be updated via listener
    }

    public void updateButtonsUIThread() {
        requireActivity().runOnUiThread(() -> {
            initializeButtons();
            logger.info("Render in UI-Thread ({} entries)", Entries.getInstance().getEntryTree().size());
            updateButtons(); // update buttons after entries loaded
        });
    }

    public void updateButtons() {
        updateButtons( Entries.getCurrentTopicString(), Entries.getOffset());
    }

    public void updateButtons(String topic, int offset) {
        ViewGroup buttonList = getMainActivity().findViewById(R.id.ButtonList);
        if (buttonList == null) {
            logger.error("ButtonList ViewGroup not found in MainActivity");
            return;
        }
        if (true) {
            logger.info("updateButtons() skipped - TEST");
            return;
        }
        int limit = 20; // buttonList.getChildCount();
        logger.info("Updating buttons for topic: {}, offset: {}, limit: {}", topic, offset, limit);
        Iterator<Map.Entry<String, Entry>> entriesIterator = Entries.getEntriesIterator(topic, offset);
        if (!entriesIterator.hasNext()) {
            logger.info("No entries found for topic: {}, offset: {}", topic, offset);
            topic = Entries.restoreLastTopic().getFullPath();
            entriesIterator = Entries.getEntriesIterator(topic, 0); // fallback to root
            // Toast message
            Toast.makeText(getActivity(), "No entries found for the selected topic.", Toast.LENGTH_SHORT).show();
        }
        int idx = 0;
        while (entriesIterator.hasNext() && limit > 0) {
            Map.Entry<String, Entry> e = entriesIterator.next();
            Entry entry = e.getValue();
            logger.info("Entry: {}", entry.content);
            // KLUDGE  iterate over Buttons in ButtonList

            // iterate over Buttons in ButtonList
            View button = buttonList.getChildAt(idx);
            if (button instanceof Button) {
                Button btn = (Button) button;
                btn.setText(entry.content);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(v -> {
                    this.setTopic(entry);
                });
                btn.setOnLongClickListener(v -> {
                    // TODO move that all to FirstFragment
                    // determine first fragment
                    navigateToEdit(entry);
                    return true;
                });

                /*
                // move button 1px left
                // Get the button's LayoutParams
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
                // Convert 1dp to pixels
                float scale = button.getContext().getResources().getDisplayMetrics().density;
                int shiftInPixel = (int) (idx * 40 * scale + 0.5f);
                // Adjust the left margin
                params.leftMargin += shiftInPixel;
                params.height = btn.getHeight(); // keep height - even if it is wrap content on shrink
                int parentWidth = ((ViewGroup) btn.getParent()).getWidth();
                //params.width = (int) (parentWidth * 0.8 - shiftInPixel); // width is initially not fixed - but can be set here
                // Apply the updated LayoutParams back to the button
                button.setLayoutParams(params);
                */
                // make button height larger
                MaterialButton btn_m = (MaterialButton) button;
                btn_m.setInsetTop( 0);
                btn_m.setInsetBottom(0);
                // compensate with margins
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) button.getLayoutParams();
                params.topMargin = 10;
                params.bottomMargin = 10;
                button.setLayoutParams(params);


/*

                btn.setOnTouchListener(new OnSwipeTouchListener(this) {
                    public void onSwipeRight() {
                        Toast.makeText(getActivity(), "Swiped right on entry: " + entry.content, Toast.LENGTH_SHORT).show();
                        //Entries.setTopicEntry(entry); // set topic to this entry
                    }
                    public void onSwipeLeft() {
                        Toast.makeText(getActivity(), "Swiped left on entry: " + entry.content, Toast.LENGTH_SHORT).show();
                        //navigateToEdit(entry); // navigate to edit this entry
                    }

                });

 */
            }

            limit--;
            idx++;
        }


        while (limit > 0) {
            if (idx >= buttonList.getChildCount() || buttonList.getChildAt(idx) == null) {
                logger.warn("No more buttons available in ButtonList to hide (idx: {}, childCount: {})", idx, buttonList.getChildCount());
                break;
            }
            buttonList.getChildAt(idx).setVisibility(View.GONE);
            logger.info("No more entries (hiding button at index {} - content {})"
                    , idx, buttonList.getChildAt(idx).toString());
            limit--;
            idx++;
        }
        //
        // getMainActivity().getSupportActionBar().setTitle("FAYF - " + topic);
        binding.ButtonScrollView.post(() -> binding.ButtonScrollView.fullScroll(View.FOCUS_UP));
    }





    /*
        Actions

     */


    public void navigateToEdit(Entry entry) {
        Entries.setCurrentEntry(entry);
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Entries.setOnTopicChangedListener("FirstFragment", null); // remove listener
    }



}