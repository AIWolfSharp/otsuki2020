/**
 * Medium.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.List;

import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Judge;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameInfo;
import org.aiwolf.common.net.GameSetting;

import jp.gr.java_conf.otk.aiwolf.compe2020.common.MetaInfo;

/**
 * medium
 * 
 * @author otsuki
 */
public class Medium extends Villager {

	private int coDate;
	private boolean hasCo;
	private int myIdentHead;

	public Medium(MetaInfo metaInfo) {
		super(metaInfo);
	}

	@Override
	public void initialize(GameInfo gameInfo, GameSetting gameSetting) {
		super.initialize(gameInfo, gameSetting);
		coDate = 3;
		hasCo = false;
		myIdentHead = 0;
	}

	@Override
	public String talk() {
		if (!hasCo && (getDate() >= coDate || isCo(Role.MEDIUM) || foundWolf())) {
			talkCo(Role.MEDIUM);
			hasCo = true;
		}
		if (hasCo) {
			List<Judge> myIdents = getMyIdentifications();
			int nextHead = myIdents.size();
			List<Judge> idents = myIdents.subList(myIdentHead, nextHead);
			Content[] judges = idents.stream().map(j -> dayContent(getMe(), j.getDay(),
					identContent(getMe(), j.getTarget(), j.getResult()))).toArray(size -> new Content[size]);
			if (judges.length == 1) {
				enqueueTalk(judges[0]);
			} else if (judges.length > 1) {
				enqueueTalk(andContent(getMe(), judges));
			}
			idents.forEach(j -> talkIdentified(j.getTarget(), j.getResult()));
			myIdentHead = nextHead;
		}
		return super.talk();
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
