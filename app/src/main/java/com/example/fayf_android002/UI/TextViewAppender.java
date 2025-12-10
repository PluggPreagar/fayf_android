package com.example.fayf_android002.UI;

import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import com.example.fayf_android002.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TextViewAppender {
    private static final Logger log = LoggerFactory.getLogger(TextViewAppender.class);
    private static TextView logTextView;
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    private static List<String> logLines = Collections.synchronizedList(new ArrayList<>());
    private static long nextTimestampToFlush = 0;

    public static void initialize(TextView textView) {
        logTextView = textView;
    }

    public static String appendLog(String message) {
        logLines.add(message);
        while (logLines.size() > 500) {
            // remove oldest 50 lines
            logLines.subList(0, 50).clear();
        }
        long currentTime = System.currentTimeMillis();
        if (logTextView != null
                && logTextView.getVisibility() == TextView.VISIBLE
                && currentTime > nextTimestampToFlush) {
            // Delay update in 500ms - Batch updates to avoid UI lag
            nextTimestampToFlush = currentTime + 500;
            uiHandler.postDelayed(() -> {
                // still have issues with concurrent modification exception, so make a copy
                try{
                    String join = String.join("\n", logLines);
                    logTextView.setText(join);
                } catch (Exception e) {
                    // ignore concurrent modification
                }
            }, 500);
        }
        return message;
    }

    private static void enhanceLog(ILoggingEvent iLoggingEvent) {
        // add stack trace info for errors
        if (iLoggingEvent.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.ERROR)) {
            UtilDebug.logCompactCallStack("Error Callstack: \n\t" + iLoggingEvent.getFormattedMessage());
        }
    }


    public static void setupSLF4J() {
        // Get the root logger
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        // read logback.xml configuration
        log.info("Setting up TextViewAppender for SLF4J");



        // Create a custom appender
        ch.qos.logback.core.AppenderBase<ILoggingEvent> textViewAppender =
                new ch.qos.logback.core.AppenderBase<>() {
                    @Override
                    protected void append(ILoggingEvent iLoggingEvent) {
                        enhanceLog(iLoggingEvent);
                        appendLog(iLoggingEvent);
                    }
                };

        // Start the appender
        textViewAppender.setContext(rootLogger.getLoggerContext());
        textViewAppender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(textViewAppender);
    }

    private static void appendLog(ILoggingEvent iLoggingEvent) {
        String logMessage = String.format("%s [%s] - %s",
                iLoggingEvent.getLevel().toString().charAt(0),
                Util.shortenLeft( iLoggingEvent.getLoggerName(), 7),
                iLoggingEvent.getFormattedMessage());
        appendLog(logMessage);
    }


}