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

import org.apache.commons.lang.StringUtils;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.model.IDelegateCalendarAccount;

/**
 * {@link IDelegateCalendarAccount} implementation that represents
 * and Oracle Resource account.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarResourceAccount.java 2557 2010-09-13 19:58:36Z npblair $
 */
public class OracleCalendarResourceAccount extends
		AbstractOracleCalendarAccount implements IDelegateCalendarAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;

	public static final String ORACLE_INVALID_EMAIL_DOMAIN = "@email.invalid";
	
	private String resourceName;
	private String location;
	private ICalendarAccount accountOwner;
	private String contactInformation;
	private String accountOwnerUsername;
	
	/**
	 * 
	 */
	public OracleCalendarResourceAccount() {
	}
	/**
	 * 
	 * @param accountOwner
	 */
	public OracleCalendarResourceAccount(ICalendarAccount accountOwner) {
		this.accountOwner = accountOwner;
	}
	/**
	 * 
	 * @param accountOwner
	 * @param resourceName
	 */
	public OracleCalendarResourceAccount(ICalendarAccount accountOwner, String resourceName) {
		this(accountOwner);
		this.resourceName = resourceName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.IDelegateCalendarAccount#getAccountOwnerUsername()
	 */
	@Override
	public String getAccountOwnerUsername() {
		return accountOwnerUsername;
	}
	/**
	 * @param accountOwnerUsername the accountOwnerUsername to set
	 */
	public void setAccountOwnerUsername(String accountOwnerUsername) {
		this.accountOwnerUsername = accountOwnerUsername;
	}
	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}
	/**
	 * @param resourceName the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.IDelegateCalendarAccount#getLocation()
	 */
	@Override
	public String getLocation() {
		return this.location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	/**
	 * @return the contactInformation
	 */
	@Override
	public String getContactInformation() {
		return contactInformation;
	}
	/**
	 * @param contactInformation the contactInformation to set
	 */
	public void setContactInformation(String contactInformation) {
		this.contactInformation = contactInformation;
	}
	/**
	 * @param accountOwner the owner to set
	 */
	public void setAccountOwner(ICalendarAccount accountOwner) {
		this.accountOwner = accountOwner;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.IDelegateCalendarAccount#getAccountOwner()
	 */
	@Override
	public ICalendarAccount getAccountOwner() {
		return this.accountOwner;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarAccount#getCalendarLoginId()
	 */
	@Override
	public String getCalendarLoginId() {
		StringBuilder login = new StringBuilder();
		login.append("?/RS=");
		login.append(this.getResourceName());
		login.append("/ND=");
		login.append(this.getCalendarNodeId());
		login.append("/");
		return login.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarAccount#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return this.getResourceName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getUsername()
	 */
	@Override
	public String getUsername() {
		return this.getResourceName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.AbstractCalendarAccount#getEmailAddress()
	 */
	@Override
	public String getEmailAddress() {
		String email = super.getEmailAddress();
		if(StringUtils.isBlank(email)) {
			String oracleGuid = getOracleGuid();
			if(StringUtils.isNotBlank(oracleGuid)) {
				return oracleGuid + ORACLE_INVALID_EMAIL_DOMAIN;
			}
		}
		
		return email;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((accountOwner == null) ? 0 : accountOwner.hashCode());
		result = prime
				* result
				+ ((accountOwnerUsername == null) ? 0 : accountOwnerUsername
						.hashCode());
		result = prime
				* result
				+ ((contactInformation == null) ? 0 : contactInformation
						.hashCode());
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		result = prime * result
				+ ((resourceName == null) ? 0 : resourceName.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OracleCalendarResourceAccount other = (OracleCalendarResourceAccount) obj;
		if (accountOwner == null) {
			if (other.accountOwner != null)
				return false;
		} else if (!accountOwner.equals(other.accountOwner))
			return false;
		if (accountOwnerUsername == null) {
			if (other.accountOwnerUsername != null)
				return false;
		} else if (!accountOwnerUsername.equals(other.accountOwnerUsername))
			return false;
		if (contactInformation == null) {
			if (other.contactInformation != null)
				return false;
		} else if (!contactInformation.equals(other.contactInformation))
			return false;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		if (resourceName == null) {
			if (other.resourceName != null)
				return false;
		} else if (!resourceName.equals(other.resourceName))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OracleCalendarResourceAccount [accountOwner=");
		builder.append(accountOwner);
		builder.append(", accountOwnerUsername=");
		builder.append(accountOwnerUsername);
		builder.append(", contactInformation=");
		builder.append(contactInformation);
		builder.append(", location=");
		builder.append(location);
		builder.append(", resourceName=");
		builder.append(resourceName);
		builder.append("]");
		return builder.toString();
	}
	
	
	
}
