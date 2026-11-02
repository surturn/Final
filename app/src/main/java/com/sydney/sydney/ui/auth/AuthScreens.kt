package com.sydney.sydney.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sydney.sydney.R
import com.sydney.sydney.container
import com.sydney.sydney.ui.components.ColFindTopBar
import com.sydney.sydney.ui.components.ErrorText
import com.sydney.sydney.ui.components.FormField
import com.sydney.sydney.ui.util.viewModelFactory

@Composable
fun LoginScreen(onBack: () -> Unit, onRegister: () -> Unit, onSuccess: () -> Unit) {
    val container = LocalContext.current.container
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactory { LoginViewModel(container.auth) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) { if (state.done) onSuccess() }

    Scaffold(topBar = { ColFindTopBar("Log in", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Logo()
            FormField(
                value = state.email,
                onValueChange = viewModel::onEmail,
                label = "Email",
                error = state.emailError,
                keyboardType = KeyboardType.Email,
                enabled = !state.loading,
            )
            FormField(
                value = state.password,
                onValueChange = viewModel::onPassword,
                label = "Password",
                error = state.passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                password = true,
                enabled = !state.loading,
            )
            ErrorText(state.error)
            SubmitButton(text = "Log in", loading = state.loading, onClick = viewModel::submit)
            TextButton(onClick = onRegister, enabled = !state.loading) { Text("New here? Create an account") }
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, onLogin: () -> Unit, onSuccess: () -> Unit) {
    val container = LocalContext.current.container
    val viewModel: RegisterViewModel = viewModel(
        factory = viewModelFactory { RegisterViewModel(container.auth, container.profiles) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.done) { if (state.done) onSuccess() }

    Scaffold(topBar = { ColFindTopBar("Create account", onBack) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Logo()
            FormField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = "Full name",
                error = state.nameError,
                enabled = !state.loading,
            )
            FormField(
                value = state.email,
                onValueChange = viewModel::onEmail,
                label = "Email",
                error = state.emailError,
                keyboardType = KeyboardType.Email,
                enabled = !state.loading,
            )
            FormField(
                value = state.password,
                onValueChange = viewModel::onPassword,
                label = "Password",
                error = state.passwordError,
                supportingText = "At least 8 characters with a number",
                keyboardType = KeyboardType.Password,
                password = true,
                enabled = !state.loading,
            )
            FormField(
                value = state.confirmation,
                onValueChange = viewModel::onConfirmation,
                label = "Confirm password",
                error = state.confirmationError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                password = true,
                enabled = !state.loading,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.isLandlord,
                    onCheckedChange = viewModel::onLandlord,
                    enabled = !state.loading,
                )
                Text(
                    "I own a hostel and want to list it (needs approval)",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            ErrorText(state.error)
            SubmitButton(text = "Create account", loading = state.loading, onClick = viewModel::submit)
            TextButton(onClick = onLogin, enabled = !state.loading) { Text("Already have an account? Log in") }
        }
    }
}

@Composable
private fun Logo() {
    Image(
        painter = painterResource(id = R.drawable.colfind),
        contentDescription = "ColFind logo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(16.dp)),
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SubmitButton(text: String, loading: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(text)
        }
    }
}
