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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleGUIDSourceImplTest.java 2001 2010-04-23 17:33:18Z npblair $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:oracle-calendar-beans.xml"})
public class OracleGUIDSourceImplTest {

	private OracleGUIDSourceImpl oracleGUIDSource;

	/**
	 * @param oracleGUIDSource the oracleGUIDSource to set
	 */
	@Autowired
	public void setOracleGUIDSource(OracleGUIDSourceImpl oracleGUIDSource) {
		this.oracleGUIDSource = oracleGUIDSource;
	}
	
	@Test
	public void testNonexistent() throws Exception {
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("fake");
		user.setCtcalxitemid("20000:99999");
		String guid = this.oracleGUIDSource.getOracleGUID(user);
		Assert.assertNull(guid);
	}
	
	@Test
	public void testKnownUser() throws Exception {
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		String guid = this.oracleGUIDSource.getOracleGUID(user);
		Assert.assertNotNull(guid);
		Assert.assertEquals("200000118219869582153896", guid);
	}
	
	@Test
	public void testKnownResource() throws Exception {
		OracleCalendarResourceAccount resource = new OracleCalendarResourceAccount();
		resource.setCtcalxitemid("20000:01516");
		resource.setResourceName("DOIT blair test");
		String guid = this.oracleGUIDSource.getOracleGUID(resource);
		Assert.assertNotNull(guid);
		Assert.assertEquals("200000151619869596736320", guid);
	}
	
	
}
