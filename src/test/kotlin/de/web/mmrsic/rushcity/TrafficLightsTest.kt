package de.web.mmrsic.rushcity


import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test cases for traffic lights control in Rush City game.
 */
class TrafficLightsTest {

    @Test
    fun testGameTrafficLightsControlCreation() {

        val vehiclePhase = 5.0
        val pedestrianPhase = 2.0

        val unitUnderTest = TrafficLightsControl(
            object : TrafficLightsControl.Pattern {
                override fun vehiclePhase(): Double = vehiclePhase
                override fun pedestrianPhase(): Double = pedestrianPhase
            })

        val trafficLights = listOf(
            CityMap.TrafficLight(CityMap.TrafficLight.LightColor.RED),
            CityMap.TrafficLight(CityMap.TrafficLight.LightColor.RED),
            CityMap.TrafficLight(CityMap.TrafficLight.LightColor.RED),
            CityMap.TrafficLight(CityMap.TrafficLight.LightColor.RED)
        )

        // At the beginning, all light must be as RED
        assertAllRed(trafficLights, 0)

        var time = vehiclePhase / 2
        unitUnderTest.addTime(time)
        unitUnderTest.setLights(trafficLights)
        assertAllRedExcept(trafficLights, 0, time)

        time += vehiclePhase / 2
        unitUnderTest.addTime(vehiclePhase / 2)
        unitUnderTest.setLights(trafficLights)
        assertAllRed(trafficLights, time)

        // After a whole vehicle phase and a whole pedestrian phase,
        // the second traffic light must have turned to green
        time += pedestrianPhase
        unitUnderTest.addTime(pedestrianPhase)
        unitUnderTest.setLights(trafficLights)
        assertAllRedExcept(trafficLights, 1, time)

        // The next vehicle + pedestrian phase must have turned the traffic light #3 to green
        time += vehiclePhase + pedestrianPhase
        unitUnderTest.addTime(vehiclePhase + pedestrianPhase)
        unitUnderTest.setLights(trafficLights)
        assertAllRedExcept(trafficLights, 2, time)

        // The last traffic light #4 must have turned to green after all traffic lights turned to red
        // just after the vehicle phase
        time += vehiclePhase
        unitUnderTest.addTime(vehiclePhase)
        unitUnderTest.setLights(trafficLights)
        assertAllRed(trafficLights, time)
        time += pedestrianPhase
        unitUnderTest.addTime(pedestrianPhase)
        unitUnderTest.setLights(trafficLights)
        assertAllRedExcept(trafficLights, 3, time)

        // The next vehicle + pedestrian phase must have turned the traffic light #0 to green again,
        // as the circle starts anew
        time += vehiclePhase + pedestrianPhase
        unitUnderTest.addTime(vehiclePhase + pedestrianPhase)
        unitUnderTest.setLights(trafficLights)
        assertAllRedExcept(trafficLights, 0, time)
    }

    /**
     * Assert that in a given list of [CityMap.TrafficLight]s all elements show [CityMap.TrafficLight.LightColor.RED].
     * @param trafficLights the list to check
     * @param time the time passed - used in the assertion message
     */
    private fun assertAllRed(trafficLights: List<CityMap.TrafficLight>, time: Number) {
        for ((idx, trafficLight) in trafficLights.withIndex()) {
            assertTrue(trafficLight.isRed(), "After $time time, the light at $idx must be RED")
        }
    }

    private fun assertAllRedExcept(trafficLights: List<CityMap.TrafficLight>, greenIdx: Int, time: Number) {
        for ((idx, trafficLight) in trafficLights.withIndex()) {
            val expectedLight = when {
                idx == greenIdx -> CityMap.TrafficLight.LightColor.GREEN
                else -> CityMap.TrafficLight.LightColor.RED
            }
            assertEquals(
                expectedLight, trafficLight.color,
                " After $time time, the light at index $idx must show $expectedLight"
            )
        }
    }

}