package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LostFoundStatus
import com.example.data.MaintenanceStatus
import com.example.data.PortalRole
import com.example.ui.theme.CampusBlue
import com.example.ui.theme.InkDark
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SoftIceBg
import com.example.ui.theme.StatusFixedBg
import com.example.ui.theme.StatusFixedText
import com.example.ui.theme.StatusOngoingBg
import com.example.ui.theme.StatusOngoingText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.ui.theme.TabInactive
import com.example.ui.util.ImagePickerHelper

@Composable
fun AssiSTITopBar(
  showDropdownArrow: Boolean = true,
  portalRole: PortalRole = PortalRole.STUDENT,
  isAdminAuthorized: Boolean = false,
  onLogoClick: (() -> Unit)? = null,
  onHistoryClick: (() -> Unit)? = null,
  onFeedbackClick: (() -> Unit)? = null,
  onSwitchRoleClick: (() -> Unit)? = null,
  onLogOutClick: (() -> Unit)? = null,
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Surface(
    color = CampusBlue,
    shadowElevation = 2.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onLogoClick != null) {
          Modifier
            .clip(RoundedCornerShape(10.dp))
            .minimumInteractiveComponentSize()
            .testTag("top_bar_home_button")
            .clickable(onClickLabel = "Return to Home", onClick = onLogoClick)
            .padding(end = 6.dp)
        } else {
          Modifier
        },
      ) {
        Text(
          text = "AssiSTI",
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 32.sp,
          color = RoyalNavy,
        )
        if (portalRole == PortalRole.ADMIN) {
          Spacer(Modifier.width(8.dp))
          Surface(
            color = RoyalNavy,
            shape = RoundedCornerShape(10.dp),
          ) {
            Text(
              text = "ADMIN",
              color = PureWhite,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            )
          }
        }
      }

      Box {
        Box(
          modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("profile_menu_button")
            .clip(CircleShape)
            .clickable { menuExpanded = true },
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .border(2.dp, RoyalNavy, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Profile Menu",
              tint = RoyalNavy,
              modifier = Modifier.size(30.dp),
            )
          }
          if (showDropdownArrow) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(18.dp)
                .clip(CircleShape)
                .background(PureWhite)
                .border(1.dp, RoyalNavy, CircleShape),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Open Profile Menu",
                tint = RoyalNavy,
                modifier = Modifier.size(14.dp),
              )
            }
          }
        }

        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false },
          containerColor = CampusBlue,
          border = BorderStroke(1.5.dp, InkDark),
          shape = RoundedCornerShape(12.dp),
        ) {
          if (onHistoryClick != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = "History",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = RoyalNavy,
                )
              },
              onClick = {
                menuExpanded = false
                onHistoryClick()
              },
              modifier = Modifier.testTag("menu_history_item"),
            )
          }
          if (onFeedbackClick != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = "Feedback",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = RoyalNavy,
                )
              },
              onClick = {
                menuExpanded = false
                onFeedbackClick()
              },
              modifier = Modifier.testTag("menu_feedback_item"),
            )
          }
          if (isAdminAuthorized && onSwitchRoleClick != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = if (portalRole == PortalRole.ADMIN) "Student View" else "Admin View",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = RoyalNavy,
                )
              },
              onClick = {
                menuExpanded = false
                onSwitchRoleClick()
              },
              modifier = Modifier.testTag("menu_switch_role_item"),
            )
          }
          if (onLogOutClick != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = "Log Out",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = RoyalNavy,
                )
              },
              onClick = {
                menuExpanded = false
                onLogOutClick()
              },
              modifier = Modifier.testTag("menu_logout_item"),
            )
          }
        }
      }
    }
  }
}

enum class BottomNavTab {
  ANNOUNCEMENTS,
  CREATE,
  REPORTS,
}

@Composable
fun AssiSTIBottomNav(
  activeTab: BottomNavTab?,
  showCenterPlus: Boolean = true,
  onAnnouncementsClick: () -> Unit,
  onCreateClick: () -> Unit = {},
  onReportsClick: () -> Unit,
  showBackArrow: Boolean = false,
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(bottom = 16.dp, top = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    // Connected Canva pill bar
    Box(contentAlignment = Alignment.Center) {
      Surface(
        color = CampusBlue,
        shape = RoundedCornerShape(36.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
          .width(if (showCenterPlus) 244.dp else 170.dp)
          .height(52.dp),
      ) {}

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (showCenterPlus) 26.dp else 44.dp),
      ) {
        BottomCircleNavButton(
          icon = Icons.Default.Campaign,
          contentDescription = "Announcements",
          isActive = activeTab == BottomNavTab.ANNOUNCEMENTS,
          size = 54.dp,
          onClick = onAnnouncementsClick,
          testTag = "nav_announcements_button",
        )

        if (showCenterPlus) {
          BottomCircleNavButton(
            icon = Icons.Default.Add,
            contentDescription = "Create Report",
            isActive = activeTab == BottomNavTab.CREATE,
            size = 76.dp,
            iconSize = 44.dp,
            onClick = onCreateClick,
            testTag = "nav_create_button",
          )
        }

        BottomCircleNavButton(
          icon = Icons.Default.EditNote,
          contentDescription = "Reports List",
          isActive = activeTab == BottomNavTab.REPORTS,
          size = 54.dp,
          onClick = onReportsClick,
          testTag = "nav_reports_button",
        )
      }
    }

    if (showBackArrow && onBackClick != null) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .align(Alignment.CenterStart)
          .padding(start = 12.dp)
          .minimumInteractiveComponentSize()
          .testTag("bottom_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Navigate back",
          tint = RoyalNavy,
          modifier = Modifier.size(30.dp),
        )
      }
    }
  }
}

