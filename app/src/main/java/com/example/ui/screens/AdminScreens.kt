package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.LostFoundStatus
import com.example.data.MaintenanceReport
import com.example.data.MaintenanceStatus
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.ui.components.AssiSTIBottomNav
import com.example.ui.components.AssiSTITopBar
import com.example.ui.components.BottomLeftBackBar
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CampusWatermarkBackground
import com.example.ui.components.LostFoundStatusBadge
import com.example.ui.components.MaintenanceStatusBadge
import com.example.ui.components.ReportCategoryTabs
import com.example.ui.components.ReportThumbnailBox
import com.example.ui.components.SpeechBubbleCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InkDark
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftIceBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminAnnouncementsScreen(
  announcementsState: UiState<List<Announcement>>,
  isBusy: Boolean,
  onPublishAnnouncement: (title: String, message: String) -> Unit,
  onDeleteAnnouncement: (String) -> Unit,
  onHomeClick: () -> Unit,
  onSwitchToStudentView: () -> Unit,
  onLogOut: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var titleText by remember { mutableStateOf("") }
  var messageText by remember { mutableStateOf("") }

  CampusWatermarkBackground(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding(),
    ) {
      AssiSTITopBar(
        showDropdownArrow = true,
        portalRole = PortalRole.ADMIN,
        isAdminAuthorized = true,
        onLogoClick = onHomeClick,
        onSwitchRoleClick = onSwitchToStudentView,
        onLogOutClick = onLogOut,
      )

      // Canva Page 19: Scrollable Announcement speech bubbles
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      ) {
        when (announcementsState) {
          is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalNavy)
          }
          is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(announcementsState.message, color = MutedInk, textAlign = TextAlign.Center)
          }
          is UiState.Success -> {
            if (announcementsState.data.isEmpty()) {
              Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = "No announcements yet.\nType an advisory below to publish it to the campus board.",
                  color = MutedInk,
                  textAlign = TextAlign.Center,
                )
              }
            } else {
              LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                  .fillMaxSize()
                  .widthIn(max = 600.dp)
                  .align(Alignment.TopCenter),
              ) {
                items(announcementsState.data, key = { it.id }) { ann ->
                  Column(modifier = Modifier.fillMaxWidth()) {
                    val dateLabel = ann.createdAtMillis.takeIf { it > 0L }?.let {
                      SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(it))
                    } ?: "Just now"
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, bottom = 4.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "Announcement · $dateLabel",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoyalNavy,
                      )
                      IconButton(
                        onClick = { onDeleteAnnouncement(ann.id) },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                      ) {
                        Icon(
                          imageVector = Icons.Default.DeleteOutline,
                          contentDescription = "Delete announcement",
                          tint = ErrorRed,
                          modifier = Modifier.size(20.dp),
                        )
                      }
                    }

                    SpeechBubbleCard {
                      Text(
                        text = ann.title,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RoyalNavy,
                      )
                      Spacer(Modifier.height(4.dp))
                      Text(
                        text = ann.message,
                        fontSize = 14.sp,
                        color = InkDark,
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Canva Page 19: Bottom white pill composer bar ("Enter text" + Send icon)
      Surface(
        color = PureWhite,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
          OutlinedTextField(
            value = titleText,
            onValueChange = { if (it.length <= 100) titleText = it },
            singleLine = true,
            placeholder = { Text("Headline (optional)", color = MutedInk.copy(alpha = 0.65f)) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = RoyalNavy,
              unfocusedBorderColor = SoftIceBg,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("announcement_title_input"),
          )
          Spacer(Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = messageText,
              onValueChange = { if (it.length <= 1000) messageText = it },
              placeholder = { Text("Enter text", color = MutedInk.copy(alpha = 0.65f)) },
              maxLines = 3,
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalNavy,
                unfocusedBorderColor = SoftIceBg,
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("announcement_message_input"),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
              onClick = {
                val msg = messageText.trim()
                if (msg.isNotEmpty()) {
                  val headline = titleText.trim().ifBlank { "Campus Announcement" }
                  onPublishAnnouncement(headline, msg)
                  titleText = ""
                  messageText = ""
                }
              },
              enabled = !isBusy && messageText.trim().isNotEmpty(),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("announcement_send_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Publish announcement",
                tint = RoyalNavy,
                modifier = Modifier.size(28.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminReportsListScreen(
  maintenanceState: UiState<List<MaintenanceReport>>,
  lostFoundState: UiState<List<LostFoundItem>>,
  onInspectMaintenance: (MaintenanceReport) -> Unit,
  onInspectLostFound: (LostFoundItem) -> Unit,
  onHomeClick: () -> Unit,
  onAnnouncementsClick: () -> Unit,
  onSwitchToStudentView: () -> Unit,
  onLogOut: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var selectedTab by remember { mutableIntStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg),
  ) {
    AssiSTITopBar(
      showDropdownArrow = true,
      portalRole = PortalRole.ADMIN,
      isAdminAuthorized = true,
      onLogoClick = onHomeClick,
      onSwitchRoleClick = onSwitchToStudentView,
      onLogOutClick = onLogOut,
    )

    ReportCategoryTabs(
      selectedTab = selectedTab,
      onSelectTab = { selectedTab = it },
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
    ) {
      if (selectedTab == 0) {
        // Canva Pages 20 & 21: Maintenance IMG cards + circular Magnifying Glass inspect icon
        when (maintenanceState) {
          is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalNavy)
          }
          is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(maintenanceState.message, color = MutedInk, textAlign = TextAlign.Center)
          }
          is UiState.Success -> {
            if (maintenanceState.data.isEmpty()) {
              Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No maintenance reports submitted yet.", color = MutedInk)
              }
            } else {
              LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                  .fillMaxSize()
                  .widthIn(max = 600.dp)
                  .align(Alignment.TopCenter),
              ) {
                items(maintenanceState.data, key = { it.id }) { report ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { onInspectMaintenance(report) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                  ) {
                    ReportThumbnailBox(
                      photoBase64 = report.photoBase64,
                      fallbackLabel = "IMG",
                      size = 132.dp,
                    )
                    Column(
                      modifier = Modifier.weight(1f),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text = report.issueCategory,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = RoyalNavy,
                      )
                      Text(
                        text = report.studentName,
                        fontSize = 12.sp,
                        color = MutedInk,
                      )
                      MaintenanceStatusBadge(report.statusEnum())
                    }
                    Surface(
                      color = Color(0xFFC2C9DA),
                      shape = CircleShape,
                      modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("inspect_maintenance_${report.id}")
                        .clip(CircleShape)
                        .clickable { onInspectMaintenance(report) },
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.Search,
                          contentDescription = "Inspect maintenance report",
                          tint = RoyalNavy,
                          modifier = Modifier.size(26.dp),
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      } else {
        // Canva Page 24: Lost & Found speech-bubble REPORT cards + circular Magnifying Glass inspect icon
        when (lostFoundState) {
          is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = RoyalNavy)
          }
          is UiState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(lostFoundState.message, color = MutedInk, textAlign = TextAlign.Center)
          }
          is UiState.Success -> {
            if (lostFoundState.data.isEmpty()) {
              Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No lost & found reports submitted yet.", color = MutedInk)
              }
            } else {
              LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                  .fillMaxSize()
                  .widthIn(max = 600.dp)
                  .align(Alignment.TopCenter),
              ) {
                items(lostFoundState.data, key = { it.id }) { item ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                    SpeechBubbleCard(
                      onClick = { onInspectLostFound(item) },
                      modifier = Modifier.weight(1f),
                    ) {
                      Text(
                        text = "REPORT",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = InkDark,
                      )
                      Spacer(Modifier.height(4.dp))
                      Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = InkDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Spacer(Modifier.height(6.dp))
                      LostFoundStatusBadge(item.statusEnum())
                    }

                    Surface(
                      color = Color(0xFFC2C9DA),
                      shape = CircleShape,
                      modifier = Modifier
                        .size(48.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("inspect_lostfound_${item.id}")
                        .clip(CircleShape)
                        .clickable { onInspectLostFound(item) },
                    ) {
                      Box(contentAlignment = Alignment.Center) {
                        Icon(
                          imageVector = Icons.Default.Search,
                          contentDescription = "Inspect lost and found report",
                          tint = RoyalNavy,
                          modifier = Modifier.size(26.dp),
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Canva Pages 20 & 24: 2-button bottom pill + bottom-left Back arrow
    AssiSTIBottomNav(
      activeTab = BottomNavTab.REPORTS,
      showCenterPlus = false,
      onAnnouncementsClick = onAnnouncementsClick,
      onReportsClick = {},
      showBackArrow = true,
      onBackClick = onBack,
    )
  }
}

@Composable
fun AdminMaintenanceDetailScreen(
  report: MaintenanceReport,
  onUpdateStatus: (MaintenanceStatus) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var dropdownExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg)
      .statusBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .widthIn(max = 560.dp)
        .align(Alignment.CenterHorizontally)
        .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
      // Canva Pages 22 & 23
      Text(
        text = "Student: ${report.studentName}",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = InkDark,
      )
      Spacer(Modifier.height(10.dp))

      ReportThumbnailBox(
        photoBase64 = report.photoBase64,
        fallbackLabel = "IMG",
        size = 210.dp,
      )

      Spacer(Modifier.height(18.dp))

      Text(
        text = "Issue: ${report.issueCategory}",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        color = InkDark,
      )

      Spacer(Modifier.height(10.dp))

      Text(
        text = "Description:",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = InkDark,
      )
      Spacer(Modifier.height(8.dp))

      Surface(
        color = PureWhite,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 120.dp, max = 220.dp),
      ) {
        Text(
          text = report.description,
          fontSize = 15.sp,
          color = InkDark,
          modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        )
      }

      Spacer(Modifier.height(20.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = RoyalNavy,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = "Status",
            color = SoftIceBg,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
          )
        }

        Box {
          Surface(
            color = RoyalNavy,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("admin_maintenance_status_dropdown")
              .clip(RoundedCornerShape(20.dp))
              .clickable { dropdownExpanded = true },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = report.statusEnum().label,
                color = Color(0xFF93B8E2),
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change maintenance status",
                tint = Color(0xFF93B8E2),
              )
            }
          }

          DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            containerColor = RoyalNavy,
            shape = RoundedCornerShape(18.dp),
          ) {
            MaintenanceStatus.entries.forEach { option ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = option.label,
                    color = Color(0xFF93B8E2),
                    fontWeight = FontWeight.Bold,
                  )
                },
                onClick = {
                  dropdownExpanded = false
                  onUpdateStatus(option)
                },
                modifier = Modifier.testTag("status_option_${option.name.lowercase()}"),
              )
            }
          }
        }
      }
    }

    BottomLeftBackBar(onBack = onBack)
  }
}

@Composable
fun AdminLostFoundDetailScreen(
  item: LostFoundItem,
  onUpdateStatus: (LostFoundStatus) -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var dropdownExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg)
      .statusBarsPadding(),
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .widthIn(max = 560.dp)
        .align(Alignment.CenterHorizontally)
        .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
      // Canva Pages 25 & 26
      Text(
        text = "Student: ${item.studentName}",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = InkDark,
      )
      Spacer(Modifier.height(10.dp))

      ReportThumbnailBox(
        photoBase64 = item.photoBase64,
        fallbackLabel = "ITEM",
        size = 220.dp,
      )

      Spacer(Modifier.height(22.dp))

      Text(
        text = "Description:",
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = InkDark,
      )
      Spacer(Modifier.height(8.dp))

      Surface(
        color = PureWhite,
        shape = RoundedCornerShape(22.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = 120.dp, max = 220.dp),
      ) {
        Text(
          text = item.description,
          fontSize = 15.sp,
          color = InkDark,
          modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        )
      }

      Spacer(Modifier.height(20.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Surface(
          color = RoyalNavy,
          shape = RoundedCornerShape(50),
        ) {
          Text(
            text = "Status",
            color = SoftIceBg,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
          )
        }

        Box {
          Surface(
            color = RoyalNavy,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .testTag("admin_lostfound_status_dropdown")
              .clip(RoundedCornerShape(20.dp))
              .clickable { dropdownExpanded = true },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = item.statusEnum().label,
                color = Color(0xFF93B8E2),
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Change lost and found status",
                tint = Color(0xFF93B8E2),
              )
            }
          }

          DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            containerColor = RoyalNavy,
            shape = RoundedCornerShape(18.dp),
          ) {
            LostFoundStatus.entries.forEach { option ->
              DropdownMenuItem(
                text = {
                  Text(
                    text = option.label,
                    color = Color(0xFF93B8E2),
                    fontWeight = FontWeight.Bold,
                  )
                },
                onClick = {
                  dropdownExpanded = false
                  onUpdateStatus(option)
                },
                modifier = Modifier.testTag("lf_status_option_${option.name.lowercase()}"),
              )
            }
          }
        }
      }
    }

    BottomLeftBackBar(onBack = onBack)
  }
}
