package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LostFoundItem
import com.example.data.LostFoundStatus
import com.example.data.MaintenanceIssueOption
import com.example.data.MaintenanceReport
import com.example.data.MaintenanceStatus
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.ui.components.AssiSTIBottomNav
import com.example.ui.components.AssiSTITopBar
import com.example.ui.components.BottomNavTab
import com.example.ui.components.LostFoundStatusBadge
import com.example.ui.components.MaintenanceStatusBadge
import com.example.ui.components.ReportCategoryTabs
import com.example.ui.components.ReportThumbnailBox
import com.example.ui.components.SpeechBubbleCard
import com.example.ui.theme.CampusBlue
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.InkDark
import com.example.ui.theme.MutedInk
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalNavy
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SoftIceBg
import com.example.ui.util.ImagePickerHelper
import kotlinx.coroutines.launch

@Composable
fun MaintenanceCategoryScreen(
  onSelectIssue: (MaintenanceIssueOption) -> Unit,
  onAnnouncementsClick: () -> Unit,
  onReportsClick: () -> Unit,
  onBack: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenFeedback: () -> Unit,
  onLogOut: () -> Unit,
) {
  BackHandler(onBack = onBack)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg),
  ) {
    AssiSTITopBar(
      showDropdownArrow = false,
      portalRole = PortalRole.STUDENT,
      onLogoClick = onAnnouncementsClick,
      onHistoryClick = onOpenHistory,
      onFeedbackClick = onOpenFeedback,
      onLogOutClick = onLogOut,
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      contentAlignment = Alignment.Center,
    ) {
      // Canva Page 9: Dark navy container with staggered 2-column issue tiles
      Surface(
        color = RoyalNavy,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 560.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
          val rows = listOf(
            MaintenanceIssueOption.AIRCON to MaintenanceIssueOption.ELECTRICAL,
            MaintenanceIssueOption.FURNITURE to MaintenanceIssueOption.RESTROOM,
            MaintenanceIssueOption.LAB_EQUIPMENT to MaintenanceIssueOption.OTHER,
          )
          rows.forEach { (leftOption, rightOption) ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
              IssueCategoryTile(
                option = leftOption,
                onClick = { onSelectIssue(leftOption) },
                modifier = Modifier.weight(1f),
              )
              IssueCategoryTile(
                option = rightOption,
                onClick = { onSelectIssue(rightOption) },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }

    AssiSTIBottomNav(
      activeTab = BottomNavTab.CREATE,
      showCenterPlus = true,
      onAnnouncementsClick = onAnnouncementsClick,
      onCreateClick = onBack,
      onReportsClick = onReportsClick,
    )
  }
}

@Composable
private fun IssueCategoryTile(
  option: MaintenanceIssueOption,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    color = SoftIceBg,
    shape = RoundedCornerShape(22.dp),
    shadowElevation = 6.dp,
    modifier = modifier
      .height(112.dp)
      .minimumInteractiveComponentSize()
      .testTag("issue_tile_${option.name.lowercase()}")
      .clip(RoundedCornerShape(22.dp))
      .clickable(onClick = onClick),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = option.subtitle,
        fontFamily = PlusJakartaSansFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        color = RoyalNavy,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = option.title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = RoyalNavy.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun OtherIssueDescriptionScreen(
  initialDescription: String,
  onNext: (String) -> Unit,
  onAnnouncementsClick: () -> Unit,
  onReportsClick: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var description by remember { mutableStateOf(initialDescription) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg),
  ) {
    AssiSTITopBar(
      showDropdownArrow = false,
      onLogoClick = onAnnouncementsClick,
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center,
    ) {
      // Canva Page 11: Slate card with OTHER: heading, white bordered Description box, Next button
      Surface(
        color = SlateCard,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp, vertical = 36.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = "OTHER:",
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = RoyalNavy,
          )
          Spacer(Modifier.height(22.dp))
          OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 1000) description = it },
            placeholder = {
              Text("Description", color = MutedInk.copy(alpha = 0.7f))
            },
            minLines = 2,
            maxLines = 5,
            shape = RoundedCornerShape(6.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite,
              focusedBorderColor = InkDark,
              unfocusedBorderColor = InkDark,
              focusedTextColor = InkDark,
              unfocusedTextColor = InkDark,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("other_issue_description_input"),
          )
          Spacer(Modifier.height(26.dp))
          Button(
            onClick = { onNext(description) },
            enabled = description.trim().isNotEmpty(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
              containerColor = CampusBlue,
              contentColor = RoyalNavy,
            ),
            modifier = Modifier
              .width(150.dp)
              .height(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("other_issue_next_button"),
          ) {
            Text(
              text = "Next",
              fontFamily = PlusJakartaSansFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
            )
          }
        }
      }
    }

    AssiSTIBottomNav(
      activeTab = BottomNavTab.CREATE,
      showCenterPlus = true,
      onAnnouncementsClick = onAnnouncementsClick,
      onCreateClick = onBack,
      onReportsClick = onReportsClick,
    )
  }
}

