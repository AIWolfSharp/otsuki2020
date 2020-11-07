/**
 * BasePlayer5.java
 * Copyright (c) 2020 OTSUKI Takashi
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
 * base class for 5 agent village
 * 
 * @author otsuki
 */
public class BasePlayer5 extends BasePlayer {

	private Map<Agent, Role> forwardEstimateMap = new HashMap<>();
	private Map<Agent, Double> rateMap = new HashMap<>();

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

	protected void setForwardEstimateMap(Map<Agent, Role> map) {
		forwardEstimateMap = map;
	}

	protected void updateEstimateMap() {
		updateEstimateMap(null, null);
	}

	/**
	 * 
	 * @param excludedRole
	 * @param excludedAgents
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
	 * estimated role of the agent
	 * 
	 * @param agent
	 * @return
	 */
	protected Role getEstimateOf(Agent agent) {
		updateEstimateMap();
		return forwardEstimateMap.get(agent);
	}

	/**
	 * the list agents estimated to be role
	 * 
	 * @param role
	 * @return
	 */
	protected List<Agent> getEstimateOf(Role role) {
		getEstimateNotHaving(role);
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	protected List<Agent> getEstimateNotHaving(Role role, Agent... excludedAgents) {
		updateEstimateMap(role, Arrays.asList(excludedAgents));
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	protected List<Agent> getEstimateLimitting(Role role, Agent... limitedAgents) {
		return getEstimateLimitting(role, Arrays.asList(limitedAgents));
	}

	protected List<Agent> getEstimateLimitting(Role role, List<Agent> limitedAgents) {
		List<Agent> excludedAgents = getOthers();
		excludedAgents.removeAll(limitedAgents);
		updateEstimateMap(role, excludedAgents);
		return forwardEstimateMap.keySet().stream().filter(a -> forwardEstimateMap.get(a) == role).collect(Collectors.toList());
	}

	/**
	 * winning rate of the agent
	 * 
	 * @param agent
	 * @return
	 */
	protected double getRateOf(Agent agent) {
		return rateMap.get(agent);
	}

	/**
	 * 
	 * @param gameInfo
	 * @param fakeRole       no modification if null specified
	 * @param fakeDivination no modification if null specified
	 * @return
	 */
	protected GameInfo getFakeGameInfo(GameInfo gameInfo, Role fakeRole, Judge fakeDivination) {
		return new GameInfoModifier(gameInfo).setFakeRole(fakeRole).setDivineResult(fakeDivination).toGameInfo();
	}

}
