package de.web.mmrsic.rushcity

import java.io.PrintStream


/**
 * A city map consists of rows and columns where the grid positions denote streets.
 */
class CityMap(val numRows: Int, val numCols: Int) {
    val frame: Map<Direction, Border> = mapOf(
        Direction.NORTHBOUND to Border(this, Direction.NORTHBOUND),
        Direction.EASTBOUND to Border(this, Direction.EASTBOUND),
        Direction.SOUTHBOUND to Border(this, Direction.SOUTHBOUND),
        Direction.WESTBOUND to Border(this, Direction.WESTBOUND)
    )

    internal val rows: Array<Row> = Array(numRows) { idx -> Row(idx) }
    internal val columns: Array<Column> = Array(numCols) { idx -> Column(idx) }
    internal val streets: MutableMap<Coordinate, Street> = mutableMapOf()

    /**
     * Print a representation of this map to a given PrintStream.
     */
    fun print(out: PrintStream) {
        out.append("Map ").print(numCols)
        out.append("x").println(numRows)
        for (rowIdx in 0 until numRows) {
            for (colIdx in 0 until numCols) {
                if (isStreet(Coordinate(Row(rowIdx), Column(colIdx)))) {
                    out.append('X')
                } else {
                    out.append(' ')
                }
            }
            out.println()
        }
    }

    fun streets(): Collection<Street> = streets.values

    /**
     * Check whether there is a street for a given coordinate.
     */
    fun isStreet(coordinate: Coordinate): Boolean = streets[coordinate] != null

    fun streetAt(row: Int, column: Int): Street? = streetAt(Coordinate(Row(row), Column(column)))
    fun streetAt(coordinate: Coordinate): Street? = streets[coordinate]
    fun neighbors(street: Street): Collection<Street> = street.neighbors()

    /** Add traffic lights to all streets that represent cross roads. */
    fun createDefaultTrafficLights(color: TrafficLight.LightColor) =
        streets().forEach { street -> createDefaultTrafficLights(street, color) }

    /** Add traffic lights to all incoming streets of a given street. */
    private fun createDefaultTrafficLights(street: Street, color: TrafficLight.LightColor) {
        val neighbors = neighbors(street)
        if (neighbors.size <= 2) {
            return
        }
        neighbors.forEach { neighbor ->
            street.lanes[neighbor.directionTo(street)]!!.trafficLight = TrafficLight(color)
        }
    }


    /**
     * A single part of a coordinate, e.g. the x position.
     */
    interface CoordinatePart {
        val index: Int
    }

    /**
     * A row of a city map.
     */
    data class Row(override val index: Int) : CoordinatePart {
        override fun toString(): String {
            return "r=$index"
        }
    }

    /**
     * A column of a city map.
     */
    data class Column(override val index: Int) : CoordinatePart {
        override fun toString(): String {
            return "c=$index"
        }
    }

    /**
     * A coordinate of a city map.
     */
    data class Coordinate(val row: Row, val column: Column) {
        override fun toString(): String {
            return "[$row $column]"
        }
    }

    /**
     * A border for a given city map and a given (exiting) direction.
     */
    data class Border(val map: CityMap, val direction: Direction) {
        private val parkingLot = mutableMapOf<Coordinate, ParkingLot>()

        fun carStart(street: Street) = parkingLot(street).start
        fun carTarget(street: Street) = parkingLot(street).target

        private fun parkingLot(street: Street): ParkingLot {
            if (!isAdjoining(street)) {
                throw IllegalArgumentException()
            }
            parkingLot.getOrPut(street.coordinate) { -> ParkingLot(street) }
            return parkingLot[street.coordinate]!!
        }

        /**
         * Check whether a given street is adjoining this border.
         */
        fun isAdjoining(street: Street): Boolean {
            return when (direction) {
                Direction.NORTHBOUND -> street.coordinate.row.index == 0
                Direction.EASTBOUND -> street.coordinate.column.index == map.numCols - 1
                Direction.WESTBOUND -> street.coordinate.column.index == 0
                Direction.SOUTHBOUND -> street.coordinate.row.index == map.numRows - 1
            }
        }

