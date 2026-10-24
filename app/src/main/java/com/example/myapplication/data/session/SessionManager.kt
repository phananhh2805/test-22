package com.example.myapplication.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.model.UserSession

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "employee_salary_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ACCOUNT_ID = "account_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_EMPLOYEE_ID = "employee_id"
        private const val KEY_FULLNAME = "fullname"
    }

    fun saveSession(session: UserSession) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_ACCOUNT_ID, session.accountId)
        editor.putString(KEY_USERNAME, session.username)
        editor.putString(KEY_ROLE, session.role)
        if (session.employeeId != null) {
            editor.putLong(KEY_EMPLOYEE_ID, session.employeeId)
        } else {
            editor.remove(KEY_EMPLOYEE_ID)
        }
        editor.putString(KEY_FULLNAME, session.fullname ?: "")
        editor.apply()
    }

    fun getSession(): UserSession? {
        if (!isLoggedIn()) return null
        val accountId = prefs.getLong(KEY_ACCOUNT_ID, -1L)
        val username = prefs.getString(KEY_USERNAME, "") ?: ""
        val role = prefs.getString(KEY_ROLE, "") ?: ""
        val employeeId = if (prefs.contains(KEY_EMPLOYEE_ID)) {
            prefs.getLong(KEY_EMPLOYEE_ID, -1L).takeIf { it != -1L }
        } else null
        val fullname = prefs.getString(KEY_FULLNAME, "")

        if (accountId == -1L || username.isEmpty() || role.isEmpty()) {
            return null
        }

        return UserSession(
            accountId = accountId,
            username = username,
            role = role,
            employeeId = employeeId,
            fullname = fullname
        )
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
