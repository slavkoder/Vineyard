package com.hitherejoe.vineyard;


import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.hitherejoe.vineyard.util.CustomMatchers.withItemText;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<>(MainActivity.class, false, false);

    @Rule
    public final TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void allCategoriesAreDisplayed() {
        stubVideoFeedData();

        main.launchActivity(null);

        List<String> categoryList = getCategoriesArray();
        for (int i = 0; i < categoryList.size(); i++) {
            // We don't need to click on the first item as it's already in focus
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
            onView(withItemText(categoryList.get(i), R.id.browse_headers))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void searchActivityOpens() {
        stubVideoFeedData();

        main.launchActivity(null);
        onView(withId(R.id.title_orb))
                .perform(click());
        onView(withId(R.id.search_fragment))
                .check(matches(isDisplayed()));
    }

    @Test
    public void postsDisplayAndAreBrowseable() throws InterruptedException {
        VineyardService.PostResponse postResponsePopular = createMockPostResponse();
        doReturn(Observable.just(postResponsePopular))
                .when(component.getMockDataManager())
                .getPopularPosts(anyString(), anyString());

        VineyardService.PostResponse postResponseEditors = createMockPostResponse();
        doReturn(Observable.just(postResponseEditors))
                .when(component.getMockDataManager())
                .getEditorsPicksPosts(anyString(), anyString());

        VineyardService.PostResponse postResponseScary = createMockPostResponse();
        doReturn(Observable.just(postResponseScary))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Scary"), anyString(), anyString());

        VineyardService.PostResponse postResponseComedy = createMockPostResponse();
        doReturn(Observable.just(postResponseComedy))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Comedy"), anyString(), anyString());

        VineyardService.PostResponse postResponseAnimals = createMockPostResponse();
        doReturn(Observable.just(postResponseAnimals))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Animals"), anyString(), anyString());

        VineyardService.PostResponse postResponseMusic = createMockPostResponse();
        doReturn(Observable.just(postResponseMusic))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Music"), anyString(), anyString());

        VineyardService.PostResponse postResponseArt = createMockPostResponse();
        doReturn(Observable.just(postResponseArt))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Art"), anyString(), anyString());

        VineyardService.PostResponse postResponseDance = createMockPostResponse();
        doReturn(Observable.just(postResponseDance))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Dance"), anyString(), anyString());

        VineyardService.PostResponse postResponseSports = createMockPostResponse();
        doReturn(Observable.just(postResponseSports))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Sports"), anyString(), anyString());

        VineyardService.PostResponse postResponseOmg = createMockPostResponse();
        doReturn(Observable.just(postResponseOmg))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("OMG"), anyString(), anyString());

        VineyardService.PostResponse postResponseStyle = createMockPostResponse();
        doReturn(Observable.just(postResponseStyle))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Style"), anyString(), anyString());

        VineyardService.PostResponse postResponseFamily = createMockPostResponse();
        doReturn(Observable.just(postResponseFamily))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Family"), anyString(), anyString());

        VineyardService.PostResponse postResponseFood = createMockPostResponse();
        doReturn(Observable.just(postResponseFood))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Food"), anyString(), anyString());

        VineyardService.PostResponse postResponseDiy = createMockPostResponse();
        doReturn(Observable.just(postResponseDiy))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("DIY"), anyString(), anyString());

        VineyardService.PostResponse postResponsePlaces = createMockPostResponse();
        doReturn(Observable.just(postResponsePlaces))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("Places"), anyString(), anyString());

        VineyardService.PostResponse postResponseNews = createMockPostResponse();
        doReturn(Observable.just(postResponseNews))
                .when(component.getMockDataManager())
                .getPostsByTag(eq("News"), anyString(), anyString());

        main.launchActivity(null);

        List<VineyardService.PostResponse> responses = Arrays.asList(
                postResponsePopular,
                postResponseEditors,
                postResponseScary,
                postResponseComedy,
                postResponseAnimals,
                postResponseMusic,
                postResponseArt,
                postResponseDance,
                postResponseSports,
                postResponseOmg,
                postResponseStyle,
                postResponseFamily,
                postResponseFood,
                postResponseDiy,
                postResponsePlaces,
                postResponseNews);

        List<String> categoryList = getCategoriesArray();
        for (int i = 0; i < categoryList.size() - 1; i++) {
            onView(withId(R.id.browse_headers))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            // We don't need to click on the first item as it's already in focus
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
            List<Post> posts = responses.get(i).data.records;
            for (int n = 0; n < posts.size(); n++) {
                checkItemAtPosition(n, posts.get(n));
            }
            pressBack();
            // No developer options to disable animations on the ADT-1, so this sleep is needed
            Thread.sleep(200);
        }
    }

    @Test
    public void testOptionsDisplayAndAreBrowsable() {
        stubVideoFeedData();
        main.launchActivity(null);

        List<String> categoryList = getCategoriesArray();
        for (int i = 0; i < categoryList.size(); i++) {
            // We don't need to click on the first item as it's already in focus
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
        }
        onView(withId(R.id.browse_headers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(categoryList.size() - 1, click()));
        onView(withItemText("Options", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
    }

    @Test
    public void autoLoopGuidedStepOpens() {
        stubVideoFeedData();
        main.launchActivity(null);

        List<String> categoryList = getCategoriesArray();
        for (int i = 0; i < categoryList.size(); i++) {
            // We don't need to click on the first item as it's already in focus
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
        }
        onView(withId(R.id.browse_headers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(categoryList.size() - 1, click()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText(R.string.guided_step_auto_loop_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void autoLoopShowsSetState() {
        stubVideoFeedData();
        Context context = InstrumentationRegistry.getTargetContext();

        when(VineyardApplication.get(context).getComponent().preferencesHelper().getShouldAutoLoop())
                .thenReturn(false);

        main.launchActivity(null);

        List<String> categoryList = getCategoriesArray();
        for (int i = 0; i < categoryList.size(); i++) {
            // We don't need to click on the first item as it's already in focus
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
        }

        onView(withId(R.id.browse_headers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(categoryList.size() - 1, click()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText(R.string.guided_step_auto_loop_disabled_description))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withItemText("Options", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Disabled", R.id.browse_container_dock))
                .check(matches(isDisplayed()))
                .perform(click());

        String enabledDescriptionText =
                InstrumentationRegistry.getTargetContext()
                        .getString(R.string.guided_step_auto_loop_enabled_description);

        onView(withItemText("Enabled", R.id.guidedactions_list))
                .check(matches(isDisplayed()))
                .perform(click());

        when(VineyardApplication.get(context).getComponent().preferencesHelper().getShouldAutoLoop())
                .thenReturn(true);

        onView(withItemText(enabledDescriptionText, R.id.guidedactions_list))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withItemText("Options", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Enabled", R.id.browse_container_dock))
                .check(matches(isDisplayed()))
                .perform(click());

        String disabledDescriptionText =
                InstrumentationRegistry.getTargetContext()
                        .getString(R.string.guided_step_auto_loop_disabled_description);

        closeSoftKeyboard();

        onView(withItemText("Disabled", R.id.guidedactions_list))
                .check(matches(isDisplayed()))
                .perform(click());

        when(VineyardApplication.get(context).getComponent().preferencesHelper().getShouldAutoLoop())
                .thenReturn(false);

        onView(withItemText(disabledDescriptionText, R.id.guidedactions_list))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withItemText("Options", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Auto-loop", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
        onView(withItemText("Disabled", R.id.browse_container_dock))
                .check(matches(isDisplayed()));
    }

    private List<String> getCategoriesArray() {
        String[] categories =
                InstrumentationRegistry.getTargetContext()
                        .getResources().getStringArray(R.array.categories);
        List<String> categoryList = new ArrayList<>();
        categoryList.addAll(Arrays.asList(categories));
        categoryList.add("Options");
        return categoryList;
    }

    private void stubVideoFeedData() {
        List<Post> mockPosts = TestDataFactory.createMockListOfPosts(5);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;

        when(component.getMockDataManager().getPopularPosts(anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        List<Post> mockTagPosts = TestDataFactory.createMockListOfPosts(5);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = mockTagPosts;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        List<Post> mockEditorsPosts = TestDataFactory.createMockListOfPosts(5);
        VineyardService.PostResponse postEditosResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data editorsData = new VineyardService.PostResponse.Data();
        editorsData.records = mockEditorsPosts;
        postEditosResponse.data = editorsData;

        when(component.getMockDataManager().getEditorsPicksPosts(anyString(), anyString()))
                .thenReturn(Observable.just(postEditosResponse));
    }

    private VineyardService.PostResponse createMockPostResponse() {
        List<Post> mockPosts = TestDataFactory.createMockListOfPosts(5);
        Collections.sort(mockPosts);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;
        return postResponse;
    }

    private void checkItemAtPosition(int position, Post post) throws InterruptedException {
        if (position > 0) {
            onView(withItemText(post.description, R.id.browse_container_dock)).perform(click());
            // No developer options to disable animations on the ADT-1, so this sleep is needed
            Thread.sleep(200);
        }
        onView(withItemText(post.description, R.id.browse_container_dock)).check(matches(isDisplayed()));
        onView(withItemText(post.username, R.id.browse_container_dock)).check(matches(isDisplayed()));
    }

}