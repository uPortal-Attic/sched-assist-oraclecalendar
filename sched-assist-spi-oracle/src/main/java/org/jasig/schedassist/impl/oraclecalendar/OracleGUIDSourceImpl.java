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
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;

import com.googlecode.ehcache.annotations.Cacheable;

/**
 * {@link OracleGUIDSource} implementation that uses the Oracle CSDK.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleGUIDSourceImpl.java 2451 2010-09-01 20:03:18Z npblair $
 */
public final class OracleGUIDSourceImpl extends OracleCalendarSDKSupport implements
OracleGUIDSource {

	private Map<String, OracleCalendarServerNode> serverNodes = new HashMap<String, OracleCalendarServerNode>();

	private Log LOG = LogFactory.getLog(this.getClass());
	/**
	 * 
	 */
	public OracleGUIDSourceImpl() {
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
	public String getOracleGUID(final ICalendarAccount account) {
		OracleCalendarServerNode serverNode = serverNodes.get(getOracleNode(account));
		if(serverNode == null) {
			LOG.debug("no servernode available for account: " + account);
			return null;
		}
		
		Session session = null;
		try {
			session = new Session();
			// connectAsSysop
			session.connectAsSysop(Api.CSDK_FLAG_NONE, 
					serverNode.getServerAddress(), 
					serverNode.getNodeName(), 
					serverNode.getSysopPassword());

			// switch identity to owner
			session.setIdentity(Api.CSDK_FLAG_NONE, account.getCalendarLoginId());

			String guid = getOracleGUID(account, session);
			return guid;

		} catch (StatusException e) {
			LOG.error("caught Api.StatusException returning null GUID for " + account);
			LOG.debug(e);
			return null;
		} finally {
			disconnectQuietly(session);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.OracleGUIDSource#getOracleGUID(org.jasig.schedassist.model.ICalendarAccount, oracle.calendar.sdk.Session)
	 */
	@Override
	public String getOracleGUID(final ICalendarAccount account,
			final Session session) throws Api.StatusException {
		Handle handle = session.getHandle(Api.CSDK_FLAG_NONE, account.getCalendarLoginId());
		String guid = handle.getGUID();
		LOG.debug("user guid: " + guid);
		return guid;
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	protected String getOracleNode(final ICalendarAccount user) {
		if(user instanceof AbstractOracleCalendarAccount) {
			AbstractOracleCalendarAccount account = (AbstractOracleCalendarAccount) user;
			String nodeId = account.getCalendarNodeId();
			return nodeId;
		} else {
			throw new IllegalArgumentException("argument is not an oracle account: " + user);
		}
	}
	
	/**
	 * Call {@link Session#disconnect(int)} if the argument is not null, catching and
	 * ignoring (debug log) an {@link Api.StatusException}s that occur.
	 * 
	 * @param session
	 */
	private void disconnectQuietly(final Session session) {
		if(null != session) {
			try {
				session.disconnect(Api.CSDK_FLAG_NONE);
			} catch (Api.StatusException e) {
				LOG.debug("ignoring Api.StatusException on disconnect", e);
			}
		} else {
			LOG.debug("ignoring disconnectQuietly call for null session");
		}
	}
}
