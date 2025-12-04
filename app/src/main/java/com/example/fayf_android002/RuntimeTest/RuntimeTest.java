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
        Entries.clearAllEntries();
        Entries.setEntry("/", "t1", "c1");
        Entries.setEntry("/", "t2", "c2 >");
        Entries.setEntry("/", "t3", "c3");
        Entries.setEntry("/t2", "t2.1", "c2.1");
        Entries.setEntry("/t2", "t2.2", "c2.2");
        Entries.checkDataIntegrity();
    }


    public void runTests(FragmentManager fragmentManager) {
        // Placeholder for runtime test logic
        logger.info("Running runtime tests...");
        logger.info("Entry tree:\n" + Entries.getInstance().toString());
        UtilDebug.inspectView();

        ActionQueue queue = new ActionQueue(R.id.FirstFragment).delay(1000);
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
                .click(R.id.button_send).waitForVisible(R.id.button2)
                .isVisible(R.id.button2, newContent + " >")
                ;
        queue.testBlock("add first child to t3 - by click on fab-button during edit")
                .longClick(R.id.button3)
                .waitForVisible(R.id.editext_second)
                .click(R.id.fab).delay(500).waitForVisible(R.id.editext_second)
                .setText(R.id.editext_second, "c3.1")
                .click(R.id.button_send).waitForVisible(R.id.button3)
                .isVisible(R.id.button3, "c3 >").doc("c3 is now a parent")
                ;
        queue.testBlock("done"); // check finish of last block
        queue.run();
    }

    public void runTests2(FragmentManager fragmentManager) {

        RuntimeTester runtimeTester = new RuntimeTester(fragmentManager);
        logger.info("Running runtime tests...");

        // Check if a first Fragment is visible
        boolean isFirstFragmentVisible = runtimeTester.isFragmentVisible(R.id.FirstFragment);
        logger.info("Is FirstFragment visible? " + isFirstFragmentVisible);

        // check if Button1 in FirstFragment is visible
        boolean isButton1Visible = runtimeTester.isViewVisible(R.id.FirstFragment, R.id.button1);
        logger.info("Is button1 in FirstFragment visible? " + isButton1Visible);

        // Get text from a Button in the FirstFragment
        String buttonText = Util.optional(runtimeTester.getTextFromView(R.id.FirstFragment, R.id.button1),"");
        logger.info("Text of button1 in FirstFragment: " + buttonText);

        // check if Button1 in FirstFragment can be clicked
        try {
            runtimeTester.performAction(R.id.FirstFragment, R.id.button1);
            logger.info("Button1 in FirstFragment clicked successfully.");
        } catch (Exception e) {
            logger.error("Failed to click Button1 in FirstFragment.", e);
        }


        // Check if a view is visible in the SecondFragment
        boolean isVisible = runtimeTester.isViewVisible(R.id.SecondFragment, R.id.editext_second);
        logger.info("Is edittext_second visible in SecondFragment? " + isVisible);

    }

}
