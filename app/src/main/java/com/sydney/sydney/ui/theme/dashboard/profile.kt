package com.sydney.sydney.ui.theme.dashboard
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController, studentKey: String) {
    val context = LocalContext.current
    var studentName by remember { mutableStateOf("") }
    var studentCampus by remember { mutableStateOf("") }
    var studentEmail by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Fetch student data from Firestore
    LaunchedEffect(studentKey) {
        val db = Firebase.firestore
        db.collection("Students").document(studentKey).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    studentName = document.getString("studentName") ?: ""
                    studentCampus = document.getString("studentCampus") ?: ""
                    studentEmail = document.getString("studentEmail") ?: ""
                    location = document.getString("location") ?: ""
                    phone = document.getString("phone") ?: ""
                    imageUrl = document.getString("imageUrl") ?: ""
                } else {
                    Toast.makeText(context, "Student not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to fetch student data", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Student Profile")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                )
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(2.dp, Color.Gray, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Name: $studentName", color = Color.Cyan, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Campus: $studentCampus", color = Color.Cyan, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Email: $studentEmail", color = Color.Cyan, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Location: $location", color = Color.Cyan, style = MaterialTheme.typography.body1)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Phone: $phone", color = Color.Cyan, style = MaterialTheme.typography.body1)
            }
        }
    )
}
