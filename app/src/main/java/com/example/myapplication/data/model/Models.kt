package com.example.myapplication.data.model

data class Account(
    val id: Long = 0,
    val username: String,
    val password: String,
    val role: String // "ADMIN" or "EMPLOYEE"
)

data class Employee(
    val id: Long = 0,
    val code: String,
    val fullname: String,
    val email: String,
    val phone: String,
    val hourlyRate: Double,
    val accountId: Long?
)

data class WorkLog(
    val id: Long = 0,
    val employeeId: Long,
    val workDate: String, // YYYY-MM-DD
    val checkInTime: String? = null, // HH:mm:ss
    val checkOutTime: String? = null, // HH:mm:ss
    val totalHours: Double = 0.0,
    val status: String // "CHECKED_IN", "COMPLETED"
)

data class Salary(
    val id: Long = 0,
    val employeeId: Long,
    val month: Int,
    val year: Int,
    val totalHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val totalSalary: Double = 0.0,
    val status: String // "PENDING", "PAID"
)

data class UserSession(
    val accountId: Long,
    val username: String,
    val role: String,
    val employeeId: Long? = null,
    val fullname: String? = null
)
