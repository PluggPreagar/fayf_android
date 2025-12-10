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
        Entries.setEntry("/", "t1", "c1");
        Entries.setEntry("/", "t2", "c2 >");
        Entries.setEntry("/", "t3", "c3");
        Entries.setEntry("/t2", "t2.1", "c2.1");
        Entries.setEntry("/t2", "t2.2", "c2.2");
        Entries.setEntry("/_/config", "dark_mode_YN", "false");
        Entries.setEntry("/_/config", "test_string", "default_value");
    }


    public void runTests(FragmentManager fragmentManager) {
        // Placeholder for runtime test logic
        logger.info("Running runtime tests...");
        logger.info("Entry tree:\n" + Entries.getInstance().toString());
        UtilDebug.inspectView();

        ActionQueue queue = new ActionQueue(R.id.FirstFragment);

        // TODO handle 404 - "empty entries" , "to be initialized"-Fragment


        queue.testBlock("config toggle boolean - dark_mode")
                .click(R.id.action_settings)
                .waitForVisible( "dark_mode: false")
                .click("dark_mode: false")
                .delay(500)
                .waitForVisible( "dark_mode: true")
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
                .isVisible(R.id.button1, "c1")
                .isVisible(R.id.button2, "c2 >");
        queue.testBlock("stay - no child to t1")
                .isVisible(R.id.button1, "c1")
                .click(R.id.button1);
        queue.testBlock("goto 't2' child")
                .click(R.id.button2,500)
                .waitForVisible(R.id.button1, "c2.1")
                .isVisible(R.id.button2, "c2.2" )
                .clickBack();

        String newContent = "c2_"+ Util.getCurrentTimestamp();
        queue.testBlock("edit t2")
                .longClick(R.id.button2,1000).waitForVisible(R.id.editext_second)
                .setText(R.id.editext_second, newContent)
                .click(R.id.button_send)
                .waitForVisible(newContent + " >")
                ;

        queue.testBlock("add first child to t3 - by click on fab-button during edit")
                .longClick(R.id.button3)
                .waitForVisible(R.id.editext_second)
                .click(R.id.fab).delay(500).waitForVisible(R.id.editext_second)
                .setText(R.id.editext_second, "c3.1")
                .click(R.id.button_send)
                .waitForVisible(R.id.button1, "c3.1").doc("show new child c3.1")
                .clickBack()
                .waitForVisible(R.id.button3, "c3 >").doc("c3 is now a topic with child")
                ;

        queue.testBlock("done"); // check finish of last block
        queue.run();
    }



}
