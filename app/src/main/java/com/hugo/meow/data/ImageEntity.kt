package com.hugo.imagepreviewer.utils

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "meow_images")
data class ImageEntity(
    @PrimaryKey val id: String,
    val url: String,
    val width: Int,
    val height: Int
)

@Dao
interface ImageDao {
    @Query("SELECT * From meow_images")
    suspend fun getAll(): List<ImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<ImageEntity>)

    @Query("DELETE From meow_images")
    suspend fun deleteAllSync()

    @Query("DELETE From meow_images")
    fun deleteAll()
}

@Entity(tableName = "local_meow_images")
data class LocalImageEntity(
    @PrimaryKey val id: String,
    val path: String,
    val width: Int,
    val height: Int
)

@Dao
interface LocalImageDao {
    @Query("SELECT * From local_meow_images")
    suspend fun getAll(): List<LocalImageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<LocalImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(images: LocalImageEntity)
}