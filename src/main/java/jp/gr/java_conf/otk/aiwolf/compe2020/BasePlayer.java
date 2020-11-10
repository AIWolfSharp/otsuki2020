/**
 * BasePlayer.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.AgreeContentBuilder;
import org.aiwolf.client.lib.AndContentBuilder;
import org.aiwolf.client.lib.AttackContentBuilder;
import org.aiwolf.client.lib.AttackedContentBuilder;
import org.aiwolf.client.lib.BecauseContentBuilder;
import org.aiwolf.client.lib.ComingoutContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.DayContentBuilder;
import org.aiwolf.client.lib.DisagreeContentBuilder;
import org.aiwolf.client.lib.DivinationContentBuilder;
import org.aiwolf.client.lib.DivinedResultContentBuilder;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.GuardCandidateContentBuilder;
import org.aiwolf.client.lib.GuardedAgentContentBuilder;
import org.aiwolf.client.lib.IdentContentBuilder;
import org.aiwolf.client.lib.InquiryContentBuilder;
import org.aiwolf.client.lib.NotContentBuilder;
import org.aiwolf.client.lib.OrContentBuilder;
import org.aiwolf.client.lib.RequestContentBuilder;
import org.aiwolf.client.lib.TalkType;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.VoteContentBuilder;
import org.aiwolf.client.lib.VotedContentBuilder;
import org.aiwolf.client.lib.XorContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Status;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Base class for all players.
 * 
 * @author otsuki
 */
public class BasePlayer implements Player {

	/**
	 * True on debug.
	 */
	protected boolean debug = true;

	private MetaInfo metaInfo;

	/**
	 * Constructs BasePlayer with meta information.
	 * 
	 * @param metaInfo meta information
	 */
	public BasePlayer(MetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}

	/**
	 * Returns the agent representing this player.
	 * 
	 * @return the agent representing this player
	 */
	protected Agent getMe() {
		return me;
	}

	private Agent me;

	/**
	 * Returns the role of this player.
	 * 
	 * @return the role of this player
	 */
	protected Role getMyRole() {
		return myRole;
	}

	private Role myRole;

	/**
	 * Returns the date of today.
	 * 
	 * @return date of today
	 */
	protected int getDate() {
		return date;
	}

	private int date;

	/**
	 * Returns the current turn of talk.
	 * 
	 * @return the current turn of talk
	 */
	protected int getTurn() {
		return turn;
	}

	/**
	 * Advances the turn.
	 */
	protected void nextTurn() {
		turn++;
	}

	private int turn;

	/**
	 * Returns the index of the latest talk.
	 * 
	 * @return the index of the latest talk
	 */
	protected int getLastTalkIdx() {
		return lastTalkIdx;
	}

	private int lastTalkIdx;

	/**
	 * True in case of re-vote.
	 */
	protected boolean isRevote;

	/**
	 * True in case of PP.
	 */
	protected boolean isPP;

	/**
	 * Returns current game information.
	 * 
	 * @return current game information
	 */
	protected GameInfo getGameInfo() {
		return gameInfo;
	}

	private GameInfo gameInfo;

	/**
	 * Returns other agents.
	 * 
	 * @return the list of other agents
	 */
	protected List<Agent> getOthers() {
		return getOthers(gameInfo.getAgentList());
	}

	/**
	 * Returns the list of given agents except for this.
	 * 
	 * @param agents the agent list given
	 * @return the list of given agents except for this
	 */
	protected List<Agent> getOthers(Collection<Agent> agents) {
		return agents.stream().filter(a -> a != me).distinct().collect(Collectors.toList());
	}

	/**
	 * Returns alive other agents.
	 * 
	 * @return the list of alive other agents
	 */
	protected List<Agent> getAliveOthers() {
		return getAlive(getOthers());
	}

	private Set<Agent> executedAgents = new HashSet<>();

	/**
	 * Returns whether or not the agent was executed.
	 * 
	 * @param agent the agent
	 * @return true if the agent was executed
	 */
	protected boolean isExecuted(Agent agent) {
		return executedAgents.contains(agent);
	}

	private Set<Agent> killedAgents = new HashSet<>();

	/**
	 * Returns whether or not the agent was killed by werewolf's attack.
	 * 
	 * @param agent the agent
	 * @return true if the agent was killed
	 */
	protected boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	private List<Judge> divinationReports = new ArrayList<>();

	/**
	 * Returns all divination reports reported until now.
	 * 
	 * @return the list of divination results reported until now
	 */
	protected List<Judge> getDivinationReports() {
		return Collections.unmodifiableList(divinationReports);
	}

	private Map<Agent, Judge> divinationMap = new HashMap<>();

