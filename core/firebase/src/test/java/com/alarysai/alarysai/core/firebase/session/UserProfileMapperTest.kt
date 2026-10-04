package com.alarysai.alarysai.core.firebase.session

import com.alarysai.alarysai.core.common.language.Language
import com.alarysai.alarysai.core.common.session.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileMapperTest {

    @Test
    fun `maps the profile fields`() {
        assertEquals(
            UserProfile("Marina Alves", "https://x/foto.jpg", Language.EN),
            UserProfileDto(displayName = " Marina Alves ", photoUrl = "https://x/foto.jpg", language = "en").toDomain(),
        )
    }

    @Test
    fun `blank name, unsafe photo and unknown language become null`() {
        assertEquals(
            UserProfile(null, null, null),
            UserProfileDto(displayName = " ", photoUrl = "http://x/foto.jpg", language = "fr").toDomain(),
        )
    }
}
