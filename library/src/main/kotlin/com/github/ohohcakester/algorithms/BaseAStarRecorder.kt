package com.github.ohohcakester.algorithms

import com.github.ohohcakester.datatypes.SnapshotItem
import java.awt.Color

class BaseAStarRecorder(val algorithm: BaseAStar) : PathFindingRecorder() {
	override fun computeSearchSnapshot(): List<SnapshotItem> {
		val list = java.util.ArrayList<SnapshotItem>()
		var current = algorithm.goalParentIndex()
		var finalPathSet: MutableSet<Int>? = null
		if (algorithm.getParent(current) >= 0) {
			finalPathSet = java.util.HashSet<Int>()
			while (current != -1) {
				finalPathSet.add(current)
				current = algorithm.getParent(current)
			}
		}

		val size = algorithm.parentSize
		for (i in 0..size - 1) {
			if (algorithm.getParent(i) != -1) {
				if (finalPathSet != null && finalPathSet.contains(i)) {
					list.add(SnapshotItem.generate(algorithm.snapshotEdge(i), Color.BLUE))
				} else {
					list.add(SnapshotItem.generate(algorithm.snapshotEdge(i)))
				}
			}
			val vertexSnapshot = algorithm.snapshotVertex(i)
			if (vertexSnapshot != null) {
				list.add(SnapshotItem.generate(vertexSnapshot))
			}
		}

		return list
	}
}