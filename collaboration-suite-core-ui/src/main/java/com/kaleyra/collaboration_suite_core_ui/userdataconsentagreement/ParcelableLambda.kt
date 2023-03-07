package com.kaleyra.collaboration_suite_core_ui.userdataconsentagreement

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ParcelableLambda(val block: () -> Unit): Parcelable