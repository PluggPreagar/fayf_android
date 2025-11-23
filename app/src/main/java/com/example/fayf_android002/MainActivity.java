package com.example.fayf_android002;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.fayf_android002.RuntimeTest.RuntimeTest;
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

    private static MainActivity instance = null;
    public Menu menu = null;

    public static MainActivity getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView logTextView = findViewById(R.id.logTextView);
        TextViewAppender.initialize(logTextView);
        TextViewAppender.setupSLF4J();

        logger.info( TextViewAppender.appendLog( "MainActivity onCreate() called" ) );

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        setTopic(Entries.ROOT_TOPIC);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();

                 */

                // add Entry for current topic
                // FIXME TODO  - create new Entry and set as currentEntry
                // FIXME TODO   InputFragment will complete the entry with content
                // FIXME TODO   allow Back navigation to FirstFragment


                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_content_main);
                NavController navController = navHostFragment.getNavController();
                // firstFragment -add-> opens SecondFragment (to add new child to current topic)
                // secondFragment -add-> stays in SecondFragment (add new child to current entry - nested entry)
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() == R.id.SecondFragment) {
                    // already in SecondFragment - just create new child entry
                    Entry currentEntry = Entries.getCurrentEntry();
                    // TODO make robust -- check for "in update of entry" state, not in "add an entry" state
                    if (currentEntry.content.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please complete the current entry before adding a new one.", Toast.LENGTH_LONG).show();
                        logger.info("FAB clicked in SecondFragment - but current entry is not completed yet");
                        return;
                    } else {
                        Entries.setCurrentEntry( Entries.createNewChildEntry( Entries.getCurrentEntry(), "") ); // empty content for now
                        // stay in SecondFragment
                        navController.navigate(R.id.action_SecondFragment_to_SecondFragment);
                        logger.info("FAB clicked in SecondFragment - creating new child entry to entry");
                    }
                } else if (navHostFragment != null) {
                    // only valid from FirstFragment
                    Entries.setCurrentEntry( Entries.createNewChildEntry( Entries.getCurrentTopicEntry(), "") ); // empty content for now
                    navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            }
        });

        Entries.setOnTopicChangedListener( "MainActivity", currentTopicEntry -> {
            runOnUiThread(() -> {
                logger.info("Topic changed listener triggered for currentTopicEntry: {}", currentTopicEntry.getTopic());
                updateActionBarTitle(currentTopicEntry);
            });
        });


        /* added for scrollview */

        scrollView = findViewById(R.id.ButtonScrollView);
        scrollView.setOnTouchListener(this);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);

    }

    @Override
    public void onDestroy() {
        logger.info("MainActivity onDestroy() called");
        Entries.save( getApplicationContext());
        super.onDestroy();
    }

    @Override
    public void onStop() {
        logger.info("MainActivity onStop() called");
        Entries.save( getApplicationContext());
        super.onStop();
    }

    // which function is called on closing application



    /*



     */


    public FloatingActionButton getFab(){
        return binding.fab;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu; // for Runtime-Test-Access
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (null == item || null == item.getTitle() || item.getItemId() == R.id.menu_main) {
            // KLUDGE allow Runtime-Test to trigger back button
            // but we can't create/get MenuItem directly
            logger.info("back-menu-button pressed");
            // check if in InputFragment, then go back to FirstFragment
            onBackPressed();
            return true;
        }


        int id = item.getItemId();
        // items defined in res/menu/menu_main.xml
        logger.info("Settings Menu item selected: {} (id: {})", item.getTitle(), id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            logger.info("Action menu item selected");
            if (Entries.getCurrentTopicString().startsWith(Entry.HIDDEN_ENTRY_PATH+Entry.PATH_SEPARATOR)) {
                setTopic(Entries.ROOT_TOPIC);
            } else {
                setTopic(Configuration.CONFIG_PATH);
            }
            return true;
        } else if (id == R.id.action_about) {
            logger.info("About menu item selected");
            Toast.makeText(this, R.string.action_about_msg, Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_test) {
            logger.info("Runtime tests menu item selected");
            // runtimeTest.runTests( getSupportFragmentManager() ); // does not find FirstFragment properly
            // run async on ui-thread
            new Thread(() -> {
                try {
                    // wait a bit to let ui settle
                    Thread.sleep(500);
                    RuntimeTest runtimeTest = new RuntimeTest();
                    runtimeTest.runTests(getSupportFragmentManager());
                } catch (Exception e) {
                    logger.error("Error while running tests in a separate thread", e);
                }
            }).start();
            return true;
        } else if (id == R.id.action_refresh) {
            logger.info("Refresh UI");
            // force refresh of FirstFragment
            Entries.callTopicChangedListeners( Entries.getCurrentTopicEntry() );
            return true;
        } else if (id == R.id.action_load_from_web) {
            logger.info("action_load_from_web");
            Entries.setTopicEntry( Entries.getEntry(Entries.ROOT_TOPIC) );
            Toast.makeText(getApplicationContext(), "download data", Toast.LENGTH_SHORT).show();
            Entries.load_async( getApplicationContext(), true);
            return true;
        } else if (id == R.id.toggle_log) {
            Configuration.SHOW_LOGS.toggleValue();
            if (Configuration.SHOW_LOGS.asBoolean()) {
                Toast.makeText(this, "Logs shown", Toast.LENGTH_SHORT).show();
                binding.logScrollView.setVisibility(View.VISIBLE);

            } else {
                Toast.makeText(this, "Logs hidden", Toast.LENGTH_SHORT).show();
                binding.logScrollView.setVisibility(View.GONE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        /* does not update FirstFragment properly  -- need to force update
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();

         */
        /* Kill switch to FirstFragment and refresh
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            navController.navigate(R.id.action_FirstFragment_to_FirstFragment);
        }
         */
        // force refresh of FirstFragment
        //onBackPressed();

        String topicBefore = Entries.getCurrentTopicString();
        Entry entry = Entries.moveUpOneTopicLevel();
        String topic = entry.getTopic();
        logger.info("Navigating up topic: {} (from {}) (UP)", topic, topicBefore);


        return true;
    }


    @Override
    public void onBackPressed() {
        String topicBefore = Entries.getCurrentTopicString();
        Entry entry = Entries.moveUpOneTopicLevel();
        String topic = entry.getTopic();
        logger.info("Navigating up topic: {} (from {}) (BACK)", topic, topicBefore);
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
            //scrollView.scrollTo(0,scrollView.getBottom()/2);
            Toast.makeText(getBaseContext(),"Scroll View bottom reached",Toast.LENGTH_SHORT).show();
        }
        if(topDetector <= 0){
            // scrollView.scrollTo(0,scrollView.getBottom()/2);
            Toast.makeText(getBaseContext(),"Scroll View top reached",Toast.LENGTH_SHORT).show();
        }
        recursionGuard = false;
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // forward touch event to child views
        View viewTouchedInProgress = Entries.getViewTouchedInProgress();
        if (viewTouchedInProgress != null ) {
            // prevent scrolling once Direction is Set
            // true to consume event here -> prevent scrollview from scrolling
            boolean b = viewTouchedInProgress.dispatchTouchEvent(event);
            // TODO KLUDGE - check direction
            boolean b2 = OnTouchListener.isDirectionX;
            Log.d(TAG, "MainActivity onTouch: forwarded, returned: " + b +" is:" + b2 + " event: " + event.toString());
            return b2;
        } else {
            Log.d(TAG, "MainActivity onTouch: " + event.toString());
        }
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

        // update title in menu bar
        updateActionBarTitle(entry);

        return topic;
    }

    public void updateActionBarTitle( Entry currentTopicEntry) { // allow to handover already known currentTopicEntry
        if (null == currentTopicEntry) {
            currentTopicEntry = Entries.getCurrentTopicEntry();
        }
        // update title in action bar
        if (getSupportActionBar() != null) {
            String newTitle = "FayF";
            boolean isRootTopic = currentTopicEntry != null && (currentTopicEntry.isRootTopic() || currentTopicEntry.getTopic().isEmpty());
            logger.info("Updating action bar title for topic: \"{}\" {}"
                    , null == currentTopicEntry ? "" : currentTopicEntry.getContent()
                    , isRootTopic ? "(root topic)" : "(enable back button)");
            if (isRootTopic) {
                newTitle += " of " + Entries.getTenant();
            } else if (null != currentTopicEntry && null != currentTopicEntry.topic && Util.isFilled(currentTopicEntry.content)) { // may have DUMMY currentTopicEntry here
                newTitle += " - " + Util.shortenString(currentTopicEntry.content, 30);
            }
            getSupportActionBar().setTitle(newTitle);
            // enable back button in action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootTopic); // show back button if not root
        }
    }





}