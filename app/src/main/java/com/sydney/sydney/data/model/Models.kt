package com.sydney.sydney.data.model

/** Where a user stands in the landlord approval flow. Only an admin can set APPROVED or REJECTED. */
enum class LandlordStatus(val wire: String) {
    NONE("none"),
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromWire(value: String?): LandlordStatus =
            values().firstOrNull { it.wire == value } ?: NONE
    }
}

/** Private account record (users/{uid}). Only its owner can read it. */
data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val campus: String = "",
    val phone: String = "",
    val location: String = "",
    val landlordStatus: LandlordStatus = LandlordStatus.NONE,
    val createdAt: Long = 0L,
)

/** Public directory entry (students/{uid}). Never carries a phone number or email. */
data class StudentEntry(
    val uid: String,
    val displayName: String,
    val campus: String = "",
    val location: String = "",
    val photoUrl: String = "",
)

/** A hostel listing (hostels/{id}). Price is in Kenyan shillings per month. */
data class Hostel(
    val id: String,
    val name: String,
    val description: String,
    val priceKes: Long,
    val location: String = "",
    val imageUrl: String = "",
    val ownerId: String = "",
    val createdAt: Long = 0L,
)

/** What a landlord submits to publish a hostel. [imageUri] is a content or file URI as text. */
data class NewHostel(
    val name: String,
    val description: String,
    val priceKes: Long,
    val location: String,
    val imageUri: String,
)
