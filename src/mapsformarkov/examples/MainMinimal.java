package mapsformarkov.examples;

import java.awt.Point;

import mapsformarkov.MDPMapState;
import mapsformarkov.MapAction;
import mapsformarkov.MapDisplayer;
import mapsformarkov.MoveToGoalOnSlidingObstacleGridMDP;
import mapsformarkov.PointState;
import markov.GeneralizedValueFunction;
import markov.Policy;
import markov.impl.Policies;
import markov.impl.ValueFunctions;

public class MainMinimal {
	
	public static void main(String[] args) throws InterruptedException
	{
		MoveToGoalOnSlidingObstacleGridMDP mdp = MapExample.getExampleMapMdp(26);
		
		MapDisplayer d = MapDisplayer.newInstance(mdp);
		d.setVisible(true);
		
		
		Policy<MDPMapState, MapAction> optimal = Policies.getOptimalPolicy(mdp, 100);
		d.setPolicyToDraw(optimal);
		
		d.setDrawingPath(PointState.newInstance(new Point(32, 50), mdp.getWidth()), 100);
		
		GeneralizedValueFunction<MDPMapState, Double> optimalValue = ValueFunctions.getAverageValueOf(mdp, optimal, 100);
		d.setBackgroundColorToDraw(optimalValue);
		
		d.exportToFile("output/myexport.png");
	}

}
