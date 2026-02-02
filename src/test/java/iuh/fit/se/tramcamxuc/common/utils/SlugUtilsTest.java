package iuh.fit.se.tramcamxuc.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SlugUtilsTest {

    @Test
    @DisplayName("Should convert normal string to slug")
    void testToSlug_WithNormalString() {
        String result = SlugUtils.toSlug("Hello World");
        assertEquals("hello-world", result);
    }

    @Test
    @DisplayName("Should convert Vietnamese characters to slug")
    void testToSlug_WithVietnameseCharacters() {
        String result = SlugUtils.toSlug("Trạm Cảm Xúc");
        // toSlug uses NFD normalization which keeps the base characters
        assertEquals("tram-cam-xuc", result);
    }

    @Test
    @DisplayName("Should remove special characters")
    void testToSlug_WithSpecialCharacters() {
        String result = SlugUtils.toSlug("Hello@World#123");
        assertEquals("helloworld123", result);
    }

    @Test
    @DisplayName("Should replace multiple spaces with hyphens")
    void testToSlug_WithMultipleSpaces() {
        String result = SlugUtils.toSlug("Hello   World   Test");
        assertEquals("hello---world---test", result);
    }

    @Test
    @DisplayName("Should handle null input")
    void testToSlug_WithNullInput() {
        String result = SlugUtils.toSlug(null);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle empty string")
    void testToSlug_WithEmptyString() {
        String result = SlugUtils.toSlug("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should generate slug with suffix")
    void testGenerateSlug_ReturnsSlugWithSuffix() {
        String result = SlugUtils.generateSlug("Test Title");
        assertTrue(result.startsWith("test-title-"));
        assertTrue(result.length() > "test-title-".length());
    }

    @Test
    @DisplayName("Should generate unique slug with Vietnamese title")
    void testGenerateSlug_WithVietnameseTitle() {
        String result = SlugUtils.generateSlug("Bài Hát Hay");
        assertTrue(result.startsWith("bai-hat-hay-"));
        assertTrue(result.length() > 12);
    }

    @Test
    @DisplayName("Should generate unique slugs for same input")
    void testGenerateSlug_UniqueForSameInput() {
        String result1 = SlugUtils.generateSlug("Same Title");
        String result2 = SlugUtils.generateSlug("Same Title");
        assertNotEquals(result1, result2, "Generated slugs should be unique");
    }

    @Test
    @DisplayName("Should generate slug with 6-character suffix")
    void testGenerateSlug_SuffixLength() {
        String result = SlugUtils.generateSlug("Test");
        String[] parts = result.split("-");
        String suffix = parts[parts.length - 1];
        assertEquals(6, suffix.length(), "Suffix should be 6 characters long");
    }
}
