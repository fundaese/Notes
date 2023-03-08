package com.example.notes.db

import androidx.room.*

@Dao
interface NoteDao {
//We need functions for access the database.
// We create dao each Entity

    @Insert
    suspend fun addNote(note: Note)

    @Query("SELECT * FROM note ORDER BY id DESC") //table name = note
    suspend fun getAllNotes() : List<Note>

    @Insert
    suspend fun addMultipleNotes(vararg note: Note)

    @Update
    suspend fun updateNotes(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)
}