package de.web.mmrsic.rushcity


/**
 * An instance that may be positioned at or relative to a city map.
 */
interface Positionable {
    fun x(): Double
    fun y(): Double
    /**
     * The Direction to another positionable.
     * Invariant: this.directionTo(another) == another.directionTo(this).opposite()
     */
    fun directionTo(anotherPos: Positionable): Direction {
        val thisX = x()
        val otherX = anotherPos.x()
        return if (thisX != otherX) {
            if (thisX < otherX)
                Direction.EASTBOUND
            else
                Direction.WESTBOUND
        } else if (y() < anotherPos.y())
            Direction.SOUTHBOUND
        else Direction.NORTHBOUND
    }
}