package de.web.mmrsic.rushcity

import kotlin.math.floor
import kotlin.math.min

/**
 * A car is able to drive from a start street to a target street where the adjoining parking lots are used
 * to "slide" onto and off the city map.
 */
class Car(private val start: CityMap.Street, private val target: CityMap.Street) : TimeAware, Positionable {
    private val path = CarPath(start.shortestLanePath(target))
    private val front = FrontPart(path.lanePath.lanes[0])
    private val rear = RearPart(path.path[0])
    var coveredDistance = 0.0

    override fun x() = rear.x() + when (rear.directionTo(front)) {
        Direction.EASTBOUND -> distanceOffset()
        Direction.WESTBOUND -> -distanceOffset()
        else -> 0.0
    }

    override fun y() = rear.y() + when (rear.directionTo(front)) {
        Direction.NORTHBOUND -> -distanceOffset()
        Direction.SOUTHBOUND -> distanceOffset()
        else -> 0.0
    }

    /** Current direction this car is heading. */
    fun direction() = rear.directionTo(front)

    /** All directions that are blocked by this car for other cars. */
    fun blockingDirections(): Collection<Direction> {
        val result = Direction.values().toMutableList()
        if (front.carBlockable !== rear.carBlockable) {
            result.remove(direction())
        }
        return result
    }

    private fun distanceOffset() = coveredDistance.rem(1)

    /** Check wether this car has reached its target on the city map. */
    fun isAtTarget(): Boolean = coveredDistance >= path.length()

    override fun toString(): String = "Car#${hashCode()} " +
            "[dist=$coveredDistance/${path.length()}][x/y=${x()}/${y()}]" +
            "[start: $start][target: $target][$front][$rear]"

    override fun addTime(deltaTime: Double) {
        if (isAtTarget()) return

        val pathSize = path.path.size
        val oldRearLaneIdx = floor(coveredDistance).toInt()

        val newDist = coveredDistance + deltaTime
        val maxRearLaneIdx = min(floor(newDist).toInt(), pathSize - 1)
        for (rearLaneIdx in (oldRearLaneIdx)..maxRearLaneIdx) {
            val frontLaneIdx = rearLaneIdx + 1
            val newRearLane = path.path[rearLaneIdx]
            val newFrontLane: CarBlockable? = if (frontLaneIdx < pathSize) path.path[frontLaneIdx] else null
            rear.advanceTo(newRearLane)
            coveredDistance = rearLaneIdx.toDouble()
            if (newFrontLane == null) {
                return
            }
            val newDirection = newRearLane.directionTo(newFrontLane)
            if (newFrontLane.isBlocked() && newFrontLane.blockedDirections().contains(newDirection)) {
                return
            }
            front.advanceTo(newFrontLane)
            coveredDistance = newDist
        }
    }

    /**
     * A car part.
     */
    abstract class Part(var carBlockable: CarBlockable) : Positionable {
        override fun x() = carBlockable.x()
        override fun y() = carBlockable.y()
        open fun advanceTo(next: CarBlockable) {
            carBlockable = next
        }
    }

    /**
     * The front part of a car.
     */
    inner class FrontPart(streetBlock: CarUsable) : Part(streetBlock) {

        init {
            streetBlock.addEnteringCar(this@Car)
        }

        override fun toString(): String {
            return "${x()}/${y()} Front->$carBlockable"
        }

        override fun advanceTo(next: CarBlockable) {
            val sb = carBlockable
            if (sb as? CarUsable != null) {
                sb.removeEnteringCar(this@Car)
            }
            if (next as? CarUsable != null) {
                next.addEnteringCar(this@Car)
            }
            super.advanceTo(next)
        }
    }

    /**
     * The rear part of a car.
     */
    inner class RearPart(streetBlock: CarBlockable) : Part(streetBlock) {

        init {
            streetBlock.blockingCar = this@Car
        }

        override fun toString(): String {
            return "${x()}/${y()} Rear->$carBlockable"
        }

        override fun advanceTo(next: CarBlockable) {
            carBlockable.blockingCar = null
            next.blockingCar = this@Car
            super.advanceTo(next)
        }
    }
}

/**
 * An instance blockable by a single car for all other cars using any of its blocked directions.
 */
interface CarBlockable : Positionable {
    /**
     * The only car blocking this instance for other cars.
     */
    var blockingCar: Car?

    /**
     * Whether this instance is currently blocked for cars.
     */
    fun isBlocked() = blockingCar != null

    /**
     * All directions of a car this instance is blocking.
     */
    fun blockedDirections(): Collection<Direction>

}

/**
 * An object that may be used by cars which may enter it or block it.
 */
interface CarUsable : CarBlockable {
    /**
     * All cars currently entering this instance.
     */
    fun enteringCars(): Collection<Car>

    /**
     * Add a given car to the entering cars of this instance.
     */
    fun addEnteringCar(car: Car)

    /**
     * Remove a given car from the entering cars of this instance.
     */
    fun removeEnteringCar(car: Car)
}

/**
 * The path of a car on a city map.
 */
internal class CarPath(val lanePath: LanePath) {
    val path: List<CarBlockable>

    init {
        val firstStreet = lanePath.lanes[0].street
        val lastStreet = lanePath.lanes[lanePath.length() - 1].street
        val first = firstStreet.border()!!.carStart(firstStreet)
        val last = lastStreet.border()!!.carTarget(lastStreet)
        path = mutableListOf(first, last)
        path.addAll(1, lanePath.lanes)
    }

    fun length() = path.size - 1

    override fun toString(): String {
        return "CarPath (len=${length()}): $path"
    }
}

