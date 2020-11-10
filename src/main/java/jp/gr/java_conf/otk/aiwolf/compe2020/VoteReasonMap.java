/**
 * VoteReasonMap.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import static java.util.Comparator.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;

/**
 * A mapping between agent and its declared vote with reason.
 * 
 * @author otsuki
 */
public class VoteReasonMap extends HashMap<Agent, Entry<Agent, Content>> {

	private static final long serialVersionUID = -1706725487037832992L;
	private Map<Agent, Integer> voteCountMap = new HashMap<>();
	private boolean isChanged = false;

	private void countVote() {
		voteCountMap.clear();
		keySet().stream().map(voter -> get(voter).getKey()).distinct().forEach(voted -> {
			voteCountMap.put(voted, (int) keySet().stream().filter(a -> get(a).getKey() == voted).count());
		});
	}

	/**
	 * Returns whether or not there is modification since last call of this method.
	 * 
	 * @return true if there is any modification
	 */
	public boolean isChanged() {
		return isChanged && !(isChanged = false);
	}

	/**
	 * Associates vote target with voter, and reason with vote if any.
	 * 
	 * @param voter  the agent declaring voting will
	 * @param voted  the agent voted by voter
	 * @param reason the reason for voting
	 * @return true on success
	 */
	public boolean put(Agent voter, Agent voted, Content reason) {
		if (voter == null) {
			return false;
		}
		if (voted == null) {
			remove(voter);
			countVote();
			return true;
		}
		Entry<Agent, Content> lastEntry = get(voter);
		if (lastEntry == null || lastEntry.getKey() != voted) {
			isChanged = true;
		}
		put(voter, new SimpleEntry<Agent, Content>(voted, reason));
		countVote();
		return true;
	}

	/**
	 * Associates vote target with voter.
	 * 
	 * @param voter the agent declaring voting will
	 * @param voted the agent voted by voter
	 * @return true on success
	 */
	public boolean put(Agent voter, Agent voted) {
		return put(voter, voted, null);
	}

	/**
	 * Deletes the voter's association.
	 * 
	 * @param voter the voter
	 * @return true on success
	 */
	public boolean cancel(Agent voter) {
		return put(voter, null, null);
	}

	/**
	 * Associates the utterance of vote with the reason.
	 * 
	 * @param vote   the utterance of vote
	 * @param reason the reason of the vote
	 * @return true on success
	 */
	public boolean put(Content vote, Content reason) {
		if (vote.getTopic() == Topic.VOTE) {
			Agent voter = vote.getSubject();
			Agent voted = vote.getTarget();
			return put(voter, voted, reason);
		}
		return false;
	}

	/**
	 * Registers the utterance concerning vote, and associates the reason with the vote if any.
	 * 
	 * @param content the utterance concerning vote
	 * @return true on success
	 */
	public boolean put(Content content) {
		if (content.getTopic() == Topic.VOTE) {
			return put(content, null);
		} else if (content.getOperator() == Operator.BECAUSE && content.getContentList().get(1).getTopic() == Topic.VOTE) {
			return put(content.getContentList().get(1), content.getContentList().get(0));
		}
		return false;
	}

	/**
	 * Returns the number of votes the voted agent got.
	 * 
	 * @param voted the voted agent
	 * @return the number of votes
	 */
	public int getVoteCount(Agent voted) {
		return voteCountMap.getOrDefault(voted, 0);
	}

	/**
	 * Returns the total number of votes.
	 * 
	 * @return the total number of votes
	 */
	public int getVoteCount() {
		return voteCountMap.keySet().stream().mapToInt(a -> getVoteCount(a)).sum();
	}

	/**
	 * Returns a list of agents sorted in descending order of vote count.
	 * 
	 * @param excludes a list of agents to exclude from the list to be returned
	 * @return a list of agents
	 */
	public List<Agent> getOrderedList(List<Agent> excludes) {
		return voteCountMap.keySet().stream().filter(a -> !excludes.contains(a))
				.sorted(comparing(a -> getVoteCount(a), reverseOrder())).collect(Collectors.toList());
	}

	/**
	 * Returns a list of agents sorted in descending order of vote count.
	 * 
	 * @param excludes agents to exclude from the list to be returned
	 * @return a list of agents
	 */
	public List<Agent> getOrderedList(Agent... excludes) {
		return getOrderedList(Arrays.asList(excludes));
	}

	/**
	 * Returns the agent voted by the voter.
	 * 
	 * @param voter the voter
	 * @return the voted agent
	 */
	public Agent getTarget(Agent voter) {
		return containsKey(voter) ? get(voter).getKey() : null;
	}

	/**
	 * Returns the reason of the voter's vote.
	 * 
	 * @param voter the voter
	 * @return the reason of the voter's vote
	 */
	public Content getReason(Agent voter) {
		return containsKey(voter) ? get(voter).getValue() : null;
	}

	/**
	 * Returns the reason why the voter votes for the voted.
	 * 
	 * @param voter the voter
	 * @param voted the voted agent
	 * @return the reason
	 */
	public Content getReason(Agent voter, Agent voted) {
		return getTarget(voter) == voted ? getReason(voted) : null;
	}

	@Override
	public void clear() {
		super.clear();
		voteCountMap.clear();
	}

	/**
	 * Returns the agent which gets the maximum number of votes.
	 * 
	 * @param excludes agents to exclude from result
	 * @return the top winning agent
	 */
	public Agent getTop(Agent... excludes) {
		return voteCountMap.keySet().stream().filter(a -> !Arrays.asList(excludes).contains(a))
				.max(comparing(a -> getVoteCount(a))).orElse(null);
	}

	/**
	 * Returns a list of agents that get the maximum number of votes.
	 * 
	 * @return a list of agents
	 */
	public List<Agent> getWinners() {
		List<Agent> agents = values().stream().map(e -> e.getKey()).distinct().collect(Collectors.toList());
		int max = agents.stream().map(a -> getVoteCount(a)).max(Comparator.naturalOrder()).orElse(0);
		return agents.stream().filter(a -> getVoteCount(a) == max).collect(Collectors.toList());
	}

	/**
	 * Return whether or not the agent is a winner.
	 * 
	 * @param agent the agent
	 * @return true if the agent is a winner
	 */
	public boolean isWinner(Agent agent) {
		return getWinners().contains(agent);
	}

}
