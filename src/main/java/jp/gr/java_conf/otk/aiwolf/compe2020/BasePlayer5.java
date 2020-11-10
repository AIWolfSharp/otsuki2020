/**
 * BasePlayer5.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.GameInfoModifier;
import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Base class for all players in 5 agent village.
 * 
 * @author otsuki
 */
public class BasePlayer5 extends BasePlayer {

	private Map<Agent, Role> forwardEstimateMap = new HashMap<>();
	private Map<Agent, Double> rateMap = new HashMap<>();

	/**
	 * Constructs BasePlayer5 with meta information.
	 * 
	 * @param metaInfo meta information
	 */
	public BasePlayer5(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		getOthers().forEach(a -> rateMap.put(a, getGameCount() == 0 ? 0 : (double) getWinCount(a) / getGameCount()));

		forwardEstimateMap.clear();
		getOthers().forEach(a -> forwardEstimateMap.put(a, Role.ANY));
	}

	/**
	 * Sets the mapping between the agent and its role estimated by this player.
	 * 
	 * @param map
	 */
	protected void setForwardEstimateMap(Map<Agent, Role> map) {
		forwardEstimateMap = map;
	}

	/**
	 * Updates EstimateMap for all roles and all agents.
	 */
	protected void updateEstimateMap() {
		updateEstimateMap(null, null);
	}

