package com.example.fayf_android002.UI;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
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

    static Logger logger = LoggerFactory.getLogger(MainItemAdapter.class);

    private final ArrayList<Map.Entry<String, Entry>> entries = new ArrayList<Map.Entry<String, Entry>>();
    private final Context context;

    private static boolean dataUpdated = false; // TODO introduce usage, reason

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

    public void updateData(SortedEntryMap topicEntries, RecyclerView recyclerView, Entries.OnDataChanged.ChangeType changeType) {
        entries.clear();
        entries.addAll( topicEntries.entrySet() );
        logger.info("MainItemAdapter updateData called with " + topicEntries.size() + " entries.");
        // allow to show more details like rank, as data is updated by changing the ranking
        dataUpdated = changeType.equals( Entries.OnDataChanged.ChangeType.ENTRY_RANK_CHANGED ) ;
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
        String text = entry.getContent();
        if(topic.equals(Config.CONFIG_PATH)) {
            text = Config.DisplayName(e.getKey()) + ": " + entry.getContent();
        }
        // remove suffix from button text for better readability - and uniq appearance
        // text = text.replaceFirst("\\s*>\\s*$", ""); // remove trailing >
        if (Entries.isTopic(entryKey)) {
            if (!text.endsWith(">")) {
                text = text.trim() + " >"; // ensure trailing >
            }
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
        String content = entry.getContent();
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

        // set background to red
        //button.setBackgroundColor( button.getContext().getColor(R.color.light_blue) ); // FAIL - hard blue

        // reset Tint, in case it was set before
        // set to gray if disabled
        //button.setBackgroundColor( button.getContext().getColor(R.color.orange_yellow_2) ); // FAIL - hard blue

        Drawable background = button.getBackground();
        if (background instanceof ColorDrawable) {
            int color = ((ColorDrawable) background).getColor();
            logger.info("Button background color: " + color);
        } else {
            logger.info("Button background is not a ColorDrawable");
        }

        // use drawable tinting to set background color --> work
//        button.setBackgroundColor( button.getContext().getColor(R.color.orange_yellow_2) );
//        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.orange_yellow_2) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);

        // works
//        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
//        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.orange_yellow_2) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);
        // works
//        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
//        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.red) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);

        // works fix strange behavior: just set background color
//        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
//        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.colorPrimaryVeryLight) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);

        // works
//        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
//        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.orange_yellow_2) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);

        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
        button.setBackgroundTintList( button.getContext().getColorStateList(R.color.transparent) );
        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);


        // works - add border
//        GradientDrawable borderDrawable = new GradientDrawable();
//        borderDrawable.setColor( button.getContext().getColor(android.R.color.transparent)); // Transparent background
//        borderDrawable.setStroke(4, button.getContext().getColor(android.R.color.darker_gray)); // Light gray border
//        borderDrawable.setCornerRadius(16); // Optional: Rounded corners
//        // Apply the border drawable
//        button.setBackgroundTintList(null); // Clear any existing tint
//        button.setBackgroundColor( button.getContext().getColor(R.color.white) );
//        button.setBackgroundTintMode(android.graphics.PorterDuff.Mode.SRC_IN);
//        button.setBackground(borderDrawable); // Apply the border


        // add bagde to button for vote count
        addBadgeToButton(button, entry.getRank());



    }

    // Add badge to button for vote count
    private static void addBadgeToButton(MaterialButton button, int voteCount) {
        // Check if a badge already exists
        FrameLayout frameLayout = (FrameLayout) button.getParent();
        TextView existingBadge = frameLayout.findViewWithTag("badge");
        if (existingBadge != null) {
            frameLayout.removeView(existingBadge); // Remove the existing badge
        }
        if (voteCount == 0) {
            return; // No badge for zero votes
        }
        if (!dataUpdated){
            return; // only show badge if data was updated( probably ranking changed)
        }
        // add badge
        Context context = button.getContext();
        logger.info("Adding badge '{}' with {}.", button.getText(), voteCount);

        // Create a TextView for the badge
        TextView badge = new TextView(context);
        badge.setTag("badge"); // Tag to identify the badge later
        badge.setText(String.valueOf(voteCount));
        badge.setTextColor(context.getColor(android.R.color.white));
        badge.setTextSize(12);
        badge.setPadding(8, 4, 8, 4);
        badge.setGravity(Gravity.CENTER);

        // Create a GradientDrawable for the badge background
        GradientDrawable badgeBackground = new GradientDrawable();
        badgeBackground.setCornerRadius(8); // Rounded corners
        if (voteCount > 0) {
            badgeBackground.setColor(context.getColor(android.R.color.holo_green_light)); // Green for positive votes
        } else {
            badgeBackground.setColor(context.getColor(android.R.color.holo_red_light)); // Red for negative votes
        }
        badge.setBackground(badgeBackground);

        // Set layout parameters for the badge
        FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        badgeParams.gravity = Gravity.END | Gravity.TOP; // Position the badge at the top-right corner
        badgeParams.setMargins(0, 8, 32, 0); // Adjust margins as needed

        // Replace the button's parent with the FrameLayout
        frameLayout.addView(badge, badgeParams);
    }



}
