package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Announcement
import com.example.data.LostFoundItem
import com.example.data.MaintenanceReport
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.ui.components.AssiSTIBottomNav
import com.example.ui.components.AssiSTITopBar
import com.example.ui.components.BottomLeftBackBar
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CampusWatermarkBackground
import com.example.ui.components.CreateActionBubble
import com.example.ui.components.LostFoundStatusBadge
import com.example.ui.components.MaintenanceStatusBadge
import com.example.ui.theme.CampusBlue
import com.example.ui.theme.InkDark
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SoftIceBg
import com.example.ui.theme.StiGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AnnouncementSlide(val title: String, val message: String, val tag: String)

@Composable
fun HomeDashboardScreen(
  portalRole: PortalRole,
  isAdminAuthorized: Boolean,
  announcementsState: UiState<List<Announcement>>,
  onOpenHistory: () -> Unit,
  onOpenFeedback: () -> Unit,
  onSwitchRole: () -> Unit,
  onLogOut: () -> Unit,
  onAnnouncementsTabClick: () -> Unit,
  onSelectMaintenanceCreate: () -> Unit,
  onSelectLostFoundCreate: () -> Unit,
  onReportsTabClick: () -> Unit,
) {
  var showCreateBubble by remember { mutableStateOf(false) }

  CampusWatermarkBackground(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize()) {
      Column(modifier = Modifier.fillMaxSize()) {
        AssiSTITopBar(
          showDropdownArrow = true,
          portalRole = portalRole,
          isAdminAuthorized = isAdminAuthorized,
          onHistoryClick = onOpenHistory,
          onFeedbackClick = onOpenFeedback,
          onSwitchRoleClick = onSwitchRole,
          onLogOutClick = onLogOut,
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .align(Alignment.CenterHorizontally)
            .padding(horizontal = 24.dp, vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Spacer(Modifier.height(12.dp))

          // Canva Pages 3, 4, 7, 18: Rounded white carousel container with 5 indicator dots
          Surface(
            color = PureWhite,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
              .fillMaxWidth()
              .height(330.dp),
          ) {
            when (announcementsState) {
              is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(color = RoyalNavy)
                }
              }

              is UiState.Error -> {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Text(
                    text = announcementsState.message,
                    color = MutedInk,
                    textAlign = TextAlign.Center,
                  )
                }
              }

              is UiState.Success -> {
                if (announcementsState.data.isEmpty()) {
                  Column(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    Surface(
                      color = Color(0xFFC5CCD8),
                      shape = RoundedCornerShape(14.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("announcements_empty_state"),
                    ) {
                      Column(
                        modifier = Modifier
                          .fillMaxSize()
                          .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                      ) {
                        Icon(
                          imageVector = Icons.Default.Campaign,
                          contentDescription = null,
                          tint = RoyalNavy,
                          modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                          text = "No announcements yet",
                          fontFamily = PlusJakartaSansFamily,
                          fontWeight = FontWeight.ExtraBold,
                          fontSize = 20.sp,
                          color = RoyalNavy,
                          textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                          text = "Announcements posted by the admin will appear here.",
                          fontSize = 15.sp,
                          lineHeight = 22.sp,
                          color = InkDark,
                          textAlign = TextAlign.Center,
                        )
                      }
                    }
                  }
                } else {
                  val slides = announcementsState.data.map { ann ->
                    val dateStr = ann.createdAtMillis.takeIf { it > 0L }?.let {
                      SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(it))
                    } ?: "Today"
                    AnnouncementSlide(
                      title = ann.title,
                      message = ann.message,
                      tag = "${ann.authorName} · $dateStr",
                    )
                  }

                  val pagerState = rememberPagerState(pageCount = { slides.size })
                  Column(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    Surface(
                      color = Color(0xFFC5CCD8),
                      shape = RoundedCornerShape(14.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    ) {
                      HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                      ) { page ->
                        val slide = slides[page]
                        Column(
                          modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                          verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                          Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                          ) {
                            Surface(color = RoyalNavy, shape = RoundedCornerShape(8.dp)) {
                              Text(
                                text = slide.tag,
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                              )
                            }
                            Icon(
                              imageVector = Icons.Default.Campaign,
                              contentDescription = null,
                              tint = RoyalNavy,
                              modifier = Modifier.size(18.dp),
                            )
                          }

                          Column {
                            Text(
                              text = slide.title,
                              fontFamily = PlusJakartaSansFamily,
                              fontWeight = FontWeight.ExtraBold,
                              fontSize = 20.sp,
                              color = RoyalNavy,
                              maxLines = 2,
                              overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                              text = slide.message,
                              fontSize = 15.sp,
                              lineHeight = 22.sp,
                              color = InkDark,
                              maxLines = 5,
                              overflow = TextOverflow.Ellipsis,
                            )
                          }

                          Text(
                            text = "STI College Sta. Mesa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RoyalNavy.copy(alpha = 0.75f),
                          )
                        }
                      }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Dynamic pager indicator matching the exact number of announcements
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                      repeat(slides.size) { index ->
                        val active = index == pagerState.currentPage
                        Box(
                          modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (active) Color(0xFF4A4E57) else Color(0xFFA9AEB8))
                            .testTag("announcement_dot_$index")
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          Spacer(Modifier.weight(1f))

          if (showCreateBubble) {
            CreateActionBubble(
              onMaintenanceClick = {
                showCreateBubble = false
                onSelectMaintenanceCreate()
              },
              onLostAndFoundClick = {
                showCreateBubble = false
                onSelectLostFoundCreate()
              },
              modifier = Modifier.padding(bottom = 8.dp),
            )
          }
        }

        AssiSTIBottomNav(
          activeTab = if (showCreateBubble) BottomNavTab.CREATE else BottomNavTab.ANNOUNCEMENTS,
          showCenterPlus = true,
          onAnnouncementsClick = {
            showCreateBubble = false
            onAnnouncementsTabClick()
          },
          onCreateClick = {
            if (portalRole == PortalRole.ADMIN) {
              onAnnouncementsTabClick()
            } else {
              showCreateBubble = !showCreateBubble
            }
          },
          onReportsClick = {
            showCreateBubble = false
            onReportsTabClick()
          },
        )
      }
    }
  }
}

private sealed interface HistoryEntry {
  data class Maint(val report: MaintenanceReport) : HistoryEntry
  data class Lost(val item: LostFoundItem) : HistoryEntry
}

@Composable
fun StudentHistoryScreen(
  userProfile: UserProfile,
  maintenanceState: UiState<List<MaintenanceReport>>,
  lostFoundState: UiState<List<LostFoundItem>>,
  onHomeClick: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var selectedEntry by remember { mutableStateOf<HistoryEntry?>(null) }

  CampusWatermarkBackground(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      AssiSTITopBar(
        showDropdownArrow = false,
        onLogoClick = onHomeClick,
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .widthIn(max = 620.dp)
          .align(Alignment.CenterHorizontally),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // Canva Page 6: Large circular profile avatar overlapping horizontal navy rule
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .background(RoyalNavy)
          )
          Surface(
            color = SoftIceBg,
            shape = CircleShape,
            border = BorderStroke(5.dp, RoyalNavy),
            modifier = Modifier.size(136.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Student Avatar",
                tint = RoyalNavy,
                modifier = Modifier.size(88.dp),
              )
            }
          }
        }

        Spacer(Modifier.height(10.dp))

        Text(
          text = userProfile.displayName.uppercase(Locale.getDefault()),
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 24.sp,
          color = CampusBlue,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(14.dp))

        Text(
          text = "Reports History",
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = RoyalNavy,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        )

        Spacer(Modifier.height(10.dp))

        val maintList = (maintenanceState as? UiState.Success)?.data.orEmpty()
        val myLostList = (lostFoundState as? UiState.Success)?.data
          .orEmpty()
          .filter { it.userId == userProfile.userId }

        val entries = buildList {
          maintList.forEach { add(HistoryEntry.Maint(it)) }
          myLostList.forEach { add(HistoryEntry.Lost(it)) }
        }

        if (entries.isEmpty()) {
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = "No reports submitted yet.\nTap + on the home screen to report an issue or lost item.",
              color = MutedInk,
              textAlign = TextAlign.Center,
              fontSize = 15.sp,
            )
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
          ) {
            items(
              items = entries,
              key = { entry ->
                when (entry) {
                  is HistoryEntry.Maint -> "m_${entry.report.id}"
                  is HistoryEntry.Lost -> "l_${entry.item.id}"
                }
              },
            ) { entry ->
              Surface(
                color = PureWhite,
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 3.dp,
                modifier = Modifier
                  .height(152.dp)
                  .clickable { selectedEntry = entry },
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                  verticalArrangement = Arrangement.SpaceBetween,
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Icon(
                      imageVector = when (entry) {
                        is HistoryEntry.Maint -> Icons.Default.Build
                        is HistoryEntry.Lost -> Icons.Default.FindInPage
                      },
                      contentDescription = null,
                      tint = RoyalNavy,
                      modifier = Modifier.size(16.dp),
                    )
                    Text(
                      text = when (entry) {
                        is HistoryEntry.Maint -> entry.report.issueCategory
                        is HistoryEntry.Lost -> "Lost & Found"
                      },
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = RoyalNavy,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                  }

                  Text(
                    text = when (entry) {
                      is HistoryEntry.Maint -> entry.report.description
                      is HistoryEntry.Lost -> entry.item.description
                    },
                    fontSize = 13.sp,
                    color = InkDark,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                  )

                  when (entry) {
                    is HistoryEntry.Maint -> MaintenanceStatusBadge(entry.report.statusEnum())
                    is HistoryEntry.Lost -> LostFoundStatusBadge(entry.item.statusEnum())
                  }
                }
              }
            }
          }
        }
      }

      BottomLeftBackBar(onBack = onBack)
    }
  }

  selectedEntry?.let { entry ->
    AlertDialog(
      onDismissRequest = { selectedEntry = null },
      confirmButton = {
        TextButton(onClick = { selectedEntry = null }) { Text("Close") }
      },
      title = {
        Text(
          text = when (entry) {
            is HistoryEntry.Maint -> entry.report.issueCategory
            is HistoryEntry.Lost -> "Lost & Found Report"
          },
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          val photo = when (entry) {
            is HistoryEntry.Maint -> entry.report.photoBase64
            is HistoryEntry.Lost -> entry.item.photoBase64
          }
          if (!photo.isNullOrBlank()) {
            com.example.ui.components.ReportThumbnailBox(
              photoBase64 = photo,
              fallbackLabel = if (entry is HistoryEntry.Lost) "ITEM" else "IMG",
              size = 180.dp,
            )
          }
          Text(
            text = when (entry) {
              is HistoryEntry.Maint -> entry.report.description
              is HistoryEntry.Lost -> entry.item.description
            },
          )
          when (entry) {
            is HistoryEntry.Maint -> MaintenanceStatusBadge(entry.report.statusEnum())
            is HistoryEntry.Lost -> LostFoundStatusBadge(entry.item.statusEnum())
          }
        }
      },
    )
  }
}

