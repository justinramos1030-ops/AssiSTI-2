package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.AssiSTITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthNavigationComposeTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun adminLoginBackButtonReturnsToPortalSelect() {
    composeTestRule.setContent {
      AssiSTITheme {
        AuthScreen(
          onStudentLogin = { _, _ -> Result.success(Unit) },
          onAdminLogin = { _, _ -> Result.success(Unit) },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("admin_login_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("admin_id_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_login_back_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("microsoft_login_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_login_button").assertIsDisplayed()
  }

  @Test
  fun studentLoginBackButtonReturnsToPortalSelect() {
    composeTestRule.setContent {
      AssiSTITheme {
        AuthScreen(
          onStudentLogin = { _, _ -> Result.success(Unit) },
          onAdminLogin = { _, _ -> Result.success(Unit) },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("microsoft_login_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("student_email_input").assertIsDisplayed()
    composeTestRule.onNodeWithTag("student_login_back_button").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("microsoft_login_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_login_button").assertIsDisplayed()
  }
}
