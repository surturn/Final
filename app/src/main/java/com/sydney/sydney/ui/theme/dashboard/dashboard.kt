package com.sydney.sydney.ui.theme.dashboard


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import com.sydney.sydney.navigation.ROUTE_VIEW_HOSTELS
import com.sydney.sydney.navigation.ROUTE_VIEW_STUDENTS


private var progressDialog: ProgressDialog? = null
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DashboardScreen(navController: NavHostController) {
    var campus by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var studentCount by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    val firestores = Firebase.firestore
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        navController.popBackStack()
    }

    // Fetch user details from Firestore
    LaunchedEffect(key1 = currentUser?.uid) {
        if (currentUser != null) {
            val userDocRef = firestore.collection("Students").document(currentUser.uid)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        user = document.toObject<User>()
                        campus = user?.campus ?: ""
                        name = user?.name ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(context, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        }
    }

    LaunchedEffect(Unit) {
        firestores.collection("Students")
            .get()
            .addOnSuccessListener { result ->
                studentCount = result.size()
            }
            .addOnFailureListener { exception ->
                // Handle failures
                Toast.makeText(context, "Failed to load students count: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Dashboard", color = Color.White, fontSize = 30.sp)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon", tint = Color.White)
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                val dashboardItems = listOf(
                    DashboardItemData(
                        title = "Profile",
                        icon = Icons.Default.AccountCircle,
                        badgeCount = 0,
                        onClick = {// Navigate to profile screen
                           // navController.navigate(ROUTE_PROFILE) {
                                //popUpTo("home") { inclusive = true }
                        }
                    ),

                    DashboardItemData(
                        title = "Hostels",
                        icon = Icons.Default.Home,
                        badgeCount = 0,
                        onClick = {
                            navController.navigate(ROUTE_VIEW_HOSTELS)
                        }
                    ),
                    DashboardItemData(
                        title = "Students",
                        icon = Icons.Default.Person,
                        badgeCount = 0,
                        onClick = {
                            navController.navigate(ROUTE_VIEW_STUDENTS)
                        }
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(dashboardItems) { item ->
                        DashboardItem(item)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardItem(item: DashboardItemData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 8.dp,
        backgroundColor = Color.White,
        onClick = item.onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = "Dashboard Icon",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.subtitle1,
                color = Color.Black
            )
            if (item.badgeCount > 0) {
                Badge(count = item.badgeCount)
            }
        }
    }
}

@Composable
fun Badge(count: Int) {
    Box(
        modifier = Modifier
            .padding(start = 8.dp)
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.Red),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.caption,
            color = Color.White
        )
    }
}

data class DashboardItemData(
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int,
    val onClick: () -> Unit
)

data class User(
    val userId: String = "",
    val campus: String = "",
    val name: String = ""
)

fun saveUserDetails(user: User, onComplete: (Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("Students").document(user.userId)
        .set(user, SetOptions.merge())
        .addOnSuccessListener {
            progressDialog?.dismiss()
            onComplete(true)
        }
        .addOnFailureListener { e ->
            progressDialog?.dismiss()
            onComplete(false)
        }
}

@Composable
fun ProfileForm(
    name: String,
    campus: String,
    onNameChange: (String) -> Unit,
    onCampusChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = campus,
            onValueChange = onCampusChange,
            label = { Text("Campus") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSave) {
            Text("Save")
        }
    }
}