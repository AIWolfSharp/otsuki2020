/**
 * MetaInfo.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;

/**
 * meta information
 * 
 * @author otsuki
 *
 */
public class MetaInfo {

	private int gameCount;

	private Map<Role, int[]> roleCountMap = new HashMap<>();

	private Map<Agent, int[]> winCountMap = new HashMap<>();

	private Map<Agent, Boolean> ppAbility = new HashMap<>();

	private Map<Role, RoleEstimator> estimatorMap = new HashMap<>();

	public MetaInfo() {
		for (Role role : Arrays.asList(Role.WEREWOLF, Role.VILLAGER, Role.SEER, Role.POSSESSED, Role.MEDIUM, Role.BODYGUARD)) {
			estimatorMap.put(role, new RoleEstimator(role));
			roleCountMap.put(role, new int[] { 0 });
		}
	}

	/**
	 * 
	 * @param gameInfo
	 */
	public void initialize(GameInfo gameInfo) {
		gameInfo.getExistingRoles().forEach(r -> getRoleEstimator(r).initialize(gameInfo));
	}

	/**
	 * 
	 * @param gameInfo
	 */
	public void update(GameInfo gameInfo) {
		gameInfo.getExistingRoles().forEach(r -> getRoleEstimator(r).update(gameInfo));
	}

	/**
	 * 
	 * @param agent
	 * @return
	 */
	public boolean getPpAbility(Agent agent) {
		if (!ppAbility.containsKey(agent)) {
			ppAbility.put(agent, false);
		}
		return ppAbility.get(agent);
	}

	/**
	 * 
	 * @param agent
	 * @param ability
	 */
	public void setPpAbility(Agent agent, boolean ability) {
		if (getPpAbility(agent) != ability) {
			ppAbility.put(agent, ability);
		}
	}

	/**
	 * 
	 * @param role
	 * @return
	 */
	public int getRoleCount(Role role) {
		return roleCountMap.get(role)[0];
	}

	/**
	 * 
	 * @param role
	 */
	public void incrementRoleCount(Role role) {
		roleCountMap.get(role)[0]++;
	}

	/**
	 * 
	 * @param role
	 * @return
	 */
	public RoleEstimator getRoleEstimator(Role role) {
		return estimatorMap.get(role);
	}

	/**
	 * @return the gameCount
	 */
	public int getGamecount() {
		return gameCount;
	}

	/**
	 * 
	 */
	public void finish(GameInfo gameInfo) {
		gameInfo.getExistingRoles().forEach(r -> getRoleEstimator(r).finish());
		gameCount++;
	}

	/**
	 * @return the winCount
	 */
	public int getWinCount(Agent agent) {
		if (agent == null) {
			return 0;
		}
		if (!winCountMap.containsKey(agent)) {
			winCountMap.put(agent, new int[] { 0 });
		}
		return winCountMap.get(agent)[0];
	}

	/**
	 * Increments winCount of the agent.
	 */
	public void incrementWinCount(Agent agent) {
		if (agent == null) {
			return;
		}
		if (!winCountMap.containsKey(agent)) {
			winCountMap.put(agent, new int[] { 1 });
		} else {
			winCountMap.get(agent)[0]++;
		}
	}

}
