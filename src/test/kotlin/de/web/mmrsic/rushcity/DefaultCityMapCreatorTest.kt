package de.web.mmrsic.rushcity

import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.*

/**
 * Test cases for default map creation.
 */
class DefaultCityMapCreatorTest {

    /**
     * Test whether creation of a 20x15 map with default streets yields the expected result.
     */
    @Test
    fun create20x15() {
        val unitUnderTest = DefaultCityMapCreator()

        val numCols = 20
        val numRows = 15
        val createdMap = unitUnderTest.create(numRows, numCols)
        assertEquals(numRows, createdMap.numRows, "Number of map rows")
        assertEquals(numCols, createdMap.numCols, "Number of map columns")
        for (dir in enumValues<Direction>()) {
            assertNotNull(createdMap.frame[dir], "Frame in direction $dir must not be null")
            assertEquals(dir, createdMap.frame[dir]?.direction, "Direction of $dir border")
        }

        val mapOutput = ByteArrayOutputStream()
        createdMap.print(PrintStream(mapOutput))
        val expected = ("Map 20x15\r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "XXXXXXXXXXXXXXXXXXXX\r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "XXXXXXXXXXXXXXXXXXXX\r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n" +
                "XXXXXXXXXXXXXXXXXXXX\r\n" +
                "  X    X    X    X  \r\n" +
                "  X    X    X    X  \r\n").replace("\r\n", System.getProperty("line.separator"))
        assertEquals(expected, mapOutput.toString())

        // Check a non-street
        val nonStreetCoord = CityMap.Coordinate(CityMap.Row(0), CityMap.Column(0))
        assertFalse(createdMap.isStreet(nonStreetCoord))
        assertNull(createdMap.streetAt(nonStreetCoord))

        // Check some arbitrary coordinate with a street
        val streetCoord = CityMap.Coordinate(CityMap.Row(2), CityMap.Column(7))
        assertTrue(createdMap.isStreet(streetCoord))
        assertEquals(streetCoord, createdMap.streetAt(streetCoord)!!.coordinate)

        // Check neighbors
        assertEquals(4, createdMap.neighbors(createdMap.streetAt(streetCoord)!!).size)

        // Check shortest path
        createdMap.frame[Direction.NORTHBOUND]
        val start = createdMap.streetAt(0, 2)!!
        val target = createdMap.streetAt(numRows - 1, 2)!!
        val path = start.shortestPath(target)
        assertEquals(numRows, path.length())
        for (expectedIdx in 0 until numRows) {
            assertEquals(expectedIdx, path.streets[expectedIdx].coordinate.row.index)
        }
    }


}