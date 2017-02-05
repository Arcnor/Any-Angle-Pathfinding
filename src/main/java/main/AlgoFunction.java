package main;

import com.github.ohohcakester.algorithms.PathFindingAlgorithm;
import com.github.ohohcakester.grid.GridGraph;
import kotlin.jvm.functions.Function2;

import java.util.function.BiFunction;

@FunctionalInterface
public interface AlgoFunction<P> {
	PathFindingAlgorithm<P> getAlgo(GridGraph gridGraph, int sx, int sy, int ex, int ey, Function2<Integer, Integer, P> pointConstructor);
}