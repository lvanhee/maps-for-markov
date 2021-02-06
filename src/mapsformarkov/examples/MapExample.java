package mapsformarkov.examples;

import java.awt.Point;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import mapsformarkov.MoveToGoalOnSlidingObstacleGridMDP;

public class MapExample {

	/**
	 * This procedure generates a set of points for a 64x53 map. The code is a bit convoluted as it was used for
	 * highlighting some properties for a scientific article.
	 * @return
	 */
	public static Set<Point> getPointDistributionExample(){
		Set<Point> obstacles = new HashSet<>();

		for(int i = 5 ; i < MoveToGoalOnSlidingObstacleGridMDP.height-5; i++)
			obstacles.add(new Point(MoveToGoalOnSlidingObstacleGridMDP.width/2, i));

		Random r = new Random(0);
		for(int i = 0 ; i < MoveToGoalOnSlidingObstacleGridMDP.width; i++)
		{
			int y = 7 + r.nextInt(5);
			if(i>MoveToGoalOnSlidingObstacleGridMDP.width/2)
				y+=MoveToGoalOnSlidingObstacleGridMDP.height*2/3;

			double proba=0.5;
			if(i>MoveToGoalOnSlidingObstacleGridMDP.width/2)
				proba+=0.2;
			if(r.nextDouble()< proba) obstacles.add(new Point(i, y));
		}

		obstacles.add(new Point(26, 8));
		obstacles.add(new Point(29, 9));
		obstacles.add(new Point(23, 8));
		obstacles.remove(new Point(29, 10));
		obstacles.add(new Point(31, 7));

		//	obstacles.add(new Point(15, 7));

		obstacles.add(new Point(26, 10));
		obstacles.add(new Point(34, 45));

		obstacles.add(new Point(37, 11));


		obstacles.add(new Point(33, 42));
		obstacles.add(new Point(35, 42));
		obstacles.add(new Point(35, 43));

		obstacles.add(new Point(38, 45));
		obstacles.add(new Point(34, 47));

		obstacles.add(new Point(41, 48));

		obstacles.add(new Point(36, 44));obstacles.add(new Point(38, 44)); obstacles.add(new Point(38, 47)); obstacles.add(new Point(38, 43));


		//obstacles.add(new Point(36, 28));

		for(int i = 0 ; i < MoveToGoalOnSlidingObstacleGridMDP.width; i++)
			for(int j = 0 ; j < MoveToGoalOnSlidingObstacleGridMDP.height; j++)
				if(r.nextDouble()<0.01)
					obstacles.add(new Point(i,j));

		for(int i = 0 ; i < 45 ; i++)
			obstacles.remove(new Point(40, i));
		for(int i = 0 ; i < 45 ; i++)
			obstacles.remove(new Point(41, i));
		for(int i = 0 ; i < 40 ; i++)
			obstacles.remove(new Point(39, i));
		for(int i = 0 ; i < 20 ; i++)
			obstacles.remove(new Point(12, i));

		obstacles.add(new Point(41, 47));
		obstacles.add(new Point(14, 8));
		return obstacles;
	}
	
	/**
	 * Gives an example that was used for a scientific paper.
	 * @param xgoal: the x axis where the goal is placed
	 * @return
	 */
	public static MoveToGoalOnSlidingObstacleGridMDP getExampleMapMdp(int xgoal)
	{
		return MoveToGoalOnSlidingObstacleGridMDP.newInstance(getPointDistributionExample(), new Point(xgoal, 2));
	}
}
