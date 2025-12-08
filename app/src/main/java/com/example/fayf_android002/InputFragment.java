package com.example.fayf_android002;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.RuntimeTest.RuntimeTester;
import com.example.fayf_android002.databinding.FragmentSecondBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputFragment extends Fragment {

    Logger logger = LoggerFactory.getLogger(InputFragment.class);

    private FragmentSecondBinding binding;
    private String oldValue = "";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        RuntimeTester.registerFragment("InputFragment", this, R.id.FirstFragment, binding.getRoot());
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //binding.buttonCancel.setOnClickListener(v -> backToFirstFragment() );
        binding.buttonSecond.setOnClickListener(v -> backToFirstFragment() );

        binding.buttonDelete.setOnClickListener(v -> onDelete() );
        //binding.buttonStar.setOnClickListener(v -> onSendEntry() );
        binding.buttonSend.setOnClickListener(v -> onSendEntry() );

        binding.editextSecond.requestFocus();
        /*binding.editextSecond.postDelayed(() -> {
            binding.editextSecond.requestFocus();
        }, 100);
        */

        Entry entry = Entries.getCurrentEntry();
        oldValue = null == entry ? "" : entry.content;
        binding.editextSecond.setText(oldValue);
        binding.editextSecond.setSelection( binding.editextSecond.getText().length());
        binding.editextSecond.setHint( entry == null || entry.content.isEmpty() ? "New entry content" : "" );
        // set TextView to entry content
        binding.textViewHidden.setVisibility(View.GONE); // hide edit text for now

        // disable fab in FirstFragment
        // FIXME ((MainActivity) requireActivity()).getFab().setVisibility(View.GONE);

        // open keyboard
        // KeyboardUtil.showKeyboard(requireActivity(), binding.editextSecond);
        binding.editextSecond.requestFocus();
        binding.editextSecond.postDelayed(() -> {
            binding.editextSecond.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                view.requestFocus();
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);


        binding.editextSecond.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                onSendEntry(); // Trigger send action
                return true; // Consume the event
            }
            return false; // Pass the event to other listeners
        });

    }

    public void  backToFirstFragment(){
        Entries.upOneTopicLevel(); // ???
        MainActivity.getInstance().switchToFirstFragment();
    }


    public void onSendEntry(){
        String newContent = binding.editextSecond.getText().toString();
        EntryKey entryKey = Entries.getCurrentEntryKey();
        Entries.setEntry(entryKey, newContent, getContext());
        logger.info("Entry updated: {}", entryKey.getFullPath());
        Toast.makeText(getActivity(), getString(R.string.send_toast), Toast.LENGTH_SHORT).show();
        // Tenant-Change forces reload in Entries.setEntry
        if (!oldValue.equals(newContent)
                && entryKey.topic.startsWith(Config.CONFIG_PATH)
                && entryKey.nodeId.equals(Config.TENANT.name()) ) {
            Toast.makeText(getActivity(), getString(R.string.tenant_changed_reload_toast), Toast.LENGTH_LONG).show();
            Entries.rootTopic();
            Entries.load_async(MainActivity.getInstance());
        }
        backToFirstFragment();
    }

    public void onDelete(){
        EntryKey entry = Entries.getCurrentEntryKey();
        logger.info("Entry deleting: {}", entry.getFullPath());
        Entries.removeEntry(entry);
        backToFirstFragment();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.info("InputFragment resumed");
        if (binding != null) {
            binding.editextSecond.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.editextSecond, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            logger.warn("Binding is null in onResume - unable to focus editextSecond");
        }
    }

}