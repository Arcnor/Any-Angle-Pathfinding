@file:Suppress("NOTHING_TO_INLINE")

package com.github.ohohcakester.grid

import com.github.ohohcakester.datatypes.Point

/**
 * Represents the Grid of blocked/unblocked tiles.
 */
class GridGraph(val sizeX: Int, val sizeY: Int) {
	private companion object {
		val SQRT_TWO = Math.sqrt(2.0).toFloat()
		val SQRT_TWO_DOUBLE = Math.sqrt(2.0)
		val SQRT_TWO_MINUS_ONE = (Math.sqrt(2.0) - 1).toFloat()
	}

	private val sizeXplusOne: Int
	private val tiles = Array(sizeY) { BooleanArray(sizeX) }

	init {
		this.sizeXplusOne = sizeX + 1
	}

	fun setBlocked(x: Int, y: Int, value: Boolean) {
		tiles[y][x] = value
	}

	fun trySetBlocked(x: Int, y: Int, value: Boolean) {
		if (isValidBlock(x, y))
			tiles[y][x] = value
	}

	fun isBlocked(x: Int, y: Int) = when {
		x >= sizeX || y >= sizeY -> true
		x < 0 || y < 0 -> true
		else -> tiles[y][x]
	}

	fun isValidCoordinate(x: Int, y: Int) = x <= sizeX && y <= sizeY && x >= 0 && y >= 0

	fun isValidBlock(x: Int, y: Int) = x < sizeX && y < sizeY && x >= 0 && y >= 0

	fun toOneDimIndex(x: Int, y: Int) = y * sizeXplusOne + x

	fun toTwoDimX(index: Int) = index % sizeXplusOne

	fun toTwoDimY(index: Int) = index / sizeXplusOne

	fun isUnblockedCoordinate(x: Int, y: Int) = !topRightOfBlockedTile(x, y) ||
			!topLeftOfBlockedTile(x, y) ||
			!bottomRightOfBlockedTile(x, y) ||
			!bottomLeftOfBlockedTile(x, y)

	inline fun topRightOfBlockedTile(x: Int, y: Int) = isBlocked(x - 1, y - 1)

	inline fun topLeftOfBlockedTile(x: Int, y: Int) = isBlocked(x, y - 1)

	inline fun bottomRightOfBlockedTile(x: Int, y: Int) = isBlocked(x - 1, y)

	inline fun bottomLeftOfBlockedTile(x: Int, y: Int) = isBlocked(x, y)

	/**
	 * x1,y1,x2,y2 refer to the top left corner of the tile.

	 * @param x1 Condition: x1 between 0 and sizeX inclusive.
	 * *
	 * @param y1 Condition: y1 between 0 and sizeY inclusive.
	 * *
	 * @param x2 Condition: x2 between 0 and sizeX inclusive.
	 * *
	 * @param y2 Condition: y2 between 0 and sizeY inclusive.
	 * *
	 * @return distance.
	 */
	fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
		val xDiff = x2 - x1
		val yDiff = y2 - y1

