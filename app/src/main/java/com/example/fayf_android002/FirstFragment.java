package com.example.fayf_android002;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fayf_android002.databinding.FragmentFirstBinding;
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

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        logger.info("FirstFragment onCreateView() called");

        if (0 == Entries.getInstance().getEntryTree().size()) {

            /* get Data */
            Entries.setOnEntriesLoadedListener(loadedEntries -> requireActivity().runOnUiThread(() -> {
                        logger.info("Entries loaded callback received ({} entries)", Entries.getInstance().getEntryTree().size());
                        updateButtons( ); // update buttons after entries loaded
                    }
            ));
            Entries.load_async(); // async load entries
        } else {
            updateButtons( ); // update buttons if entries already loaded
        }

        //((MainActivity) requireActivity()).getFab().setVisibility(View.GONE);
        Entries.setOnTopicChangedListener("FirstFragment", entry -> {
            logger.info("Topic changed callback received: {}", entry.getFullPath());
            updateButtonsUIThread();
        });

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logger.info("FirstFragment onViewCreated() called");

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

    }





    /*
            B U T T O N S
     */

    private MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }



    public void setTopic(Entry entry) {
        Entries.setTopicEntry(entry); // e.g. not valid for leaf entries
        // button will be updated via listener
    }

    public void updateButtonsUIThread() {
        requireActivity().runOnUiThread(() -> {
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
        int limit = buttonList.getChildCount();
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
            }

            limit--;
            idx++;
        }
        // for testing ...
        if (false && limit>0){
            //limit --;
            Button button = (Button) buttonList.getChildAt(1);
            // set current timestamp
            button.setText("" + System.currentTimeMillis());
            button.setOnClickListener(v -> {
                button.setText("" + System.currentTimeMillis());
            });
        }

        while (limit > 0) {
            buttonList.getChildAt(idx).setVisibility(View.GONE);
            logger.info("No more entries (hiding button at index {} - content {})"
                    , idx, buttonList.getChildAt(idx).toString());
            limit--;
            idx++;
        }
        //
        // getMainActivity().getSupportActionBar().setTitle("FAYF - " + topic);
    }





    /*
        Actions

     */


    public void navigateToEdit(Entry entry) {
        Bundle args = new Bundle();
        args.putString("entryFullPath", null == entry ? "/" : entry.getFullPath());
        args.putString("METHOD", "EDIT");
        //
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment, args);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Entries.setOnTopicChangedListener("FirstFragment", null); // remove listener
    }



}