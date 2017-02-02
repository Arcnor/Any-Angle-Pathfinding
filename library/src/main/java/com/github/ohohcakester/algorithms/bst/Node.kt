package com.github.ohohcakester.algorithms.bst


open class Node<E>(var data: E) {
	/*private static int counter = 0; // DEBUGGING TOOL
    private int index;
    private void setIndex() {
        index = counter; counter++;
    }*/

	var left: Node<E>? = null
	var right: Node<E>? = null

	var next: Node<E>? = null
		internal set
	var prev: Node<E>? = null
		internal set

	/**
	 * Returns a string representation of the node.

	 * @return A string representation of the data fields
	 */
	override fun toString() = data.toString()
}

