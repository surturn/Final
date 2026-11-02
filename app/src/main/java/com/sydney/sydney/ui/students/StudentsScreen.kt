package com.sydney.sydney.ui.students

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sydney.sydney.container
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.ui.components.ColFindTopBar
import com.sydney.sydney.ui.components.LoadingBox
import com.sydney.sydney.ui.components.MessageBox
import com.sydney.sydney.ui.components.RemoteImage
import com.sydney.sydney.ui.util.viewModelFactory

@Composable
fun StudentsScreen(onBack: () -> Unit) {
    val container = LocalContext.current.container
    val viewModel: StudentsViewModel = viewModel(factory = viewModelFactory { StudentsViewModel(container.students) })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { ColFindTopBar("Students", onBack) }) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQuery,
                placeholder = { Text("Search by name, campus or area") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                state.loading -> LoadingBox()
                state.error != null -> MessageBox(state.error.orEmpty(), actionLabel = "Try again", onAction = viewModel::retry)
                state.students.isEmpty() -> MessageBox(
                    if (state.query.isBlank()) "No students have joined yet" else "No students match your search",
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.students, key = { it.uid }) { StudentCard(it) }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(student: StudentEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            RemoteImage(
                url = student.photoUrl,
                contentDescription = student.displayName,
                placeholderIcon = Icons.Default.Person,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            )
            Column(Modifier.padding(12.dp)) {
                Text(student.displayName, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (student.campus.isNotEmpty()) {
                    Text(student.campus, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (student.location.isNotEmpty()) {
                    Text(student.location, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