        /**
         * A parking lot which may accommodate a single car.
         */
        class ParkingLot(val street: Street) {

            val start = object : CarBlockable {
                override var blockingCar: Car? = null
                override fun directions(): Collection<Direction> = listOf(street.border()!!.direction)
                override fun x() = street.x() + when (street.border()!!.direction) {
                    Direction.EASTBOUND -> 2.0
                    Direction.WESTBOUND -> -1.0
                    Direction.SOUTHBOUND -> 1.0
                    Direction.NORTHBOUND -> 0.0
                }

                override fun y() = street.y() + when (street.border()!!.direction) {
                    Direction.NORTHBOUND -> -1.0
                    Direction.SOUTHBOUND -> 2.0
                    Direction.WESTBOUND -> 1.0
                    Direction.EASTBOUND -> 0.0
                }

                override fun toString() = "ParkingLotStart ${isBlocked()}"
            }

            val target = object : CarBlockable {
                override fun toString() = "ParkingLotTarget ${isBlocked()}"
                override var blockingCar: Car? = null
                override fun isBlocked() = false
                override fun directions(): Collection<Direction> = listOf(street.border()!!.direction)
                override fun x() = street.x() + when (street.border()!!.direction) {
                    Direction.EASTBOUND -> 2.0
                    Direction.WESTBOUND -> -1.0
                    Direction.NORTHBOUND -> 1.0
                    Direction.SOUTHBOUND -> 0.0
                }

                override fun y() = street.y() + when (street.border()!!.direction) {
                    Direction.NORTHBOUND -> -1.0
                    Direction.SOUTHBOUND -> 2.0
                    Direction.EASTBOUND -> 1.0
                    Direction.WESTBOUND -> 0.0
                }
            }
        }
    }

    /**
     * A street is a positionable tile of a city map with a coordinate, consisting of four lanes.
     */
    data class Street(val map: CityMap, val coordinate: Coordinate) : Positionable {
        /**
         * Mapping from entering direction to lane.
         */
        val lanes: Map<Direction, Lane>

        init {
            lanes = mapOf(
                Direction.NORTHBOUND to Lane(this, Direction.NORTHBOUND),
                Direction.EASTBOUND to Lane(this, Direction.EASTBOUND),
                Direction.SOUTHBOUND to Lane(this, Direction.SOUTHBOUND),
                Direction.WESTBOUND to Lane(this, Direction.WESTBOUND)
            )
        }

        override fun toString(): String {
            return "Street$coordinate"
        }

        override fun x(): Double {
            return coordinate.column.index * 2.0
        }

        override fun y(): Double {
            return coordinate.row.index * 2.0
        }

        fun neighbors(): Collection<Street> {
            val origRow = coordinate.row
            val origCol = coordinate.column
            val coords = listOf(
                Coordinate(Row(origRow.index - 1), origCol),
                Coordinate(origRow, Column(origCol.index - 1)),
                Coordinate(origRow, Column(origCol.index + 1)),
                Coordinate(Row(origRow.index + 1), origCol)
            )

            val result = hashSetOf<Street>()
            coords.filter { coord -> map.isStreet(coord) }.forEach { coord -> result.add(map.streetAt(coord)!!) }
            return result
        }

        /**
         * The border adjacent to this street if any.
         */
        fun border(): Border? {
            return when {
                coordinate.row.index == 0 -> map.frame[Direction.NORTHBOUND]!!
                coordinate.column.index == 0 -> map.frame[Direction.WESTBOUND]!!
                coordinate.row.index == map.numRows - 1 -> map.frame[Direction.SOUTHBOUND]!!
                coordinate.column.index == map.numCols - 1 -> map.frame[Direction.EASTBOUND]!!
                else -> null
            }
        }

