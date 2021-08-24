package mapsformarkov;

import java.util.ArrayList;


import cachingutils.Cache;
import markov.impl.StateHorizonPair;
import obstaclemaps.MinimalExample;

public class GridStateHorizonToRewardDistributionCacheImpl<V> implements Cache<StateHorizonPair<MDPMapState>, V> {
	
	private final java.util.List<V>table;
	
	private int added = 0;
	private final int horizon;
	
	private GridStateHorizonToRewardDistributionCacheImpl(int horizon) {
		this.horizon = horizon;
		table = new ArrayList<V>(MinimalExample.NB_TILES * (horizon+2) + 1);
		for(int i = 0 ; i < MinimalExample.NB_TILES * (horizon+2) + 1; i++)
			table.add(null);
		
		
	}

	@Override
	public synchronized void add(StateHorizonPair<MDPMapState> i, V df) {
		//if(added%1000==0)System.out.println(added+" "+df.getItems().size()+" "+i.getHorizon());
		added++;
		int index = indexOf(i);
		//assert(table[index]==null);
		table.set(index,df);
	}

	@Override
	public synchronized boolean has(StateHorizonPair<MDPMapState> i) {
		return table.get(indexOf(i))!= null;
	}

	private synchronized int indexOf(StateHorizonPair<MDPMapState> i) {
		if(i.getState() instanceof GoalReachedState) return i.getHorizon();
		PointState p =(PointState)i.getState();
		
		return p.getPoint().x + p.getPoint().y*MinimalExample.width + MinimalExample.NB_TILES*i.getHorizon()+horizon;
	}

	@Override
	public synchronized V get(StateHorizonPair<MDPMapState> i) {
		return table.get(indexOf(i));
	}

	public static<V> Cache<StateHorizonPair<MDPMapState>, V> newInstance(int horizon) {
		return new GridStateHorizonToRewardDistributionCacheImpl(horizon);
	}
	
}
