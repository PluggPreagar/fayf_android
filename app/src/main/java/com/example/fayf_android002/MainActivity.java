package com.example.fayf_android002;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.fayf_android002.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener,
        ViewTreeObserver.OnScrollChangedListener {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    NestedScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        setTopic("/");

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();

                 */

                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);

                if (navHostFragment != null) {
                    NavController navController = navHostFragment.getNavController();
                    navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            }
        });

        /* added for scrollview */

        scrollView = findViewById(R.id.ButtonScrollView);
        scrollView.setOnTouchListener(this);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);

    }

    public FloatingActionButton getFab(){
        return binding.fab;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        logger.info("Menu item selected: {}", item.getTitle());

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            logger.info("Settings menu item selected");
            if (Entries.getCurrentTopicString().startsWith("/_")) {
                setTopic("/");
            } else {
                setTopic("/_/config");
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        Entry entry = Entries.moveUpOneTopicLevel();
        logger.info("Navigating up from topic: {}", entry.getTopic());
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public void onBackPressed() {
        Entry entry = Entries.moveUpOneTopicLevel();
        String topic = entry.getTopic();
        // String topic = Entry.getTopicFromFullPath(this.topic); // get parent topic
        if (null==topic){
            super.onBackPressed();
        }
        // update UI accordingly - handled by fragments observing topic changes
        // get FirstFragment and refresh its view
        /* does not update FirstFragment properly  -- need to force update

           Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
                .navigate(R.id.FirstFragment, args);
        */
        /*  androidx.navigation.fragment.NavHostFragment cannot be cast to com.example.fayf_android002.FirstFragment

        FirstFragment firstFragment = (FirstFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (firstFragment != null && firstFragment.isVisible()) {
            firstFragment.updateButtons();
        }

        */
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            if (navController.getCurrentDestination() != null &&
                    navController.getCurrentDestination().getId() == R.id.FirstFragment) {
                logger.info("FirstFragment is already visible, refreshing it");
                /* not refreshing ui properly

                    navController.navigate(R.id.FirstFragment, args); // Re-navigate to refresh

                 */
                /*
                // FIXME Workaround: navigate to a dummy destination and back
                logger.warn("Navigating to SecondFragment as workaround");
                navController.navigate(R.id.SecondFragment, args); // Re-navigate to refresh
                navController.navigate(R.id.FirstFragment, args); // Re-navigate to refresh
                 */
                /*
                    OnBackInvokedCallback is not enabled for the application.
                    Set 'android:enableOnBackInvokedCallback="true"' in the application manifest.
                if (navHostFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .detach(navHostFragment) // Detach the fragment
                            .attach(navHostFragment) // Re-attach the fragment
                            .commit(); // Commit the transaction
                }
                *

                 */
                /*
                NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.FirstFragment, true)
                        .build(); // Pop up to FirstFragment to clear it from back stack
                navController.navigate(R.id.FirstFragment, args, navOptions); // Re-navigate to refresh
                 */
                /*
                args.putLong("timestamp", System.currentTimeMillis()); // force refresh by unique arg
                navController.navigate(R.id.FirstFragment, args); // Re-navigate to refresh

                 */
                /* tried with ui-thread -- no effect
                navController.navigate(R.id.FirstFragment, args); // Re-navigate to refresh
                 */
                //args.putString("FORWARD_REFRESH", Long.toString(System.currentTimeMillis())); // force refresh by unique arg
                //navController.navigate(R.id.SecondFragment, args); // Re-navigate to refresh
                //navController.navigate(R.id.FirstFragment, args); // Re-navigate to refresh
                navController.navigate(R.id.action_FirstFragment_to_FirstFragment); // Re-navigate to refresh
            } else {
                logger.info("Navigating to FirstFragment");
                navController.navigate(R.id.FirstFragment);
            }
        } else {
            logger.warn("NavHostFragment not found");
        }

    }




    /*
            S C R O L L V I E W
     */

    // prevent self bounce


    boolean recursionGuard = false;
    public void onScrollChanged(){
        //if (recursionGuard) return;
        recursionGuard = true;
        // Detect scrollview top and bottom
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int topDetector = scrollView.getScrollY();
        int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
        if(bottomDetector == 0 ){
            scrollView.scrollTo(0,scrollView.getBottom()/2);
            // Toast.makeText(getBaseContext(),"Scroll View bottom reached",Toast.LENGTH_SHORT).show();
        }
        if(topDetector <= 0){
            scrollView.scrollTo(0,scrollView.getBottom()/2);
            // Toast.makeText(getBaseContext(),"Scroll View top reached",Toast.LENGTH_SHORT).show();
        }
        recursionGuard = false;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: " + event.toString());
        return false;
    }

    /*


     */

    public String setTopic(Entry entry) {
        return setTopic(entry.getFullPath()); // set own path as topic for next level
    }

    public String setTopic(String topic) {
        logger.info("Setting topic to: {}", topic);
        // TODO use entry not string
        Entry entry = Entries.getEntryOrNew( Entry.fixTopic( topic )); // ensure entry exists
        Entries.setTopicEntry(entry);
        Entries.setCurrentEntry(null);
        Entries.setOffset(0);

        // update title in action bar
        if (getSupportActionBar() != null) {
            logger.info("Updating action bar title for topic: {}", topic);
            String newTitle = "FayF";
            if (null != entry && null != entry.topic && Util.isFilled(entry.content)) { // may have DUMMY entry here
                newTitle += " - " + Util.shortenString(entry.content, 30);
            }
            getSupportActionBar().setTitle(newTitle);
            // enable back button in action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(!entry.getTopic().equals("/")); // show back button if not root
        }
        // update title in menu bar

        return topic;
    }





}