        class Lane(
            val street: Street,
            val enteringDirection: Direction,
            var trafficLight: TrafficLight? = null
        ) : Positionable, CarUsable {
            private val enteringCars: MutableSet<Car> = mutableSetOf()

            override fun toString(): String =
                "$enteringDirection [${trafficLight?.color}][${isBlocked()}] Lane@$street (${x()}, ${y()})"

            override fun directions() = listOf(enteringDirection)

            override fun x(): Double {
                return street.x() + when (enteringDirection) {
                    Direction.NORTHBOUND -> 1f
                    Direction.EASTBOUND -> 0f
                    Direction.WESTBOUND -> 1f
                    Direction.SOUTHBOUND -> 0f
                }
            }

            override fun y(): Double {
                return street.y() + when (enteringDirection) {
                    Direction.NORTHBOUND -> 1f
                    Direction.EASTBOUND -> 1f
                    Direction.WESTBOUND -> 0f
                    Direction.SOUTHBOUND -> 0f
                }
            }

            override var blockingCar: Car? = null

            override fun enteringCars(): Collection<Car> = enteringCars

            override fun addEnteringCar(car: Car) {
                enteringCars.add(car)
            }

            override fun removeEnteringCar(car: Car) {
                enteringCars.remove(car)
            }

            override fun isBlocked() = super.isBlocked() || (trafficLight != null && trafficLight!!.isRed())

        }
    }

    /**
     * A traffic light may block cars entering a specific lane of a city map street.
     */
    class TrafficLight(var color: LightColor) {

        fun isRed() = color === LightColor.RED
        fun isGreen() = color === LightColor.GREEN
        fun setRed() {
            color = LightColor.RED
        }

        fun setGreen() {
            color = LightColor.GREEN
        }

        enum class LightColor { RED, GREEN }
    }

}

/**
 * A creator for CityMaps.
 */
interface CityMapCreator {
    /**
     * Create a CityMap for given number of rows and number of columns.
     */
    fun create(numRows: Int, numColumns: Int): CityMap
}

/**
 * Creator of city maps with default streets.
 */
class DefaultCityMapCreator : CityMapCreator {
    override fun create(numRows: Int, numColumns: Int): CityMap {
        val result = CityMap(numRows, numColumns)
        if (numRows % 5 == 0 || numRows % 5 == 4) {
            result.createStreetsRows(2, 5)
        } else if (numRows % 5 == 3) {
            result.createStreetsRows(1, 5)
        } else if (numRows % 3 == 0) {
            result.createStreetsRows(1, 3)
        } else if (numRows % 7 == 0) {
            result.createStreetsRows(3, 7)
        } else {
            result.createStreetsRows(3, 5)
        }
        if (numColumns % 5 == 0 || numColumns % 5 == 4) {
            result.createStreetsCols(2, 5)
        } else if (numColumns % 5 == 3) {
            result.createStreetsCols(1, 5)
        } else if (numColumns % 3 == 0) {
            result.createStreetsCols(1, 3)
        } else if (numColumns % 7 == 0) {
            result.createStreetsCols(3, 7)
        } else {
            result.createStreetsCols(3, 5)
        }

        result.createDefaultTrafficLights(CityMap.TrafficLight.LightColor.RED)

        return result
    }
}

// Helpers for creating streets

private fun CityMap.placeStreet(coordinate: CityMap.Coordinate): CityMap.Street {
    val result = CityMap.Street(this, coordinate)
    streets[coordinate] = result
    return result
}

private fun CityMap.createStreetsRows(startRow: Int, rowDistance: Int) {
    for (rowIdx: Int in startRow until numRows step rowDistance) {
        createStreetRow(rowIdx)
    }
}

private fun CityMap.createStreetsCols(startCol: Int, colDistance: Int) {
    for (colIdx: Int in startCol until numCols step colDistance) {
        createStreetColumn(colIdx)
    }
}

private fun CityMap.createStreetRow(rowIdx: Int) {
    for (colIdx: Int in 0 until numCols) {
        placeStreet(CityMap.Coordinate(this.rows[rowIdx], this.columns[colIdx]))
    }
}

private fun CityMap.createStreetColumn(colIdx: Int) {
    for (rowIdx: Int in 0 until numRows) {
        placeStreet(CityMap.Coordinate(this.rows[rowIdx], this.columns[colIdx]))
    }
}

