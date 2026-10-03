package com.alarysai.alarysai.feature.advertisers.domain.usecase

import com.alarysai.alarysai.core.common.text.normalizedForSearch
import com.alarysai.alarysai.feature.advertisers.domain.model.Advertiser
import com.alarysai.alarysai.feature.advertisers.domain.model.AdvertiserGroup
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

/**
 * Groups advertisers by their free-text `type`, like the panel's `advertiserTypes`
 * (`src/features/advertisers/domain`): "Patrocínio", "patrocinio " and "PATROCINIO" are one type,
 * shown with the first spelling found. Groups are sorted alphabetically (Portuguese collation);
 * advertisers without a type come last. Inside a group the incoming order is kept.
 */
class GroupAdvertisersByTypeUseCase @Inject constructor() {

    operator fun invoke(advertisers: List<Advertiser>): List<AdvertiserGroup> {
        val byKey = linkedMapOf<String, MutableList<Advertiser>>()
        advertisers.forEach { advertiser ->
            byKey.getOrPut(advertiser.type.normalizedForSearch()) { mutableListOf() } += advertiser
        }
        val untyped = byKey.remove("")
        val collator = Collator.getInstance(Locale("pt", "BR")).apply { strength = Collator.PRIMARY }
        val typed = byKey.values
            .map { members -> AdvertiserGroup(type = members.first().type, advertisers = members) }
            .sortedWith { first, second -> collator.compare(first.type, second.type) }
        return typed + listOfNotNull(untyped?.let { AdvertiserGroup(type = null, advertisers = it) })
    }
}
