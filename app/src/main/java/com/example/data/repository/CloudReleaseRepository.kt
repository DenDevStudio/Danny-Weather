package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AppAccentTheme
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Locale

@IgnoreExtraProperties
data class CloudAppRelease(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val targetVersion: String = "",
    val releaseNotes: String = "",
    val appTitle: String = "Danny Weather",
    val accentTheme: String = AppAccentTheme.GOLD.name,
    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudVipAccessEntry(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val email: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

class CloudReleaseRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before writing to Firestore.")
    }

    /**
     * Publicly subscribes to the canonical `/app_releases/latest` document in realtime
     * so all app users receive live accent theme changes and release prompts immediately.
     */
    fun observeLatestRelease(): Flow<CloudAppRelease?> = flow {
        val docRef = db.collection(COLLECTION_RELEASES).document(LATEST_RELEASE_DOC_ID)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            CloudAppRelease::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun getAvailableReleases(): Result<List<CloudAppRelease>> {
        return try {
            val querySnapshot = db.collection(COLLECTION_RELEASES)
                .whereEqualTo("isAvailable", true)
                .get()
                .await()
            val releases = querySnapshot.toObjects(
                CloudAppRelease::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(releases)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, COLLECTION_RELEASES)
            Result.failure(e)
        }
    }

    suspend fun publishRelease(
        targetVersion: String,
        releaseNotes: String,
        appTitle: String = "Danny Weather",
        accentTheme: AppAccentTheme = AppAccentTheme.GOLD
    ): Result<CloudAppRelease> {
        val docRef = db.collection(COLLECTION_RELEASES).document(LATEST_RELEASE_DOC_ID)
        return try {
            val uid = requireUserId()
            val cleanVersion = targetVersion.trim().take(32).ifBlank { "2.1" }
            val cleanNotes = releaseNotes.trim().take(1500)
            val cleanTitle = appTitle.trim().take(40).ifBlank { "Danny Weather" }
            val cleanTheme = accentTheme.name.take(32)

            val existing = docRef.get().await()
            if (existing.exists()) {
                val updatePayload = mapOf(
                    "targetVersion" to cleanVersion,
                    "releaseNotes" to cleanNotes,
                    "appTitle" to cleanTitle,
                    "accentTheme" to cleanTheme,
                    "isAvailable" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            } else {
                val createPayload = mapOf(
                    "authorId" to uid,
                    "targetVersion" to cleanVersion,
                    "releaseNotes" to cleanNotes,
                    "appTitle" to cleanTitle,
                    "accentTheme" to cleanTheme,
                    "isAvailable" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            }

            val saved = docRef.get().await().toObject(
                CloudAppRelease::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ) ?: CloudAppRelease(
                id = LATEST_RELEASE_DOC_ID,
                authorId = uid,
                targetVersion = cleanVersion,
                releaseNotes = cleanNotes,
                appTitle = cleanTitle,
                accentTheme = cleanTheme,
                isAvailable = true
            )
            Result.success(saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Immediately persists & broadcasts the selected accent color theme to all users
     * without triggering a forced app update dialog if no release was previously published.
     */
    suspend fun updateCloudAccentTheme(
        accentTheme: AppAccentTheme,
        fallbackVersion: String = "2.0",
        appTitle: String = "Danny Weather"
    ): Result<Unit> {
        val docRef = db.collection(COLLECTION_RELEASES).document(LATEST_RELEASE_DOC_ID)
        return try {
            val uid = requireUserId()
            val cleanTheme = accentTheme.name.take(32)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "accentTheme" to cleanTheme,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "authorId" to uid,
                        "targetVersion" to fallbackVersion.trim().take(32).ifBlank { "2.0" },
                        "releaseNotes" to "",
                        "appTitle" to appTitle.trim().take(40).ifBlank { "Danny Weather" },
                        "accentTheme" to cleanTheme,
                        "isAvailable" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun hideLatestRelease(): Result<Unit> {
        val docRef = db.collection(COLLECTION_RELEASES).document(LATEST_RELEASE_DOC_ID)
        return try {
            requireUserId()
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "isAvailable" to false,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Checks in real time whether the currently signed-in user's Google email is in `/vip_access`.
     * Rules strictly permit querying only the caller's own verified email.
     */
    fun observeOwnVipStatus(): Flow<Boolean> = flow {
        val email = auth.currentUser?.email?.trim()?.lowercase(Locale.ROOT)
        if (email.isNullOrBlank()) {
            emit(false)
            return@flow
        }
        val query = db.collection(COLLECTION_VIP_ACCESS)
            .whereEqualTo("email", email)
            .limit(1)
        emitAll(
            query.snapshots()
                .map { snapshot -> !snapshot.isEmpty }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    emit(false)
                }
        )
    }

    /**
     * Admin-only live stream of all whitelisted VIP emails for the DenDev Studio manager UI.
     */
    fun observeAllVipEmails(): Flow<List<CloudVipAccessEntry>> = flow {
        requireUserId()
        val query = db.collection(COLLECTION_VIP_ACCESS)
        emitAll(
            query.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudVipAccessEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.email }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, COLLECTION_VIP_ACCESS)
                    }
                    throw error
                }
        )
    }

    suspend fun addVipEmail(rawEmail: String): Result<CloudVipAccessEntry> {
        val cleanEmail = rawEmail.trim().lowercase(Locale.ROOT)
        if (!EMAIL_REGEX.matches(cleanEmail)) {
            return Result.failure(IllegalArgumentException("Некоректний формат email"))
        }
        val docId = vipDocIdForEmail(cleanEmail)
        val docRef = db.collection(COLLECTION_VIP_ACCESS).document(docId)
        return try {
            val uid = requireUserId()
            val payload = mapOf(
                "authorId" to uid,
                "email" to cleanEmail,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            val saved = CloudVipAccessEntry(id = docId, authorId = uid, email = cleanEmail)
            Result.success(saved)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun removeVipEmail(emailOrDocId: String): Result<Unit> {
        val docId = if (emailOrDocId.startsWith("vip_")) emailOrDocId else vipDocIdForEmail(emailOrDocId)
        val docRef = db.collection(COLLECTION_VIP_ACCESS).document(docId)
        return try {
            requireUserId()
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    companion object {
        const val COLLECTION_RELEASES = "app_releases"
        const val LATEST_RELEASE_DOC_ID = "latest"
        const val COLLECTION_VIP_ACCESS = "vip_access"
        private val EMAIL_REGEX = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

        fun vipDocIdForEmail(rawEmail: String): String {
            val normalized = rawEmail.trim().lowercase(Locale.ROOT)
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(normalized.toByteArray(Charsets.UTF_8))
                .joinToString("") { b -> ((b.toInt() and 0xFF) + 0x100).toString(16).substring(1) }
                .take(24)
            return "vip_$digest"
        }
    }
}
