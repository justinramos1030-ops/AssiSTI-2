package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
  tableName = "users",
  indices = [Index(value = ["loginKey"], unique = true)],
)
data class UserEntity(
  @PrimaryKey val id: String,
  val loginKey: String,
  val displayName: String,
  val email: String,
  val role: String, // "STUDENT" or "ADMIN"
  val passwordHash: String,
  val salt: String,
  val campus: String = "STI Sta. Mesa",
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
  @PrimaryKey val id: String,
  val authorId: String,
  val authorName: String,
  val title: String,
  val message: String,
  val campusId: String,
  val published: Boolean,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

@Entity(tableName = "maintenance_reports")
data class MaintenanceReportEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val studentName: String,
  val issueCategory: String,
  val description: String,
  val photoBase64: String?,
  val status: String,
  val campusId: String,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

@Entity(tableName = "lost_found_items")
data class LostFoundItemEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val studentName: String,
  val description: String,
  val photoBase64: String?,
  val status: String,
  val campusId: String,
  val createdAtMillis: Long,
  val updatedAtMillis: Long,
)

@Entity(tableName = "feedback")
data class FeedbackEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val studentName: String,
  val rating: Int,
  val message: String,
  val createdAtMillis: Long,
)

@Dao
interface AssiSTIDao {
  // Auth / Users
  @Query("SELECT * FROM users WHERE loginKey = :loginKey LIMIT 1")
  suspend fun findUserByLoginKey(loginKey: String): UserEntity?

  @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
  suspend fun findUserById(id: String): UserEntity?

  @Query("SELECT COUNT(*) FROM users")
  suspend fun countUsers(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertUsersIgnore(users: List<UserEntity>)

  // Announcements
  @Query("SELECT * FROM announcements WHERE campusId = :campusId AND published = 1 ORDER BY createdAtMillis DESC")
  fun observeAnnouncements(campusId: String): Flow<List<AnnouncementEntity>>

  @Query("SELECT COUNT(*) FROM announcements")
  suspend fun countAnnouncements(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnouncement(announcement: AnnouncementEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAnnouncementsIgnore(items: List<AnnouncementEntity>)

  @Query("DELETE FROM announcements WHERE id = :id")
  suspend fun deleteAnnouncementById(id: String)

  @Query("DELETE FROM announcements WHERE id IN ('ann_1', 'ann_2', 'ann_3')")
  suspend fun deleteSeededAnnouncements()

  // Maintenance Reports
  @Query("SELECT * FROM maintenance_reports ORDER BY createdAtMillis DESC")
  fun observeAllMaintenanceReports(): Flow<List<MaintenanceReportEntity>>

  @Query("SELECT * FROM maintenance_reports WHERE userId = :userId ORDER BY createdAtMillis DESC")
  fun observeUserMaintenanceReports(userId: String): Flow<List<MaintenanceReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMaintenanceReport(report: MaintenanceReportEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertMaintenanceReportsIgnore(reports: List<MaintenanceReportEntity>)

  @Query("UPDATE maintenance_reports SET status = :status, updatedAtMillis = :updatedAt WHERE id = :reportId")
  suspend fun updateMaintenanceStatus(reportId: String, status: String, updatedAt: Long)

  @Query("DELETE FROM maintenance_reports WHERE id = :reportId")
  suspend fun deleteMaintenanceReportById(reportId: String)

  @Query("DELETE FROM maintenance_reports WHERE userId = 'seed_student_1'")
  suspend fun deleteSeededMaintenanceReports()

  // Lost & Found
  @Query("SELECT * FROM lost_found_items WHERE campusId = :campusId ORDER BY createdAtMillis DESC")
  fun observeLostFoundItems(campusId: String): Flow<List<LostFoundItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLostFoundItem(item: LostFoundItemEntity)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertLostFoundItemsIgnore(items: List<LostFoundItemEntity>)

  @Query("UPDATE lost_found_items SET status = :status, updatedAtMillis = :updatedAt WHERE id = :itemId")
  suspend fun updateLostFoundStatus(itemId: String, status: String, updatedAt: Long)

  @Query("DELETE FROM lost_found_items WHERE id = :itemId")
  suspend fun deleteLostFoundItemById(itemId: String)

  @Query("DELETE FROM lost_found_items WHERE userId = 'seed_student_1'")
  suspend fun deleteSeededLostFoundItems()

  // Feedback
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeedback(feedback: FeedbackEntity)
}

@Database(
  entities = [
    UserEntity::class,
    AnnouncementEntity::class,
    MaintenanceReportEntity::class,
    LostFoundItemEntity::class,
    FeedbackEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class AssiSTIDatabase : RoomDatabase() {
  abstract fun assistiDao(): AssiSTIDao

  companion object {
    @Volatile
    private var INSTANCE: AssiSTIDatabase? = null

    fun getInstance(context: Context): AssiSTIDatabase =
      INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          AssiSTIDatabase::class.java,
          "assisti_campus.db",
        ).build().also { INSTANCE = it }
      }
  }
}
