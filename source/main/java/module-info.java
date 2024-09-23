module com.parallelsymmetry.utility.comm {

	// Compile time only
	requires static lombok;

	// Compile and runtime
	requires com.parallelsymmetry.utility;
	requires purejavacomm;
	requires java.logging;

	exports com.parallelsymmetry.utility.comm;
}
