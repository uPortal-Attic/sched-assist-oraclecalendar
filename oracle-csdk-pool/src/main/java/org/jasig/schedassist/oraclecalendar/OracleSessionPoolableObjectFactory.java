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

package org.jasig.schedassist.oraclecalendar;

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Session;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subclass of {@link BaseKeyedPoolableObjectFactory} for pooling
 * Oracle {@link Session}s.
 * 
 * The keys used in this class are {@link OracleCalendarServerNode}s.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleSessionPoolableObjectFactory.java 2879 2010-11-10 18:42:07Z npblair $
 */
public class OracleSessionPoolableObjectFactory extends
		BaseKeyedPoolableObjectFactory {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * When passed as the 2nd argument to {@link Session#getCapabilities(int, int)}, this flag
	 * queries the server for it's version number (and hence requires a server connection).
	 */
	protected static final int CAPI_CAPAB_SERVER_VERSION = 0x03;
	
	/**
	 * The key argument is cast to {@link OracleCalendarServerNode}.
	 * 
	 * Create a new {@link Session} and call {@link Session#connectAsSysop(int, String, String, String)}
	 * for the provided {@link OracleCalendarServerNode}.
	 * 
	 *  (non-Javadoc)
	 * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#makeObject(java.lang.Object)
	 */
	@Override
	public Object makeObject(Object key) throws Exception {
		if(LOG.isDebugEnabled()) {
			LOG.debug("makeObject called with " + key);
		}
		OracleCalendarServerNode serverNode = (OracleCalendarServerNode) key;
		
		// new session
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Session session = new Session();
		stopWatch.split();
		if(LOG.isInfoEnabled()) {
			LOG.info("CSDK Session constructor elapsed time (msec): "  + stopWatch.getSplitTime());
		}
		
		// connectAsSysop
		session.connectAsSysop(Api.CSDK_FLAG_NONE, 
				serverNode.getServerAddress(), 
				serverNode.getNodeName(), 
				serverNode.getSysopPassword());
		stopWatch.stop();
		if(LOG.isInfoEnabled()) {
			LOG.info("CSDK Session connectAsSysop elapsed time (msec): " + stopWatch.getSplitTime()); 
		}
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("makeObject success for key: " + key + ", new session: " + session);
		}
		return session;
	}

	/**
	 * Cast the 2nd argument as a {@link Session}.
	 * If non-null, call {@link Session#disconnect(int)}.
	 * 
	 *  (non-Javadoc)
	 * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#destroyObject(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void destroyObject(Object key, Object obj) throws Exception {
		if(LOG.isDebugEnabled()) {
			LOG.debug("destroyObject called with " + key + " and " + obj);
		}
		
		Session session = (Session) obj;
		
		// disconnect
		if(null != session) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			session.disconnect(Api.CSDK_FLAG_NONE);
			stopWatch.stop();
			
			if(LOG.isInfoEnabled()) {
				LOG.info("CSDK Session disconnect elapsed time (msec): " + stopWatch.getTime());
			}
			if(LOG.isDebugEnabled()) {
				LOG.debug("finished disconnect for key " + key + ", session " + session);
			}
		}
		else {
			if(LOG.isDebugEnabled()) {
				LOG.debug("Session object was null for key: " + key + ", skipping disconnect call");
			}
		}
	}

	/**
	 * Casts the 2nd argument as a {@link Session}.
	 * 
	 * If null, returns false.
	 * If not null, returns result of {@link Session#isValid}.
	 * 
	 *  (non-Javadoc)
	 * @see org.apache.commons.pool.BaseKeyedPoolableObjectFactory#validateObject(java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean validateObject(Object key, Object obj) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("validate called with " + key + " and " + obj);
		}
		if(null != obj) {
			Session session = (Session) obj;
			try {
				String serverVersion = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_SERVER_VERSION);
				String auth = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_AUTH);
				String unsupportedProperties = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_UNSUPPORTED_ICAL_PROP);
				boolean isValidValue = session.isValid();
				LOG.debug("capabilities for key " + key + "; version=" + serverVersion + ", auth=" + auth + ", unsupportedProperties=" + unsupportedProperties + ", isValid result=" + isValidValue);
				return true;
			} catch (StatusException e) {
				LOG.debug("caught StatusException testing session for key " + key, e);
				return false;
			}
		} else {
			LOG.warn("session was null for validateObject call on key " + key);
			return false;
		}
	}

}
