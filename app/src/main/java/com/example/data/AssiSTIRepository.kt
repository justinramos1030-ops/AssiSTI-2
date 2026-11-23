package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID

class AssiSTIRepository(
  private val dao: AssiSTIDao,
  private val prefs: SharedPreferences,
) {
  private val seedMutex = Mutex()

  private val _sessionFlow = MutableStateFlow<UserSession?>(null)
  val sessionFlow: StateFlow<UserSession?> = _sessionFlow.asStateFlow()

  suspend fun initializeSession(): UserSession? = withContext(Dispatchers.IO) {
    seedInitialDataIfNeeded()
    val savedUid = prefs.getString(KEY_USER_ID, null) ?: return@withContext null
    val user = dao.findUserById(savedUid)
    if (user == null) {
      prefs.edit().remove(KEY_USER_ID).remove(KEY_ACTIVE_ROLE).apply()
      _sessionFlow.value = null
      return@withContext null
    }
    val isAdmin = user.role == ROLE_ADMIN
    val savedRoleName = prefs.getString(KEY_ACTIVE_ROLE, PortalRole.STUDENT.name)
    val activeRole = if (isAdmin && savedRoleName == PortalRole.ADMIN.name) {
      PortalRole.ADMIN
    } else {
      PortalRole.STUDENT
    }
    val session = UserSession(
      userId = user.id,
      displayName = user.displayName,
      emailOrId = user.email,
      activeRole = activeRole,
      isAdminAuthorized = isAdmin,
    )
    _sessionFlow.value = session
    session
  }

  suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
    seedMutex.withLock {
      if (!prefs.getBoolean(KEY_CLEANED_SAMPLE_REPORTS_V1, false)) {
        dao.deleteSeededMaintenanceReports()
        dao.deleteSeededLostFoundItems()
        prefs.edit().putBoolean(KEY_CLEANED_SAMPLE_REPORTS_V1, true).apply()
      }
      if (!prefs.getBoolean(KEY_CLEANED_SAMPLE_ANNOUNCEMENTS_V1, false)) {
        dao.deleteSeededAnnouncements()
        prefs.edit().putBoolean(KEY_CLEANED_SAMPLE_ANNOUNCEMENTS_V1, true).apply()
      }
      if (prefs.getBoolean(KEY_SEEDED_V1, false) && dao.countUsers() > 0) return@withLock
      val now = System.currentTimeMillis()

      val adminSalt1 = generateSalt()
      val adminSalt2 = generateSalt()
      val studentSalt = generateSalt()

      dao.insertUsersIgnore(
        listOf(
          UserEntity(
            id = "admin_default",
            loginKey = "admin",
            displayName = "STI Sta. Mesa Admin",
            email = "admin@sti.edu.ph",
            role = ROLE_ADMIN,
            passwordHash = hashPassword("admin123", adminSalt1),
            salt = adminSalt1,
            campus = CAMPUS_DISPLAY_NAME,
            createdAtMillis = now,
            updatedAtMillis = now,
          ),
          UserEntity(
            id = "admin_sti",
            loginKey = "sti_admin",
            displayName = "Campus Facilities Admin",
            email = "facilities@sti.edu.ph",
            role = ROLE_ADMIN,
            passwordHash = hashPassword("admin123", adminSalt2),
            salt = adminSalt2,
            campus = CAMPUS_DISPLAY_NAME,
            createdAtMillis = now,
            updatedAtMillis = now,
          ),
          UserEntity(
            id = "student_demo",
            loginKey = "student@sti.edu.ph",
            displayName = "STI Student",
            email = "student@sti.edu.ph",
            role = ROLE_STUDENT,
            passwordHash = hashPassword("student123", studentSalt),
            salt = studentSalt,
            campus = CAMPUS_DISPLAY_NAME,
            createdAtMillis = now,
            updatedAtMillis = now,
          ),
        )
      )

      prefs.edit().putBoolean(KEY_SEEDED_V1, true).apply()
    }
  }

  suspend fun signInStudent(email: String, password: String): Result<UserSession> =
    withContext(Dispatchers.IO) {
      try {
        seedInitialDataIfNeeded()
        val cleanEmail = email.trim()
        require(cleanEmail.isNotEmpty() && cleanEmail.contains("@") && cleanEmail.substringAfter("@").contains(".")) {
          "Please enter a valid Microsoft / STI email address (e.g. name@sti.edu.ph)."
        }
        require(password.isNotBlank()) { "Please enter your password." }

        val loginKey = cleanEmail.lowercase(Locale.ROOT)
        val existing = dao.findUserByLoginKey(loginKey)
        val user = if (existing != null) {
          val expected = hashPassword(password, existing.salt)
          if (expected != existing.passwordHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password for $cleanEmail."))
          }
          existing
        } else {
          val now = System.currentTimeMillis()
          val salt = generateSalt()
          val derivedName = deriveDisplayName(cleanEmail)
          val created = UserEntity(
            id = "stu_${UUID.randomUUID().toString().replace("-", "").take(16)}",
            loginKey = loginKey,
            displayName = derivedName,
            email = cleanEmail,
            role = ROLE_STUDENT,
            passwordHash = hashPassword(password, salt),
            salt = salt,
            campus = CAMPUS_DISPLAY_NAME,
            createdAtMillis = now,
            updatedAtMillis = now,
          )
          dao.insertUser(created)
          created
        }

        val session = UserSession(
          userId = user.id,
          displayName = user.displayName,
          emailOrId = user.email,
          activeRole = PortalRole.STUDENT,
          isAdminAuthorized = user.role == ROLE_ADMIN,
        )
        saveSession(session)
        Result.success(session)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun signInAdmin(adminId: String, password: String): Result<UserSession> =
    withContext(Dispatchers.IO) {
      try {
        seedInitialDataIfNeeded()
        val cleanId = adminId.trim()
        require(cleanId.isNotEmpty()) { "Please enter your Admin ID." }
        require(password.isNotBlank()) { "Please enter your password." }

        val existing = dao.findUserByLoginKey(cleanId.lowercase(Locale.ROOT))
          ?: return@withContext Result.failure(
            IllegalArgumentException("Admin ID \"$cleanId\" not found. Try \"admin\" (password: admin123).")
          )

        if (existing.role != ROLE_ADMIN) {
          return@withContext Result.failure(
            IllegalArgumentException("\"$cleanId\" is registered as a Student account.")
          )
        }

        val expected = hashPassword(password, existing.salt)
        if (expected != existing.passwordHash) {
          return@withContext Result.failure(
            IllegalArgumentException("Incorrect password for Admin ID \"$cleanId\".")
          )
        }

        val session = UserSession(
          userId = existing.id,
          displayName = existing.displayName,
          emailOrId = existing.email,
          activeRole = PortalRole.ADMIN,
          isAdminAuthorized = true,
        )
        saveSession(session)
        Result.success(session)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  fun updateStoredRole(role: PortalRole) {
    val current = _sessionFlow.value ?: return
    if (role == PortalRole.ADMIN && !current.isAdminAuthorized) return
    prefs.edit().putString(KEY_ACTIVE_ROLE, role.name).apply()
    _sessionFlow.value = current.copy(activeRole = role)
  }

  fun signOut() {
    prefs.edit().remove(KEY_USER_ID).remove(KEY_ACTIVE_ROLE).apply()
    _sessionFlow.value = null
  }

  private fun saveSession(session: UserSession) {
    prefs.edit()
      .putString(KEY_USER_ID, session.userId)
      .putString(KEY_ACTIVE_ROLE, session.activeRole.name)
      .apply()
    _sessionFlow.value = session
  }

  fun observeAnnouncements(): Flow<List<Announcement>> =
    dao.observeAnnouncements(CAMPUS_ID).map { list ->
      list.map {
        Announcement(
          id = it.id,
          authorId = it.authorId,
          authorName = it.authorName,
          title = it.title,
          message = it.message,
          campusId = it.campusId,
          published = it.published,
          createdAtMillis = it.createdAtMillis,
          updatedAtMillis = it.updatedAtMillis,
        )
      }
    }

  fun observeMaintenanceReports(userId: String, isAdminView: Boolean): Flow<List<MaintenanceReport>> {
    val source = if (isAdminView) {
      dao.observeAllMaintenanceReports()
    } else {
      dao.observeUserMaintenanceReports(userId)
    }
    return source.map { list ->
      list.map {
        MaintenanceReport(
          id = it.id,
          userId = it.userId,
          studentName = it.studentName,
          issueCategory = it.issueCategory,
          description = it.description,
          photoBase64 = it.photoBase64,
          status = it.status,
          campusId = it.campusId,
          createdAtMillis = it.createdAtMillis,
          updatedAtMillis = it.updatedAtMillis,
        )
      }
    }
  }

  fun observeLostFoundItems(): Flow<List<LostFoundItem>> =
    dao.observeLostFoundItems(CAMPUS_ID).map { list ->
      list.map {
        LostFoundItem(
          id = it.id,
          userId = it.userId,
          studentName = it.studentName,
          description = it.description,
          photoBase64 = it.photoBase64,
          status = it.status,
          campusId = it.campusId,
          createdAtMillis = it.createdAtMillis,
          updatedAtMillis = it.updatedAtMillis,
        )
      }
    }

  suspend fun submitMaintenanceReport(
    userId: String,
    studentName: String,
    issueCategory: String,
    description: String,
    photoBase64: String?,
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      require(userId.isNotBlank()) { "Please sign in first." }
      val cleanCategory = issueCategory.trim().take(60)
      val cleanDescription = description.trim().take(1000)
      require(cleanCategory.isNotEmpty()) { "Please select an issue category." }
      require(cleanDescription.isNotEmpty()) { "Please enter a description." }
      if (photoBase64 != null) {
        require(photoBase64.length <= MAX_PHOTO_BASE64_LEN) { "Selected image is too large." }
      }
      val now = System.currentTimeMillis()
      dao.insertMaintenanceReport(
        MaintenanceReportEntity(
          id = "maint_${UUID.randomUUID().toString().replace("-", "").take(16)}",
          userId = userId,
          studentName = studentName.trim().ifBlank { "STI Student" }.take(80),
          issueCategory = cleanCategory,
          description = cleanDescription,
          photoBase64 = photoBase64?.takeIf { it.isNotBlank() },
          status = MaintenanceStatus.PENDING.label,
          campusId = CAMPUS_ID,
          createdAtMillis = now,
          updatedAtMillis = now,
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateMaintenanceStatus(reportId: String, newStatus: MaintenanceStatus): Result<Unit> =
    withContext(Dispatchers.IO) {
      try {
        require(reportId.isNotBlank()) { "Report ID is required." }
        dao.updateMaintenanceStatus(reportId, newStatus.label, System.currentTimeMillis())
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun deleteMaintenanceReport(reportId: String): Result<Unit> =
    withContext(Dispatchers.IO) {
      try {
        require(reportId.isNotBlank()) { "Report ID is required." }
        dao.deleteMaintenanceReportById(reportId)
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun submitLostFoundItem(
    userId: String,
    studentName: String,
    description: String,
    photoBase64: String?,
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      require(userId.isNotBlank()) { "Please sign in first." }
      val cleanDescription = description.trim().take(1000)
      require(cleanDescription.isNotEmpty()) { "Please describe the lost or found item." }
      if (photoBase64 != null) {
        require(photoBase64.length <= MAX_PHOTO_BASE64_LEN) { "Selected image is too large." }
      }
      val now = System.currentTimeMillis()
      dao.insertLostFoundItem(
        LostFoundItemEntity(
          id = "lf_${UUID.randomUUID().toString().replace("-", "").take(16)}",
          userId = userId,
          studentName = studentName.trim().ifBlank { "STI Student" }.take(80),
          description = cleanDescription,
          photoBase64 = photoBase64?.takeIf { it.isNotBlank() },
          status = LostFoundStatus.ON_HOLD.label,
          campusId = CAMPUS_ID,
          createdAtMillis = now,
          updatedAtMillis = now,
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateLostFoundStatus(itemId: String, newStatus: LostFoundStatus): Result<Unit> =
    withContext(Dispatchers.IO) {
      try {
        require(itemId.isNotBlank()) { "Item ID is required." }
        dao.updateLostFoundStatus(itemId, newStatus.label, System.currentTimeMillis())
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun deleteLostFoundItem(itemId: String): Result<Unit> =
    withContext(Dispatchers.IO) {
      try {
        require(itemId.isNotBlank()) { "Item ID is required." }
        dao.deleteLostFoundItemById(itemId)
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun createAnnouncement(
    authorId: String,
    authorName: String,
    title: String,
    message: String,
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      val cleanTitle = title.trim().ifBlank { "Campus Announcement" }.take(100)
      val cleanMessage = message.trim().take(1000)
      require(cleanMessage.isNotEmpty()) { "Please enter an announcement message." }
      val now = System.currentTimeMillis()
      dao.insertAnnouncement(
        AnnouncementEntity(
          id = "ann_${UUID.randomUUID().toString().replace("-", "").take(16)}",
          authorId = authorId,
          authorName = authorName.trim().ifBlank { "STI Sta. Mesa Admin" }.take(80),
          title = cleanTitle,
          message = cleanMessage,
          campusId = CAMPUS_ID,
          published = true,
          createdAtMillis = now,
          updatedAtMillis = now,
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteAnnouncement(announcementId: String): Result<Unit> =
    withContext(Dispatchers.IO) {
      try {
        require(announcementId.isNotBlank()) { "Announcement ID is required." }
        dao.deleteAnnouncementById(announcementId)
        Result.success(Unit)
      } catch (e: Exception) {
        Result.failure(e)
      }
    }

  suspend fun submitFeedback(
    userId: String,
    studentName: String,
    rating: Int,
    message: String,
  ): Result<Unit> = withContext(Dispatchers.IO) {
    try {
      require(userId.isNotBlank()) { "Please sign in first." }
      val cleanMessage = message.trim().take(500)
      require(cleanMessage.isNotEmpty()) { "Please enter your feedback." }
      dao.insertFeedback(
        FeedbackEntity(
          id = "fb_${UUID.randomUUID().toString().replace("-", "").take(16)}",
          userId = userId,
          studentName = studentName.trim().ifBlank { "STI Student" }.take(80),
          rating = rating.coerceIn(1, 5),
          message = cleanMessage,
          createdAtMillis = System.currentTimeMillis(),
        )
      )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  private fun deriveDisplayName(email: String): String {
    val local = email.substringBefore("@")
      .split(".", "_", "-")
      .filter { it.isNotBlank() }
      .joinToString(" ") { part ->
        part.replaceFirstChar { ch -> if (ch.isLowerCase()) ch.titlecase(Locale.getDefault()) else ch.toString() }
      }
    return local.ifBlank { "STI Student" }.take(80)
  }

  private fun generateSalt(): String {
    val bytes = ByteArray(16)
    SecureRandom().nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
  }

  private fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
      .digest("$salt:$password".toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
  }

  companion object {
    const val CAMPUS_ID = "sti_sta_mesa"
    const val CAMPUS_DISPLAY_NAME = "STI Sta. Mesa"
    const val MAX_PHOTO_BASE64_LEN = 350_000
    private const val ROLE_STUDENT = "STUDENT"
    private const val ROLE_ADMIN = "ADMIN"
    private const val PREFS_NAME = "assisti_session_prefs"
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val KEY_ACTIVE_ROLE = "active_portal_role"
    private const val KEY_SEEDED_V1 = "seeded_v1"
    private const val KEY_CLEANED_SAMPLE_REPORTS_V1 = "cleaned_sample_reports_v1"
    private const val KEY_CLEANED_SAMPLE_ANNOUNCEMENTS_V1 = "cleaned_sample_announcements_v1"

    @Volatile
    private var INSTANCE: AssiSTIRepository? = null

    fun getInstance(context: Context): AssiSTIRepository =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: AssiSTIRepository(
          dao = AssiSTIDatabase.getInstance(context).assistiDao(),
          prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE),
        ).also { INSTANCE = it }
      }
  }
}
