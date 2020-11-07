/**
 * GameInfoModifier.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameInfoToSend;
import org.aiwolf.common.net.JudgeToSend;
import org.aiwolf.common.net.TalkToSend;
import org.aiwolf.common.net.VoteToSend;

/**
 * utility for modification of GameInfo
 * 
 * @author otsuki
 *
 */
public class GameInfoModifier extends GameInfoToSend {

	GameInfo gameInfo;

	public GameInfoModifier(GameInfo gameInfo) {
		this.gameInfo = gameInfo;

		setDay(gameInfo.getDay());
		setAgent(gameInfo.getAgent().getAgentIdx());
		if (gameInfo.getMediumResult() != null) {
			setMediumResult(new JudgeToSend(gameInfo.getMediumResult()));
		}
		if (gameInfo.getDivineResult() != null) {
			setDivineResult(new JudgeToSend(gameInfo.getDivineResult()));
		}
		if (gameInfo.getExecutedAgent() != null) {
			setExecutedAgent(gameInfo.getExecutedAgent().getAgentIdx());
		}
		if (gameInfo.getLatestExecutedAgent() != null) {
			setLatestExecutedAgent(gameInfo.getLatestExecutedAgent().getAgentIdx());
		}
		if (gameInfo.getAttackedAgent() != null) {
			setAttackedAgent(gameInfo.getAttackedAgent().getAgentIdx());
		}
		if (gameInfo.getCursedFox() != null) {
			setCursedFox(gameInfo.getCursedFox().getAgentIdx());
		}
		if (gameInfo.getGuardedAgent() != null) {
			setGuardedAgent(gameInfo.getGuardedAgent().getAgentIdx());
		}
		setVoteList(gameInfo.getVoteList().stream().map(v -> new VoteToSend(v)).collect(Collectors.toList()));
		setLatestVoteList(gameInfo.getLatestVoteList().stream().map(v -> new VoteToSend(v)).collect(Collectors.toList()));
		setAttackVoteList(gameInfo.getAttackVoteList().stream().map(v -> new VoteToSend(v)).collect(Collectors.toList()));
		setLatestAttackVoteList(gameInfo.getLatestAttackVoteList().stream().map(v -> new VoteToSend(v)).collect(Collectors.toList()));
		setTalkList(gameInfo.getTalkList().stream().map(t -> new TalkToSend(t)).collect(Collectors.toList()));
		setWhisperList(gameInfo.getWhisperList().stream().map(t -> new TalkToSend(t)).collect(Collectors.toList()));
		setStatusMap(new LinkedHashMap<>(gameInfo.getStatusMap().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getAgentIdx(), e -> e.getValue().toString()))));
		setRoleMap(new LinkedHashMap<>(gameInfo.getRoleMap().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getAgentIdx(), e -> e.getValue().toString()))));
		setRemainTalkMap(new LinkedHashMap<>(gameInfo.getRemainTalkMap().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getAgentIdx(), e -> e.getValue()))));
		setRemainWhisperMap(new LinkedHashMap<>(gameInfo.getRemainWhisperMap().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey().getAgentIdx(), e -> e.getValue()))));
		setLastDeadAgentList(gameInfo.getLastDeadAgentList().stream().map(a -> a.getAgentIdx()).collect(Collectors.toList()));
		setExistingRoleList(gameInfo.getExistingRoles().stream().map(r -> r.toString()).collect(Collectors.toList()));
	}

	public GameInfoModifier setFakeRole(Role role) {
		if (role != null) {
			LinkedHashMap<Integer, String> fakeRoleMap = new LinkedHashMap<>();
			fakeRoleMap.put(gameInfo.getAgent().getAgentIdx(), role.toString());
			setRoleMap(fakeRoleMap);
		}
		return this;
	}

	public GameInfoModifier setDivineResult(Judge judge) {
		if (judge != null) {
			setDivineResult(new JudgeToSend(judge));
		}
		return this;
	}

}
