/**
 * BasePlayer.java
 * Copyright (c) 2020 OTSUKI Takashi
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
 * base class for all role classes
 * 
 * @author otsuki
 */
public class BasePlayer implements Player {

	protected boolean debug = true;

	private MetaInfo metaInfo;

	public BasePlayer(MetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}

	/**
	 * myself
	 * 
	 * @return myself
	 */
	protected Agent getMe() {
		return me;
	}

	private Agent me;

	/**
	 * my role
	 * 
	 * @return my role
	 */
	protected Role getMyRole() {
		return myRole;
	}

	private Role myRole;

	/**
	 * date
	 * 
	 * @return date
	 */
	protected int getDate() {
		return date;
	}

	private int date;

	/**
	 * turn
	 * 
	 * @return turn
	 */
	protected int getTurn() {
		return turn;
	}

	/**
	 * advance turn
	 */
	protected void nextTurn() {
		turn++;
	}

	private int turn;

	/**
	 * Index of the latest Talk
	 * 
	 * @return index of the latest talk
	 */
	protected int getLastTalkIdx() {
		return lastTalkIdx;
	}

	private int lastTalkIdx;

	protected boolean isRevote;

	protected boolean isPP;

	/**
	 * game information
	 * 
	 * @return the gameInfo
	 */
	protected GameInfo getGameInfo() {
		return gameInfo;
	}

	private GameInfo gameInfo;

	/**
	 * others
	 * 
	 * @return list of other agents
	 */
	protected List<Agent> getOthers() {
		return getOthers(gameInfo.getAgentList());
	}

	/**
	 * extract other agents from agent list
	 * 
	 * @param agents agent list
	 * @return list of other agents
	 */
	protected List<Agent> getOthers(Collection<Agent> agents) {
		return agents.stream().filter(a -> a != me).distinct().collect(Collectors.toList());
	}

	/**
	 * alive others
	 * 
	 * @return list of alive others
	 */
	protected List<Agent> getAliveOthers() {
		return getAlive(getOthers());
	}

	private Set<Agent> executedAgents = new HashSet<>();

	/**
	 * whether or not the agent has been executed
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isExecuted(Agent agent) {
		return executedAgents.contains(agent);
	}

	private Set<Agent> killedAgents = new HashSet<>();

	/**
	 * whether or not the agent has been killed by werewolf's attack
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isKilled(Agent agent) {
		return killedAgents.contains(agent);
	}

	private List<Judge> divinationReports = new ArrayList<>();

	/**
	 * list of divination reports
	 * 
	 * @return
	 */
	protected List<Judge> getDivinationReports() {
		return Collections.unmodifiableList(divinationReports);
	}

	private Map<Agent, Judge> divinationMap = new HashMap<>();

	/**
	 * latest divination reported by reporter
	 * 
	 * @param reporter
	 * @return null if no report
	 */
	protected Judge getDivinationBy(Agent reporter) {
		return divinationMap.getOrDefault(reporter, null);
	}

	private List<Judge> myDivinations = new ArrayList<>();

	/**
	 * my divination result
	 * 
	 * @return
	 */
	protected List<Judge> getMyDivinations() {
		return Collections.unmodifiableList(myDivinations);
	}

	/**
	 * my divination on certain day
	 * 
	 * @param day date
	 * @return
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
	 * my medium result
	 * 
	 * @return
	 */
	protected List<Judge> getMyIdentifications() {
		return Collections.unmodifiableList(myIdentifications);
	}

	private List<Judge> identReports = new ArrayList<>();

	/**
	 * list of identification reports
	 * 
	 * @return the identReports
	 */
	protected List<Judge> getIdentReports() {
		return Collections.unmodifiableList(identReports);
	}

	private Map<Agent, Judge> identMap = new HashMap<>();

	/**
	 * latest identification reported by reporter
	 * 
	 * @param reporter
	 * @return null if no report
	 */
	protected Judge getIdentBy(Agent reporter) {
		return identMap.getOrDefault(reporter, null);
	}

	private Deque<Content> talkQueue = new LinkedList<>();

	/**
	 * vote candidate
	 */
	protected Agent voteCandidate;

