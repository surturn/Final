package com.sydney.sydney.ui.theme.live

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.sydney.sydney.navigation.ROUTE_HOME
import kotlinx.coroutines.tasks.await

data class Hostel(
    var id: String = "",
    val name: String = "",
    val description: String ="",
    val price: String ="",
    var imageUrl: String = ""
)



@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelListScreen(navController: NavController, hostels: List<Hostel>) {
    var isLoading by remember { mutableStateOf(true) }
    var hostelList by remember { mutableStateOf(emptyList<Hostel>()) }
    var displayedHostelCount by remember { mutableStateOf(20) }
    var progress by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        fetchHostels { fetchedHostels ->
            hostelList = fetchedHostels
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Hostels",fontSize = 30.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_HOME)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = Color.White
                        )
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
//                    .background(Color.Black)
            ) {
                if (isLoading) {
                    // Progress indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(progress = progress / 100f)
                        Text(text = "Loading Hostels... ", fontSize = 20.sp)
                    }
                } else {
                    if (hostelList.isEmpty()) {
                        // No live found
                        Text(text = "No Hostel found")
                    } else {
                        // Hostels list
                        LazyVerticalGrid(columns = GridCells.Fixed(1)) {
                            items(hostelList.take(displayedHostelCount)) { hostel ->
                                HostelListItem(hostel) {
                                    navController.navigate("hostelDetail/${hostel.id}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Load More Button
                        if (displayedHostelCount < hostelList.size) {
                            Button(
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xff0FB06A)),
                                onClick = { displayedHostelCount += 1 },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(text = "More Hostels")
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun HostelListItem(hostel: Hostel, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick(hostel.id) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Hostel Image
            Image(
                painter = rememberImagePainter(hostel.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Hostel Details
            Column {
                Text(text = hostel.name)
                Text(text = "Price: ${hostel.price}")
            }
        }
    }
}

private suspend fun fetchHostels(onSuccess: (List<Hostel>) -> Unit) {
    val firestore = Firebase.firestore
    val snapshot = firestore.collection("hostels").get().await()
    val hostelList = snapshot.documents.mapNotNull { doc ->
        val hostel = doc.toObject<Hostel>()
        hostel?.id = doc.id
        hostel
    }
    onSuccess(hostelList)
}

suspend fun fetchHostel(hostelId: String, onSuccess: (Hostel?) -> Unit) {
    val firestore = Firebase.firestore
    val docRef = firestore.collection("hostels").document(hostelId)
    val snapshot = docRef.get().await()
    val hostel = snapshot.toObject<Hostel>()
    onSuccess(hostel)
}

