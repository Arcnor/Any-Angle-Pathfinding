package draw;

import com.github.ohohcakester.grid.GridGraph;
import draw.GridLineSet.FractionLine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GridLineDrawer implements Drawer {

	private static final int LINE_THICKNESS = 3;

	private final GridLineSet gridLineSet;
	private final GridGraph gridGraph;
	private final int resX;
	private final int resY;


	public GridLineDrawer(GridGraph gridGraph, GridLineSet gridLineSet, int resX, int resY) {
		this.resX = resX;
		this.resY = resY;
		this.gridGraph = gridGraph;
		this.gridLineSet = gridLineSet;
	}

	/* (non-Javadoc)
	 * @see draw.Drawer#draw(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		if (gridLineSet == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(LINE_THICKNESS));

		for (GridLineSet.Line line : gridLineSet.getLineList()) {
			drawLine(g, line);
		}
		for (GridLineSet.FractionLine line : gridLineSet.getFractionLineList()) {
			drawFractionLine(g, line);
		}

		g2.setStroke(new BasicStroke(1));
	}

	private void drawFractionLine(Graphics g, FractionLine line) {
		float width = (float) resX / gridGraph.getSizeX();
		float height = (float) resY / gridGraph.getSizeY();
		int x1 = (int) (width * line.x1.getN() / line.x1.getD());
		int y1 = (int) (height * line.y1.getN() / line.y1.getD());
		int x2 = (int) (width * line.x2.getN() / line.x2.getD());
		int y2 = (int) (height * line.y2.getN() / line.y2.getD());

		g.setColor(line.color);

		//g2.draw(new Line2D.Float(x1, y1, x2, y2));
		g.drawLine(x1, y1, x2, y2);
		g.setColor(Color.BLACK);
	}

	private void drawLine(Graphics g, GridLineSet.Line line) {
		float width = (float) resX / gridGraph.getSizeX();
		float height = (float) resY / gridGraph.getSizeY();
		int x1 = (int) (width * line.x1);
		int y1 = (int) (height * line.y1);
		int x2 = (int) (width * line.x2);
		int y2 = (int) (height * line.y2);

		g.setColor(line.color);

		//g2.draw(new Line2D.Float(x1, y1, x2, y2));
		g.drawLine(x1, y1, x2, y2);
		g.setColor(Color.BLACK);
	}
}
