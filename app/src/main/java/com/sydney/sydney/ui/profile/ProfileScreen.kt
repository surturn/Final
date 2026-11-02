package com.sydney.sydney.ui.profile

import com.sydney.sydney.ui.components.QuietButton
import com.sydney.sydney.ui.components.SecondaryButton
import com.sydney.sydney.ui.components.PrimaryButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sydney.sydney.container
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.ui.components.ColFindTopBar
import com.sydney.sydney.ui.components.ErrorText
import com.sydney.sydney.ui.components.FormField
import com.sydney.sydney.ui.components.LoadingBox
import com.sydney.sydney.ui.components.RemoteImage
import com.sydney.sydney.ui.util.viewModelFactory

@Composable
fun ProfileScreen(onBack: () -> Unit, onDeleted: () -> Unit) {
    val container = LocalContext.current.container
    val viewModel: ProfileViewModel = viewModel(
        factory = viewModelFactory { ProfileViewModel(container.auth, container.profiles) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhoto(uri.toString())
    }

    var confirmDelete by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = { ColFindTopBar("My profile", onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RemoteImage(
                url = state.newPhotoUri ?: state.photoUrl,
                contentDescription = "Profile photo",
                placeholderIcon = Icons.Default.Person,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
            )
            SecondaryButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = !state.saving,
            ) { Text(if (state.photoUrl.isEmpty() && state.newPhotoUri == null) "Add a photo" else "Change photo") }

            if (!state.exists) {
                Text(
                    "Your profile is not saved yet. Fill it in and save to appear in the student directory.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(state.email, style = MaterialTheme.typography.bodyMedium)

            FormField(state.name, viewModel::onName, "Full name", error = state.nameError, enabled = !state.saving)
            FormField(state.campus, viewModel::onCampus, "Campus", error = state.campusError, enabled = !state.saving)
            FormField(state.location, viewModel::onLocation, "Area where you live", error = state.locationError, enabled = !state.saving)
            FormField(
                state.phone,
                viewModel::onPhone,
                "Phone",
                error = state.phoneError,
                supportingText = "Private. Other students never see your phone number",
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                enabled = !state.saving,
            )
            ErrorText(state.error)
            PrimaryButton(onClick = viewModel::save, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) {
                if (state.saving) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Save")
            }

            if (state.exists) {
                when (state.status) {
                    LandlordStatus.NONE -> SecondaryButton(
                        onClick = viewModel::applyForLandlord,
                        enabled = !state.applying,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Apply as a landlord") }
                    LandlordStatus.PENDING -> Text("Landlord request: waiting for approval", style = MaterialTheme.typography.bodyMedium)
                    LandlordStatus.APPROVED -> Text("Landlord: approved", style = MaterialTheme.typography.bodyMedium)
                    LandlordStatus.REJECTED -> Text("Landlord request: not approved", style = MaterialTheme.typography.bodyMedium)
                }
            }

            QuietButton(onClick = { confirmDelete = true }, enabled = !state.saving && !state.deleting) {
                Text("Delete my account", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { if (!state.deleting) confirmDelete = false },
            title = { Text("Delete your account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This permanently removes your profile, photo and any hostels you listed. It cannot be undone.")
                    FormField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = "Password",
                        password = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        enabled = !state.deleting,
                    )
                    ErrorText(state.error)
                }
            },
            confirmButton = {
                QuietButton(onClick = { viewModel.deleteAccount(deletePassword) }, enabled = !state.deleting) {
                    if (state.deleting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                QuietButton(onClick = { confirmDelete = false }, enabled = !state.deleting) { Text("Cancel") }
            },
        )
    }
}
