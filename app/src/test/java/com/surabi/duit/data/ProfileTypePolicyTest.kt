package com.surabi.duit.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileTypePolicyTest {
    @Test fun mapsLegacyWorkspaceTypesToCanonicalProfiles() {
        assertEquals(ProfileTypePolicy.PERSONAL, ProfileTypePolicy.fromWorkspaceType("PERSONAL"))
        assertEquals(ProfileTypePolicy.FAMILY, ProfileTypePolicy.fromWorkspaceType("FAMILY"))
        assertEquals(ProfileTypePolicy.BUSINESS, ProfileTypePolicy.fromWorkspaceType("COMPANY"))
        assertEquals(ProfileTypePolicy.PROJECT, ProfileTypePolicy.fromWorkspaceType("PROJECT"))
        assertEquals(ProfileTypePolicy.CUSTOM, ProfileTypePolicy.fromWorkspaceType("OTHER"))
    }

    @Test fun normalizesSupportedProfileTypes() {
        assertEquals(ProfileTypePolicy.BUSINESS, ProfileTypePolicy.normalize(" business "))
    }

    @Test fun rejectsUnsupportedProfileTypes() {
        assertThrows(IllegalArgumentException::class.java) {
            ProfileTypePolicy.normalize("UNKNOWN")
        }
    }
}
