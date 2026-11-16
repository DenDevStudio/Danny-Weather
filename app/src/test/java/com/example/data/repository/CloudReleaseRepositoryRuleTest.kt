package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.AppAccentTheme
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudReleaseRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun publishAndObserveRelease_verifiedAdmin_publishesAndUnauthReadsRealtime() = runBlocking {
        signInTestUser(ADMIN_EMAIL, markEmailVerified = true)
        val adminRepo = CloudReleaseRepository(firestore, auth)

        val publishResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.publishRelease(
                targetVersion = "2.2",
                releaseNotes = "Оновлення погоди та хмарної розсилки",
                appTitle = "Danny Weather",
                accentTheme = AppAccentTheme.EMERALD
            )
        }
        assertTrue("Expected admin publish to succeed: ${publishResult.exceptionOrNull()}", publishResult.isSuccess)
        val saved = publishResult.getOrThrow()
        assertEquals("2.2", saved.targetVersion)
        assertEquals(AppAccentTheme.EMERALD.name, saved.accentTheme)
        assertTrue(saved.isAvailable)

        // Update accent theme independently in realtime
        val themeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.updateCloudAccentTheme(AppAccentTheme.SKY)
        }
        assertTrue(themeResult.isSuccess)

        // Unauthenticated users receive realtime release + accentTheme updates
        auth.signOut()
        val publicRepo = CloudReleaseRepository(firestore, auth)
        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            publicRepo.observeLatestRelease().first { it != null && it.accentTheme == AppAccentTheme.SKY.name }
        }
        assertNotNull(observed)
        assertEquals("2.2", observed!!.targetVersion)
        assertEquals(AppAccentTheme.SKY.name, observed.accentTheme)
        assertTrue(observed.isAvailable)

        val listResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            publicRepo.getAvailableReleases()
        }
        assertTrue(listResult.isSuccess)
        assertTrue(listResult.getOrThrow().any { it.targetVersion == "2.2" })

        // Verify admin can subsequently hide release notice
        signInTestUser(ADMIN_EMAIL, markEmailVerified = true)
        val hideResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.hideLatestRelease()
        }
        assertTrue(hideResult.isSuccess)

        val updatedObserved = withTimeout(FLOW_TIMEOUT_MS) {
            adminRepo.observeLatestRelease().first { it != null && !it.isAvailable }
        }
        assertFalse(updatedObserved!!.isAvailable)
    }

    @Test
    fun vipWhitelist_adminManagesAndFriendSeesOnlyOwnAccess() = runBlocking {
        signInTestUser(ADMIN_EMAIL, markEmailVerified = true)
        val adminRepo = CloudReleaseRepository(firestore, auth)

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.addVipEmail(FRIEND_EMAIL)
        }
        assertTrue("Expected VIP add to succeed: ${addResult.exceptionOrNull()}", addResult.isSuccess)

        val adminList = withTimeout(FLOW_TIMEOUT_MS) {
            adminRepo.observeAllVipEmails().first { list -> list.any { it.email == FRIEND_EMAIL } }
        }
        assertTrue(adminList.any { it.email == FRIEND_EMAIL })

        // Friend signed in sees their own VIP status unlocked
        signInTestUser(FRIEND_EMAIL, markEmailVerified = true)
        val friendRepo = CloudReleaseRepository(firestore, auth)
        val isFriendVip = withTimeout(FLOW_TIMEOUT_MS) {
            friendRepo.observeOwnVipStatus().first { it }
        }
        assertTrue(isFriendVip)

        // Unlisted user does NOT get VIP status and cannot list vip_access
        signInTestUser(OTHER_EMAIL, markEmailVerified = true)
        val otherRepo = CloudReleaseRepository(firestore, auth)
        val isOtherVip = withTimeout(FLOW_TIMEOUT_MS) {
            otherRepo.observeOwnVipStatus().first()
        }
        assertFalse(isOtherVip)

        val nonAdminAddAttempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            otherRepo.addVipEmail(OTHER_EMAIL)
        }
        assertTrue(nonAdminAddAttempt.isFailure)

        // Admin cleans up VIP entry
        signInTestUser(ADMIN_EMAIL, markEmailVerified = true)
        val removeResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.removeVipEmail(FRIEND_EMAIL)
        }
        assertTrue(removeResult.isSuccess)
    }

    @Test
    fun publishRelease_nonAdminFriend_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ADMIN_EMAIL, markEmailVerified = true)
        val adminRepo = CloudReleaseRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            adminRepo.publishRelease(
                targetVersion = "2.3",
                releaseNotes = "Базовий реліз"
            ).getOrThrow()
        }

        signInTestUser(FRIEND_EMAIL, markEmailVerified = true)
        val friendRepo = CloudReleaseRepository(firestore, auth)
        val attempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            friendRepo.publishRelease(
                targetVersion = "9.9",
                releaseNotes = "Спроба підміни"
            )
        }
        assertTrue(attempt.isFailure)
        val firestoreException = checkNotNull(attempt.exceptionOrNull()?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException, got ${attempt.exceptionOrNull()}"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun publishRelease_unverifiedAdminEmail_failsWithPermissionDenied() = runBlocking {
        signInTestUser(UNVERIFIED_ADMIN_EMAIL, markEmailVerified = false)
        val spoofRepo = CloudReleaseRepository(firestore, auth)

        val attempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            spoofRepo.publishRelease(
                targetVersion = "2.9",
                releaseNotes = "Непідтверджений акаунт"
            )
        }
        assertTrue(attempt.isFailure)
        val firestoreException = checkNotNull(attempt.exceptionOrNull()?.unwrapFirestoreException()) {
            "Expected FirebaseFirestoreException, got ${attempt.exceptionOrNull()}"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun publishRelease_unauthenticatedCaller_failsFailFast() = runBlocking {
        auth.signOut()
        val repo = CloudReleaseRepository(firestore, auth)
        val attempt = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.publishRelease(
                targetVersion = "9.9",
                releaseNotes = "Анонімна спроба запису"
            )
        }
        assertTrue(attempt.isFailure)
        assertTrue(attempt.exceptionOrNull() is IllegalStateException)
    }

    private companion object {
        const val ADMIN_EMAIL = "danyloandtadik@gmail.com"
        const val UNVERIFIED_ADMIN_EMAIL = "danyloandtadik@gmail.com"
        const val FRIEND_EMAIL = "friend@test.com"
        const val OTHER_EMAIL = "stranger@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
