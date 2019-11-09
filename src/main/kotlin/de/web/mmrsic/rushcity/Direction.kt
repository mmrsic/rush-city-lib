package de.web.mmrsic.rushcity

import kotlin.random.Random

/**
 * All possible directions on a city map.
 */
enum class Direction {
    NORTHBOUND {
        override fun turnLeft(): Direction {
            return WESTBOUND
        }
    },

    EASTBOUND {
        override fun turnLeft(): Direction {
            return NORTHBOUND
        }
    },

    WESTBOUND {
        override fun turnLeft(): Direction {
            return SOUTHBOUND
        }
    },

    SOUTHBOUND {
        override fun turnLeft(): Direction {
            return EASTBOUND
        }
    };

    abstract fun turnLeft(): Direction
    fun turnRight(): Direction = opposite().turnLeft()
    fun opposite(): Direction = turnLeft().turnLeft()
}

/**
 * A sublist of given size for some random directions of all possible directions.
 */
fun randomDistinctDirections(num: Int): List<Direction> {
    val candidates = enumValues<Direction>().toMutableList()
    val result = mutableListOf<Direction>()
    for (resultIdx in 0 until num) {
        val resultElem = candidates.removeAt(Random.nextInt(candidates.size))
        result.add(resultElem)
    }
    return result
}