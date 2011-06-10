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
import oracle.calendar.sdk.Session;

import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link OracleCalendarSDKSupport} with the
 * "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.useOracleApiInit" parameter
 * set to false.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarSDKSupportNoOracleApiInitTest.java 2246 2010-06-30 18:57:24Z npblair $
 */
public class OracleCalendarSDKSupportNoOracleApiInitTest {

	@Test
	public void testInitDirectSystemLoadLibrary() throws Exception {
		System.setProperty("org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.useOracleApiInit", "false");
		
		new OracleCalendarSDKSupport() {};
		
		Session session = new Session();
		String version = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_CSDK_VERSION);
		String capabVersion = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_VERSION);
		Assert.assertNotNull(version);
		Assert.assertNotNull(capabVersion);
		
		System.out.println("CSDK version: " + version);
		System.out.println("CSDK capab version:" + capabVersion);
	}
}
