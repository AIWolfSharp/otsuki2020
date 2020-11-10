/**
 * Feature.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import static java.util.Comparator.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.data.Vote;
import org.aiwolf.common.net.GameInfo;
import org.nd4j.common.util.ArrayUtil;

public class Feature {

	// role-related constants
	public static final int NUM_ROLES = 6;
	public static final Map<Role, Integer> roleIntMap = Map.of(Role.WEREWOLF, 0, Role.VILLAGER, 1, Role.SEER, 2, Role.POSSESSED, 3, Role.MEDIUM, 4, Role.BODYGUARD, 5);
	public static final Map<Role, double[]> roleVectorMap = Map.of(
			Role.ANY, new double[] { 0, 0, 0, 0, 0, 0 },
			Role.WEREWOLF, new double[] { 1, 0, 0, 0, 0, 0 },
			Role.VILLAGER, new double[] { 0, 1, 0, 0, 0, 0 },
			Role.SEER, new double[] { 0, 0, 1, 0, 0, 0 },
			Role.POSSESSED, new double[] { 0, 0, 0, 1, 0, 0 },
			Role.MEDIUM, new double[] { 0, 0, 0, 0, 1, 0 },
			Role.BODYGUARD, new double[] { 0, 0, 0, 0, 0, 1 });

	// constants related to the features of each agent
	public static final int NUM_AGENT_FEATURE = 18;
	// [0] 1 if dead
	public static final int IS_DEAD = 0;
	// [1] the number of human judges received
	public static final int NUM_JUDGED_AS_WHITE = 1;
	// [2] the number of werewolf judges received
	public static final int NUM_JUDGED_AS_BLACK = 2;
	// [3] 1 if CO werewolf
	public static final int CO_WEREWOLF = 3;
	// [4] 1 if CO villager
	public static final int CO_VILLAGER = 4;
	// [5] 1 if CO seer
	public static final int CO_SEER = 5;
	// [6] 1 if CO possessed human
	public static final int CO_POSSESSED = 6;
	// [7] 1 if CO medium
	public static final int CO_MEDIUM = 7;
	// [8] 1 if CO bodyguard
	public static final int CO_BODYGUARD = 8;
	// [9] the number of human judges reported
	public static final int NUM_WHITE_JUDGEMENT = 9;
	// [10] the number of werewolf judges reported
	public static final int NUM_BLACK_JUDGEMENT = 10;
	// [11] the number of votes different from voting declaration
	public static final int NUM_CHANGE_VOTE = 11;
	// [12] 1 if enemy
	public static final int IS_ENEMY = 12;
	// [13] 1 if ally
	public static final int IS_ALLY = 13;
	// [14] 1 if human
	public static final int IS_HUMAN = 14;
	// [15] 1 if werewolf
	public static final int IS_WEREWOLF = 15;
	// [16] 1 if executed
	public static final int IS_EXECUTED = 16;
	// [17] 1 if killed
	public static final int IS_KILLED = 17;

	// utterance-related constants
	public static final int NUM_TOPICS = 7;
	public static final int NUM_TURNS = 10;
	public static final int UT_SKIP = 0;
	public static final int UT_VOTE = 1;
	public static final int UT_ESTIMATE = 2;
	public static final int UT_CO = 3;
	public static final int UT_DIVINED = 4;
	public static final int UT_IDENTIFIED = 5;
	public static final int UT_OPERATOR = 6;

	public static final Map<Topic, Integer> topicIntMap = Map.of(
			Topic.SKIP, Feature.UT_SKIP,
			Topic.OVER, Feature.UT_SKIP,
			Topic.VOTE, Feature.UT_VOTE,
			Topic.ESTIMATE, Feature.UT_ESTIMATE,
			Topic.COMINGOUT, Feature.UT_CO,
			Topic.DIVINED, Feature.UT_DIVINED,
			Topic.IDENTIFIED, Feature.UT_IDENTIFIED,
			Topic.OPERATOR, Feature.UT_OPERATOR);

	private Map<Agent, AgentFeature> featureMap = new HashMap<>();

	private Agent me;
	private Role myRole;
	private int date = -1;
	private int talkListHead;
	private boolean isModified;

	// divine result
	private Map<Agent, Map<Agent, Species>> divinationMap = new HashMap<>();
	// medium result
	private Map<Agent, Map<Agent, Species>> identMap = new HashMap<>();
	// confirmed information
	private Map<Agent, Species> speciesMap = new HashMap<>();
	// voting declaration
	private Map<Agent, Vote> votingMap = new HashMap<>();
	// CO
	private Map<Agent, Role> comingoutMap = new HashMap<>();
	// estimate
	private Map<Agent, Map<Agent, Role>> estimateMap = new HashMap<>();
	// utterance statistics
	private Map<Agent, UtteranceStatistics> statMap = new HashMap<>();

	public Feature(GameInfo gameInfo) {
		me = gameInfo.getAgent();
		myRole = gameInfo.getRole();
		for (Agent a : gameInfo.getAgentList()) {
			featureMap.put(a, new AgentFeature(NUM_AGENT_FEATURE));
			divinationMap.put(a, new HashMap<Agent, Species>());
			identMap.put(a, new HashMap<Agent, Species>());
			estimateMap.put(a, new HashMap<Agent, Role>());
			Role hisRole = gameInfo.getRoleMap().getOrDefault(a, Role.ANY);
			if (myRole == Role.WEREWOLF) {
				if (hisRole == Role.WEREWOLF) {
					speciesMap.put(a, Species.WEREWOLF);
				} else {
					speciesMap.put(a, Species.HUMAN);
				}
			} else {
				speciesMap.put(a, hisRole.getSpecies());
			}
			statMap.put(a, new UtteranceStatistics());
		}
		isModified = true;
	}

	/**
	 * update of features
	 * 
	 * @param gameInfo
	 * @param isDebug  true if output unparsable utterance to stderr
	 * @return false if failed to update
	 */
	public boolean update(GameInfo gameInfo, boolean isDebug) {
		if (gameInfo.getDay() < date || gameInfo.getStatusMap().get(me) == Status.DEAD) {
			return false;
		}

		Map<Agent, AgentFeature> oldFeatureMap = new HashMap<>();
		featureMap.keySet().stream().forEach(a -> oldFeatureMap.put(a, featureMap.get(a).clone()));

		if (gameInfo.getDay() > date) {
			date = gameInfo.getDay();
			for (Agent a : gameInfo.getAgentList()) {
				featureMap.get(a).set(IS_DEAD, gameInfo.getStatusMap().get(a) == Status.ALIVE ? 0 : 1);
			}
			for (Vote vote : gameInfo.getVoteList()) {
				Agent voter = vote.getAgent();
				if (votingMap.containsKey(voter) && vote.getDay() == votingMap.get(voter).getDay() && vote.getTarget() != votingMap.get(voter).getTarget()) {
					featureMap.get(voter).inc(NUM_CHANGE_VOTE);
				}
			}
			if (gameInfo.getDivineResult() != null) {
				speciesMap.put(gameInfo.getDivineResult().getTarget(), gameInfo.getDivineResult().getResult());
			}
			if (gameInfo.getMediumResult() != null) {
				speciesMap.put(gameInfo.getMediumResult().getTarget(), gameInfo.getMediumResult().getResult());
			}
			if (gameInfo.getExecutedAgent() != null) {
				featureMap.get(gameInfo.getExecutedAgent()).set(IS_EXECUTED, 1);
			}
			gameInfo.getLastDeadAgentList().stream().forEach(a -> {
				featureMap.get(a).set(IS_KILLED, 1);
				speciesMap.put(a, Species.HUMAN);
			});
			talkListHead = 0;
			votingMap.clear();
		}

		for (int i = talkListHead; i < gameInfo.getTalkList().size(); i++) {
			Talk talk = gameInfo.getTalkList().get(i);
			Content content = new Content(talk);
			if (!parseSentence(content) && isDebug) {
				System.err.println(content);
			}
		}
		talkListHead = gameInfo.getTalkList().size();

		for (Agent a : gameInfo.getAgentList()) {
			featureMap.get(a).set(NUM_JUDGED_AS_WHITE, 0);
			featureMap.get(a).set(NUM_JUDGED_AS_BLACK, 0);
			featureMap.get(a).set(NUM_WHITE_JUDGEMENT, 0);
			featureMap.get(a).set(NUM_BLACK_JUDGEMENT, 0);
		}
		for (Agent agent : divinationMap.keySet()) {
			for (Agent target : divinationMap.get(agent).keySet()) {
				Species result = divinationMap.get(agent).get(target);
				if (result == Species.HUMAN) {
					featureMap.get(target).inc(NUM_JUDGED_AS_WHITE);
					featureMap.get(agent).inc(NUM_WHITE_JUDGEMENT);
					if (target == me) {
						featureMap.get(agent).set(IS_ALLY, 1);
					}
					if (speciesMap.get(target) == Species.WEREWOLF) {
						featureMap.get(agent).set(IS_ENEMY, 1);
					}
				} else if (result == Species.WEREWOLF) {
					featureMap.get(target).inc(NUM_JUDGED_AS_BLACK);
					featureMap.get(agent).inc(NUM_BLACK_JUDGEMENT);
					if (target == me) {
						featureMap.get(agent).set(IS_ENEMY, 1);
					}
					if (speciesMap.get(target) == Species.HUMAN) {
						featureMap.get(agent).set(IS_ENEMY, 1);
					}
				}
			}
		}
		for (Agent a : identMap.keySet()) {
			for (Agent target : identMap.get(a).keySet()) {
				Species result = identMap.get(a).get(target);
				if ((result == Species.WEREWOLF && speciesMap.get(target) == Species.HUMAN) || (result == Species.HUMAN && speciesMap.get(target) == Species.WEREWOLF)) {
					featureMap.get(a).set(IS_ENEMY, 1);
				}
			}
		}
		for (Agent a : speciesMap.keySet()) {
			if (speciesMap.get(a) == Species.HUMAN) {
				featureMap.get(a).set(IS_HUMAN, 1);
			} else if (speciesMap.get(a) == Species.WEREWOLF) {
				featureMap.get(a).set(IS_WEREWOLF, 1);
			}
		}

		isModified = isModified || featureMap.entrySet().stream().anyMatch(e -> !e.getValue().equals(oldFeatureMap.get(e.getKey())));
		return true;
	}

	private void updateStatMap(Content content) {
		if (content.getTalkDate() != 1) {
			return;
		}
		isModified = statMap.get(content.getTalker()).set(content.getTurn(), content.getTopic(), 1);
	}

	private boolean parseSentence(Content content) {
		if (content.getTopic() == Topic.SKIP || content.getTopic() == Topic.OVER) {
			updateStatMap(content);
			return true;
		}
		return addCo(content) || addDivined(content) || addIdentified(content) || addVote(content) || addEstimate(content)
				|| parseOperator(content);
	}

	private boolean parseOperator(Content content) {
		if (content.getTopic() == Topic.OPERATOR) {
			updateStatMap(content);
			return parseBecause(content) || parseDay(content) || parseAnd(content) || parseOr(content) || parseXor(content) || parseRequset(content) || parseInquire(content);
		}
		return false;
	}

	private boolean parseBecause(Content content) {
		if (content.getOperator() == Operator.BECAUSE) {
			Talk subTalk = new Talk(content.getIndex(), content.getTalkDate(), content.getTurn(), content.getTalker(), content.getContentList().get(1).getText());
			return parseSentence(new Content(subTalk));
		}
		return false;
	}

	private boolean parseDay(Content content) {
		if (content.getOperator() == Operator.DAY) {
			Talk subTalk = new Talk(content.getIndex(), content.getTalkDate(), content.getTurn(), content.getTalker(), content.getContentList().get(0).getText());
			return parseSentence(new Content(subTalk));
		}
		return false;
	}

	private boolean parseAnd(Content content) {
		if (content.getOperator() == Operator.AND) {
			return content.getContentList().stream()
					.allMatch(c -> parseSentence(new Content(new Talk(content.getIndex(), content.getTalkDate(), content.getTurn(), content.getTalker(), c.getText()))));
		}
		return false;
	}

	private boolean parseOr(Content content) {
		if (content.getOperator() == Operator.OR) {
			return content.getContentList().stream().allMatch(c -> c.getTopic() == Topic.VOTE
					&& parseSentence(new Content(new Talk(content.getIndex(), content.getTalkDate(), content.getTurn(), content.getTalker(), c.getText()))));
		}
		return false;
	}

	private boolean parseXor(Content content) {
		if (content.getOperator() == Operator.XOR) {
			return content.getContentList().stream().allMatch(c -> (c.getTopic() == Topic.VOTE || c.getTopic() == Topic.ESTIMATE)
					&& parseSentence(new Content(new Talk(content.getIndex(), content.getTalkDate(), content.getTurn(), content.getTalker(), c.getText()))));
		}
		return false;
	}

	private boolean parseRequset(Content content) {
		if (content.getOperator() == Operator.REQUEST) {
			return true;
		}
		return false;
	}

	private boolean parseInquire(Content content) {
		if (content.getOperator() == Operator.INQUIRE) {
			return true;
		}
		return false;
	}

	public double[] getFeatureArrayOf(Agent agent) {
		List<double[]> dList = new ArrayList<>();
		dList.add(new double[] { date });
		dList.add(roleVectorMap.get(myRole));
		dList.add(featureMap.get(me).getArray());
		dList.add(featureMap.get(agent).getArray());
		List<AgentFeature> agentFeatureList = new ArrayList<>();
		for (Agent a : featureMap.keySet()) {
			if (a != me && a != agent) {
				agentFeatureList.add(featureMap.get(a));
			}
		}
		for (AgentFeature f : agentFeatureList.stream().sorted(comparing(AgentFeature::getSum).thenComparing(AgentFeature::getBin)).collect(Collectors.toList())) {
			dList.add(f.getArray());
		}
		return ArrayUtil.combineDouble(dList);
	}

	public double[] getUtterancePatternOf(Agent agent) {
		return statMap.get(agent).getAbsoluteVerctor();
	}

	public boolean isModified() {
		return isModified;
	}

	private boolean addCo(Content content) {
		if (content.getTopic() == Topic.COMINGOUT && content.getSubject() == content.getTarget()) {
			updateStatMap(content);
			addCo(content.getSubject(), content.getRole());
			return true;
		}
		return false;
	}

	private void addCo(Agent talker, Role coRole) {
		comingoutMap.put(talker, coRole);
		switch (coRole) {
		case WEREWOLF:
			featureMap.get(talker).set(CO_WEREWOLF, 1);
			break;
		case VILLAGER:
			featureMap.get(talker).set(CO_VILLAGER, 1);
			break;
		case SEER:
			featureMap.get(talker).set(CO_SEER, 1);
			if (myRole == Role.SEER && talker != me) {
				featureMap.get(talker).set(IS_ENEMY, 1);
			}
			break;
		case POSSESSED:
			featureMap.get(talker).set(CO_POSSESSED, 1);
			break;
		case MEDIUM:
			featureMap.get(talker).set(CO_MEDIUM, 1);
			if (myRole == Role.MEDIUM && talker != me) {
				featureMap.get(talker).set(IS_ENEMY, 1);
			}
			break;
		case BODYGUARD:
			featureMap.get(talker).set(CO_BODYGUARD, 1);
			break;
		default:
			break;
		}
	}

	private boolean addDivined(Content content) {
		if (content.getTopic() == Topic.DIVINED) {
			updateStatMap(content);
			addDivined(content.getSubject(), content.getTarget(), content.getResult());
			return true;
		}
		return false;
	}

	private void addDivined(Agent talker, Agent target, Species result) {
		addCo(talker, Role.SEER);
		divinationMap.get(talker).put(target, result);
	}

	private boolean addIdentified(Content content) {
		if (content.getTopic() == Topic.IDENTIFIED) {
			updateStatMap(content);
			addIdentified(content.getSubject(), content.getTarget(), content.getResult());
			return true;
		}
		return false;
	}

	private void addIdentified(Agent talker, Agent target, Species result) {
		addCo(talker, Role.MEDIUM);
		identMap.get(talker).put(target, result);
	}

	private boolean addVote(Content content) {
		if (content.getTopic() == Topic.VOTE) {
			updateStatMap(content);
			votingMap.put(content.getSubject(), new Vote(date, content.getSubject(), content.getTarget()));
			return true;
		}
		return false;
	}

	private boolean addEstimate(Content content) {
		if (content.getTopic() == Topic.ESTIMATE) {
			updateStatMap(content);
			estimateMap.get(content.getSubject()).put(content.getTarget(), content.getRole());
			return true;
		}
		return false;
	}

}
