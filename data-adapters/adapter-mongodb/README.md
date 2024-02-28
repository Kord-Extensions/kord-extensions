# MongoDB Adapter

This module provides a data adapter (and some utilities) for MongoDB.
This includes some codecs, for commonly-used types - which you can use in your own codec registry if you wish.

# Usage

* **Maven repo:** Maven Central for releases, `https://s01.oss.sonatype.org/content/repositories/snapshots` for
  snapshots
* **Maven coordinates:** `com.kotlindiscord.kord.extensions:adapter-mongodb:VERSION`

To switch to the MongoDB data adapter follow these steps:

1. Add the MongoDB dependencies to your project. You can get the latest version number
   [from Maven Central](https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-kotlin-sync):
	- `org.mongodb:mongodb-driver-kotlin-coroutine`
	- `org.mongodb:bson-kotlinx`
2. Set the `ADAPTER_MONGODB_URI` environmental variable to a MongoDB connection string.
3. Use the `mongoDB` function to set up the data adapter.

   ```kotlin
   suspend fun main() {
       val bot = ExtensibleBot(System.getenv("TOKEN")) {
           mongoDB()
       }

       bot.start()
   }
   ```

4. If you use MongoDB elsewhere in your project, you can use the provided codecs to handle these types:
	- `DateTimePeriod` (kotlinx Datetime)
	- `Instant` (Kotlinx Datetime)
	- `Snowflake` (Kord)
	- `StorageType` (KordEx)

   ```kotlin
   // import: com.kotlindiscord.kord.extensions.adapters.mongodb.kordExCodecRegistry
   val registry = CodecRegistries.fromRegistries(
       kordExCodecRegistry,
       MongoClientSettings.getDefaultCodecRegistry(),
   )

   val client = MongoClient.create(MONGODB_URI)
   val database = client.getDatabase("database-name")

   val collection = database.getCollection<T>("name")
       .withCodecRegistry(registry)
   ```

   For more information on working with codecs,
   see [the MongoDB documentation](https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/data-formats/codecs).

# Notes

* All provided codecs store their respective data types as strings in the database.
* If you need to migrate from another data adapter to this one, you should read the code for both data adapters before
  writing your own migration code.
