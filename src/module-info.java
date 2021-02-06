module mapsformarkov {
	requires transitive java.desktop;
	requires markov;
	requires finitestatemachine;
	exports mapsformarkov;
	exports mapsformarkov.examples;
}