	/**
	 * Returns the latest divination result reported by the agent.
	 * 
	 * @param reporter the agent who reported its divination
	 * @return null if no report
	 */
	protected Judge getDivinationBy(Agent reporter) {
		return divinationMap.getOrDefault(reporter, null);
	}

	private List<Judge> myDivinations = new ArrayList<>();

	/**
	 * Returns all divination results of this player.
	 * 
	 * @return the list of divination results of this player
	 */
	protected List<Judge> getMyDivinations() {
		return Collections.unmodifiableList(myDivinations);
	}

	/**
	 * Returns the divination result by this player on specified day.
	 * 
	 * @param day the day specified
	 * @return the divination result by this player on the day specified
	 */
	protected Judge getMyDivinationOnDay(int day) {
		for (Judge j : myDivinations) {
			if (j.getDay() == day) {
				return j;
			}
		}
		return null;
	}

	private List<Judge> myIdentifications = new ArrayList<>();

	/**
	 * Returns all results of medium done by this player.
	 * 
	 * @return the list of all medium results done by this player
	 */
	protected List<Judge> getMyIdentifications() {
		return Collections.unmodifiableList(myIdentifications);
	}

	private List<Judge> identReports = new ArrayList<>();

	/**
	 * Returns all medium reports reported until now.
	 * 
	 * @return the list of medium results reported until now
	 */
	protected List<Judge> getIdentReports() {
		return Collections.unmodifiableList(identReports);
	}

	private Map<Agent, Judge> identMap = new HashMap<>();

	/**
	 * Returns the latest medium result reported by the agent.
	 * 
	 * @param reporter the agent who reported its medium
	 * @return null if no report
	 */
	protected Judge getIdentBy(Agent reporter) {
		return identMap.getOrDefault(reporter, null);
	}

	private Deque<Content> talkQueue = new LinkedList<>();

	/**
	 * Vote candidate.
	 */
	protected Agent voteCandidate;

	/**
	 * Declared vote candidate.
	 */
	protected Agent declaredVoteCandidate;

	/**
	 * Declared estimated werewolf.
	 */
	protected Agent declaredWolf;

	private Map<Agent, Role> comingoutMap = new HashMap<>();

	/**
	 * Returns whether or not the agent has done CO.
	 * 
	 * @param agent the agent
	 * @return true if the agent has done CO
	 */
	protected boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	/**
	 * Returns whether or not the CO of the role has been done.
	 * 
	 * @param role the role
	 * @return true if the CO has been done
	 */
	protected boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/**
	 * Returns the role of the CO the agent has done.
	 * 
	 * @param agent the agent
	 * @return null if the agent has not done CO
	 */
	protected Role getCoRole(Agent agent) {
		return comingoutMap.getOrDefault(agent, null);
	}

	/**
	 * Returns the agents that has done CO of the role.
	 * 
	 * @param role the role
	 * @return the list of the agents that has been done CO
	 */
	protected List<Agent> getProfessed(Role role) {
		return comingoutMap.keySet().stream().filter(a -> getCoRole(a) == role).collect(Collectors.toList());
	}

	private int talkListHead;

	private EstimateReasonMap estimateReasonMap = new EstimateReasonMap();

	/**
	 * Returns the mapping between agent and its estimate with reason.
	 * 
	 * @return the estimateReasonMap
	 */
	protected EstimateReasonMap getEstimateReasonMap() {
		return estimateReasonMap;
	}

	private VoteReasonMap voteReasonMap = new VoteReasonMap();

	/**
	 * Returns the mapping between agent and its declared vote with reason.
	 * 
	 * @return the voteReasonMap
	 */
	protected VoteReasonMap getVoteReasonMap() {
		return voteReasonMap;
	}

	private VoteRequestCounter voteRequestCounter = new VoteRequestCounter();

	/**
	 * Returns the counter of vote requests.
	 * 
	 * @return the voteRequestCounter
	 */
	protected VoteRequestCounter getVoteRequestCounter() {
		return voteRequestCounter;
	}

	/**
	 * Returns the probability that the agent is the role.
	 * 
	 * @param agent the agent
	 * @param role  the role
	 * @return the probability that the agent is the role
	 */
	protected double getProbOf(Agent agent, Role role) {
		return metaInfo.getRoleEstimator(role).get(agent);
	}

	/**
	 * Overwrites the probability that the agent is the role, with new probability.
	 * 
	 * @param agent the agent
	 * @param role  the role
	 * @param prob  new probability
	 */
	protected void overwriteProbOf(Agent agent, Role role, double prob) {
		metaInfo.getRoleEstimator(role).put(agent, prob);
	}

	/**
	 * Returns the number of games this player participated.
	 * 
	 * @return the number of games this player participated
	 */
	protected int getGameCount() {
		return metaInfo.getGamecount();
	}

