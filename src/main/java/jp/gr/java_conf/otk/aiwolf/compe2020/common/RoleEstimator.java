/**
 * RoleEstimator.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import static java.util.Comparator.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.util.ArrayUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * role estimator
 * 
 * @author otsuki
 *
 */
public class RoleEstimator extends HashMap<Agent, Double> {

	private static final long serialVersionUID = 2814603016683115994L;

	private int date;
	private Role role;
	private GameInfo gameInfo;
	private MultiLayerNetwork[] model05;
	private MultiLayerNetwork[] model15;
	private Map<Agent, Map<Role, UtteranceStatistics>> statMap = new HashMap<>();

	private Feature feature;

	private Map<Agent, Double> randomMap = new HashMap<>();

	public RoleEstimator(Role role) {
		this.role = role;
		// load models for 5 agent village
		model05 = new MultiLayerNetwork[4];
		for (int date = 1; date < 3; date++) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(
					String.format("05/%s/%02d/bestModel.h5", role.toString().toLowerCase(), date));
			try {
				model05[date] = is == null ? null : KerasModelImport.importKerasSequentialModelAndWeights(is);
			} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		model05[0] = model05[1];
		model05[3] = model05[2];

		// load models for 15 agent village
		model15 = new MultiLayerNetwork[15];
		for (int date = 1; date < 7; date++) {
			InputStream is = getClass().getClassLoader().getResourceAsStream(
					String.format("15/%s/%02d/bestModel.h5", role.toString().toLowerCase(), date));
			try {
				model15[date] = KerasModelImport.importKerasSequentialModelAndWeights(is);
			} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
				e.printStackTrace();
			}
		}
		model15[0] = model15[1];
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				String.format("15/%s/07-11/bestModel.h5", role.toString().toLowerCase()));
		try {
			model15[7] = KerasModelImport.importKerasSequentialModelAndWeights(is);
		} catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
			e.printStackTrace();
		}
		for (int date = 8; date < 15; date++) {
			model15[date] = model15[7];
		}

		for (int ai = 1; ai <= 15; ai++) {
			Map<Role, UtteranceStatistics> map = new HashMap<>();
			for (Role r : Arrays.asList(Role.WEREWOLF, Role.VILLAGER, Role.SEER, Role.POSSESSED, Role.MEDIUM, Role.BODYGUARD)) {
				map.put(r, new UtteranceStatistics(0.01));
			}
			statMap.put(Agent.getAgent(ai), map);
		}
	}

	/**
	 * called from Player#initialize()
	 * 
	 * @param gameInfo
	 */
	public void initialize(GameInfo gameInfo) {
		date = -1;
		for (Agent agent : gameInfo.getAgentList()) {
			if (agent == gameInfo.getAgent() && gameInfo.getRole() == role) {
				put(agent, Double.valueOf(1));
			} else {
				put(agent, Double.valueOf(0));
			}
			randomMap.put(agent, Math.random() / 1.0e7);
		}
		feature = new Feature(gameInfo);
	}

	/**
	 * called from Player#update()
	 * 
	 * @param gameInfo
	 */
	public void update(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		date = gameInfo.getDay();
		feature.update(gameInfo, false);
		if (feature.isModified()) {
			updateValue();
		}
	}

	private void updateValue() {
		for (Agent agent : gameInfo.getAgentList()) {
			if (agent == gameInfo.getAgent()) {
				continue;
			}
			List<double[]> f1 = new ArrayList<>();
			f1.add(feature.getFeatureArrayOf(agent));
			for (Role r : Arrays.asList(Role.WEREWOLF, Role.VILLAGER, Role.SEER, Role.POSSESSED, Role.MEDIUM, Role.BODYGUARD)) {
				double[] f2 = feature.getUtterancePatternOf(agent);
				double[] f3 = statMap.get(agent).get(r).getRelativeVerctor();
				double[] f4 = new double[f3.length];
				for (int i = 0; i < f3.length; i++) {
					f4[i] = f2[i] * f3[i];
				}
				f1.add(f4);
			}
			double[] f = ArrayUtil.combineDouble(f1);
			INDArray input = Nd4j.zeros(1, f.length);
			input.putRow(0, Nd4j.create(f));
			MultiLayerNetwork model = gameInfo.getAgentList().size() == 5 ? model05[date] : model15[date];
			INDArray output = model.output(input, false);
			put(agent, output.getDouble(0) + randomMap.get(agent));
		}
	}

	/**
	 * aggregation at the end of game
	 */
	public void finish() {
		for (Agent agent : gameInfo.getAgentList()) {
			Role r = gameInfo.getRoleMap().get(agent);
			statMap.get(agent).get(r).add(ArrayUtil.reshapeDouble(feature.getUtterancePatternOf(agent), Feature.NUM_TURNS, Feature.NUM_TOPICS));
		}
	}

	/**
	 * returns the agent having maximum probability
	 * 
	 * @param agents
	 *               list of candidates
	 * @return the agent having maximum probability
	 */
	public Agent max(Collection<Agent> agents, Agent... excludes) {
		return agents.stream().filter(a -> !Arrays.asList(excludes).contains(a))
				.max(comparing(a -> get(a))).orElse(null);
	}

	/**
	 * returns the agent having minimum probability
	 * 
	 * @param agents
	 *               list of candidates
	 * @return the agent having minimum probability
	 */
	public Agent min(Collection<Agent> agents, Agent... excludes) {
		return agents.stream().filter(a -> !Arrays.asList(excludes).contains(a))
				.min(comparing(a -> get(a))).orElse(null);
	}

	/**
	 * returns a list of agents sorted by role probability in descending order
	 * 
	 * @param agents
	 *               a list of agents to be sorted
	 * @return a list of agents sorted by role probability in descending order
	 */
	public List<Agent> descneding(Collection<Agent> agents) {
		return agents.stream().sorted(comparing(a -> get(a)).reversed()).collect(Collectors.toList());
	}

	/**
	 * returns a list of agents sorted by role probability in ascending order
	 * 
	 * @param agents
	 *               a list of agents to be sorted
	 * @return a list of agents sorted by role probability in ascending order
	 */
	public List<Agent> ascneding(Collection<Agent> agents) {
		return agents.stream().sorted(comparing(a -> get(a))).collect(Collectors.toList());
	}

}