	/**
	 * declared vote candidate
	 */
	protected Agent declaredVoteCandidate;

	/**
	 * declared estimated werewolf
	 */
	protected Agent declaredWolf;

	private Map<Agent, Role> comingoutMap = new HashMap<>();

	/**
	 * whether or not the agent did CO
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isCo(Agent agent) {
		return comingoutMap.containsKey(agent);
	}

	/**
	 * whether or not the role was CO
	 * 
	 * @param role
	 * @return
	 */
	protected boolean isCo(Role role) {
		return comingoutMap.containsValue(role);
	}

	/**
	 * the role the agent came out
	 * 
	 * @param agent
	 * @return null if he did not CO
	 */
	protected Role getCoRole(Agent agent) {
		return comingoutMap.getOrDefault(agent, null);
	}

	/**
	 * the list of agents that CO the role
	 * 
	 * @param role
	 * @return
	 */
	protected List<Agent> getProfessed(Role role) {
		return comingoutMap.keySet().stream().filter(a -> getCoRole(a) == role).collect(Collectors.toList());
	}

	private int talkListHead;

	private EstimateReasonMap estimateReasonMap = new EstimateReasonMap();

	/**
	 * the map of estimate and its reason
	 * 
	 * @return the estimateReasonMap
	 */
	protected EstimateReasonMap getEstimateReasonMap() {
		return estimateReasonMap;
	}

	private VoteReasonMap voteReasonMap = new VoteReasonMap();

	/**
	 * the map of vote and its reason
	 * 
	 * @return the voteReasonMap
	 */
	protected VoteReasonMap getVoteReasonMap() {
		return voteReasonMap;
	}

	private VoteRequestCounter voteRequestCounter = new VoteRequestCounter();

	protected VoteRequestCounter getVoteRequestCounter() {
		return voteRequestCounter;
	}

	/**
	 * role probability
	 * 
	 * @param role
	 * @param agent
	 * @return probability
	 */
	protected double getProbOf(Agent agent, Role role) {
		return metaInfo.getRoleEstimator(role).get(agent);
	}

	/**
	 * overwrites role probability
	 * 
	 * @param agent
	 * @param role
	 * @param prob
	 */
	protected void overwriteProbOf(Agent agent, Role role, double prob) {
		metaInfo.getRoleEstimator(role).put(agent, prob);
	}

	/**
	 * the number of games participated
	 * 
	 * @return
	 */
	protected int getGameCount() {
		return metaInfo.getGamecount();
	}

	/**
	 * the number of games in which I was the role
	 * 
	 * @param role
	 * @return
	 */
	protected int getRoleCount(Role role) {
		return metaInfo.getRoleCount(role);
	}

	/**
	 * the number of win games
	 * 
	 * @param agent
	 * @return
	 */
	protected int getWinCount(Agent agent) {
		return metaInfo.getWinCount(agent);
	}

	/**
	 * whether or not the agent can PP
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean getPpAbility(Agent agent) {
		return metaInfo.getPpAbility(agent);
	}

	/**
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isLikeSeer(Agent agent) {
		return agent != null && maxRole(agent) == Role.SEER;
	}

	/**
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isLikeWolf(Agent agent) {
		return agent != null && isGray(agent) && maxRole(agent) == Role.WEREWOLF;
	}

	/**
	 * the agent's maximum probability role
	 * 
	 * @param agent
	 * @return
	 */
	protected Role maxRole(Agent agent) {
		return gameInfo.getExistingRoles().stream().max(Comparator.comparing(r -> getProbOf(agent, r))).orElse(Role.ANY);
	}

	/**
	 * the agent with the maximum probability of being the role
	 * 
	 * @param role     the role
	 * @param agents   the list of agents
	 * @param excludes the agents excluded
	 * @return the agent with the maximum probability
	 */
	protected Agent selectMax(Role role, Collection<Agent> agents, Agent... excludes) {
		return metaInfo.getRoleEstimator(role).max(agents, excludes);
	}

