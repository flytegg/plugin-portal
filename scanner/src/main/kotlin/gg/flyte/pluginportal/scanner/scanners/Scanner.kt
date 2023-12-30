package gg.flyte.pluginportal.scanner.scanners

import gg.flyte.pluginportal.api.type.MarketplacePlugin
import gg.flyte.pluginportal.scanner.MongoPlugin
import io.github.cdimascio.dotenv.dotenv
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.coroutine.insertOne
import org.litote.kmongo.reactivestreams.KMongo

abstract class Scanner {

    private val client = KMongo.createClient(dotenv {  }["MONGO_URI"]).coroutine
    private val database = client.getDatabase("pp-2_0")
    private val collection get() = database.getCollection<MongoPlugin>("plugins")

    abstract suspend fun scan()
    suspend fun MarketplacePlugin.addPluginToDatabase() {
        println("Inserting plugin: ${getUniqueName()}")
        collection.insertOne(this.toMongoPlugin())
    }

    private fun MarketplacePlugin.toMongoPlugin() = MongoPlugin(
        this.id,
        this.displayInfo,
        this.statistics,
        this.releaseData,
        this.versions,
        this.alternatePluginIds
    )
}