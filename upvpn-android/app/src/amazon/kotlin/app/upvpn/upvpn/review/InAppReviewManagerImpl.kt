package app.upvpn.upvpn.review

import android.app.Activity
import android.content.Context

class InAppReviewManagerImpl(@Suppress("UNUSED_PARAMETER") context: Context) : InAppReviewManager {
    override fun canRequestReview(connectedDurationMs: Long) = false
    override fun recordReviewRequested() {}
    override fun launchReviewFlow(activity: Activity) {}
}
