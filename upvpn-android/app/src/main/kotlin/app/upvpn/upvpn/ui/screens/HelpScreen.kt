package app.upvpn.upvpn.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.model.LOCATION_COLD_COLOR
import app.upvpn.upvpn.model.LOCATION_WARM_COLOR

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navigateUp: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Help", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "What are color indicators?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(LOCATION_WARM_COLOR)
                    )
                    Text(
                        text = "Connect quickly to available servers",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(LOCATION_COLD_COLOR)
                    )
                    Text(
                        text = "Create and connect to a new server",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Questions about product or pricing?",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    buildAnnotatedString {
                        append("Visit ")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            withLink(LinkAnnotation.Url(url = "${BuildConfig.UPVPN_BASE_URL}/faq")) {
                                append("FAQ")
                            }
                        }

                        append("\n\nOr email us at ")

                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            withLink(LinkAnnotation.Url(url = "mailto:support@upvpn.app")) {
                                append("support@upvpn.app")
                            }
                        }

                        append(" and we'll be happy to assist!")
                    },
                    textAlign = TextAlign.Left,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                buildAnnotatedString {

                    withStyle(style = SpanStyle(fontSize = 12.sp)) {

                        append("To delete your account, visit the ")

                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            withLink(LinkAnnotation.Url(url = "${BuildConfig.UPVPN_BASE_URL}/dashboard/account")) {
                                append("account page on the dashboard")
                            }
                        }

                        append("\n\n")
                        withStyle(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.tertiary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            withLink(LinkAnnotation.Url(url = BuildConfig.UPVPN_BASE_URL + "/oss/android/latest")) {
                                append("Acknowledgements")
                            }
                        }
                    }
                })
        }
    }
}
