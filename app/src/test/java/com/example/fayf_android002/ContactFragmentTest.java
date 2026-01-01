package com.example.fayf_android002;

import junit.framework.TestCase;

import java.util.Map;

public class ContactFragmentTest extends TestCase {

    public void testCreateLink() {
        assertEquals("https://fayf.info/?tid=12345"
                , ContactFragment.createLink(Map.of("tid", "12345")));
    }

    public void testExtractTenantIdFromQrContent() {
        assertEquals("67890"
                , ContactFragment.extractTenantIdFromQrContent("https://fayf.info?tid=67890"));

    }
}