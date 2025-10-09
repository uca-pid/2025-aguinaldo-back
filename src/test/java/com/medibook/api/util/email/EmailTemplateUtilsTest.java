package com.medibook.api.util.email;

import org.junit.jupiter.api.Test;

import com.medibook.api.util.EmailTemplateUtils;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateUtilsTest {

    @Test
    void testCreateEmailWrapper() {
        String title = "Test Title";
        String content = "<p>Test content</p>";
        String result = EmailTemplateUtils.createEmailWrapper(title, content);

        assertNotNull(result);
        assertTrue(result.contains(title));
        assertTrue(result.contains(content));
        assertTrue(result.contains("MediBook"));
        assertTrue(result.contains("<html>"));
    }

    @Test
    void testCreateInfoBlock() {
        String content = "Test info";
        String backgroundColor = "#f0f0f0";
        String borderColor = "#ccc";
        String result = EmailTemplateUtils.createInfoBlock(content, backgroundColor, borderColor);

        assertNotNull(result);
        assertTrue(result.contains(content));
        assertTrue(result.contains("background-color: " + backgroundColor));
        assertTrue(result.contains("border: 1px solid " + borderColor));
    }

    @Test
    void testCreateTitle() {
        String title = "Test Title";
        String color = "#ff0000";
        String result = EmailTemplateUtils.createTitle(title, color);

        assertNotNull(result);
        assertTrue(result.contains(title));
        assertTrue(result.contains("color: " + color));
        assertTrue(result.contains("<h1"));
    }

    @Test
    void testCreateList() {
        String[] items = {"Item 1", "Item 2", "Item 3"};
        String result = EmailTemplateUtils.createList(items);

        assertNotNull(result);
        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("<li>Item 1</li>"));
        assertTrue(result.contains("<li>Item 2</li>"));
        assertTrue(result.contains("<li>Item 3</li>"));
        assertTrue(result.contains("</ul>"));
    }

    @Test
    void testCreateListEmpty() {
        String[] items = {};
        String result = EmailTemplateUtils.createList(items);

        assertNotNull(result);
        assertTrue(result.contains("<ul>"));
        assertTrue(result.contains("</ul>"));
        assertFalse(result.contains("<li>"));
    }

    @Test
    void testCreateSignature() {
        String result = EmailTemplateUtils.createSignature();

        assertNotNull(result);
        assertTrue(result.contains("Saludos"));
        assertTrue(result.contains("MediBook"));
        assertTrue(result.contains("<br>"));
    }

    @Test
    void testConstants() {
        assertEquals("Arial, sans-serif", EmailTemplateUtils.DEFAULT_FONT_FAMILY);
        assertEquals("#2c5aa0", EmailTemplateUtils.PRIMARY_COLOR);
        assertEquals("#28a745", EmailTemplateUtils.SUCCESS_COLOR);
        assertEquals("#ffc107", EmailTemplateUtils.WARNING_COLOR);
        assertEquals("#dc3545", EmailTemplateUtils.DANGER_COLOR);
        assertEquals("#f8f9fa", EmailTemplateUtils.LIGHT_BACKGROUND);
        assertEquals("#fff3cd", EmailTemplateUtils.WARNING_BACKGROUND);
        assertEquals("#f8d7da", EmailTemplateUtils.DANGER_BACKGROUND);
    }
}