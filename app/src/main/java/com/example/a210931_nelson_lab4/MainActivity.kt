package com.example.a210931_nelson_project1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.a210931_nelson_project1.ui.theme.FixMyCityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }

            FixMyCityTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FixMyCityApp(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}

enum class FixMyCityScreen(val title: String) {
    Home("Home"),
    History("My Reports"),
    Notifications("Notifications"),
    Profile("User Profile"),
    ReportStep1("Step 1: Basic Info"),
    ReportStep2("Step 2: Issue Details"),
    Summary("Step 3: Review")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixMyCityApp(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController(),
    viewModel: ReportViewModel = viewModel() // Compose akan cari fail ReportViewModel.kt anda secara automatik
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: FixMyCityScreen.Home.name

    val showTopAppBar = currentRoute in listOf(
        FixMyCityScreen.History.name,
        FixMyCityScreen.Notifications.name,
        FixMyCityScreen.Profile.name,
        FixMyCityScreen.ReportStep1.name,
        FixMyCityScreen.ReportStep2.name,
        FixMyCityScreen.Summary.name
    )

    val showBottomBar = currentRoute in listOf(
        FixMyCityScreen.Home.name,
        FixMyCityScreen.History.name,
        FixMyCityScreen.Notifications.name,
        FixMyCityScreen.Profile.name
    )

    // Ambil data profil dari ViewModel (Kebal Rotate Screen)
    val userName by viewModel.userName.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    Scaffold(
        topBar = {
            if (showTopAppBar) {
                TopAppBar(
                    title = { Text(FixMyCityScreen.valueOf(currentRoute).title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                    navigationIcon = {
                        if (!showBottomBar) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AestheticBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(FixMyCityScreen.Home.name)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = FixMyCityScreen.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = FixMyCityScreen.Home.name) {
                FixMyCityHome(isDarkTheme, onThemeToggle, userName)
            }

            composable(route = FixMyCityScreen.History.name) {
                val historyList by viewModel.reportHistory.collectAsState()
                HistoryScreen(historyList)
            }

            composable(route = FixMyCityScreen.Notifications.name) {
                NotificationsScreen()
            }

            composable(route = FixMyCityScreen.Profile.name) {
                ProfileScreen(
                    name = userName,
                    phone = userPhone,
                    email = userEmail,
                    onSaveProfile = { newName, newPhone, newEmail ->
                        viewModel.updateProfile(newName, newPhone, newEmail)
                    }
                )
            }

            composable(route = FixMyCityScreen.ReportStep1.name) {
                val uiState by viewModel.uiState.collectAsState()
                ReportStep1Screen(
                    uiState = uiState,
                    onCategoryChange = { viewModel.updateCategory(it) },
                    onLocationChange = { viewModel.updateLocation(it) },
                    onNextButtonClicked = { navController.navigate(FixMyCityScreen.ReportStep2.name) }
                )
            }

            composable(route = FixMyCityScreen.ReportStep2.name) {
                val uiState by viewModel.uiState.collectAsState()
                ReportStep2Screen(
                    uiState = uiState,
                    onDetailsChange = { viewModel.updateDetails(it) },
                    onNextButtonClicked = { navController.navigate(FixMyCityScreen.Summary.name) }
                )
            }

            composable(route = FixMyCityScreen.Summary.name) {
                val uiState by viewModel.uiState.collectAsState()
                ReportSummaryScreen(
                    uiState = uiState,
                    onSubmitButtonClicked = {
                        viewModel.submitReport()
                        navController.navigate(FixMyCityScreen.History.name) {
                            popUpTo(FixMyCityScreen.Home.name) { inclusive = false }
                        }
                    }
                )
            }
        }
    }
}

// ------------------------------------------------------------------
// SCREENS (Komponen UI yang kita bina sebelum ini diletakkan di sini)
// ------------------------------------------------------------------

@Composable
fun FixMyCityHome(isDarkTheme: Boolean, onThemeToggle: (Boolean) -> Unit, userName: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("FixMyCity", color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Welcome, $userName", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Switch(checked = isDarkTheme, onCheckedChange = onThemeToggle, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary, checkedTrackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCard("Pending", "12", Color(0xFFE57373))
            StatCard("Fixed", "85", Color(0xFF81C784))
            StatCard("Upvotes", "1.2k", MaterialTheme.colorScheme.tertiary)
        }
        Text("Trending Issues Near You", modifier = Modifier.padding(start = 16.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            IssueCard("Lubang Besar Berbahaya", "Pothole", 142, "In Progress", "KM 4.5, Jalan Ipoh, Kuala Lumpur", "Lubang dikesan semakin membesar selepas hujan lebat kelmarin. Boleh membahayakan penunggang motosikal pada waktu malam.")
            IssueCard("Lampu Isyarat Rosak", "Streetlight", 89, "Pending", "Persimpangan Jalan Raja Laut", "Lampu isyarat asyik berkelip merah menyebabkan kesesakan teruk setiap pagi. Mohon pihak DBKL hantar technician segera.")
            IssueCard("Paip Utama Pecah", "Water Pipe Leak", 56, "Pending", "Depan Sekolah Kebangsaan Sentul", "Air mencurah-curah keluar ke atas jalan raya sejak 6 pagi. Tekanan air di rumah berdekatan semakin rendah.")
            IssueCard("Vandalisme di Taman", "Vandalism", 24, "Fixed", "Taman Tasik Titiwangsa (Zon B)", "Papan tanda arah dan bangku rehat diconteng dengan cat semburan. Perlu dibersihkan untuk keselesaan pengunjung.")
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun NotificationsScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Official Updates", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
        NotificationItem("Report Resolved!", "Your report 'Pothole at Jalan Ipoh' has been successfully fixed by the city council. Thank you for making our city safer!", "2 hours ago")
        NotificationItem("Area Alert: Water Disruption", "Scheduled maintenance will cause a temporary water disruption in your area tomorrow from 9 AM to 5 PM.", "1 day ago")
        NotificationItem("Community Milestone Reached", "Our community just reached 500 fixed issues this month. Check the leaderboard!", "3 days ago")
    }
}

@Composable
fun NotificationItem(title: String, message: String, time: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(time, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun IssueCard(title: String, category: String, initialUpvotes: Int, status: String, address: String, description: String) {
    var expanded by remember { mutableStateOf(false) }
    var isUpvoted by remember { mutableStateOf(false) }
    var currentUpvotes by remember { mutableStateOf(initialUpvotes) }

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { expanded = !expanded }.animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Text("", fontSize = 24.sp) }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("$category • $status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isUpvoted) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)).clickable { isUpvoted = !isUpvoted; currentUpvotes = if (isUpvoted) currentUpvotes + 1 else currentUpvotes - 1 }.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Rounded.ThumbUp, contentDescription = "Upvote", tint = if (isUpvoted) Color(0xFFD84315) else MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Text(currentUpvotes.toString(), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (isUpvoted) Color(0xFFD84315) else MaterialTheme.colorScheme.tertiary)
                }
            }
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Text("Address:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(address, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Description:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ProfileScreen(name: String, phone: String, email: String, onSaveProfile: (String, String, String) -> Unit) {
    var tempName by remember { mutableStateOf(name) }
    var tempPhone by remember { mutableStateOf(phone) }
    var tempEmail by remember { mutableStateOf( value = email) }
    var showUpdateSuccess by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Person, contentDescription = "User Avatar", modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.tertiary) }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = name.ifEmpty { "Your Name" }, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        //Text(text = phone.ifEmpty { "Your Phone" }, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        //Text(text = email.ifEmpty { "Your Email" }, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Information", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = tempPhone, onValueChange = { tempPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = tempEmail, onValueChange = { tempEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { onSaveProfile(tempName, tempPhone, tempEmail); showUpdateSuccess = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), shape = RoundedCornerShape(12.dp)) {
            Text("UPDATE PROFILE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F))
        }
    }

    if (showUpdateSuccess) {
        AlertDialog(onDismissRequest = { showUpdateSuccess = false }, containerColor = MaterialTheme.colorScheme.surface, title = { Text("Profile Updated", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }, text = { Text("Your profile has been saved successfully", color = MaterialTheme.colorScheme.onSurface) }, confirmButton = { TextButton(onClick = { showUpdateSuccess = false }) { Text("OK", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold) } })
    }
}

@Composable
fun AestheticBottomNavBar(currentRoute: String, onNavigate: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), modifier = Modifier.fillMaxWidth().height(70.dp)) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { onNavigate(FixMyCityScreen.Home.name) }) { Icon(Icons.Rounded.Home, contentDescription = "Home", modifier = Modifier.size(28.dp), tint = if (currentRoute == FixMyCityScreen.Home.name) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                    IconButton(onClick = { onNavigate(FixMyCityScreen.History.name) }) { Icon(Icons.Rounded.Description , contentDescription = "History", modifier = Modifier.size(28.dp), tint = if (currentRoute == FixMyCityScreen.History.name) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                }
                Spacer(modifier = Modifier.width(70.dp))
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = { onNavigate(FixMyCityScreen.Notifications.name) }) { Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", modifier = Modifier.size(28.dp), tint = if (currentRoute == FixMyCityScreen.Notifications.name) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                    IconButton(onClick = { onNavigate(FixMyCityScreen.Profile.name) }) { Icon(Icons.Rounded.Person, contentDescription = "Profile", modifier = Modifier.size(28.dp), tint = if (currentRoute == FixMyCityScreen.Profile.name) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) }
                }
            }
        }
        FloatingActionButton(onClick = { onNavigate(FixMyCityScreen.ReportStep1.name) }, containerColor = MaterialTheme.colorScheme.tertiary, shape = CircleShape, elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp), modifier = Modifier.padding(bottom = 30.dp).size(64.dp)) { Icon(Icons.Rounded.Add, contentDescription = "Report", modifier = Modifier.size(36.dp), tint = Color(0xFF1C1B1F)) }
    }
}

