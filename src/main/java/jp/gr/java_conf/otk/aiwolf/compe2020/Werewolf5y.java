/**
 * Werewolf5y.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Werewolf for 5 agent village with no vote declaration on day 1.
 * 
 * @author otsuki
 */
public class Werewolf5y extends Werewolf5 {

	/**
	 * Constructs Werewolf5y with meta information.
	 * 
	 * @param metaInfo     meta information for werewolf
	 * @param fakeMetaInfo meta information for fake role
	 */
	public Werewolf5y(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo, fakeMetaInfo);
	}

	@Override
	public String talk() {
		switch (getDate()) {
		case 1:
			voteCandidate = selectMax(Role.VILLAGER, getEstimateOf(Role.VILLAGER));
			if (voteCandidate != declaredVoteCandidate) {
				enqueueTalk(estimateContent(getMe(), voteCandidate, Role.WEREWOLF));
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

}
