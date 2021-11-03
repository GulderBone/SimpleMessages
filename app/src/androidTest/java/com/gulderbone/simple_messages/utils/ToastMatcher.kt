package com.gulderbone.simple_messages.utils

import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_TOAST
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/**
 * // To assert a toast does *not* pop up:
 * onToast("text").check(doesNotExist())
 * onToast(textId).check(doesNotExist())
 *
 * // To assert a toast does pop up:
 * onToast("text").check(matches(isDisplayed()))
 * onToast(textId).check(matches(isDisplayed()))
 */
class ToastMatcher(private val maxFailures: Int = DEFAULT_MAX_FAILURES) : TypeSafeMatcher<Root>() {

    private var failures = 0

    override fun describeTo(description: Description) {
        description.appendText("is toast")
    }

    public override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        @Suppress("DEPRECATION") // TYPE_TOAST is deprecated in favor of TYPE_APPLICATION_OVERLAY
        if (type == TYPE_TOAST || type == TYPE_APPLICATION_OVERLAY) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            if (windowToken === appToken) {
                return true
            }
        }
        return (++failures >= maxFailures)
    }

    companion object {

        private const val DEFAULT_MAX_FAILURES = 5

        fun onToast(text: String, maxRetries: Int = DEFAULT_MAX_FAILURES) = onView(withText(text)).inRoot(isToast(maxRetries))!!

        fun onToast(textId: Int, maxRetries: Int = DEFAULT_MAX_FAILURES) = onView(withText(textId)).inRoot(isToast(maxRetries))!!

        private fun isToast(maxRetries: Int = DEFAULT_MAX_FAILURES): Matcher<Root> {
            return ToastMatcher(maxRetries)
        }
    }

}