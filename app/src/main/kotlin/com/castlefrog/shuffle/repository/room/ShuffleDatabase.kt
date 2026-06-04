package com.castlefrog.shuffle.repository.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [ShuffleListEntity::class, ShuffleItemEntity::class, SelectedListEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ShuffleDatabase : RoomDatabase() {
    abstract fun shuffleListDao(): ShuffleListDao

    companion object {
        @Volatile
        private var INSTANCE: ShuffleDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): ShuffleDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, scope).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context, scope: CoroutineScope): ShuffleDatabase {
            var database: ShuffleDatabase? = null
            return Room.databaseBuilder(
                context.applicationContext,
                ShuffleDatabase::class.java,
                "shuffle.db",
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    scope.launch {
                        database?.shuffleListDao()?.let { prepopulate(it) }
                    }
                }
            }).build().also { database = it }
        }

        private suspend fun prepopulate(dao: ShuffleListDao) {
            val lists = listOf(
                ShuffleListEntity(name = "Date Night", subsetSize = 1, createdAt = 0),
                ShuffleListEntity(name = "Exercise", subsetSize = 1, createdAt = 1),
                ShuffleListEntity(name = "Meals", subsetSize = 1, createdAt = 2),
            )
            val items = buildList {
                addAll(DATE_NIGHT_ITEMS.mapIndexed { i, text ->
                    ShuffleItemEntity(listName = "Date Night", text = text, sortOrder = i)
                })
                addAll(EXERCISE_ITEMS.mapIndexed { i, text ->
                    ShuffleItemEntity(listName = "Exercise", text = text, sortOrder = i)
                })
                addAll(MEALS_ITEMS.mapIndexed { i, text ->
                    ShuffleItemEntity(listName = "Meals", text = text, sortOrder = i)
                })
            }
            lists.forEach { dao.insertList(it) }
            dao.insertItems(items)
            dao.upsertSelectedList(SelectedListEntity(listName = "Date Night"))
        }

        private val DATE_NIGHT_ITEMS = listOf(
            "Concert", "Wine and Paint", "Festival", "Weekend Trip out of town", "Pamper Night",
            "Cruise", "Dinner Rooftop", "Cooking Class", "Tourist Attraction", "Train Ride",
            "Hotel with View", "Wine tasting or brewery tour", "Water Ballon Fight", "Arcade",
            "Learn Chess", "Share Secrets", "Meditate", "Jigsaw Puzzle",
            "Get \"5 senses\" gifts for each other", "Planetarium", "Clean House",
            "Throw out unwanted items", "Read aloud to each other",
            "Favorites night (each other's favorite food, drink, activity)", "Boat Ride",
            "Decorate / Organize a room", "Throwback night", "Plant something",
            "Research together", "Write letters to each other", "Nightclub", "Sporting event",
            "Fix something", "Desert from scratch", "Run / Workout", "Try new game",
            "DIY project", "Message", "Hiking", "Stargazing", "New Restaurant", "Bonfire",
            "Aquarium", "Rock Climbing", "Zoo", "Dinner and Movie", "Swimming", "Ice Cream",
            "Kayaking", "State Fair", "Watch sunrise or sunset", "Indoor Skydiving",
            "Geocaching", "Dance Class", "Trampoline Park", "Paddle Boarding", "Sailing",
            "Cave Exploring", "Axe / Knife Throwing", "Ghost Tour", "Ghost Town", "Scuba Diving",
            "Ziplining", "Bowling", "Drive in Movie", "Horseback Riding", "Opera / Play",
            "Dinner out with friends and family", "Tour Downtown Shops", "Escape room", "Museum",
            "Camping", "Laser Tag", "Road trip", "Air Bob", "Visit national park",
            "Go out for dessert", "Farmers Market", "Mall", "Bookstore", "New skill class",
            "Make time capsule", "Berry picking",
            "\$20 limit to purchase outfit from thrift store, see who has the best",
            "Outdoor picnic", "Antique shop", "Visit local landmarks", "Video game", "Nerf war",
            "Candlelight Dinner", "Dinner (New recipe)", "Indoor picnic", "Easter egg hunt",
            "Create photo collage", "Communication building exercise", "Board game",
            "Create bucket list", "Hot chocolate by fire", "Horror movie marathon",
        )

        private val EXERCISE_ITEMS = listOf(
            "Bear crawl", "Burpee", "Calf raise", "Dip", "Jumping jack", "Leg raises", "Lunge",
            "Mountain climber", "Pelvic lift", "Plank", "Pull-up", "Push-up", "Split jump",
            "Sit Up", "Squat", "Squat thrust",
        )

        private val MEALS_ITEMS = listOf(
            "Lasagna", "Ravioli", "Taco", "Pancakes", "Hot dogs", "Fried chicken", "Burgers",
            "Spaghetti with meatballs", "Sloppy joes", "Steak and potato",
        )
    }
}
