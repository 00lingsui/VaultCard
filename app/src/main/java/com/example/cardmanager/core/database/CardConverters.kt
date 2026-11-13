package com.example.cardmanager.core.database

import androidx.room.TypeConverter
import com.example.cardmanager.core.database.entity.CardNetwork

class CardConverters {
    @TypeConverter
    fun fromCardNetwork(network: CardNetwork): String = network.name

    @TypeConverter
    fun toCardNetwork(value: String?): CardNetwork = CardNetwork.fromString(value)
}