		return when {
			xDiff == 0 -> Math.abs(yDiff).toFloat()
			yDiff == 0 -> Math.abs(xDiff).toFloat()
			xDiff == yDiff || xDiff == -yDiff -> SQRT_TWO * Math.abs(xDiff)
			else -> {
				val squareDistance = xDiff * xDiff + yDiff * yDiff

				Math.sqrt(squareDistance.toDouble()).toFloat()
			}
		}

	}

	fun distance_double(x1: Int, y1: Int, x2: Int, y2: Int): Double {
		val xDiff = x2 - x1
		val yDiff = y2 - y1

		return when {
			xDiff == 0 -> Math.abs(yDiff).toDouble()
			yDiff == 0 -> Math.abs(xDiff).toDouble()
			xDiff == yDiff || xDiff == -yDiff -> SQRT_TWO_DOUBLE * Math.abs(xDiff)
			else -> {
				val squareDistance = xDiff * xDiff + yDiff * yDiff

				Math.sqrt(squareDistance.toDouble())
			}
		}

	}

	/**
	 * Octile distance:
	 * min(dx,dy)*sqrt(2) + (max(dx,dy)-min(dx,dy))
	 * = min(dx,dy)*(sqrt(2)-1) + max(dx,dy)
	 */
	fun octileDistance(x1: Int, y1: Int, x2: Int, y2: Int): Float {
		var dx = x1 - x2
		var dy = y1 - y2
		if (dx < 0) dx = -dx
		if (dy < 0) dy = -dy

		var min = dx
		var max = dy
		if (dy < dx) {
			min = dy
			max = dx
		}

		return min * SQRT_TWO_MINUS_ONE + max
	}

	/**
	 * Same as lineOfSight, but only works with a vertex and its 8 immediate neighbours.
	 * Also (x1,y1) != (x2,y2)
	 */
	fun neighbourLineOfSight(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
		return when {
			x1 == x2 -> when {
				y1 > y2 -> !isBlocked(x1, y2) || !isBlocked(x1 - 1, y2)
				else -> !isBlocked(x1, y1) || !isBlocked(x1 - 1, y1)
			}
			x1 < x2 -> when {
				y1 == y2 -> !isBlocked(x1, y1) || !isBlocked(x1, y1 - 1)
				y1 < y2 -> !isBlocked(x1, y1)
				else -> !isBlocked(x1, y2)
			}
			else -> when {
				y1 == y2 -> !isBlocked(x2, y1) || !isBlocked(x2, y1 - 1)
				y1 < y2 -> !isBlocked(x2, y1)
				else -> !isBlocked(x2, y2)
			}
		}
	}


	/**
	 * @return true iff there is line-of-sight from (x1,y1) to (x2,y2).
	 */
	fun lineOfSight(x1: Int, y1: Int, x2: Int, y2: Int): Boolean {
		var x1 = x1
		var y1 = y1
		var dy = y2 - y1
		var dx = x2 - x1

		var f = 0

		var signY = 1
		var signX = 1
		var offsetX = 0
		var offsetY = 0

		if (dy < 0) {
			dy *= -1
			signY = -1
			offsetY = -1
		}
		if (dx < 0) {
			dx *= -1
			signX = -1
			offsetX = -1
		}

		if (dx >= dy) {
			while (x1 != x2) {
				f += dy
				if (f >= dx) {
					if (isBlocked(x1 + offsetX, y1 + offsetY))
						return false
					y1 += signY
					f -= dx
				}
				when {
					f != 0 && isBlocked(x1 + offsetX, y1 + offsetY) -> return false
					dy == 0 && isBlocked(x1 + offsetX, y1) && isBlocked(x1 + offsetX, y1 - 1) -> return false
					else -> x1 += signX
				}
			}
		} else {
			while (y1 != y2) {
				f += dx
				if (f >= dy) {
					if (isBlocked(x1 + offsetX, y1 + offsetY))
						return false
					x1 += signX
					f -= dy
				}
				when {
					f != 0 && isBlocked(x1 + offsetX, y1 + offsetY) -> return false
					dx == 0 && isBlocked(x1, y1 + offsetY) && isBlocked(x1 - 1, y1 + offsetY) -> return false
					else -> y1 += signY
				}
			}
		}
		return true
	}

	fun findFirstBlockedTile(x1: Int, y1: Int, dx: Int, dy: Int): Point {
		var x1 = x1
		var y1 = y1
		var dx = dx
		var dy = dy

		var f = 0

		var signY = 1
		var signX = 1
		var offsetX = 0
		var offsetY = 0

		if (dy < 0) {
			dy *= -1
			signY = -1
			offsetY = -1
		}
		if (dx < 0) {
			dx *= -1
			signX = -1
			offsetX = -1
		}

		if (dx >= dy) {
			while (true) {
				f += dy
				if (f >= dx) {
					if (isBlocked(x1 + offsetX, y1 + offsetY))
						return Point(x1 + offsetX, y1 + offsetY)
					y1 += signY
					f -= dx
				}
				if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
					return Point(x1 + offsetX, y1 + offsetY)
				if (dy == 0 && isBlocked(x1 + offsetX, y1) && isBlocked(x1 + offsetX, y1 - 1))
					return Point(x1 + offsetX, -1)

				x1 += signX
			}
		} else {
			while (true) {
				f += dx
				if (f >= dy) {
					if (isBlocked(x1 + offsetX, y1 + offsetY))
						return Point(x1 + offsetX, y1 + offsetY)
					x1 += signX
					f -= dy
				}
				if (f != 0 && isBlocked(x1 + offsetX, y1 + offsetY))
					return Point(x1 + offsetX, y1 + offsetY)
				if (dx == 0 && isBlocked(x1, y1 + offsetY) && isBlocked(x1 - 1, y1 + offsetY))
					return Point(-1, y1 + offsetY)

				y1 += signY
			}
		}
		//return null;
	}


	/**
	 * Used by Accelerated A* and MazeAnalysis.
	 * leftRange is the number of blocks you can move left before hitting a blocked tile.
	 * downRange is the number of blocks you can move down before hitting a blocked tile.
	 * For blocked tiles, leftRange, downRange are both -1.
	 *
	 *
	 * How to use the maxRange property:
	 *
	 *
	 * x,y is the starting point.
	 * k is the number of tiles diagonally up-right of the starting point.
	 * int i = x-y+sizeY;
	 * int j = Math.min(x, y);
	 * return maxRange[i][j + k] - k;
	 */
	fun computeMaxDownLeftRanges(): Array<IntArray> {
		val downRange = Array(sizeY + 1) { IntArray(sizeX + 1) }
		val leftRange = Array(sizeY + 1) { IntArray(sizeX + 1) }

		for (y in 0..sizeY - 1) {
			leftRange[y][0] = if (isBlocked(0, y)) -1 else 0

			for (x in 1..sizeX - 1) {
				leftRange[y][x] = if (isBlocked(x, y)) -1 else leftRange[y][x - 1] + 1
			}
		}

		for (x in 0..sizeX - 1) {
			downRange[0][x] = if (isBlocked(x, 0)) -1 else 0

			for (y in 1..sizeY - 1) {
				downRange[y][x] = if (isBlocked(x, y)) -1 else downRange[y - 1][x] + 1
			}
		}

		for (x in 0..sizeX + 1 - 1) {
			downRange[sizeY][x] = -1
			leftRange[sizeY][x] = -1
		}

		for (y in 0..sizeY - 1) {
			downRange[y][sizeX] = -1
			leftRange[y][sizeX] = -1
		}

		val maxSize = Math.min(sizeX, sizeY) + 1
		val size = sizeX + sizeY + 1
		val maxRanges = Array(size) { i ->
			val currSize = Math.min(maxSize, Math.min(i + 1, size - i))

			var currX = i - sizeY
			if (currX < 0) currX = 0
			var currY = currX - i + sizeY

			IntArray(currSize) {
				Math.min(downRange[currY][currX], leftRange[currY++][currX++])
			}
		}
		return maxRanges
	}

	/**
	 * @return the percentage of blocked tiles as compared to the total grid size.
	 */
	val percentageBlocked: Float
		get() = numBlocked.toFloat() / (sizeX * sizeY)

	/**
	 * @return the number of blocked tiles in the grid.
	 */
	val numBlocked: Int
		get() {
			var nBlocked = 0
			for (y in 0..sizeY - 1) {
				for (x in 0..sizeX - 1) {
					if (isBlocked(x, y)) {
						nBlocked++
					}
				}
			}
			return nBlocked
		}


	/**
	 * Checks whether the path (x1,y1),(x2,y2),(x3,y3) is taut.
	 */
	fun isTaut(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int): Boolean {
		if (x1 < x2) {
			if (y1 < y2) {
				return isTautFromBottomLeft(x1, y1, x2, y2, x3, y3)
			} else if (y2 < y1) {
				return isTautFromTopLeft(x1, y1, x2, y2, x3, y3)
			} else { // y1 == y2
				return isTautFromLeft(x1, y1, x2, y2, x3, y3)
			}
		} else if (x2 < x1) {
			if (y1 < y2) {
				return isTautFromBottomRight(x1, y1, x2, y2, x3, y3)
			} else if (y2 < y1) {
				return isTautFromTopRight(x1, y1, x2, y2, x3, y3)
			} else { // y1 == y2
				return isTautFromRight(x1, y1, x2, y2, x3, y3)
			}
		} else { // x2 == x1
			if (y1 < y2) {
				return isTautFromBottom(x1, y1, x2, y2, x3, y3)
			} else if (y2 < y1) {
				return isTautFromTop(x1, y1, x2, y2, x3, y3)
			} else { // y1 == y2
				throw UnsupportedOperationException("v == u?")
			}
		}
	}


	private fun isTautFromBottomLeft(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                                 y3: Int): Boolean {
		if (x3 < x2 || y3 < y2) return false

		val compareGradients = (y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1) // m1 - m2
		if (compareGradients < 0) { // m1 < m2
			return bottomRightOfBlockedTile(x2, y2)
		} else if (compareGradients > 0) { // m1 > m2
			return topLeftOfBlockedTile(x2, y2)
		} else { // m1 == m2
			return true
		}
	}


	private fun isTautFromTopLeft(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                              y3: Int): Boolean {
		if (x3 < x2 || y3 > y2) return false

		val compareGradients = (y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1) // m1 - m2
		if (compareGradients < 0) { // m1 < m2
			return bottomLeftOfBlockedTile(x2, y2)
		} else if (compareGradients > 0) { // m1 > m2
			return topRightOfBlockedTile(x2, y2)
		} else { // m1 == m2
			return true
		}
	}

	private fun isTautFromBottomRight(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                                  y3: Int): Boolean {
		if (x3 > x2 || y3 < y2) return false
		val compareGradients = (y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1) // m1 - m2
		if (compareGradients < 0) { // m1 < m2
			return topRightOfBlockedTile(x2, y2)
		} else if (compareGradients > 0) { // m1 > m2
			return bottomLeftOfBlockedTile(x2, y2)
		} else { // m1 == m2
			return true
		}
	}


	private fun isTautFromTopRight(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                               y3: Int): Boolean {
		if (x3 > x2 || y3 > y2) return false

		val compareGradients = (y2 - y1) * (x3 - x2) - (y3 - y2) * (x2 - x1) // m1 - m2
		if (compareGradients < 0) { // m1 < m2
			return topLeftOfBlockedTile(x2, y2)
		} else if (compareGradients > 0) { // m1 > m2
			return bottomRightOfBlockedTile(x2, y2)
		} else { // m1 == m2
			return true
		}
	}


	private fun isTautFromLeft(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                           y3: Int): Boolean {
		if (x3 < x2) return false

		val dy = y3 - y2
		if (dy < 0) { // y3 < y2
			return topRightOfBlockedTile(x2, y2)
		} else if (dy > 0) { // y3 > y2
			return bottomRightOfBlockedTile(x2, y2)
		} else { // y3 == y2
			return true
		}
	}

	private fun isTautFromRight(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                            y3: Int): Boolean {
		if (x3 > x2) return false

		val dy = y3 - y2
		if (dy < 0) { // y3 < y2
			return topLeftOfBlockedTile(x2, y2)
		} else if (dy > 0) { // y3 > y2
			return bottomLeftOfBlockedTile(x2, y2)
		} else { // y3 == y2
			return true
		}
	}

	private fun isTautFromBottom(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                             y3: Int): Boolean {
		if (y3 < y2) return false

		val dx = x3 - x2
		if (dx < 0) { // x3 < x2
			return topRightOfBlockedTile(x2, y2)
		} else if (dx > 0) { // x3 > x2
			return topLeftOfBlockedTile(x2, y2)
		} else { // x3 == x2
			return true
		}
	}

	private fun isTautFromTop(x1: Int, y1: Int, x2: Int, y2: Int, x3: Int,
	                          y3: Int): Boolean {
		if (y3 > y2) return false

		val dx = x3 - x2
		if (dx < 0) { // x3 < x2
			return bottomRightOfBlockedTile(x2, y2)
		} else if (dx > 0) { // x3 > x2
			return bottomLeftOfBlockedTile(x2, y2)
		} else { // x3 == x2
			return true
		}
	}
}
