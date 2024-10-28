package app.upvpn.upvpn.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            Text(
                buildAnnotatedString {
                    append("Have questions about ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("Product or Pricing?")
                    }

                    append("\n\nVisit ")
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
                style = MaterialTheme.typography.bodyLarge
            )

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
