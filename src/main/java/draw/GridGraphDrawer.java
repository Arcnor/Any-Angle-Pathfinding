package draw;

import com.github.ohohcakester.grid.GridGraph;

import java.awt.Graphics;


public class GridGraphDrawer implements Drawer {
	private final GridGraph gridGraph;

	private final int resX;
	private final int resY;

	public GridGraphDrawer(GridGraph gridGraph, int resX, int resY) {
		this.gridGraph = gridGraph;
		this.resX = resX;
		this.resY = resY;
	}

	@Override
	public void draw(Graphics g) {
		for (int x = 0; x < gridGraph.getSizeX(); x++) {
			for (int y = 0; y < gridGraph.getSizeY(); y++) {
				if (gridGraph.isBlocked(x, y)) {
					drawSquare(g, x, y);
				}
			}
		}
	}

	private void drawSquare(Graphics g, int x, int y) {
		float width = (float) resX / gridGraph.getSizeX();
		float height = (float) resY / gridGraph.getSizeY();
		float xPos = width * x;
		float yPos = height * y;

		g.fillRect((int) xPos, (int) yPos, (int) width + 1, (int) height + 1);
	}
}
