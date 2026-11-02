package com.sydney.sydney.ui.hostels

import com.sydney.sydney.ui.components.entrance
import com.sydney.sydney.ui.components.HostelListSkeleton
import com.sydney.sydney.ui.components.HeartToggle
import com.sydney.sydney.ui.components.PressableCard
import com.sydney.sydney.ui.components.SecondaryButton
import com.sydney.sydney.ui.components.PrimaryButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sydney.sydney.container
import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.domain.Validators
import com.sydney.sydney.ui.components.ColFindTopBar
import com.sydney.sydney.ui.components.ErrorText
import com.sydney.sydney.ui.components.FormField
import com.sydney.sydney.ui.components.GuestBanner
import com.sydney.sydney.ui.components.SignInPromptDialog
import com.sydney.sydney.ui.components.LoadingBox
import com.sydney.sydney.ui.components.MessageBox
import com.sydney.sydney.ui.components.RemoteImage
import com.sydney.sydney.ui.home.dial
import com.sydney.sydney.ui.util.viewModelFactory
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun HostelListScreen(
    onBack: () -> Unit,
    onOpen: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    val container = LocalContext.current.container
    val viewModel: HostelListViewModel = viewModel(
        factory = viewModelFactory { HostelListViewModel(container.hostels, container.auth, container.saved) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showSignInPrompt) {
        SignInPromptDialog(
            message = "Create a free account to save hostels and find them again later.",
            onLogin = onLogin,
            onRegister = onRegister,
            onDismiss = viewModel::dismissPrompt,
        )
    }

    Scaffold(topBar = { ColFindTopBar("Hostels", onBack) }) { padding ->
        Column(Modifier.padding(padding)) {
            if (!state.signedIn) {
                GuestBanner(
                    "Log in to save hostels and see more",
                    onLogin,
                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQuery,
                placeholder = { Text("Search by name or area") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                state.loading -> HostelListSkeleton()
                state.error != null -> MessageBox(state.error.orEmpty(), actionLabel = "Try again", onAction = viewModel::retry)
                state.hostels.isEmpty() -> MessageBox(
                    if (state.total == 0) "No hostels have been listed yet" else "No hostels match your search",
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(state.hostels, key = { _, it -> it.id }) { index, hostel ->
                        HostelCard(
                            hostel,
                            saved = hostel.id in state.savedIds,
                            onToggleSave = { viewModel.onSaveClick(hostel.id) },
                            modifier = Modifier.entrance(hostel.id, index),
                        ) { onOpen(hostel.id) }
                    }
                }
            }
            ErrorText(state.saveError)
        }
    }
}

/** The hostels the signed-in user saved. Needs an account, so the nav host guards the route. */
@Composable
fun SavedHostelsScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val container = LocalContext.current.container
    val viewModel: SavedViewModel = viewModel(
        factory = viewModelFactory { SavedViewModel(container.hostels, container.auth, container.saved) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { ColFindTopBar("Saved hostels", onBack) }) { padding ->
        when {
            state.loading -> HostelListSkeleton(Modifier.padding(padding))
            state.error != null -> MessageBox(state.error.orEmpty(), Modifier.padding(padding), "Try again", viewModel::retry)
            state.hostels.isEmpty() -> MessageBox("Tap the heart on a hostel to save it here", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                itemsIndexed(state.hostels, key = { _, it -> it.id }) { index, hostel ->
                    HostelCard(
                        hostel,
                        saved = true,
                        onToggleSave = { viewModel.unsave(hostel.id) },
                        modifier = Modifier.entrance(hostel.id, index),
                    ) { onOpen(hostel.id) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HostelCard(
    hostel: Hostel,
    saved: Boolean,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    PressableCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RemoteImage(
                url = hostel.imageUrl,
                contentDescription = hostel.name,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(hostel.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (hostel.location.isNotEmpty()) {
                    Text(hostel.location, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    Validators.formatPrice(hostel.priceKes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            HeartToggle(
                saved = saved,
                onToggle = onToggleSave,
                contentDescription = if (saved) "Remove ${hostel.name} from saved" else "Save ${hostel.name}",
            )
        }
    }
}

@Composable
fun HostelDetailScreen(hostelId: String, onBack: () -> Unit, onLogin: () -> Unit, onRegister: () -> Unit) {
    val context = LocalContext.current
    val container = context.container
    val viewModel: HostelDetailViewModel = viewModel(
        key = hostelId,
        factory = viewModelFactory {
            HostelDetailViewModel(container.hostels, hostelId, container.auth, container.saved, container.recent)
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hostel = state.hostel

    if (state.showSignInPrompt) {
        SignInPromptDialog(
            message = "Create a free account to save hostels and find them again later.",
            onLogin = onLogin,
            onRegister = onRegister,
            onDismiss = viewModel::dismissPrompt,
        )
    }

    Scaffold(
        topBar = {
            ColFindTopBar(hostel?.name ?: "Hostel", onBack) {
                if (hostel != null) {
                    HeartToggle(
                        saved = state.isSaved,
                        onToggle = viewModel::onSaveClick,
                        contentDescription = if (state.isSaved) "Remove from saved" else "Save this hostel",
                    )
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox(Modifier.padding(padding))
            hostel == null -> MessageBox(
                state.error.orEmpty(),
                modifier = Modifier.padding(padding),
                actionLabel = "Try again",
                onAction = viewModel::load,
            )
            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            ) {
                RemoteImage(
                    url = hostel.imageUrl,
                    contentDescription = hostel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                )
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(hostel.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        Validators.formatPrice(hostel.priceKes),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    if (hostel.location.isNotEmpty()) Text(hostel.location, style = MaterialTheme.typography.bodyMedium)
                    Text(hostel.description, style = MaterialTheme.typography.bodyLarge)
                    if (hostel.contactPhone.isNotEmpty()) {
                        PrimaryButton(onClick = { dial(context, hostel.contactPhone) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Call the landlord")
                        }
                    }
                    ErrorText(state.saveError)
                }
            }
        }
    }
}

@Composable
fun AddHostelScreen(onBack: () -> Unit, onDone: () -> Unit) {
    val container = LocalContext.current.container
    val viewModel: AddHostelViewModel = viewModel(
        factory = viewModelFactory { AddHostelViewModel(container.auth, container.profiles, container.hostels) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onImage(uri.toString())
    }

    LaunchedEffect(state.done) { if (state.done) onDone() }

    Scaffold(topBar = { ColFindTopBar("Add a hostel", onBack) }) { padding ->
        when {
            state.checking -> LoadingBox(Modifier.padding(padding))
            state.status != LandlordStatus.APPROVED -> MessageBox(
                state.error ?: when (state.status) {
                    LandlordStatus.PENDING -> "Your landlord request is still waiting for approval"
                    LandlordStatus.REJECTED -> "Your landlord request was not approved"
                    else -> "Only approved landlords can add hostels. Apply from the home screen"
                },
                modifier = Modifier.padding(padding),
                actionLabel = "Check again",
                onAction = viewModel::checkStatus,
            )
            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RemoteImage(
                    url = state.imageUri.orEmpty(),
                    contentDescription = "Selected hostel photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
                SecondaryButton(
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    enabled = !state.submitting,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (state.imageUri == null) "Choose a photo" else "Change photo") }
                ErrorText(state.imageError)
                FormField(state.name, viewModel::onName, "Hostel name", error = state.nameError, enabled = !state.submitting)
                FormField(
                    state.price,
                    viewModel::onPrice,
                    "Monthly price (KES)",
                    error = state.priceError,
                    keyboardType = KeyboardType.Number,
                    enabled = !state.submitting,
                )
                FormField(state.location, viewModel::onLocation, "Area or landmark", enabled = !state.submitting)
                FormField(
                    state.phone,
                    viewModel::onPhone,
                    "Contact phone",
                    error = state.phoneError,
                    supportingText = "Students will see this number",
                    keyboardType = KeyboardType.Phone,
                    enabled = !state.submitting,
                )
                FormField(
                    state.description,
                    viewModel::onDescription,
                    "Description",
                    error = state.descriptionError,
                    minLines = 4,
                    imeAction = ImeAction.Default,
                    enabled = !state.submitting,
                )
                ErrorText(state.error)
                PrimaryButton(onClick = viewModel::submit, enabled = !state.submitting, modifier = Modifier.fillMaxWidth()) {
                    if (state.submitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Publish hostel")
                }
            }
        }
    }
}
