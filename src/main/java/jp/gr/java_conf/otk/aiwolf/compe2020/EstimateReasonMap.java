/**
 * EstimateReasonMap.java
 * Copyright (c) 2020 OTSUKI Takashi
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
 * estimate and its reason of each agent
 * 
 * @author otsuki
 */
@SuppressWarnings("serial")
class EstimateReasonMap extends HashMap<Agent, Map<Agent, Estimate>> {

	boolean put(Estimate estimate) {
		if (estimate == null) {
			return false;
		}
		Agent estimater = estimate.getEstimater();
		Agent estimated = estimate.getEstimated();
		if (estimater == null || estimated == null) {
			return false;
		}
		put(estimater, new HashMap<Agent, Estimate>() {
			private static final long serialVersionUID = 1L;
			{
				put(estimated, estimate);
			}
		});
		return true;
	}

	/**
	 * register the utterance if it is estimate, the reason is also registered if any
	 * 
	 * @param content
	 *                utterance
	 * @return true if the registration is success
	 */
	boolean put(Content content) {
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
	 * 
	 * @param estimater
	 * @param estimated
	 * @return
	 */
	Content getContent(Agent estimater, Agent estimated) {
		Estimate estimate = getEstimate(estimater, estimated);
		return estimate != null ? estimate.toContent() : null;
	}

	/**
	 * 
	 * @param estimater
	 * @param estimated
	 * @return
	 */
	Estimate getEstimate(Agent estimater, Agent estimated) {
		return get(estimater) != null ? get(estimater).get(estimated) : null;
	}

	/**
	 * 
	 * @param estimater
	 * @param estimated
	 * @return
	 */
	Content getReason(Agent estimater, Agent estimated) {
		Content content = getContent(estimater, estimated);
		return content != null && content.getOperator() == Operator.BECAUSE ? content.getContentList().get(0) : null;
	}

	List<Agent> getEstimated(Agent estimater, Role role) {
		if (containsKey(estimater)) {
			return get(estimater).values().stream()
					.filter(e -> e.hasRole(role)).map(e -> e.getEstimated()).distinct().collect(Collectors.toList());
		}
		return new ArrayList<Agent>();
	}
}