	/**
	 * Returns the number of games in which this player was the role.
	 * 
	 * @param role the role
	 * @return the number of games in which this player was the role
	 */
	protected int getRoleCount(Role role) {
		return metaInfo.getRoleCount(role);
	}

	/**
	 * Returns the number of win games of the agent.
	 * 
	 * @param agent the agent
	 * @return the number of win games of the agent
	 */
	protected int getWinCount(Agent agent) {
		return metaInfo.getWinCount(agent);
	}

	/**
	 * Returns whether or not the agent can do PP.
	 * 
	 * @param agent the agent
	 * @return true if the agent can do PP
	 */
	protected boolean getPpAbility(Agent agent) {
		return metaInfo.getPpAbility(agent);
	}

	/**
	 * Returns whether or not the agent is like a seer.
	 * 
	 * @param agent the agent
	 * @return true if the agent is like a seer
	 */
	protected boolean isLikeSeer(Agent agent) {
		return agent != null && maxRole(agent) == Role.SEER;
	}

	/**
	 * Returns whether or not the agent is like a werewolf.
	 * 
	 * @param agent the agent
	 * @return true if the agent is like a werewolf
	 */
	protected boolean isLikeWolf(Agent agent) {
		return agent != null && isGray(agent) && maxRole(agent) == Role.WEREWOLF;
	}

	/**
	 * Returns the role with the maximum probability that the agent is the role.
	 * 
	 * @param agent the agent
	 * @return the role with the maximum probability
	 */
	protected Role maxRole(Agent agent) {
		return gameInfo.getExistingRoles().stream().max(Comparator.comparing(r -> getProbOf(agent, r))).orElse(Role.ANY);
	}

	/**
	 * Returns the agent with the maximum probability of being the role, from the candidates.
	 * 
	 * @param role     the role
	 * @param agents   the candidates
	 * @param excludes the agents to be excluded
	 * @return the agent with the maximum probability
	 */
	protected Agent selectMax(Role role, Collection<Agent> agents, Agent... excludes) {
		return metaInfo.getRoleEstimator(role).max(agents, excludes);
	}

	/**
	 * Returns the agent with the minimum probability of being the role, from the candidates.
	 * 
	 * @param role     the role
	 * @param agents   the candidates
	 * @param excludes the agents to be excluded
	 * @return the agent with the minimum probability
	 */
	protected Agent selectMin(Role role, Collection<Agent> agents, Agent... excludes) {
		return metaInfo.getRoleEstimator(role).min(agents, excludes);
	}

	/**
	 * Returns the list of agents sorted by role probability in ascending order.
	 * 
	 * @param role   the role
	 * @param agents the list of agents to be sorted
	 * @return the sorted list of agents
	 */
	protected List<Agent> ascending(Role role, Collection<Agent> agents) {
		return metaInfo.getRoleEstimator(role).ascneding(agents);
	}

	/**
	 * Returns the list of agents sorted by role probability in descending order.
	 * 
	 * @param role   the role
	 * @param agents the list of agents to be sorted
	 * @return the sorted list of agents
	 */
	protected List<Agent> descending(Role role, Collection<Agent> agents) {
		return metaInfo.getRoleEstimator(role).descneding(agents);
	}

	private Map<Agent, Species> speciesMap = new HashMap<>();

	/**
	 * Returns whether or not the agent is human.
	 * 
	 * @param agent the agent
	 * @return true if the agent is human
	 */
	protected boolean isHuman(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.HUMAN;
	}

	/**
	 * Sets the agent to human.
	 * 
	 * @param agent the agent
	 */
	protected void setHuman(Agent agent) {
		speciesMap.put(agent, Species.HUMAN);
	}

	/**
	 * Returns the list of humans from the candidates.
	 * 
	 * @param agents the candidates
	 * @return the list of human agents
	 */
	protected List<Agent> getHuman(Collection<Agent> agents) {
		return agents.stream().filter(a -> isHuman(a)).collect(Collectors.toList());
	}

	/**
	 * Returns whether or not the agent is unjudged.
	 * 
	 * @param agent the agent
	 * @return true if the agent is unjudged
	 */
	protected boolean isGray(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.ANY;
	}

	/**
	 * Sets the agent to unjudged.
	 * 
	 * @param agent the agent
	 */
	protected void setGray(Agent agent) {
		speciesMap.put(agent, Species.ANY);
	}

	/**
	 * Returns the list of unjudged agents from the candidates.
	 * 
	 * @param agents the candidates
	 * @return the list of unjudged agents
	 */
	protected List<Agent> getGray(Collection<Agent> agents) {
		return agents.stream().filter(a -> isGray(a)).collect(Collectors.toList());
	}

