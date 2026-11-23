package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AssiSTIDatabase
import com.example.data.AssiSTIRepository
import com.example.data.LostFoundStatus
import com.example.data.MaintenanceStatus
import com.example.data.PortalRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AssiSTIRepositoryTest {

  private lateinit var db: AssiSTIDatabase
  private lateinit var repository: AssiSTIRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AssiSTIDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val prefs = context.getSharedPreferences("test_assisti_prefs_${System.nanoTime()}", Context.MODE_PRIVATE)
    repository = AssiSTIRepository(db.assistiDao(), prefs)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `student login registers new account and persists session`() = runTest {
    val result = repository.signInStudent("juan.delacruz@sti.edu.ph", "mypass123")
    assertTrue(result.isSuccess)
    val session = result.getOrThrow()
    assertEquals("Juan Delacruz", session.displayName)
    assertEquals(PortalRole.STUDENT, session.activeRole)
    assertFalse(session.isAdminAuthorized)

    // Subsequent login with wrong password fails
    repository.signOut()
    assertNull(repository.sessionFlow.value)
    val wrongPass = repository.signInStudent("juan.delacruz@sti.edu.ph", "wrong")
    assertTrue(wrongPass.isFailure)

    // Correct password succeeds and restores session
    val retry = repository.signInStudent("juan.delacruz@sti.edu.ph", "mypass123")
    assertTrue(retry.isSuccess)
    assertNotNull(repository.initializeSession())
  }

  @Test
  fun `admin login works with seeded admin credentials and rejects student`() = runTest {
    val ok = repository.signInAdmin("admin", "admin123")
    assertTrue(ok.isSuccess)
    val session = ok.getOrThrow()
    assertEquals(PortalRole.ADMIN, session.activeRole)
    assertTrue(session.isAdminAuthorized)

    val badPass = repository.signInAdmin("admin", "wrong")
    assertTrue(badPass.isFailure)

    repository.signInStudent("ana@sti.edu.ph", "pass123")
    val notAdmin = repository.signInAdmin("ana@sti.edu.ph", "pass123")
    assertTrue(notAdmin.isFailure)
  }

  @Test
  fun `maintenance and lost found start empty and full workflow persists cleanly`() = runTest {
    val student = repository.signInStudent("test.student@sti.edu.ph", "secret123").getOrThrow()

    assertTrue(repository.observeLostFoundItems().first().isEmpty())
    assertTrue(repository.observeMaintenanceReports(student.userId, isAdminView = true).first().isEmpty())

    val maintResult = repository.submitMaintenanceReport(
      userId = student.userId,
      studentName = student.displayName,
      issueCategory = "Aircon / HVAC",
      description = "AC leaking in Room 302",
      photoBase64 = null,
    )
    assertTrue(maintResult.isSuccess)

    val studentMaintList = repository.observeMaintenanceReports(student.userId, isAdminView = false).first()
    assertEquals(1, studentMaintList.size)
    assertEquals(MaintenanceStatus.PENDING, studentMaintList.first().statusEnum())

    repository.updateMaintenanceStatus(studentMaintList.first().id, MaintenanceStatus.FIXED)
    val updatedMaint = repository.observeMaintenanceReports(student.userId, isAdminView = false).first()
    assertEquals(MaintenanceStatus.FIXED, updatedMaint.first().statusEnum())

    val lfResult = repository.submitLostFoundItem(
      userId = student.userId,
      studentName = student.displayName,
      description = "Black wallet near library",
      photoBase64 = null,
    )
    assertTrue(lfResult.isSuccess)
    val lfItems = repository.observeLostFoundItems().first()
    val createdLf = lfItems.first { it.description == "Black wallet near library" }
    assertEquals(LostFoundStatus.ON_HOLD, createdLf.statusEnum())

    repository.updateLostFoundStatus(createdLf.id, LostFoundStatus.CLAIMED)
    val updatedLf = repository.observeLostFoundItems().first().first { it.id == createdLf.id }
    assertEquals(LostFoundStatus.CLAIMED, updatedLf.statusEnum())
  }

  @Test
  fun `announcements start empty and create and delete plus feedback succeed`() = runTest {
    val admin = repository.signInAdmin("admin", "admin123").getOrThrow()
    assertTrue(repository.observeAnnouncements().first().isEmpty())

    val post = repository.createAnnouncement(
      authorId = admin.userId,
      authorName = admin.displayName,
      title = "Campus Power Check",
      message = "Brief power check at 5 PM.",
    )
    assertTrue(post.isSuccess)

    val list = repository.observeAnnouncements().first()
    val created = list.first { it.title == "Campus Power Check" }
    repository.deleteAnnouncement(created.id)
    val afterDelete = repository.observeAnnouncements().first()
    assertFalse(afterDelete.any { it.id == created.id })

    val fb = repository.submitFeedback(admin.userId, admin.displayName, 5, "Great app!")
    assertTrue(fb.isSuccess)
  }
}
