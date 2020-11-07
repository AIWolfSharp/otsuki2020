/**
 * Werewolf.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import static java.util.Comparator.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.data.Talk;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.JudgeToSend;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.GameInfoModifier;
import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * werewolf
 * 
 * @author otsuki
 */
public class Werewolf extends BasePlayer {

	private Role fakeRole;
	private Agent attackVoteCandidate;
	private Agent declaredAttackVoteCandidate;
	private int whisperListHead;
	private Deque<Content> whisperQueue = new LinkedList<>();
	private Map<Agent, Role> ourComingoutMap = new HashMap<>();
	private AttackVoteReasonMap attackVoteReasonMap = new AttackVoteReasonMap();
	private BasePlayer innerPlayer;
	private GameSetting gameSetting;
	private GameInfo initialGameInfo;
	private List<Agent> randomHumans;
	private Agent possessed;
	private Agent declaredPossessed;
	private Agent fakeDivineTarget;
	private Species fakeResult;
	private MetaInfo fakeMetaInfo;

	/**
	 * 
	 * @param metaInfo     meta information for werewolf
	 * @param fakeMetaInfo meta information for fake role
	 */
	public Werewolf(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo);
		this.fakeMetaInfo = fakeMetaInfo;
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		initialGameInfo = gameInfo;
		this.gameSetting = gameSetting;

