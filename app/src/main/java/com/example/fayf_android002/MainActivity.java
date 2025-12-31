package com.example.fayf_android002;

import android.content.Context;
import android.os.Bundle;
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
import com.example.fayf_android002.IO.IncrementalUpdateScheduler;
import com.example.fayf_android002.RuntimeTest.RuntimeChecker;
import com.example.fayf_android002.RuntimeTest.RuntimeTest;
import com.example.fayf_android002.UI.TextViewAppender;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.databinding.ActivityMainBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends AppCompatActivity  {

    public static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private ActivityMainBinding binding;
    NestedScrollView scrollView;

    private EntryViewModel entryViewModel;

    private static MainActivity instance = null;
    public Menu menu = null;
    private boolean isSubmittingQuery   = false;
    private boolean[] menuDefaultVisibilityStored = null;

    public static MainActivity getInstance() {
        return instance;
    }

    public static void userInfo(String s) {
        if (instance != null) {
             instance.runOnUiThread(() -> {
                Toast.makeText(instance.getContext(), s, Toast.LENGTH_SHORT).show();
            });
        } else {
            logger.warn("MainActivity instance is null - cannot show toast: {}", s);
        }
    }

    public static Context getContext() {
        if (null == instance) {
            logger.error("MainActivity instance is null - cannot get context");
            return null;
        }
        Context context =  instance.getApplicationContext();
        if (null == context) {
            logger.error("MainActivity getContext() - application context is null");
        }
        return null == context ? instance : context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this; // set instance early for Entries.loadConfig - needs context, but not UI yet
        super.onCreate(savedInstanceState);

        if (Entries.entryTree.isEmpty()) {
            logger.info("Entries are empty on MainActivity onCreate - loading from local storage");
            Entries.loadConfig( getContext());
        } else {
            logger.info("Entries already loaded before MainActivity onCreate");
        }

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

        if (Config.SHOW_LOGS.getBooleanValue()) {
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
        if (Config.RUN_SELF_TEST.getBooleanValue()) {
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
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof FirstFragment && currentFragment.isVisible()){
                    logger.info("FAB clicked - switching to InputFragment - add new entry to current topic");
                    int randomId = (int) (Math.random() * 100000);
                    EntryKey key = new EntryKey(Entries.getCurrentEntryKey().getFullPath(), String.valueOf(randomId));
                    Entries.setCurrentEntryKey(key);
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

        IncrementalUpdateScheduler.startPeriodicUpdates( );

        RuntimeChecker.check();
        // on first run load FirstFragment or if tenant is "tst"
        if (((String)Config.TENANT.getDefaultValue()).equals(Config.TENANT.getValue())) {
            switchToContactFragment();
        } else {
            switchToFirstFragment();
        }

    }




    private boolean loadFragment(Fragment fragment) {
        logger.info("Loading fragment: {}", fragment.getClass().getSimpleName());
        RuntimeChecker.check();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }

    public static boolean switchToFirstFragment() {
        if (null == instance) {
            logger.error("MainActivity instance is null - cannot switch to FirstFragment");
            return false;
        }
        return instance.loadFragment(new FirstFragment());
    }

    public static boolean switchToInputFragment() {
        if (null == instance) {
            logger.error("MainActivity instance is null - cannot switch to InputFragment");
            return false;
        }
        return instance.loadFragment(new InputFragment());
    }

    public static boolean switchToContactFragment() {
        if (null == instance) {
            logger.error("MainActivity instance is null - cannot switch to ContactFragment");
            return false;
        }
        boolean loaded = instance.loadFragment(new ContactFragment());
        instance.getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
        return loaded;
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
    public void onPause() {
        logger.info("MainActivity onPause() called");
        Entries.save( getContext());
        super.onPause();
    }

    @Override
    public void onStop() {
        logger.info("MainActivity onStop() called");
        // wait for storage to finish
        while ( Entries.isSaveRunning()) {
            try {
                logger.info("Waiting for DataStorageLocal to finish saving...");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for DataStorageLocal to finish saving", e);
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        // will be reseted each time Activity is resumed / menu is invalidated
        if (Config.FULL_MENU_YN.getBooleanValue()){
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                item.setVisible(true); // Enable visibility
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RuntimeChecker.check();
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (null == item || null == item.getTitle() || item.getItemId() == R.id.menu_main) {
            // KLUDGE allow Runtime-Test to trigger back button
            // but we can't create/get MenuItem directly
            logger.info("back-menu-button pressed");
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof FirstFragment)) {
                logger.info("Switching to FirstFragment on back-menu-button");
                switchToFirstFragment();
            }
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
            // switch to first fragment showing config entries
            // check if first fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (!(currentFragment instanceof FirstFragment)) {
                logger.info("Switching to FirstFragment to show config entries");
                switchToFirstFragment();
            }
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
            Toast.makeText(getContext(), "download data", Toast.LENGTH_SHORT).show();
            Entries.loadAsync( getContext(), true);
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
        } else if ( id == R.id.action_report_bug){
            // show bug report instructions --> create bug node / and open in InputFragment
            UtilDebug.logCompactCallStack();
            Entry currentEntry = Entries.getCurrentEntry();
            int randomId = (int) (Math.random() * 100000);
            String bugReportContent = "Bug Report " + randomId +  "\n\n"
                    + "Topic: " + Entries.getCurrentEntryKey().getFullPath() + "\n"
                    + "Content: " + ( null == currentEntry ? "<null>" : currentEntry.getContent() ) + "\n\n"
                    + "Please describe the issue here...\n";
            EntryKey bugEntryKey = new EntryKey( "/_/bug", "bug_" + randomId);
            Entries.setEntry( bugEntryKey, bugReportContent, getContext());
            Entries.setCurrentEntryKey(bugEntryKey);
            switchToInputFragment(); // reload InputFragment
        } else if ( id == R.id.action_contact){
            logger.info("Contact menu item selected");
            switchToContactFragment();
            return true;
        } else if (id == R.id.action_switch_tenant) {
            logger.info("Switch tenant menu item selected");
            Entries.setCurrentEntryKey( new EntryKey(  Config.CONFIG_PATH + "/tenant") );
            switchToFirstFragment();
        } else if (id == R.id.action_save) {
            Entries.save( getContext());
            Toast.makeText(this, "saving data", Toast.LENGTH_SHORT).show();
        }
        RuntimeChecker.check();
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
        if (false){
            super.onBackPressed();
            return;
        }
        // super.onBackPressed(); // SKIPP to prevent closing ...
        // same es pressing ESC-key or up-button in action bar
        String topicBefore = Entries.getCurrentEntryKey().getFullPath();
        // switch from InputFragment to FirstFragment if needed
        // TODO only if in InputFragment

        // check current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof InputFragment) {
            logger.info("Back pressed in InputFragment - switching to FirstFragment");
            if (!Entries.isTopic(Entries.getCurrentEntryKey())) {
                // if in InputFragment for a topic - go up one level
                Entries.upOneTopicLevel();
                // TODO check why
                logger.warn("Navigating up topic: {} (from {}) (BACK from InputFragment)", Entries.getCurrentEntryKey().getFullPath(), topicBefore);
            }
            switchToFirstFragment();
        } else {
            logger.info("Back pressed in FirstFragment or other - navigating up topic");
            Entries.upOneTopicLevel();
            logger.info("Navigating up topic: {} (from {}) (BACK)", Entries.getCurrentEntryKey().getFullPath(), topicBefore);
        }
        //
    }





    /*


     */

    public void updateActionBarTitle( ) { // allow to handover already known currentTopicEntry
        EntryKey currentTopicEntry = Entries.getCurrentEntryKey();
        // update title in action bar
        if (getSupportActionBar() != null) {
            String newTitle = "";
            boolean isRootTopic = EntryTree.isRootKey(currentTopicEntry);
            Entry entry = Entries.getEntry(currentTopicEntry);
            logger.info("Updating action bar title for topic: \"{}\" {}"
                    , null == entry ? "" : entry.getContent()
                    , isRootTopic ? "(root topic)" : "(enable back button)");
            String tenant = Config.TENANT.getValue();
            newTitle = "";
            if (Util.isFilled(tenant)) {
                tenant = tenant.replaceAll("^.*:|__.*$", " "); // remove id if present
                // TODO KLUDGE .. remove trailing _<numbers>, but not years
                tenant = tenant.replaceAll("_[0-9][0-9][0-9][0-9][0-9][0-9]*$", "");
            }
            if (isRootTopic ||  null == entry || !Util.isFilled(entry.getContent())) {
                getSupportActionBar().setTitle("FayF of " + tenant);
                getSupportActionBar().setSubtitle("");
            } else {
                getSupportActionBar().setTitle(tenant);
                newTitle = Util.shortenString(entry.getContent(), 30);
                getSupportActionBar().setSubtitle(newTitle); // show full title as subtitle
            }
            // enable back button in action bar
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRootTopic); // show back button if not root
        }
        getSupportActionBar().show();
    }

}