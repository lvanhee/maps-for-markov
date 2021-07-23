package mapsformarkov;

import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import markov.MDP;
import markov.impl.StateProbabilityDistributionHashImpl;

/**
 * For students: this class demonstrates a more fully-fledged implementation of a MDP than in the basic example in the
 * MDP package.
 * See how this implementation is actually very straightforward from the MDP model (states, possible actions, 
 * effects of actions, rewards per state). 
 * @author loisv
 *
 */
public class MoveToGoalOnSlidingObstacleGridMDP implements MDP<MDPMapState, MapAction>{
	
	//predefined constants, kept here for simplicity
	//but the highest quality implementation should allow including them as private parameters 
	//rather than as public constants
	public static final int DEFAULT_HORIZON = 200;
	public static final double DEFAULT_PENALTY_FOR_HITTING_OBSTACLES = -60;
	public static final double DEFAULT_PENALITY_PER_ROUND_FOR_BATTERY_CONSUMPTION = -1;
	public static final double SLIDE_OUT_PROBABILITY = 0.05;
	
	private final Set<Point> obstacles;
	private final Point goal;
	public static final int width = 64;
	public static final int height = 53;
	public static final int NB_TILES = width*height;
	
	
	private MoveToGoalOnSlidingObstacleGridMDP(Set<Point> obstacles, Point goal) {
		this.obstacles = obstacles;
		this.goal = goal;
	}
	
	private final Set<MDPMapState> cacheStates = new HashSet<>();
	@Override
	public Set<MDPMapState> getAllStates() {
		if(cacheStates.size()>0)return cacheStates;
		
		for(int i = 0 ; i < width ; i ++)
			for(int j = 0 ; j < height ; j++)
				cacheStates.add(PointState.newInstance(new Point(i,j), this.getWidth()));
		cacheStates.add(GoalReachedState.INSTANCE);
		
		return cacheStates;				
	}
	
	@Override
	public Set<MapAction> getPossibleActionsIn(MDPMapState s) {
		if(s instanceof GoalReachedState)
			return Arrays.asList(MapAction.STAND_BY).stream().collect(Collectors.toSet());
		PointState p = (PointState)s;
		if(p.getPoint().equals(goal))
			return Arrays.asList(MapAction.STAND_BY).stream().collect(Collectors.toSet());
		
		Set<MapAction> res = Arrays.asList(MapAction.values()).stream().collect(Collectors.toSet());
		res.remove(MapAction.STAND_BY);
		if(p.getPoint().getX()<=0)
			res.remove(MapAction.WEST);
		if(p.getPoint().getX()>=width-1)
			res.remove(MapAction.EAST);
		if(p.getPoint().getY()>=height-1)
			res.remove(MapAction.SOUTH);
		if(p.getPoint().getY()<=0)
			res.remove(MapAction.NORTH);
		return res;
	}

	@Override
	public double getRewardFor(MDPMapState s, MapAction a) {
		if(s instanceof GoalReachedState) return 0;
		PointState p = (PointState)s;
		if(p.getPoint().equals(goal))
			return 0;
		if(obstacles.contains(p.getPoint())) return DEFAULT_PENALTY_FOR_HITTING_OBSTACLES;
		
		return DEFAULT_PENALITY_PER_ROUND_FOR_BATTERY_CONSUMPTION;
	}

	private final StateProbabilityDistributionHashImpl<MDPMapState>[] cacheConsequences = new StateProbabilityDistributionHashImpl[(NB_TILES+1)*MapAction.values().length];
	@Override
	public StateProbabilityDistributionHashImpl<MDPMapState> getConsequencesOf(MDPMapState s, MapAction a) {
		
		if(s.equals(GoalReachedState.INSTANCE))return StateProbabilityDistributionHashImpl.newInstance(s);
				
		//this caching procedure allows saving time generating state probability distributions
		//not needed, but good example of a simple optimization trick for MDPs
		final int index = (1+s.getIndex())+(NB_TILES+1)*(MapAction.indexOf(a));
		if(index>cacheConsequences.length)
			throw new Error();
		if(cacheConsequences[index]!=null)
			return cacheConsequences[index];
		
		PointState p = (PointState)s;
		if(p.getPoint().equals(goal))return StateProbabilityDistributionHashImpl.newInstance(GoalReachedState.INSTANCE);
		
		int x = 0;
		int y = 0;
		switch (a) {
		case EAST: x = 1; break;
		case NORTH: y = -1; break;
		case SOUTH: y = 1; break;
		case WEST: x = -1; break;
		default: throw new Error();
		}
	

		Point target = new Point(p.getPoint().x+x, p.getPoint().y+y);
		
		Set<Point> neighborsOfTarget = new HashSet<>();
		neighborsOfTarget.add(new Point(target.x-1, target.y-1));
		neighborsOfTarget.add(new Point(target.x-1, target.y));
		neighborsOfTarget.add(new Point(target.x-1, target.y+1));
		neighborsOfTarget.add(new Point(target.x, target.y-1));
		neighborsOfTarget.add(new Point(target.x, target.y+1));
		neighborsOfTarget.add(new Point(target.x+1, target.y-1));
		neighborsOfTarget.add(new Point(target.x+1, target.y));
		neighborsOfTarget.add(new Point(target.x+1, target.y+1));
		neighborsOfTarget = neighborsOfTarget.stream().filter(pt->pt.x>0  && pt.y>0 && pt.x<width && pt.y<height)
				.collect(Collectors.toSet());
		
		
		final double probabiltyPerShift = SLIDE_OUT_PROBABILITY / neighborsOfTarget.size();
		
		Map<MDPMapState, Double> res = new HashMap<>();
		res.put(PointState.newInstance(target,this.getWidth()), 1 - SLIDE_OUT_PROBABILITY);
		for(Point pt:neighborsOfTarget) res.put(PointState.newInstance(pt, this.getWidth()), probabiltyPerShift);
		
		cacheConsequences[index] = StateProbabilityDistributionHashImpl.newInstance(res); 
		return cacheConsequences[index];
	}
	
	public static MoveToGoalOnSlidingObstacleGridMDP newInstance(Set<Point> obstacles, Point goal) {return new MoveToGoalOnSlidingObstacleGridMDP(obstacles,goal);}


	public Set<Point> getObstacles() {
		return obstacles;
	}


	public Point getGoalPoint() {
		return goal;
	}

	
	public static Set<MDPMapState> getNeighboursStates(Set<MDPMapState> original, Set<MDPMapState>toExclude, int width, int height) {
		return (Set<MDPMapState>) original.stream()
		.filter(x->!(x instanceof GoalReachedState))
		.map(x->(PointState)x)
		.map(x->{
			Set<PointState> res = new HashSet<>();
			if(x.getPoint().x>0) res.add(PointState.newInstance(new Point(x.getPoint().x-1, x.getPoint().y),width));
			if(x.getPoint().y>0) res.add(PointState.newInstance(new Point(x.getPoint().x, x.getPoint().y-1),width));
			if(x.getPoint().x<MoveToGoalOnSlidingObstacleGridMDP.width-1)
				res.add(PointState.newInstance(new Point(x.getPoint().x+1, x.getPoint().y),height));
			if(x.getPoint().y<MoveToGoalOnSlidingObstacleGridMDP.height-1)
				res.add(PointState.newInstance(new Point(x.getPoint().x, x.getPoint().y+1),height));
			return res;
		})
		.reduce(new HashSet(), (x,y)->{x.addAll(y);return x;})
		.stream()
		.filter(x-> !toExclude.contains(x))
		.collect(Collectors.toSet());
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}
}