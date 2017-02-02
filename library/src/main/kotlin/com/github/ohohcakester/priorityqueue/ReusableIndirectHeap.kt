package com.github.ohohcakester.priorityqueue


/**
 * Indirect binary heap. Used for O(lgn) deleteMin and O(lgn) decreaseKey.
 * Runtime: O(1)
 */
class ReusableIndirectHeap(private var heapSize: Int) {
	init {
		initialise(heapSize, java.lang.Float.POSITIVE_INFINITY)
	}

	private fun bubbleUp(index: Int) {
		if (index == 0)
		// Reached root
			return

		val parent = (index - 1) / 2
		if (getKey(index) < getKey(parent)) {
			// If meets the conditions to bubble up,
			swapData(index, parent)
			bubbleUp(parent)
		}
	}

	private fun swapData(a: Int, b: Int) {
		// s = Data at a = out[a]
		// t = Data at b = out[b]
		// key[a] <-> key[b]
		// in[s] <-> in[t]
		// out[a] <-> out[b]

		val s = getOut(a)
		val t = getOut(b)

		swapKey(a, b)
		swapIn(s, t)
		swapOut(a, b)
	}

	/**
	 * swap integers in list
	 */
	private fun swapKey(i1: Int, i2: Int) {
		val temp = getKey(i1)
		setKey(i1, getKey(i2))
		setKey(i2, temp)
	}

	/**
	 * swap integers in list
	 */
	private fun swapOut(i1: Int, i2: Int) {
		val temp = getOut(i1)
		setOut(i1, getOut(i2))
		setOut(i2, temp)
	}

	/**
	 * swap integers in list
	 */
	private fun swapIn(i1: Int, i2: Int) {
		val temp = getIn(i1)
		setIn(i1, getIn(i2))
		setIn(i2, temp)
	}

	private fun smallerNode(index1: Int, index2: Int): Int {
		if (index1 >= heapSize) {
			if (index2 >= heapSize)
				return -1

			return index2
		}
		if (index2 >= heapSize)
			return index1

		return if (getKey(index1) < getKey(index2)) index1 else index2
	}

	private fun bubbleDown(index: Int) {
		val leftChild = leftChild(index)
		val rightChild = rightChild(index)

		val smallerChild = smallerNode(leftChild, rightChild)
		if (smallerChild == -1) return

		if (getKey(index) > getKey(smallerChild)) {
			// If meets the conditions to bubble down,
			swapData(index, smallerChild)
			bubbleDown(smallerChild)
		}
	}

	/**
	 * Runtime: O(lgn)
	 */
	fun decreaseKey(outIndex: Int, newKey: Float) {
		// Assume newKey < old key
		//System.out.println(keyList);
		//System.out.println(inList);
		//System.out.println(outList);
		val inIndex = getIn(outIndex)
		setKey(inIndex, newKey)
		bubbleUp(inIndex)
	}

	val minValue: Float
		get() = getKey(0)

	/**
	 * Runtime: O(lgn)

	 * @return index of min element
	 */
	fun popMinIndex(): Int {
		if (heapSize == 0)
			throw NullPointerException("Indirect Heap is empty!")
		else if (heapSize == 1) {
			val s = getOut(0)
			setIn(s, -1)
			heapSize--
			return getOut(0)
		}
		// nodeList.size() > 1

		// s = Data at 0 = out[0]
		// t = Data at lastIndex = out[lastIndex]
		// key[0] = key[lastIndex], remove key[lastIndex]
		// in[s] = -1
		// in[t] = 0
		// out[0] = out[lastIndex], remove out[lastIndex]

		//E temp = keyList.get(0);
		val lastIndex = heapSize - 1

		val s = getOut(0)
		val t = getOut(lastIndex)

		setKey(0, getKey(lastIndex))
		setIn(s, -1)
		setIn(t, 0)
		setOut(0, getOut(lastIndex))

		heapSize--

		bubbleDown(0)

		return s
	}

	fun arrayToString(): String {
		val sb = StringBuilder()
		for (i in 0..heapSize - 1) {
			sb.append(i)
			sb.append(" ")
			sb.append(getKey(i))
			sb.append("\n")
		}
		return sb.toString()
	}

	val isEmpty: Boolean
		get() = heapSize <= 0

	// FIXME: This looks A LOT like `Memory`. Refactor?
	companion object {
		var ticketCheck: IntArray? = null
		var ticketNumber = 0
		private lateinit var keyList: FloatArray
		private lateinit var inList: IntArray
		private lateinit var outList: IntArray
		private var defaultKey = java.lang.Float.POSITIVE_INFINITY

		fun initialise(size: Int, defaultKey: Float) {
			ReusableIndirectHeap.defaultKey = defaultKey

			if (ticketCheck == null || ticketCheck!!.size != size) {
				keyList = FloatArray(size)
				inList = IntArray(size)
				outList = IntArray(size)
				ticketCheck = IntArray(size)
				ticketNumber = 1
			} else if (ticketNumber == -1) {
				ticketCheck = IntArray(size)
				ticketNumber = 1
			} else {
				ticketNumber++
			}
		}

		fun getKey(index: Int): Float {
			if (ticketCheck!![index] != ticketNumber) return defaultKey
			return keyList[index]
		}

		fun getIn(index: Int): Int {
			if (ticketCheck!![index] != ticketNumber) return index
			return inList[index]
		}

		fun getOut(index: Int): Int {
			if (ticketCheck!![index] != ticketNumber) return index
			return outList[index]
		}

		fun setKey(index: Int, value: Float) {
			if (ticketCheck!![index] != ticketNumber) {
				keyList[index] = value
				inList[index] = index
				outList[index] = index
				ticketCheck!![index] = ticketNumber
			} else {
				keyList[index] = value
			}
		}

		fun setIn(index: Int, value: Int) {
			if (ticketCheck!![index] != ticketNumber) {
				keyList[index] = defaultKey
				inList[index] = value
				outList[index] = index
				ticketCheck!![index] = ticketNumber
			} else {
				inList[index] = value
			}
		}

		fun setOut(index: Int, value: Int) {
			if (ticketCheck!![index] != ticketNumber) {
				keyList[index] = defaultKey
				inList[index] = index
				outList[index] = value
				ticketCheck!![index] = ticketNumber
			} else {
				outList[index] = value
			}
		}

		private fun parent(index: Int): Int {
			return (index - 1) / 2
		}

		private fun leftChild(index: Int): Int {
			return 2 * index + 1
		}

		private fun rightChild(index: Int): Int {
			return 2 * index + 2
		}
	}
}