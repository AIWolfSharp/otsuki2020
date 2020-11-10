/**
 * EstimateReasonMap.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * A mapping between an agent and its estimate with a reason.
 * 
 * @author otsuki
 */
@SuppressWarnings("serial")
public class EstimateReasonMap extends HashMap<Agent, Map<Agent, Estimate>> {

	/**
	 * Registers an estimate.
	 * 
	 * @param estimate the estimate to be registered
	 * @return true on success
	 */
	public boolean put(Estimate estimate) {
		if (estimate == null) {
			return false;
		}
		Agent estimator = estimate.getEstimator();
		Agent estimated = estimate.getEstimated();
		if (estimator == null || estimated == null) {
			return false;
		}
		put(estimator, new HashMap<Agent, Estimate>() {
			private static final long serialVersionUID = 1L;
			{
				put(estimated, estimate);
			}
		});
		return true;
	}

	/**
	 * Registers the estimate contained in the utterance.
	 * 
	 * @param content the utterance
	 * @return true on success
	 */
	public boolean put(Content content) {
		List<Estimate> estimates = Estimate.parseContent(content);
		if (estimates == null || estimates.isEmpty()) {
			return false;
		}
		for (Estimate e : estimates) {
			if (!put(e)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the agent's estimate about the estimated agent in the form of Content.
	 * 
	 * @param estimater the agent which does estimate
	 * @param estimated the estimated agent
	 * @return the estimate in the form of Content
	 */
	public Content getContent(Agent estimater, Agent estimated) {
		Estimate estimate = getEstimate(estimater, estimated);
		return estimate != null ? estimate.toContent() : null;
	}

	/**
	 * Returns the agent's estimate about the estimated agent in the form of Estimate.
	 * 
	 * @param estimater the agent which does estimate
	 * @param estimated the estimated agent
	 * @return the estimate in the form of Estimate
	 */
	public Estimate getEstimate(Agent estimater, Agent estimated) {
		return get(estimater) != null ? get(estimater).get(estimated) : null;
	}

	/**
	 * Returns the reason of the agent's estimate about the estimated agent.
	 * 
	 * @param estimater the agent which does estimate
	 * @param estimated the estimated agent
	 * @return the reason in the form of Content
	 */
	public Content getReason(Agent estimater, Agent estimated) {
		Content content = getContent(estimater, estimated);
		return content != null && content.getOperator() == Operator.BECAUSE ? content.getContentList().get(0) : null;
	}

	/**
	 * Returns the agents estimated to be the role by the agent which does estimate.
	 * 
	 * @param estimater the agent which does estimate
	 * @param role      the estimated role
	 * @return the list of agents estimated to be the role
	 */
	public List<Agent> getEstimated(Agent estimater, Role role) {
		if (containsKey(estimater)) {
			return get(estimater).values().stream()
					.filter(e -> e.hasRole(role)).map(e -> e.getEstimated()).distinct().collect(Collectors.toList());
		}
		return new ArrayList<Agent>();
	}
}