	/**
	 * the agent with the minimum probability of being the role
	 * 
	 * @param role     the role
	 * @param agents   the list of agents
	 * @param excludes the agents excluded
	 * @return the agent with the miimum probability
	 */
	protected Agent selectMin(Role role, Collection<Agent> agents, Agent... excludes) {
		return metaInfo.getRoleEstimator(role).min(agents, excludes);
	}

	/**
	 * agent list sorted by role probability in ascending order
	 * 
	 * @param role   the role
	 * @param agents list of agents to be sorted
	 * @return sorted list of agents
	 */
	protected List<Agent> ascending(Role role, Collection<Agent> agents) {
		return metaInfo.getRoleEstimator(role).ascneding(agents);
	}

	/**
	 * agent list sorted by role probability in descending order
	 * 
	 * @param role   the role
	 * @param agents list of agents to be sorted
	 * @return sorted list of agents
	 */
	protected List<Agent> descending(Role role, Collection<Agent> agents) {
		return metaInfo.getRoleEstimator(role).descneding(agents);
	}

	private Map<Agent, Species> speciesMap = new HashMap<>();

	/**
	 * whether or not the agent is human
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isHuman(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.HUMAN;
	}

	/**
	 * sets the agent human
	 * 
	 * @param agent
	 */
	protected void setHuman(Agent agent) {
		speciesMap.put(agent, Species.HUMAN);
	}

	/**
	 * extracts humans
	 * 
	 * @param agents
	 * @return
	 */
	protected List<Agent> getHuman(Collection<Agent> agents) {
		return agents.stream().filter(a -> isHuman(a)).collect(Collectors.toList());
	}

	/**
	 * whether or not the agent is gray
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isGray(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.ANY;
	}

	/**
	 * sets the agent gray
	 * 
	 * @param agent
	 */
	protected void setGray(Agent agent) {
		speciesMap.put(agent, Species.ANY);
	}

	/**
	 * extracts gray agents
	 * 
	 * @param agents
	 * @return
	 */
	protected List<Agent> getGray(Collection<Agent> agents) {
		return agents.stream().filter(a -> isGray(a)).collect(Collectors.toList());
	}

	/**
	 * whether or not the agent is werewolf
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isWolf(Agent agent) {
		return speciesMap.getOrDefault(agent, Species.ANY) == Species.WEREWOLF;
	}

	/**
	 * sets the agent werewolf
	 * 
	 * @param agent
	 */
	protected void setWolf(Agent agent) {
		speciesMap.put(agent, Species.WEREWOLF);
	}

	/**
	 * extracts werewolves
	 * 
	 * @param agents
	 * @return
	 */
	protected List<Agent> getWolf(Collection<Agent> agents) {
		return agents.stream().filter(a -> isWolf(a)).collect(Collectors.toList());
	}

	/**
	 * whethre or not I found a werewolf
	 * 
	 * @return
	 */
	protected boolean foundWolf() {
		return speciesMap.values().contains(Species.WEREWOLF);
	}

	/**
	 * whether or not I found a possessed human
	 * 
	 * @return
	 */
	protected boolean foundPossessed() {
		double all = getOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		double alive = getAliveOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		return alive > 0.5 * all || isCo(Role.POSSESSED);
	}

	/**
	 * the list of fake seers
	 * 
	 * @return
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
	 * whether or not the agent is fake seer
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isFakeSeer(Agent agent) {
		return getFakeSeers().contains(agent);
	}

	/**
	 * the list of fake mediums
	 * 
	 * @return
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
	 * whether or not the agent is fake medium
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isFakeMedium(Agent agent) {
		return getFakeMediums().contains(agent);
	}

	/**
	 * whether or not the judge conflicts my judge
	 * 
	 * @param judge
	 * @return
	 */
	protected boolean conflicts(Judge judge) {
		return (isHuman(judge.getTarget()) && judge.getResult() == Species.WEREWOLF)
				|| (isWolf(judge.getTarget()) && judge.getResult() == Species.HUMAN);
	}

	/**
	 * 
	 * @param role
	 * @return
	 */
	protected List<Agent> getEstimates(Role role) {
		return estimateReasonMap.getEstimated(me, role);
	}

