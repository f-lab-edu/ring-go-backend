package com.ringgo.domain.activity.entity

import com.ringgo.domain.meeting.entity.Meeting
import com.ringgo.domain.user.entity.User
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("EXPENSE")
class ExpenseActivity(
    meeting: Meeting,
    creator: User
) : Activity(
    meeting = meeting,
    creator = creator,
    type = "EXPENSE",
) {
    companion object {
        fun create(meeting: Meeting, creator: User): ExpenseActivity {
            return ExpenseActivity(
                meeting = meeting,
                creator = creator
            )
        }
    }
}