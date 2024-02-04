package gg.flyte.pluginportal.backend.routing.routes.v1.plugins

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import gg.flyte.pluginportal.backend.gson
import gg.flyte.pluginportal.client.PaginatedResult
import gg.flyte.pluginportal.client.Pagination
import gg.flyte.pluginportal.backend.routing.routes.v1.recognize.HashType
import gg.flyte.pluginportal.api.type.MarketplacePlugin
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object PluginService {

    suspend fun getPaginatedResultFromDatabase(limit: Int, offset: Int, filterNameStartsWith: String?): PaginatedResult<MarketplacePlugin> {
        // Construct a regular expression for names starting with the specified value
        val regex = "^$filterNameStartsWith"

        // Query your KMongo collection with filters to get the total count
        val totalCountQuery = pluginCollection
            .run {
                if (filterNameStartsWith != null) {
                    countDocuments(Filters.regex("displayInfo.name", regex))
                } else {
                    countDocuments()
                }
            }

        // Query your KMongo collection with pagination parameters and filters
        val result = pluginCollection
            .run {
                if (filterNameStartsWith != null) {
                    find(Filters.regex("displayInfo.name", regex))
                } else {
                    find()
                }
            }
            .skip(offset)
            .limit(limit)
            .toList()

        return PaginatedResult(
            Pagination(limit, offset, totalCountQuery.toInt()),
            result.map { it.toDto() }
        )
    }




    private val client = KMongo.createClient(
        MongoClientSettings.builder()
            .applyConnectionString(
                ConnectionString(System.getenv("MONGO_URI") ?: dotenv {}["MONGO_URI"])
            )
            .build()
    ).coroutine
    private val database = client.getDatabase("pp-2_0")
    private val pluginCollection = database.getCollection<MongoPlugin>("plugins")

//    suspend fun getPaginatedPlugins(
//        name: String,
//        page: Int,
//        limit: Int
//    ): Flow<MongoPlugin> = pluginCollection
//        .find()
//        .filter(Filters.regex("name", name, "i"))
//        .skip(skip = (page - 1) * limit)
//        .limit(limit = limit)
//        .partial(true)
//        .toFlow()

    suspend fun getPlugin(id: String) = pluginCollection.findOneById(id)

    fun getPluginByHashes(hashes: HashSet<HashMap<HashType, String>>): Flow<MongoPlugin> {

        val newHashes = hashSetOf<String>().apply {
            hashes.forEach { hash -> addAll(hash.entries.map { it.value }) }
        }

        return pluginCollection.find().toFlow()
            .filter { plugin ->
                val pluginJson = gson.toJson(plugin)
                newHashes.any { hash -> pluginJson.contains(hash) }
            }

    }
}