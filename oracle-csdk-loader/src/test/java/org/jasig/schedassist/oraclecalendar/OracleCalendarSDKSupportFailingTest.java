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

import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKUnavailableError;
import org.junit.Test;


/**
 * Test for {@link OracleCalendarSDKSupport} with a bogus path for the
 * org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkConfigFile system property.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarSDKSupportFailingTest.java 1821 2010-03-03 19:14:16Z npblair $
 */
public class OracleCalendarSDKSupportFailingTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConfigFileDoesntExist() throws Exception {
		System.setProperty("org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkConfigFile", "bogus.ini");
		try {
			new OracleCalendarSDKSupport() {};
		} catch (OracleCalendarSDKUnavailableError e) {	
			// success
		} 
	}
}
