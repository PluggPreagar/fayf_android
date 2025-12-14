package com.example.fayf_android002;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.example.fayf_android002.Entry.Entries;
import com.example.fayf_android002.Entry.Entry;
import com.example.fayf_android002.Entry.EntryKey;
import com.example.fayf_android002.Entry.EntryTree;
import com.example.fayf_android002.RuntimeTest.RuntimeTest;
import com.example.fayf_android002.UI.CustomOnTouchListener;
import com.example.fayf_android002.UI.MotionEventFixed;
import com.example.fayf_android002.UI.TextViewAppender;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.databinding.ActivityMainBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private ActivityMainBinding binding;
    NestedScrollView scrollView;

    private EntryViewModel entryViewModel;

    private static MainActivity instance = null;
    public Menu menu = null;
    private boolean isSubmittingQuery   = false;

    public static MainActivity getInstance() {
        return instance;
    }

    public static void notifyUser(String s) {
        if (instance != null) {
             instance.runOnUiThread(() -> {
                Toast.makeText(instance.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            });
        } else {
            logger.warn("MainActivity instance is null - cannot show toast: {}", s);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        // keep state on orientation change - use ViewModel
        entryViewModel = new ViewModelProvider(this).get(EntryViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView logTextView = findViewById(R.id.logTextView);
        TextViewAppender.initialize(logTextView);
        TextViewAppender.setupSLF4J();
        logTextView.setOnLongClickListener(v -> {
            TextViewAppender.clearLog();
            return true;
        });

        logger.info( "MainActivity onCreate() called" );

        setSupportActionBar(binding.toolbar);

        // keep current entry on orientation change
        // Entries.setCurrentEntryKey(EntryTree.ROOT_ENTRY_KEY); // clear current entry on app start - go to root

        if (Config.SHOW_LOGS.asBoolean()) {
            binding.logScrollView.setVisibility(View.VISIBLE);
        } else {
            binding.logScrollView.setVisibility(View.GONE);
        }
        if (false) {
            Config.TENANT.setValue("tst5");
            Config.RUN_SELF_TEST.setValue("true"); // disable self-test auto-run for normal app start
        }

        // init self-test entries if started
        // prevent auto-load from storage
        if (Config.RUN_SELF_TEST.asBoolean()) {
            logger.info("Config.RUN_SELF_TEST is enabled - initializing self-test entries");
            RuntimeTest.initSelfTest();
            // start self test in 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    RuntimeTest runtimeTest = new RuntimeTest();
                    runtimeTest.runTests(getSupportFragmentManager());
                } catch (Exception e) {
                    logger.error("Error while running self-test in a separate thread", e);
                }
            }).start();
        }

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
                 */
                // TODO KLUDGE check if in InputFragment is set
                //
                if (null != UtilDebug.getView(R.id.FirstFragment)) {
                    logger.info("FAB clicked - switching to InputFragment");
                    switchToInputFragment();
                } else {
                    logger.info("FAB clicked - already in InputFragment - create child entry");
                    // create child entry and set current entry as topic
                    // get random id
                    int randomId = (int) (Math.random() * 100000);
                    EntryKey key = new EntryKey(Entries.getCurrentEntryKey().getFullPath(), String.valueOf(randomId));
                    Entries.setCurrentEntryKey(key);
                    switchToInputFragment(); // reload InputFragment
                }
            }
        });

        Entries.setOnTopicChangedListener( "MainActivity", currentTopicEntry -> {
            runOnUiThread(() -> {refresh( currentTopicEntry );});
        });
        refresh(Entries.getCurrentEntryKey());

        switchToFirstFragment();

    }


    private void loadFragment(Fragment fragment) {
        logger.info("Loading fragment: {}", fragment.getClass().getSimpleName());
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void switchToFirstFragment() {
        loadFragment(new FirstFragment());
    }

    public void switchToInputFragment() {
        loadFragment(new InputFragment());
    }

    private void refresh(EntryKey currentTopicEntry){
        // called in runOnUiThread from onTopicChangedListener
        logger.info("Refreshing MainActivity UI for topic: {}", Entries.getCurrentEntryKey().getFullPath());
        updateActionBarTitle( );
         //
        UtilDebug.logCompactCallStack("MainActivity refresh");
        // UtilDebug.inspectView();
    }


    @Override
    public void onDestroy() {
        logger.info("MainActivity onDestroy() called");
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu; // for Runtime-Test-Access

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Set a listener to detect when the SearchView is expanded
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Update the search term when the SearchView is expanded
                searchView.setQuery(Entries.getSearchQuery(), false); // Set the desired initial query
                return true; // Return true to expand the view
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                return true; // Return true to collapse the view
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                    // Handle search query submission
                isSubmittingQuery = true;
                performSearch(query);
                searchView.clearFocus(); // hide keyboard
                searchItem.collapseActionView(); // collapse search view -> triggers onQueryTextChange with empty text
                isSubmittingQuery = false;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes in the search bar
                if (isSubmittingQuery) {
                    // ignore text change events triggered by query submission
                    return true;
                }
                filterResults(newText);
                return true;
            }
    });

    super.onCreateOptionsMenu(menu);


        return true;
    }

    private void performSearch(String query) {
        logger.info("Search submitted: {}", query);
    }

    private void filterResults(String newText) {
        // Implement filtering logic here
        // only if searchView is expanded - otherwise ignore, as collpasing triggers empty text change
        MenuItem searchItem = menu.findItem(R.id.action_search);
        logger.info("Search text changed: {}", newText);
        Entries.setSearchQuery(newText);
        Entries.setCurrentEntryKey( Entries.getCurrentEntryKey());
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
            if (Entries.getCurrentEntryKey().getFullPath().startsWith(Config.CONFIG_PATH)) {
                Entries.setCurrentEntryKey( null ); // clear current entry
            } else {
                Entries.setCurrentEntryKey( new EntryKey(Config.CONFIG_PATH) );
            }
            return true;
        } else if (id == R.id.action_about) {
            logger.info("About menu item selected");
            Toast.makeText(this, R.string.action_about_msg, Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_test) {
            logger.info("Runtime tests menu item selected");
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
    } else if (id == R.id.action_check) {
        Entries.setCurrentEntryKey(new EntryKey("/_/check"));
    } else if (id == R.id.action_refresh) {
            logger.info("Refresh UI");
            // force refresh of FirstFragment
            runOnUiThread(() -> {
                refresh( null);
            });
            return true;
        } else if (id == R.id.action_load_from_web) {
            logger.info("action_load_from_web");
            Toast.makeText(getApplicationContext(), "download data", Toast.LENGTH_SHORT).show();
            Entries.load_async( getApplicationContext(), true);
            return true;
        } else if (id == R.id.toggle_log) {
            Config.SHOW_LOGS.toggleValue();
            if (Integer.valueOf( Config.SHOW_LOGS.getValue()) > 0) {
                Toast.makeText(this, "Logs shown", Toast.LENGTH_SHORT).show();
                binding.logScrollView.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Logs hidden", Toast.LENGTH_SHORT).show();
                binding.logScrollView.setVisibility(View.GONE);
            }
            return true;
        } else if ( id == R.id.action_log_details) {
            // show logs
            if (Integer.valueOf( Config.SHOW_LOGS.getValue()) == 0) {
                Config.SHOW_LOGS.toggleValue();
                binding.logScrollView.setVisibility(View.VISIBLE);
            }
            // iterate over visible entries
            UtilDebug.inspectView();
            Entries.getEntriesIterator( Entries.getOffset()).forEachRemaining( entry -> {
                logger.info(  entry.toString() );
            });
        } else if ( id == R.id.action_force_sort){
            logger.info("Forcing sort of entries");
            Entries.sortCurrentTopic();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        String topicBefore = Entries.getCurrentEntryKey().getFullPath();
        Entries.upOneTopicLevel();
        logger.info("Navigating up topic: {} (from {}) (UP)", Entries.getCurrentEntryKey().getFullPath(), topicBefore);
        return true;
    }


    @Override
    public void onBackPressed() {
        // super.onBackPressed(); // SKIPP to prevent closing ...
        // same es pressing ESC-key or up-button in action bar
        String topicBefore = Entries.getCurrentEntryKey().getFullPath();
        // switch from InputFragment to FirstFragment if needed
        // TODO only if in InputFragment

        // check current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof InputFragment) {
            logger.info("Back pressed in InputFragment - switching to FirstFragment");
            switchToFirstFragment();
        } else {
            logger.info("Back pressed in FirstFragment or other - navigating up topic");
            Entries.upOneTopicLevel();
            logger.info("Navigating up topic: {} (from {}) (BACK)", Entries.getCurrentEntryKey().getFullPath(), topicBefore);
        }
        //
    }




    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // forward touch event to child views
        logger.debug("MainActivity onTouch: event: " + event.toString());
        CustomOnTouchListener viewTouchedInProgress = Entries.getViewTouchedInProgress();
        if (viewTouchedInProgress != null ) {
            // prevent scrolling once Direction is Set
            // true to consume event here -> prevent scrollview from scrolling
            boolean b = viewTouchedInProgress.onTouch( new MotionEventFixed( event) );
            // TODO KLUDGE - check direction
            boolean b2 = CustomOnTouchListener.directionX;
            Log.d(TAG, "MainActivity onTouch: forwarded, returned: " + b +" is:" + b2 + " event: " + event.toString());
            return b2;
        } else {
            Log.d(TAG, "MainActivity onTouch: " + event.toString());
        }
        return false;
    }

    /*


     */

    public void updateActionBarTitle( ) { // allow to handover already known currentTopicEntry
        EntryKey currentTopicEntry = Entries.getCurrentEntryKey();
        // update title in action bar
        if (getSupportActionBar() != null) {
            String newTitle = "FayF";
            boolean isRootTopic = EntryTree.isRootKey(currentTopicEntry);
            Entry entry = Entries.getEntry(currentTopicEntry);
            logger.info("Updating action bar title for topic: \"{}\" {}"
                    , null == entry ? "" : entry.content
                    , isRootTopic ? "(root topic)" : "(enable back button)");
            if (isRootTopic ||  null == entry || !Util.isFilled(entry.content)) {
                String tenant = Config.TENANT.getValue();
                if (Util.isFilled(tenant)) {
                    newTitle += " of " + Config.TENANT.getValue();
                }
            } else {
                newTitle = Util.shortenString(entry.content, 30);
            }
            getSupportActionBar().setTitle(newTitle);
            // enable back button in action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootTopic); // show back button if not root
        }
        getSupportActionBar().show();
    }





}