	/**
	 * Returns whether or not the agent is werewolf.
	 * 
	 * @param agent the agent
	 * @return true if the agent is a werewolf
	 */
	protected boolean isWolf(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.WEREWOLF;
	}

	/**
	 * Sets the agent to werewolf.
	 * 
	 * @param agent the agent
	 */
	protected void setWolf(Agent agent) {
		speciesMap.put(agent, Species.WEREWOLF);
	}

	/**
	 * Returns the list of werewolves from the candidates.
	 * 
	 * @param agents the candidates
	 * @return the list of agents whose species is werewolf
	 */
	protected List<Agent> getWolf(Collection<Agent> agents) {
		return agents.stream().filter(a -> isWolf(a)).collect(Collectors.toList());
	}

	/**
	 * Returns whethre or not this player has found a werewolf.
	 * 
	 * @return true if this player has found a werewolf
	 */
	protected boolean foundWolf() {
		return speciesMap.values().contains(Species.WEREWOLF);
	}

	/**
	 * Returns whether or not this player has found a possessed human.
	 * 
	 * @return true if this player has found a possessed human
	 */
	protected boolean foundPossessed() {
		double all = getOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		double alive = getAliveOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		return alive > 0.5 * all || isCo(Role.POSSESSED);
	}

	/**
	 * Return fake seers.
	 * 
	 * @return the agent list of fake seers
	 */
	protected List<Agent> getFakeSeers() {
		if (myRole == Role.SEER) {
			return getProfessed(Role.SEER);
		} else {
			return divinationReports.stream().filter(j -> conflicts(j))
					.map(j -> j.getAgent()).distinct().collect(Collectors.toList());
		}
	}

	/**
	 * Return whether or not the agent is a fake seer.
	 * 
	 * @param agent the agent
	 * @return true if the agent is a fake seer
	 */
	protected boolean isFakeSeer(Agent agent) {
		return getFakeSeers().contains(agent);
	}

	/**
	 * Return fake mediums.
	 * 
	 * @return the agent list of fake mediums
	 */
	protected List<Agent> getFakeMediums() {
		if (myRole == Role.MEDIUM) {
			return getProfessed(Role.MEDIUM);
		} else {
			return identReports.stream().filter(j -> conflicts(j))
					.map(j -> j.getAgent()).distinct().collect(Collectors.toList());
		}
	}

	/**
	 * Return whether or not the agent is a fake medium.
	 * 
	 * @param agent the agent
	 * @return true if the agent is a fake medium
	 */
	protected boolean isFakeMedium(Agent agent) {
		return getFakeMediums().contains(agent);
	}

	/**
	 * Returns whether or not the judge conflicts known judges.
	 * 
	 * @param judge the judge
	 * @return true if the judge conflicts known judges
	 */
	protected boolean conflicts(Judge judge) {
		return (isHuman(judge.getTarget()) && judge.getResult() == Species.WEREWOLF)
				|| (isWolf(judge.getTarget()) && judge.getResult() == Species.HUMAN);
	}

	/**
	 * Returns the agents whose role this player thinks is the role.
	 * 
	 * @param the role
	 * @return the list of agents whose role this player thinks is the role
	 */
	protected List<Agent> getEstimates(Role role) {
		return estimateReasonMap.getEstimated(me, role);
	}

	/**
	 * Sets the role that this player estimates about the target.
	 * 
	 * @param target the target agent
	 * @param role   the estimated role
	 */
	protected void setEstimate(Agent target, Role role) {
		if (target == null) {
			return;
		}
		if (role != null) {
			estimateReasonMap.put(new Estimate(me, target, role));
		} else {
			if (estimateReasonMap.containsKey(me)) {
				estimateReasonMap.get(me).remove(target);
			}
		}
	}

	/**
	 * Returns whether or not this player estimates the agent as the role.
	 * 
	 * @param agent the agent
	 * @param role  the role
	 * @return true if this player estimates the agent as the role
	 */
	protected boolean isEstimated(Agent agent, Role role) {
		if (estimateReasonMap.containsKey(me) && estimateReasonMap.get(me).containsKey(agent)) {
			estimateReasonMap.get(me).get(agent).hasRole(role);
		}
		return false;
	}

