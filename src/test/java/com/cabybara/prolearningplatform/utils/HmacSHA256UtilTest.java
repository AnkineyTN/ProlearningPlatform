package com.cabybara.prolearningplatform.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HmacSHA256UtilTest {

    @Test
    void signReturnsStableHashForSameInput() {
        String signature = HmacSHA256Util.sign("payload", "secret");

        assertEquals("b82fcb791acec57859b989b430a826488ce2e479fdf92326bd0a2e8375a42ba4", signature);
    }

    @Test
    void signReturnsDifferentHashWhenKeyChanges() {
        String firstSignature = HmacSHA256Util.sign("payload", "secret-1");
        String secondSignature = HmacSHA256Util.sign("payload", "secret-2");

        assertNotEquals(firstSignature, secondSignature);
    }
}
