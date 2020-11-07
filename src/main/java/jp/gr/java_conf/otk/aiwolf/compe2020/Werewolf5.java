/**
 * Werewolf5.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * werewolf for 5 agent village
 * 
 * @author otsuki
 */
public class Werewolf5 extends BasePlayer5 {

	private BasePlayer5 innerVillager;
	protected MetaInfo fakeMetaInfo;

	public Werewolf5(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo);
		this.fakeMetaInfo = fakeMetaInfo;
		innerVillager = new Villager5(fakeMetaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo, Role.VILLAGER, null);
		fakeMetaInfo.initialize(fakeGameInfo);
		innerVillager.initialize(fakeGameInfo, gameSetting);
	}

	@Override
	public void update(GameInfo gameInfo) {
		super.update(gameInfo);
		GameInfo fakeGameInfo = getFakeGameInfo(gameInfo, Role.VILLAGER, null);
		fakeMetaInfo.update(fakeGameInfo);
		innerVillager.update(fakeGameInfo);
	}

	@Override
	public String talk() {
		switch (getDate()) {
		case 1:
			voteCandidate = innerVillager.getEstimateOf(Role.WEREWOLF).get(0);
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				declaredVoteCandidate = voteCandidate;
			}
			break;
		case 2:
			voteCandidate = selectMin(Role.POSSESSED, getAliveOthers());
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				Agent ally = selectMax(Role.POSSESSED, getAliveOthers(), voteCandidate);
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

	@Override
	public Agent vote() {
		if (!isRevote) {
			getVoteReasonMap().put(getMe(), voteCandidate);
			voteCandidate = adjustVote(getVoteReasonMap(), getMe(), 1.0);
		} else {
			VoteReasonMap vmap = new VoteReasonMap();
			getGameInfo().getLatestVoteList().forEach(v -> vmap.put(v.getAgent(), v.getTarget()));
			voteCandidate = adjustVote(vmap, getMe(), 1.0);
		}
		isRevote = true;
		return voteCandidate;
	}

	@Override
	public Agent attack() {
		return selectMin(Role.POSSESSED, getAliveOthers());
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
