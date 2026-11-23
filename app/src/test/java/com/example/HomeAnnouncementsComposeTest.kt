package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.Announcement
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.theme.AssiSTITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeAnnouncementsComposeTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private fun sampleAnnouncements(count: Int): List<Announcement> =
    (1..count).map { idx ->
      Announcement(
        id = "ann_$idx",
        authorId = "admin_default",
        authorName = "STI Sta. Mesa Admin",
        title = "Announcement $idx",
        message = "Details for announcement $idx",
        createdAtMillis = 1_700_000_000_000L + idx * 1000L,
      )
    }

  @Test
  fun emptyStateShowsNoAnnouncementsMessageAndNoDots() {
    composeTestRule.setContent {
      AssiSTITheme {
        HomeDashboardScreen(
          portalRole = PortalRole.STUDENT,
          isAdminAuthorized = false,
          announcementsState = UiState.Success(emptyList()),
          onOpenHistory = {},
          onOpenFeedback = {},
          onSwitchRole = {},
          onLogOut = {},
          onAnnouncementsTabClick = {},
          onSelectMaintenanceCreate = {},
          onSelectLostFoundCreate = {},
          onReportsTabClick = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("announcements_empty_state").assertIsDisplayed()
    composeTestRule.onNodeWithText("No announcements yet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Announcements posted by the admin will appear here.").assertIsDisplayed()
    composeTestRule.onNodeWithTag("announcement_dot_0").assertDoesNotExist()
  }

  @Test
  fun indicatorDotsMatchThreeAnnouncements() {
    composeTestRule.setContent {
      AssiSTITheme {
        HomeDashboardScreen(
          portalRole = PortalRole.STUDENT,
          isAdminAuthorized = false,
          announcementsState = UiState.Success(sampleAnnouncements(3)),
          onOpenHistory = {},
          onOpenFeedback = {},
          onSwitchRole = {},
          onLogOut = {},
          onAnnouncementsTabClick = {},
          onSelectMaintenanceCreate = {},
          onSelectLostFoundCreate = {},
          onReportsTabClick = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("announcement_dot_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("announcement_dot_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("announcement_dot_2").assertIsDisplayed()
    composeTestRule.onNodeWithTag("announcement_dot_3").assertDoesNotExist()
  }

  @Test
  fun indicatorDotsExpandBeyondFiveWhenAdminPostsMore() {
    composeTestRule.setContent {
      AssiSTITheme {
        HomeDashboardScreen(
          portalRole = PortalRole.ADMIN,
          isAdminAuthorized = true,
          announcementsState = UiState.Success(sampleAnnouncements(6)),
          onOpenHistory = {},
          onOpenFeedback = {},
          onSwitchRole = {},
          onLogOut = {},
          onAnnouncementsTabClick = {},
          onSelectMaintenanceCreate = {},
          onSelectLostFoundCreate = {},
          onReportsTabClick = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    for (index in 0 until 6) {
      composeTestRule.onNodeWithTag("announcement_dot_$index").assertIsDisplayed()
    }
    composeTestRule.onNodeWithTag("announcement_dot_6").assertDoesNotExist()
  }
}
