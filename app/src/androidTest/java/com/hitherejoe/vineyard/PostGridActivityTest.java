package com.hitherejoe.vineyard;


import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import rx.Observable;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hitherejoe.vineyard.util.CustomMatchers.withItemText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PostGridActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<PostGridActivity> main =
            new ActivityTestRule<>(PostGridActivity.class, false, false);

    @Rule
    public final TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void listOfUserPostsShowsAndIsScrollable() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(postList);
        stubPostGridData(postList);

        Context context = InstrumentationRegistry.getTargetContext();
        User mockUser = TestDataFactory.createMockUser();
        mockUser.username = "123";

        Intent intent = PostGridActivity.getStartIntent(context, mockUser);
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));

        checkPostsDisplayOnRecyclerView(postList, 0);
    }

    @Test
    public void listOfTagPostsShowsAndIsScrollable() {
        List<Post> tagList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(tagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));

        checkPostsDisplayOnRecyclerView(tagList, 0);
    }

    @Test
    public void listOfUserPostsShowsAndIsScrollableWithPagination() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(postList);

        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = postList;
        postResponse.data = data;
        data.nextPage = 2;
        data.anchorStr = "anchor_string";

        when(component.getMockDataManager().getPostsByUser(anyString(), eq("1"), anyString()))
                .thenReturn(Observable.just(postResponse));

        List<Post> postListTwo = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(postListTwo);

        VineyardService.PostResponse postResponseTwo = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data dataTwo = new VineyardService.PostResponse.Data();
        dataTwo.records = postListTwo;
        postResponseTwo.data = dataTwo;
        dataTwo.nextPage = 0;
        dataTwo.anchorStr = "anchor_string";

        when(component.getMockDataManager().getPostsByUser(anyString(), eq("2"), anyString()))
                .thenReturn(Observable.just(postResponseTwo));

        when(component.getMockDataManager().getPostsByUser(anyString(), eq("0"), anyString()))
                .thenReturn(Observable.<VineyardService.PostResponse>empty());

        Context context = InstrumentationRegistry.getTargetContext();
        User mockUser = TestDataFactory.createMockUser();
        mockUser.username = "123";
        Intent intent = PostGridActivity.getStartIntent(context, mockUser);
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));

        checkPostsDisplayOnRecyclerView(postList, 0);

        checkPostsDisplayOnRecyclerView(postListTwo, postList.size());
    }

    @Test
    public void tryAgainCardIsDisplayed() {
        doReturn(Observable.just(new RuntimeException()))
                .when(component.getMockDataManager())
                .getPostsByTag(anyString(), anyString(), anyString());

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));
        onView(withItemText("Oops", R.id.browse_grid)).check(matches(isDisplayed()));
        onView(withItemText("Try again?", R.id.browse_grid)).check(matches(isDisplayed()));
    }

    @Test
    public void tryAgainCardFetchesContentWhenClicked() {
        doReturn(Observable.just(new RuntimeException()))
                .when(component.getMockDataManager())
                .getPostsByTag(anyString(), anyString(), anyString());

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));
        onView(withItemText("Oops", R.id.browse_grid)).check(matches(isDisplayed()));
        onView(withItemText("Try again?", R.id.browse_grid)).check(matches(isDisplayed()));

        List<Post> tagList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(tagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;
        when(component.getMockDataManager().getPostsByTag(eq("cat"), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        onView(withId(R.id.browse_grid))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        onView(withItemText("Oops", R.id.browse_grid)).check(doesNotExist());
        onView(withItemText("Try again?", R.id.browse_grid)).check(doesNotExist());

        checkPostsDisplayOnRecyclerView(tagList, 0);
    }

    @Test
    public void tryAgainCardFetchesContentOnFocus() {
        List<Post> tagList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(tagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;
        postTagResponse.data.nextPage = 2;

        when(component.getMockDataManager().getPostsByTag(eq("cat"), eq("1"), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));

        doReturn(Observable.just(new RuntimeException()))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("cat"), eq("2"), anyString());

        checkPostsDisplayOnRecyclerView(tagList, 0);

        String errorMessage =
                InstrumentationRegistry.getTargetContext()
                        .getString(R.string.error_message_loading_more_posts);

        onView(withText(errorMessage))
                .inRoot(withDecorView(not(is(main.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        onView(withId(R.id.browse_grid))
                .perform(RecyclerViewActions.actionOnItemAtPosition(tagList.size() - 4, click()));

        List<Post> secondTagList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(secondTagList);
        tagData.records = secondTagList;
        postTagResponse.data = tagData;
        postTagResponse.data.nextPage = 0;
        doReturn(Observable.just(postTagResponse))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("cat"), eq("2"), anyString());
        when(component.getMockDataManager().getPostsByUser(anyString(), eq("0"), anyString()))
                .thenReturn(Observable.<VineyardService.PostResponse>empty());

        onView(withId(R.id.browse_grid))
                .perform(RecyclerViewActions.actionOnItemAtPosition(secondTagList.size() - 5, click()));

        checkPostsDisplayOnRecyclerView(secondTagList, tagList.size());
    }

    @Test
    public void reloadCardIsDisplayed() {
        List<Post> tagList = TestDataFactory.createMockListOfPosts(0);
        Collections.sort(tagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));

        onView(withItemText("No videos", R.id.browse_grid)).check(matches(isDisplayed()));
        onView(withItemText("Check again?", R.id.browse_grid)).check(matches(isDisplayed()));
    }

    @Test
    public void reloadCardReloadsContentWhenClicked() {
        List<Post> emptyTagList = TestDataFactory.createMockListOfPosts(0);
        Collections.sort(emptyTagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = emptyTagList;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(eq("cat"), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        Context context = InstrumentationRegistry.getTargetContext();
        Tag mockTag = TestDataFactory.createMockTag("cat");
        Intent intent = PostGridActivity.getStartIntent(context, mockTag);
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));

        onView(withItemText("No videos", R.id.browse_grid)).check(matches(isDisplayed()));
        onView(withItemText("Check again?", R.id.browse_grid)).check(matches(isDisplayed()));

        List<Post> tagList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(tagList);
        tagData.records = tagList;
        postTagResponse.data = tagData;
        when(component.getMockDataManager().getPostsByTag(eq("cat"), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        onView(withItemText("Check again?", R.id.browse_grid)).perform(click());

        onView(withItemText("No videos", R.id.browse_grid)).check(doesNotExist());
        onView(withItemText("Check again?", R.id.browse_grid)).check(doesNotExist());

        checkPostsDisplayOnRecyclerView(tagList, 0);
    }

    @Test
    public void errorFragmentNotDisplayed() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(10);
        Collections.sort(postList);
        stubPostGridData(postList);

        Context context = InstrumentationRegistry.getTargetContext();
        User mockUser = TestDataFactory.createMockUser();
        mockUser.username = "123";
        Intent intent = PostGridActivity.getStartIntent(context, mockUser);
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));

        String errorTitle = context.getString(R.string.text_error_oops_title);
        String errorMessage = context.getString(R.string.text_error_oops_message);
        String dismissText = context.getString(R.string.text_error_dismiss);

        onView(withText(errorTitle))
                .check(doesNotExist());
        onView(withText(errorMessage))
                .check(doesNotExist());
        onView(withText(dismissText))
                .check(doesNotExist());
    }

    @Test
    public void errorFragmentDisplayed() {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, null);
        main.launchActivity(intent);

        String errorTitle = context.getString(R.string.text_error_oops_title);
        String errorMessage = context.getString(R.string.text_error_oops_message);
        String dismissText = context.getString(R.string.text_error_dismiss);

        onView(withText(errorTitle))
                .check(matches(isDisplayed()));
        onView(withText(errorMessage))
                .check(matches(isDisplayed()));
        onView(withText(dismissText))
                .check(matches(isDisplayed()));
    }

    private void stubPostGridData(List<Post> postList) {
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = postList;
        postResponse.data = data;

        when(component.getMockDataManager().getPostsByUser(anyString(), eq("1"), anyString()))
                .thenReturn(Observable.just(postResponse));
    }

    /**
     * This method checks that the given list of posts display within the VerticalGridView. At the time
     * of writing this, RecyclerViewActions is really buggy with grid based recycler views - so this
     * method traverses through the rows of the grid (starting at left-to-right, then right-to-left
     * and vice versa). This isn't ideal, but currently proper testing doesn't seem supported.
     *
     * @param postsToCheck posts to be checked
     */
    private void checkPostsDisplayOnRecyclerView(List<Post> postsToCheck, int position) {
        int columnCount = 5;
        int size = postsToCheck.size() + position;
        int pos = 0;

        for (int i = position; i < size; i++) {
            // The first item starts as selected, clicking on this would open the Playback Activity
            checkItemAtPosition(i, postsToCheck.get(pos));

            // If we get to the end of the current row then we need to go down to the next one
            if (((i + 1) % columnCount) == 0) {
                int nextRowStart = i + columnCount;
                int nextRowEnd = nextRowStart - columnCount + 1;
                for (int n = nextRowStart; n >= nextRowEnd; n--) {
                    checkItemAtPosition(n, postsToCheck.get(n - position));
                }
                // Set i to the start of the row beneath the one we've just checked
                i = i + columnCount;
            }
            pos++;
        }
    }

    private void checkItemAtPosition(int position, Post post) {
        // VerticalGridFragment->BaseGridView->RecyclerView means we can use RecyclerViewActions! :D
        if (position > 0) {
            onView(withId(R.id.browse_grid))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        }
        onView(withItemText(post.description, R.id.browse_grid)).check(matches(isDisplayed()));
        onView(withItemText(post.username, R.id.browse_grid)).check(matches(isDisplayed()));
    }

}