	/**
	 * Returns whether or not the agent is alive.
	 * 
	 * @param agent the agent
	 * @return true if the agent is alive
	 */
	protected boolean isAlive(Agent agent) {
		return gameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/**
	 * Returns whether or not the agent is dead.
	 * 
	 * @param agent the agent
	 * @return true if the agent is dead
	 */
	protected boolean isDead(Agent agent) {
		return gameInfo.getStatusMap().get(agent) == Status.DEAD;
	}

	/**
	 * Returns the alive agents from the candidates.
	 * 
	 * @param agents the candidates
	 * @return the list of alive agents
	 */
	protected List<Agent> getAlive(Collection<Agent> agents) {
		return agents.stream().filter(a -> isAlive(a)).distinct().collect(Collectors.toList());
	}

	/**
	 * Returns the dead agents from the candidates.
	 * 
	 * @param agents the candidates
	 * @return the list of dead agents
	 */
	protected List<Agent> getDead(Collection<Agent> agents) {
		return agents.stream().filter(a -> isDead(a)).distinct().collect(Collectors.toList());
	}

	/**
	 * Returns one agent from the candidates randomly.
	 * 
	 * @param list the list of candidates
	 * @return the agent selected randomly from the list
	 */
	public static <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/**
	 * Returns the first of the list.
	 * 
	 * @param list the list
	 * @return null if the list is empty
	 */
	public static <T> T selectFirst(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	/**
	 * Returns the last of the list.
	 * 
	 * @param list the list
	 * @return null if the list is empty
	 */
	public static <T> T selectLast(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(list.size() - 1);
		}
	}

	/**
	 * Returns whether or not the role exists in the village.
	 * 
	 * @param role the role
	 * @return true if the role exists
	 */
	protected boolean exists(Role role) {
		return gameInfo.getExistingRoles().contains(role);
	}

	@Override
	public String getName() {
		return "BasePlayer";
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		this.gameInfo = gameInfo;
		date = -1;
		isPP = false;
		me = gameInfo.getAgent();
		myRole = gameInfo.getRole();
		executedAgents.clear();
		killedAgents.clear();
		divinationReports.clear();
		divinationMap.clear();
		identReports.clear();
		identMap.clear();
		myDivinations.clear();
		myIdentifications.clear();
		comingoutMap.clear();
		estimateReasonMap.clear();
		speciesMap.clear();
		if (myRole != Role.WEREWOLF) {
			gameInfo.getAgentList().forEach(a -> speciesMap.put(a, Species.ANY));
			speciesMap.put(me, Species.HUMAN);
		} else {
			gameInfo.getAgentList().forEach(a -> speciesMap.put(a, Species.HUMAN));
			gameInfo.getRoleMap().keySet().forEach(a -> speciesMap.put(a, Species.WEREWOLF));
		}
	}

	@Override
	public void update(GameInfo gameInfo) {
		this.gameInfo = gameInfo;

		if (gameInfo.getDay() == date + 1) {
			date = gameInfo.getDay();
			return;
		}

		addExecutedAgent(gameInfo.getLatestExecutedAgent());

		for (int i = talkListHead; i < gameInfo.getTalkList().size(); i++) {
			Talk talk = gameInfo.getTalkList().get(i);
			lastTalkIdx = talk.getIdx();
			Agent talker = talk.getAgent();
			if (talker == me) {
				continue;
			}
			Content content = new Content(talk.getText());
			if (content.getSubject() == Content.UNSPEC) {
				content = replaceSubject(content, talker);
			}
			parseSentence(content);
		}

		if (gameInfo.getAgentList().size() == 5) {
			getDead(getOthers()).forEach(a -> setHuman(a));
		}

		talkListHead = gameInfo.getTalkList().size();
	}

	/**
	 * Parses the sentence recursively.
	 * 
	 * @param content the sentence to be parsed
	 */
	protected void parseSentence(Content content) {
		if (estimateReasonMap.put(content)) {
			return;
		}
		if (voteReasonMap.put(content)) {
			return;
		}
		switch (content.getTopic()) {
		case COMINGOUT:
			comingoutMap.put(content.getTarget(), content.getRole());
			if (content.getRole() == Role.WEREWOLF || content.getRole() == Role.POSSESSED) {
				metaInfo.setPpAbility(content.getTarget(), true);
			}
			return;
		case DIVINED:
			divinationReports.add(new Judge(date, content.getSubject(), content.getTarget(), content.getResult()));
			divinationMap.put(content.getSubject(), divinationReports.get(divinationReports.size() - 1));
			return;
		case IDENTIFIED:
			identReports.add(new Judge(date, content.getSubject(), content.getTarget(), content.getResult()));
			identMap.put(content.getSubject(), identReports.get(identReports.size() - 1));
			return;
		case OPERATOR:
			parseOperator(content);
			return;
		default:
			break;
		}
	}

	/**
	 * Parses the sentence containing operator.
	 * 
	 * @param content the sentence to be parsed
	 */
	protected void parseOperator(Content content) {
		switch (content.getOperator()) {
		case BECAUSE:
			parseSentence(content.getContentList().get(1));
			break;
		case DAY:
			parseSentence(content.getContentList().get(0));
			break;
		case AND:
		case OR:
		case XOR:
			for (Content c : content.getContentList()) {
				parseSentence(c);
			}
			break;
		case REQUEST:
			if (voteRequestCounter.add(content)) {
				return;
			}
			break;
		case INQUIRE:
			break;
		default:
			break;
		}
	}

	@Override
	public void dayStart() {
		addExecutedAgent(gameInfo.getExecutedAgent());

		gameInfo.getLastDeadAgentList().forEach(a -> addKilledAgent(a));

		Judge divination = gameInfo.getDivineResult();
		if (divination != null) {
			myDivinations.add(divination);
			if (divination.getResult() == Species.WEREWOLF) {
				setWolf(divination.getTarget());
			} else {
				setHuman(divination.getTarget());
			}
		}
		Judge ident = gameInfo.getMediumResult();
		if (ident != null) {
			myIdentifications.add(ident);
			if (ident.getResult() == Species.WEREWOLF) {
				setWolf(ident.getTarget());
			} else {
				setHuman(ident.getTarget());
			}
		}
		isRevote = false;
		talkQueue.clear();
		declaredVoteCandidate = null;
		declaredWolf = null;
		voteCandidate = null;
		talkListHead = 0;
		voteReasonMap.clear();
		voteRequestCounter.clear();
		turn = 0;
		lastTalkIdx = -1;
	}

	private void addExecutedAgent(Agent executedAgent) {
		if (executedAgent != null) {
			executedAgents.add(executedAgent);
		}
	}

	private void addKilledAgent(Agent killedAgent) {
		if (killedAgent != null) {
			killedAgents.add(killedAgent);
			setHuman(killedAgent);
		}
	}

	/**
	 * Determines the vote candidate.
	 * 
	 * <blockquote>called from talk() and vote()</blockquote>
	 * 
	 * @param isLast
	 *               true if called from vote(), otherwise false
	 */
	protected void chooseVoteCandidate(boolean isLast) {
		if (isLast) {
			voteCandidate = adjustVote(getVoteReasonMap(), getMe(), 0.5);
		}
	}

	@Override
	public String talk() {
		chooseVoteCandidate(false);
		if (voteCandidate != declaredVoteCandidate) {
			cancelVoting();
			cancelTalk(requestContent(me, Content.ANY, voteContent(Content.ANY, declaredVoteCandidate)));
			if (voteCandidate != null) {
				enqueueTalk(voteContent(me, voteCandidate));
				enqueueTalk(requestContent(me, Content.ANY, voteContent(Content.ANY, voteCandidate)));
			}
			declaredVoteCandidate = voteCandidate;
		}
		nextTurn();
		return dequeueTalk();
	}

	/**
	 * Queues the content of talk.
	 * 
	 * @param content the content of talk
	 */
	protected void enqueueTalk(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			talkQueue.offer(replaceSubject(content, me));
		} else {
			talkQueue.offer(content);
		}
	}

