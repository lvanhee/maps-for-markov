package mapsformarkov;

import finitestatemachine.Action;

public enum MapAction implements Action {NORTH, SOUTH, EAST, WEST, STAND_BY;

	public static int indexOf(MapAction a) {
		switch (a) {
		case NORTH: return 0;
		case SOUTH: return 1;
		case EAST: return 2;
		case WEST: return 3;
		case STAND_BY: return 4;
		}
		throw new Error();
	}
};
