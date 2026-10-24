package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.myapplication.data.session.SessionManager
import com.example.myapplication.ui.admin.AdminDashboardScreen
import com.example.myapplication.ui.employee.EmployeeDashboardScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(this)

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentSession by remember { mutableStateOf(sessionManager.getSession()) }

                    val logoutHandler = {
                        sessionManager.clearSession()
                        currentSession = null
                    }

                    val session = currentSession
                    when {
                        session == null -> {
                            LoginScreen(
                                onLoginSuccess = { newSession ->
                                    currentSession = newSession
                                }
                            )
                        }
                        session.role == "ADMIN" -> {
                            AdminDashboardScreen(
                                userSession = session,
                                onLogout = logoutHandler
                            )
                        }
                        else -> {
                            EmployeeDashboardScreen(
                                userSession = session,
                                onLogout = logoutHandler
                            )
                        }
                    }
                }
            }
        }
    }
}
