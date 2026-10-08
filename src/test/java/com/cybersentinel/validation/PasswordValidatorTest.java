package com.cybersentinel.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void nullIsInvalid() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void tooShortIsInvalid() {
        assertFalse(validator.isValid("1234567", null));
    }

    @Test
    void exactly8CharactersIsValid() {
        assertTrue(validator.isValid("12345678", null));
    }

    @Test
    void exactly72BytesIsValid() {
        // 72 'a's = 72 bytes
        String valid = "a".repeat(72);
        assertTrue(validator.isValid(valid, null));
    }

    @Test
    void moreThan72BytesIsInvalid() {
        // 73 'a's = 73 bytes
        String invalid = "a".repeat(73);
        assertFalse(validator.isValid(invalid, null));
    }

    @Test
    void emojisCountAsMultipleBytes() {
        // '😀' is 4 bytes. 18 * 4 = 72 bytes. Wait, is string length < 8?
        // '😀' string length in Java is 2 chars. 18 * 2 = 36 chars.
        // So 18 emojis is exactly 72 bytes and 36 chars (> 8 chars).
        String emojis18 = "😀".repeat(18);
        assertTrue(validator.isValid(emojis18, null));
        
        // 19 emojis = 76 bytes -> should be invalid.
        String emojis19 = "😀".repeat(19);
        assertFalse(validator.isValid(emojis19, null));
    }
}
