package com.alarysai.alarysai.core.firebase

/**
 * Field names and values shared by every content collection.
 * Source of truth: `web_admin/docs/data-model.md`.
 */
object FirestoreContract {
    const val FIELD_STATUS = "status"
    const val FIELD_ORDER = "order"
    const val FIELD_CATEGORY_ID = "categoryId"

    /** Categories, tips and advertisers. */
    const val STATUS_ACTIVE = "active"

    /** Questionnaires. */
    const val STATUS_PUBLISHED = "published"
}