	/**
	 * Dequeues the content of talk.
	 * 
	 * @return the content of talk
	 */
	protected String dequeueTalk() {
		if (talkQueue.isEmpty()) {
			return Talk.SKIP;
		}
		Content content = talkQueue.poll();
		if (content.getSubject() == me) {
			return Content.stripSubject(content.getText());
		}
		return content.getText();
	}

	@Override
	public Agent vote() {
		chooseVoteCandidate(true);
		isRevote = true;
		return voteCandidate;
	}

	/**
	 * Adjusts vote based on the status of vote declaration of other players.
	 * 
	 * @param vrmap     the status of vote declaration
	 * @param agent     the agent to be saved
	 * @param threshold the threshold of voting rate
	 * @return adjusted vote candidate
	 */
	protected Agent adjustVote(VoteReasonMap vrmap, Agent agent, double threshold) {
		Agent voted = vrmap.getTarget(getMe());

		if (vrmap.getVoteCount() < getAliveOthers().size() * threshold) {
			return voted;
		}

		vrmap.cancel(getMe());
		List<Agent> winners = vrmap.getWinners();
		List<Agent> orderedList = vrmap.getOrderedList(agent, getMe());
		vrmap.put(getMe(), voted);

		if (!winners.contains(voted) && winners.contains(agent)) {
			if (!orderedList.isEmpty()) {
				return orderedList.get(0);
			}
		}
		return voted;
	}

	@Override
	public String whisper() {
		return null;
	}

	@Override
	public Agent attack() {
		return null;
	}

	@Override
	public Agent divine() {
		return null;
	}

	@Override
	public Agent guard() {
		return null;
	}

	@Override
	public void finish() {
	}

