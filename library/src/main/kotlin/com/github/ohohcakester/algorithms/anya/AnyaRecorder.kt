package com.github.ohohcakester.algorithms.anya

import com.github.ohohcakester.algorithms.PathFindingRecorder
import com.github.ohohcakester.datatypes.SnapshotItem

class AnyaRecorder<P>(val algorithm: Anya<P>) : PathFindingRecorder() {
	override fun computeSearchSnapshot(): List<SnapshotItem> {
		val states = algorithm.states
		val list = java.util.ArrayList<SnapshotItem>(states.size)

		for (state in states) {
			// y, xLn, xLd, xRn, xRd, px, py
			if (state == null) continue

			val line = intArrayOf(
					state.y,
					state.xL.n,
					state.xL.d,
					state.xR.n,
					state.xR.d,
					state.basePoint.x,
					state.basePoint.y
			)
			list.add(SnapshotItem.generate(line))
		}

		val pq = algorithm.pq!!
		if (pq.isNotEmpty()) {
			val index = pq.minIndex
			val state = states[index]!!

			val line = intArrayOf(
					state.y,
					state.xL.n,
					state.xL.d,
					state.xR.n,
					state.xR.d
			)
			list.add(SnapshotItem.generate(line))
		}

		return list
	}
}

