/**
 * RoleAssignPlayer.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * Player which assigns player class according to its role.
 * 
 * @author otsuki
 */
public class RoleAssignPlayer implements Player {

	private Player villager;
	private Player seer;
	private Player medium;
	private Player bodyguard;
	private Player possessed;
	private Player werewolf;

	private Player villager5;
	private Player seer5;
	private Player possessed5;
	private Player werewolf5;
	private Player werewolf5s;
	private Player werewolf5x;
	private Player werewolf5y;

	private Player player;

	private MetaInfo metaInfo;
	private MetaInfo fakeMetaInfo;

	private int winCountAsWolf;
	private int myLastWinCount;
	private Agent me;
	private Role myRole;
	private int werewolf5Mode;

	/**
	 * Constructs RoleAssignPlayer with meta information.
	 * 
	 * @param metaInfo meta information
	 */
	public RoleAssignPlayer(MetaInfo metaInfo) {
		this.metaInfo = metaInfo;
		fakeMetaInfo = new MetaInfo();

		villager = new Villager(metaInfo);
		seer = new Seer(metaInfo);
		medium = new Medium(metaInfo);
		bodyguard = new Bodyguard(metaInfo);
		possessed = new Possessed(metaInfo, fakeMetaInfo);
		werewolf = new Werewolf(metaInfo, fakeMetaInfo);

		villager5 = new Villager5(metaInfo);
		seer5 = new Seer5(metaInfo);
		possessed5 = new Possessed5(metaInfo, fakeMetaInfo);
		werewolf5 = new Werewolf5(metaInfo, fakeMetaInfo);
		werewolf5s = new Werewolf5s(metaInfo, fakeMetaInfo);
		werewolf5x = new Werewolf5x(metaInfo, fakeMetaInfo);
		werewolf5y = new Werewolf5y(metaInfo, fakeMetaInfo);
	}

	@Override
	public String getName() {
		return "RoleAssignPlayer";
	}

	@Override
	public void update(GameInfo gameInfo) {
		player.update(gameInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		int playerNum = gameSetting.getPlayerNum();
		me = gameInfo.getAgent();
		myRole = gameInfo.getRole();
		metaInfo.incrementRoleCount(myRole);
		switch (myRole) {
		case VILLAGER:
			player = playerNum == 5 ? villager5 : villager;
			break;
		case SEER:
			player = playerNum == 5 ? seer5 : seer;
			break;
		case MEDIUM:
			player = medium;
			break;
		case BODYGUARD:
			player = bodyguard;
			break;
		case POSSESSED:
			player = playerNum == 5 ? possessed5 : possessed;
			break;
		case WEREWOLF:
			if (playerNum == 5) {
				if (metaInfo.getRoleCount(Role.WEREWOLF) == 6) {
					if (winCountAsWolf < 2) {
						werewolf5Mode = 1;
					} else {
						werewolf5Mode = 2;
					}
				}
				switch (werewolf5Mode) {
				case 0:
					player = Math.random() < 0.5 ? werewolf5x : werewolf5y;
					break;
				case 1:
					if (metaInfo.getGamecount() < 40) {
						player = Math.random() < 0.5 ? werewolf5x : werewolf5y;
					} else if (metaInfo.getGamecount() < 80) {
						player = werewolf5s;
					} else {
						player = werewolf5;
					}
					break;
				case 2:
					player = Math.random() < 0.5 ? werewolf5 : werewolf5s;
					break;
				default:
					break;
				}
			} else {
				player = werewolf;
			}
			break;
		default:
			player = villager;
			break;
		}
		player.initialize(gameInfo, gameSetting);

		myLastWinCount = metaInfo.getWinCount(me);
	}

	@Override
	public final void dayStart() {
		player.dayStart();
	}

	@Override
	public final String talk() {
		return player.talk();
	}

	@Override
	public final String whisper() {
		return player.whisper();
	}

	@Override
	public final Agent vote() {
		return player.vote();
	}

	@Override
	public final Agent attack() {
		return player.attack();
	}

	@Override
	public final Agent divine() {
		return player.divine();
	}

	@Override
	public final Agent guard() {
		return player.guard();
	}

	@Override
	public final void finish() {
		player.finish();
		if (myRole == Role.WEREWOLF) {
			winCountAsWolf += metaInfo.getWinCount(me) - myLastWinCount;
		}
	}

}
