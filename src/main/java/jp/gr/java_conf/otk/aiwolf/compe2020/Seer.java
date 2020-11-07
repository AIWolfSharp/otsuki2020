/**
 * Seer.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import static java.util.Comparator.*;

import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * seer
 * 
 * @author otsuki
 */
public class Seer extends Villager {

	private int coDate;
	private boolean hasCo;
	private int myDivinationHead;

	public Seer(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		coDate = 3;
		hasCo = false;
		myDivinationHead = 0;
	}

	@Override
	protected void chooseVoteCandidate0() {
		voteCandidate = getAliveOthers().stream().max(comparing(a -> voteEval(a))).orElse(null);
		getVoteReasonMap().put(getMe(), voteCandidate);
	}

	private double voteEval(Agent agent) {
		return getProbOf(agent, Role.WEREWOLF) + 0.1 * getProbOf(agent, Role.POSSESSED)
				+ (isWolf(agent) ? 1.0 : 0.0) + (isHuman(agent) ? -1.0 : 0.0)
				+ (isFakeSeer(agent) ? 0.5 : 0.0) + (isFakeMedium(agent) ? 0.5 : 0.0);
	}

	@Override
	public String talk() {
		if (getGameInfo().getAliveAgentList().size() <= 3) {
			if (!isPP && foundPossessed()) {
				isPP = true;
				talkCo(Role.WEREWOLF);
			}
			return super.talk();
		}
		if (!hasCo && (getDate() >= coDate || isCo(Role.SEER) || foundWolf())) {
			talkCo(Role.SEER);
			hasCo = true;
		}
		if (hasCo) {
			List<Judge> myDivinations = getMyDivinations();
			int nextHead = myDivinations.size();
			List<Judge> divinations = myDivinations.subList(myDivinationHead, nextHead);
			if (divinations.size() > 1) {
				Content[] judges = divinations.stream().map(j -> dayContent(getMe(), j.getDay(),
						divinedContent(getMe(), j.getTarget(), j.getResult()))).toArray(size -> new Content[size]);
				if (judges.length == 1) {
					enqueueTalk(judges[0]);
				} else if (judges.length > 1) {
					enqueueTalk(andContent(getMe(), judges));
				}
			}
			divinations.forEach(j -> talkDivined(j.getTarget(), j.getResult()));
			myDivinationHead = nextHead;
		}
		return super.talk();
	}

	@Override
	public Agent divine() {
		return getGray(getAliveOthers()).stream().max(comparing(a -> divineEval(a))).orElse(null);
	}

	private double divineEval(Agent agent) {
		return getProbOf(agent, Role.WEREWOLF);
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
