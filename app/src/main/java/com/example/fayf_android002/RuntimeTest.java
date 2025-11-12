package com.example.fayf_android002;

import android.view.View;
import android.widget.Button;
import androidx.fragment.app.FragmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuntimeTest {

    Logger logger = LoggerFactory.getLogger(RuntimeTest.class);

    public void runTests(FragmentManager fragmentManager) {
        // Placeholder for runtime test logic

        RuntimeTester runtimeTester = new RuntimeTester(fragmentManager);
        logger.info("Running runtime tests...");

        RuntimeTester.FragmentInfo firstFragmentInfo = runtimeTester.getRegisteredFragment("FirstFragment");
        logger.info("Is FirstFragment visible? " + (null == firstFragmentInfo ? "unkown" : firstFragmentInfo.fragment.isVisible()));

        Button btn = (Button) firstFragmentInfo.view.findViewById(R.id.button1);
        // apply click on button in 1 second
        logger.info("Button1 text: " + btn.getText().toString());
        new android.os.Handler().postDelayed(() -> btn.performClick(), 1000);


        // check if btn1 text changed in 2 seconds
        new android.os.Handler().postDelayed(() -> {
            logger.info("Button1 text after click: " + btn.getText().toString());
        }, 2000);

        // long press button in 3 seconds
        new android.os.Handler().postDelayed(() -> {
            btn.performLongClick();
            logger.info("Button1 long clicked.");
        }, 3000);



        // check if InputFragment is visible
        RuntimeTester.FragmentInfo inputFragmentInfo = runtimeTester.getRegisteredFragment("InputFragment");
        logger.info("Is InputFragment visible? " + (null == inputFragmentInfo ? "unkown" : inputFragmentInfo.fragment.isVisible()));
        // check again in 2 seconds
        new android.os.Handler().postDelayed(() -> {
            RuntimeTester.FragmentInfo inputFragmentInfo2 = runtimeTester.getRegisteredFragment("InputFragment");
            logger.info("Is InputFragment visible? " + (null == inputFragmentInfo2 ? "unkown" : inputFragmentInfo2.fragment.isVisible()));
        }, 4000);


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
