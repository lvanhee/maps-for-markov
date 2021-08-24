module mapsformarkov {
	requires transitive java.desktop;
	requires markov;
	requires finitestatemachine;
	requires cachingutils;
	requires obstaclemaps;
	exports mapsformarkov;
	exports mapsformarkov.examples;
}