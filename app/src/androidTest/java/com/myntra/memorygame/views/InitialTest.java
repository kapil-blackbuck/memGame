package com.myntra.memorygame.views;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.myntra.memorygame.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class InitialTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void initialTest() {
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.tiles_recycler_view),
                        withParent(allOf(withId(R.id.content_main),
                                withParent(withId(R.id.coord)))),
                        isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.tiles_recycler_view),
                        withParent(allOf(withId(R.id.content_main),
                                withParent(withId(R.id.coord)))),
                        isDisplayed()));
        recyclerView2.perform(actionOnItemAtPosition(4, click()));

        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.tiles_recycler_view),
                        withParent(allOf(withId(R.id.content_main),
                                withParent(withId(R.id.coord)))),
                        isDisplayed()));
        recyclerView3.perform(actionOnItemAtPosition(5, click()));


        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.restartButton), withText("Restart"),
                        withParent(allOf(withId(R.id.content_main),
                                withParent(withId(R.id.coord)))),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction recyclerView4 = onView(
                allOf(withId(R.id.tiles_recycler_view),
                        withParent(allOf(withId(R.id.content_main),
                                withParent(withId(R.id.coord)))),
                        isDisplayed()));
        recyclerView4.perform(actionOnItemAtPosition(2, click()));

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
