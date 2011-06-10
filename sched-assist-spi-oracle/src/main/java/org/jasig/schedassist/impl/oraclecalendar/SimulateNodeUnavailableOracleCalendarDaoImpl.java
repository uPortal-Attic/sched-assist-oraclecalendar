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

import java.util.ArrayList;
import java.util.List;

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Session;

import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;

/**
 * Extension of {@link PooledOracleCalendarDaoImpl} intended for testing the scenario
 * where one (or more) of the Oracle Calendar Server nodes is offline for maintenance.
 * 
 * The list of nodes should be provided at construction via {@link #setUnavailableNodes(List)}, each
 * String in the list being the node id (e.g. "10000", "11000", etc)  that should simulate being unavailable.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: SimulateNodeUnavailableOracleCalendarDaoImpl.java $
 */
public class SimulateNodeUnavailableOracleCalendarDaoImpl extends
		PooledOracleCalendarDaoImpl {

	private List<String> unavailableNodes = new ArrayList<String>();
	
	/**
	 * @param unavailableNodes the unavailableNodes to set
	 */
	public void setUnavailableNodes(List<String> unavailableNodes) {
		this.unavailableNodes = unavailableNodes;
	}

	/**
	 * Throws a {@link Api.StatusException} with {@link Api#CSDK_STAT_LIBRARY_SERVER_UNAVAILABLE}
	 * for nodes in the unavailable list.
	 * 
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.impl.oraclecalendar.PooledOracleCalendarDaoImpl#getSession(org.jasig.schedassist.model.ICalendarAccount, org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode)
	 */
	@Override
	protected Session getSession(ICalendarAccount calendarAccount,
			OracleCalendarServerNode serverNode) throws StatusException {
		if(unavailableNodes.contains(serverNode.getNodeName())) {
			@SuppressWarnings("deprecation")
			StatusException e = new StatusException(Api.CAPI_STAT_LIBRARY_SERVER_UNAVAILABLE, "CAPI_STAT_LIBRARY_SERVER_UNAVAILABLE");
			throw e;
		} else {
			return super.getSession(calendarAccount, serverNode);
		}
	}

}
