package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AssiSTIRepository
import com.example.data.MaintenanceIssueOption
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.data.UserSession
import com.example.ui.AssiSTIViewModel
import com.example.ui.screens.AdminAnnouncementsScreen
import com.example.ui.screens.AdminLostFoundDetailScreen
import com.example.ui.screens.AdminMaintenanceDetailScreen
import com.example.ui.screens.AdminReportsListScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.FeedbackDialog
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MaintenanceCategoryScreen
import com.example.ui.screens.OtherIssueDescriptionScreen
import com.example.ui.screens.ReportSubmissionScreen
import com.example.ui.screens.StudentHistoryScreen
import com.example.ui.screens.StudentReportsListScreen
import com.example.ui.theme.AssiSTITheme
import com.example.ui.theme.RoyalNavy
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private object AssiSTIRoutes {
  const val HOME = "home"
  const val HISTORY = "history"
  const val MAINTENANCE_CATEGORIES = "maintenance_categories"
  const val MAINTENANCE_OTHER = "maintenance_other"
  const val MAINTENANCE_SUBMIT = "maintenance_submit/{category}/{draft}"
  const val LOST_FOUND_SUBMIT = "lost_found_submit"
  const val STUDENT_REPORTS = "student_reports/{tab}"
  const val ADMIN_ANNOUNCEMENTS = "admin_announcements"
  const val ADMIN_REPORTS = "admin_reports"
  const val ADMIN_MAINTENANCE_DETAIL = "admin_maintenance_detail/{reportId}"
  const val ADMIN_LOST_FOUND_DETAIL = "admin_lost_found_detail/{itemId}"

  fun maintenanceSubmit(category: String, draft: String = ""): String {
    val encCat = URLEncoder.encode(category, StandardCharsets.UTF_8.name())
    val encDraft = URLEncoder.encode(draft.ifEmpty { "_" }, StandardCharsets.UTF_8.name())
    return "maintenance_submit/$encCat/$encDraft"
  }

  fun studentReports(tab: Int = 0) = "student_reports/$tab"
  fun adminMaintenanceDetail(reportId: String) = "admin_maintenance_detail/$reportId"
  fun adminLostFoundDetail(itemId: String) = "admin_lost_found_detail/$itemId"
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      AssiSTITheme {
        AppContent()
      }
    }
  }
}

@Composable
fun AppContent() {
  val context = LocalContext.current
  val repository = remember(context) { AssiSTIRepository.getInstance(context) }
  var isInitialized by remember { mutableStateOf(false) }

  LaunchedEffect(repository) {
    repository.initializeSession()
    isInitialized = true
  }

  val session by repository.sessionFlow.collectAsStateWithLifecycle()

  if (!isInitialized) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = RoyalNavy)
    }
    return
  }

  val activeSession = session
  if (activeSession == null) {
    AuthScreen(
      onStudentLogin = { email, password ->
        repository.signInStudent(email, password).map { }
      },
      onAdminLogin = { adminId, password ->
        repository.signInAdmin(adminId, password).map { }
      },
    )
  } else {
    key(activeSession.userId) {
      val viewModel: AssiSTIViewModel = viewModel(
        key = activeSession.userId,
        factory = viewModelFactory {
          initializer {
            AssiSTIViewModel(
              repository = repository,
              session = activeSession,
            )
          }
        },
      )

      AuthenticatedAssiSTIApp(
        session = activeSession,
        viewModel = viewModel,
        onSignOut = { repository.signOut() },
      )
    }
  }
}

