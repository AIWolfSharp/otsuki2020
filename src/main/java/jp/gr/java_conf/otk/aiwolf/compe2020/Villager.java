/**
 * Villager.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import static java.util.Comparator.*;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * villager
 * 
 * @author otsuki
 */
public class Villager extends BasePlayer {

	public Villager(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public void dayStart() {
		super.dayStart();
	}

	/**
	 * 
	 */
	protected void chooseVoteCandidate0() {
		voteCandidate = getAliveOthers().stream().max(comparing(a -> voteEval1(a))).orElse(null);
		getVoteReasonMap().put(getMe(), voteCandidate);
		if (getVoteReasonMap().getVoteCount(voteCandidate) < getAliveOthers().size() * 0.5) {
			voteCandidate = getAliveOthers().stream().max(comparing(a -> voteEval2(a))).orElse(null);
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
				if (isLikeWolf(voteCandidate)) {
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

	private double voteEval1(Agent agent) {
		return getVoteReasonMap().getVoteCount(agent) + getProbOf(agent, Role.WEREWOLF);
	}

	private double voteEval2(Agent agent) {
		return getProbOf(agent, Role.WEREWOLF) + 0.1 * getProbOf(agent, Role.POSSESSED)
				+ (isWolf(agent) ? 1.0 : 0.0) + (isHuman(agent) ? -1.0 : 0.0)
				+ (isFakeSeer(agent) ? 0.5 : 0.0) + (isFakeMedium(agent) ? 0.5 : 0.0);
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
