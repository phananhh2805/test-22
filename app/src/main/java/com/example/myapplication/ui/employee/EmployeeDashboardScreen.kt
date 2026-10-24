package com.example.myapplication.ui.employee

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Employee
import com.example.myapplication.data.model.Salary
import com.example.myapplication.data.model.UserSession
import com.example.myapplication.data.model.WorkLog
import com.example.myapplication.data.repository.AppRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    userSession: UserSession,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }

    var employee by remember { mutableStateOf<Employee?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(userSession.employeeId) {
        if (userSession.employeeId != null) {
            employee = repository.getEmployeeById(userSession.employeeId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = employee?.fullname ?: userSession.fullname ?: "Nhân Viên",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mã NV: ${employee?.code ?: "---"} | Lương: ${String.format(Locale.US, "%.0f", employee?.hourlyRate ?: 0.0)} VNĐ/h",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TouchApp, contentDescription = null) },
                    label = { Text("Chấm công") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Lịch sử") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    label = { Text("Lương cá nhân") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (employee == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> CheckInOutTab(repository, employee!!)
                    1 -> AttendanceHistoryTab(repository, employee!!)
                    2 -> MySalaryTab(repository, employee!!)
                }
            }
        }
    }
}

// ==========================================
// TAB 1: CHECK-IN / CHECK-OUT
// ==========================================

@Composable
private fun CheckInOutTab(
    repository: AppRepository,
    employee: Employee
) {
    val context = LocalContext.current

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val todayDate = remember { dateFormat.format(Date()) }

    var todayWorkLog by remember { mutableStateOf<WorkLog?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        todayWorkLog = repository.getTodayWorkLog(employee.id, todayDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Hôm nay: $todayDate", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        todayWorkLog == null -> "Trạng thái: Chưa Check-In"
                        todayWorkLog?.status == "CHECKED_IN" -> "Trạng thái: Đang trong ca làm việc"
                        else -> "Trạng thái: Đã hoàn thành ca hôm nay"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        todayWorkLog == null -> Color.Gray
                        todayWorkLog?.status == "CHECKED_IN" -> Color(0xFFE65100)
                        else -> Color(0xFF2E7D32)
                    },
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (todayWorkLog == null) {
            // Check-In Button
            Button(
                onClick = {
                    val currentTime = timeFormat.format(Date())
                    val (success, msg) = repository.checkIn(employee.id, todayDate, currentTime)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) refreshTrigger++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CHECK IN NGAY", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else if (todayWorkLog?.status == "CHECKED_IN") {
            // Checked In -> Show Check-Out Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Thời gian Check-In: ${todayWorkLog?.checkInTime ?: "--:--"}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val currentTime = timeFormat.format(Date())
                    val (success, msg) = repository.checkOut(employee.id, todayDate, currentTime)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) refreshTrigger++
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CHECK OUT KẾT THÚC CA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            // Completed for today
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Đã Chấm Công Xong Hôm Nay!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Giờ vào: ${todayWorkLog?.checkInTime}", fontSize = 14.sp)
                    Text("Giờ ra: ${todayWorkLog?.checkOutTime}", fontSize = 14.sp)
                    Text(
                        "Tổng giờ làm: ${String.format(Locale.US, "%.2f", todayWorkLog?.totalHours ?: 0.0)} giờ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: ATTENDANCE HISTORY
// ==========================================

@Composable
private fun AttendanceHistoryTab(
    repository: AppRepository,
    employee: Employee
) {
    var logs by remember { mutableStateOf(emptyList<WorkLog>()) }

    LaunchedEffect(employee.id) {
        logs = repository.getWorkLogsForEmployee(employee.id)
    }

    val totalHoursThisMonth = logs.sumOf { it.totalHours }
    val totalDaysWorked = logs.count { it.status == "COMPLETED" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng ngày làm", fontSize = 13.sp, color = Color.Gray)
                    Text("$totalDaysWorked ngày", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tổng giờ tích lũy", fontSize = 13.sp, color = Color.Gray)
                    Text("${String.format(Locale.US, "%.1f", totalHoursThisMonth)} giờ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Lịch Sử Chấm Công Cá Nhân", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có lịch sử chấm công", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(logs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ngày: ${log.workDate}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    if (log.status == "COMPLETED") "Hoàn thành" else "Đang làm",
                                    fontWeight = FontWeight.Bold,
                                    color = if (log.status == "COMPLETED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vào: ${log.checkInTime ?: "--:--"}", fontSize = 13.sp)
                                Text("Ra: ${log.checkOutTime ?: "--:--"}", fontSize = 13.sp)
                                Text(
                                    "Tổng: ${String.format(Locale.US, "%.2f", log.totalHours)}h",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: MY SALARY
// ==========================================

@Composable
private fun MySalaryTab(
    repository: AppRepository,
    employee: Employee
) {
    var salaries by remember { mutableStateOf(emptyList<Salary>()) }

    LaunchedEffect(employee.id) {
        salaries = repository.getSalariesByEmployee(employee.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Bảng Lương Hàng Tháng Của Tôi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (salaries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có bảng lương nào được tính", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(salaries) { sal ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tháng ${sal.month}/${sal.year}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    if (sal.status == "PAID") "ĐÃ NHẬN" else "ĐANG CHỜ",
                                    fontWeight = FontWeight.Bold,
                                    color = if (sal.status == "PAID") Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tổng giờ làm: ${String.format(Locale.US, "%.1f", sal.totalHours)}h", fontSize = 13.sp)
                            Text("Giờ tăng ca: ${String.format(Locale.US, "%.1f", sal.overtimeHours)}h", fontSize = 13.sp)
                            Text("Đơn giá: ${String.format(Locale.US, "%.0f", employee.hourlyRate)} VNĐ/h", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Thực nhận:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "${String.format(Locale.US, "%.0f", sal.totalSalary)} VNĐ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
