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

import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Session;

import org.jasig.schedassist.model.ICalendarAccount;

/**
 * Defines operations for retrieving the Oracle GUID for {@link ICalendarAccount}s.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleGUIDSource.java 1992 2010-04-23 17:25:45Z npblair $
 */
public interface OracleGUIDSource {

	/**
	 * Connect to the Oracle Calendar server and find the GUID for the {@link ICalendarAccount}.
	 * @param account
	 * @return the Oracle GUID for the account if set, null if not available
	 */
	String getOracleGUID(ICalendarAccount account);
	
	/**
	 * Useful overloaded version to re-use an existing Oracle {@link Session} to retrieve
	 * the Oracle GUID for the {@link ICalendarAccount}. 
	 * 
	 * @param account
	 * @param session an existing Oracle {@link Session} object to re-use
	 * @return the Oracle GUID for the account if set, null if not available
	 * @throws StatusException 
	 */
	String getOracleGUID(ICalendarAccount account, Session session) throws StatusException;
}
