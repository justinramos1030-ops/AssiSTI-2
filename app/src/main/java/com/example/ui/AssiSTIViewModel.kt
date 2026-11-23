package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Announcement
import com.example.data.AssiSTIRepository
import com.example.data.LostFoundItem
import com.example.data.LostFoundStatus
import com.example.data.MaintenanceReport
import com.example.data.MaintenanceStatus
import com.example.data.PortalRole
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.data.UserSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AssiSTIViewModel(
  private val repository: AssiSTIRepository,
  session: UserSession,
) : ViewModel() {

  private val currentUserId = session.userId

  private val _portalRole = MutableStateFlow(session.activeRole)
  val portalRole: StateFlow<PortalRole> = _portalRole.asStateFlow()

  private val _isAdminAuthorized = MutableStateFlow(session.isAdminAuthorized)
  val isAdminAuthorized: StateFlow<Boolean> = _isAdminAuthorized.asStateFlow()

  private val _userProfile = MutableStateFlow(
    UserProfile(
      id = session.userId,
      userId = session.userId,
      displayName = session.displayName,
      email = session.emailOrId,
    )
  )
  val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _statusBanner = MutableStateFlow<String?>(null)
  val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

  val announcementsState: StateFlow<UiState<List<Announcement>>> =
    repository.observeAnnouncements()
      .map<List<Announcement>, UiState<List<Announcement>>> { UiState.Success(it) }
      .catch { error ->
        if (error is CancellationException) throw error
        Log.w(TAG, "Failed to load announcements", error)
        emit(UiState.Error(error.localizedMessage ?: "Failed to load announcements"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  val maintenanceState: StateFlow<UiState<List<MaintenanceReport>>> =
    combine(portalRole, isAdminAuthorized) { role, isAdmin ->
      role == PortalRole.ADMIN && isAdmin
    }.flatMapLatest { isAdminQuery ->
      repository.observeMaintenanceReports(userId = currentUserId, isAdminView = isAdminQuery)
        .map<List<MaintenanceReport>, UiState<List<MaintenanceReport>>> { UiState.Success(it) }
        .catch { error ->
          if (error is CancellationException) throw error
          Log.w(TAG, "Failed to load maintenance reports", error)
          emit(UiState.Error(error.localizedMessage ?: "Failed to load maintenance reports"))
        }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = UiState.Loading,
    )

  val lostFoundState: StateFlow<UiState<List<LostFoundItem>>> =
    repository.observeLostFoundItems()
      .map<List<LostFoundItem>, UiState<List<LostFoundItem>>> { UiState.Success(it) }
      .catch { error ->
        if (error is CancellationException) throw error
        Log.w(TAG, "Failed to load lost & found items", error)
        emit(UiState.Error(error.localizedMessage ?: "Failed to load lost & found items"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading,
      )

  fun switchRole(target: PortalRole) {
    if (target == PortalRole.ADMIN && !_isAdminAuthorized.value) {
      _statusBanner.value = "Admin Portal requires an authorized Admin ID login."
      return
    }
    _portalRole.value = target
    repository.updateStoredRole(target)
  }

  fun clearBanner() {
    _statusBanner.value = null
  }

  fun submitMaintenanceReport(
    issueCategory: String,
    description: String,
    photoBase64: String?,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.submitMaintenanceReport(
        userId = currentUserId,
        studentName = _userProfile.value.displayName,
        issueCategory = issueCategory,
        description = description,
        photoBase64 = photoBase64,
      )
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Maintenance report submitted!"
          onSuccess()
        },
        onFailure = { error ->
          _statusBanner.value = error.localizedMessage ?: "Failed to submit report."
        },
      )
    }
  }

  fun updateMaintenanceStatus(reportId: String, status: MaintenanceStatus) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.updateMaintenanceStatus(reportId, status)
      _isBusy.value = false
      result.fold(
        onSuccess = { _statusBanner.value = "Report status updated to ${status.label}." },
        onFailure = { error -> _statusBanner.value = error.localizedMessage ?: "Unable to update status." },
      )
    }
  }

  fun deleteMaintenanceReport(reportId: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.deleteMaintenanceReport(reportId)
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Report removed."
          onSuccess()
        },
        onFailure = { error -> _statusBanner.value = error.localizedMessage ?: "Unable to delete report." },
      )
    }
  }

  fun submitLostFoundItem(
    description: String,
    photoBase64: String?,
    onSuccess: () -> Unit,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.submitLostFoundItem(
        userId = currentUserId,
        studentName = _userProfile.value.displayName,
        description = description,
        photoBase64 = photoBase64,
      )
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Lost & Found report posted!"
          onSuccess()
        },
        onFailure = { error ->
          _statusBanner.value = error.localizedMessage ?: "Failed to submit item."
        },
      )
    }
  }

  fun updateLostFoundStatus(itemId: String, status: LostFoundStatus) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.updateLostFoundStatus(itemId, status)
      _isBusy.value = false
      result.fold(
        onSuccess = { _statusBanner.value = "Item status updated to ${status.label}." },
        onFailure = { error -> _statusBanner.value = error.localizedMessage ?: "Unable to update item status." },
      )
    }
  }

  fun deleteLostFoundItem(itemId: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.deleteLostFoundItem(itemId)
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Item removed."
          onSuccess()
        },
        onFailure = { error -> _statusBanner.value = error.localizedMessage ?: "Unable to remove item." },
      )
    }
  }

  fun postAnnouncement(title: String, message: String, onSuccess: () -> Unit = {}) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.createAnnouncement(
        authorId = currentUserId,
        authorName = _userProfile.value.displayName,
        title = title,
        message = message,
      )
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Announcement published to campus feed!"
          onSuccess()
        },
        onFailure = { error ->
          _statusBanner.value = error.localizedMessage ?: "Failed to post announcement."
        },
      )
    }
  }

  fun deleteAnnouncement(announcementId: String) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.deleteAnnouncement(announcementId)
      _isBusy.value = false
      result.fold(
        onSuccess = { _statusBanner.value = "Announcement deleted." },
        onFailure = { error -> _statusBanner.value = error.localizedMessage ?: "Unable to delete announcement." },
      )
    }
  }

  fun submitFeedback(rating: Int, message: String, onSuccess: () -> Unit) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.submitFeedback(
        userId = currentUserId,
        studentName = _userProfile.value.displayName,
        rating = rating,
        message = message,
      )
      _isBusy.value = false
      result.fold(
        onSuccess = {
          _statusBanner.value = "Thank you for your feedback!"
          onSuccess()
        },
        onFailure = { error ->
          _statusBanner.value = error.localizedMessage ?: "Failed to submit feedback."
        },
      )
    }
  }

  private companion object {
    const val TAG = "AssiSTIVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
