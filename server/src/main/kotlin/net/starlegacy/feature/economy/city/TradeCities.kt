package net.starlegacy.feature.economy.city

import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.SettlementCache
import net.starlegacy.command.economy.BazaarCommand.fail
import net.starlegacy.database.Oid
import net.starlegacy.database.enumValue
import net.starlegacy.database.get
import net.starlegacy.database.nullable
import net.starlegacy.database.oid
import net.starlegacy.database.schema.nations.NPCTerritoryOwner
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.nations.NATIONS_BALANCE
import net.starlegacy.feature.nations.NationsBalancing
import net.starlegacy.feature.nations.region.types.RegionTerritory
import java.util.concurrent.ConcurrentHashMap

object TradeCities : SLComponent() {
	private val cities: MutableMap<Oid<Territory>, TradeCityData> = ConcurrentHashMap()

	override fun onEnable() {
		val settlements = SettlementCache.all()
			.filter { it.cityState == Settlement.CityState.ACTIVE }
			.associate {
				it.territory to TradeCityData(
					it.id,
					it.tradeCityType ?: TradeCityType.SETTLEMENT,
					it.territory,
					it.name,
					NATIONS_BALANCE.settlement.maxTaxPercent
				)
			}
		cities.putAll(settlements)
		Settlement.watchUpdates { change ->
			val id: Oid<Settlement> = change.oid

			change[Settlement::cityState]?.also {
				val data: SettlementCache.SettlementData = SettlementCache[id]

				val newState = it.nullable()?.enumValue<Settlement.CityState>()

				if (newState == Settlement.CityState.ACTIVE) {
					cities[data.territory] = TradeCityData(data.id, data.tradeCityType ?: TradeCityType.SETTLEMENT, data.territory, data.name, NATIONS_BALANCE.settlement.maxTaxPercent)
				} else {
					cities.remove(data.territory)
				}
			}
		}
		Settlement.watchDeletes { change ->
			val oid = change.oid
			cities.filter { it.value.cityOid == oid }.forEach { cities.remove(it.value.territoryId) }
		}

		val npcType = TradeCityType.NPC
		val npcCities = NPCTerritoryOwner.all().associate {
			it.territory to TradeCityData(
				it._id,
				npcType,
				it.territory,
				it.name,
				NATIONS_BALANCE.settlement.maxTaxPercent
			)
		}
		cities.putAll(npcCities)
		NPCTerritoryOwner.watchInserts { change ->
			val fullDocument = change.fullDocument ?: return@watchInserts
			cities[fullDocument.territory] =
				TradeCityData(
					change.oid, npcType, fullDocument.territory, fullDocument.name, NATIONS_BALANCE.settlement.maxTaxPercent
				)
		}
		NPCTerritoryOwner.watchDeletes { change ->
			val oid = change.oid
			cities.filter { it.value.cityOid == oid }.forEach { cities.remove(it.value.territoryId) }
		}
	}

	fun getAll(): List<TradeCityData> = cities.values.toList()

	fun isCity(territory: RegionTerritory): Boolean {
		return cities.containsKey(territory.id)
	}

	fun getIfCity(territory: RegionTerritory): TradeCityData? {
		return cities[territory.id]
	}
}
