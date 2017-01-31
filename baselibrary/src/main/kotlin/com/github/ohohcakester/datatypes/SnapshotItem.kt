package com.github.ohohcakester.datatypes

import java.awt.Color
import java.util.Arrays
import java.util.HashMap

/**
 * Contains a [x1,y1,x2,y2] or [x,y] and a colour.
 * Refer to GridObjects.java for how the path array works.
 */
class SnapshotItem private constructor(val path: Array<Int>, val color: Color?) {
	companion object {
		private var cached: HashMap<SnapshotItem, SnapshotItem>? = null

		fun generate(path: Array<Int>, color: Color): SnapshotItem {
			return getCached(SnapshotItem(path, color))
		}

		fun generate(path: Array<Int>): SnapshotItem {
			return getCached(SnapshotItem(path, null))
		}

		fun clearCached() {
			cached!!.clear()
			cached = null
		}

		private fun getCached(item: SnapshotItem): SnapshotItem {
			if (cached == null) {
				cached = HashMap<SnapshotItem, SnapshotItem>()
			}
			val get = cached!![item]
			if (get == null) {
				cached!!.put(item, item)
				return item
			} else {
				return get
			}
		}
	}

	override fun hashCode(): Int {
		val prime = 31
		var result = 1
		result = prime * result + (color?.hashCode() ?: 0)
		result = prime * result + path[0]
		result = prime * result + Arrays.hashCode(path)
		return result
	}

	override fun equals(obj: Any?): Boolean {
		if (this === obj)
			return true
		if (obj == null)
			return false
		if (javaClass != obj.javaClass)
			return false
		val other = obj as SnapshotItem?
		if (color == null) {
			if (other!!.color != null)
				return false
		} else if (color != other!!.color)
			return false
		if (!Arrays.equals(path, other.path))
			return false
		return true
	}
}