	/**
	 * Replaces the subject of the content with new subject.
	 * 
	 * @param content    the content
	 * @param newSubject new subject
	 * @return the content whose subject is replaced
	 */
	protected static Content replaceSubject(Content content, Agent newSubject) {
		if (content.getTopic() == Topic.SKIP || content.getTopic() == Topic.OVER) {
			return content;
		}
		if (newSubject == Content.UNSPEC) {
			return new Content(Content.stripSubject(content.getText()));
		} else {
			return new Content(newSubject + " " + Content.stripSubject(content.getText()));
		}
	}

	/**
	 * Returns the content of agreement.
	 * 
	 * @param subject  the subject of utterance
	 * @param talkType the type of the talk the subject agree with
	 * @param talkDay  the date of the talk the subject agree with
	 * @param talkID   the talkID of the talk the subject agree with
	 * @return the content of agreement
	 */
	protected static Content agreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new AgreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/**
	 * Returns the content of disagreement.
	 * 
	 * @param subject  the subject of utterance
	 * @param talkType the type of the talk the subject disagree with
	 * @param talkDay  the date of the talk the subject disagree with
	 * @param talkID   the talkID of the talk the subject disagree with
	 * @return the content of disagreement
	 */
	protected static Content disagreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new DisagreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/**
	 * Returns the content of vote declaration.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject wants to vote for
	 * @return the content of vote declaration
	 */
	protected static Content voteContent(Agent subject, Agent target) {
		return new Content(new VoteContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of vote report.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject voted for
	 * @return the content of vote report
	 */
	protected static Content votedContent(Agent subject, Agent target) {
		return new Content(new VotedContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of attack vote declaration.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject wants to attack
	 * @return the content of attack vote declaration
	 */
	protected static Content attackContent(Agent subject, Agent target) {
		return new Content(new AttackContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of attack vote report.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject wanted to attack
	 * @return the content of attack vote report
	 */
	protected static Content attackedContent(Agent subject, Agent target) {
		return new Content(new AttackedContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of guard declaration.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject wants to guard
	 * @return the content of guard declaration
	 */
	protected static Content guardContent(Agent subject, Agent target) {
		return new Content(new GuardCandidateContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of guard report.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject guarded
	 * @return the content of guard report
	 */
	protected static Content guardedContent(Agent subject, Agent target) {
		return new Content(new GuardedAgentContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of estimate.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject estimates
	 * @param role    the estimated role of the target by the subject
	 * @return the content of estimate
	 */
	protected static Content estimateContent(Agent subject, Agent target, Role role) {
		return new Content(new EstimateContentBuilder(subject, target == null ? Content.ANY : target, role));
	}

	/**
	 * Returns the content of CO.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent the subject announces the role
	 * @param role    the role of the target to be announced by the subject
	 * @return the content of CO
	 */
	protected static Content coContent(Agent subject, Agent target, Role role) {
		return new Content(new ComingoutContentBuilder(subject, target == null ? Content.ANY : target, role));
	}

	/**
	 * Returns the content of request.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the target of request
	 * @param content the content of request
	 * @return the content of request
	 */
	protected static Content requestContent(Agent subject, Agent target, Content content) {
		return new Content(new RequestContentBuilder(subject, target == null ? Content.ANY : target, content));
	}

	/**
	 * Returns the content of inquiry.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the target of inquiry
	 * @param content the content of inquiry
	 * @return the content of inquiry
	 */
	protected static Content inquiryContent(Agent subject, Agent target, Content content) {
		return new Content(new InquiryContentBuilder(subject, target == null ? Content.ANY : target, content));
	}

	/**
	 * Returns the content of divination.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent to be divined
	 * @return the content of divination
	 */
	protected static Content divinationContent(Agent subject, Agent target) {
		return new Content(new DivinationContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * Returns the content of divination report.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the divined agent
	 * @param result  the result of divination
	 * @return the content of divination report
	 */
	protected static Content divinedContent(Agent subject, Agent target, Species result) {
		return new Content(new DivinedResultContentBuilder(subject, target == null ? Content.ANY : target, result));
	}

	/**
	 * Returns the content of identification.
	 * 
	 * @param subject the subject of utterance
	 * @param target  the agent idetified
	 * @param result  the result of identification
	 * @return the content of identification
	 */
	protected static Content identContent(Agent subject, Agent target, Species result) {
		return new Content(new IdentContentBuilder(subject, target == null ? Content.ANY : target, result));
	}

	/**
	 * Returns the content of AND operator.
	 * 
	 * @param subject  the subject of utterance
	 * @param contents the operands of AND operator
	 * @return the content of AND operator
	 */
	protected static Content andContent(Agent subject, Content... contents) {
		return new Content(new AndContentBuilder(subject, contents));
	}

	/**
	 * Returns the content of OR operator.
	 * 
	 * @param subject  the subject of utterance
	 * @param contents the operands of OR operator
	 * @return the content of OR operator
	 */
	protected static Content orContent(Agent subject, Content... contents) {
		return new Content(new OrContentBuilder(subject, contents));
	}

	/**
	 * Returns the content of XOR operator.
	 * 
	 * @param subject  the subject of utterance
	 * @param content1 the first operand of XOR operator
	 * @param content2 the second operand of XOR operator
	 * @return the content of XOR operator
	 */
	protected static Content xorContent(Agent subject, Content content1, Content content2) {
		return new Content(new XorContentBuilder(subject, content1, content2));
	}

	/**
	 * Returns the content of NOT operator.
	 * 
	 * @param subject the subject of utterance
	 * @param content the denied content
	 * @return the content of NOT operator
	 */
	protected static Content notContent(Agent subject, Content content) {
		return new Content(new NotContentBuilder(subject, content));
	}

	/**
	 * Returns the content of DAY operator.
	 * 
	 * @param subject the subject of utterance
	 * @param day     the date of the content
	 * @param content the content
	 * @return the content of DAY operator
	 */
	protected static Content dayContent(Agent subject, int day, Content content) {
		return new Content(new DayContentBuilder(subject, day, content));
	}

	/**
	 * Returns the content of BECAUSE operator.
	 * 
	 * @param subject the subject of utterance
	 * @param reason  the reason of the action
	 * @param action  the action based on the reason
	 * @return the content of BECAUSE operator
	 */
	protected static Content becauseContent(Agent subject, Content reason, Content action) {
		return new Content(new BecauseContentBuilder(subject, reason, action));
	}

	/**
	 * Cancels the queued talk.
	 * 
	 * @param content the talk to be canceled
	 */
	protected void cancelTalk(Content content) {
		Iterator<Content> it = talkQueue.iterator();
		while (it.hasNext()) {
			Content c = it.next();
			if (c.equals(content)) {
				it.remove();
			} else if (c.getTopic() == content.getTopic()) {
				switch (c.getTopic()) {
				case VOTE:
					it.remove();
					break;
				case COMINGOUT:
				case ESTIMATE:
				case DIVINED:
				case IDENTIFIED:
					if (c.getTarget() == content.getTarget()) {
						it.remove();
					}
					break;

				default:
					break;
				}
			}
		}
	}

	/**
	 * Cancels all talks in the queue.
	 */
	protected void cancelAllTalk() {
		talkQueue.clear();
	}

	/**
	 * Queues CO talk.
	 * 
	 * @param role the role to be announced by this player
	 */
	protected void talkCo(Role role) {
		cancelCo();
		enqueueTalk(coContent(me, me, role));
	}

	/**
	 * Cancels this player's CO talk.
	 */
	protected void cancelCo() {
		cancelTalk(coContent(me, me, Role.ANY));
	}

	/**
	 * Queues estimate talk.
	 * 
	 * @param target the agent the subject estimates
	 * @param role   the estimated role of the target by the subject
	 */
	protected void talkEstimate(Agent target, Role role) {
		cancelEstimate(target);
		enqueueTalk(estimateContent(me, target, role));
	}

	/**
	 * Cancels this player's estimate talk about the target.
	 * 
	 * @param target the target of estimate to be cancelled
	 */
	protected void cancelEstimate(Agent target) {
		cancelTalk(estimateContent(me, target, Role.ANY));
	}

	/**
	 * Queues vote declaration.
	 * 
	 * @param target the target of this player's vote
	 */
	protected void talkVoting(Agent target) {
		cancelVoting();
		enqueueTalk(voteContent(me, target));
	}

	/**
	 * Cancels this player's vote declaration.
	 */
	protected void cancelVoting() {
		cancelTalk(voteContent(me, Content.ANY));
	}

	/**
	 * Queues divination report.
	 * 
	 * @param target the divined agent
	 * @param result the result of divination
	 */
	protected void talkDivined(Agent target, Species result) {
		cancelDivined(target);
		enqueueTalk(divinedContent(me, target, result));
	}

	/**
	 * Cancels divination report of the target.
	 * 
	 * @param target the target agent in the report to be cancelled
	 */
	protected void cancelDivined(Agent target) {
		cancelTalk(divinedContent(me, target, Species.ANY));
	}

	/**
	 * Queues identification report.
	 * 
	 * @param target the identified agent
	 * @param result the result of identification
	 */
	protected void talkIdentified(Agent target, Species result) {
		cancelIdentified(target);
		enqueueTalk(identContent(me, target, result));
	}

	/**
	 * Cancels identification report.
	 * 
	 * @param target the target agent in the report to be cancelled
	 */
	protected void cancelIdentified(Agent target) {
		cancelTalk(identContent(me, target, Species.ANY));
	}

}
