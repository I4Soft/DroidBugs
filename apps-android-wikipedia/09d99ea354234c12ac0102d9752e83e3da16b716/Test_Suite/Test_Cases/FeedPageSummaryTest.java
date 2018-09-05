package org.wikipedia.feed.model;

import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wikipedia.feed.mostread.MostReadArticlesTest;
import org.wikipedia.test.TestRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@RunWith(TestRunner.class)
@SuppressWarnings("checkstyle:magicnumber")
public class FeedPageSummaryTest {
    private List<FeedPageSummary> subjects;

    @Before public void setUp() throws Throwable {
        subjects = MostReadArticlesTest.unmarshal("most_read.json").articles();
    }

    @Test public void testUnmarshalThumbnails() throws Throwable {
        FeedPageSummary subject = subjects.get(3);

        assertThat(subject.getNormalizedTitle(), is("Marilyn Monroe"));
        assertThat(subject.getTitle(), is("Marilyn_Monroe"));
        assertThat(subject.getDescription(), is("American actress, model, and singer"));

        Uri thumbnail = Uri.parse("https://upload.wikimedia.org/wikipedia/commons/thumb/0/0a/Marilyn_Monroe_in_1952.jpg/229px-Marilyn_Monroe_in_1952.jpg");
        assertThat(Uri.parse(subject.getThumbnailUrl()), is(thumbnail));
    }

    @Test public void testUnmarshalNoThumbnails() {
        FeedPageSummary subject = subjects.get(0);

        assertThat(subject.getNormalizedTitle(), is("Bicycle Race"));
        assertThat(subject.getTitle(), is("Bicycle_Race"));
        assertThat(subject.getDescription(), is("rock song by Queen"));
        assertThat(subject.getThumbnailUrl(), nullValue());
    }
}