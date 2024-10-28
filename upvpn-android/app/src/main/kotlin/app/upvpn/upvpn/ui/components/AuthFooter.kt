package app.upvpn.upvpn.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import app.upvpn.upvpn.BuildConfig

@Composable
fun AuthFooter() {
    Text(
        buildAnnotatedString {
            append("By using ")

            if (BuildConfig.IS_AMAZON) {
                append("UpVPN.app")
            } else {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.tertiary,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    withLink(LinkAnnotation.Url(url = BuildConfig.UPVPN_BASE_URL)) {
                        append("UpVPN.app")
                    }
                }
            }

            append(" you agree to our ")

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                withLink(LinkAnnotation.Url(url = "${BuildConfig.UPVPN_BASE_URL}/terms-of-service")) {
                    append("Terms")
                }
            }

            append(" and ")

            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                withLink(LinkAnnotation.Url(url = "${BuildConfig.UPVPN_BASE_URL}/privacy-policy")) {
                    append("Privacy Policy")
                }
            }

        },
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
    )
}
