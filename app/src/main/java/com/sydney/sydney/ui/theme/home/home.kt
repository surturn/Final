package com.sydney.sydney.ui.theme.home

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sydney.sydney.R
import com.sydney.sydney.navigation.ROUTE_ADD_HOSTELS
import com.sydney.sydney.navigation.ROUTE_DASHBOARD
import com.sydney.sydney.navigation.ROUTE_LOGIN
import com.sydney.sydney.navigation.ROUTE_PROFILE
import com.sydney.sydney.navigation.ROUTE_REGISTER
import com.sydney.sydney.navigation.ROUTE_SEARCH

data class Screen(val title: String, val icon: Int)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {

    var isDrawerOpen by remember { mutableStateOf(false) }

    val callLauncher: ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { _ -> }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Home", textAlign = TextAlign.Start, color = Color.Yellow)
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_LOGIN)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable {
                        if (isDrawerOpen) {
                            isDrawerOpen = false
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.colfind),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "ColFind",
                                fontSize = 30.sp,
                                color = Color.Yellow,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { navController.navigate(ROUTE_LOGIN) },
                                    colors = ButtonDefaults.buttonColors( Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "Login", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = { navController.navigate(ROUTE_REGISTER) },
                                    colors = ButtonDefaults.buttonColors( Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "Register", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Having a problem?",
                                    fontSize = 15.sp,
                                    color = Color.Yellow,
                                    fontFamily = FontFamily.Cursive
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Call me",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    color = Color.Yellow,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:0794817115")
                                        callLauncher.launch(intent)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Add Hostels",
                                fontSize = 20.sp,
                                color = Color.Yellow,
                                modifier = Modifier.clickable {
                                    navController.navigate(ROUTE_ADD_HOSTELS)
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = { BottomBar(navController = navController) }
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val selectedIndex = remember { mutableStateOf(0) }
    BottomNavigation(
        elevation = 10.dp,
        backgroundColor = Color.Black
    ) {
        BottomNavigationItem(
            icon = {
                Icon(imageVector = Icons.Default.Home, contentDescription = "", tint = Color.White)
            },
            label = { Text(text = "Home", color = Color.White) },
            selected = (selectedIndex.value == 0),
            onClick = { selectedIndex.value = 0 }
        )

        BottomNavigationItem(
            icon = {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "", tint = Color.White)
            },
            label = { Text(text = "Favorite", color = Color.White) },
            selected = (selectedIndex.value == 1),
            onClick = { selectedIndex.value = 1 }
        )

        BottomNavigationItem(
            icon = {
                Icon(imageVector = Icons.Default.Person, contentDescription = "", tint = Color.White)
            },
            label = { Text(text = "Students", color = Color.White) },
            selected = (selectedIndex.value == 2),
            onClick = {
                selectedIndex.value = 2
                navController.navigate(ROUTE_SEARCH)
            }
        )
    }
}
