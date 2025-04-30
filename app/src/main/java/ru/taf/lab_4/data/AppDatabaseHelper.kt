package ru.taf.lab_4.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.taf.lab_4.model.Stats
import ru.taf.lab_4.model.User
import ru.taf.lab_4.utils.sha256

class AppDatabaseHelper(context: Context) : SQLiteOpenHelper(context, "app.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE user (
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                name TEXT NOT NULL, 
                surname TEXT NOT NULL, 
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                is_correct INTEGER,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES user(id)
            )
        """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS user")
            db.execSQL("DROP TABLE IF EXISTS stats")
            onCreate(db)
        }
    }

    fun saveUser(user: User) {
        val values = ContentValues().apply {
            put("name", user.name)
            put("surname", user.surname)
            put("email", user.email)
            put("password", user.password.sha256())
        }
        writableDatabase.insert("user", null, values)
    }

    fun getUser(email: String, password: String): User? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM user WHERE email = ? AND password = ?",
            arrayOf(email, password.sha256())
        )
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getInt(it.getColumnIndexOrThrow("id")),
                    name = it.getString(it.getColumnIndexOrThrow("name")),
                    surname = it.getString(it.getColumnIndexOrThrow("surname")),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    password = it.getString(it.getColumnIndexOrThrow("password"))
                )
            } else null
        }
    }

    fun addAnswer(userId: Int, isCorrect: Boolean) {
        writableDatabase.execSQL(
            "INSERT INTO stats (user_id, is_correct) VALUES (?, ?)",
            arrayOf(userId, if (isCorrect) 1 else 0)
        )
    }

    fun getStats(userId: Int): Stats {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT 
                COUNT(*) as total,
                SUM(is_correct) as correct
            FROM stats
            WHERE user_id = ?
            """, arrayOf(userId.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) {
                Stats(it.getInt(0), it.getInt(1))
            } else Stats(0, 0)
        }
    }
}

