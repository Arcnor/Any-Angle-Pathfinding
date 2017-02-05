package com.github.ohohcakester.priorityqueue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndirectHeapTest {
	private companion object {
		fun assertFloatEquals(expected: Float, actual: Float) {
			assertTrue(Math.abs(expected - actual) < 0.001f)
		}
	}

	@Test
	fun test() {
		val pq = FastVariableSizeIndirectHeap(7)

		val indexes = IntArray(239)
		(0..60).forEach { i -> indexes[i] = pq.insert((i * 73 % 239).toFloat()) }

		pq.reserve(200)

		(61..238).forEach { i -> indexes[i] = pq.insert((i * 73 % 239).toFloat()) }

		pq.reserve(400)

		assertEquals(239, pq.size().toLong())

		pq.reserve(1000)

		assertEquals(239, pq.size().toLong())

		for (i in 0..99) {
			assertEquals(i.toLong(), pq.minValue.toInt().toLong())
			assertEquals(i.toLong(), (pq.popMinIndex() * 73 % 239).toLong())
		}

		assertEquals(true, pq.isNotEmpty())
		assertEquals(139, pq.size().toLong())

		val newHandles = IntArray(139)
		var currIndex = 0

		for (i in 0..238) {
			if (i * 73 % 239 >= 100) {
				pq.decreaseKey(indexes[i], (-1 - currIndex).toFloat())
				newHandles[currIndex++] = indexes[i]
			}
		}

		for (i in 0..38) {
			assertEquals((i - 139).toLong(), pq.minValue.toInt().toLong())
			assertEquals(newHandles[139 - i - 1].toLong(), pq.popMinIndex().toLong())
		}

		// Now we only have newHandles[0:100] in the pq, with values -1 to -100 respectively.
		// Next we insert values from -0.5 to -100.5 (101 values). newerHandles[0] is -0.5.

		val newerHandles = IntArray(101)
		for (i in 0..100) {
			newerHandles[i] = pq.insert(-i - 0.5f)
		}


		assertEquals(201, pq.size().toLong())

		// Now we pop everything.
		for (i in 0..99) {
			assertFloatEquals(-100.5f + i, pq.minValue)
			assertEquals(newerHandles[100 - i].toLong(), pq.popMinIndex().toLong())

			assertEquals((200 - 2 * i).toLong(), pq.size().toLong())

			assertFloatEquals(-100f + i, pq.minValue)
			assertEquals(newHandles[99 - i].toLong(), pq.popMinIndex().toLong())

			assertEquals((199 - 2 * i).toLong(), pq.size().toLong())
		}

		assertFloatEquals(-0.5f, pq.minValue)
		pq.decreaseKey(newerHandles[0], -1000f)
		assertFloatEquals(-1000f, pq.minValue)
		assertEquals(newerHandles[0].toLong(), pq.popMinIndex().toLong())

		assertEquals(0, pq.size().toLong())
	}
}