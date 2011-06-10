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

import org.apache.commons.pool.KeyedObjectPool;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;

/**
 * Subclass of {@link AbstractOracleCalendarDao} that depends on a 
 * Commons Pool {@link KeyedObjectPool} that manages Oracle {@link Session}s.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: PooledOracleCalendarDaoImpl.java $
 */
public class PooledOracleCalendarDaoImpl extends AbstractOracleCalendarDao {

	private KeyedObjectPool oracleSessionPool;
	
	/**
	 * @param oracleSessionPool the oracleSessionPool to set
	 */
	public void setOracleSessionPool(KeyedObjectPool oracleSessionPool) {
		this.oracleSessionPool = oracleSessionPool;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarDao#getSession(org.jasig.schedassist.model.ICalendarAccount, org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode)
	 */
	@Override
	protected Session getSession(ICalendarAccount calendarAccount,
			OracleCalendarServerNode serverNode) throws StatusException {
		try {
			Session session = (Session) oracleSessionPool.borrowObject(serverNode);
			
			session.setIdentity(Api.CSDK_FLAG_NONE, calendarAccount.getCalendarLoginId());
			return session;
		} catch (Exception e) {
			throw new OracleCalendarDataAccessException("unable to retrieve Session from pool", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.AbstractOracleCalendarDao#doneWithSession(oracle.calendar.sdk.Session, org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode, boolean)
	 */
	@Override
	protected void doneWithSession(Session session,
			OracleCalendarServerNode serverNode, boolean invalidate) {
		try {
			if(invalidate) {
				if(null != session) {
					LOG.info("telling pool to invalidate the following session " + session + " on node " + serverNode.getNodeName());
					oracleSessionPool.invalidateObject(serverNode, session);
				}
				else {
					LOG.debug("session already null, skipping call to invalidateObject");
				}
			}
			else {
				LOG.debug("returning session to pool");
				oracleSessionPool.returnObject(serverNode, session);
			}
		} catch (Exception e) {
			LOG.error("caught exception while returning session to pool", e);
		}
	}

}
