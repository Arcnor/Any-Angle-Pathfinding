package main;

import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.grid.GridGraph;

public interface AlgoFunction {
	public abstract PathFindingAlgorithm getAlgo(GridGraph gridGraph, int sx, int sy, int ex, int ey);
}