@Composable
fun FeedbackDialog(
  isSubmitting: Boolean,
  onDismiss: () -> Unit,
  onSubmit: (rating: Int, message: String) -> Unit,
) {
  var rating by remember { mutableIntStateOf(5) }
  var message by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = SoftCardBg,
    title = {
      Text(
        text = "Campus Feedback",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        color = RoyalNavy,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "How was your AssiSTI experience?",
          color = InkDark,
          fontSize = 14.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          (1..5).forEach { star ->
            IconButton(
              onClick = { rating = star },
              modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
              Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Rate $star stars",
                tint = if (star <= rating) StiGold else MutedInk,
                modifier = Modifier.size(30.dp),
              )
            }
          }
        }
        OutlinedTextField(
          value = message,
          onValueChange = { if (it.length <= 500) message = it },
          label = { Text("Share suggestions or feedback") },
          minLines = 3,
          maxLines = 5,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("feedback_message_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(rating, message) },
        enabled = !isSubmitting && message.trim().isNotEmpty(),
        colors = ButtonDefaults.buttonColors(containerColor = RoyalNavy),
        modifier = Modifier.testTag("feedback_submit_button"),
      ) {
        Text(if (isSubmitting) "Sending..." else "Submit")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel", color = RoyalNavy) }
    },
  )
}
