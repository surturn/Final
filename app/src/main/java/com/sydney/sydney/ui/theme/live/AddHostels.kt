@file:Suppress("NAME_SHADOWING")
package com.sydney.sydney.ui.theme.live
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sydney.sydney.navigation.ROUTE_HOME
import com.sydney.sydney.navigation.ROUTE_VIEW_HOSTELS
import java.util.UUID

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHostelScreen(navController: NavController, onHostelAdded: () -> Unit) {
    var hostelName by remember { mutableStateOf("") }
    var hostelDescription by remember { mutableStateOf("") }
    var hostelPrice by remember { mutableStateOf("") }
    var hostelImageUri by remember { mutableStateOf<Uri?>(null) }

    // Track if fields are empty
    var hostelNameError by remember { mutableStateOf(false) }
    var hostelDescriptionError by remember { mutableStateOf(false) }
    var hostelPriceError by remember { mutableStateOf(false) }
    var hostelImageError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            hostelImageUri = it
        }
    }
    BackHandler {
        navController.navigate(ROUTE_HOME)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Add Hostels", fontSize = 30.sp, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_VIEW_HOSTELS)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                item {
                    if (hostelImageUri != null) {
                        // Display selected image
                        Image(
                            painter = rememberImagePainter(hostelImageUri), // Using rememberImagePainter with Uri
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        // Display placeholder if no image selected
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Selected", modifier = Modifier.padding(8.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { launcher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Select Image",color= Color.Black)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = hostelName,
                        onValueChange = { hostelName = it },
                        label = { Text("Hostel Name",color= Color.White) },
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.Red,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedIndicatorColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = hostelDescription,
                        onValueChange = { hostelDescription = it },
                        label = { Text("Hostel Description",color= Color.White) },
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.Red,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedIndicatorColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = hostelPrice,
                        onValueChange = { hostelPrice = it },
                        label = { Text("Hostel Price",color= Color.White) },
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.Red,
                            unfocusedIndicatorColor = Color.Gray,
                            focusedIndicatorColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (hostelNameError) {
                        Text("Hostel Name is required", color = Color.Red)
                    }
                    if (hostelDescriptionError) {
                        Text("Hostel Description is required", color = Color.Red)
                    }
                    if (hostelPriceError) {
                        Text("Hostel Price is required", color = Color.Red)
                    }
                    if (hostelImageError) {
                        Text("Hostel Image is required", color = Color.Red)
                    }

                    // Button to add hostel
                    Button(
                        onClick = {
                            // Reset error flags

                            hostelNameError = hostelName.isBlank()
                            hostelDescriptionError = hostelDescription.isBlank()
                            hostelPriceError = hostelPrice.isBlank()
                            hostelImageError = hostelImageUri == null

                            // Add hostel if all fields are filled
                            if (!hostelNameError && !hostelDescriptionError && !hostelPriceError && !hostelImageError) {
                                addHostelToFirestore(
                                    navController,
                                    onHostelAdded,
                                    hostelName,
                                    hostelDescription,
                                    hostelPrice,
                                    hostelImageUri
                                )
                            }

                        },
                        colors = ButtonDefaults.buttonColors(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Hostel",color= Color.Black)
                    }
                }
            }
        }
    )
}

private fun addHostelToFirestore(
    navController: NavController,
    onHostelAdded: () -> Unit,
    hostelName: String,
    hostelDescription: String,
    hostelPrice: String,
    hostelImageUri: Uri?
) {
    if (hostelName.isEmpty() || hostelDescription.isEmpty() || hostelPrice.isEmpty() || hostelImageUri == null) {
        // Validate input fields
        Toast.makeText(navController.context, "Please fill in all fields and select an image.", Toast.LENGTH_SHORT).show()
        return
    }

    val hostelId = UUID.randomUUID().toString()

    val firestore = FirebaseFirestore.getInstance()
    val hostelData = hashMapOf(
        "name" to hostelName,
        "description" to hostelDescription,
        "price" to hostelPrice,
        "imageUrl" to ""
    )

    firestore.collection("hostels").document(hostelId)
        .set(hostelData)
        .addOnSuccessListener {
           Log.d(TAG,"DocumentSnapshot added with ID:$hostelId")
            uploadImageToStorage(hostelId, hostelImageUri) { imageUrl ->
                if (imageUrl != null) {
                    firestore.collection("hostels").document(hostelId)
                        .update("imageUrl", imageUrl)
                        .addOnSuccessListener {
                            // Display toast message
                            Toast.makeText(
                                navController.context,
                                "Hostel added successfully!",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to another screen
                            navController.navigate("home")

                            // Invoke the onHostelAdded callback
                            onHostelAdded()
                        }
                        .addOnFailureListener { e ->
                            // Handle error updating hostel document
                            Toast.makeText(navController.context, "Failed to update hostel image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            onHostelAdded()
                        }
                } else {
                    // Handle error uploading image
                    Toast.makeText(navController.context, "Failed to upload hostel image.", Toast.LENGTH_SHORT).show()
                    onHostelAdded()
                }
            }
        }
        .addOnFailureListener { e ->
            // Handle error adding hostel to Firestore
            Toast.makeText(navController.context, "Failed to add hostel: ${e.message}", Toast.LENGTH_SHORT).show()
            onHostelAdded()
        }
}

private fun uploadImageToStorage(hostelId: String, imageUri: Uri, callback: (String?) -> Unit) {
    val storageRef = Firebase.storage.reference.child("hostels/$hostelId.jpg")
    val uploadTask = storageRef.putFile(imageUri)

    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            callback(uri.toString())
        }.addOnFailureListener { e ->
            // Handle error getting download URL
            callback(null)
        }
    }.addOnFailureListener { e ->
        // Handle error uploading image
    callback(null)
    }
}
