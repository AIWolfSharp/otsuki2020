/**
 * AgentFeature.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import java.util.Arrays;

import org.nd4j.common.util.ArrayUtil;

/**
 * container of agent features
 * 
 * @author otsuki
 *
 */
public class AgentFeature implements Cloneable {

	private double[] feature;
	private double sum;
	private int bin;

	public double getSum() {
		return sum;
	}

	public int getBin() {
		return bin;
	}

	public double[] getArray() {
		return feature;
	}

	public int[] getIntArray() {
		return ArrayUtil.toInts(feature);
	}

	public double get(int index) {
		return feature[index];
	}

	public double set(int index, double value) {
		return feature[index] = value;
	}

	public double inc(int index) {
		return ++feature[index];
	}

	public AgentFeature(double[] feature) {
		this.feature = feature.clone();
		StringBuilder s = new StringBuilder();
		for (double d : feature) {
			sum += d;
			s.append(d == 0 ? "0" : "1");
		}
		bin = Integer.parseInt(s.toString(), 2);
	}

	public AgentFeature(int[] feature) {
		this(ArrayUtil.toDoubles(feature));
	}

	public AgentFeature(int size) {
		this(new double[size]);
	}

	@Override
	protected AgentFeature clone() {
		return new AgentFeature(feature);
	}

	public boolean equals(AgentFeature af) {
		return Arrays.equals(feature, af.feature);
	}
}
