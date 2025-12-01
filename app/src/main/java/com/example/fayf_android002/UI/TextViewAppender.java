package com.example.fayf_android002.UI;

import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.example.fayf_android002.RuntimeTest.UtilDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class TextViewAppender {
    private static final Logger log = LoggerFactory.getLogger(TextViewAppender.class);
    private static TextView logTextView;
    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    public static void initialize(TextView textView) {
        logTextView = textView;
    }

    public static String appendLog(String message) {
        if (logTextView != null) {
            uiHandler.post(() -> {
                // insert at beginning - top to bottom
                logTextView.setText(String.format("%s\n%s", message, logTextView.getText().toString()));
                // trim to last 1000 lines
                int maxLines = 100;
                int lineCount = logTextView.getLineCount();
                if (lineCount > maxLines) {
                    String text = logTextView.getText().toString();
                    List<String> split = Arrays.asList(text.split("\n"));
                    // keep only the last maxLines lines
                    split = split.subList(0, maxLines - 20);
                    logTextView.setText(String.join("\n", split));
                }
            });
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
                        appendLog(iLoggingEvent.getFormattedMessage());
                    }
                };

        // Start the appender
        textViewAppender.setContext(rootLogger.getLoggerContext());
        textViewAppender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(textViewAppender);
    }


}