package de.web.mmrsic.rushcity

import org.junit.Test
import kotlin.test.assertEquals


/**
 * Test suite for Car instances.
 */
class CarTest {

    /**
     * Test whether a car drives along the expected path within a 12x8 city map.
     */
    @Test
    fun testCreate12x8AndDriveCar() {
        val mapCreator = DefaultCityMapCreator()
        val createdMap = mapCreator.create(8, 12)
        createdMap.print(System.out)

        val start = createdMap.streetAt(1, 0)!!
        val target = createdMap.streetAt(0, 4)!!

        // Check car
        val unitUnderTest = Car(start, target)
        assertCarPositionedAt(unitUnderTest, 0.0, -1.0, 3.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 1.0, 0.0, 3.0)
        unitUnderTest.addTime(8.0)
        assertCarPositionedAt(unitUnderTest, 9.0, 8.0, 3.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 10.0, 9.0, 3.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 11.0, 9.0, 2.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 12.0, 9.0, 1.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 13.0, 9.0, 0.0)
        unitUnderTest.addTime(1.0)
        assertCarPositionedAt(unitUnderTest, 14.0, 9.0, -1.0)
        unitUnderTest.addTime(1.12)
        assertCarPositionedAt(unitUnderTest, 14.0, 9.0, -1.0)
    }

    @Test
    fun testMultipleCarsAndTrafficLightQueue() {
        val map = DefaultCityMapCreator().create(15, 15)
        map.print(System.out)
        map.createDefaultTrafficLights(CityMap.TrafficLight.LightColor.GREEN)

        val start = map.streetAt(2, 14)!!
        val target = map.streetAt(14, 12)!!
        val carPathDist = 32.0

        val cars = mutableListOf<Car>()
        cars.add(Car(start, target))
        cars.last().addTime(40.0)
        assertCarPositionedAt(cars.last(), carPathDist, target.x(), target.y() + 2)

        val redLightStreet = map.streetAt(7, 12)!!
        val redLightLane = redLightStreet.lanes[Direction.SOUTHBOUND]!!
        val redTrafficLight = redLightLane.trafficLight!!
        redTrafficLight.setRed()
        println("Red light lane: $redLightLane")

        println("Fill traffic light waiting queue northbound")
        for (carNo in 1..10) {
            cars.add(Car(start, target))
            cars.last().addTime(20.0)
            assertCarPositionedAt(cars.last(), 16.0 - carNo, redLightLane.x(), redLightLane.y() - carNo)
        }

        println("Fill traffic light waiting queue eastbound")
        for (carNo in 11..16) {
            cars.add(Car(start, target))
            cars.last().addTime(6.27)
            assertCarPositionedAt(cars.last(), 16.0 - carNo, redLightLane.x() - 10 + carNo, redLightLane.y() - 10)
        }

        println("Release all cars and expect them to reach the target")
        redTrafficLight.setGreen()
        cars.forEach { car ->
            car.addTime(55.5555)
            assertCarPositionedAt(car, 32.0, cars.first().x(), cars.first().y())
        }
    }

    /**
     * Test whether a Car moves along the path even if a step is less than a single Lane.
     */
    @Test
    fun testMiniSteps() {
        val map = DefaultCityMapCreator().create(3, 3)
        map.print(System.out)

        val start = map.streetAt(0, 1)!!
        val target = map.streetAt(1, 2)!!

        // Check car movement
        val unitUnderTest = Car(start, target)
        unitUnderTest.addTime(0.5)
        assertCarPositionedAt(unitUnderTest, 0.5, 2.0, -0.5)
        unitUnderTest.addTime(0.5)
        assertCarPositionedAt(unitUnderTest, 1.0, 2.0, 0.0)
        unitUnderTest.addTime(1.5)
        assertCarPositionedAt(unitUnderTest, 2.5, 2.0, 1.5)
    }

    // HELPERS //

    /**
     * Assert that a given car is at a given distance, and a given x,y coordinate.
     */
    private fun assertCarPositionedAt(car: Car, dist: Double, x: Double, y: Double) {
        println(car)
        assertEquals(dist, car.coveredDistance, "Covered distance")
        assertEquals(x, car.x(), "X position")
        assertEquals(y, car.y(), "Y position")
    }
}