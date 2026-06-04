package com.castlefrog.shuffle.repository

import com.castlefrog.shuffle.model.ShuffleItem
import com.castlefrog.shuffle.model.ShuffleList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class InMemoryShuffleListRepository : ShuffleListRepository {
    private val lists = MutableStateFlow<Map<String, ShuffleList>>(
        listOf(
            ShuffleList(
                name = "Date Night",
                subsetSize = 1,

                items = listOf(
                    // Orange
                    "Concert",
                    "Wine and Paint",
                    "Festival",
                    "Weekend Trip out of town",
                    "Pamper Night",
                    "Cruise",
                    "Dinner Rooftop",
                    "Cooking Class",
                    "Tourist Attraction",
                    "Train Ride",
                    "Hotel with View",
                    "Wine tasting or brewery tour",
                    // White
                    "Water Ballon Fight",
                    "Arcade",
                    "Learn Chess",
                    "Share Secrets",
                    "Meditate",
                    "Jigsaw Puzzle",
                    "Get “5 senses” gifts for each other",
                    "Planetarium",
                    "Clean House",
                    "Throw out unwanted items",
                    "Read aloud to each other",
                    "Favorites night (each other’s favorite food, drink, activity)",
                    "Boat Ride",
                    "Decorate / Organize a room",
                    "Throwback night",
                    "Plant something",
                    "Research together",
                    "Write letters to each other",
                    "Nightclub",
                    "Sporting event",
                    "Fix something",
                    "Desert from scratch",
                    "Run / Workout",
                    "Try new game",
                    "DIY project",
                    // Green
                    "Message",
                    "Hiking",
                    "Stargazing",
                    "New Restaurant",
                    "Bonfire",
                    "Aquarium",
                    "Rock Climbing",
                    "Zoo",
                    "Dinner and Movie",
                    "Swimming",
                    "Ice Cream",
                    "Kayaking",
                    "State Fair",
                    "Watch sunrise or sunset",
                    // Red
                    "Indoor Skydiving",
                    "Geocaching",
                    "Train Ride",
                    "Dance Class",
                    "Trampoline Park",
                    "Paddle Boarding",
                    "Sailing",
                    "Cave Exploring",
                    "Axe / Knife Throwing",
                    "Ghost Tour",
                    "Ghost Town",
                    "Scuba Diving",
                    "Ziplining",
                    // Purple
                    "Bowling",
                    "Drive in Movie",
                    "Horseback Riding",
                    "Opera / Play",
                    "Dinner out with friends and family",
                    "Tour Downtown Shops",
                    "Escape room",
                    "Museum",
                    "Camping",
                    "Laser Tag",
                    "Arcade",
                    "Road trip",
                    "Air Bob",
                    // Yellow
                    "Visit national park",
                    "Go out for dessert",
                    "Farmers Market",
                    "Mall",
                    "Bookstore",
                    "New skill class",
                    "Make time capsule",
                    "Berry picking",
                    "$20 limit to purchase outfit from thrift store, see who has the best",
                    "Outdoor picnic",
                    "Antique shop",
                    "Visit local landmarks",
                    // Blue
                    "Video game",
                    "Nerf war",
                    "Candlelight Dinner",
                    "Dinner (New recipe)",
                    "Indoor picnic",
                    "Easter egg hunt",
                    "Create photo collage",
                    "Communication building exercise",
                    "Board game",
                    "Create bucket list",
                    "Hot chocolate by fire",
                    "Horror movie marathon",
            ).map { ShuffleItem(text = it) },
            ),
            ShuffleList(
                name = "Exercise",
                subsetSize = 1,
                items = listOf(
                    "Bear crawl",
                    "Burpee",
                    "Calf raise",
                    "Dip",
                    "Jumping jack",
                    "Leg raises",
                    "Lunge",
                    "Mountain climber",
                    "Pelvic lift",
                    "Plank",
                    "Pull-up",
                    "Push-up",
                    "Split jump",
                    "Sit Up",
                    "Squat",
                    "Squat thrust",
                ).map { ShuffleItem(text = it) },
            ),
            ShuffleList(
                name = "Meals",
                subsetSize = 1,
                items = listOf(
                    "Lasagna",
                    "Ravioli",
                    "Taco",
                    "Pancakes",
                    "Hot dogs",
                    "Fried chicken",
                    "Burgers",
                    "Spaghetti with meatballs",
                    "Sloppy joes",
                    "Steak and potato",
                ).map { ShuffleItem(text = it) },
            ),
        ).associateBy { it.name }
    )

    private var selectedListIndex = 0

    override fun getAllShuffleListNames(): Flow<List<String>> {
        return lists.map { it.keys.toList() }
    }

    override fun getCurrentSelectedList(): Flow<ShuffleList> {
        return lists.map { it.values.toList()[selectedListIndex] }
    }

    override fun setCurrentSelectedList(name: String): Flow<Unit> = flow {
        selectedListIndex = lists.value.keys.indexOf(name)
        emit(Unit)
    }

    override fun getShuffleListByName(name: String): Flow<ShuffleList> {
        return lists.map { it.getValue(name) }
    }

    override fun createShuffleList(name: String): Flow<Unit> = flow {
        lists.value += (name to ShuffleList(name = name, subsetSize = 1, items = emptyList()))
        emit(Unit)
    }

    override fun deleteShuffleList(name: String): Flow<Unit> = flow {
        lists.value -= name
        emit(Unit)
    }

    override fun addItemToShuffleList(name: String, item: String): Flow<Unit> = flow {
        val list = lists.value.getValue(name)
        lists.value += (name to list.copy(items = list.items + ShuffleItem(text = item)))
        emit(Unit)
    }

    override fun removeItemFromShuffleList(name: String, item: String): Flow<Unit> = flow {
        val list = lists.value.getValue(name)
        lists.value += (name to list.copy(items = list.items.filter { it.text != item }))
        emit(Unit)
    }
}
