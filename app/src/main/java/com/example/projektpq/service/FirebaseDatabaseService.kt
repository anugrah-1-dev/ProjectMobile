package com.example.projektpq.service

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseService {

    private val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance().apply {
            setPersistenceEnabled(true)
        }
    }

    private val usersRef: DatabaseReference by lazy { database.getReference("users") }
    private val publicDataRef: DatabaseReference by lazy { database.getReference("public_data") }

    // Menyimpan data user dengan suspend function
    suspend fun saveUserData(userId: String, userData: Map<String, Any>): Result<Boolean> {
        return try {
            usersRef.child(userId).setValue(userData).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Membaca data user sekali (one-time read)
    suspend fun getUserDataOnce(userId: String): Result<Map<String, Any>> {
        return try {
            val snapshot = usersRef.child(userId).get().await()
            if (snapshot.exists()) {
                val userData = snapshot.value as? Map<String, Any>
                if (userData != null) {
                    Result.success(userData)
                } else {
                    Result.failure(Exception("Data tidak valid"))
                }
            } else {
                Result.failure(Exception("User tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Membaca data user dengan real-time listener
    fun getUserData(userId: String): Flow<Result<Map<String, Any>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.value as? Map<String, Any>
                    if (userData != null) {
                        trySend(Result.success(userData))
                    } else {
                        trySend(Result.failure(Exception("Data tidak valid")))
                    }
                } else {
                    trySend(Result.failure(Exception("User tidak ditemukan")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(Exception(error.message)))
            }
        }

        usersRef.child(userId).addValueEventListener(listener)

        awaitClose {
            usersRef.child(userId).removeEventListener(listener)
        }
    }

    // Update data user
    suspend fun updateUserData(userId: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            usersRef.child(userId).updateChildren(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hapus data user
    suspend fun deleteUserData(userId: String): Result<Boolean> {
        return try {
            usersRef.child(userId).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Menyimpan data public
    suspend fun savePublicData(path: String, data: Any): Result<Boolean> {
        return try {
            publicDataRef.child(path).setValue(data).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Membaca data public sekali (one-time read)
    suspend fun getPublicDataOnce(path: String): Result<Any> {
        return try {
            val snapshot = publicDataRef.child(path).get().await()
            if (snapshot.exists()) {
                val data = snapshot.value
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Data tidak valid"))
                }
            } else {
                Result.failure(Exception("Data tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Membaca data public dengan real-time listener
    fun getPublicData(path: String): Flow<Result<Any>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.value
                    if (data != null) {
                        trySend(Result.success(data))
                    } else {
                        trySend(Result.failure(Exception("Data tidak valid")))
                    }
                } else {
                    trySend(Result.failure(Exception("Data tidak ditemukan")))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(Exception(error.message)))
            }
        }

        publicDataRef.child(path).addValueEventListener(listener)

        awaitClose {
            publicDataRef.child(path).removeEventListener(listener)
        }
    }

    // Update data public
    suspend fun updatePublicData(path: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            publicDataRef.child(path).updateChildren(updates).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hapus data public
    suspend fun deletePublicData(path: String): Result<Boolean> {
        return try {
            publicDataRef.child(path).removeValue().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}