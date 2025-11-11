package com.example.fayf_android002;

import androidx.fragment.app.FragmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuntimeTest {

    Logger logger = LoggerFactory.getLogger(RuntimeTest.class);

    public void runTests(FragmentManager fragmentManager) {
        // Placeholder for runtime test logic

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
