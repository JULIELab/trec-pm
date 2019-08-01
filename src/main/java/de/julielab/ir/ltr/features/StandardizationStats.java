package de.julielab.ir.ltr.features;

public class StandardizationStats {
	public StandardizationStats(double[] means, double[] stdDevs) {
		this.means = means;
		stdDeviations = stdDevs;
	}
	public double[] means;
	public double[] stdDeviations;
}
