/**
 * AttackVoteReasonMap.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.common.data.Agent;

/**
 * A mapping between agent and its declared attack vote with reason.
 * 
 * @author otsuki
 */
@SuppressWarnings("serial")
public class AttackVoteReasonMap extends VoteReasonMap {

	@Override
	public boolean put(Content vote, Content reason) {
		if (vote.getTopic() == Topic.ATTACK) {
			Agent voter = vote.getSubject();
			Agent voted = vote.getTarget();
			return put(voter, voted, reason);
		}
		return false;
	}

	@Override
	public boolean put(Content content) {
		if (content.getTopic() == Topic.ATTACK) {
			return put(content, null);
		} else if (content.getOperator() == Operator.BECAUSE && content.getContentList().get(1).getTopic() == Topic.ATTACK) {
			return put(content.getContentList().get(1), content.getContentList().get(0));
		}
		return false;
	}

}
