package com.example.fayf_android002.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fayf_android002.Config;
import com.example.fayf_android002.Entry.*;
import com.example.fayf_android002.MainActivity;
import com.example.fayf_android002.R;
import com.google.android.material.appbar.AppBarLayout;
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
        logger.info("MainItemAdapter initialized with " + entryMap.size() + " entries.");
        this.context = context;
        // TODO improve to avoid direct dependency
        entries.addAll( entryMap.entrySet() );
        patchLayoutHeight();
    }

    private void patchLayoutHeight() {
        // KLUDGE: allow oversize if only 1 item (for testing)
        // set layout height to wrap content
    }

    public void updateData(SortedEntryMap topicEntries, RecyclerView recyclerView) {
        entries.clear();
        entries.addAll( topicEntries.entrySet() );
        logger.info("MainItemAdapter updateData called with " + topicEntries.size() + " entries.");
        patchLayoutHeight();
        // call on main thread
        MainActivity.getInstance().runOnUiThread(() -> {
                notifyDataSetChanged();
                if (null != recyclerView ){
                    recyclerView.post(() -> recyclerView.scrollToPosition(0));
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.scrollToPositionWithOffset(0, 0);
                    }
                    // Ensure the AppBar is expanded
                    AppBarLayout appBarLayout = MainActivity.getInstance().findViewById(R.id.appbar);
                    if (appBarLayout != null) {
                        appBarLayout.setExpanded(true, true);
                    }                }

            }
        );
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
        Map.Entry<String, Entry> e = entries.get(position);
        String topic = Entries.getCurrentEntryKey().getFullPath();
        EntryKey entryKey = new EntryKey(topic, e.getKey());
        Entry entry = Entries.getEntry(entryKey);
        if (entry == null) {
            logger.error("onBindViewHolder: Entry is null for key " + e.getKey());
            return;
        }
        //holder.button.setId( View.generateViewId() ); // unique ID for each button - helpful for testing
        String text = entry.content;
        if(topic.startsWith(Config.CONFIG_PATH)) {
            text = Config.DisplayName(e.getKey()) + ": " + entry.content;
        }
        holder.button.setText(text);
        styleButton( (MaterialButton) holder.button, position, entryKey, e.getValue() );
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

    public static void styleButton(MaterialButton button, int position, EntryKey entryKey, Entry entry) {
        // TODO implement styleButton
        String content = entry.content;
        if (button instanceof ButtonTouchable) {
            ((ButtonTouchable) button).setEntry(entryKey, null);
        }
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
        button.setTextColor( button.getContext().getColor(R.color.black) );
        /*
        button.setOnClickListener(v -> {
            Entries.setCurrentEntryKey(entryKey);
            // will trigger
            // ButtonTouchable.performClick(ButtonTouchable.java:162) too
            // ButtonTouchable$1.onSingleTapConfirmed(ButtonTouchable.java:46)
        });
        button.setOnLongClickListener(v -> {
            Entries.setCurrentEntryKey(entryKey);
            MainActivity.getInstance().switchToInputFragment();
            return true; // indicate that the long click was handled
        });

         */
    }


}
