package mapsformarkov;

import java.awt.Point;
import java.io.Serializable;

@SuppressWarnings("serial")
public final class PointState implements MDPMapState, Serializable {
	
	private final Point p;
	
	private final int mapSize;
	
	private PointState(Point p, int mapSize)
	{
		this.p = p;
		this.mapSize = mapSize;
	}

	public Point getPoint() {
		return p;
	}
	
	public String toString()
	{
		return p.x+","+p.y;
	}
	
	public int hashCode()
	{
		return p.x + p.y*mapSize+1;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof GoalReachedState) return false;
		return 
				((PointState)o).p.equals(this.p);
	}

	@Override
	public int getIndex() {
		return hashCode();
	}
	
	public static PointState parse(String x, int mapSize)
	 {
    	return new PointState(new Point(Integer.parseInt(x.split(",")[0]), Integer.parseInt(x.split(",")[1])),mapSize);
    }

	public static PointState newInstance(Point p, int mapSize) {
		return new PointState(p, mapSize);
	};

}
