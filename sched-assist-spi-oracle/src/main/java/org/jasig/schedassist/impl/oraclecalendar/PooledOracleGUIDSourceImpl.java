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

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Handle;
import oracle.calendar.sdk.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.KeyedObjectPool;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;

import com.googlecode.ehcache.annotations.Cacheable;

/**
 * Commons {@link KeyedObjectPool} backed implementation of {@link OracleGUIDSource}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: PooledOracleGUIDSourceImpl.java $
 */
public class PooledOracleGUIDSourceImpl extends OracleCalendarSDKSupport
		implements OracleGUIDSource {

	private Log LOG = LogFactory.getLog(this.getClass());
	
	private KeyedObjectPool oracleSessionPool;
	private Map<String, OracleCalendarServerNode> serverNodes = new HashMap<String, OracleCalendarServerNode>();

	/**
	 * @param oracleSessionPool the oracleSessionPool to set
	 */
	public void setOracleSessionPool(KeyedObjectPool oracleSessionPool) {
		this.oracleSessionPool = oracleSessionPool;
	}
	/**
	 * @param serverNodes the serverNodes to set
	 */
	public void setServerNodes(Map<String, OracleCalendarServerNode> serverNodes) {
		this.serverNodes = serverNodes;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.OracleGUIDSource#getOracleGUID(org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Cacheable(cacheName="oracleGUIDCache", selfPopulating=true)
	@Override
	public String getOracleGUID(ICalendarAccount account) {
		OracleCalendarServerNode serverNode = getOracleCalendarServerNode(account);

		if(serverNode == null) {
			LOG.debug("no servernode available for account: " + account);
			return null;
		}
		
		Session session = null;
		boolean invalidateSession = false;
		
		try {
			session = getOracleSession(account, serverNode);
			String guid = getOracleGUID(account, session);
			return guid;

		} catch (StatusException e) {
			LOG.error("caught Api.StatusException returning null GUID for " + account);
			LOG.debug(e);
			invalidateSession = true;
			return null;
		} finally {
			doneWithSession(serverNode, session, invalidateSession);
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.OracleGUIDSource#getOracleGUID(org.jasig.schedassist.model.ICalendarAccount, oracle.calendar.sdk.Session)
	 */
	@Override
	public String getOracleGUID(final ICalendarAccount account,
			final Session session) throws Api.StatusException {
		if(session == null) {
			return null;
		}
		Handle handle = session.getHandle(Api.CSDK_FLAG_NONE, account.getCalendarLoginId());
		String guid = handle.getGUID();
		LOG.debug("user guid: " + guid);
		return guid;
	}
	/**
	 * 
	 * @param account
	 * @return the {@link OracleCalendarServerNode} for the account
	 */
	protected final OracleCalendarServerNode getOracleCalendarServerNode(ICalendarAccount account) {
		String nodeId = this.getOracleNodeId(account);
		return this.serverNodes.get(nodeId);
	}
	/**
	 * @param user
	 * @return the ID of oracle calendar server node from the user's calendarUniqueId (ctcalxitemid)
	 */
	protected String getOracleNodeId(final ICalendarAccount user) {
		if(user instanceof AbstractOracleCalendarAccount) {
			AbstractOracleCalendarAccount account = (AbstractOracleCalendarAccount) user;
			return account.getCalendarNodeId();
		} else {
			throw new IllegalArgumentException("argument is not an oracle account: " + user);
		}
	}
	/**
	 * 
	 * @param account
	 * @return
	 */
	protected Session getOracleSession(ICalendarAccount account, OracleCalendarServerNode serverNode) {
		try {
			Session session = (Session) oracleSessionPool.borrowObject(serverNode);
			
			session.setIdentity(Api.CSDK_FLAG_NONE, account.getCalendarLoginId());
			return session;
		} catch (Exception e) {
			LOG.error("unable to retrieve Session from pool for " + account, e);
			return null;
		}
	}
	
	/**
	 * 
	 * @param account
	 * @param session
	 */
	protected void doneWithSession(OracleCalendarServerNode serverNode, Session session, boolean invalidate) {
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
