package com.example.myapplication.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "employee_management.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_ACCOUNT = "Account"
        const val TABLE_EMPLOYEE = "Employee"
        const val TABLE_WORK_LOG = "WorkLog"
        const val TABLE_SALARY = "Salary"

        // Account Columns
        const val ACCOUNT_ID = "id"
        const val ACCOUNT_USERNAME = "username"
        const val ACCOUNT_PASSWORD = "password"
        const val ACCOUNT_ROLE = "role"

        // Employee Columns
        const val EMP_ID = "id"
        const val EMP_CODE = "code"
        const val EMP_FULLNAME = "fullname"
        const val EMP_EMAIL = "email"
        const val EMP_PHONE = "phone"
        const val EMP_HOURLY_RATE = "hourlyRate"
        const val EMP_ACCOUNT_ID = "accountId"

        // WorkLog Columns
        const val WL_ID = "id"
        const val WL_EMPLOYEE_ID = "employeeId"
        const val WL_WORK_DATE = "workDate"
        const val WL_CHECK_IN_TIME = "checkInTime"
        const val WL_CHECK_OUT_TIME = "checkOutTime"
        const val WL_TOTAL_HOURS = "totalHours"
        const val WL_STATUS = "status"

        // Salary Columns
        const val SAL_ID = "id"
        const val SAL_EMPLOYEE_ID = "employeeId"
        const val SAL_MONTH = "month"
        const val SAL_YEAR = "year"
        const val SAL_TOTAL_HOURS = "totalHours"
        const val SAL_OVERTIME_HOURS = "overtimeHours"
        const val SAL_TOTAL_SALARY = "totalSalary"
        const val SAL_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createAccountTable = """
            CREATE TABLE $TABLE_ACCOUNT (
                $ACCOUNT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $ACCOUNT_USERNAME TEXT UNIQUE NOT NULL,
                $ACCOUNT_PASSWORD TEXT NOT NULL,
                $ACCOUNT_ROLE TEXT NOT NULL
            );
        """.trimIndent()

        val createEmployeeTable = """
            CREATE TABLE $TABLE_EMPLOYEE (
                $EMP_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EMP_CODE TEXT UNIQUE NOT NULL,
                $EMP_FULLNAME TEXT NOT NULL,
                $EMP_EMAIL TEXT NOT NULL,
                $EMP_PHONE TEXT NOT NULL,
                $EMP_HOURLY_RATE REAL NOT NULL,
                $EMP_ACCOUNT_ID INTEGER,
                FOREIGN KEY($EMP_ACCOUNT_ID) REFERENCES $TABLE_ACCOUNT($ACCOUNT_ID) ON DELETE SET NULL
            );
        """.trimIndent()

        val createWorkLogTable = """
            CREATE TABLE $TABLE_WORK_LOG (
                $WL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $WL_EMPLOYEE_ID INTEGER NOT NULL,
                $WL_WORK_DATE TEXT NOT NULL,
                $WL_CHECK_IN_TIME TEXT,
                $WL_CHECK_OUT_TIME TEXT,
                $WL_TOTAL_HOURS REAL DEFAULT 0.0,
                $WL_STATUS TEXT NOT NULL,
                FOREIGN KEY($WL_EMPLOYEE_ID) REFERENCES $TABLE_EMPLOYEE($EMP_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        val createSalaryTable = """
            CREATE TABLE $TABLE_SALARY (
                $SAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $SAL_EMPLOYEE_ID INTEGER NOT NULL,
                $SAL_MONTH INTEGER NOT NULL,
                $SAL_YEAR INTEGER NOT NULL,
                $SAL_TOTAL_HOURS REAL DEFAULT 0.0,
                $SAL_OVERTIME_HOURS REAL DEFAULT 0.0,
                $SAL_TOTAL_SALARY REAL DEFAULT 0.0,
                $SAL_STATUS TEXT NOT NULL,
                FOREIGN KEY($SAL_EMPLOYEE_ID) REFERENCES $TABLE_EMPLOYEE($EMP_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createAccountTable)
        db.execSQL(createEmployeeTable)
        db.execSQL(createWorkLogTable)
        db.execSQL(createSalaryTable)

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SALARY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORK_LOG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EMPLOYEE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNT")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        // 1. Admin Account
        val adminValues = ContentValues().apply {
            put(ACCOUNT_USERNAME, "admin")
            put(ACCOUNT_PASSWORD, "admin123")
            put(ACCOUNT_ROLE, "ADMIN")
        }
        db.insert(TABLE_ACCOUNT, null, adminValues)

        // 2. Employee 1 Account & Record
        val emp1AccValues = ContentValues().apply {
            put(ACCOUNT_USERNAME, "emp001")
            put(ACCOUNT_PASSWORD, "123456")
            put(ACCOUNT_ROLE, "EMPLOYEE")
        }
        val emp1AccId = db.insert(TABLE_ACCOUNT, null, emp1AccValues)

        val emp1Values = ContentValues().apply {
            put(EMP_CODE, "EMP001")
            put(EMP_FULLNAME, "Nguyễn Văn A")
            put(EMP_EMAIL, "nguyenvana@gmail.com")
            put(EMP_PHONE, "0901234567")
            put(EMP_HOURLY_RATE, 50.0)
            put(EMP_ACCOUNT_ID, emp1AccId)
        }
        val emp1Id = db.insert(TABLE_EMPLOYEE, null, emp1Values)

        // 3. Employee 2 Account & Record
        val emp2AccValues = ContentValues().apply {
            put(ACCOUNT_USERNAME, "emp002")
            put(ACCOUNT_PASSWORD, "123456")
            put(ACCOUNT_ROLE, "EMPLOYEE")
        }
        val emp2AccId = db.insert(TABLE_ACCOUNT, null, emp2AccValues)

        val emp2Values = ContentValues().apply {
            put(EMP_CODE, "EMP002")
            put(EMP_FULLNAME, "Trần Thị B")
            put(EMP_EMAIL, "tranthib@gmail.com")
            put(EMP_PHONE, "0909876543")
            put(EMP_HOURLY_RATE, 60.0)
            put(EMP_ACCOUNT_ID, emp2AccId)
        }
        val emp2Id = db.insert(TABLE_EMPLOYEE, null, emp2Values)

        // 4. Sample WorkLogs for EMP001
        val sampleWorkLogs = listOf(
            Triple("2025-02-20", "08:00:00", "17:00:00"),
            Triple("2025-02-21", "08:00:00", "17:30:00"),
            Triple("2025-02-22", "08:30:00", "17:00:00"),
            Triple("2025-02-23", "08:00:00", "18:00:00")
        )

        for ((date, checkIn, checkOut) in sampleWorkLogs) {
            val totalH = 8.5
            val wlValues = ContentValues().apply {
                put(WL_EMPLOYEE_ID, emp1Id)
                put(WL_WORK_DATE, date)
                put(WL_CHECK_IN_TIME, checkIn)
                put(WL_CHECK_OUT_TIME, checkOut)
                put(WL_TOTAL_HOURS, totalH)
                put(WL_STATUS, "COMPLETED")
            }
            db.insert(TABLE_WORK_LOG, null, wlValues)
        }

        // 5. Sample WorkLogs for EMP002
        val sampleWorkLogs2 = listOf(
            Triple("2025-02-20", "08:00:00", "17:00:00"),
            Triple("2025-02-21", "08:00:00", "18:00:00")
        )

        for ((date, checkIn, checkOut) in sampleWorkLogs2) {
            val wlValues = ContentValues().apply {
                put(WL_EMPLOYEE_ID, emp2Id)
                put(WL_WORK_DATE, date)
                put(WL_CHECK_IN_TIME, checkIn)
                put(WL_CHECK_OUT_TIME, checkOut)
                put(WL_TOTAL_HOURS, 9.0)
                put(WL_STATUS, "COMPLETED")
            }
            db.insert(TABLE_WORK_LOG, null, wlValues)
        }

        // 6. Sample Salary Record
        val sal1 = ContentValues().apply {
            put(SAL_EMPLOYEE_ID, emp1Id)
            put(SAL_MONTH, 2)
            put(SAL_YEAR, 2025)
            put(SAL_TOTAL_HOURS, 34.0)
            put(SAL_OVERTIME_HOURS, 2.0)
            put(SAL_TOTAL_SALARY, 34.0 * 50.0 + 2.0 * 50.0 * 0.5)
            put(SAL_STATUS, "PENDING")
        }
        db.insert(TABLE_SALARY, null, sal1)

        val sal2 = ContentValues().apply {
            put(SAL_EMPLOYEE_ID, emp2Id)
            put(SAL_MONTH, 2)
            put(SAL_YEAR, 2025)
            put(SAL_TOTAL_HOURS, 18.0)
            put(SAL_OVERTIME_HOURS, 2.0)
            put(SAL_TOTAL_SALARY, 18.0 * 60.0 + 2.0 * 60.0 * 0.5)
            put(SAL_STATUS, "PAID")
        }
        db.insert(TABLE_SALARY, null, sal2)
    }
}