@Composable
fun HistoryScreen(historyList: List<ReportItem>) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No reports submitted yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historyList.reversed()) { report ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(report.category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface); Text(report.status, color = Color(0xFFE57373), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(report.location, fontSize = 14.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(report.details, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportStep1Screen(uiState: ReportUiState, onCategoryChange: (String) -> Unit, onLocationChange: (String) -> Unit, onNextButtonClicked: () -> Unit) {
    val categories = listOf("Pothole", "Broken Streetlight", "Water Pipe Leak", "Vandalism", "Other")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(readOnly = true, value = uiState.category, onValueChange = { }, label = { Text("Select Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { categories.forEach { selectionOption -> DropdownMenuItem(text = { Text(selectionOption) }, onClick = { onCategoryChange(selectionOption); expanded = false }) } }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = uiState.location, onValueChange = onLocationChange, label = { Text("Specific Location") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNextButtonClicked, enabled = uiState.category.isNotEmpty() && uiState.location.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), shape = RoundedCornerShape(12.dp)) { Text("NEXT: ADD DETAILS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F)) }
    }
}

@Composable
fun ReportStep2Screen(uiState: ReportUiState, onDetailsChange: (String) -> Unit, onNextButtonClicked: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Describe the Issue", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = uiState.details, onValueChange = onDetailsChange, label = { Text("Provide specific details") }, modifier = Modifier.fillMaxWidth().height(180.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.tertiary, focusedLabelColor = MaterialTheme.colorScheme.tertiary))
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNextButtonClicked, enabled = uiState.details.isNotEmpty(), modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), shape = RoundedCornerShape(12.dp)) { Text("REVIEW REPORT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1C1B1F)) }
    }
}

@Composable
fun ReportSummaryScreen(uiState: ReportUiState, onSubmitButtonClicked: () -> Unit) {
    var showSuccessDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Please Verify Your Report", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Category:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(uiState.category, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Location:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(uiState.location, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Details:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(uiState.details, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { showSuccessDialog = true }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(12.dp)) { Text("SUBMIT", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) }
    }
    if (showSuccessDialog) {
        AlertDialog(onDismissRequest = { }, containerColor = MaterialTheme.colorScheme.surface, title = { Text("Success!", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) }, text = { Text("Your report for '${uiState.category}' has been sent.", color = MaterialTheme.colorScheme.onSurface) }, confirmButton = { TextButton(onClick = { showSuccessDialog = false; onSubmitButtonClicked() }) { Text("RETURN HOME", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold) } })
    }
}

@Composable
fun StatCard(label: String, value: String, highlightColor: Color) {
    Card(modifier = Modifier.width(105.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), shape = RoundedCornerShape(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp).fillMaxWidth()) { Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = highlightColor); Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
    }
}