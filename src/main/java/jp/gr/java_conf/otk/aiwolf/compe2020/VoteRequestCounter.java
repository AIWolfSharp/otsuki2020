/**
 * VoteRequestCounter.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;

/**
 * Counts requests for vote.
 * 
 * @author otsuki
 */
@SuppressWarnings("serial")
public class VoteRequestCounter extends ArrayList<Content> {

	private Map<Agent, Agent> agentRequestMap = new HashMap<>();

	private Map<Agent, Integer> agentCountMap = new HashMap<>();

	private List<Agent> orderedAgentList;

	private boolean isChanged = false;

	private void count() {
		agentCountMap.clear();
		List<Agent> votedAgents = agentRequestMap.values().stream().distinct().collect(Collectors.toList());
		for (Agent agent : votedAgents) {
			int count = (int) agentRequestMap.values().stream().filter(a -> a == agent).count();
			agentCountMap.put(agent, count);
		}
	}

	@Override
	public boolean add(Content content) {
		Agent requester = content.getSubject();
		if (content.getOperator() == Operator.REQUEST) {
			Content c = content.getContentList().get(0);
			if (c != null && c.getTopic() == Topic.VOTE) {
				super.add(content);
				Agent voted = c.getTarget();
				if (!agentRequestMap.containsKey(requester) || agentRequestMap.get(requester) != voted) {
					isChanged = true;
					agentRequestMap.put(requester, voted);
					count();
					orderedAgentList = agentCountMap.keySet().stream()
							.sorted((a1, a2) -> getCount(a2) - getCount(a1)).collect(Collectors.toList());
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the map between the requesting agent and the requested agent.
	 * 
	 * @return the map between a agent and the other agent
	 */
	public Map<Agent, Agent> getRequestMap() {
		return agentRequestMap;
	}

	/**
	 * Returns the number of votes for the agent.
	 * 
	 * @param the agent
	 * @return the number of votes for the agent
	 */
	public int getCount(Agent agent) {
		return agentCountMap.containsKey(agent) ? agentCountMap.get(agent) : 0;
	}

	/**
	 * Returns the list of agents sorted by the number of votes in descending order.
	 * 
	 * @return the sorted list of agents
	 */
	public List<Agent> getOrderedAgentList() {
		return orderedAgentList;
	}

	/**
	 * Returns the list of agents that get the maximum number of votes.
	 * 
	 * @return the list of agents having the maximum number of votes
	 */
	public List<Agent> getTopAgentList() {
		if (orderedAgentList != null && !orderedAgentList.isEmpty()) {
			int max = getCount(orderedAgentList.get(0));
			return orderedAgentList.stream().filter(a -> getCount(a) == max).collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * Returns whether or not there is modification since last call of this method.
	 * 
	 * @return true if there is modification
	 */
	public boolean isChanged() {
		return isChanged && !(isChanged = false);
	}

	@Override
	public void clear() {
		super.clear();
		agentRequestMap.clear();
		agentCountMap.clear();
		if (orderedAgentList != null) {
			orderedAgentList.clear();
		}
	}

}
