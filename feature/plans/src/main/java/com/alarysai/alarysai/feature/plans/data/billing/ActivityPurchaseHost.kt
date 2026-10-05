package com.alarysai.alarysai.feature.plans.data.billing

import android.app.Activity
import com.alarysai.alarysai.feature.plans.domain.repository.PurchaseHost

/** The Play Store purchase sheet opens on top of this Activity. */
class ActivityPurchaseHost(val activity: Activity) : PurchaseHost
