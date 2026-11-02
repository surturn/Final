package com.sydney.sydney.ui.home

import com.sydney.sydney.ui.components.RemoteImage
import com.sydney.sydney.domain.Validators
import com.sydney.sydney.data.model.Hostel
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sydney.sydney.R
import com.sydney.sydney.container
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.ui.components.ErrorText
import com.sydney.sydney.ui.util.viewModelFactory

class HomeActions(
    val onLogin: () -> Unit,
    val onRegister: () -> Unit,
    val onHostels: () -> Unit,
    val onStudents: () -> Unit,
    val onProfile: () -> Unit,
    val onAddHostel: () -> Unit,
    val onSaved: () -> Unit,
    val onOpenHostel: (String) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(actions: HomeActions) {
    val context = LocalContext.current
    val container = context.container
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(container.auth, container.profiles, container.hostels, container.recent) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val supportPhone = stringResource(R.string.support_phone)

    // An admin can approve a landlord while the app is in the background, so re-read on return.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    val signedIn = state.uid != null
    val profile = state.profile

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.colfind),
                    contentDescription = "ColFind logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (profile != null) "Hi, ${profile.displayName}" else "Find a hostel near your campus",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (!signedIn) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = actions.onLogin, modifier = Modifier.weight(1f)) { Text("Log in") }
                        OutlinedButton(onClick = actions.onRegister, modifier = Modifier.weight(1f)) { Text("Register") }
                    }
                }
            }

            item {
                MenuCard(Icons.Default.Home, "Browse hostels", "See listings and prices", actions.onHostels)
            }

            if (signedIn) {
                if (state.profileIncomplete) {
                    item {
                        MenuCard(
                            Icons.Default.AccountCircle,
                            "Complete your profile",
                            "Add your campus, phone and area so landlords and students can reach you",
                            actions.onProfile,
                        )
                    }
                }
                item {
                    MenuCard(Icons.Default.Favorite, "Saved hostels", "Hostels you have hearted", actions.onSaved)
                }
                if (state.recent.isNotEmpty()) {
                    item { RecentRow(state.recent, actions.onOpenHostel) }
                }
                item {
                    MenuCard(Icons.Default.Person, "Student directory", "Find other students", actions.onStudents)
                }
                item {
                    val subtitle = if (state.loading || profile != null) "Update your details and photo" else "Finish setting up your profile"
                    MenuCard(Icons.Default.AccountCircle, "My profile", subtitle, actions.onProfile)
                }
                if (profile != null) {
                    item { LandlordCard(profile.landlordStatus, state.applying, viewModel::applyForLandlord, actions.onAddHostel) }
                }
                item { ErrorText(state.error) }
                item { TextButton(onClick = viewModel::signOut) { Text("Sign out") } }
            }

            if (state.loading && signedIn) {
                item { CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) }
            }

            item {
                TextButton(onClick = { dial(context, supportPhone) }) { Text("Need help? Call support") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentRow(recent: List<Hostel>, onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Recently viewed", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(recent, key = { it.id }) { hostel ->
                Card(onClick = { onOpen(hostel.id) }, modifier = Modifier.width(160.dp)) {
                    Column {
                        RemoteImage(
                            url = hostel.imageUrl,
                            contentDescription = hostel.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                        )
                        Column(Modifier.padding(8.dp)) {
                            Text(hostel.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                Validators.formatPrice(hostel.priceKes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandlordCard(
    status: LandlordStatus,
    applying: Boolean,
    onApply: () -> Unit,
    onAddHostel: () -> Unit,
) {
    when (status) {
        LandlordStatus.APPROVED ->
            MenuCard(Icons.Default.Add, "Add a hostel", "List a new hostel for students", onAddHostel)
        LandlordStatus.NONE -> Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Own a hostel?", style = MaterialTheme.typography.titleMedium)
                Text("Ask to become a landlord. Once approved you can list hostels.", style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onApply, enabled = !applying) { Text("Apply as a landlord") }
            }
        }
        LandlordStatus.PENDING -> StatusCard("Your landlord request is waiting for approval")
        LandlordStatus.REJECTED -> StatusCard("Your landlord request was not approved. Contact support if you think this is a mistake")
    }
}

@Composable
private fun StatusCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Opens the dialer with the number filled in. No call permission is needed. */
internal fun dial(context: Context, phone: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
    } catch (e: ActivityNotFoundException) {
        // Tablets without a phone app have nothing to open. Nothing else to do.
    }
}
