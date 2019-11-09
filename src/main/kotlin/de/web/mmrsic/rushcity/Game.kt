package de.web.mmrsic.rushcity

import kotlin.math.floor


/**
 * A single Rush City game.
 */
class Game {
    val time: GameTime = GameTime(0.0)
}

/**
 * Manager of a list of CityMap.TrafficLight elements which are set to green on a round-robin basis.
 */
class TrafficLightsControl(var pattern: Pattern) : TimeAware {

    private val time: GameTime = GameTime(0.0)

    override fun addTime(deltaTime: Double) = time.addTime(deltaTime)

    /**
     * Set a given list of traffic lights according to the current phase of this traffic lights control.
     * @param trafficLights list of traffic lights for which to set the color to red or green
     */
    fun setLights(trafficLights: List<CityMap.TrafficLight>) {
        if (trafficLights.isEmpty()) {
            return
        }

        val singleTrafficLightDuration = pattern.vehiclePhase() + pattern.pedestrianPhase()
        val numCompletedDurations: Double = (time.overall / singleTrafficLightDuration)
        val idxActiveTrafficLight: Int = floor(numCompletedDurations).toInt()
        val timeActiveTrafficLight: Double = numCompletedDurations - idxActiveTrafficLight
        val isActiveTrafficLightGreen = timeActiveTrafficLight < (pattern.vehiclePhase() / (pattern.wholePhase()))
        val listIdxActiveTrafficLight = idxActiveTrafficLight % trafficLights.size
        for ((idx, trafficLight) in trafficLights.withIndex()) {
            when {
                idx != listIdxActiveTrafficLight -> trafficLight.setRed()
                isActiveTrafficLightGreen -> trafficLight.setGreen()
                else -> trafficLight.setRed()
            }
        }
    }

    /**
     * A pattern for a [TrafficLightsControl]. It defines the phases when vehicles may move and when pedestrians may move.
     */
    interface Pattern {
        /**
         * The vehicle phase of a traffic lights control pattern tells how long a single traffic light allows
         * movement of vehicles.
         * @return a value > 0f
         */
        fun vehiclePhase(): Double

        /**
         * The pedestrian phase of a traffic lights control pattern tells how long only pedestrians are allowed to move,
         * that is, how long all vehicles are show the red light.
         * @return a value > 0f
         */
        fun pedestrianPhase(): Double

        /**
         * The whole phase of this pattern as defined by all the its parts.
         * @return [vehiclePhase] + [pedestrianPhase]
         */
        fun wholePhase(): Double = vehiclePhase() + pedestrianPhase()
    }

}


/**
 * Game time instance able to measure the overall completed time.
 */
class GameTime(var overall: Double) : TimeAware {
    override fun addTime(deltaTime: Double) {
        overall += deltaTime
    }
}