@Composable
fun ReportSubmissionScreen(
  isLostAndFound: Boolean,
  issueCategory: String,
  initialDescription: String = "",
  isSubmitting: Boolean,
  onSubmit: (description: String, photoBase64: String?) -> Unit,
  onAnnouncementsClick: () -> Unit,
  onReportsClick: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var description by remember { mutableStateOf(initialDescription) }
  var photoBase64 by remember { mutableStateOf<String?>(null) }
  var isEncodingPhoto by remember { mutableStateOf(false) }
  var photoError by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // Zero-permission Android Photo Picker per Google Play policy
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      isEncodingPhoto = true
      photoError = null
      scope.launch {
        val encoded = ImagePickerHelper.encodeUriToBase64(context, uri)
        if (encoded == null) {
          photoError = "Could not load selected image. Please try another photo."
        } else {
          photoBase64 = encoded
        }
        isEncodingPhoto = false
      }
    }
  }

  val previewBitmap = remember(photoBase64) { ImagePickerHelper.decodeBase64ToImageBitmap(photoBase64) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg),
  ) {
    AssiSTITopBar(
      showDropdownArrow = false,
      onLogoClick = onAnnouncementsClick,
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 20.dp),
      contentAlignment = Alignment.Center,
    ) {
      // Canva Pages 10 & 13: Slate card with + Upload Image box, Description input, Done button
      Surface(
        color = SlateCard,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = if (isLostAndFound) "Lost & Found" else issueCategory,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = PureWhite,
          )
          Spacer(Modifier.height(18.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Surface(
              color = PureWhite,
              shape = RoundedCornerShape(22.dp),
              border = BorderStroke(1.5.dp, InkDark),
              modifier = Modifier
                .size(136.dp)
                .minimumInteractiveComponentSize()
                .testTag("upload_image_box")
                .clip(RoundedCornerShape(22.dp))
                .clickable {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
            ) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                when {
                  isEncodingPhoto -> CircularProgressIndicator(color = RoyalNavy)
                  previewBitmap != null -> Image(
                    bitmap = previewBitmap,
                    contentDescription = "Selected photo preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                  )
                  else -> Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Upload Image",
                    tint = Color(0xFF3E312B),
                    modifier = Modifier.size(64.dp),
                  )
                }
              }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                text = if (photoBase64 == null) "Upload Image" else "Change Image",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = RoyalNavy.copy(alpha = 0.85f),
                modifier = Modifier.clickable {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
              )
              if (photoBase64 != null) {
                TextButton(
                  onClick = { photoBase64 = null },
                  contentPadding = PaddingValues(0.dp),
                ) {
                  Text("Remove photo", color = PureWhite, fontSize = 13.sp)
                }
              }
            }
          }

          if (photoError != null) {
            Spacer(Modifier.height(10.dp))
            Text(
              text = photoError ?: "",
              color = ErrorRed,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
            )
          }

          Spacer(Modifier.height(28.dp))

          OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= 1000) description = it },
            placeholder = {
              Text("Description", color = MutedInk.copy(alpha = 0.7f))
            },
            minLines = 2,
            maxLines = 5,
            shape = if (isLostAndFound) RoundedCornerShape(6.dp) else RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = PureWhite,
              unfocusedContainerColor = PureWhite,
              focusedBorderColor = InkDark,
              unfocusedBorderColor = InkDark,
              focusedTextColor = InkDark,
              unfocusedTextColor = InkDark,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("report_description_input"),
          )

          Spacer(Modifier.height(34.dp))

          Button(
            onClick = { onSubmit(description, photoBase64) },
            enabled = !isSubmitting && !isEncodingPhoto && description.trim().isNotEmpty(),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
              containerColor = CampusBlue,
              contentColor = RoyalNavy,
            ),
            modifier = Modifier
              .width(150.dp)
              .height(48.dp)
              .minimumInteractiveComponentSize()
              .testTag("report_done_button"),
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = RoyalNavy,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text = "Done",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
            }
          }
        }
      }
    }

    AssiSTIBottomNav(
      activeTab = BottomNavTab.CREATE,
      showCenterPlus = true,
      onAnnouncementsClick = onAnnouncementsClick,
      onCreateClick = onBack,
      onReportsClick = onReportsClick,
    )
  }
}