	/**
	 * Updates EstimateMap excluding the case that some agents' specified role.
	 * 
	 * @param excludedRole   the role to be excluded
	 * @param excludedAgents the agents to be excluded
	 */
	protected void updateEstimateMap(Role excludedRole, List<Agent> excludedAgents) {
		Map<Role, List<Agent>> candidatesMap = new HashMap<>();
		for (Role r : Arrays.asList(Role.WEREWOLF, Role.VILLAGER, Role.SEER, Role.POSSESSED)) {
			if (r != excludedRole) {
				candidatesMap.put(r, getOthers());
			} else {
				candidatesMap.put(r, getOthers().stream()
						.filter(a -> excludedAgents != null && !excludedAgents.contains(a)).collect(Collectors.toList()));
			}
		}
		double bestValue = 0;
		switch (getMyRole()) {
		case WEREWOLF:
			for (Agent seer : candidatesMap.get(Role.SEER)) {
				for (Agent possessed : candidatesMap.get(Role.POSSESSED).stream().filter(a -> a != seer).collect(Collectors.toList())) {
					Map<Agent, Role> estimateMap = new HashMap<>();
					getOthers().forEach(a -> estimateMap.put(a, Role.VILLAGER));
					estimateMap.put(seer, Role.SEER);
					estimateMap.put(possessed, Role.POSSESSED);
					double value = estimateMap.keySet().stream().mapToDouble(a -> getProbOf(a, estimateMap.get(a))).sum();
					if (value > bestValue) {
						bestValue = value;
						setForwardEstimateMap(estimateMap);
					}
				}
			}
			break;
		case VILLAGER:
			for (Agent seer : candidatesMap.get(Role.SEER)) {
				for (Agent werewolf : candidatesMap.get(Role.WEREWOLF).stream().filter(a -> a != seer).collect(Collectors.toList())) {
					for (Agent possessed : candidatesMap.get(Role.POSSESSED).stream().filter(a -> a != seer && a != werewolf).collect(Collectors.toList())) {
						Map<Agent, Role> estimateMap = new HashMap<>();
						getOthers().forEach(a -> estimateMap.put(a, Role.VILLAGER));
						estimateMap.put(seer, Role.SEER);
						estimateMap.put(werewolf, Role.WEREWOLF);
						estimateMap.put(possessed, Role.POSSESSED);
						double value = estimateMap.keySet().stream().mapToDouble(a -> getProbOf(a, estimateMap.get(a))).sum();
						if (value > bestValue) {
							bestValue = value;
							setForwardEstimateMap(estimateMap);
						}
					}
				}
			}
			break;
		case SEER:
			for (Agent werewolf : candidatesMap.get(Role.WEREWOLF)) {
				for (Agent possessed : candidatesMap.get(Role.POSSESSED).stream().filter(a -> a != werewolf).collect(Collectors.toList())) {
					Map<Agent, Role> estimateMap = new HashMap<>();
					getOthers().forEach(a -> estimateMap.put(a, Role.VILLAGER));
					estimateMap.put(werewolf, Role.WEREWOLF);
					estimateMap.put(possessed, Role.POSSESSED);
					double value = estimateMap.keySet().stream().mapToDouble(a -> getProbOf(a, estimateMap.get(a))).sum();
					if (value > bestValue) {
						bestValue = value;
						setForwardEstimateMap(estimateMap);
					}
				}
			}
			break;
		case POSSESSED:
			for (Agent seer : candidatesMap.get(Role.SEER)) {
				for (Agent werewolf : candidatesMap.get(Role.WEREWOLF).stream().filter(a -> a != seer).collect(Collectors.toList())) {
					Map<Agent, Role> estimateMap = new HashMap<>();
					getOthers().forEach(a -> estimateMap.put(a, Role.VILLAGER));
					estimateMap.put(seer, Role.SEER);
					estimateMap.put(werewolf, Role.WEREWOLF);
					double value = estimateMap.keySet().stream().mapToDouble(a -> getProbOf(a, estimateMap.get(a))).sum();
					if (value > bestValue) {
						bestValue = value;
						setForwardEstimateMap(estimateMap);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the role of the agent estimated by this player.
	 * 
	 * @param the agent
	 * @return the estimated role
	 */
	protected Role getEstimateOf(Agent agent) {
		updateEstimateMap();
		return forwardEstimateMap.get(agent);
	}

	/**
	 * Returns the list of agents estimated to be the role.
	 * 
	 * @param role the role
	 * @return the list of agents
	 */
	protected List<Agent> getEstimateOf(Role role) {
		getEstimateNotHaving(role);
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	/**
	 * Returns the list of agents estimated to be the role, excluding specified agents.
	 * 
	 * @param role           the role
	 * @param excludedAgents the agents to be excluded from the result
	 * @return the list of agents
	 */
	protected List<Agent> getEstimateNotHaving(Role role, Agent... excludedAgents) {
		updateEstimateMap(role, Arrays.asList(excludedAgents));
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	/**
	 * Returns the list of limited agents estimated to be the role.
	 * 
	 * @param role          the role
	 * @param limitedAgents the limited agents
	 * @return
	 */
	protected List<Agent> getEstimateLimitting(Role role, Agent... limitedAgents) {
		return getEstimateLimitting(role, Arrays.asList(limitedAgents));
	}

	/**
	 * Returns the list of limited agents estimated to be the role.
	 * 
	 * @param role          the role
	 * @param limitedAgents the list of limited agents
	 * @return
	 */
	protected List<Agent> getEstimateLimitting(Role role, List<Agent> limitedAgents) {
		List<Agent> excludedAgents = getOthers();
		excludedAgents.removeAll(limitedAgents);
		updateEstimateMap(role, excludedAgents);
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	/**
	 * Returns the winning rate of the agent.
	 * 
	 * @param agent the agent
	 * @return the winning rate between 0.0 and 1.0
	 */
	protected double getRateOf(Agent agent) {
		return rateMap.get(agent);
	}

	/**
	 * Returns the fake game information containing this player's fake role and fake divination.
	 * 
	 * @param gameInfo       the original game information
	 * @param fakeRole       this player's fake role, null if no fake
	 * @param fakeDivination this player's fake divination, null if no fake
	 * @return the fake game informaion
	 */
	protected GameInfo getFakeGameInfo(GameInfo gameInfo, Role fakeRole, Judge fakeDivination) {
		return new GameInfoModifier(gameInfo).setFakeRole(fakeRole).setDivineResult(fakeDivination).toGameInfo();
	}

}
