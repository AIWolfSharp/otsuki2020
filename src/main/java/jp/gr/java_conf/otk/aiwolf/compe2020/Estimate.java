/**
 * Estimate.java
 * 
 * Copyright 2020 OTSUKI Takashi
 * SPDX-License-Identifier: Apache-2.0
 */
package jp.gr.java_conf.otk.aiwolf.compe2020;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aiwolf.client.lib.AndContentBuilder;
import org.aiwolf.client.lib.BecauseContentBuilder;
import org.aiwolf.client.lib.Content;
import org.aiwolf.client.lib.EstimateContentBuilder;
import org.aiwolf.client.lib.Operator;
import org.aiwolf.client.lib.Topic;
import org.aiwolf.client.lib.XorContentBuilder;
import org.aiwolf.common.data.Agent;
import org.aiwolf.common.data.Role;

/**
 * Class representing a role estimate and its reason.
 * 
 * @author otsuki
 *
 */
public class Estimate {

	/**
	 * Parses a sentence and returns estimates included in the sentence.
	 * 
	 * @param content the sentence to be parsed
	 * @return the list of Estimate
	 */
	public static List<Estimate> parseContent(Content content) {
		if (content == null) {
			return null;
		}

		if (content.getTopic() == Topic.ESTIMATE) {
			return Arrays.asList(new Estimate(content.getSubject(), content.getTarget(), content.getRole()));
		}

		if (content.getOperator() == Operator.AND || content.getOperator() == Operator.OR || content.getOperator() == Operator.XOR) {
			List<Estimate> estimates = new ArrayList<>();
			for (Content c : content.getContentList()) {
				if (c.getTopic() == Topic.ESTIMATE) {
					estimates.add(new Estimate(c.getSubject(), c.getTarget(), c.getRole()));
				}
			}
			if (estimates.size() == 0) {
				return null;
			}
			return estimates;
		}

		if (content.getOperator() == Operator.BECAUSE) {
			Content reason = content.getContentList().get(0);
			List<Estimate> estimates = parseContent(content.getContentList().get(1));
			if (estimates == null) {
				return null;
			}
			for (Estimate e : estimates) {
				e.addReason(reason);
			}
			return estimates;
		}

		return null;
	}

	private Agent estimator;
	private Agent estimated;
	private List<Role> roles = new ArrayList<>();
	private List<Content> reasons = new ArrayList<>();

	/**
	 * Constructs an Estimate.
	 * 
	 * @param estimator the estimating agent
	 * @param estimated the estimated agent
	 * @param roles     the estimated roles
	 */
	public Estimate(Agent estimator, Agent estimated, Role... roles) {
		this.estimator = estimator;
		this.estimated = estimated;
		for (Role r : roles) {
			addRole(r);
		}
	}

	/**
	 * Constructs an Estimate with a reason.
	 * 
	 * @param estimator the estimating agent
	 * @param estimated the estimated agent
	 * @param reason    the reason
	 * @param roles     the estimated roles
	 */
	public Estimate(Agent estimator, Agent estimated, Content reason, Role... roles) {
		this(estimator, estimated, roles);
		addReason(reason);
	}

	/**
	 * Adds an estimated role to the existings.
	 * 
	 * @param role the estimated role to be added
	 */
	public void addRole(Role role) {
		if (!roles.contains(role)) {
			roles.add(role);
		}
	}

	/**
	 * Sets unique estimated role.
	 * 
	 * @param role the estimated role
	 */
	public void resetRole(Role role) {
		roles.clear();
		roles.add(role);
	}

	/**
	 * Adds a reason to the existings.
	 * 
	 * @param reason the reason to be added
	 */
	public void addReason(Content reason) {
		if (!reasons.contains(reason)) {
			reasons.add(reason);
		}
	}

	/**
	 * Returns whether or not this estimate about the role.
	 * 
	 * @param role the role
	 * @return true if this estimate is about the role
	 */
	public boolean hasRole(Role role) {
		return roles.contains(role);
	}

	/**
	 * Returns whether or not this estimate has the reason specified.
	 * 
	 * @param reason the reason
	 * @return true if this estimate has the reason
	 */
	public boolean hasReason(Content reason) {
		return reasons.contains(reason);
	}

	/**
	 * Returns this estimate in the form of Content.
	 * 
	 * @return ESTIMATE if no reason, otherwise, BECAUSE
	 */
	public Content toContent() {
		Content estimate = getEstimateContent();
		if (estimate == null) {
			return null;
		}
		Content reason = getReasonContent();
		if (reason == null) {
			return estimate;
		}
		return new Content(new BecauseContentBuilder(estimator, reason, estimate));
	}

	/**
	 * Returns the agent which estimates.
	 * 
	 * @return the estimator
	 */
	public Agent getEstimator() {
		return estimator;
	}

	/**
	 * Returns the estimated agent.
	 * 
	 * @return the estimated agent
	 */
	public Agent getEstimated() {
		return estimated;
	}

	/**
	 * Returns the estimates about multiple roles in a form of XOR.
	 * 
	 * @return in a form of ESTIMATE in case of single role, otherwise, in a form of XOR omitting the third and following
	 *         ones
	 */
	public Content getEstimateContent() {
		Content[] estimates = roles.stream().map(r -> new Content(new EstimateContentBuilder(estimator, estimated, r))).toArray(size -> new Content[size]);
		if (estimates.length == 0) {
			return null;
		}
		if (estimates.length == 1) {
			return estimates[0];
		}
		return new Content(new XorContentBuilder(estimator, estimates[0], estimates[1])); // 3つ目以降は無視
	}

	/**
	 * Returns the reasons of this estimate in a form of AND.
	 * 
	 * @return in a form of ESTIMATE in case of single reason, otherwise, in a form of AND
	 */
	public Content getReasonContent() {
		if (reasons.isEmpty()) {
			return null;
		}
		if (reasons.size() == 1) {
			return reasons.get(0);
		}
		return new Content(new AndContentBuilder(estimator, reasons));
	}

	@Override
	public String toString() {
		return toContent().getText();
	}

}
