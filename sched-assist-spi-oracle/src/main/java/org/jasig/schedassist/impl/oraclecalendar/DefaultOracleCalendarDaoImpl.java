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

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Session;

import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;

/**
 * Default {@link AbstractOracleCalendarDao}. 
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: DefaultOracleCalendarDaoImpl.java $
 */
public class DefaultOracleCalendarDaoImpl extends AbstractOracleCalendarDao {

	/**
	 * Creates a new {@link Session} each time this method is called.
	 * 
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarDao#getSession(org.jasig.schedassist.model.ICalendarAccount, org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode)
	 */
	@Override
	protected Session getSession(ICalendarAccount calendarAccount, OracleCalendarServerNode serverNode) throws StatusException {
		Session session = new Session();
		// connectAsSysop
		session.connectAsSysop(Api.CSDK_FLAG_NONE, 
				serverNode.getServerAddress(), 
				serverNode.getNodeName(), 
				serverNode.getSysopPassword());

		// switch identity to owner
		session.setIdentity(Api.CSDK_FLAG_NONE, calendarAccount.getCalendarLoginId());
		return session;
	}

	/**
	 * Call {@link Session#disconnect(int)} if the argument is not null, catching and
	 * ignoring (debug log) an {@link Api.StatusException}s that occur.
	 * 
	 * Ignores the "invalidate" argument - always disconnects.
	 * 
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarDao#doneWithSession(oracle.calendar.sdk.Session, org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode, boolean)
	 */
	@Override
	protected void doneWithSession(Session session,
			OracleCalendarServerNode serverNode, boolean invalidate) {
		disconnectSessionQuietly(session);
	}

}
