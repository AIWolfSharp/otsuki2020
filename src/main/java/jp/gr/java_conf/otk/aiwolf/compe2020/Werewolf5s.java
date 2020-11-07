/**
 * Werewolf5s.java
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
 * werewolf for 5 agent village (fake seer)
 * 
 * @author otsuki
 */
public class Werewolf5s extends Werewolf5 {

	private BasePlayer5 innerSeer;
	private MetaInfo fakeMetaInfo;
	private Agent divinedHuman;
	private Judge fakeDivination;
	private Deque<GameInfo> gameInfoList = new LinkedList<>();

	public Werewolf5s(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo, fakeMetaInfo);
		this.fakeMetaInfo = fakeMetaInfo;
		innerSeer = new Seer5(fakeMetaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		divinedHuman = null;
		fakeDivination = null;
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
		Agent possessed = getEstimateOf(Role.POSSESSED).get(0);
		switch (getDate()) {
		case 1:
			switch (getTurn()) {
			case 0:
				enqueueTalk(coContent(getMe(), getMe(), Role.SEER));
				break;
			case 1:
				divinedHuman = possessed;
				enqueueTalk(divinedContent(getMe(), divinedHuman, Species.HUMAN));
				fakeDivination = new Judge(getDate(), getMe(), divinedHuman, Species.HUMAN);
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
				fakeGameInfo = getFakeGameInfo(gameInfoList.poll(), Role.SEER, fakeDivination);
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
			voteCandidate = selectMin(Role.POSSESSED, getAliveOthers());
			Agent ally = selectMax(Role.POSSESSED, getAliveOthers(), voteCandidate);
			if (getTurn() == 0) {
				if (divinedHuman == ally) {
					enqueueTalk(divinedContent(getMe(), voteCandidate, Species.WEREWOLF));
				} else {
					enqueueTalk(divinedContent(getMe(), ally, Species.HUMAN));
				}
			}
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				enqueueTalk(requestContent(getMe(), ally, voteContent(ally, voteCandidate)));
				declaredVoteCandidate = voteCandidate;
			}
			if (!isPP) {
				isPP = getAliveOthers().size() == 2 && (isCo(Role.WEREWOLF) || isCo(Role.POSSESSED));
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

}
