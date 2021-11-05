package com.example.connectmap;

import android.widget.AutoCompleteTextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.example.connectmap.activities.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);


    /*
        Test the search capability ui
     */

    @Test
    public void testSearch() {
        onView(withId(R.id.action_search)).perform(click());
        onView(isAssignableFrom(AutoCompleteTextView.class)).perform(typeText("teszt"));
    }
}
