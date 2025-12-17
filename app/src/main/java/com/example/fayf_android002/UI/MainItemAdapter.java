package com.example.fayf_android002.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.R;
import com.google.android.material.button.MaterialButton;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class MainItemAdapter extends RecyclerView.Adapter<MainItemAdapter.MainItemViewHolder> {

    Logger logger = LoggerFactory.getLogger(MainItemAdapter.class);

    private final ArrayList<Map.Entry<String, Entry>> entries = new ArrayList<Map.Entry<String, Entry>>();
    private final Context context;

    public MainItemAdapter(Context context, SortedEntryMap entryMap) {
        this.context = context;
        // TODO improve to avoid direct dependency
        entries.addAll( entryMap.entrySet() );
    }

    public void updateData(SortedEntryMap topicEntries) {
        entries.clear();
        entries.addAll( topicEntries.entrySet() );
        // call on main thread
        MainActivity.getInstance().runOnUiThread(() -> notifyDataSetChanged() );
    }


    @NonNull
    @Override
    public MainItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // invoked by the layout manager to create new ViewHolders
        View view = LayoutInflater.from(context).inflate(R.layout.row_item, parent, false);
        return new MainItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MainItemViewHolder holder, int position) {
        Map.Entry<String, Entry> entryKey = entries.get(position);
        String topic = Entries.getCurrentEntryKey().getFullPath();
        Entry entry = Entries.getEntry( new EntryKey(topic, entryKey.getKey()));
        if (entry == null) {
            logger.error("onBindViewHolder: Entry is null for key " + entryKey.getKey());
            return;
        }
        holder.button.setText(entry.content);
        styleButton( (MaterialButton) holder.button, position, new EntryKey(entryKey.getKey()), entryKey.getValue() );
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }


    /*
        * ViewHolder class for the button
     */

    public static class MainItemViewHolder extends RecyclerView.ViewHolder {
        Button button;

        public MainItemViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.row_item_button);
        }
    }


    /*
        Style related code
     */

    private void styleButton(MaterialButton button, int position, EntryKey entryKey, Entry entry) {
        // TODO implement styleButton
        String content = entry.content;
        button.setIconResource(
                // content.endsWith(".") ? R.drawable.baseline_folder_24 :  // Meinung.
                content.endsWith("??") ? EntryStyle.COUNTER_QUESTION.getIconResourceId() :  // Gegenfrage ??
                        content.endsWith("?") ? EntryStyle.QUESTION.getIconResourceId() :  // Frage / Diskussion ?
                                content.endsWith("!-") ? EntryStyle.FALSE_FACT.getIconResourceId() :  // falsifizerter Fact !-
                                        !entry.signedVotes.isEmpty() ? R.mipmap.icons8_guarantee_100_3 :
                                                content.endsWith("!") ? EntryStyle.FACT.getIconResourceId() :  // Fact !
                                                        content.endsWith("@") ? EntryStyle.REFERENCE.getIconResourceId() :  // Reference @
                                                                entry.getMyVote() > 0 ? R.mipmap.icons8_thumbs_up_96 :
                                                                        entry.getMyVote() < 0 ? R.mipmap.icons8_thumbs_down_right_48 :
                                                                                Entries.isTopic(entryKey) ? R.drawable.baseline_chevron_right_24 :
                                                                                        R.drawable.ic_baseline_note_24
        );
        // black text
        button.setTextColor( context.getColor(R.color.black) );
        button.setOnClickListener(v -> {
            Entries.setCurrentEntryKey(entryKey);
        });
        button.setOnLongClickListener(v -> {
            Entries.setCurrentEntryKey(entryKey);
            MainActivity.getInstance().switchToInputFragment();
            return true; // indicate that the long click was handled
        });
    }


}
