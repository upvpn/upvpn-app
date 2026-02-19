package app.upvpn.upvpn.review

import android.app.Activity

interface InAppReviewManager {
    fun canRequestReview(connectedDurationMs: Long): Boolean
    fun recordReviewRequested()
    fun launchReviewFlow(activity: Activity)
}
