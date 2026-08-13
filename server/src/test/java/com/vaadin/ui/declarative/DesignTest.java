package com.vaadin.ui.declarative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Tests for {@link Design} declarative support class.
 *
 * @author Vaadin Ltd
 */
public class DesignTest {

    private static final String NON_ASCII_STRING = "\u043C";

    // Expected UTF-8 bytes for '\u043C' (Cyrillic small letter el)
    private static final byte[] EXPECTED_UTF8_BYTES = new byte[] { (byte) 0xD0, (byte) 0xBC };

    @Test
    public void write_usesUtf8Encoding_consistentOutput() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Component label = new Label(NON_ASCII_STRING);
        Design.write(label, out);
        byte[] bytes = out.toByteArray();
        // Verify the output contains the correct UTF-8 bytes for the non-ASCII char
        // This proves Design.write uses UTF-8, not the system default charset
        int index = findBytes(bytes, EXPECTED_UTF8_BYTES);
        assertNotEquals(
                "Design.write should output UTF-8 encoded bytes for non-ASCII characters",
                -1, index);
    }

    @Test
    public void write_writtenLabelHasCorrectValue() throws IOException {
        String html = getHtml();
        assertEquals(
                "Non ascii string parsed from serialized HTML "
                        + "differs from expected",
                NON_ASCII_STRING, getHtmlLabelValue(html));
    }

    private String getHtml() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Component label = new Label(NON_ASCII_STRING);
        Design.write(label, out);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private String getHtmlLabelValue(String html) {
        Document document = Jsoup.parse(html);
        Element label = document.select("vaadin-label").get(0);

        StringBuilder builder = new StringBuilder();
        for (Node child : label.childNodes()) {
            if (child instanceof TextNode) {
                builder.append(((TextNode) child).getWholeText());
            }
        }
        return builder.toString().trim();
    }

    private int findBytes(byte[] haystack, byte[] needle) {
        outer: for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

}