@Composable
private fun AuthenticatedAssiSTIApp(
  session: UserSession,
  viewModel: AssiSTIViewModel,
  onSignOut: () -> Unit,
) {
  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }

  val portalRole by viewModel.portalRole.collectAsStateWithLifecycle()
  val isAdminAuthorized by viewModel.isAdminAuthorized.collectAsStateWithLifecycle()
  val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
  val announcementsState by viewModel.announcementsState.collectAsStateWithLifecycle()
  val maintenanceState by viewModel.maintenanceState.collectAsStateWithLifecycle()
  val lostFoundState by viewModel.lostFoundState.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
  val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

  var showFeedbackDialog by remember { mutableStateOf(false) }

  LaunchedEffect(statusBanner) {
    val msg = statusBanner
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearBanner()
    }
  }

  Scaffold(
    snackbarHost = {
      SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.navigationBarsPadding(),
      )
    },
  ) { _ ->
    Box(modifier = Modifier.fillMaxSize()) {
      NavHost(
        navController = navController,
        startDestination = AssiSTIRoutes.HOME,
      ) {
        composable(AssiSTIRoutes.HOME) {
          HomeDashboardScreen(
            portalRole = portalRole,
            isAdminAuthorized = isAdminAuthorized,
            announcementsState = announcementsState,
            onOpenHistory = { navController.navigate(AssiSTIRoutes.HISTORY) },
            onOpenFeedback = { showFeedbackDialog = true },
            onSwitchRole = {
              val nextRole = if (portalRole == PortalRole.ADMIN) PortalRole.STUDENT else PortalRole.ADMIN
              viewModel.switchRole(nextRole)
            },
            onLogOut = onSignOut,
            onAnnouncementsTabClick = {
              if (portalRole == PortalRole.ADMIN) {
                navController.navigate(AssiSTIRoutes.ADMIN_ANNOUNCEMENTS) {
                  launchSingleTop = true
                }
              }
            },
            onSelectMaintenanceCreate = {
              navController.navigate(AssiSTIRoutes.MAINTENANCE_CATEGORIES)
            },
            onSelectLostFoundCreate = {
              navController.navigate(AssiSTIRoutes.LOST_FOUND_SUBMIT)
            },
            onReportsTabClick = {
              if (portalRole == PortalRole.ADMIN) {
                navController.navigate(AssiSTIRoutes.ADMIN_REPORTS) {
                  launchSingleTop = true
                }
              } else {
                navController.navigate(AssiSTIRoutes.studentReports(0)) {
                  launchSingleTop = true
                }
              }
            },
          )
        }

        composable(AssiSTIRoutes.HISTORY) {
          StudentHistoryScreen(
            userProfile = userProfile,
            maintenanceState = maintenanceState,
            lostFoundState = lostFoundState,
            onHomeClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onBack = { navController.popBackStack() },
          )
        }

        composable(AssiSTIRoutes.MAINTENANCE_CATEGORIES) {
          MaintenanceCategoryScreen(
            onSelectIssue = { option ->
              if (option == MaintenanceIssueOption.OTHER) {
                navController.navigate(AssiSTIRoutes.MAINTENANCE_OTHER)
              } else {
                navController.navigate(AssiSTIRoutes.maintenanceSubmit(option.title))
              }
            },
            onAnnouncementsClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onReportsClick = {
              navController.navigate(AssiSTIRoutes.studentReports(0)) {
                popUpTo(AssiSTIRoutes.HOME)
              }
            },
            onBack = { navController.popBackStack() },
            onOpenHistory = { navController.navigate(AssiSTIRoutes.HISTORY) },
            onOpenFeedback = { showFeedbackDialog = true },
            onLogOut = onSignOut,
          )
        }

        composable(AssiSTIRoutes.MAINTENANCE_OTHER) {
          OtherIssueDescriptionScreen(
            initialDescription = "",
            onNext = { desc ->
              navController.navigate(AssiSTIRoutes.maintenanceSubmit("Other", desc))
            },
            onAnnouncementsClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onReportsClick = {
              navController.navigate(AssiSTIRoutes.studentReports(0)) {
                popUpTo(AssiSTIRoutes.HOME)
              }
            },
            onBack = { navController.popBackStack() },
          )
        }

        composable(
          route = AssiSTIRoutes.MAINTENANCE_SUBMIT,
          arguments = listOf(
            navArgument("category") { type = NavType.StringType },
            navArgument("draft") { type = NavType.StringType },
          ),
        ) { backStackEntry ->
          val rawCat = backStackEntry.arguments?.getString("category") ?: "Maintenance"
          val rawDraft = backStackEntry.arguments?.getString("draft") ?: "_"
          val category = URLDecoder.decode(rawCat, StandardCharsets.UTF_8.name())
          val draft = URLDecoder.decode(rawDraft, StandardCharsets.UTF_8.name()).let {
            if (it == "_") "" else it
          }

          ReportSubmissionScreen(
            isLostAndFound = false,
            issueCategory = category,
            initialDescription = draft,
            isSubmitting = isBusy,
            onSubmit = { desc, photoBase64 ->
              viewModel.submitMaintenanceReport(
                issueCategory = category,
                description = desc,
                photoBase64 = photoBase64,
                onSuccess = {
                  navController.navigate(AssiSTIRoutes.studentReports(0)) {
                    popUpTo(AssiSTIRoutes.HOME)
                  }
                },
              )
            },
            onAnnouncementsClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onReportsClick = {
              navController.navigate(AssiSTIRoutes.studentReports(0)) {
                popUpTo(AssiSTIRoutes.HOME)
              }
            },
            onBack = { navController.popBackStack() },
          )
        }

        composable(AssiSTIRoutes.LOST_FOUND_SUBMIT) {
          ReportSubmissionScreen(
            isLostAndFound = true,
            issueCategory = "Lost & Found",
            isSubmitting = isBusy,
            onSubmit = { desc, photoBase64 ->
              viewModel.submitLostFoundItem(
                description = desc,
                photoBase64 = photoBase64,
                onSuccess = {
                  navController.navigate(AssiSTIRoutes.studentReports(1)) {
                    popUpTo(AssiSTIRoutes.HOME)
                  }
                },
              )
            },
            onAnnouncementsClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onReportsClick = {
              navController.navigate(AssiSTIRoutes.studentReports(1)) {
                popUpTo(AssiSTIRoutes.HOME)
              }
            },
            onBack = { navController.popBackStack() },
          )
        }

        composable(
          route = AssiSTIRoutes.STUDENT_REPORTS,
          arguments = listOf(navArgument("tab") { type = NavType.IntType }),
        ) { backStackEntry ->
          val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
          StudentReportsListScreen(
            currentUserId = session.userId,
            initialTab = initialTab,
            maintenanceState = maintenanceState,
            lostFoundState = lostFoundState,
            onDeleteMaintenance = { viewModel.deleteMaintenanceReport(it) },
            onDeleteLostFound = { viewModel.deleteLostFoundItem(it) },
            onAnnouncementsClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onCreateClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onOpenHistory = { navController.navigate(AssiSTIRoutes.HISTORY) },
            onOpenFeedback = { showFeedbackDialog = true },
            onLogOut = onSignOut,
            onBack = { navController.popBackStack() },
          )
        }

        composable(AssiSTIRoutes.ADMIN_ANNOUNCEMENTS) {
          AdminAnnouncementsScreen(
            announcementsState = announcementsState,
            isBusy = isBusy,
            onPublishAnnouncement = { title, msg ->
              viewModel.postAnnouncement(title, msg)
            },
            onDeleteAnnouncement = { viewModel.deleteAnnouncement(it) },
            onHomeClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onSwitchToStudentView = {
              viewModel.switchRole(PortalRole.STUDENT)
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onLogOut = onSignOut,
            onBack = { navController.popBackStack() },
          )
        }

        composable(AssiSTIRoutes.ADMIN_REPORTS) {
          AdminReportsListScreen(
            maintenanceState = maintenanceState,
            lostFoundState = lostFoundState,
            onInspectMaintenance = { report ->
              navController.navigate(AssiSTIRoutes.adminMaintenanceDetail(report.id))
            },
            onInspectLostFound = { item ->
              navController.navigate(AssiSTIRoutes.adminLostFoundDetail(item.id))
            },
            onHomeClick = {
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onAnnouncementsClick = {
              navController.navigate(AssiSTIRoutes.ADMIN_ANNOUNCEMENTS) {
                launchSingleTop = true
              }
            },
            onSwitchToStudentView = {
              viewModel.switchRole(PortalRole.STUDENT)
              navController.popBackStack(AssiSTIRoutes.HOME, inclusive = false)
            },
            onLogOut = onSignOut,
            onBack = { navController.popBackStack() },
          )
        }

        composable(
          route = AssiSTIRoutes.ADMIN_MAINTENANCE_DETAIL,
          arguments = listOf(navArgument("reportId") { type = NavType.StringType }),
        ) { backStackEntry ->
          val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
          when (val state = maintenanceState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = RoyalNavy)
            }
            is UiState.Error -> LaunchedEffect(Unit) { navController.popBackStack() }
            is UiState.Success -> {
              val report = state.data.firstOrNull { it.id == reportId }
              if (report != null) {
                AdminMaintenanceDetailScreen(
                  report = report,
                  onUpdateStatus = { newStatus ->
                    viewModel.updateMaintenanceStatus(report.id, newStatus)
                  },
                  onBack = { navController.popBackStack() },
                )
              } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
              }
            }
          }
        }

        composable(
          route = AssiSTIRoutes.ADMIN_LOST_FOUND_DETAIL,
          arguments = listOf(navArgument("itemId") { type = NavType.StringType }),
        ) { backStackEntry ->
          val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
          when (val state = lostFoundState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(color = RoyalNavy)
            }
            is UiState.Error -> LaunchedEffect(Unit) { navController.popBackStack() }
            is UiState.Success -> {
              val item = state.data.firstOrNull { it.id == itemId }
              if (item != null) {
                AdminLostFoundDetailScreen(
                  item = item,
                  onUpdateStatus = { newStatus ->
                    viewModel.updateLostFoundStatus(item.id, newStatus)
                  },
                  onBack = { navController.popBackStack() },
                )
              } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
              }
            }
          }
        }
      }

      if (showFeedbackDialog) {
        FeedbackDialog(
          isSubmitting = isBusy,
          onDismiss = { showFeedbackDialog = false },
          onSubmit = { rating, message ->
            viewModel.submitFeedback(rating, message) {
              showFeedbackDialog = false
            }
          },
        )
      }
    }
  }
}
