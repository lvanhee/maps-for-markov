package mapsformarkov.examples;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mapsformarkov.MoveToGoalOnSlidingObstacleGridMDP;
import obstaclemaps.MinimalExample;

public class MapExample {

	
	
	/**
	 * Gives an example that was used for a scientific paper.
	 * @param xgoal: the x axis where the goal is placed
	 * @return
	 */
	public static MoveToGoalOnSlidingObstacleGridMDP getExampleMapMdp(int xgoal)
	{
		return MoveToGoalOnSlidingObstacleGridMDP.newInstance(MinimalExample.getPointDistributionExample(), new Point(xgoal, 2));
	}
}
