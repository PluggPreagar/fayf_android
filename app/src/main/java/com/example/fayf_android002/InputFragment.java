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
import com.example.fayf_android002.Entry.EntryStyle;
import com.example.fayf_android002.RuntimeTest.RuntimeTester;
import com.example.fayf_android002.databinding.FragmentSecondBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class InputFragment extends Fragment {

    Logger logger = LoggerFactory.getLogger(InputFragment.class);

    private FragmentSecondBinding binding;
    private String oldValue = "";

    Map<EntryStyle, View> styleButtonMap = null;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        logger.info("InputFragment onCreateView called");
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        RuntimeTester.registerFragment("InputFragment", this, R.id.FirstFragment, binding.getRoot());
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logger.info("InputFragment onViewCreated called");

        //binding.buttonCancel.setOnClickListener(v -> backToFirstFragment() );
        //binding.buttonSecond.setOnClickListener(v -> backToFirstFragment() );
        styleButtonMap = Map.of(
                EntryStyle.FACT, binding.buttonStyleFact,
                EntryStyle.FALSE_FACT, binding.buttonStyleFalseFact,
                EntryStyle.QUESTION, binding.buttonStyleQuestion,
                EntryStyle.COUNTER_QUESTION, binding.buttonStyleCounterQuestion,
                EntryStyle.REFERENCE, binding.buttonStyleReference
        );

        binding.buttonDelete.setOnClickListener(v -> onDelete() );
        //binding.buttonStar.setOnClickListener(v -> onSendEntry() );
        binding.buttonSend.setOnClickListener(v -> onSendEntry() );

        binding.editextSecond.requestFocus();
        /*binding.editextSecond.postDelayed(() -> {
            binding.editextSecond.requestFocus();
        }, 100);
        */

        // disable fab in FirstFragment
        // FIXME ((MainActivity) requireActivity()).getFab().setVisibility(View.GONE);

        // open keyboard
        // KeyboardUtil.showKeyboard(requireActivity(), binding.editextSecond);
        binding.editextSecond.requestFocus();
        binding.editextSecond.postDelayed(() -> {
            if (null != binding && null != binding.editextSecond) {
                binding.editextSecond.requestFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    view.requestFocus();
                    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                }
            }  else {
                logger.warn("editextSecond is null in postDelayed");
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

    @Override
    public void onStart() {
        super.onStart();
        logger.info("InputFragment started");
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.info("InputFragment resumed");
        Entry entry = Entries.getCurrentEntry();
        oldValue = null == entry ? "" : entry.content;
        EntryStyle entryStyle = EntryStyle.getByContent(oldValue);
        styleButtonMap.forEach(
                (style, button) -> {
                    button.setSelected(style.getIconResourceId() == entryStyle.getIconResourceId());
                    button.setOnClickListener( v -> toggle(v) );
                    //button.setBackgroundResource(style.getIconResourceId());
                }
        );
        toggle(null); // initial alpha setup
        //
        if (oldValue.endsWith(entryStyle.getSuffix())) {
            oldValue = oldValue.substring(0, oldValue.length() - entryStyle.getSuffix().length());
        }
        //

        binding.editextSecond.setText(oldValue);
        binding.editextSecond.setSelection( binding.editextSecond.getText().length());
        binding.editextSecond.setHint( entry == null || entry.content.isEmpty() ? "New entry content" : "" );

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

    private void toggle(View v) {
        if (null != v){
            v.setSelected(!v.isSelected());
            if (v.isSelected()) {
                for(View otherButton : styleButtonMap.values()){
                    if (otherButton != v){
                        otherButton.setSelected(false);
                    }
                }
                // selecting = sending
                onSendEntry();
                return;
            }
        }
        // iterate over all style buttons and set alpha if not selected
        for (View button : styleButtonMap.values()) {
            if (button.isSelected()) {
                button.setAlpha(1.0f);
            } else {
                button.setAlpha(0.25f);
            }
        }
    }




    public void  backToFirstFragment(){
        Entries.upOneTopicLevel(); // ???
        MainActivity.getInstance().switchToFirstFragment();
    }


    public void onSendEntry(){
        String newContent = binding.editextSecond.getText().toString();
        // check style buttons
        String suffix = "";
        if (binding.buttonStyleFact.isSelected()) {
            suffix = EntryStyle.FACT.getSuffix();
        } else if (binding.buttonStyleFalseFact.isSelected()) {
            suffix = EntryStyle.FALSE_FACT.getSuffix();
        } else if (binding.buttonStyleQuestion.isSelected()) {
            suffix = EntryStyle.QUESTION.getSuffix();
        } else if (binding.buttonStyleCounterQuestion.isSelected()) {
            suffix = EntryStyle.COUNTER_QUESTION.getSuffix();
        } else if (binding.buttonStyleReference.isSelected()) {
            suffix = EntryStyle.REFERENCE.getSuffix();
        }
        if (!suffix.isEmpty()){
            EntryStyle byContent = EntryStyle.getByContent(newContent);// validate current suffix
            if (!suffix.equals(byContent.getSuffix()) && newContent.endsWith(suffix)) {
                // remove existing suffix - and re-append new one
                newContent = newContent.substring(0, newContent.length() - byContent.getSuffix().length());
                newContent += suffix;
            } if (!newContent.endsWith(suffix)) {
                newContent += suffix;
            }
        }

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



}