@Composable
fun StudentReportsListScreen(
  currentUserId: String,
  initialTab: Int = 0,
  maintenanceState: UiState<List<MaintenanceReport>>,
  lostFoundState: UiState<List<LostFoundItem>>,
  onDeleteMaintenance: (String) -> Unit,
  onDeleteLostFound: (String) -> Unit,
  onAnnouncementsClick: () -> Unit,
  onCreateClick: () -> Unit,
  onOpenHistory: () -> Unit,
  onOpenFeedback: () -> Unit,
  onLogOut: () -> Unit,
  onBack: () -> Unit,
) {
  BackHandler(onBack = onBack)

  var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
  var selectedMaint by remember { mutableStateOf<MaintenanceReport?>(null) }
  var selectedLost by remember { mutableStateOf<LostFoundItem?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(SoftIceBg),
  ) {
    AssiSTITopBar(
      showDropdownArrow = false,
      portalRole = PortalRole.STUDENT,
      onLogoClick = onAnnouncementsClick,
      onHistoryClick = onOpenHistory,
      onFeedbackClick = onOpenFeedback,
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
        // Canva Page 14: Student Maintenance IMG cards
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
                Text(
                  text = "No maintenance reports yet.\nTap + below to report a campus issue.",
                  color = MutedInk,
                  textAlign = TextAlign.Center,
                )
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
                      .clickable { selectedMaint = report },
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
                        fontSize = 16.sp,
                        color = RoyalNavy,
                      )
                      Text(
                        text = report.description,
                        fontSize = 14.sp,
                        color = InkDark,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        MaintenanceStatusBadge(report.statusEnum())
                        if (report.userId == currentUserId && report.statusEnum() == MaintenanceStatus.PENDING) {
                          IconButton(
                            onClick = { onDeleteMaintenance(report.id) },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                          ) {
                            Icon(
                              imageVector = Icons.Default.DeleteOutline,
                              contentDescription = "Delete pending report",
                              tint = ErrorRed,
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
      } else {
        // Canva Page 15: Student Lost & Found Speech-Bubble REPORT cards
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
                Text(
                  text = "No lost & found reports yet.\nTap + below to post a found or lost item.",
                  color = MutedInk,
                  textAlign = TextAlign.Center,
                )
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
                  SpeechBubbleCard(
                    onClick = { selectedLost = item },
                    modifier = Modifier.fillMaxWidth(0.86f),
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                      if (!item.photoBase64.isNullOrBlank()) {
                        ReportThumbnailBox(
                          photoBase64 = item.photoBase64,
                          fallbackLabel = "ITEM",
                          size = 64.dp,
                        )
                      }
                      Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        Text(
                          text = "REPORT",
                          fontFamily = PlusJakartaSansFamily,
                          fontWeight = FontWeight.ExtraBold,
                          fontSize = 18.sp,
                          color = InkDark,
                        )
                        Text(
                          text = item.description,
                          fontSize = 14.sp,
                          color = InkDark,
                          maxLines = 3,
                          overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.SpaceBetween,
                          modifier = Modifier.fillMaxWidth(),
                        ) {
                          LostFoundStatusBadge(item.statusEnum())
                          if (item.userId == currentUserId && item.statusEnum() == LostFoundStatus.ON_HOLD) {
                            IconButton(
                              onClick = { onDeleteLostFound(item.id) },
                              modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                              Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete item report",
                                tint = ErrorRed,
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
        }
      }
    }

    AssiSTIBottomNav(
      activeTab = BottomNavTab.REPORTS,
      showCenterPlus = true,
      onAnnouncementsClick = onAnnouncementsClick,
      onCreateClick = onCreateClick,
      onReportsClick = {},
    )
  }

  selectedMaint?.let { report ->
    AlertDialog(
      onDismissRequest = { selectedMaint = null },
      confirmButton = { TextButton(onClick = { selectedMaint = null }) { Text("Close") } },
      title = { Text(report.issueCategory, fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!report.photoBase64.isNullOrBlank()) {
            ReportThumbnailBox(report.photoBase64, size = 180.dp)
          }
          Text("Reported by: ${report.studentName}", fontWeight = FontWeight.SemiBold)
          Text(report.description)
          MaintenanceStatusBadge(report.statusEnum())
        }
      },
    )
  }

  selectedLost?.let { item ->
    AlertDialog(
      onDismissRequest = { selectedLost = null },
      confirmButton = { TextButton(onClick = { selectedLost = null }) { Text("Close") } },
      title = { Text("Lost & Found Report", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!item.photoBase64.isNullOrBlank()) {
            ReportThumbnailBox(item.photoBase64, fallbackLabel = "ITEM", size = 180.dp)
          }
          Text("Reported by: ${item.studentName}", fontWeight = FontWeight.SemiBold)
          Text(item.description)
          LostFoundStatusBadge(item.statusEnum())
        }
      },
    )
  }
}
