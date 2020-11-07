/**
 * Content.java
 * Copyright (c) 2020 OTSUKI Takashi
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;

/**
 * A subclass of Content including talk's information.
 * 
 * @author otsuki
 *
 */
public class Content extends org.aiwolf.client.lib.Content {

	private Agent talker;

	public Agent getTalker() {
		return talker;
	}

	private int index;

	public int getIndex() {
		return index;
	}

	private int date;

	public int getTalkDate() {
		return date;
	}

	private int turn;

	public int getTurn() {
		return turn;
	}

	/**
	 * @param input
	 */
	public Content(Talk talk) {
		super(((talk.getText().equals(Talk.SKIP) || talk.getText().equals(Talk.OVER)) ? "" : talk.getAgent()) + Content.stripSubject(talk.getText()));
		talker = talk.getAgent();
		index = talk.getIdx();
		date = talk.getDay();
		turn = talk.getTurn();
	}

}