	/**
	 * 
	 * @param target
	 * @param role
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
	 * 
	 * @param agent
	 * @param role
	 * @return
	 */
	protected boolean isEstimated(Agent agent, Role role) {
		if (estimateReasonMap.containsKey(me) && estimateReasonMap.get(me).containsKey(agent)) {
			estimateReasonMap.get(me).get(agent).hasRole(role);
		}
		return false;
	}

	/**
	 * whether or not the agent is alive
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isAlive(Agent agent) {
		return gameInfo.getStatusMap().get(agent) == Status.ALIVE;
	}

	/**
	 * whether or not the agent is dead
	 * 
	 * @param agent
	 * @return
	 */
	protected boolean isDead(Agent agent) {
		return gameInfo.getStatusMap().get(agent) == Status.DEAD;
	}

	/**
	 * extracts alive agents
	 * 
	 * @param agents
	 * @return
	 */
	protected List<Agent> getAlive(Collection<Agent> agents) {
		return agents.stream().filter(a -> isAlive(a)).distinct().collect(Collectors.toList());
	}

	/**
	 * extracts dead agents
	 * 
	 * @param agents
	 * @return
	 */
	protected List<Agent> getDead(Collection<Agent> agents) {
		return agents.stream().filter(a -> isDead(a)).distinct().collect(Collectors.toList());
	}

