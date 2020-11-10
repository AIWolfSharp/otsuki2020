/**
 * Villager5.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Villager for 5 agent village
 * 
 * @author otsuki
 */
public class Villager5 extends BasePlayer5 {

	/**
	 * Constructs Villager5 with meta information.
	 * 
	 * @param metaInfo meta information
	 */
	public Villager5(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public String talk() {
		voteCandidate = getEstimateOf(Role.WEREWOLF).get(0);
		switch (getDate()) {
		case 1:
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				declaredVoteCandidate = voteCandidate;
			}
			break;
		case 2:
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(voteContent(getMe(), voteCandidate));
				Agent ally = selectMin(Role.WEREWOLF, getAliveOthers(), voteCandidate);
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
		if (getDate() == 1) {
			if (!isRevote) {
				getVoteReasonMap().put(getMe(), voteCandidate);
				voteCandidate = adjustVote(getVoteReasonMap(), getMe(), 1.0);
			} else {
				VoteReasonMap vmap = new VoteReasonMap();
				getGameInfo().getLatestVoteList().forEach(v -> vmap.put(v.getAgent(), v.getTarget()));
				voteCandidate = adjustVote(vmap, getMe(), 1.0);
			}
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
