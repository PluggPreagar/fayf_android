package com.example.fayf_android002.RuntimeTest;

import androidx.fragment.app.FragmentManager;
import com.example.fayf_android002.*;
import com.example.fayf_android002.Entry.Entries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuntimeTest {

    Logger logger = LoggerFactory.getLogger(RuntimeTest.class);

    public static void initSelfTest() {
        // Placeholder for any initialization logic needed before running tests
        Config.TENANT.setValue("RuntimeTest");
        Entries.resetEntries();
        Entries.setEntryInternal("/", "t1", "c1");
        Entries.setEntryInternal("/", "t2", "c2 >");
        Entries.setEntryInternal("/", "t3", "c3");
        Entries.setEntryInternal("/t2", "t2.1", "c2.1");
        Entries.setEntryInternal("/t2", "t2.2", "c2.2");
        Entries.setEntryInternal("/_/config", "dark_mode_YN", "false");
        Entries.setEntryInternal("/_/config", "test_string", "default_value");
    }


    public void runTests(FragmentManager fragmentManager) {
        // Placeholder for runtime test logic
        logger.info("Running runtime tests...");
        initSelfTest();
        logger.info("Entry tree: " + Entries.size() + " entries.");

        Entries.rootTopic(); // ensure starting at root, force reload
        // delay 200ms to allow UI to update
        try {
            Thread.sleep(200); // Delay for 200 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            e.printStackTrace();
        }

        UtilDebug.inspectView();

        ActionQueue queue = new ActionQueue(R.id.FirstFragment);

        // TODO handle 404 - "empty entries" , "to be initialized"-Fragment


        queue.testBlock("config toggle boolean - dark_mode")
                .click(R.id.action_settings)
                .waitForVisible( "dark_mode: false", 2*5000) // wait up to 10s for config to load
                .click("dark_mode: false")
                .delay(500)
                .waitForVisible( "dark_mode: true", 2*5000)
                .delay(500)
                .click("dark_mode: true")
                .delay(500)
                .waitForVisible( "dark_mode: false")
                .doc("navigating to hidden = non-existing entry path to force root-topic")
                .clickUp() // TODO one clickUp should go to root if no parent
                .clickBack()
                .clickBack()
                .waitForVisible("c1")
        ;

        queue.testBlock("config set value directly - test_string")
                .click(R.id.action_settings) // TODO: do not hard code button IDs, find by text or entryKey
                .waitForVisible( "test_string: default_value")
                .click("test_string: default_value").doc("click on config - edit value")
                //.delay(500)
                //.longClick("test_string: default_value")
                .waitForVisible(R.id.editext_second)
                .isText(R.id.editext_second, "default_value")
                .setText(R.id.editext_second, "new_value")
                .click(R.id.button_send)
                .waitForVisible("test_string: new_value")
                .clickBack()
                .clickBack()
        ;

        queue.testBlock("start at root")
                .waitForVisible( "c1")
                .waitForVisible( "c2 >");
        queue.testBlock("stay - no child to t1")
                .waitForVisible("c1")
                .click("c1");
        queue.testBlock("goto 't2' child")
                .click("c2")
                .waitForVisible("c2.1")
                .waitForVisible("c2.2" )
                .clickBack();

        String newContent = "c2_"+ Util.getCurrentTimestamp();
        queue.testBlock("edit t2")
                .longClick("c2").waitForVisible(R.id.editext_second)
                .setText(R.id.editext_second, newContent)
                .click(R.id.button_send)
                .waitForVisible(newContent + " >")
                ;

        queue.testBlock("add first child to t3 - by click on fab-button during edit")
                .longClick("c3")
                .waitForVisible(R.id.editext_second)
                .click(R.id.fab).delay(500).waitForVisible(R.id.editext_second)
                .setText(R.id.editext_second, "c3.1")
                .click(R.id.button_send)
                .waitForVisible("c3.1").doc("show new child c3.1")
                .clickBack()
                .waitForVisible( "c3 >").doc("c3 is now a topic with child")
                ;

        queue.testBlock("done"); // check finish of last block
        queue.run();
    }



}
