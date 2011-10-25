/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.schedassist.impl.oraclecalendar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jasig.schedassist.model.AbstractCalendarAccount;

/**
 * Abstract subclass of {@link AbstractCalendarAccount} that adds 
 * fields common to all Oracle calendar accounts.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: AbstractOracleCalendarAccount.java 2819 2010-10-28 15:19:39Z npblair $
 */
public abstract class AbstractOracleCalendarAccount extends AbstractCalendarAccount {

	private static final long serialVersionUID = 53706L;

	public static final String ORACLE_GUID_ATTRIBUTE = AbstractOracleCalendarAccount.class.getPackage().getName() + ".AbstractOracleCalendarAccount.ORACLE_GUID";
	protected static final String CTCALXITEMID = "ctcalxitemid";
	protected static final String WISCEDUCALEMAIL = "wisceducalemail";
	private static final String DELIM = ":";
	private String ctcalxitemid = "";
	private String givenName;
	private String surname;
	private String oracleGuid;
	private Map<String, String> attributesMap = new HashMap<String, String>();
	
	/**
	 * 
	 */
	public AbstractOracleCalendarAccount() {
	}
	/**
	 * 
	 * @param attributesMap
	 */
	public AbstractOracleCalendarAccount(Map<String, String> attributesMap) {
		this.attributesMap = attributesMap;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getAttributeValue(java.lang.String)
	 */
	@Override
	public final String getAttributeValue(String attributeName) {
		return this.attributesMap.get(attributeName);
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getAttributes()
	 */
	@Override
	public final Map<String, String> getAttributes() {
		return this.attributesMap;
	}
	/**
	 * @param attributesMap the attributesMap to set
	 */
	public final void setAttributes(Map<String, String> attributesMap) {
		this.attributesMap = attributesMap;
	}
	/**
	 * @return the ctcalxitemid
	 */
	public final String getCtcalxitemid() {
		return ctcalxitemid;
	}
	/**
	 * @param ctcalxitemid the ctcalxitemid to set
	 */
	public final void setCtcalxitemid(String ctcalxitemid) {
		this.ctcalxitemid = ctcalxitemid;
	}
	/**
	 * @return the givenName
	 */
	public final String getGivenName() {
		return givenName;
	}
	/**
	 * @param givenName the givenName to set
	 */
	public final void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return this.getGivenSurname();
	}
	/**
	 * @return the surname
	 */
	public final String getSurname() {
		return surname;
	}
	/**
	 * @param surname the surname to set
	 */
	public final void setSurname(String surname) {
		this.surname = surname;
	}
	
	/**
	 * Oracle generates a display name by concatenating the givenName and sn attributes.
	 * 
	 * @return oracle givenName+sn construct
	 */
	public final String getGivenSurname() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.givenName);
		builder.append(" ");
		builder.append(this.surname);
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getCalendarUniqueId()
	 */
	@Override
	public final String getCalendarUniqueId() {
		return this.ctcalxitemid;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#setCalendarUniqueId(java.lang.String)
	 */
	@Override
	public final void setCalendarUniqueId(String calendarUniqueId) {
		this.setCtcalxitemid(calendarUniqueId);
	}
	/**
	 * @return the node Id.
	 */
	public final String getCalendarNodeId() {
		return getCalendarUniqueId().split(DELIM)[0];
	}
	/**
	 * @return the calendar Id
	 */
	public final String getCalendarId() {
		return getCalendarUniqueId().split(DELIM)[1];
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#isEligible()
	 */
	@Override
	public final boolean isEligible() {
		return StringUtils.isNotBlank(this.ctcalxitemid) && StringUtils.isNotBlank(getEmailAddress()) && !isBogusOracleEmailAddress();
	}
	
	/**
	 * Oracle Calendar will set a bogus email address for accounts that don't have a proper
	 * email attribute value set in LDAP.
	 * This bogus email address looks like: ORACLE_GUID@email.invalid.
	 * 
	 * @return true if this instance has a bogus Oracle Calendar email address
	 */
	public boolean isBogusOracleEmailAddress() {
		String email = getEmailAddress();
		if(StringUtils.isBlank(email)) {
			return false;
		}
		
		return email.endsWith("email.invalid");
	}
	/**
	 * Ignored, no-op.
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#setEligible(boolean)
	 */
	@Override
	public final void setEligible(boolean eligible) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getCalendarLoginId()
	 */
	public abstract String getCalendarLoginId();
	

	/**
	 * @return the oracleGuid
	 */
	public final String getOracleGuid() {
		return oracleGuid;
	}
	/**
	 * @param oracleGuid the oracleGuid to set
	 */
	public final void setOracleGuid(String oracleGuid) {
		this.oracleGuid = oracleGuid;
		this.attributesMap.put(ORACLE_GUID_ATTRIBUTE, oracleGuid);
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((attributesMap == null) ? 0 : attributesMap.hashCode());
		result = prime * result
				+ ((ctcalxitemid == null) ? 0 : ctcalxitemid.hashCode());
		result = prime * result
				+ ((givenName == null) ? 0 : givenName.hashCode());
		result = prime * result
				+ ((oracleGuid == null) ? 0 : oracleGuid.hashCode());
		result = prime * result + ((surname == null) ? 0 : surname.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AbstractOracleCalendarAccount)) {
			return false;
		}
		AbstractOracleCalendarAccount other = (AbstractOracleCalendarAccount) obj;
		if (attributesMap == null) {
			if (other.attributesMap != null) {
				return false;
			}
		} else if (!attributesMap.equals(other.attributesMap)) {
			return false;
		}
		if (ctcalxitemid == null) {
			if (other.ctcalxitemid != null) {
				return false;
			}
		} else if (!ctcalxitemid.equals(other.ctcalxitemid)) {
			return false;
		}
		if (givenName == null) {
			if (other.givenName != null) {
				return false;
			}
		} else if (!givenName.equals(other.givenName)) {
			return false;
		}
		if (oracleGuid == null) {
			if (other.oracleGuid != null) {
				return false;
			}
		} else if (!oracleGuid.equals(other.oracleGuid)) {
			return false;
		}
		if (surname == null) {
			if (other.surname != null) {
				return false;
			}
		} else if (!surname.equals(other.surname)) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractOracleCalendarAccount [attributesMap=");
		builder.append(attributesMap);
		builder.append(", ctcalxitemid=");
		builder.append(ctcalxitemid);
		builder.append(", givenName=");
		builder.append(givenName);
		builder.append(", oracleGuid=");
		builder.append(oracleGuid);
		builder.append(", surname=");
		builder.append(surname);
		builder.append("]");
		return builder.toString();
	}
	
}
