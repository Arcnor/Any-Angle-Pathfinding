package draw;

import com.github.ohohcakester.grid.GridGraph;

import java.awt.Color;
import java.awt.Graphics;

public class GridPointDrawer implements Drawer {

	private static final int CIRCLE_SCALE_FACTOR = 1;
	private static final float OUTLINE_SCALE_FACTOR = 0.3f;

	private static final Color OUTLINE_COLOR = new Color(64, 64, 64);


	private final GridPointSet gridPointSet;
	private final GridGraph gridGraph;
	private final int resX;
	private final int resY;

	private final int outline;
	private final int halfOutline;
	private final float width;
	private final float height;
	protected int circleSize;
	protected int halfCircleSize;

	public GridPointDrawer(GridGraph gridGraph, GridPointSet gridPointSet, int resX, int resY) {
		int minCircleSize = gridPointSet == null ? Integer.MIN_VALUE : gridPointSet.minCircleSize();

		this.resX = resX;
		this.resY = resY;
		width = (float) resX / gridGraph.getSizeX();
		height = (float) resY / gridGraph.getSizeY();
		circleSize = Math.max((int) (width / 4), minCircleSize);
		circleSize *= CIRCLE_SCALE_FACTOR;
		halfCircleSize = circleSize / 2;

		outline = (int) (circleSize * OUTLINE_SCALE_FACTOR);
		halfOutline = outline / 2;

		this.gridGraph = gridGraph;
		this.gridPointSet = gridPointSet;
	}

	@Override
	public void draw(Graphics g) {
		if (gridPointSet == null) {
			return;
		}
		for (GridPointSet.ColourPoint point : gridPointSet.getPointList()) {
			drawPoint(g, point);
		}
	}

	private void drawPoint(Graphics g, GridPointSet.ColourPoint point) {
		int x = (int) (width * point.x);
		int y = (int) (height * point.y);

		if (outline >= 0) {
			g.setColor(OUTLINE_COLOR);
			g.fillOval(x - halfCircleSize - halfOutline, y - halfCircleSize - halfOutline, circleSize + outline, circleSize + outline);
		}
		g.setColor(point.color);
		g.fillOval(x - halfCircleSize, y - halfCircleSize, circleSize, circleSize);
		g.setColor(Color.BLACK);
	}
}
