/**
 * UtteranceStatistics.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import java.util.Arrays;

import org.aiwolf.client.lib.Topic;
import org.nd4j.common.util.ArrayUtil;

/**
 * utterance statistics
 * 
 * @author otsuki
 *
 */
public class UtteranceStatistics {

	private double[][] absolute = new double[Feature.NUM_TURNS][Feature.NUM_TOPICS];
	private double[][] relative = new double[Feature.NUM_TURNS][Feature.NUM_TOPICS];

	public UtteranceStatistics() {
	}

	public UtteranceStatistics(double initial_value) {
		for (int turn = 0; turn < Feature.NUM_TURNS; turn++) {
			for (int iTopic = 0; iTopic < Feature.NUM_TOPICS; iTopic++) {
				absolute[turn][iTopic] = initial_value;
			}
		}
	}

	/**
	 * set value
	 * 
	 * @param turn
	 * @param topic
	 * @param value
	 * @return false in case of invalid argument or no change in value
	 */
	public boolean set(int turn, Topic topic, double value) {
		if (turn < 0 || turn >= Feature.NUM_TURNS || Feature.topicIntMap.get(topic) == null || absolute[turn][Feature.topicIntMap.get(topic)] == value) {
			return false;
		}
		absolute[turn][Feature.topicIntMap.get(topic)] = value;
		return true;
	}

	/**
	 * get value
	 * 
	 * @param turn
	 * @param topic
	 * @return the value
	 */
	public double get(int turn, Topic topic) {
		if (turn < 0 || turn >= Feature.NUM_TURNS || Feature.topicIntMap.get(topic) == null) {
			return 0;
		}
		return absolute[turn][Feature.topicIntMap.get(topic)];
	}

	/**
	 * increment value
	 * 
	 * @param turn
	 * @param topic
	 * @return false in case of invalid argument
	 */
	public boolean increment(int turn, Topic topic) {
		if (turn < 0 || turn >= Feature.NUM_TURNS || Feature.topicIntMap.get(topic) == null) {
			return false;
		}
		absolute[turn][Feature.topicIntMap.get(topic)]++;
		return true;
	}

	/**
	 * add statistic
	 * 
	 * @param addition
	 * @return
	 */
	public boolean add(double[][] addition) {
		if (addition.length != Feature.NUM_TURNS || addition[0].length != Feature.NUM_TOPICS) {
			return false;
		}
		for (int turn = 0; turn < Feature.NUM_TURNS; turn++) {
			for (int iTopic = 0; iTopic < Feature.NUM_TOPICS; iTopic++) {
				absolute[turn][iTopic] += addition[turn][iTopic];
			}
		}
		return true;
	}

	/**
	 * get relative frequency matrix
	 * 
	 * @return
	 */
	public double[][] getRelativeMatrix() {
		for (int turn = 0; turn < Feature.NUM_TURNS; turn++) {
			double sum = 0;
			for (int iTopic = 0; iTopic < Feature.NUM_TOPICS; iTopic++) {
				sum += absolute[turn][iTopic];
			}
			for (int iTopic = 0; iTopic < Feature.NUM_TOPICS; iTopic++) {
				relative[turn][iTopic] = sum == 0 ? 0 : absolute[turn][iTopic] / sum;
			}
		}
		return relative;
	}

	/**
	 * get relative frequency in vector form
	 * 
	 * @return
	 */
	public double[] getRelativeVerctor() {
		return ArrayUtil.combineDouble(Arrays.asList(getRelativeMatrix()));
	}

	/**
	 * get frequency matrix
	 * 
	 * @return
	 */
	public double[][] getAbsoluteMatrix() {
		return absolute;
	}

	/**
	 * get frequency in vector form
	 * 
	 * @return
	 */
	public double[] getAbsoluteVerctor() {
		return ArrayUtil.combineDouble(Arrays.asList(absolute));
	}

}
