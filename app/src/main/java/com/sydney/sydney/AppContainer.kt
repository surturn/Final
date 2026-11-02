package com.sydney.sydney

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sydney.sydney.data.firebase.FirebaseAuthRepository
import com.sydney.sydney.data.firebase.FirestoreHostelRepository
import com.sydney.sydney.data.firebase.FirestoreProfileRepository
import com.sydney.sydney.data.firebase.FirestoreSavedRepository
import com.sydney.sydney.data.firebase.FirestoreStudentRepository
import com.sydney.sydney.data.firebase.ImageProcessor
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.HostelRepository
import com.sydney.sydney.data.local.SharedPrefsRecentlyViewed
import com.sydney.sydney.data.repo.ProfileRepository
import com.sydney.sydney.data.repo.RecentlyViewedStore
import com.sydney.sydney.data.repo.SavedRepository
import com.sydney.sydney.data.repo.StudentRepository

/** Builds the repositories once so every screen shares the same Firebase instances. */
class AppContainer(context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val images = ImageProcessor(context.applicationContext)

    val auth: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance())
    val profiles: ProfileRepository = FirestoreProfileRepository(db, storage, images)
    val students: StudentRepository = FirestoreStudentRepository(db)
    val hostels: HostelRepository = FirestoreHostelRepository(db, storage, images)
    val saved: SavedRepository = FirestoreSavedRepository(db)
    val recent: RecentlyViewedStore = SharedPrefsRecentlyViewed(context.applicationContext)
}

class ColFindApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

val Context.container: AppContainer
    get() = (applicationContext as ColFindApp).container
