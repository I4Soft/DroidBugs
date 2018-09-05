package org.wikipedia.feed.news;

import com.google.common.reflect.TypeToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.feed.model.FeedPageSummary;
import org.wikipedia.json.GsonUtil;
import org.wikipedia.test.TestFileUtil;
import org.wikipedia.test.TestRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RunWith(TestRunner.class)
public class NewsLinkCardTest {
    private static WikiSite TEST = WikiSite.forLanguageCode("test");
    private List<NewsItem> content;

    @Before public void setUp() throws Throwable {
        String json = TestFileUtil.readRawFile("news_2016_11_07.json");
        TypeToken<List<NewsItem>> typeToken = new TypeToken<List<NewsItem>>(){};
        content = GsonUtil.getDefaultGson().fromJson(json, typeToken.getType());
    }

    @Test public void testTitleNormalization() throws Throwable {
        List<NewsItemCard> newsItemCards = NewsListCard.toItemCards(content, TEST);
        for (NewsItemCard newsItemCard : newsItemCards) {
            for (FeedPageSummary link : newsItemCard.links()) {
                assertThat(new NewsLinkCard(link, TEST).title(), not(containsString("_")));
            }
        }
    }
}