@Composable
private fun BottomCircleNavButton(
  icon: ImageVector,
  contentDescription: String,
  isActive: Boolean,
  size: Dp,
  iconSize: Dp = 28.dp,
  testTag: String,
  onClick: () -> Unit,
) {
  Surface(
    color = if (isActive) RoyalNavy else CampusBlue,
    shape = CircleShape,
    shadowElevation = if (isActive) 6.dp else 2.dp,
    modifier = Modifier
      .size(size)
      .minimumInteractiveComponentSize()
      .testTag(testTag)
      .clip(CircleShape)
      .clickable(onClick = onClick),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (isActive) SoftIceBg else RoyalNavy,
        modifier = Modifier.size(iconSize),
      )
    }
  }
}

@Composable
fun CreateActionBubble(
  onMaintenanceClick: () -> Unit,
  onLostAndFoundClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    Surface(
      color = CampusBlue,
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.5.dp, InkDark),
      shadowElevation = 8.dp,
    ) {
      Column(
        modifier = Modifier
          .width(228.dp)
          .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Maintenance",
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = RoyalNavy,
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("bubble_maintenance_option")
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onMaintenanceClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        )
        Text(
          text = "Lost & Found",
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = RoyalNavy,
          modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .testTag("bubble_lost_found_option")
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onLostAndFoundClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        )
      }
    }
    Canvas(modifier = Modifier.size(width = 22.dp, height = 14.dp)) {
      val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width / 2f, size.height)
        lineTo(size.width, 0f)
        close()
      }
      drawPath(path, color = CampusBlue)
      drawLine(InkDark, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height), strokeWidth = 3f)
      drawLine(InkDark, start = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height), end = androidx.compose.ui.geometry.Offset(size.width, 0f), strokeWidth = 3f)
    }
  }
}

@Composable
fun ReportCategoryTabs(
  selectedTab: Int, // 0 = Maintenance, 1 = Lost & Found
  onSelectTab: (Int) -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    listOf("Maintenance", "Lost & Found").forEachIndexed { index, title ->
      val active = selectedTab == index
      Box(
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .background(if (active) CampusBlue else TabInactive)
          .testTag("tab_${index}")
          .clickable { onSelectTab(index) },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = title,
          fontFamily = PlusJakartaSansFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = RoyalNavy,
        )
        if (active) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .height(3.dp)
              .background(RoyalNavy)
          )
        }
      }
    }
  }
}

@Composable
fun SpeechBubbleCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(modifier = modifier) {
    Surface(
      color = PureWhite,
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.5.dp, InkDark),
      shadowElevation = 2.dp,
      modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        content = content,
      )
    }
    // Speech bubble bottom-left pointer matching Canva Pages 15, 19, 24
    Canvas(
      modifier = Modifier
        .padding(start = 26.dp)
        .offset(y = (-1).dp)
        .size(width = 28.dp, height = 18.dp)
    ) {
      val fillPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, size.height)
        lineTo(size.width, 0f)
        close()
      }
      drawPath(fillPath, color = PureWhite)
      val strokePath = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, size.height)
        lineTo(size.width, 0f)
      }
      drawPath(strokePath, color = InkDark, style = Stroke(width = 3.5f))
    }
  }
}

@Composable
fun MaintenanceStatusBadge(status: MaintenanceStatus) {
  val (bg, fg) = when (status) {
    MaintenanceStatus.PENDING -> StatusPendingBg to StatusPendingText
    MaintenanceStatus.ON_GOING -> StatusOngoingBg to StatusOngoingText
    MaintenanceStatus.FIXED -> StatusFixedBg to StatusFixedText
  }
  Surface(color = bg, shape = RoundedCornerShape(50)) {
    Text(
      text = status.label,
      color = fg,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
    )
  }
}

@Composable
fun LostFoundStatusBadge(status: LostFoundStatus) {
  val (bg, fg) = when (status) {
    LostFoundStatus.ON_HOLD -> StatusPendingBg to StatusPendingText
    LostFoundStatus.CLAIMED -> StatusFixedBg to StatusFixedText
  }
  Surface(color = bg, shape = RoundedCornerShape(50)) {
    Text(
      text = status.label,
      color = fg,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
    )
  }
}

@Composable
fun ReportThumbnailBox(
  photoBase64: String?,
  fallbackLabel: String = "IMG",
  size: Dp = 112.dp,
  modifier: Modifier = Modifier,
) {
  val bitmap = remember(photoBase64) { ImagePickerHelper.decodeBase64ToImageBitmap(photoBase64) }
  Surface(
    color = PureWhite,
    shape = RoundedCornerShape(20.dp),
    shadowElevation = 2.dp,
    modifier = modifier.size(size),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
      if (bitmap != null) {
        Image(
          bitmap = bitmap,
          contentDescription = "Uploaded report image",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        )
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = fallbackLabel,
            tint = MutedInk.copy(alpha = 0.55f),
            modifier = Modifier.size(30.dp),
          )
          Text(
            text = fallbackLabel,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = InkDark,
          )
        }
      }
    }
  }
}

@Composable
fun BottomLeftBackBar(onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = onBack,
      modifier = Modifier
        .minimumInteractiveComponentSize()
        .testTag("screen_back_button"),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = RoyalNavy,
        modifier = Modifier.size(32.dp),
      )
    }
  }
}
