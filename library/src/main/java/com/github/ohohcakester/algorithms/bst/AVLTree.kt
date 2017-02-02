package com.github.ohohcakester.algorithms.bst

import java.util.Stack

internal class AVLNode<E>(data: E, var balance: Int = 10) : Node<E>(data)

class AVLTree<E : Comparable<E>> : BinarySearchTree<E>() {
	var first: Node<E>? = null
		private set
	var last: Node<E>? = null
		private set

	init {
		root = null
	}

	private fun rightRightRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates root left.

		/* Nodes with balance change: Root, Right child.
         * Old Root: +2, Old Right child: +1
         *
         * New Root(Old RC): 0,
         * New Left child(Old Root): 0
         */

		root.balance = 0
		(root.right as AVLNode<E>).balance = 0
		return rotateLeft(root)
	}

	private fun leftLeftRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates root rigjt.

		/* Nodes with balance change: Root, Left child.
         * Old Root: -2, Old Left child: -1
         *
         * New Root(Old LC): 0,
         * New Right child(Old Root): 0
         */

		root.balance = 0
		(root.left as AVLNode<E>).balance = 0
		return rotateRight(root)
	}


	private fun rightCenterRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates root left.

		/* Nodes with balance change: Root, Right child.
         * Old Root: +2, Old Right child: 0
         *
         * New Root(Old RC): -1,
         * New Left child(Old Root): +1
         */

		(root.right as AVLNode<E>).balance = -1
		root.balance = 1
		return rotateLeft(root)
	}


	private fun leftCenterRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates root right.

		/* Nodes with balance change: Root, Left child.
         * Old Root: -2, Old Left child: 0
         *
         * New Root(Old RC): +1,
         * New Right child(Old Root): -1
         */

		(root.left as AVLNode<E>).balance = 1
		root.balance = -1
		return rotateRight(root)
	}


	private fun leftRightRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates LC left, then root right.

		/* Nodes with balance change: Root, Left child, LR grandchild.
         * Old Root: -2, Old Left child: +1, Old LR grandchild: -1 or +1
         *
         * New Root(Old LR grandchild): 0
         * New Right child(Old root): (Old RL == -1) ? +1 : 0
         * New Left child(Old Left child): (Old RL == +1) ? -1 : 0
         */
		val balance = (root.left!!.right as AVLNode<E>).balance
		(root.left!!.right as AVLNode<E>).balance = 0
		root.balance = if (balance == -1) 1 else 0
		(root.left as AVLNode<E>).balance = if (balance == 1) -1 else 0

		root.left = rotateLeft(root.left!!)
		return rotateRight(root)
	}

	private fun rightLeftRotate(root: AVLNode<E>): Node<E> {
		// Note: changes balance.
		// Rotates RC right, then root left.

		/* Nodes with balance change: Root, Right child, RL grandchild.
         * Old Root: +2, Old Right child: -1, Old RL grandchild: +1 or -1
         *
         * New Root(Old RL grandchild): 0
         * New Left child(Old root): (Old RL == +1) ? -1 : 0
         * New Right child(Old Right child): (Old RL == -1) ? +1 : 0
         */
		val balance = (root.right!!.left as AVLNode<E>).balance
		(root.right!!.left as AVLNode<E>).balance = 0
		root.balance = if (balance == 1) -1 else 0
		(root.right as AVLNode<E>).balance = if (balance == -1) 1 else 0


		root.right = rotateRight(root.right!!)
		return rotateLeft(root)
	}


	private fun conditionalRotate(current: AVLNode<E>): Node<E> {
		// 6 Possibilities:
		// Current = +2, current.right = +1 / -1 / 0
		// Current = -2, current.left = +1 / -1 / 0
		// Note: child with 0 balance occurs only for deletion.

		// Does rotation and readjusts balance accordingly.

		if (current.balance == 2) {
			if ((current.right as AVLNode<E>).balance == 1)
				return rightRightRotate(current)
			else if ((current.right as AVLNode<E>).balance == -1)
				return rightLeftRotate(current)
			else
			// right.balance == 0
				return rightCenterRotate(current)
		} else { // current.balance == -2
			if ((current.left as AVLNode<E>).balance == -1)
				return leftLeftRotate(current)
			else if ((current.left as AVLNode<E>).balance == 1)
				return leftRightRotate(current)
			else
			// left.balance == 0
				return leftCenterRotate(current)
		}
	}

	private fun log(node: Node<E>) {
		println(node.data.toString() + " : " + (node as AVLNode<E>).balance)
	}

	fun insert(data: E) {
		val newRoot = insert(root, data, null, false) ?: return
		root = newRoot
		if ((root as AVLNode<E>).balance == 10)
			(root as AVLNode<E>).balance = 0
	}

	/*private void printCurrent(Node<E> current) { // DEBUGGING TOOL
        if (current == null)return;
        System.out.println(current +": >> " + current.left + " " + current.right + " | " + current.prev + " " + current.next);
    }*/

	private fun insert(current: Node<E>?, data: E, parent: Node<E>?, wentRight: Boolean): Node<E>? {
		if (current == null) {
			val newNode = AVLNode(data)
			if (parent != null) {
				if (wentRight) {
					// parent is left of newNode.
					newNode.next = parent.next
					newNode.prev = parent
					if (parent.next != null) {
						parent.next!!.prev = newNode
					} else {
						last = newNode
					}
					parent.next = newNode
				} else {
					// parent is right of newNode.
					newNode.prev = parent.prev
					newNode.next = parent
					if (parent.prev != null) {
						parent.prev!!.next = newNode
					} else {
						first = newNode
					}
					parent.prev = newNode
				}
			} else {
				// Root
				first = newNode
				last = newNode
			}
			return newNode
		}

		val compare = data.compareTo(current.data!!)

		if (compare == 0)
			return null // don't insert

		if (compare < 0) {
			val newLeft = insert(current.left, data, current, false) ?: return null
			current.left = newLeft

			// Modify balance / Rebalancing
			if ((current.left as AVLNode<E>).balance == 0)
				return null // child has been balanced to 0. End recursion.
			else {
				if ((current.left as AVLNode<E>).balance == 10)
					(current.left as AVLNode<E>).balance = 0

				// child has been unbalanced away from 0. Change balance of current.
				val avlCurrent = current as AVLNode<E>?
				avlCurrent!!.balance--
				if (avlCurrent.balance == -2) {
					// Unbalanced to -2. need rotation.
					return conditionalRotate(avlCurrent)
				}
				return current
			}
		} else {
			val newRight = insert(current.right, data, current, true) ?: return null
			current.right = newRight

			// Modify balance / Rebalancing
			if ((current.right as AVLNode<E>).balance == 0)
				return null // child has been balanced to 0. End recursion.
			else {
				if ((current.right as AVLNode<E>).balance == 10)
					(current.right as AVLNode<E>).balance = 0

				// child has been unbalanced away from 0. Change balance of current.
				val avlCurrent = current as AVLNode<E>?
				avlCurrent!!.balance++
				if (avlCurrent.balance == 2) {
					// Unbalanced to 2. need rotation.
					return conditionalRotate(avlCurrent)
				}
				return current
			}
		}
	}

	fun logAll() {
		logAll(root)
	}

	private fun logAll(current: Node<E>?) {
		if (current == null) return
		logAll(current.left)
		log(current)
		logAll(current.right)
	}

	fun delete(data: E) {
		val nodeStack = Stack<Node<E>>()
		delete(data, nodeStack)
	}

	private fun delete(data: E, nodeStack: Stack<Node<E>>) {
		var current = searchAndPush(data, nodeStack) ?: throw NullPointerException("Item not found!")

		if (current.left == null) {
			linkedListRemove(current)
			linkFromParent(current, current.right, nodeStack) // set parent to point to right.
			current = current.right!!
		} else if (current.right == null) {
			linkedListRemove(current)
			linkFromParent(current, current.left, nodeStack) // set parent to point to right.
			current = current.left!!
		} else {// two children.
			nodeStack.push(current)
			val newCurrent: Node<E>
			if (current.right!!.left == null) {
				newCurrent = current.right!!
				current.right = newCurrent.right
			} else {
				// Note: newCurrent has no left child.
				newCurrent = findSmallestChildAndPush(current.right!!, nodeStack) // newTarget is inorder successor.
				nodeStack.peek().left = newCurrent.right
			}
			current.data = newCurrent.data // replace target data, search for new target.
			linkedListRemove(newCurrent)
			current = newCurrent.right!!
		}

		// Now to rebalance while clearing the stack.
		while (nodeStack.isNotEmpty()) {
			val parent = nodeStack.peek() as AVLNode<E>
			if (parent.left === current) {
				// Current is a left child
				if (parent.right === current)
				// basically current and right is null
					parent.balance = 0
				else
					parent.balance++
			} else {
				// Current is a right child.
				parent.balance--
			}

			// new current. = previous parent. for next loop.
			current = nodeStack.pop() // = parent

			// Note: continue loop iff parent balance != -1 and != 1.
			// End loop if parent balance == -1 or balance == 1;
			if (parent.balance == 2 || parent.balance == -2) {

				// Rotate parent accordingly.
				if (nodeStack.isEmpty()) {
					root = conditionalRotate(parent)
				} else {
					val grandParent = nodeStack.peek()

					if (grandParent.left === parent) {
						grandParent.left = conditionalRotate(parent)
						current = grandParent.left!!
					} else if (grandParent.right === parent) {
						grandParent.right = conditionalRotate(parent)
						current = grandParent.right!!
					}
				}
			}

			// Now current is the new parent.
			if ((current as AVLNode<E>).balance != 0) { // balance == 1 or == -1
				// End loop
				break
			}
			// else continue looping.
		}
	}

	private fun linkFromParent(current: Node<E>, next: Node<E>?, nodeStack: Stack<Node<E>>) {
		if (nodeStack.isEmpty())
			root = next
		else if (nodeStack.peek().left === current)
			nodeStack.peek().left = next
		else
			nodeStack.peek().right = next
	}

	private fun searchAndPush(data: E, nodeStack: Stack<Node<E>>): Node<E>? {
		var current = root

		while (true) {
			if (current == null)
				return null

			nodeStack.push(current)

			val compare = data.compareTo(current.data!!)

			if (compare < 0)
				current = current.left
			else if (compare > 0)
				current = current.right
			else
			// compare == 0.
				return nodeStack.pop()
		}

	}

	private fun findSmallestChildAndPush(current: Node<E>, nodeStack: Stack<Node<E>>): Node<E> {
		var current = current
		// assumpe current != null.

		while (current.left != null) {
			nodeStack.push(current)
			current = current.left!!
		}
		return current
	}


	private fun linkedListRemove(current: Node<E>) {
		if (current.next != null) {
			current.next!!.prev = current.prev
		} else {
			last = current.prev
		}
		if (current.prev != null) {
			current.prev!!.next = current.next
		} else {
			first = current.next
		}
	}
}