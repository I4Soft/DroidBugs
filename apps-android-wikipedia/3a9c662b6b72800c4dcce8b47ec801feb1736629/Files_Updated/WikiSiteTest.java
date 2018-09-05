package org.wikipedia.dataclient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.wikipedia.json.GsonMarshaller;
import org.wikipedia.json.GsonUnmarshaller;
import org.wikipedia.page.PageTitle;
import org.wikipedia.test.TestParcelUtil;
import org.wikipedia.test.TestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(TestRunner.class) public class WikiSiteTest {
    @Test public void testSupportedAuthority() {
        assertThat(WikiSite.supportedAuthority("fr.wikipedia.org"), is(true));
        assertThat(WikiSite.supportedAuthority("fr.m.wikipedia.org"), is(true));
        assertThat(WikiSite.supportedAuthority("roa-rup.wikipedia.org"), is(true));

        assertThat(WikiSite.supportedAuthority("google.com"), is(false));
    }

    @Test public void testForLanguageCodeScheme() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.scheme(), is("https"));
    }

    @Test public void testForLanguageCodeAuthority() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.authority(), is("test.wikipedia.org"));
    }

    @Test public void testForLanguageCodeLanguage() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.languageCode(), is("test"));
    }

    @Test public void testForLanguageCodeNoLanguage() {
        WikiSite subject = WikiSite.forLanguageCode("");
        assertThat(subject.languageCode(), is(""));
    }

    @Test public void testForLanguageCodeNoLanguageAuthority() {
        WikiSite subject = WikiSite.forLanguageCode("");
        assertThat(subject.authority(), is("wikipedia.org"));
    }

    @Test public void testForLanguageCodeLanguageAuthority() {
        WikiSite subject = WikiSite.forLanguageCode("zh-hans");
        assertThat(subject.authority(), is("zh.wikipedia.org"));
    }

    @Test public void testCtorScheme() {
        WikiSite subject = new WikiSite(false, "simple.wikipedia.beta.wmflabs.org", "simple");
        assertThat(subject.secureScheme(), is(false));
    }

    @Test public void testCtorNoScheme() {
        WikiSite subject = new WikiSite("wikipedia.org");
        assertThat(subject.secureScheme(), is(true));
    }

    @Test public void testCtorAuthority() {
        WikiSite subject = new WikiSite("test.wikipedia.org");
        assertThat(subject.authority(), is("test.wikipedia.org"));
    }

    @Test public void testCtorAuthorityLanguage() {
        WikiSite subject = new WikiSite("test.wikipedia.org");
        assertThat(subject.languageCode(), is("test"));
    }

    @Test public void testCtorAuthorityNoLanguage() {
        WikiSite subject = new WikiSite("wikipedia.org");
        assertThat(subject.languageCode(), is(""));
    }

    @Test public void testCtorMobileAuthorityLanguage() {
        WikiSite subject = new WikiSite("test.m.wikipedia.org");
        assertThat(subject.languageCode(), is("test"));
    }

    @Test public void testCtorMobileAuthorityNoLanguage() {
        WikiSite subject = new WikiSite("m.wikipedia.org");
        assertThat(subject.languageCode(), is(""));
    }

    @Test public void testCtorParcel() throws Throwable {
        WikiSite subject = WikiSite.forLanguageCode("test");
        TestParcelUtil.test(subject);
    }

    @Test public void testSecureSchemeHttp() {
        WikiSite subject = new WikiSite(false, "192.168.1.11:8080", "");
        assertThat(subject.secureScheme(), is(false));
    }

    @Test public void testSecureSchemeHttps() {
        WikiSite subject = new WikiSite(true, "192.168.1.11:8080", "");
        assertThat(subject.secureScheme(), is(true));
    }

    @Test public void testSchemeHttp() {
        WikiSite subject = new WikiSite(false, "meta.wikimedia.org", "");
        assertThat(subject.scheme(), is("http"));
    }

    @Test public void testSchemeHttps() {
        WikiSite subject = new WikiSite(true, "meta.wikimedia.org", "");
        assertThat(subject.scheme(), is("https"));
    }

    @Test public void testAuthority() {
        WikiSite subject = new WikiSite(true, "test.wikipedia.org", "test");
        assertThat(subject.authority(), is("test.wikipedia.org"));
    }

    @Test public void testMobileAuthorityLanguage() {
        WikiSite subject = WikiSite.forLanguageCode("fiu-vro");
        assertThat(subject.mobileAuthority(), is("fiu-vro.m.wikipedia.org"));
    }

    @Test public void testMobileAuthorityNoLanguage() {
        WikiSite subject = new WikiSite("wikipedia.org");
        assertThat(subject.mobileAuthority(), is("m.wikipedia.org"));
    }

    @Test public void testMobileAuthorityLanguageAuthority() {
        WikiSite subject = new WikiSite("no.wikipedia.org", "nb");
        assertThat(subject.mobileAuthority(), is("no.m.wikipedia.org"));
    }

    @Test public void testMobileAuthorityMobileAuthority() {
        WikiSite subject = new WikiSite("ru.m.wikipedia.org");
        assertThat(subject.mobileAuthority(), is("ru.m.wikipedia.org"));
    }

    @Test public void testMobileAuthorityMobileAuthorityNoLanguage() {
        WikiSite subject = new WikiSite("m.wikipedia.org");
        assertThat(subject.mobileAuthority(), is("m.wikipedia.org"));
    }

    @Test public void testHost() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.host(), is("test.wikipedia.org"));
    }

    @Test public void testMobileHost() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.mobileHost(), is("test.m.wikipedia.org"));
    }

    @Test public void testMobileHostNoLanguage() {
        WikiSite subject = WikiSite.forLanguageCode("");
        assertThat(subject.mobileHost(), is("m.wikipedia.org"));
    }

    @Test public void testPort() {
        final int port = 8080;
        WikiSite subject = new WikiSite("192.168.1.11:" + port);
        assertThat(subject.port(), is(port));
    }

    @Test public void testPortDefault() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.port(), is(-1));
    }

    @Test public void testPath() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.path("Segment"), is("/w/Segment"));
    }

    @Test public void testPathEmpty() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.path(""), is("/w/"));
    }

    @Test public void testUrl() {
        WikiSite subject = new WikiSite(false, "test.192.168.1.11:8080", "test");
        assertThat(subject.url(), is("http://test.192.168.1.11:8080"));
    }

    @Test public void testUrlPath() {
        WikiSite subject = WikiSite.forLanguageCode("test");
        assertThat(subject.url("Segment"), is("https://test.wikipedia.org/w/Segment"));
    }

    @Test public void testLanguageCode() {
        WikiSite subject = WikiSite.forLanguageCode("lang");
        assertThat(subject.languageCode(), is("lang"));
    }

    @Test public void testUnmarshal() {
        WikiSite wiki = WikiSite.forLanguageCode("test");
        assertThat(GsonUnmarshaller.unmarshal(WikiSite.class, GsonMarshaller.marshal(wiki)), is(wiki));
    }

    @Test public void testUnmarshalScheme() {
        WikiSite wiki = new WikiSite(false, "wikipedia.org", "");
        assertThat(GsonUnmarshaller.unmarshal(WikiSite.class, GsonMarshaller.marshal(wiki)), is(wiki));
    }

    @Test public void testTitleForInternalLink() {
        WikiSite wiki = WikiSite.forLanguageCode("en");
        assertThat(new PageTitle("Main Page", wiki), is(wiki.titleForInternalLink("")));
        assertThat(new PageTitle("Main Page", wiki), is(wiki.titleForInternalLink("/wiki/")));
        assertThat(new PageTitle("wiki", wiki), is(wiki.titleForInternalLink("wiki")));
        assertThat(new PageTitle("wiki", wiki), is(wiki.titleForInternalLink("/wiki/wiki")));
        assertThat(new PageTitle("wiki/wiki", wiki), is(wiki.titleForInternalLink("/wiki/wiki/wiki")));
    }

    @Test public void testEquals() {
        assertThat(WikiSite.forLanguageCode("en"), is(WikiSite.forLanguageCode("en")));

        assertThat(WikiSite.forLanguageCode("ta"), not(WikiSite.forLanguageCode("en")));
        assertThat(WikiSite.forLanguageCode("ta").equals("ta.wikipedia.org"), is(false));
    }

    @Test public void testNormalization() {
        assertThat("bm.wikipedia.org", is(WikiSite.forLanguageCode("bm").authority()));
    }
}
