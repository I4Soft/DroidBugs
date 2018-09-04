package com.fsck.k9.mail.store.imap;


import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.AuthenticationFailedException;
import com.fsck.k9.mail.CertificateValidationException;
import com.fsck.k9.mail.CertificateValidationException.Reason;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.K9MailLib;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.ssl.TrustedSocketFactory;
import com.fsck.k9.mail.store.imap.mockserver.MockImapServer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okio.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class ImapConnectionTest {
    private static final boolean DEBUGGING = false;

    private static final String USERNAME = "user";
    private static final String PASSWORD = "123456";
    private static final int SOCKET_CONNECT_TIMEOUT = 2000;
    private static final int SOCKET_READ_TIMEOUT = 1000;


    private TrustedSocketFactory socketFactory;
    private ConnectivityManager connectivityManager;
    private SimpleImapSettings settings;


    @Before
    public void setUp() throws Exception {
        connectivityManager = mock(ConnectivityManager.class);
        socketFactory = new TestTrustedSocketFactory();

        settings = new SimpleImapSettings();
        settings.setUsername(USERNAME);
        settings.setPassword(PASSWORD);

        if (DEBUGGING) {
            ShadowLog.stream = System.out;
            K9MailLib.setDebug(true);
            K9MailLib.setDebugSensitive(true);
        }
    }

    @Test
    public void open_withCapabilitiesInInitialResponse_shouldNotIssueCapabilitiesCommand() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        server.output("* OK [CAPABILITY IMAP4 IMAP4REV1 AUTH=PLAIN]");
        server.expect("1 AUTHENTICATE PLAIN");
        server.output("+");
        server.expect(ByteString.encodeUtf8("\000" + USERNAME+ "\000" + PASSWORD).base64());
        server.output("1 OK Success");
        server.expect("2 LIST \"\" \"\"");
        server.output("* LIST () \"/\" foo/bar");
        server.output("2 OK");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authPlain() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=PLAIN");
        server.expect("2 AUTHENTICATE PLAIN");
        server.output("+");
        server.expect(ByteString.encodeUtf8("\000" + USERNAME+ "\000" + PASSWORD).base64());
        server.output("2 OK Success");
        simplePostAuthenticationDialog(server);
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authPlainWithLoginDisabled_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "LOGINDISABLED");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Server doesn't support unencrypted passwords using AUTH=PLAIN and LOGIN is disabled.",
                    e.getMessage());
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authPlainWithAuthenticationFailure_shouldFallbackToLogin() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=PLAIN");
        server.expect("2 AUTHENTICATE PLAIN");
        server.output("+");
        server.expect(ByteString.encodeUtf8("\000" + USERNAME+ "\000" + PASSWORD).base64());
        server.output("2 NO Login Failure");
        server.expect("3 LOGIN \"" + USERNAME + "\" \"" + PASSWORD + "\"");
        server.output("3 OK LOGIN completed");
        simplePostAuthenticationDialog(server, "4");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authPlainAndLoginFallbackWithAuthenticationFailure_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=PLAIN");
        server.expect("2 AUTHENTICATE PLAIN");
        server.output("+");
        server.expect(ByteString.encodeUtf8("\000" + USERNAME+ "\000" + PASSWORD).base64());
        server.output("2 NO Login Failure");
        server.expect("3 LOGIN \"" + USERNAME + "\" \"" + PASSWORD + "\"");
        server.output("3 NO Go away");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            //FIXME: improve exception message
            assertThat(e.getMessage(), containsString("response: #3# [NO, Go away]"));
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authPlainWithoutAuthPlainCapability_shouldUseLoginMethod() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server);
        server.expect("2 LOGIN \"" + USERNAME + "\" \"" + PASSWORD + "\"");
        server.output("2 OK LOGIN completed");
        simplePostAuthenticationDialog(server);
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authCramMd5() throws Exception {
        settings.setAuthType(AuthType.CRAM_MD5);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=CRAM-MD5");
        server.expect("2 AUTHENTICATE CRAM-MD5");
        server.output("+ " + ByteString.encodeUtf8("<0000.000000000@example.org>").base64());
        server.expect("dXNlciA2ZjdiOTcyYjk5YTI4NDk4OTRhN2YyMmE3MGRhZDg0OQ==");
        server.output("2 OK Success");
        simplePostAuthenticationDialog(server);
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authCramMd5WithAuthenticationFailure_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.CRAM_MD5);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=CRAM-MD5");
        server.expect("2 AUTHENTICATE CRAM-MD5");
        server.output("+ " + ByteString.encodeUtf8("<0000.000000000@example.org>").base64());
        server.expect("dXNlciA2ZjdiOTcyYjk5YTI4NDk4OTRhN2YyMmE3MGRhZDg0OQ==");
        server.output("2 NO Who are you?");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (AuthenticationFailedException e) {
            //FIXME: improve exception message
            assertThat(e.getMessage(), containsString("response: #2# [NO, Who are you?]"));
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authCramMd5WithoutAuthCramMd5Capability_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.CRAM_MD5);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=PLAIN");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (MessagingException e) {
            assertEquals("Server doesn't support encrypted passwords using CRAM-MD5.", e.getMessage());
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authExternal() throws Exception {
        settings.setAuthType(AuthType.EXTERNAL);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=EXTERNAL");
        server.expect("2 AUTHENTICATE EXTERNAL " + ByteString.encodeUtf8(USERNAME).base64());
        server.output("2 OK Success");
        simplePostAuthenticationDialog(server);
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authExternalWithAuthenticationFailure_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.EXTERNAL);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=EXTERNAL");
        server.expect("2 AUTHENTICATE EXTERNAL " + ByteString.encodeUtf8(USERNAME).base64());
        server.output("2 NO");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (CertificateValidationException e) {
            //FIXME: improve exception message
            assertThat(e.getMessage(), containsString("response: #2# [NO]"));
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_authExternalWithoutAuthExternalCapability_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.EXTERNAL);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "AUTH=PLAIN");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (CertificateValidationException e) {
            assertEquals(Reason.MissingCapability, e.getReason());
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withNamespaceCapability_shouldIssueNamespaceCommand() throws Exception {
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "NAMESPACE");
        server.expect("3 NAMESPACE");
        server.output("* NAMESPACE ((\"\" \"/\")) NIL NIL");
        server.output("3 OK command completed");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withConnectionError_shouldThrow() throws Exception {
        settings.setHost("127.1.2.3");
        settings.setPort(143);
        ImapConnection imapConnection = createImapConnection(settings, socketFactory, connectivityManager);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (MessagingException e) {
            //FIXME: Throw ConnectException
            assertEquals("Cannot connect to host", e.getMessage());
            assertNotNull(e.getCause());
            assertEquals(ConnectException.class, e.getCause().getClass());
        }
    }

    @Test
    public void open_withInvalidHostname_shouldThrow() throws Exception {
        settings.setHost("host name");
        settings.setPort(143);
        ImapConnection imapConnection = createImapConnection(settings, socketFactory, connectivityManager);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (UnknownHostException ignored) {
        }

        assertFalse(imapConnection.isOpen());
    }

    @Test
    public void open_withStartTlsCapability_shouldIssueStartTlsCommand() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setConnectionSecurity(ConnectionSecurity.STARTTLS_REQUIRED);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "STARTTLS LOGINDISABLED");
        server.expect("2 STARTTLS");
        server.output("2 OK [CAPABILITY IMAP4REV1 NAMESPACE]");
        server.startTls();
        server.expect("3 CAPABILITY");
        server.output("* CAPABILITY IMAP4 IMAP4REV1 NAMESPACE");
        server.output("3 OK");
        server.expect("4 LOGIN \"" + USERNAME + "\" \"" + PASSWORD + "\"");
        server.output("4 OK LOGIN completed");
        server.expect("5 NAMESPACE");
        server.output("* NAMESPACE ((\"\" \"/\")) NIL NIL");
        server.output("5 OK command completed");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withStartTlsButWithoutStartTlsCapability_shouldThrow() throws Exception {
        settings.setConnectionSecurity(ConnectionSecurity.STARTTLS_REQUIRED);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server);
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (CertificateValidationException e) {
            //FIXME: CertificateValidationException seems wrong
            assertEquals("STARTTLS connection security not available", e.getMessage());
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withNegativeResponseToStartTlsCommand_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setConnectionSecurity(ConnectionSecurity.STARTTLS_REQUIRED);
        MockImapServer server = new MockImapServer();
        preAuthenticationDialog(server, "STARTTLS");
        server.expect("2 STARTTLS");
        server.output("2 NO");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Expected exception");
        } catch (NegativeImapResponseException e) {
            assertThat(e.getMessage(), containsString("response: #2# [NO]"));
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withCompressDeflateCapability_shouldEnableCompression() throws Exception {
        settings.setUseCompression(true);
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "COMPRESS=DEFLATE");
        server.expect("3 COMPRESS DEFLATE");
        server.output("3 OK");
        server.enableCompression();
        simplePostAuthenticationDialog(server, "4");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withNegativeResponseToCompressionCommand_shouldContinue() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setUseCompression(true);
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "COMPRESS=DEFLATE");
        server.expect("3 COMPRESS DEFLATE");
        server.output("3 NO");
        simplePostAuthenticationDialog(server, "4");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withIoExceptionDuringCompressionCommand_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setUseCompression(true);
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "COMPRESS=DEFLATE");
        server.expect("3 COMPRESS DEFLATE");
        server.closeConnection();
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Exception expected");
        } catch (IOException ignored) {
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withIoExceptionDuringListCommand_shouldThrow() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setUseCompression(true);
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "");
        server.expect("3 LIST \"\" \"\"");
        server.output("* Now what?");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        try {
            imapConnection.open();
            fail("Exception expected");
        } catch (IOException ignored) {
        }

        server.verifyConnectionClosed();
        server.verifyInteractionCompleted();
    }

    @Test
    public void open_withNegativeResponseToListCommand() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        settings.setUseCompression(true);
        MockImapServer server = new MockImapServer();
        simplePreAuthAndLoginDialog(server, "");
        server.expect("3 LIST \"\" \"\"");
        server.output("3 NO");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);

        imapConnection.open();

        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    @Test
    public void isOpen_withoutPreviousOpen_shouldReturnFalse() throws Exception {
        ImapConnection imapConnection = createImapConnection(settings, socketFactory, connectivityManager);

        boolean result = imapConnection.isOpen();

        assertFalse(result);
    }

    @Test
    public void isOpen_afterOpen_shouldReturnTrue() throws Exception {
        MockImapServer server = new MockImapServer();
        ImapConnection imapConnection = simpleOpen(server);

        boolean result = imapConnection.isOpen();

        assertTrue(result);
        server.verifyConnectionStillOpen();

        server.shutdown();
    }

    @Test
    public void isOpen_afterOpenAndClose_shouldReturnFalse() throws Exception {
        MockImapServer server = new MockImapServer();
        ImapConnection imapConnection = simpleOpen(server);
        imapConnection.close();

        boolean result = imapConnection.isOpen();

        assertFalse(result);
        server.verifyConnectionClosed();

        server.shutdown();
    }

    @Test
    public void close_withoutOpen_shouldNotThrow() throws Exception {
        ImapConnection imapConnection = createImapConnection(settings, socketFactory, connectivityManager);

        imapConnection.close();
    }

    @Test
    public void close_afterOpen_shouldCloseConnection() throws Exception {
        MockImapServer server = new MockImapServer();
        ImapConnection imapConnection = simpleOpen(server);

        imapConnection.close();

        server.verifyConnectionClosed();

        server.shutdown();
    }

    @Test
    public void isIdleCapable_withoutIdleCapability() throws Exception {
        MockImapServer server = new MockImapServer();
        ImapConnection imapConnection = simpleOpen(server);

        boolean result = imapConnection.isIdleCapable();

        assertFalse(result);

        server.shutdown();
    }

    @Test
    public void isIdleCapable_withIdleCapability() throws Exception {
        MockImapServer server = new MockImapServer();
        ImapConnection imapConnection = simpleOpenWithCapabilities(server, "IDLE");

        boolean result = imapConnection.isIdleCapable();

        assertTrue(result);

        server.shutdown();
    }

    @Test
    public void sendContinuation() throws Exception {
        settings.setAuthType(AuthType.PLAIN);
        MockImapServer server = new MockImapServer();
        simpleOpenDialog(server, "IDLE");
        server.expect("4 IDLE");
        server.output("+ idling");
        server.expect("DONE");
        ImapConnection imapConnection = startServerAndCreateImapConnection(server);
        imapConnection.open();
        imapConnection.sendCommand("IDLE", false);
        imapConnection.readResponse();

        imapConnection.sendContinuation("DONE");

        server.waitForInteractionToComplete();
        server.verifyConnectionStillOpen();
        server.verifyInteractionCompleted();
    }

    private ImapConnection createImapConnection(ImapSettings settings, TrustedSocketFactory socketFactory,
            ConnectivityManager connectivityManager) {
        return new ImapConnection(settings, socketFactory, connectivityManager, SOCKET_CONNECT_TIMEOUT,
                SOCKET_READ_TIMEOUT);
    }

    private ImapConnection startServerAndCreateImapConnection(MockImapServer server) throws IOException {
        server.start();
        settings.setHost(server.getHost());
        settings.setPort(server.getPort());
        return createImapConnection(settings, socketFactory, connectivityManager);
    }

    private ImapConnection simpleOpen(MockImapServer server) throws Exception {
        return simpleOpenWithCapabilities(server, "");
    }

    private ImapConnection simpleOpenWithCapabilities(MockImapServer server, String capabilities) throws Exception {
        simpleOpenDialog(server, capabilities);

        ImapConnection imapConnection = startServerAndCreateImapConnection(server);
        imapConnection.open();

        return imapConnection;
    }

    private void preAuthenticationDialog(MockImapServer server) {
        preAuthenticationDialog(server, "");
    }

    private void preAuthenticationDialog(MockImapServer server, String capabilities) {
        server.output("* OK IMAP4rev1 Service Ready");
        server.expect("1 CAPABILITY");
        server.output("* CAPABILITY IMAP4 IMAP4REV1 " + capabilities);
        server.output("1 OK CAPABILITY");
    }

    private void simplePostAuthenticationDialog(MockImapServer server) {
        simplePostAuthenticationDialog(server, "3");
    }

    private void simplePostAuthenticationDialog(MockImapServer server, String tag) {
        server.expect(tag + " LIST \"\" \"\"");
        server.output("* LIST () \"/\" foo/bar");
        server.output(tag + " OK");
    }

    private void simpleOpenDialog(MockImapServer server, String capabilities) {
        simplePreAuthAndLoginDialog(server, capabilities);
        simplePostAuthenticationDialog(server);
    }

    private void simplePreAuthAndLoginDialog(MockImapServer server, String capabilities) {
        settings.setAuthType(AuthType.PLAIN);

        preAuthenticationDialog(server, capabilities);

        server.expect("2 LOGIN \"" + USERNAME + "\" \"" + PASSWORD + "\"");
        server.output("2 OK LOGIN completed");
    }

    private static class TestTrustedSocketFactory implements TrustedSocketFactory {
        @Override
        public Socket createSocket(Socket socket, String host, int port, String clientCertificateAlias)
                throws NoSuchAlgorithmException, KeyManagementException, MessagingException, IOException {

            TrustManager[] trustManagers = new TrustManager[] { new VeryTrustingTrustManager() };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory.createSocket(
                    socket,
                    socket.getInetAddress().getHostAddress(),
                    socket.getPort(),
                    true);
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    private static class VeryTrustingTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // Accept all certificates
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // Accept all certificates
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
