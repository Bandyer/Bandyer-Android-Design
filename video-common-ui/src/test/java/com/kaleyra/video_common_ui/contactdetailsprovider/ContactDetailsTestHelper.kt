package com.kaleyra.video_common_ui.contactdetailsprovider

import com.kaleyra.video_common_ui.contactdetails.model.ContactDetails
import org.junit.Assert.assertEquals

internal object ContactDetailsTestHelper {
    fun assertEqualsContactDetails(expected: List<ContactDetails>, actual: Set<ContactDetails>) {
        actual.forEachIndexed { index, contactDetails ->
            assertEquals(contactDetails.userId, expected[index].userId)
            assertEquals(contactDetails.name.value, expected[index].name.value)
            assertEquals(contactDetails.image.value, expected[index].image.value)
        }
    }
}