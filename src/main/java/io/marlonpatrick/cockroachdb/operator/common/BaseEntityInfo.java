package io.marlonpatrick.cockroachdb.operator.common;

import io.radanalytics.operator.common.EntityInfo;

public class BaseEntityInfo extends EntityInfo {

	protected String uid;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
