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

import javax.annotation.Resource;

import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.model.IDelegateCalendarAccount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test bench for {@link OracleLdapUserDaoImpl}.
 * 
 * Depends on the Spring configuration file named "ldap-test.xml" 
 * (see src/test/resources folder).
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleLdapCalendarResourceAccountDaoImplTest.java 2769 2010-10-05 19:12:19Z npblair $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:ldap-test.xml","classpath:oracle-calendar-beans.xml"})
public class OracleLdapCalendarResourceAccountDaoImplTest {

	private OracleLdapCalendarResourceAccountDaoImpl resourceAccountDao;
	private ICalendarAccount controlUser;
	private OracleCalendarResourceAccount controlResource;
	/**
	 * @param resourceAccountDao the resourceAccountDao to set
	 */
	@Resource
	public void setResourceAccountDao(
			OracleLdapCalendarResourceAccountDaoImpl resourceAccountDao) {
		this.resourceAccountDao = resourceAccountDao;
	}
	/**
	 * @param controlUser the controlUser to set
	 */
	@Resource
	public void setControlUser(final ICalendarAccount controlUser) {
		this.controlUser = controlUser;
	}
	/**
	 * @param controlResource the controlResource to set
	 */
	@Resource
	public void setControlResource(@Qualifier("control") OracleCalendarResourceAccount controlResource) {
		this.controlResource = controlResource;
	}
	/**
	 * Lookup the controlResource from the spring configuration, assert expected values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testControlResource() throws Exception {
		IDelegateCalendarAccount retrieved = this.resourceAccountDao.getDelegate(controlResource.getDisplayName(), controlUser);
		Assert.assertNotNull(retrieved);
		
		Assert.assertEquals(controlResource.getUsername(), retrieved.getUsername());
		Assert.assertEquals(controlResource.getDisplayName(), retrieved.getDisplayName());
		Assert.assertEquals(controlResource.getCalendarUniqueId(), retrieved.getCalendarUniqueId());
		Assert.assertEquals(controlResource.getEmailAddress(), retrieved.getEmailAddress());
		Assert.assertEquals(controlResource.getLocation(), retrieved.getLocation());
		Assert.assertEquals(controlResource.getContactInformation(), retrieved.getContactInformation());
		Assert.assertTrue(retrieved instanceof OracleCalendarResourceAccount);
		OracleCalendarResourceAccount casted = (OracleCalendarResourceAccount) retrieved;
		Assert.assertEquals(controlResource.getOracleGuid(), casted.getOracleGuid());

		
		retrieved = this.resourceAccountDao.getDelegateByUniqueId(controlResource.getCalendarUniqueId(), controlUser);
		Assert.assertEquals(controlResource.getUsername(), retrieved.getUsername());
		Assert.assertEquals(controlResource.getDisplayName(), retrieved.getDisplayName());
		Assert.assertEquals(controlResource.getCalendarUniqueId(), retrieved.getCalendarUniqueId());
		Assert.assertEquals(controlResource.getEmailAddress(), retrieved.getEmailAddress());
		Assert.assertEquals(controlResource.getLocation(), retrieved.getLocation());
		Assert.assertEquals(controlResource.getContactInformation(), retrieved.getContactInformation());
		
		Assert.assertTrue(retrieved instanceof OracleCalendarResourceAccount);
		casted = (OracleCalendarResourceAccount) retrieved;
		Assert.assertEquals(controlResource.getOracleGuid(), casted.getOracleGuid());
	}
	
	/**
	 * Assert getDelegate on nonexistent user returns null.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNonExistentResource () throws Exception {
		IDelegateCalendarAccount retrieved = this.resourceAccountDao.getDelegate("nonexistent", controlUser);
		Assert.assertNull(retrieved);
	}
	
}
