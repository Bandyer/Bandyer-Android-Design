package com.bandyer.video_android_glass_ui

interface Participants {
    val me: Participant
    val others: List<Participant>
    val creator: Participant?
}

interface CallParticipants: Participants {
    override val me: CallParticipant
    override val others: List<CallParticipant>
    override val creator: CallParticipant?
}

data class PhoneCallParticipants(
    override val me: CallParticipant,
    override val others: List<CallParticipant>,
    override val creator: CallParticipant?
) : CallParticipants