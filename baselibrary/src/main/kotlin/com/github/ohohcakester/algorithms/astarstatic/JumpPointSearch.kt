package com.github.ohohcakester.algorithms.astarstatic

import com.github.ohohcakester.grid.GridGraph
import com.github.ohohcakester.priorityqueue.ReusableIndirectHeap

class JumpPointSearch(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) : AStarStaticMemory(graph, sx, sy, ex, ey) {
	companion object {
		fun postSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = postSmooth(graph, sx, sy, ex, ey, ::JumpPointSearch)
		fun repeatedPostSmooth(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = repeatedPostSmooth(graph, sx, sy, ex, ey, ::JumpPointSearch)
		fun dijkstra(graph: GridGraph, sx: Int, sy: Int, ex: Int, ey: Int) = dijkstra(graph, sx, sy, ex, ey, ::JumpPointSearch)
	}

	private val neighboursdX = IntArray(8)
	private val neighboursdY = IntArray(8)
	private var neighbourCount: Int = 0

	override fun computePath() {
		neighboursdX.fill(0)
		neighboursdY.fill(0)
		neighbourCount = 0

		val totalSize = (graph.sizeX + 1) * (graph.sizeY + 1)

		val start = toOneDimIndex(sx, sy)
		finish = toOneDimIndex(ex, ey)

		pq = ReusableIndirectHeap(totalSize)
		this.initialiseMemory(totalSize, java.lang.Float.POSITIVE_INFINITY, -1, false)

		initialise(start)

		while (!pq.isEmpty) {
			val current = pq.popMinIndex()
			if (current == finish || distance(current) == java.lang.Float.POSITIVE_INFINITY) {
				maybeSaveSearchSnapshot()
				break
			}
			setVisited(current, true)

			val x = toTwoDimX(current)
			val y = toTwoDimY(current)

			computeNeighbours(current, x, y) // stores neighbours in attribute.

			for (i in 0..neighbourCount - 1) {
				val dx = neighboursdX[i]
				val dy = neighboursdY[i]

				val successor = jump(x, y, dx, dy)
				if (successor != -1) {
					tryRelax(current, x, y, successor)
				}
			}

			maybeSaveSearchSnapshot()
		}

		maybePostSmooth()
	}

	private fun jump(x: Int, y: Int, dx: Int, dy: Int): Int {
		if (dx < 0) {
			if (dy < 0) {
				return jumpDL(x, y)
			} else if (dy > 0) {
				return jumpUL(x, y)
			} else {
				return jumpL(x, y)
			}
		} else if (dx > 0) {
			if (dy < 0) {
				return jumpDR(x, y)
			} else if (dy > 0) {
				return jumpUR(x, y)
			} else {
				return jumpR(x, y)
			}
		} else {
			if (dy < 0) {
				return jumpD(x, y)
			} else {
				return jumpU(x, y)
			}
		}

	}

	private fun jumpDL(x: Int, y: Int): Int {
		var x = x
		var y = y
		while (true) {
			x -= 1
			y -= 1
			if (graph.isBlocked(x, y)) return -1
			if (x == ex && y == ey) return toOneDimIndex(x, y)
			// diagonal cannot be forced on vertices.
			if (jumpL(x, y) != -1) return toOneDimIndex(x, y)
			if (jumpD(x, y) != -1) return toOneDimIndex(x, y)
		}
	}

	private fun jumpDR(x: Int, y: Int): Int {
		var x = x
		var y = y
		while (true) {
			x += 1
			y -= 1
			if (graph.isBlocked(x - 1, y)) return -1
			if (x == ex && y == ey) return toOneDimIndex(x, y)
			// diagonal cannot be forced on vertices.
			if (jumpD(x, y) != -1) return toOneDimIndex(x, y)
			if (jumpR(x, y) != -1) return toOneDimIndex(x, y)
		}
	}

	private fun jumpUL(x: Int, y: Int): Int {
		var x = x
		var y = y
		while (true) {
			x -= 1
			y += 1
			if (graph.isBlocked(x, y - 1)) return -1
			if (x == ex && y == ey) return toOneDimIndex(x, y)
			// diagonal cannot be forced on vertices.
			if (jumpL(x, y) != -1) return toOneDimIndex(x, y)
			if (jumpU(x, y) != -1) return toOneDimIndex(x, y)
		}
	}

	private fun jumpUR(x: Int, y: Int): Int {
		var x = x
		var y = y
		while (true) {
			x += 1
			y += 1
			if (graph.isBlocked(x - 1, y - 1)) return -1
			if (x == ex && y == ey) return toOneDimIndex(x, y)
			// diagonal cannot be forced on vertices.
			if (jumpU(x, y) != -1) return toOneDimIndex(x, y)
			if (jumpR(x, y) != -1) return toOneDimIndex(x, y)
		}
	}

	private fun jumpL(x: Int, y: Int): Int {
		var x = x
		while (true) {
			x -= 1
			if (graph.isBlocked(x, y)) {
				if (graph.isBlocked(x, y - 1)) {
					return -1
				} else {
					if (!graph.isBlocked(x - 1, y)) return toOneDimIndex(x, y)
				}
			}
			if (graph.isBlocked(x, y - 1)) {
				if (!graph.isBlocked(x - 1, y - 1)) return toOneDimIndex(x, y)
			}
			if (x == ex && y == ey) return toOneDimIndex(x, y)
		}
	}

	private fun jumpR(x: Int, y: Int): Int {
		var x = x
		while (true) {
			x += 1
			if (graph.isBlocked(x - 1, y)) {
				if (graph.isBlocked(x - 1, y - 1)) {
					return -1
				} else {
					if (!graph.isBlocked(x, y)) return toOneDimIndex(x, y)
				}
			}
			if (graph.isBlocked(x - 1, y - 1)) {
				if (!graph.isBlocked(x, y - 1)) return toOneDimIndex(x, y)
			}
			if (x == ex && y == ey) return toOneDimIndex(x, y)
		}
	}

	private fun jumpD(x: Int, y: Int): Int {
		var y = y
		while (true) {
			y -= 1
			if (graph.isBlocked(x, y)) {
				if (graph.isBlocked(x - 1, y)) {
					return -1
				} else {
					if (!graph.isBlocked(x, y - 1)) return toOneDimIndex(x, y)
				}
			}
			if (graph.isBlocked(x - 1, y)) {
				if (!graph.isBlocked(x - 1, y - 1)) return toOneDimIndex(x, y)
			}
			if (x == ex && y == ey) return toOneDimIndex(x, y)
		}
	}

	private fun jumpU(x: Int, y: Int): Int {
		var y = y
		while (true) {
			y += 1
			if (graph.isBlocked(x, y - 1)) {
				if (graph.isBlocked(x - 1, y - 1)) {
					return -1
				} else {
					if (!graph.isBlocked(x, y)) return toOneDimIndex(x, y)
				}
			}
			if (graph.isBlocked(x - 1, y - 1)) {
				if (!graph.isBlocked(x - 1, y)) return toOneDimIndex(x, y)
			}
			if (x == ex && y == ey) return toOneDimIndex(x, y)
		}
	}

	private fun computeNeighbours(currentIndex: Int, cx: Int, cy: Int) {
		neighbourCount = 0

		val parentIndex = parent(currentIndex)
		if (parentIndex == -1) {
			// is start node.
			for (y in -1..1) {
				for (x in -1..1) {
					if (x == 0 && y == 0) continue
					val px = cx + x
					val py = cy + y
					if (graph.neighbourLineOfSight(cx, cy, px, py)) {
						addNeighbour(x, y)
					}
				}
			}
			return
		}

		val dirX = cx - this.toTwoDimX(parentIndex)
		val dirY = cy - this.toTwoDimY(parentIndex)

		if (dirX < 0) {
			if (dirY < 0) {
				// down-left
				if (!graph.isBlocked(cx - 1, cy - 1)) {
					addNeighbour(-1, -1)
					addNeighbour(-1, 0)
					addNeighbour(0, -1)
				} else {
					if (!graph.isBlocked(cx - 1, cy)) addNeighbour(-1, 0)
					if (!graph.isBlocked(cx, cy - 1)) addNeighbour(0, -1)
				}
			} else if (dirY > 0) {
				// up-left
				if (!graph.isBlocked(cx - 1, cy)) {
					addNeighbour(-1, 1)
					addNeighbour(-1, 0)
					addNeighbour(0, 1)
				} else {
					if (!graph.isBlocked(cx - 1, cy - 1)) addNeighbour(-1, 0)
					if (!graph.isBlocked(cx, cy)) addNeighbour(0, 1)
				}
			} else {
				// left
				if (graph.isBlocked(cx, cy)) {
					if (!graph.isBlocked(cx - 1, cy)) {
						addNeighbour(-1, 1)
						addNeighbour(0, 1)
						addNeighbour(-1, 0)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else if (graph.isBlocked(cx, cy - 1)) {
					if (!graph.isBlocked(cx - 1, cy - 1)) {
						addNeighbour(-1, -1)
						addNeighbour(0, -1)
						addNeighbour(-1, 0)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else {
					throw UnsupportedOperationException("wrong")
				}
			}
		} else if (dirX > 0) {
			if (dirY < 0) {
				// down-right
				if (!graph.isBlocked(cx, cy - 1)) {
					addNeighbour(1, -1)
					addNeighbour(1, 0)
					addNeighbour(0, -1)
				} else {
					if (!graph.isBlocked(cx, cy)) addNeighbour(1, 0)
					if (!graph.isBlocked(cx - 1, cy - 1)) addNeighbour(0, -1)
				}
			} else if (dirY > 0) {
				// up-right
				if (!graph.isBlocked(cx, cy)) {
					addNeighbour(1, 1)
					addNeighbour(1, 0)
					addNeighbour(0, 1)
				} else {
					if (!graph.isBlocked(cx, cy - 1)) addNeighbour(1, 0)
					if (!graph.isBlocked(cx - 1, cy)) addNeighbour(0, 1)
				}
			} else {
				// right
				if (graph.isBlocked(cx - 1, cy)) {
					if (!graph.isBlocked(cx, cy)) {
						addNeighbour(1, 1)
						addNeighbour(0, 1)
						addNeighbour(1, 0)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else if (graph.isBlocked(cx - 1, cy - 1)) {
					if (!graph.isBlocked(cx, cy - 1)) {
						addNeighbour(1, -1)
						addNeighbour(0, -1)
						addNeighbour(1, 0)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else {
					throw UnsupportedOperationException("wrong")
				}
			}
		} else {
			if (dirY < 0) {
				// down
				if (graph.isBlocked(cx, cy)) {
					if (!graph.isBlocked(cx, cy - 1)) {
						addNeighbour(1, -1)
						addNeighbour(1, 0)
						addNeighbour(0, -1)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else if (graph.isBlocked(cx - 1, cy)) {
					if (!graph.isBlocked(cx - 1, cy - 1)) {
						addNeighbour(-1, -1)
						addNeighbour(-1, 0)
						addNeighbour(0, -1)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else {
					throw UnsupportedOperationException("wrong")
				}
			} else { //dirY > 0
				// up
				if (graph.isBlocked(cx, cy - 1)) {
					if (!graph.isBlocked(cx, cy)) {
						addNeighbour(1, 1)
						addNeighbour(1, 0)
						addNeighbour(0, 1)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else if (graph.isBlocked(cx - 1, cy - 1)) {
					if (!graph.isBlocked(cx - 1, cy)) {
						addNeighbour(-1, 1)
						addNeighbour(-1, 0)
						addNeighbour(0, 1)
					} else {
						throw UnsupportedOperationException("wrong")
					}
				} else {
					throw UnsupportedOperationException("wrong")
				}
			}
		}
	}

	private fun addNeighbour(x: Int, y: Int) {
		neighboursdX[neighbourCount] = x
		neighboursdY[neighbourCount] = y
		neighbourCount++
	}

	override fun heuristic(x: Int, y: Int): Float {
		return graph.octileDistance(x, y, ex, ey)
	}

	private fun tryRelax(current: Int, currX: Int, currY: Int, destination: Int) {
		if (visited(destination)) return

		val destX = toTwoDimX(destination)
		val destY = toTwoDimY(destination)

		if (relax(current, destination, graph.octileDistance(currX, currY, destX, destY))) {
			// If relaxation is done.
			pq.decreaseKey(destination, distance(destination) + heuristic(destX, destY))
		}
	}
}
