package com.example.myapplication.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.myapplication.data.db.DatabaseHelper
import com.example.myapplication.data.model.Account
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Salary
import com.example.myapplication.data.model.WorkLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class AppRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // =========================================
    // ACCOUNT OPERATIONS
    // =========================================

    fun authenticate(username: String, password: String): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACCOUNT,
            null,
            "${DatabaseHelper.ACCOUNT_USERNAME} = ? AND ${DatabaseHelper.ACCOUNT_PASSWORD} = ?",
            arrayOf(username, password),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ID))
                val u = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_USERNAME))
                val p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_PASSWORD))
                val r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ROLE))
                return Account(id, u, p, r)
            }
        }
        return null
    }

    fun getAccountById(id: Long): Account? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ACCOUNT,
            null,
            "${DatabaseHelper.ACCOUNT_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val u = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_USERNAME))
                val p = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_PASSWORD))
                val r = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.ACCOUNT_ROLE))
                return Account(id, u, p, r)
            }
        }
        return null
    }

    // =========================================
    // EMPLOYEE OPERATIONS
    // =========================================

    fun getAllEmployees(query: String = ""): List<Employee> {
        val list = mutableListOf<Employee>()
        val db = dbHelper.readableDatabase
        val selection = if (query.isBlank()) null else {
            "${DatabaseHelper.EMP_FULLNAME} LIKE ? OR ${DatabaseHelper.EMP_CODE} LIKE ?"
        }
        val selectionArgs = if (query.isBlank()) null else {
            arrayOf("%$query%", "%$query%")
        }

        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            selection,
            selectionArgs,
            null, null,
            "${DatabaseHelper.EMP_CODE} ASC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.EMP_ID))
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                val accIdIdx = c.getColumnIndexOrThrow(DatabaseHelper.EMP_ACCOUNT_ID)
                val accId = if (c.isNull(accIdIdx)) null else c.getLong(accIdIdx)

                list.add(Employee(id, code, name, email, phone, rate, accId))
            }
        }
        return list
    }

    fun getEmployeeById(id: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            "${DatabaseHelper.EMP_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                val accIdIdx = c.getColumnIndexOrThrow(DatabaseHelper.EMP_ACCOUNT_ID)
                val accId = if (c.isNull(accIdIdx)) null else c.getLong(accIdIdx)
                return Employee(id, code, name, email, phone, rate, accId)
            }
        }
        return null
    }

    fun getEmployeeByAccountId(accountId: Long): Employee? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_EMPLOYEE,
            null,
            "${DatabaseHelper.EMP_ACCOUNT_ID} = ?",
            arrayOf(accountId.toString()),
            null, null, null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.EMP_ID))
                val code = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_CODE))
                val name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))
                val email = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_EMAIL))
                val phone = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_PHONE))
                val rate = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.EMP_HOURLY_RATE))
                return Employee(id, code, name, email, phone, rate, accountId)
            }
        }
        return null
    }

    fun addEmployeeWithAccount(
        code: String,
        fullname: String,
        email: String,
        phone: String,
        hourlyRate: Double,
        username: String,
        password: String
    ): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Insert Account
            val accValues = ContentValues().apply {
                put(DatabaseHelper.ACCOUNT_USERNAME, username)
                put(DatabaseHelper.ACCOUNT_PASSWORD, password)
                put(DatabaseHelper.ACCOUNT_ROLE, "EMPLOYEE")
            }
            val accId = db.insert(DatabaseHelper.TABLE_ACCOUNT, null, accValues)
            if (accId == -1L) return false

            // Insert Employee
            val empValues = ContentValues().apply {
                put(DatabaseHelper.EMP_CODE, code)
                put(DatabaseHelper.EMP_FULLNAME, fullname)
                put(DatabaseHelper.EMP_EMAIL, email)
                put(DatabaseHelper.EMP_PHONE, phone)
                put(DatabaseHelper.EMP_HOURLY_RATE, hourlyRate)
                put(DatabaseHelper.EMP_ACCOUNT_ID, accId)
            }
            val empId = db.insert(DatabaseHelper.TABLE_EMPLOYEE, null, empValues)
            if (empId == -1L) return false

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun updateEmployee(employee: Employee): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.EMP_CODE, employee.code)
            put(DatabaseHelper.EMP_FULLNAME, employee.fullname)
            put(DatabaseHelper.EMP_EMAIL, employee.email)
            put(DatabaseHelper.EMP_PHONE, employee.phone)
            put(DatabaseHelper.EMP_HOURLY_RATE, employee.hourlyRate)
        }
        val count = db.update(
            DatabaseHelper.TABLE_EMPLOYEE,
            values,
            "${DatabaseHelper.EMP_ID} = ?",
            arrayOf(employee.id.toString())
        )
        return count > 0
    }

    fun deleteEmployee(employeeId: Long): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val emp = getEmployeeById(employeeId)
            if (emp?.accountId != null) {
                db.delete(
                    DatabaseHelper.TABLE_ACCOUNT,
                    "${DatabaseHelper.ACCOUNT_ID} = ?",
                    arrayOf(emp.accountId.toString())
                )
            }
            val count = db.delete(
                DatabaseHelper.TABLE_EMPLOYEE,
                "${DatabaseHelper.EMP_ID} = ?",
                arrayOf(employeeId.toString())
            )
            db.setTransactionSuccessful()
            return count > 0
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            db.endTransaction()
        }
    }

    // =========================================
    // WORK LOG & ATTENDANCE OPERATIONS
    // =========================================

    fun getTodayWorkLog(employeeId: Long, todayDate: String): WorkLog? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_WORK_LOG,
            null,
            "${DatabaseHelper.WL_EMPLOYEE_ID} = ? AND ${DatabaseHelper.WL_WORK_DATE} = ?",
            arrayOf(employeeId.toString(), todayDate),
            null, null,
            "${DatabaseHelper.WL_ID} DESC"
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.WL_ID))
                val inTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_IN_TIME))
                val outTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_OUT_TIME))
                val totalH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.WL_TOTAL_HOURS))
                val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_STATUS))
                return WorkLog(id, employeeId, todayDate, inTime, outTime, totalH, status)
            }
        }
        return null
    }

    fun checkIn(employeeId: Long, workDate: String, checkInTime: String): Pair<Boolean, String> {
        val existing = getTodayWorkLog(employeeId, workDate)
        if (existing != null) {
            return Pair(false, "Bản ghi chấm công hôm nay đã tồn tại!")
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.WL_EMPLOYEE_ID, employeeId)
            put(DatabaseHelper.WL_WORK_DATE, workDate)
            put(DatabaseHelper.WL_CHECK_IN_TIME, checkInTime)
            put(DatabaseHelper.WL_STATUS, "CHECKED_IN")
        }
        val id = db.insert(DatabaseHelper.TABLE_WORK_LOG, null, values)
        return if (id != -1L) Pair(true, "Check-in thành công!") else Pair(false, "Check-in thất bại!")
    }

    fun checkOut(employeeId: Long, workDate: String, checkOutTime: String): Pair<Boolean, String> {
        val existing = getTodayWorkLog(employeeId, workDate)
            ?: return Pair(false, "Bạn chưa Check-in hôm nay!")

        if (existing.status == "COMPLETED") {
            return Pair(false, "Bạn đã hoàn thành Check-out hôm nay!")
        }

        val checkInStr = existing.checkInTime ?: "08:00:00"
        val calculatedHours = calculateHours(checkInStr, checkOutTime)

        if (calculatedHours <= 0) {
            return Pair(false, "Thời gian Check-out phải sau thời gian Check-in!")
        }

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.WL_CHECK_OUT_TIME, checkOutTime)
            put(DatabaseHelper.WL_TOTAL_HOURS, calculatedHours)
            put(DatabaseHelper.WL_STATUS, "COMPLETED")
        }

        val count = db.update(
            DatabaseHelper.TABLE_WORK_LOG,
            values,
            "${DatabaseHelper.WL_ID} = ?",
            arrayOf(existing.id.toString())
        )

        return if (count > 0) {
            Pair(true, "Check-out thành công! Tổng giờ làm: String.format(Locale.US, \"%.2f\", $calculatedHours)h")
        } else {
            Pair(false, "Check-out thất bại!")
        }
    }

    fun getWorkLogsForEmployee(employeeId: Long): List<WorkLog> {
        val list = mutableListOf<WorkLog>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_WORK_LOG,
            null,
            "${DatabaseHelper.WL_EMPLOYEE_ID} = ?",
            arrayOf(employeeId.toString()),
            null, null,
            "${DatabaseHelper.WL_WORK_DATE} DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.WL_ID))
                val date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_WORK_DATE))
                val inTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_IN_TIME))
                val outTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_OUT_TIME))
                val totalH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.WL_TOTAL_HOURS))
                val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_STATUS))
                list.add(WorkLog(id, employeeId, date, inTime, outTime, totalH, status))
            }
        }
        return list
    }

    fun getAllWorkLogs(): List<Pair<WorkLog, String>> {
        val list = mutableListOf<Pair<WorkLog, String>>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT w.*, e.${DatabaseHelper.EMP_FULLNAME} 
            FROM ${DatabaseHelper.TABLE_WORK_LOG} w
            JOIN ${DatabaseHelper.TABLE_EMPLOYEE} e ON w.${DatabaseHelper.WL_EMPLOYEE_ID} = e.${DatabaseHelper.EMP_ID}
            ORDER BY w.${DatabaseHelper.WL_WORK_DATE} DESC, w.${DatabaseHelper.WL_ID} DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.WL_ID))
                val empId = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.WL_EMPLOYEE_ID))
                val date = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_WORK_DATE))
                val inTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_IN_TIME))
                val outTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_CHECK_OUT_TIME))
                val totalH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.WL_TOTAL_HOURS))
                val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.WL_STATUS))
                val empName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))

                list.add(Pair(WorkLog(id, empId, date, inTime, outTime, totalH, status), empName))
            }
        }
        return list
    }

    private fun calculateHours(timeIn: String, timeOut: String): Double {
        return try {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date1 = format.parse(timeIn)
            val date2 = format.parse(timeOut)
            if (date1 != null && date2 != null) {
                val diffMillis = date2.time - date1.time
                if (diffMillis <= 0) return 0.0
                diffMillis.toDouble() / (1000 * 60 * 60)
            } else 0.0
        } catch (e: Exception) {
            // Try HH:mm format fallback
            try {
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date1 = format.parse(timeIn)
                val date2 = format.parse(timeOut)
                if (date1 != null && date2 != null) {
                    val diffMillis = date2.time - date1.time
                    if (diffMillis <= 0) return 0.0
                    diffMillis.toDouble() / (1000 * 60 * 60)
                } else 0.0
            } catch (ex: Exception) {
                0.0
            }
        }
    }

    // =========================================
    // SALARY OPERATIONS
    // =========================================

    fun calculateAndSaveSalary(month: Int, year: Int): Int {
        val employees = getAllEmployees()
        val db = dbHelper.writableDatabase
        var countCalculated = 0

        val monthStr = if (month < 10) "0$month" else "$month"
        val prefix = "$year-$monthStr"

        for (emp in employees) {
            // Get all completed work logs for this employee in month/year
            val workLogs = getWorkLogsForEmployee(emp.id).filter {
                it.workDate.startsWith(prefix) && it.status == "COMPLETED"
            }

            var totalHours = 0.0
            var overtimeHours = 0.0

            for (log in workLogs) {
                totalHours += log.totalHours
                // Daily overtime threshold (e.g. > 8 hours)
                if (log.totalHours > 8.0) {
                    overtimeHours += (log.totalHours - 8.0)
                }
            }

            // Regular hours = total - daily overtime
            val regularHours = max(0.0, totalHours - overtimeHours)
            // Overtime hours paid at 1.5x rate
            val totalSalary = (regularHours * emp.hourlyRate) + (overtimeHours * emp.hourlyRate * 1.5)

            // Check if salary record already exists
            val cursor = db.query(
                DatabaseHelper.TABLE_SALARY,
                null,
                "${DatabaseHelper.SAL_EMPLOYEE_ID} = ? AND ${DatabaseHelper.SAL_MONTH} = ? AND ${DatabaseHelper.SAL_YEAR} = ?",
                arrayOf(emp.id.toString(), month.toString(), year.toString()),
                null, null, null
            )

            val exists = cursor.use { c -> c.moveToFirst() }

            val values = ContentValues().apply {
                put(DatabaseHelper.SAL_EMPLOYEE_ID, emp.id)
                put(DatabaseHelper.SAL_MONTH, month)
                put(DatabaseHelper.SAL_YEAR, year)
                put(DatabaseHelper.SAL_TOTAL_HOURS, totalHours)
                put(DatabaseHelper.SAL_OVERTIME_HOURS, overtimeHours)
                put(DatabaseHelper.SAL_TOTAL_SALARY, totalSalary)
            }

            if (exists) {
                db.update(
                    DatabaseHelper.TABLE_SALARY,
                    values,
                    "${DatabaseHelper.SAL_EMPLOYEE_ID} = ? AND ${DatabaseHelper.SAL_MONTH} = ? AND ${DatabaseHelper.SAL_YEAR} = ?",
                    arrayOf(emp.id.toString(), month.toString(), year.toString())
                )
            } else {
                values.put(DatabaseHelper.SAL_STATUS, "PENDING")
                db.insert(DatabaseHelper.TABLE_SALARY, null, values)
            }
            countCalculated++
        }
        return countCalculated
    }

    fun getSalariesByEmployee(employeeId: Long): List<Salary> {
        val list = mutableListOf<Salary>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SALARY,
            null,
            "${DatabaseHelper.SAL_EMPLOYEE_ID} = ?",
            arrayOf(employeeId.toString()),
            null, null,
            "${DatabaseHelper.SAL_YEAR} DESC, ${DatabaseHelper.SAL_MONTH} DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.SAL_ID))
                val month = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SAL_MONTH))
                val year = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SAL_YEAR))
                val totalH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_TOTAL_HOURS))
                val otH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_OVERTIME_HOURS))
                val totalSal = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_TOTAL_SALARY))
                val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SAL_STATUS))
                list.add(Salary(id, employeeId, month, year, totalH, otH, totalSal, status))
            }
        }
        return list
    }

    fun getAllSalaries(): List<Pair<Salary, String>> {
        val list = mutableListOf<Pair<Salary, String>>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT s.*, e.${DatabaseHelper.EMP_FULLNAME} 
            FROM ${DatabaseHelper.TABLE_SALARY} s
            JOIN ${DatabaseHelper.TABLE_EMPLOYEE} e ON s.${DatabaseHelper.SAL_EMPLOYEE_ID} = e.${DatabaseHelper.EMP_ID}
            ORDER BY s.${DatabaseHelper.SAL_YEAR} DESC, s.${DatabaseHelper.SAL_MONTH} DESC, e.${DatabaseHelper.EMP_CODE} ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.SAL_ID))
                val empId = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.SAL_EMPLOYEE_ID))
                val month = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SAL_MONTH))
                val year = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.SAL_YEAR))
                val totalH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_TOTAL_HOURS))
                val otH = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_OVERTIME_HOURS))
                val totalSal = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.SAL_TOTAL_SALARY))
                val status = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.SAL_STATUS))
                val empName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.EMP_FULLNAME))

                list.add(Pair(Salary(id, empId, month, year, totalH, otH, totalSal, status), empName))
            }
        }
        return list
    }

    fun updateSalaryStatus(salaryId: Long, newStatus: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.SAL_STATUS, newStatus)
        }
        val count = db.update(
            DatabaseHelper.TABLE_SALARY,
            values,
            "${DatabaseHelper.SAL_ID} = ?",
            arrayOf(salaryId.toString())
        )
        return count > 0
    }
}
