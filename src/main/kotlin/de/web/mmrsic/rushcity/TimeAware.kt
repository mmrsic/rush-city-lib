package de.web.mmrsic.rushcity

/**
 * A game object that is aware of game time.
 */
interface TimeAware {
    /**
     * Add a given delta time to this instance.
     * @param deltaTime value to add to the overall time of this instance
     */
    fun addTime(deltaTime: Double)
}