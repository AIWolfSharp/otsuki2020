/**
 * Possessed5.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.Deque;
import java.util.LinkedList;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.data.Species;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.GameInfoModifier;
import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * possessed for 5 agent village
 * 
 * @author otsuki
 */
public class Possessed5 extends BasePlayer5 {

	private BasePlayer5 innerSeer;
	private MetaInfo fakeMetaInfo;
	private Agent divinedHuman;
	private Deque<GameInfo> gameInfoList = new LinkedList<>();

	public Possessed5(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo);
		this.fakeMetaInfo = fakeMetaInfo;
		innerSeer = new Seer5(fakeMetaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		divinedHuman = null;
		gameInfoList.clear();

		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo, Role.SEER, null);
		fakeMetaInfo.initialize(fakeGameInfo);
		innerSeer.initialize(fakeGameInfo, gameSetting);
	}

	@Override
	public void update(GameInfo gameInfo) {
		super.update(gameInfo);
		if (getDate() > 0) {
			gameInfoList.offer(new GameInfoModifier(gameInfo).toGameInfo());
		} else {
			GameInfo fakeGameInfo = getFakeGameInfo(gameInfo, Role.SEER, null);
			fakeMetaInfo.update(fakeGameInfo);
			innerSeer.update(fakeGameInfo);
		}
	}

	@Override
	public String talk() {
		Agent werewolf = getEstimateOf(Role.WEREWOLF).get(0);

		switch (getDate()) {
		case 1:
			switch (getTurn()) {
			case 0:
				enqueueTalk(coContent(getMe(), getMe(), Role.SEER));
				break;
			case 1:
				enqueueTalk(divinedContent(getMe(), werewolf, Species.HUMAN));
				divinedHuman = werewolf;
				Judge fakeDivination = new Judge(getDate(), getMe(), divinedHuman, Species.HUMAN);
				GameInfo fakeGameInfo = getFakeGameInfo(gameInfoList.poll(), Role.SEER, fakeDivination);
				fakeMetaInfo.update(fakeGameInfo);
				innerSeer.update(fakeGameInfo);
				innerSeer.dayStart();
				fakeGameInfo = getFakeGameInfo(gameInfoList.poll(), Role.SEER, fakeDivination);
				fakeMetaInfo.update(fakeGameInfo);
				innerSeer.update(fakeGameInfo);
				innerSeer.talk();
				fakeGameInfo = getFakeGameInfo(gameInfoList.poll(), Role.SEER, fakeDivination);
				fakeMetaInfo.update(fakeGameInfo);
				innerSeer.update(fakeGameInfo);
				innerSeer.talk();
				break;
			default:
				fakeGameInfo = getFakeGameInfo(gameInfoList.poll(), Role.SEER, new Judge(getDate(), getMe(), divinedHuman, Species.HUMAN));
				fakeMetaInfo.update(fakeGameInfo);
				innerSeer.update(fakeGameInfo);
				innerSeer.talk();
				voteCandidate = innerSeer.getEstimateNotHaving(Role.WEREWOLF, divinedHuman).get(0);
				if (voteCandidate != declaredVoteCandidate) {
					enqueueTalk(voteContent(getMe(), voteCandidate));
					declaredVoteCandidate = voteCandidate;
				}
				break;
			}
			break;
		case 2:
			voteCandidate = selectMin(Role.WEREWOLF, getAliveOthers(), werewolf);
			if (getTurn() == 0) {
				enqueueTalk(divinedContent(getMe(), voteCandidate, Species.WEREWOLF));
			}
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				enqueueTalk(requestContent(getMe(), werewolf, voteContent(werewolf, voteCandidate)));
				declaredVoteCandidate = voteCandidate;
			}
			if (!isPP) {
				isPP = getAliveOthers().size() == 2 && (getRoleCount(Role.POSSESSED) == 1 || getPpAbility(werewolf) || isCo(Role.WEREWOLF) || isCo(Role.POSSESSED));
				if (isPP) {
					cancelAllTalk();
					enqueueTalk(coContent(getMe(), getMe(), Role.WEREWOLF));
					declaredVoteCandidate = null;
				}
			}
			break;
		default:
			break;
		}
		nextTurn();
		return dequeueTalk();
	}

	@Override
	public Agent vote() {
		Agent werewolf = getEstimateOf(Role.WEREWOLF).get(0);
		voteCandidate = selectMin(Role.WEREWOLF, getAliveOthers(), werewolf);
		if (!isRevote) {
			getVoteReasonMap().put(getMe(), voteCandidate);
			voteCandidate = adjustVote(getVoteReasonMap(), werewolf, 1.0);
		} else {
			VoteReasonMap vmap = new VoteReasonMap();
			getGameInfo().getLatestVoteList().forEach(v -> vmap.put(v.getAgent(), v.getTarget()));
			voteCandidate = adjustVote(vmap, werewolf, 1.0);
		}
		isRevote = true;
		return voteCandidate;
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
