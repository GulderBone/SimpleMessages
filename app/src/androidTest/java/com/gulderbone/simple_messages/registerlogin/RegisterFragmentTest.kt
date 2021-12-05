package com.gulderbone.simple_messages.registerlogin

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.gulderbone.simple_messages.R
import com.gulderbone.simple_messages.presentation.latestmessages.LatestMessagesFragment
import com.gulderbone.simple_messages.utils.CountingIdlingResourceSingleton
import com.gulderbone.simple_messages.utils.ToastMatcher.Companion.onToast
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@RunWith(AndroidJUnit4::class)
class RegisterFragmentTest {

    @Rule
    @JvmField
    val activityRule = ActivityScenarioRule(RegisterFragment::class.java)

    @Before
    fun setup() {
        IdlingRegistry.getInstance().register(CountingIdlingResourceSingleton.countingIdlingResource)

        Intents.init()
        intending(hasAction(Intent.ACTION_PICK)).respondWith(getImageResult())
    }

    private fun getImageResult(): Instrumentation.ActivityResult {
        val context = InstrumentationRegistry.getInstrumentation().context
        val assetManager = context.assets
        val fileDirectory = Environment.getDataDirectory()
        fileDirectory.mkdirs()
        val file = File.createTempFile("testImage", ".jpg")

        val bitmap = BitmapFactory.decodeStream(assetManager.open("demi.jpg"))
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, file.outputStream())

        val uri = Uri.fromFile(file)

        val intent = Intent()
        intent.data = uri

        return Instrumentation.ActivityResult(Activity.RESULT_OK, intent)
    }

    private fun deleteTestAccount() {
        val instance = FirebaseAuth.getInstance()
        instance.signInWithEmailAndPassword("testUser@test.com", "password")
            .addOnCompleteListener {
                instance.currentUser?.delete()
            }
    }

    @Test
    fun registerUser() {
        onView(withId(R.id.selectphoto_button_register))
            .perform(click())
        onView(withId(R.id.username_edittext_register_text))
            .perform(typeText("testUser"))
        onView(withId(R.id.email_edittext_register_text))
            .perform(typeText("testUser@test.com"))
        onView(withId(R.id.password_edittext_register_text))
            .perform(typeText("password"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_button_register))
            .perform(click())

        didGoToLatestMessages()
    }

    @Test // TODO Does not work on Android 11
    fun registerUserWithoutProfilePhoto() {
        onView(withId(R.id.username_edittext_register_text))
            .perform(typeText("testUser"))
        onView(withId(R.id.email_edittext_register_text))
            .perform(typeText("testUser@test.com"))
        onView(withId(R.id.password_edittext_register_text))
            .perform(typeText("password"))
            .perform(closeSoftKeyboard())
        onView(withId(R.id.register_button_register))
            .perform(click())

        onToast("Please choose a profile picture").check(matches(isDisplayed()))

        activityRule.scenario.onActivity {
            onView(withText("Please choose a profile picture"))
                .inRoot(withDecorView(not(`is`(it.window.decorView))))
                .check(matches(isDisplayed()))

            onView(withText("Please choose a profile picture"))
                .inRoot(
                withDecorView(not(it.window.decorView)))
                .check(matches(isDisplayed()))
        }

    }

    private fun didGoToLatestMessages() {
        CountingIdlingResourceSingleton.countingIdlingResource.registerIdleTransitionCallback {
            intended(hasComponent(LatestMessagesFragment::class.java.name))
        }
    }

    @After
    fun clean() {
        IdlingRegistry.getInstance().unregister(CountingIdlingResourceSingleton.countingIdlingResource)

        Intents.release()
    }
}