/**
 * Possessed.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.ArrayList;
import java.util.List;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.JudgeToSend;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.GameInfoModifier;
import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * possessed
 * 
 * @author otsuki
 */
public class Possessed extends BasePlayer {

	private BasePlayer innerPlayer;
	private Judge fakeDivination;
	private MetaInfo fakeMetaInfo;
	private List<Agent> divinedList = new ArrayList<>();

	public Possessed(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo);
		this.fakeMetaInfo = fakeMetaInfo;
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);

		fakeDivination = null;
		innerPlayer = new Seer(fakeMetaInfo);
		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo);
		fakeMetaInfo.initialize(fakeGameInfo);
		innerPlayer.initialize(fakeGameInfo, gameSetting);
		divinedList.clear();
	}

	private GameInfo getFakeGameInfo(GameInfo gameInfo) {
		GameInfoModifier gim = new GameInfoModifier(gameInfo);
		gim.setFakeRole(Role.SEER);
		if (fakeDivination != null) {
			gim.setDivineResult(new JudgeToSend(fakeDivination));
		}
		return gim.toGameInfo();
	}

	@Override
	public void update(GameInfo gameInfo) {
		boolean isFakeDivineTime = getDate() > -1 && gameInfo.getDay() == getDate() + 1;
		super.update(gameInfo);

		if (isFakeDivineTime) {
			fakeDivination = nextJudge();
		}

		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo);
		fakeMetaInfo.update(fakeGameInfo);
		innerPlayer.update(fakeGameInfo);
	}

	private Judge nextJudge() {
		List<Agent> candidates = getAliveOthers();
		candidates.removeAll(divinedList);
		if (candidates.isEmpty()) {
			return null;
		}
		Judge next;
		switch (getDate()) {
		case 1:
			next = new Judge(getDate(), getMe(), selectMin(Role.WEREWOLF, candidates), Species.WEREWOLF);
			break;
		case 2:
			next = new Judge(getDate(), getMe(), selectMax(Role.WEREWOLF, candidates), Species.HUMAN);
			break;
		case 3:
			next = new Judge(getDate(), getMe(), selectMin(Role.WEREWOLF, candidates), Species.WEREWOLF);
			break;
		default:
			next = new Judge(getDate(), getMe(), selectMax(Role.WEREWOLF, candidates), Species.HUMAN);
			break;
		}
		divinedList.add(next.getTarget());
		return next;
	}

	@Override
	public void dayStart() {
		super.dayStart();
		innerPlayer.dayStart();
	}

	@Override
	public String talk() {
		if (getGameInfo().getAliveAgentList().size() <= 3) {
			if (!isPP) {
				isPP = true;
				talkCo(Role.WEREWOLF);
			}
			return super.talk();
		} else {
			return innerPlayer.talk();
		}
	}

	@Override
	protected void chooseVoteCandidate(boolean isLast) {
		voteCandidate = innerPlayer.vote();
		if (isLast) {
			if (getGameInfo().getAliveAgentList().size() <= 3) {
				voteCandidate = selectMin(Role.WEREWOLF, getAliveOthers());
			} else {
				if (!innerPlayer.getWolf(getAliveOthers()).contains(voteCandidate)) {
					Agent wolf = selectMax(Role.WEREWOLF, getAliveOthers());
					Agent target = getVoteReasonMap().getTarget(wolf);
					voteCandidate = target == null || isDead(target) ? selectMin(Role.WEREWOLF, getAliveOthers()) : target;
				}
			}
		}
	}

	@Override
	public String whisper() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent attack() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent divine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Agent guard() {
		throw new UnsupportedOperationException();
	}

}
