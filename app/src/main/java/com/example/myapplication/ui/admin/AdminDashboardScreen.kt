package com.example.myapplication.ui.admin

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
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    userSession: UserSession,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AppRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quản Lý Nhân Viên & Lương", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Xin chào, ${userSession.fullname} [ADMIN]",
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
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Nhân viên") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    label = { Text("Chấm công") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    label = { Text("Bảng lương") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> EmployeesTab(repository)
                1 -> AttendanceLogsTab(repository)
                2 -> SalaryManagementTab(repository)
            }
        }
    }
}

// ==========================================
// TAB 1: EMPLOYEES MANAGEMENT
// ==========================================

@Composable
private fun EmployeesTab(repository: AppRepository) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var employees by remember { mutableStateOf(emptyList<Employee>()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<Employee?>(null) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    fun refreshList() {
        employees = repository.getAllEmployees(searchQuery)
    }

    LaunchedEffect(searchQuery) {
        refreshList()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm nhân viên", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Tìm kiếm tên hoặc mã NV...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (employees.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không tìm thấy nhân viên nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(employees, key = { it.id }) { emp ->
                        EmployeeItemCard(
                            employee = emp,
                            onEdit = { employeeToEdit = emp },
                            onDelete = { employeeToDelete = emp }
                        )
                    }
                }
            }
        }
    }

    // Add Employee Dialog
    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { code, name, email, phone, rate, username, pass ->
                val success = repository.addEmployeeWithAccount(code, name, email, phone, rate, username, pass)
                if (success) {
                    Toast.makeText(context, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show()
                    refreshList()
                    showAddDialog = false
                } else {
                    Toast.makeText(context, "Lỗi: Mã NV hoặc Tên đăng nhập đã tồn tại!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Edit Employee Dialog
    if (employeeToEdit != null) {
        EditEmployeeDialog(
            employee = employeeToEdit!!,
            onDismiss = { employeeToEdit = null },
            onConfirm = { updatedEmp ->
                val success = repository.updateEmployee(updatedEmp)
                if (success) {
                    Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                    refreshList()
                    employeeToEdit = null
                } else {
                    Toast.makeText(context, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (employeeToDelete != null) {
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa nhân viên ${employeeToDelete?.fullname} (${employeeToDelete?.code})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = repository.deleteEmployee(employeeToDelete!!.id)
                        if (success) {
                            Toast.makeText(context, "Đã xóa nhân viên", Toast.LENGTH_SHORT).show()
                            refreshList()
                        } else {
                            Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                        }
                        employeeToDelete = null
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun EmployeeItemCard(
    employee: Employee,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${employee.code} - ${employee.fullname}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Email: ${employee.email}", fontSize = 13.sp, color = Color.DarkGray)
                Text(text = "SĐT: ${employee.phone}", fontSize = 13.sp, color = Color.DarkGray)
                Text(
                    text = "Lương/giờ: ${String.format(Locale.US, "%.0f", employee.hourlyRate)} VNĐ/h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (code: String, name: String, email: String, phone: String, rate: Double, username: String, pass: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("50") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm Nhân Viên Mới", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Mã NV (ví dụ: EMP003)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và Tên") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Lương theo giờ (VNĐ/h)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Tên tài khoản") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    singleLine = true
                )

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateStr.toDoubleOrNull()
                    if (code.isBlank() || name.isBlank() || email.isBlank() || phone.isBlank() || username.isBlank() || password.isBlank() || rate == null) {
                        errorText = "Vui lòng nhập đầy đủ và đúng định dạng!"
                    } else {
                        onConfirm(code, name, email, phone, rate, username, password)
                    }
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
private fun EditEmployeeDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onConfirm: (Employee) -> Unit
) {
    var name by remember { mutableStateOf(employee.fullname) }
    var email by remember { mutableStateOf(employee.email) }
    var phone by remember { mutableStateOf(employee.phone) }
    var rateStr by remember { mutableStateOf(employee.hourlyRate.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa Thông Tin NV: ${employee.code}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và Tên") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Lương theo giờ (VNĐ/h)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateStr.toDoubleOrNull() ?: employee.hourlyRate
                    onConfirm(employee.copy(fullname = name, email = email, phone = phone, hourlyRate = rate))
                }
            ) {
                Text("Cập Nhật")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

// ==========================================
// TAB 2: ATTENDANCE LOGS
// ==========================================

@Composable
private fun AttendanceLogsTab(repository: AppRepository) {
    var logs by remember { mutableStateOf(emptyList<Pair<WorkLog, String>>()) }

    LaunchedEffect(Unit) {
        logs = repository.getAllWorkLogs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lịch Sử Chấm Công Tất Cả Nhân Viên", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có dữ liệu chấm công nào", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(logs) { (log, empName) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(empName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(log.workDate, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vào: ${log.checkInTime ?: "--:--"}", fontSize = 13.sp)
                                Text("Ra: ${log.checkOutTime ?: "--:--"}", fontSize = 13.sp)
                                Text(
                                    "Tổng: ${String.format(Locale.US, "%.1f", log.totalHours)}h",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Trạng thái: ${if (log.status == "COMPLETED") "Đã hoàn thành" else "Đang làm việc"}",
                                fontSize = 12.sp,
                                color = if (log.status == "COMPLETED") Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: SALARY MANAGEMENT
// ==========================================

@Composable
private fun SalaryManagementTab(repository: AppRepository) {
    val context = LocalContext.current
    var salaries by remember { mutableStateOf(emptyList<Pair<Salary, String>>()) }

    val calendar = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    fun refreshSalaries() {
        salaries = repository.getAllSalaries()
    }

    LaunchedEffect(Unit) {
        refreshSalaries()
    }

    val totalPaidAmount = salaries.filter { it.first.status == "PAID" }.sumOf { it.first.totalSalary }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Tổng Lương Đã Thanh Toán: ${String.format(Locale.US, "%.0f", totalPaidAmount)} VNĐ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tính lương tháng $selectedMonth/$selectedYear", fontWeight = FontWeight.SemiBold)
                    Button(
                        onClick = {
                            val count = repository.calculateAndSaveSalary(selectedMonth, selectedYear)
                            Toast.makeText(context, "Đã tính lương cho $count nhân viên!", Toast.LENGTH_SHORT).show()
                            refreshSalaries()
                        }
                    ) {
                        Text("Tính Lương")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Bảng Lương Hàng Tháng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        if (salaries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có bảng lương nào", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(salaries) { (sal, empName) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(empName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Tháng ${sal.month}/${sal.year}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Tổng giờ làm: ${String.format(Locale.US, "%.1f", sal.totalHours)}h (Tăng ca: ${String.format(Locale.US, "%.1f", sal.overtimeHours)}h)", fontSize = 13.sp)
                            Text(
                                "Thành tiền: ${String.format(Locale.US, "%.0f", sal.totalSalary)} VNĐ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trạng thái: ${if (sal.status == "PAID") "ĐÃ THANH TOÁN" else "CHỜ DUYỆT"}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (sal.status == "PAID") Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                    fontSize = 13.sp
                                )

                                if (sal.status != "PAID") {
                                    OutlinedButton(
                                        onClick = {
                                            repository.updateSalaryStatus(sal.id, "PAID")
                                            Toast.makeText(context, "Đã duyệt thanh toán lương!", Toast.LENGTH_SHORT).show()
                                            refreshSalaries()
                                        }
                                    ) {
                                        Text("Duyệt Lương", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
