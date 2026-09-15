package com.coffeepeek.api.model.response.shop

import com.coffeepeek.api.model.ApiResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapResponseDtoTest {

    @Test
    fun decodesPascalCaseMapContract() {
        val response = Json.decodeFromString<ApiResponse<GetShopsInBoundsResponseDto>>(
            """
                {
                  "IsSuccess": true,
                  "Message": "Operation successful",
                  "Data": {
                    "Shops": [{
                      "Id": "shop-1",
                      "Latitude": 53.9,
                      "Longitude": 27.56,
                      "Title": "Coffee Shop",
                      "Type": "Specialty",
                      "PrimaryZoneId": "zone-1"
                    }],
                    "Clusters": [{
                      "Id": "10:590:331",
                      "Latitude": 53.9,
                      "Longitude": 27.56,
                      "Count": 18,
                      "Bounds": {
                        "MinLatitude": 53.87,
                        "MinLongitude": 27.51,
                        "MaxLatitude": 53.93,
                        "MaxLongitude": 27.62
                      }
                    }],
                    "Zones": [{
                      "Id": "zone-1",
                      "Name": "Октябрьская",
                      "Description": "Кофейный район",
                      "Latitude": 53.89,
                      "Longitude": 27.57,
                      "RadiusMeters": 500,
                      "ShopCount": 7
                    }],
                    "IsTruncated": true
                  }
                }
            """.trimIndent(),
        )

        assertTrue(response.isSuccess)
        val data = requireNotNull(response.data)
        assertEquals("zone-1", data.shops.single().primaryZoneId)
        assertEquals(18, data.clusters.single().count)
        assertEquals(53.87, data.clusters.single().bounds.minLatitude)
        assertEquals("Октябрьская", data.zones.single().name)
        assertEquals(500.0, data.zones.single().radiusMeters)
        assertTrue(data.isTruncated)
    }
}
