@file:Suppress("NAME_SHADOWING", "DEPRECATION")

package com.sydney.sydney.ui.theme.students

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.sydney.sydney.R
import com.sydney.sydney.navigation.ROUTE_HOME
import java.util.UUID


var progressDialog: ProgressDialog? = null
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudents(navController: NavHostController) {

    val context = LocalContext.current
    BackHandler {
        navController.navigate(ROUTE_HOME)
    }

    Scaffold(

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Student Registration")
                },
                navigationIcon = {
                    IconButton(onClick = {


                        navController.navigate(ROUTE_HOME)

                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon", tint = Color.White)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,

                    )
            )
        }, content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                verticalArrangement = Arrangement.Center,
            ) {
                LazyColumn {
                    item {
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Image(painter = painterResource(id = R.drawable.colfind) , contentDescription = "", contentScale = ContentScale.Crop
                                ,modifier= Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(10.dp)))
                            Spacer(modifier = Modifier.height(10.dp))


                            var photoUri: Uri? by remember { mutableStateOf(null) }
                            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                                photoUri = uri
                            }

                            var studentName by rememberSaveable {
                                mutableStateOf("")
                            }

                            var studentCampus by rememberSaveable {
                                mutableStateOf("")
                            }

                            var studentEmail by rememberSaveable {
                                mutableStateOf("")
                            }

                            var location by rememberSaveable {
                                mutableStateOf("")
                            }

                            var phone by rememberSaveable {
                                mutableStateOf("")
                            }



                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { studentName = it },
                                label = { Text(text = "Name") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    focusedTextColor = Color.Cyan)

                            )

                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(text = "Phone") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    focusedTextColor = Color.Cyan),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text(text = "Location") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    focusedTextColor = Color.Cyan),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = studentEmail,
                                onValueChange = { studentEmail = it },
                                label = { Text(text = "Email") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    focusedTextColor = Color.Cyan),
                                shape = RoundedCornerShape(10.dp)

                            )

                            OutlinedTextField(
                                value = studentCampus,
                                onValueChange = { studentCampus= it },
                                label = { Text(text = "Campus") },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedLabelColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    focusedTextColor = Color.Cyan),
                                shape = RoundedCornerShape(10.dp)
                            )





                            if (photoUri != null) {
                                //Use Coil to display the selected image
                                val painter = rememberAsyncImagePainter(
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(data = photoUri)
                                        .build()
                                )

                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .width(150.dp)
                                        .height(150.dp)
                                        .border(1.dp, Color.Gray),
                                    contentScale = ContentScale.Crop,

                                    )
                            } else {

                                OutlinedButton(
                                    onClick = {
                                        launcher.launch(
                                            PickVisualMediaRequest(
                                                //Here we request only photos. Change this to .ImageAndVideo if you want videos too.
                                                //Or use .VideoOnly if you only want videos.
                                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                                ) {
                                    Text(text = "select image")
                                }
                            }
                            OutlinedButton(onClick = {

                                if (photoUri != null) {

                                    progressDialog = ProgressDialog(context)
                                    progressDialog?.setMessage("Uploading data...")
                                    progressDialog?.setCancelable(false)
                                    progressDialog?.show()

                                    photoUri?.let {

                                        uploadImageToFirebaseStorage(
                                            it,
                                            studentName,
                                            studentCampus,
                                            studentEmail,
                                            location,
                                            phone,
                                            context

                                        )

                                        studentName = ""
                                        studentCampus = ""
                                        studentEmail = ""
                                        location = ""
                                        phone = ""
                                        photoUri =null

                                    }
                                } else if (studentCampus == ""){

                                    Toast.makeText(context, "Please enter class", Toast.LENGTH_SHORT).show()
                                }
                                else if (studentEmail == ""){
                                    Toast.makeText(context, "Please enter email", Toast.LENGTH_SHORT).show()
                                }
                                else if(studentName == ""){
                                    Toast.makeText(context, "Please enter name", Toast.LENGTH_SHORT).show()
                                }

                                else {
                                    Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                                }



                            }) {

                                Text(
                                    modifier = Modifier,


                                    text = "Save Student",
                                    textAlign = TextAlign.Center,
                                    color =MaterialTheme.colorScheme.onSurface
                                )



                            }

                        }
                    }
                }
            }

        })



}



fun uploadImageToFirebaseStorage(
    imageUri: Uri,
    studentName: String,
    studentCampus: String,
    studentEmail: String,
    location: String,
    phone: String,
    context: Context

) {
    val storageRef = FirebaseStorage.getInstance().reference
    val imageRef = storageRef.child("images/${UUID.randomUUID()}")

    val uploadTask = imageRef.putFile(imageUri)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let {
                throw it
            }
        }
        imageRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            saveToFirestore(
                downloadUri.toString(),
                studentName,
                studentCampus,
                studentEmail,
                location,
                phone,
                context,


                )

        } else {

            progressDialog?.dismiss()

            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("Failed to upload image: ${task.exception?.message}")
                .setPositiveButton("OK") { _, _ ->
                    // Optional: Add actions when OK is clicked


                }
                .show()


        }
    }
}

fun saveToFirestore(
    imageUrl: String,
    studentName: String,
    studentCampus: String,
    studentEmail: String,
    location: String,
    phone: String,
    context: Context
) {
    val db = Firebase.firestore
    val uniqueKey = UUID.randomUUID().toString()  // Generate a unique key

    val studentInfo = hashMapOf(
        "uniqueKey" to uniqueKey,
        "imageUrl" to imageUrl,
        "studentName" to studentName,
        "studentCampus" to studentCampus,
        "studentEmail" to studentEmail,
        "location" to location,
        "phone" to phone
    )

    db.collection("Students")
        .document(uniqueKey)  // Use the unique key as the document ID
        .set(studentInfo)
        .addOnSuccessListener {
            progressDialog?.dismiss()

            val dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setTitle("Success")
                .setMessage("Data saved successfully!")
                .setPositiveButton("OK") { _, _ ->
                    // Optional: Add actions when OK is clicked
                }
                .setIcon(R.drawable.colfind)
                .setCancelable(false)

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
        .addOnFailureListener {
            progressDialog?.dismiss()
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage("Failed to save data")
                .setPositiveButton("OK") { _, _ -> }
                .show()
        }
}

@Preview(showBackground = true)
@Composable
fun PreviewLight() {
    AddStudents(rememberNavController())
}






