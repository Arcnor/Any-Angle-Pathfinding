package com.github.ohohcakester.algorithms.anya;

import com.github.ohohcakester.algorithms.datatypes.Point;

class AnyaState {
	public final Fraction xL;
	public final Fraction xR;
	public final int y;
	public final Point basePoint;

	public float hValue;
	public float fValue;
	public float gValue;
	public AnyaState parent;
	public boolean visited;

	private AnyaState(Fraction xL, Fraction xR, int y, Point basePoint, float gValue, AnyaState parent) {
		this.xL = xL;
		this.xR = xR;
		this.y = y;
		this.basePoint = basePoint;

		this.gValue = gValue;
		this.parent = parent;
		this.visited = false;
	}

	public static AnyaState createStartState(Fraction xL, Fraction xR, int y, Point start) {
		return new AnyaState(xL, xR, y,
				start,
				0f,
				null);
	}

	public static AnyaState createObservableSuccessor(Fraction xL, Fraction xR, int y, AnyaState sourceInterval) {
		return new AnyaState(xL, xR, y,
				sourceInterval.basePoint,
				sourceInterval.gValue,
				sourceInterval.parent);
	}

	public static AnyaState createUnobservableSuccessor(Fraction xL, Fraction xR, int y, Point basePoint, AnyaState sourceInterval) {
		int dx = basePoint.getX() - sourceInterval.basePoint.getX();
		int dy = basePoint.getY() - sourceInterval.basePoint.getY();
		return new AnyaState(xL, xR, y,
				basePoint,
				sourceInterval.gValue + (float) Math.sqrt(dx * dx + dy * dy),
				sourceInterval);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// Removed null checks.
		result = prime * result + basePoint.hashCode();
		result = prime * result + xL.hashCode();
		result = prime * result + xR.hashCode();
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// Removed type checks. Removed null checks.
		AnyaState other = (AnyaState) obj;
		if (!xL.equals(other.xL)) return false;
		if (!xR.equals(other.xR)) return false;
		if (y != other.y) return false;
		if (!basePoint.equals(other.basePoint)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + xL + " " + xR + ") - " + y;
	}
}