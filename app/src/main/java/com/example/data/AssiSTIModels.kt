package com.example.data

enum class PortalRole {
  STUDENT,
  ADMIN,
}

enum class MaintenanceStatus(val label: String) {
  PENDING("Pending"),
  ON_GOING("On going"),
  FIXED("Fixed");

  companion object {
    fun fromRaw(raw: String?): MaintenanceStatus =
      entries.firstOrNull { it.label == raw } ?: PENDING
  }
}

enum class LostFoundStatus(val label: String) {
  ON_HOLD("On hold"),
  CLAIMED("Claimed");

  companion object {
    fun fromRaw(raw: String?): LostFoundStatus =
      entries.firstOrNull { it.label == raw } ?: ON_HOLD
  }
}

enum class MaintenanceIssueOption(val title: String, val subtitle: String, val isOther: Boolean = false) {
  AIRCON("Aircon / HVAC", "ISSUE"),
  ELECTRICAL("Lights & Outlets", "ISSUE"),
  FURNITURE("Chairs & Desks", "ISSUE"),
  RESTROOM("Restroom & Water", "ISSUE"),
  LAB_EQUIPMENT("Projector & PC", "ISSUE"),
  OTHER("Other Issue", "OTHER", isOther = true),
}

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class UserSession(
  val userId: String,
  val displayName: String,
  val emailOrId: String,
  val activeRole: PortalRole,
  val isAdminAuthorized: Boolean,
)

data class UserProfile(
  val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val campus: String = "STI Sta. Mesa",
  val createdAtMillis: Long = 0L,
  val updatedAtMillis: Long = 0L,
)

data class Announcement(
  val id: String = "",
  val authorId: String = "",
  val authorName: String = "",
  val title: String = "",
  val message: String = "",
  val campusId: String = "sti_sta_mesa",
  val published: Boolean = true,
  val createdAtMillis: Long = 0L,
  val updatedAtMillis: Long = 0L,
)

data class MaintenanceReport(
  val id: String = "",
  val userId: String = "",
  val studentName: String = "",
  val issueCategory: String = "",
  val description: String = "",
  val photoBase64: String? = null,
  val status: String = MaintenanceStatus.PENDING.label,
  val campusId: String = "sti_sta_mesa",
  val createdAtMillis: Long = 0L,
  val updatedAtMillis: Long = 0L,
) {
  fun statusEnum(): MaintenanceStatus = MaintenanceStatus.fromRaw(status)
}

data class LostFoundItem(
  val id: String = "",
  val userId: String = "",
  val studentName: String = "",
  val description: String = "",
  val photoBase64: String? = null,
  val status: String = LostFoundStatus.ON_HOLD.label,
  val campusId: String = "sti_sta_mesa",
  val createdAtMillis: Long = 0L,
  val updatedAtMillis: Long = 0L,
) {
  fun statusEnum(): LostFoundStatus = LostFoundStatus.fromRaw(status)
}

data class CampusFeedback(
  val id: String = "",
  val userId: String = "",
  val studentName: String = "",
  val rating: Int = 5,
  val message: String = "",
  val createdAtMillis: Long = 0L,
)
