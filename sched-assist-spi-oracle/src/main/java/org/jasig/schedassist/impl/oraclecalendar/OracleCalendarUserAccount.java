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

import java.util.Map;

import org.jasig.schedassist.model.ICalendarAccount;

/**
 * {@link AbstractOracleCalendarAccount} that represents a personal
 * or "role based account."
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarUserAccount.java 1909 2010-04-14 21:14:07Z npblair $
 */
public class OracleCalendarUserAccount extends AbstractOracleCalendarAccount {

	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;
	
	/**
	 * 
	 */
	public OracleCalendarUserAccount() {
	}
	/**
	 * 
	 * @param attributesMap
	 */
	public OracleCalendarUserAccount(Map<String, String> attributesMap) {
		super(attributesMap);
	}
	/**
	 * Clone style constructor.
	 * @param account
	 */
	public OracleCalendarUserAccount(ICalendarAccount account) {
		super(account.getAttributes());
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarAccount#getCalendarLoginId()
	 */
	@Override
	public String getCalendarLoginId() {
		return this.getUsername();
	}

}
