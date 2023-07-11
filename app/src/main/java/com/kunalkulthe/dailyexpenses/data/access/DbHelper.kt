package com.kunalkulthe.dailyexpenses.data.access

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context): SQLiteOpenHelper(context ,DB_NAME, null, DB_VERSION) {

    companion object{
        private const val DB_NAME = "dailyexpenses.db"
        private const val DB_VERSION = 10
        const val TABLE_NAME = "expenses"
        const val COLUMN_EXP_ID = "expId"
        const val COLUMN_USER_ID = "userId"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_DATE = "date"
        const val COLUMN_IS_SYNCED = "isSynced"
        const val COLUMN_IMAGE = "image"
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        val query = "CREATE TABLE $TABLE_NAME " +
                "($COLUMN_EXP_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_USER_ID TEXT NOT NULL, " +
                "$COLUMN_CATEGORY TEXT, " +
                "$COLUMN_DESCRIPTION TEXT, " +
                "$COLUMN_AMOUNT INTEGER NOT NULL, " +
                "$COLUMN_DATE TEXT NOT NULL, " +
                "$COLUMN_IS_SYNCED INTEGER NOT NULL, " +
                "$COLUMN_IMAGE BLOB" +
                ")"
        sqLiteDatabase.execSQL(query)
//
//        val insertQuery = "INSERT INTO $TABLE_NAME " +
//                "($COLUMN_USER_ID, $COLUMN_CATEGORY, $COLUMN_DESCRIPTION, $COLUMN_AMOUNT, $COLUMN_DATE, $COLUMN_IS_SYNCED, $COLUMN_IMAGE) " +
//                "VALUES ('user01', 'food', 'food', 250, '2023-06-04', 0, '')," +
//                "('user01','clothes','jeans', 5000, '2023-06-04', 0, '')"
//
//        sqLiteDatabase.execSQL(insertQuery)
    }

    override fun onUpgrade(p0: SQLiteDatabase, p1: Int, p2: Int) {
        p0.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun getWritableDb(): SQLiteDatabase
    {
        return this.writableDatabase
    }

    fun getReadableDb(): SQLiteDatabase
    {
        return  this.readableDatabase
    }
}