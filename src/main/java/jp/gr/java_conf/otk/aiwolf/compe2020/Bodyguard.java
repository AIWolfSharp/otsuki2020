/**
 * Bodyguard.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import static java.util.Comparator.*;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * bodyguadd
 * 
 * @author otsuki
 */
public class Bodyguard extends Villager {

	public Bodyguard(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public Agent guard() {
		return getAliveOthers().stream().max(comparing(a -> guardEval(a))).orElse(null);
	}

	private double guardEval(Agent agent) {
		return getProbOf(agent, Role.VILLAGER) + getProbOf(agent, Role.SEER) + getProbOf(agent, Role.MEDIUM)
				- getProbOf(agent, Role.POSSESSED) - getProbOf(agent, Role.WEREWOLF)
				+ (getCoRole(agent) == Role.SEER ? 1.0 : 0.0) + (isFakeSeer(agent) ? -2.0 : 0.0)
				+ (getCoRole(agent) == Role.MEDIUM ? 1.0 : 0.0) + (isFakeMedium(agent) ? -2.0 : 0.0)
				+ 3 * getWinCount(agent) / (getGameCount() + 0.01);
	}

}
