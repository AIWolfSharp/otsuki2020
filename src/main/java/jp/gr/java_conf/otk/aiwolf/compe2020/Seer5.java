/**
 * Seer5.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Seer for 5 agent village
 * 
 * @author otsuki
 */
public class Seer5 extends BasePlayer5 {

	/**
	 * Constructs Seer5 with meta information.
	 * 
	 * @param metaInfo meta information
	 */
	public Seer5(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public String talk() {
		switch (getDate()) {
		case 1:
			voteCandidate = getEstimateOf(Role.WEREWOLF).get(0);
			switch (getTurn()) {
			case 0:
				enqueueTalk(coContent(getMe(), getMe(), Role.SEER));
				break;
			case 1:
				Judge divination = getMyDivinationOnDay(1);
				enqueueTalk(divinedContent(getMe(), divination.getTarget(), divination.getResult()));
				break;
			default:
				if (voteCandidate != declaredVoteCandidate) {
					enqueueTalk(voteContent(getMe(), voteCandidate));
					declaredVoteCandidate = voteCandidate;
				}
				break;
			}
			break;
		case 2:
			voteCandidate = selectMax(Role.WEREWOLF, getAliveOthers());
			switch (getTurn()) {
			case 0:
				Judge divination = getMyDivinationOnDay(2);
				enqueueTalk(divinedContent(getMe(), divination.getTarget(), divination.getResult()));
				break;
			default:
				if (voteCandidate != declaredVoteCandidate) {
					enqueueTalk(voteContent(getMe(), voteCandidate));
					Agent human = selectMin(Role.WEREWOLF, getAliveOthers(), voteCandidate);
					enqueueTalk(requestContent(getMe(), human, voteContent(human, voteCandidate)));
					declaredVoteCandidate = voteCandidate;
				}
				break;
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
	public Agent divine() {
		return randomSelect(getAliveOthers());
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
	public Agent guard() {
		throw new UnsupportedOperationException();
	}

}
