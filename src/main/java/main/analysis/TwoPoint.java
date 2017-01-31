package main.analysis;

import com.github.ohohcakester.datatypes.Point;

public class TwoPoint {
	public final Point p1, p2;

	public TwoPoint(Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public TwoPoint(int x1, int y1, int x2, int y2) {
		this.p1 = new Point(x1, y1);
		this.p2 = new Point(x2, y2);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TwoPoint)) return false;
		TwoPoint other = (TwoPoint) obj;
		if (p1.equals(other.p1) && p2.equals(other.p2)) {
			return true;
		}
		if (p1.equals(other.p2) && p2.equals(other.p1)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return p1.getX() + " " + p1.getY() + " " + p2.getX() + " " + p2.getY();
	}
}