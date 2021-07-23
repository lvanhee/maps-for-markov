package mapsformarkov;

import markov.impl.TablableState;

public interface MDPMapState extends TablableState{
	public static MDPMapState parse(String s, int mapWidth)
	{
		if(s.equals("INSTANCE"))return GoalReachedState.INSTANCE;
		return PointState.parse(s, mapWidth);
	}
	
}
