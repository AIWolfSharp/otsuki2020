/**
 * Content.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020.common;

import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Talk;

/**
 * A subclass of Content including the information if talk this originated.
 * 
 * @author otsuki
 *
 */
public class Content extends org.aiwolf.client.lib.Content {

	private Agent talker;

	/**
	 * Returns the agent which uttered this.
	 * 
	 * @return the talker
	 */
	public Agent getTalker() {
		return talker;
	}

	private int index;

	/**
	 * Returns the index of the talk this originated.
	 * 
	 * @return the index of the talk
	 */
	public int getIndex() {
		return index;
	}

	private int date;

	/**
	 * Returns the date of the talk this originated.
	 * 
	 * @return the date of the talk
	 */
	public int getTalkDate() {
		return date;
	}

	private int turn;

	/**
	 * Returns the turn of the talk this originated.
	 * 
	 * @return the turn of the talk
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * Constructs a Content from a Talk.
	 * 
	 * @param talk a Talk
	 */
	public Content(Talk talk) {
		super(((talk.getText().equals(Talk.SKIP) || talk.getText().equals(Talk.OVER)) ? "" : talk.getAgent()) + Content.stripSubject(talk.getText()));
		talker = talk.getAgent();
		index = talk.getIdx();
		date = talk.getDay();
		turn = talk.getTurn();
	}

}
