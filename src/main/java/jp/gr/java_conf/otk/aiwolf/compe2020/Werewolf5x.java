/**
 * Werewolf5x.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * werewolf for 5 agent village (version x)
 * 
 * @author otsuki
 */
public class Werewolf5x extends Werewolf5 {

	public Werewolf5x(MetaInfo metaInfo, MetaInfo fakeMetaInfo) {
		super(metaInfo, fakeMetaInfo);
	}

	@Override
	public String talk() {
		switch (getDate()) {
		case 1:
			voteCandidate = selectMax(Role.VILLAGER, getEstimateOf(Role.VILLAGER));
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