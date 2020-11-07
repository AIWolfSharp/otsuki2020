/**
 * VoteReasonMap.java
 * Copyright (c) 2020 OTSUKI Takashi
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
 * the declared vote and its reason
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
	 * true if there is modification since last call of this method
	 */
	public boolean isChanged() {
		return isChanged && !(isChanged = false);
	}

	/**
	 * 
	 * @param voter
	 * @param voted
	 * @param reason
	 * @return
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
	 * 
	 * @param voter
	 * @param voted
	 * @return
	 */
	public boolean put(Agent voter, Agent voted) {
		return put(voter, voted, null);
	}

	/**
	 * 
	 * @param voter
	 * @param voted
	 * @return
	 */
	public boolean cancel(Agent voter) {
		return put(voter, null, null);
	}

	/**
	 * 
	 * @param vote
	 * @param reason
	 * @return
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
	 * 
	 * @param content
	 * @return
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
	 * 
	 * @param voted
	 * @return
	 */
	public int getVoteCount(Agent voted) {
		return voteCountMap.getOrDefault(voted, 0);
	}

	/**
	 * 
	 * @return
	 */
	public int getVoteCount() {
		return voteCountMap.keySet().stream().mapToInt(a -> getVoteCount(a)).sum();
	}

	/**
	 * 
	 * @return
	 */
	public List<Agent> getOrderedList(List<Agent> excludes) {
		return voteCountMap.keySet().stream().filter(a -> !excludes.contains(a))
				.sorted(comparing(a -> getVoteCount(a), reverseOrder())).collect(Collectors.toList());
	}

	/**
	 * 
	 * @return
	 */
	public List<Agent> getOrderedList(Agent... excludes) {
		return getOrderedList(Arrays.asList(excludes));
	}

	/**
	 * 
	 * @param voter
	 * @return
	 */
	public Agent getTarget(Agent voter) {
		return containsKey(voter) ? get(voter).getKey() : null;
	}

	/**
	 * 
	 * @param voter
	 * @param voted
	 * @return
	 */
	public Content getReason(Agent voter) {
		return containsKey(voter) ? get(voter).getValue() : null;
	}

	/**
	 * 
	 * @param voter
	 * @param voted
	 * @return
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
	 * 
	 * @param excludes
	 * @return
	 */
	public Agent getTop(Agent... excludes) {
		return voteCountMap.keySet().stream().filter(a -> !Arrays.asList(excludes).contains(a))
				.max(comparing(a -> getVoteCount(a))).orElse(null);
	}

	/**
	 * 
	 * @param voteMap
	 * @return
	 */
	public List<Agent> getWinners() {
		List<Agent> agents = values().stream().map(e -> e.getKey()).distinct().collect(Collectors.toList());
		int max = agents.stream().map(a -> getVoteCount(a)).max(Comparator.naturalOrder()).orElse(0);
		return agents.stream().filter(a -> getVoteCount(a) == max).collect(Collectors.toList());
	}

	/**
	 * 
	 * @param agent
	 * @return
	 */
	public boolean isWinner(Agent agent) {
		return getWinners().contains(agent);
	}

}
