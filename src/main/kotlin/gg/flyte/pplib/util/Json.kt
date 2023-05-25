package gg.flyte.pplib.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

val objectMapper = ObjectMapper()

fun getJsonTree(json: String): JsonNode {
    return objectMapper.readTree(json)
}