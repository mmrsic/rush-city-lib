package de.web.mmrsic.rushcity

/**
 * A list of streets which compose a path on a city map.
 */
class StreetPath(val streets: List<CityMap.Street>) {
    fun length(): Int = streets.size

    override fun toString(): String {
        return "StreetPath (len=${length()}): $streets"
    }
}

/**
 * A list of lanes which compose a path on the streets of a city map.
 */
class LanePath(val lanes: List<CityMap.Street.Lane>) {
    override fun toString(): String = "LanePath (len=${length()}): $lanes"
    fun length(): Int = lanes.size
}

/**
 * Find the path of lanes between two given streets.
 */
fun CityMap.Street.shortestLanePath(target: CityMap.Street): LanePath {
    val streetPath = this.shortestPath(target)

    var lastStreet = this
    var lastDir = this.border()!!.direction.opposite()
    val resultLaneList = mutableListOf<CityMap.Street.Lane>()
    resultLaneList.add(lastStreet.lanes[lastDir]!!)
    for (st in streetPath.streets.minus(this)) {
        val nextDir = lastStreet.directionTo(st)
        if (nextDir == lastDir || nextDir == lastDir.turnLeft()) {
            resultLaneList.add(lastStreet.lanes[lastDir.turnLeft()]!!)
        }
        if (nextDir == lastDir.turnLeft()) {
            resultLaneList.add(lastStreet.lanes[lastDir.opposite()]!!)
        }
        resultLaneList.add(st.lanes[nextDir]!!)
        lastStreet = st
        lastDir = nextDir
    }
    resultLaneList.add(target.lanes[lastDir.turnLeft()]!!)

    return LanePath(resultLaneList)
}

/**
 * Find the path of streets between two given streets.
 */
fun CityMap.Street.shortestPath(target: CityMap.Street): StreetPath {
    val handledStreets: MutableList<CityMap.Street> = mutableListOf()
    val toHandle: MutableList<StreetNode> = mutableListOf()
    toHandle.add(StreetNode(this))
    while (toHandle.isNotEmpty()) {
        val currNode = toHandle.removeAt(0)
        if (currNode.street.coordinate == target.coordinate) {
            return StreetPath(currNode.path())
        } else {
            val newDistance = currNode.distance + 1
            currNode.street.neighbors().filter { street -> !handledStreets.contains(street) }
                .forEach { street -> toHandle.add(StreetNode(street, newDistance, currNode)) }
        }
        handledStreets.add(currNode.street)
    }
    throw IllegalArgumentException("There is no path from $this to $target")
}


/**
 * A helper class for building a graph of street nodes to find the shortest path.
 */
private class StreetNode(val street: CityMap.Street, var distance: Int = 0, val predecessor: StreetNode? = null) {
    /**
     * The complete path of this street node as denoted by the chain of all predecessors.
     */
    fun path(): MutableList<CityMap.Street> = when (predecessor) {
        null -> mutableListOf(street)
        else -> predecessor.path().plus(street).toMutableList()
    }
}
