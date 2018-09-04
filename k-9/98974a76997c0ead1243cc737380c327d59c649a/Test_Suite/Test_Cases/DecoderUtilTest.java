package com.fsck.k9.mail.internet;


import com.fsck.k9.mail.K9LibRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;


@RunWith(K9LibRobolectricTestRunner.class)
public class DecoderUtilTest {

    @Test
    public void testDecodeEncodedWords() {
        String body, expect;
        MimeMessage message;

        body = "abc";
        expect = "abc";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?us-ascii?q?abc?=";
        expect = "abc";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?";
        expect = "=?";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=??";
        expect = "=??";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=???";
        expect = "=???";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=????";
        expect = "=????";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=????=";
        expect = "=????=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=??q??=";
        expect = "=??q??=";
        ;
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=??q?a?=";
        expect = "a";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=??=";
        expect = "=??=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x?=";
        expect = "=?x?=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x??=";
        expect = "=?x??=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x?q?=";
        expect = "=?x?q?=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x?q??=";
        expect = "=?x?q??=";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x?q?X?=";
        expect = "X";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        // invalid base64 string
        body = "=?us-ascii?b?abc?=";
        expect = "";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        // broken encoded header
        body = "=?us-ascii?q?abc?= =?";
        expect = "abc =?";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));

        body = "=?x?= =?";
        expect = "=?x?= =?";
        message = null;
        assertEquals(expect, DecoderUtil.decodeEncodedWords(body, message));
    }
}
