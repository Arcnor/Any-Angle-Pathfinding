package com.github.ohohcakester.datatypes

object Memory {
	private lateinit var distance: FloatArray
	private lateinit var parent: IntArray
	private lateinit var visited: BooleanArray

	private var defaultDistance = 0f
	private var defaultParent = -1
	private var defaultVisited = false

	private var ticketCheck: IntArray? = null
	private var ticketNumber = 0

	private var size = 0

	fun initialise(size: Int, defaultDistance: Float, defaultParent: Int, defaultVisited: Boolean): Int {
		Memory.defaultDistance = defaultDistance
		Memory.defaultParent = defaultParent
		Memory.defaultVisited = defaultVisited
		Memory.size = size

		if (ticketCheck == null || ticketCheck!!.size != size) {
			distance = FloatArray(size)
			parent = IntArray(size)
			visited = BooleanArray(size)
			ticketCheck = IntArray(size)
			ticketNumber = 1
		} else if (ticketNumber == -1) {
			ticketCheck = IntArray(size)
			ticketNumber = 1
		} else {
			ticketNumber++
		}

		return ticketNumber
	}

	fun currentTicket() = ticketNumber

	fun size() = size

	fun distance(index: Int): Float {
		if (ticketCheck!![index] != ticketNumber) return defaultDistance
		return distance[index]
	}

	fun parent(index: Int): Int {
		if (ticketCheck!![index] != ticketNumber) return defaultParent
		return parent[index]
	}

	fun visited(index: Int): Boolean {
		if (ticketCheck!![index] != ticketNumber) return defaultVisited
		return visited[index]
	}

	fun setDistance(index: Int, value: Float) {
		if (ticketCheck!![index] != ticketNumber) {
			distance[index] = value
			parent[index] = defaultParent
			visited[index] = defaultVisited
			ticketCheck!![index] = ticketNumber
		} else {
			distance[index] = value
		}
	}

	fun setParent(index: Int, value: Int) {
		if (ticketCheck!![index] != ticketNumber) {
			distance[index] = defaultDistance
			parent[index] = value
			visited[index] = defaultVisited
			ticketCheck!![index] = ticketNumber
		} else {
			parent[index] = value
		}
	}

	fun setVisited(index: Int, value: Boolean) {
		if (ticketCheck!![index] != ticketNumber) {
			distance[index] = defaultDistance
			parent[index] = defaultParent
			visited[index] = value
			ticketCheck!![index] = ticketNumber
		} else {
			visited[index] = value
		}
	}
}