	/**
	 * select one agent from the list randomly
	 * 
	 * @param list
	 * @return
	 */
	public static <T> T randomSelect(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get((int) (Math.random() * list.size()));
		}
	}

	/**
	 * returns null if the list is empty, otherwise returns the head of the list
	 * 
	 * @param list
	 * @return
	 */
	public static <T> T selectFirst(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	/**
	 * returns null if the list is empty, otherwise returns the last of the list
	 * 
	 * @param list
	 * @return
	 */
	public static <T> T selectLast(List<T> list) {
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(list.size() - 1);
		}
	}

	/**
	 * whether or not the role exists
	 * 
	 * @param role
	 * @return
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
	 * recursive sentence analysis
	 * 
	 * @param content
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
	 * operator sentence analysis
	 * 
	 * @param content
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
	 * decide voteCandidate
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
	 * 
	 * @param content
	 */
	protected void enqueueTalk(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			talkQueue.offer(replaceSubject(content, me));
		} else {
			talkQueue.offer(content);
		}
	}

	/**
	 * 
	 * @return
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
	 * adjust vote based on vote declaration status
	 * 
	 * @param vrmap     vote declaration status
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
	 * 
	 * @param content
	 * @param newSubject
	 * @return
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
	 * 
	 * @param subject
	 * @param talkType
	 * @param talkDay
	 * @param talkID
	 * @return
	 */
	protected static Content agreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new AgreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/**
	 * 
	 * @param subject
	 * @param talkType
	 * @param talkDay
	 * @param talkID
	 * @return
	 */
	protected static Content disagreeContent(Agent subject, TalkType talkType, int talkDay, int talkID) {
		return new Content(new DisagreeContentBuilder(subject, talkType, talkDay, talkID));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content voteContent(Agent subject, Agent target) {
		return new Content(new VoteContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content votedContent(Agent subject, Agent target) {
		return new Content(new VotedContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content attackContent(Agent subject, Agent target) {
		return new Content(new AttackContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content attackedContent(Agent subject, Agent target) {
		return new Content(new AttackedContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content guardContent(Agent subject, Agent target) {
		return new Content(new GuardCandidateContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content guardedContent(Agent subject, Agent target) {
		return new Content(new GuardedAgentContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param role
	 * @return
	 */
	protected static Content estimateContent(Agent subject, Agent target, Role role) {
		return new Content(new EstimateContentBuilder(subject, target == null ? Content.ANY : target, role));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param role
	 * @return
	 */
	protected static Content coContent(Agent subject, Agent target, Role role) {
		return new Content(new ComingoutContentBuilder(subject, target == null ? Content.ANY : target, role));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param content
	 * @return
	 */
	protected static Content requestContent(Agent subject, Agent target, Content content) {
		return new Content(new RequestContentBuilder(subject, target == null ? Content.ANY : target, content));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param content
	 * @return
	 */
	protected static Content inquiryContent(Agent subject, Agent target, Content content) {
		return new Content(new InquiryContentBuilder(subject, target == null ? Content.ANY : target, content));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @return
	 */
	protected static Content divinationContent(Agent subject, Agent target) {
		return new Content(new DivinationContentBuilder(subject, target == null ? Content.ANY : target));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param result
	 * @return
	 */
	protected static Content divinedContent(Agent subject, Agent target, Species result) {
		return new Content(new DivinedResultContentBuilder(subject, target == null ? Content.ANY : target, result));
	}

	/**
	 * 
	 * @param subject
	 * @param target
	 * @param result
	 * @return
	 */
	protected static Content identContent(Agent subject, Agent target, Species result) {
		return new Content(new IdentContentBuilder(subject, target == null ? Content.ANY : target, result));
	}

	/**
	 * 
	 * @param subject
	 * @param contents
	 * @return
	 */
	protected static Content andContent(Agent subject, Content... contents) {
		return new Content(new AndContentBuilder(subject, contents));
	}

	/**
	 * 
	 * @param subject
	 * @param contents
	 * @return
	 */
	protected static Content orContent(Agent subject, Content... contents) {
		return new Content(new OrContentBuilder(subject, contents));
	}

	/**
	 * 
	 * @param subject
	 * @param content1
	 * @param content2
	 * @return
	 */
	protected static Content xorContent(Agent subject, Content content1, Content content2) {
		return new Content(new XorContentBuilder(subject, content1, content2));
	}

	/**
	 * 
	 * @param subject
	 * @param content
	 * @return
	 */
	protected static Content notContent(Agent subject, Content content) {
		return new Content(new NotContentBuilder(subject, content));
	}

	/**
	 * 
	 * @param subject
	 * @param day
	 * @param content
	 * @return
	 */
	protected static Content dayContent(Agent subject, int day, Content content) {
		return new Content(new DayContentBuilder(subject, day, content));
	}

	/**
	 * 
	 * @param subject
	 * @param reason
	 * @param action
	 * @return
	 */
	protected static Content becauseContent(Agent subject, Content reason, Content action) {
		return new Content(new BecauseContentBuilder(subject, reason, action));
	}

	/**
	 * 
	 * @param content
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
	 * clear talk queue
	 */
	protected void cancelAllTalk() {
		talkQueue.clear();
	}

	/**
	 * 
	 * @param role
	 */
	protected void talkCo(Role role) {
		cancelCo();
		enqueueTalk(coContent(me, me, role));
	}

	/**
	 * 
	 */
	protected void cancelCo() {
		cancelTalk(coContent(me, me, Role.ANY));
	}

	/**
	 * 
	 * @param target
	 * @param role
	 */
	protected void talkEstimate(Agent target, Role role) {
		cancelEstimate(target);
		enqueueTalk(estimateContent(me, target, role));
	}

	/**
	 * 
	 * @param target
	 */
	protected void cancelEstimate(Agent target) {
		cancelTalk(estimateContent(me, target, Role.ANY));
	}

	/**
	 * 
	 * @param target
	 */
	protected void talkVoting(Agent target) {
		cancelVoting();
		enqueueTalk(voteContent(me, target));
	}

	/**
	 * 
	 */
	protected void cancelVoting() {
		cancelTalk(voteContent(me, Content.ANY));
	}

	/**
	 * 
	 * @param target
	 * @param result
	 */
	protected void talkDivined(Agent target, Species result) {
		cancelDivined(target);
		enqueueTalk(divinedContent(me, target, result));
	}

	/**
	 * 
	 * @param target
	 */
	protected void cancelDivined(Agent target) {
		cancelTalk(divinedContent(me, target, Species.ANY));
	}

	/**
	 * 
	 * @param target
	 * @param result
	 */
	protected void talkIdentified(Agent target, Species result) {
		cancelIdentified(target);
		enqueueTalk(identContent(me, target, result));
	}

	/**
	 * 
	 * @param target
	 */
	protected void cancelIdentified(Agent target) {
		cancelTalk(identContent(me, target, Species.ANY));
	}

}
