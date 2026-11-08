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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.upvpn.upvpn.BuildConfig
import app.upvpn.upvpn.model.LOCATION_COLD_COLOR
import app.upvpn.upvpn.model.LOCATION_WARM_COLOR
import app.upvpn.upvpn.model.PREMIUM_INDICATOR

// size of the warm / cold dots, which the premium indicator matches
private val INDICATOR_SIZE = 12.dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navigateUp: () -> Unit,
    // shows a web page (by url, under a title) inside the app
    openWebPage: (url: String, title: String) -> Unit = { _, _ -> },
) {
    val linkStyle = SpanStyle(
        color = MaterialTheme.colorScheme.tertiary,
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight.Medium
    )

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
                HelpSectionTitle("What do indicators mean?")
                IndicatorRow(
                    indicator = { IndicatorDot(LOCATION_WARM_COLOR) },
                    text = "Connect quickly to available servers",
                )
                Spacer(modifier = Modifier.height(8.dp))
                IndicatorRow(
                    indicator = { IndicatorDot(LOCATION_COLD_COLOR) },
                    text = "Create and connect to a new server",
                )
                Spacer(modifier = Modifier.height(8.dp))
                IndicatorRow(
                    indicator = { PremiumIndicator() },
                    text = "Premium location, costs higher",
                )

                Spacer(modifier = Modifier.height(24.dp))

                HelpSectionTitle("Pricing Calculator")
                HelpText(
                    buildAnnotatedString {
                        link("Estimate cost", linkStyle) {
                            openWebPage(
                                "${BuildConfig.UPVPN_BASE_URL}/pricing-calculator/",
                                "Pricing Calculator"
                            )
                        }
                        append(" of standard locations on pay as you go, or premium locations on any plan")
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                HelpSectionTitle("Questions about product or pricing?")
                HelpText(
                    buildAnnotatedString {
                        append("Visit ")
                        link("FAQ", linkStyle) { openWebPage("${BuildConfig.UPVPN_BASE_URL}/faq", "FAQ") }
                        append(" page")

                        append("\n\nOr email us at ")
                        withStyle(style = linkStyle) {
                            withLink(LinkAnnotation.Url(url = "mailto:support@upvpn.app")) {
                                append("support@upvpn.app")
                            }
                        }
                        append(" and we'll be happy to assist!")
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                buildAnnotatedString {
                    append("To delete your account, visit the ")
                    link("account page on the dashboard", linkStyle) {
                        openWebPage("${BuildConfig.UPVPN_BASE_URL}/dashboard/account", "Account")
                    }

                    append("\n\n")
                    link("Acknowledgements", linkStyle) {
                        openWebPage(
                            "${BuildConfig.UPVPN_BASE_URL}/oss/android/latest",
                            "Acknowledgements"
                        )
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HelpSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    HorizontalDivider(modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
}

@Composable
private fun HelpText(text: AnnotatedString) {
    Text(
        text = text,
        textAlign = TextAlign.Left,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun IndicatorRow(indicator: @Composable () -> Unit, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        indicator()
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IndicatorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(INDICATOR_SIZE)
            .clip(CircleShape)
            .background(color)
    )
}

/** The premium indicator, drawn at the size of the warm / cold dots. */
@Composable
private fun PremiumIndicator() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(INDICATOR_SIZE)) {
        Text(
            text = PREMIUM_INDICATOR,
            fontSize = 10.sp,
            lineHeight = 10.sp,
            softWrap = false,
            overflow = TextOverflow.Visible,
            maxLines = 1,
        )
    }
}

/** Appends [text] as a link in [style] that runs [onClick], e.g. to open a page inside the app. */
private fun AnnotatedString.Builder.link(text: String, style: SpanStyle, onClick: () -> Unit) {
    withStyle(style = style) {
        withLink(
            LinkAnnotation.Clickable(
                tag = text,
                linkInteractionListener = LinkInteractionListener { onClick() }
            )
        ) {
            append(text)
        }
    }
}
