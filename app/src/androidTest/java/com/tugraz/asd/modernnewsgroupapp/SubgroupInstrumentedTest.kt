package com.tugraz.asd.modernnewsgroupapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubgroupInstrumentedTest {
    @Rule
    @JvmField

    var  rule: ActivityScenarioRule<ActivityAddNewsgroup> = ActivityScenarioRule<ActivityAddNewsgroup>(ActivityAddNewsgroup::class.java)

    private fun init() {
        val inputName = onView(withId(R.id.editText_name)).check(matches(ViewMatchers.isDisplayed()))
        val inputEmail = onView(withId(R.id.editText_email)).check(matches(ViewMatchers.isDisplayed()))

        // fill text input
        inputName.perform(ViewActions.replaceText("Tamara"), ViewActions.closeSoftKeyboard())
        inputEmail.perform(ViewActions.replaceText("test@test.com"), ViewActions.closeSoftKeyboard())

        onView(ViewMatchers.withText("NEXT")).perform(click())
    }

    @Test
    fun checkIfListCollapses() {
        init()
        onView(ViewMatchers.withText("control")).perform(click())
        onView(ViewMatchers.withText("control.checkgroups")).check(matches(not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun checkIfListExpands() {
        init()
        onView(ViewMatchers.withText("control")).perform(click())
        onView(ViewMatchers.withText("control")).perform(click())
        onView(ViewMatchers.withText("control.checkgroups")).check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun checkNewsgroupFilter() {
        init()

        // filter by "vc"
        val appCompatEditText = onView(withId(R.id.editTextGroupFilter)).check(matches(ViewMatchers.isDisplayed()))
        appCompatEditText.perform(ViewActions.replaceText("vc"), ViewActions.closeSoftKeyboard())
        onView(ViewMatchers.withText("vc-graz")).check(matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun checkNewsgroupFilter1() {
        init()

        // filter by "math"
        val appCompatEditText = onView(withId(R.id.editTextGroupFilter)).check(matches(ViewMatchers.isDisplayed()))
        appCompatEditText.perform(ViewActions.replaceText("math"), ViewActions.closeSoftKeyboard())
        onView(ViewMatchers.withText("vc-graz")).check(matches(not(ViewMatchers.isDisplayed())))
    }
}