package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit,
    clearSearchQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
        TextField(
            value = searchText,
            onValueChange = onSearchValueChange,
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.padding(12.dp, 0.dp, 0.dp, 0.dp)
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = clearSearchQuery,
                        modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(30.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (searchText.isEmpty()) {
            Text(
                text = "Search Locations",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.8f)
            )
        }
    }
}
