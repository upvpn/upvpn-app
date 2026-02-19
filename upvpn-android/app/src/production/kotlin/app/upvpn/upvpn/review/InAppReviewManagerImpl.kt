package app.upvpn.upvpn.review

import android.app.Activity
import android.content.Context
import android.util.Log
import app.upvpn.upvpn.BuildConfig
import com.google.android.play.core.review.ReviewManagerFactory


class InAppReviewManagerImpl(context: Context) : InAppReviewManager {

    private val tag = "InAppReviewManager"
    private val prefs = context.getSharedPreferences("review_prefs", Context.MODE_PRIVATE)
    private val reviewManager = ReviewManagerFactory.create(context)

    companion object {
        private const val KEY_LAST_REVIEW_TIME = "last_review_requested_at"
        const val MIN_CONNECTED_DURATION_MS = 60_000L // 1 minute
        private const val REVIEW_COOLDOWN_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
    }

    override fun canRequestReview(connectedDurationMs: Long): Boolean {
        if (connectedDurationMs < MIN_CONNECTED_DURATION_MS) return false

        val lastReviewTime = prefs.getLong(KEY_LAST_REVIEW_TIME, 0L)
        if (lastReviewTime == 0L) return true

        // Debug builds: no cooldown so review can be tested easily
        if (BuildConfig.DEBUG) return true

        return (System.currentTimeMillis() - lastReviewTime) >= REVIEW_COOLDOWN_MS
    }

    override fun recordReviewRequested() {
        prefs.edit().putLong(KEY_LAST_REVIEW_TIME, System.currentTimeMillis()).apply()
    }

    override fun launchReviewFlow(activity: Activity) {
        reviewManager.requestReviewFlow()
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(activity, reviewInfo)
            }
            .addOnFailureListener { e ->
                Log.w(tag, "Review flow failed: ${e.message}")
            }
    }
}
