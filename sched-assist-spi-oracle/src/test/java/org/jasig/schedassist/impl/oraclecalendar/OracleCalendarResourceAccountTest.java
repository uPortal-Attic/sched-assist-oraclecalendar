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

import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests for {@link OracleCalendarResourceAccount}.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarResourceAccountTest.java 2557 2010-09-13 19:58:36Z npblair $
 */
public class OracleCalendarResourceAccountTest {

	/**
	 * Validate getCalendarLoginId function.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetCalendarResourceLoginId() throws Exception {
		OracleCalendarResourceAccount resourceAccount = new OracleCalendarResourceAccount();
		resourceAccount.setCtcalxitemid("20000:01516");
		resourceAccount.setResourceName("DOIT blair test");
		
		Assert.assertEquals("?/RS=DOIT blair test/ND=20000/", resourceAccount.getCalendarLoginId());
	}
	
	/**
	 * Validate {@link OracleCalendarResourceAccount#getEmailAddress()} behavior.
	 */
	@Test
	public void testGetEmailAddress() {
		OracleCalendarResourceAccount resourceAccount = new OracleCalendarResourceAccount();
		resourceAccount.setCtcalxitemid("20000:01516");
		resourceAccount.setResourceName("DOIT blair test");
		
		Assert.assertNull(resourceAccount.getEmailAddress());
		
		// set GUID, and getEmailAddress will change
		resourceAccount.setOracleGuid("0123456789ABCDE");
		
		Assert.assertEquals("0123456789ABCDE@email.invalid", resourceAccount.getEmailAddress());
		
		// override email address, and getEmailAddress will change
		resourceAccount.setEmailAddress("something@else.com");
		
		Assert.assertEquals("something@else.com", resourceAccount.getEmailAddress());
	}
}
