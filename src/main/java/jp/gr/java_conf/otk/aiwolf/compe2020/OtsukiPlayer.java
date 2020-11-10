/**
 * OtsukiPlayer.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.Map.Entry;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Team otsuki's player.
 * 
 * @author otsuki
 *
 */
public class OtsukiPlayer implements Player {

	private GameInfo gameInfo;

	private MetaInfo metaInfo;

	private Player player;

	/**
	 * Constructs an OtsukiPlayer.
	 */
	public OtsukiPlayer() {
		metaInfo = new MetaInfo();

		player = new RoleAssignPlayer(metaInfo);
	}

	@Override
	public Agent attack() {
		return player.attack();
	}

	@Override
	public void dayStart() {
		player.dayStart();
	}

	@Override
	public Agent divine() {
		return player.divine();
	}

	@Override
	public void finish() {
		metaInfo.finish(gameInfo);

		int nAliveWolf = (int) gameInfo.getAliveAgentList().stream().filter(a -> gameInfo.getRoleMap().get(a) == Role.WEREWOLF).count();
		for (Entry<Agent, Role> entry : gameInfo.getRoleMap().entrySet()) {
			if ((entry.getValue() == Role.WEREWOLF || entry.getValue() == Role.POSSESSED) == (nAliveWolf > 0)) {
				metaInfo.incrementWinCount(entry.getKey());
			}
		}

		player.finish();
	}

	@Override
	public String getName() {
		return "otsuki";
	}

	@Override
	public Agent guard() {
		return player.guard();
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		metaInfo.initialize(gameInfo);
		player.initialize(gameInfo, gameSetting);
	}

	@Override
	public String talk() {
		return player.talk();
	}

	@Override
	public void update(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		metaInfo.update(gameInfo);
		player.update(gameInfo);
	}

	@Override
	public Agent vote() {
		return player.vote();
	}

	@Override
	public String whisper() {
		return player.whisper();
	}

}
