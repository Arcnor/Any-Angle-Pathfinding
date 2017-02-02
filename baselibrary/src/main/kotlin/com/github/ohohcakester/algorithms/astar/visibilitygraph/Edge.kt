package com.github.ohohcakester.algorithms.astar.visibilitygraph

class Edge(val source: Int, val dest: Int, val weight: Float) {
	override fun hashCode(): Int {
		val prime = 31
		var result = 1
		result = prime * result + dest
		return result
	}

	/**
	 * Depends only on destination index.
	 */
	override fun equals(other: Any?): Boolean {
		if (this === other)
			return true
		if (other == null)
			return false
		if (javaClass != other.javaClass)
			return false
		other as Edge?
		if (dest != other.dest)
			return false
		return true
	}
}