		whisperQueue.clear();
		ourComingoutMap.clear();
		attackVoteReasonMap.clear();
		randomHumans = getHuman(gameInfo.getAgentList());
		Collections.shuffle(randomHumans);
		possessed = null;
		fakeDivineTarget = null;
		fakeResult = null;
		fakeRole = randomSelect(Arrays.asList(Role.VILLAGER, Role.SEER, Role.MEDIUM).stream().filter(r -> exists(r)).collect(Collectors.toList()));
		setupFakeRole(fakeRole);
	}

	private void setupFakeRole(Role fakeRole) {
		switch (fakeRole) {
		case SEER:
			innerPlayer = new Seer(fakeMetaInfo);
			break;
		case MEDIUM:
			innerPlayer = new Medium(fakeMetaInfo);
			break;
		default:
			innerPlayer = new Villager(fakeMetaInfo);
			break;
		}
		GameInfo fakeGameInfo = getFakeGameInfo(initialGameInfo);
		fakeMetaInfo.initialize(fakeGameInfo);
		innerPlayer.initialize(fakeGameInfo, gameSetting);
		enqueueWhisper(coContent(getMe(), getMe(), fakeRole));
	}

	private GameInfo getFakeGameInfo(GameInfo gameInfo) {
		GameInfoModifier gim = new GameInfoModifier(gameInfo);
		gim.setFakeRole(fakeRole);
		switch (fakeRole) {
		case SEER:
			if (fakeDivineTarget != null) {
				Judge fakeJudge = new Judge(gameInfo.getDay(), getMe(), fakeDivineTarget, fakeResult);
				gim.setDivineResult(new JudgeToSend(fakeJudge));
			}
			break;
		case MEDIUM:
			Agent executed = gameInfo.getExecutedAgent();
			if (executed != null) {
				Judge fakeJudge = new Judge(gameInfo.getDay(), getMe(), executed, fakeResult);
				gim.setMediumResult(new JudgeToSend(fakeJudge));
			}
			break;
		default:
			break;
		}
		return gim.toGameInfo();
	}

	@Override
	public void update(GameInfo gameInfo) {
		boolean isFakeDivineTime = getDate() > -1 && gameInfo.getDay() == getDate() + 1;
		super.update(gameInfo);

		if (isFakeDivineTime) {
			fakeDivineTarget = fakeRole == Role.SEER ? innerPlayer.divine() : null;
			fakeResult = nextJudge();
		}

		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo);
		fakeMetaInfo.update(fakeGameInfo);
		innerPlayer.update(fakeGameInfo);

		getWolf(gameInfo.getAgentList()).forEach(a -> innerPlayer.overwriteProbOf(a, Role.WEREWOLF, 0.0));

		processWhisper();

		List<Agent> fakeJudges = getAlive(getFakeSeers());
		fakeJudges.addAll(getAlive(getFakeMediums()));
		fakeJudges.removeIf(a -> isWolf(a));
		if (!fakeJudges.isEmpty()) {
			possessed = selectMax(Role.POSSESSED, fakeJudges);
			innerPlayer.overwriteProbOf(possessed, Role.WEREWOLF, 0.0);

			if (possessed != declaredPossessed) {
				enqueueWhisper(estimateContent(getMe(), possessed, Role.POSSESSED));
				declaredPossessed = possessed;
			}
		} else {
			possessed = null;
		}
	}

	private Species nextJudge() {
		int nWolves = innerPlayer.getWolf(getGameInfo().getAgentList()).size();
		int remain = gameSetting.getRoleNum(Role.WEREWOLF) - 1 - nWolves;
		return remain > 0 && Math.random() < 0.5 ? Species.WEREWOLF : Species.HUMAN;
	}

	private void processWhisper() {
		for (int i = whisperListHead; i < getGameInfo().getWhisperList().size(); i++) {
			Talk whisper = getGameInfo().getWhisperList().get(i);
			Agent whisperer = whisper.getAgent();
			if (whisperer == getMe()) {
				continue;
			}
			Content content = new Content(whisper.getText());
			if (content.getSubject() == Content.UNSPEC) {
				content = replaceSubject(content, whisperer);
			}
			parseWhisper(content);
		}
		whisperListHead = getGameInfo().getWhisperList().size();
	}

	private void parseWhisper(Content content) {
		if (getEstimateReasonMap().put(content)) {
			return;
		}
		if (attackVoteReasonMap.put(content)) {
			return;
		}
		switch (content.getTopic()) {
		case COMINGOUT: // Declaration of FCO
			ourComingoutMap.put(content.getSubject(), content.getRole());
			return;
		default:
			break;
		}
	}

	@Override
	public void dayStart() {
		super.dayStart();
		innerPlayer.dayStart();

		attackVoteCandidate = null;
		declaredAttackVoteCandidate = null;
		possessed = null;
		declaredPossessed = null;
		whisperListHead = 0;
	}

	@Override
	public String talk() {
		if (getGameInfo().getAliveAgentList().size() <= 3) {
			if (!isPP && foundPossessed()) {
				isPP = true;
				talkCo(Role.WEREWOLF);
			}
			return super.talk();
		} else {
			return innerPlayer.talk();
		}
	}

	@Override
	protected boolean foundPossessed() {
		double all = getOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		double alive = getAliveOthers().stream().mapToDouble(a -> getProbOf(a, Role.POSSESSED)).sum();
		return alive > 0.5 * all || isCo(Role.POSSESSED) || isCo(Role.WEREWOLF);
	}

	private Agent chooseAttackVoteCandidate() {
		return getHuman(getAliveOthers()).stream().max(comparing(a -> attackEval(a))).orElse(null);
	}

	private double attackEval(Agent agent) {
		return 0.2 * getProbOf(agent, Role.SEER)
				+ 0.1 * (exists(Role.BODYGUARD) ? getProbOf(agent, Role.BODYGUARD) : 0.0)
				+ 0.1 * (exists(Role.MEDIUM) ? getProbOf(agent, Role.MEDIUM) : 0.0)
				- getProbOf(agent, Role.POSSESSED) + (agent == possessed ? -1.0 : 0.0)
				+ 3 * getWinCount(agent) / (getGameCount() + 0.01);
	}

	@Override
	public Agent attack() {
		return chooseAttackVoteCandidate();
	}

	@Override
	public String whisper() {
		if (getDate() == 0) {
			Role oldFakeRole = fakeRole;
			Collection<Role> fakeRoles = ourComingoutMap.values();
			if (fakeRoles.size() > 0) {
				if (!fakeRoles.contains(Role.SEER)) {
					fakeRole = Role.SEER;
				} else if (!fakeRoles.contains(Role.MEDIUM)) {
					fakeRole = Role.MEDIUM;
				} else {
					fakeRole = Role.VILLAGER;
				}
			}
			if (fakeRole != oldFakeRole) {
				setupFakeRole(fakeRole);
			}
		} else {
			attackVoteCandidate = chooseAttackVoteCandidate();
			if (attackVoteCandidate != null && attackVoteCandidate != declaredAttackVoteCandidate) {
				enqueueWhisper(attackContent(getMe(), attackVoteCandidate));
				declaredAttackVoteCandidate = attackVoteCandidate;
			}
		}
		return dequeueWhisper();
	}

	private void enqueueWhisper(Content content) {
		if (content.getSubject() == Content.UNSPEC) {
			whisperQueue.offer(replaceSubject(content, getMe()));
		} else {
			whisperQueue.offer(content);
		}
	}

	private String dequeueWhisper() {
		if (whisperQueue.isEmpty()) {
			return Talk.SKIP;
		}
		Content content = whisperQueue.poll();
		if (content.getSubject() == getMe()) {
			return Content.stripSubject(content.getText());
		}
		return content.getText();
	}

	@Override
	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent guard() {
		throw new UnsupportedOperationException();
	}

	private double voteEval(Agent agent) {
		return getVoteReasonMap().getVoteCount(agent) + fakeMetaInfo.getRoleEstimator(Role.WEREWOLF).get(agent);
	}

	protected void chooseVoteCandidate0() {
		voteCandidate = getAliveOthers().stream().max(comparing(a -> voteEval(a))).orElse(null);
		getVoteReasonMap().put(getMe(), voteCandidate);
		if (getVoteReasonMap().getVoteCount(voteCandidate) < getAliveOthers().size() * 0.5) {
			voteCandidate = innerPlayer.vote();
			if (isWolf(voteCandidate)) {
				voteCandidate = selectMax(Role.WEREWOLF, getHuman(getAliveOthers()));
			}
			getVoteReasonMap().put(getMe(), voteCandidate);
		}
	}

	@Override
	protected void chooseVoteCandidate(boolean isLast) {
		chooseVoteCandidate0();

		if (isLast) {
			if (isRevote) {
				VoteReasonMap vmap = new VoteReasonMap();
				getGameInfo().getLatestVoteList().forEach(v -> vmap.put(v.getAgent(), v.getTarget()));
				voteCandidate = adjustVote(vmap, getMe(), 0.5);
			} else {
				getVoteReasonMap().put(getMe(), voteCandidate);
				voteCandidate = adjustVote(getVoteReasonMap(), getMe(), 0.5);
			}
		} else {
			if (getTurn() > 1) {
				if (innerPlayer.isLikeWolf(voteCandidate)) {
					if (voteCandidate != declaredWolf) {
						cancelEstimate(declaredWolf);
						talkEstimate(voteCandidate, Role.WEREWOLF);
						declaredWolf = voteCandidate;
					}
				}
			} else {
				voteCandidate = null;
			}
		}
	}

}
