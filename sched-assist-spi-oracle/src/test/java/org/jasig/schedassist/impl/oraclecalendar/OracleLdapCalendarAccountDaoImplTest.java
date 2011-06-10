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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test bench for {@link OracleLdapUserDaoImpl}.
 * 
 * Depends on the Spring configuration file named "ldap-test.xml" 
 * (see src/test/resources folder).
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleLdapCalendarAccountDaoImplTest.java 2016 2010-04-26 16:27:24Z npblair $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:ldap-test.xml","classpath:oracle-calendar-beans.xml"})
public class OracleLdapCalendarAccountDaoImplTest {

	private OracleLdapCalendarAccountDaoImpl calendarAccountDao;
	private ICalendarAccount controlUser;
	private ICalendarAccount controlRoleBasedAccount;
	
	/**
	 * @param calendarAccountDao the calendarUserDao to set
	 */
	@Resource
	public void setCalendarAccountDao(final OracleLdapCalendarAccountDaoImpl calendarAccountDao) {
		this.calendarAccountDao = calendarAccountDao;
	}
	
	/**
	 * @param controlUser the controlUser to set
	 */
	@Resource
	public void setControlUser(final ICalendarAccount controlUser) {
		this.controlUser = controlUser;
	}
	
	/**
	 * @param controlRoleBasedAccount the controlRoleBasedAccount to set
	 */
	@Resource
	public void setControlRoleBasedAccount(final ICalendarAccount controlRoleBasedAccount) {
		this.controlRoleBasedAccount = controlRoleBasedAccount;
	}

	/**
	 * Lookup the controlUser from the spring configuration, assert expected values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testControlUser() throws Exception {
		ICalendarAccount retrieved = this.calendarAccountDao.getCalendarAccount(controlUser.getUsername());
		Assert.assertNotNull(retrieved);
		Assert.assertEquals(controlUser.getUsername(), retrieved.getUsername());
		Assert.assertEquals(controlUser.getDisplayName(), retrieved.getDisplayName());
		Assert.assertEquals(controlUser.getCalendarUniqueId(), retrieved.getCalendarUniqueId());
		Assert.assertEquals(controlUser.getEmailAddress(), retrieved.getEmailAddress());
		
		retrieved = this.calendarAccountDao.getCalendarAccountFromUniqueId(controlUser.getCalendarUniqueId());
		Assert.assertEquals(controlUser.getUsername(), retrieved.getUsername());
		Assert.assertEquals(controlUser.getDisplayName(), retrieved.getDisplayName());
		Assert.assertEquals(controlUser.getCalendarUniqueId(), retrieved.getCalendarUniqueId());
		Assert.assertEquals(controlUser.getEmailAddress(), retrieved.getEmailAddress());
	}
	
	/**
	 * Assert getCalendarAccount on nonexistent user returns null.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNonExistentUser () throws Exception {
		Assert.assertNull(this.calendarAccountDao.getCalendarAccount("nonexistent"));
	}
	
	/**
	 * Lookup the controlRoleBasedAccount from the spring configuration, assert expected values.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRoleBasedAccount () throws Exception {
		ICalendarAccount roleAccount = this.calendarAccountDao.getCalendarAccount(controlRoleBasedAccount.getUsername());
		Assert.assertNotNull(roleAccount);
		Assert.assertEquals(controlRoleBasedAccount.getUsername(), roleAccount.getUsername());
		Assert.assertEquals(controlRoleBasedAccount.getCalendarUniqueId(), roleAccount.getCalendarUniqueId());
		Assert.assertEquals(controlRoleBasedAccount.getEmailAddress(), roleAccount.getEmailAddress());
		Assert.assertEquals(controlRoleBasedAccount.getDisplayName(), roleAccount.getDisplayName());
		
		roleAccount = this.calendarAccountDao.getCalendarAccountFromUniqueId(controlRoleBasedAccount.getCalendarUniqueId());
		Assert.assertNotNull(roleAccount);
		Assert.assertEquals(controlRoleBasedAccount.getUsername(), roleAccount.getUsername());
		Assert.assertEquals(controlRoleBasedAccount.getCalendarUniqueId(), roleAccount.getCalendarUniqueId());
		Assert.assertEquals(controlRoleBasedAccount.getEmailAddress(), roleAccount.getEmailAddress());
		Assert.assertEquals(controlRoleBasedAccount.getDisplayName(), roleAccount.getDisplayName());
	}
}
