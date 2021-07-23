module mapsformarkov {
	requires transitive java.desktop;
	requires markov;
	requires finitestatemachine;
	requires cachingutils;
	exports mapsformarkov;
	exports mapsformarkov.examples;
}