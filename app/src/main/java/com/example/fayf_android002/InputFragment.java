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
import com.example.fayf_android002.databinding.FragmentSecondBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputFragment extends Fragment {

    Logger logger = LoggerFactory.getLogger(InputFragment.class);

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        RuntimeTester.registerFragment("InputFragment", this, binding.getRoot());
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

        binding.editextSecond.setText( null == entry ? "" : entry.content);
        binding.editextSecond.setSelection( binding.editextSecond.getText().length() );
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
        NavHostFragment.findNavController(InputFragment.this)
                .navigate(R.id.action_SecondFragment_to_FirstFragment);
    }


    public void onSendEntry(){
        String newContent = binding.editextSecond.getText().toString();
        Entry entry = Entries.getCurrentEntry();
        Entries.setContent(entry, newContent);
        logger.info("Entry updated: {}", entry.getFullPath());
        Toast.makeText(getActivity(), getString(R.string.send_toast), Toast.LENGTH_SHORT).show();
        backToFirstFragment();
    }

    public void onDelete(){
        Entry entry = Entries.getCurrentEntry();
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