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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import oracle.calendar.sdk.Api.StatusException;
import oracle.calendar.sdk.Session;

import org.apache.commons.lang.time.DateUtils;
import org.jasig.schedassist.NullAffiliationSourceImpl;
import org.jasig.schedassist.model.AppointmentRole;
import org.jasig.schedassist.model.AvailableBlock;
import org.jasig.schedassist.model.AvailableBlockBuilder;
import org.jasig.schedassist.model.AvailableSchedule;
import org.jasig.schedassist.model.CommonDateOperations;
import org.jasig.schedassist.model.InputFormatException;
import org.jasig.schedassist.model.Preferences;
import org.jasig.schedassist.model.SchedulingAssistantAppointment;
import org.jasig.schedassist.model.VisitorLimit;
import org.jasig.schedassist.model.mock.MockScheduleOwner;
import org.jasig.schedassist.model.mock.MockScheduleVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test harness for {@link SimpleOracleCalendarDaoImpl}.
 * 
 * Depends on presence of running Oracle Calendar instance, configuration
 * must be provided in Spring configuration file named "oracle-calendar-beans.xml"
 * at root of the classpath.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: DefaultOracleCalendarDaoImplTest.java 2923 2010-12-08 17:39:50Z npblair $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:oracle-calendar-beans.xml"})
public class DefaultOracleCalendarDaoImplTest {

	private DefaultOracleCalendarDaoImpl oracleCalendarDao;
	private OracleEventUtilsImpl oracleEventUtils = new OracleEventUtilsImpl(new NullAffiliationSourceImpl());
	/**
	 * @param oracleCalendarDao the oracleCalendarDao to set
	 */
	@Autowired
	public void setOracleCalendarDao(
			DefaultOracleCalendarDaoImpl oracleCaendarDao) {
		this.oracleCalendarDao = oracleCaendarDao;
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetCalendar() throws Exception {
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setEmailAddress("nblair@doit.wisc.edu");
		Date now = new Date();
		Date twoWeeks = DateUtils.addDays(now, 14);
		Calendar calendar = oracleCalendarDao.getCalendar(user, now, twoWeeks);
		Assert.assertNotNull(calendar);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndCancel() throws Exception {
		// construct owner from traditional user account
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		
		MockScheduleOwner owner = new MockScheduleOwner(user, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		owner.setPreference(Preferences.LOCATION, "meeting room");
		
		// construct visitor from traditional user account
		OracleCalendarUserAccount visitorUser = new OracleCalendarUserAccount();
		visitorUser.setUsername("jstalnak");
		visitorUser.setCtcalxitemid("20000:01220");
		visitorUser.setDisplayName("JAMES G STALNAKER");
		visitorUser.setEmailAddress("jstalnak@doit.wisc.edu");
		visitorUser.setGivenName("JAMES");
		visitorUser.setSurname("STALNAKER");
		MockScheduleVisitor visitor = new MockScheduleVisitor(visitorUser);
		
		Date startDate = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
		Date endDate = DateUtils.addHours(startDate, 1);
		final DateTime ical4jstart = new DateTime(startDate);
		final DateTime ical4jend = new DateTime(endDate);
		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 1);
		VEvent event = oracleCalendarDao.createAppointment(visitor, owner, block, "testCreateEvent");
		Assert.assertNotNull(event);
		
		VEvent lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(1, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, lookupResult.getProperties(Attendee.ATTENDEE).size());
		Property visitorAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(lookupResult, visitor.getCalendarAccount());
		Assert.assertNotNull(visitorAttendee);
		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Property ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(lookupResult, owner.getCalendarAccount());
		Assert.assertNotNull(ownerAttendee);
		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Assert.assertEquals("testCreateEvent", lookupResult.getDescription().getValue());
		
		
		oracleCalendarDao.cancelAppointment(owner, event);
		VEvent lookupResultAfterCancel = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNull(lookupResultAfterCancel);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndCancelWithDelegate() throws Exception {
		// construct traditional user account
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		
		OracleCalendarResourceAccount resource = new OracleCalendarResourceAccount(user);
		resource.setCtcalxitemid("20000:01516");
		resource.setResourceName("DOIT blair test");
		resource.setContactInformation("NICHOLAS BLAIR, (608)262-2153");
		resource.setOracleGuid("200000151619869596736320");
		
		MockScheduleOwner owner = new MockScheduleOwner(resource, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		owner.setPreference(Preferences.LOCATION, "{3233 Computer Science}");
		
		// construct visitor from traditional user account
		OracleCalendarUserAccount visitorUser = new OracleCalendarUserAccount();
		visitorUser.setUsername("jstalnak");
		visitorUser.setCtcalxitemid("20000:01220");
		visitorUser.setDisplayName("JAMES G STALNAKER");
		visitorUser.setEmailAddress("jstalnak@doit.wisc.edu");
		visitorUser.setGivenName("JAMES");
		visitorUser.setSurname("STALNAKER");
		MockScheduleVisitor visitor = new MockScheduleVisitor(visitorUser);
		
		Date startDate = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
		Date endDate = DateUtils.addHours(startDate, 1);
		final DateTime ical4jstart = new DateTime(startDate);
		final DateTime ical4jend = new DateTime(endDate);
		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 1);
		VEvent event = oracleCalendarDao.createAppointment(visitor, owner, block, "testCreateEvent with resource account as owner");
		Assert.assertNotNull(event);
		
		// TODO replace with check for ORGANIZER
		//Attendee ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, owner.getCalendarAccount());
		//Assert.assertNotNull(ownerAttendee);
		//Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		
		
		VEvent lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(1, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		//Assert.assertEquals(2, lookupResult.getProperties(Attendee.ATTENDEE).size());
		Property visitorAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(lookupResult, visitor.getCalendarAccount());
		Assert.assertNotNull(visitorAttendee);
		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Assert.assertEquals("testCreateEvent with resource account as owner", lookupResult.getDescription().getValue());
		
		
		oracleCalendarDao.cancelAppointment(owner, event);
		VEvent lookupResultAfterCancel = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNull(lookupResultAfterCancel);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJoinAndLeave() throws Exception {
		// construct owner from traditional user account
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		
		MockScheduleOwner owner = new MockScheduleOwner(user, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		owner.setPreference(Preferences.LOCATION, "meeting room");
		
		// construct visitor from traditional user account
		OracleCalendarUserAccount visitorUser = new OracleCalendarUserAccount();
		visitorUser.setUsername("jstalnak");
		visitorUser.setCtcalxitemid("20000:01220");
		visitorUser.setDisplayName("JAMES G STALNAKER");
		visitorUser.setEmailAddress("jstalnak@doit.wisc.edu");
		visitorUser.setGivenName("JAMES");
		visitorUser.setSurname("STALNAKER");
		MockScheduleVisitor visitor = new MockScheduleVisitor(visitorUser);
		
		Date startDate = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
		Date endDate = DateUtils.addHours(startDate, 1);
		final DateTime ical4jstart = new DateTime(startDate);
		final DateTime ical4jend = new DateTime(endDate);
		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 2);
		VEvent event = oracleCalendarDao.createAppointment(visitor, owner, block, "testCreateAppointment, join and leave");
		Assert.assertNotNull(event);
		event = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(event);
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, event.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(2, Integer.parseInt(event.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, event.getProperties(Attendee.ATTENDEE).size());
		Property visitorAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, visitor.getCalendarAccount());
		Assert.assertNotNull(visitorAttendee);
		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Property ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, owner.getCalendarAccount());
		Assert.assertNotNull(ownerAttendee);
		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		
		VEvent lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(2, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, lookupResult.getProperties(Attendee.ATTENDEE).size());
		
		// create 2nd visitor
		OracleCalendarUserAccount visitor2User = new OracleCalendarUserAccount();
		visitor2User.setUsername("mesdjian");
		visitor2User.setCtcalxitemid("20000:01599");
		visitor2User.setDisplayName("ARA H MESDJIAN");
		visitor2User.setEmailAddress("mesdjian@wisctest.wisc.edu");
		visitor2User.setGivenName("ARA");
		visitor2User.setSurname("MESDJIAN");
		MockScheduleVisitor visitor2 = new MockScheduleVisitor(visitor2User);
		
		// make 2nd visitor join
		oracleCalendarDao.joinAppointment(visitor2, owner, lookupResult);
		lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(2, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(3, lookupResult.getProperties(Attendee.ATTENDEE).size());
		PropertyList attendeeList = lookupResult.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeeList) {
			Attendee attendee = (Attendee) o;
			Parameter participationStatus = attendee.getParameter(PartStat.PARTSTAT);
			Assert.assertEquals(PartStat.ACCEPTED, participationStatus);
			if(AppointmentRole.OWNER.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
				Assert.assertEquals("mailto:" + owner.getCalendarAccount().getEmailAddress(), attendee.getValue());
			} else if (AppointmentRole.VISITOR.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
				String value = attendee.getValue();
				if(value.equals("mailto:" + visitor.getCalendarAccount().getEmailAddress()) || value.equals("mailto:"+visitor2.getCalendarAccount().getEmailAddress()) ) {
					// success
				} else {
					Assert.fail("unexpected visitor attendee value: " + value);
				}
			}
		}
		
		// now make visitor2 leave the appointment
		oracleCalendarDao.leaveAppointment(visitor2, owner, lookupResult);
		lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(2, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, lookupResult.getProperties(Attendee.ATTENDEE).size());
		attendeeList = lookupResult.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeeList) {
			Attendee attendee = (Attendee) o;
			Parameter participationStatus = attendee.getParameter(PartStat.PARTSTAT);
			Assert.assertEquals(PartStat.ACCEPTED, participationStatus);
			if(AppointmentRole.OWNER.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
				Assert.assertEquals("mailto:" + owner.getCalendarAccount().getEmailAddress(), attendee.getValue());
			} else if (AppointmentRole.VISITOR.equals(attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE))) {
				String value = attendee.getValue();
				if(value.equals("mailto:" + visitor.getCalendarAccount().getEmailAddress())) {
					// success
				} else {
					Assert.fail("unexpected visitor attendee value: " + value);
				}
			}
		}
		
		oracleCalendarDao.cancelAppointment(owner, lookupResult);
		VEvent lookupResultAfterCancel = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNull(lookupResultAfterCancel);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAndCancelRoleBasedAccount() throws Exception {
		// construct owner from role based account
		OracleCalendarUserAccount roleBasedAccount = new OracleCalendarUserAccount();
		roleBasedAccount.setUsername("doit-availab");
		roleBasedAccount.setCtcalxitemid("20000:01757");
		roleBasedAccount.setDisplayName("DOIT AvailableTest");
		roleBasedAccount.setEmailAddress("jstalnak@wisc.edu");
		roleBasedAccount.setSurname("AvailableTest");
		roleBasedAccount.setGivenName("DOIT");
		MockScheduleOwner owner = new MockScheduleOwner(roleBasedAccount, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		owner.setPreference(Preferences.LOCATION, "meeting room");
		
		// construct visitor from traditional user account
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		MockScheduleVisitor visitor = new MockScheduleVisitor(user);
		
		Date startDate = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
		Date endDate = DateUtils.addHours(startDate, 1);
		final DateTime ical4jstart = new DateTime(startDate);
		final DateTime ical4jend = new DateTime(endDate);
		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 1);
		VEvent event = oracleCalendarDao.createAppointment(visitor, owner, block, "testCreateEvent with role-based account as owner");
		Assert.assertNotNull(event);
		event = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertEquals(ical4jstart, event.getStartDate().getDate());
		Assert.assertEquals(ical4jend, event.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, event.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(1, Integer.parseInt(event.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals(2, event.getProperties(Attendee.ATTENDEE).size());
		Property visitorAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, visitor.getCalendarAccount());
		Assert.assertNotNull(visitorAttendee);
		Assert.assertEquals(AppointmentRole.VISITOR, visitorAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Property ownerAttendee = this.oracleEventUtils.getAttendeeForUserFromEvent(event, owner.getCalendarAccount());
		Assert.assertNotNull(ownerAttendee);
		Assert.assertEquals(AppointmentRole.OWNER, ownerAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE));
		Assert.assertEquals("testCreateEvent with role-based account as owner", event.getDescription().getValue());
		
		oracleCalendarDao.cancelAppointment(owner, event);
		VEvent lookupResultAfterCancel = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNull(lookupResultAfterCancel);
	}
	
	
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateEventResource() throws Exception {
		OracleCalendarResourceAccount resourceAccount = new OracleCalendarResourceAccount();
		resourceAccount.setCtcalxitemid("20000:01516");
		resourceAccount.setResourceName("DOIT blair test");
		resourceAccount.setOracleGuid("200000151619869596736320");
		MockScheduleOwner owner = new MockScheduleOwner(resourceAccount, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		MockScheduleVisitor visitor = new MockScheduleVisitor(user);
		
		Date startDate = DateUtils.truncate(new Date(), java.util.Calendar.MINUTE);
		Date endDate = DateUtils.addHours(startDate, 1);
		final DateTime ical4jstart = new DateTime(startDate);
		final DateTime ical4jend = new DateTime(endDate);
		AvailableBlock block = AvailableBlockBuilder.createBlock(startDate, endDate, 1);
		VEvent event = oracleCalendarDao.createAppointment(visitor, owner, block, "testCreateEvent with Resource as owner");
		Assert.assertNotNull(event);
		
		VEvent lookupResult = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNotNull(lookupResult);
		Assert.assertEquals(ical4jstart, lookupResult.getStartDate().getDate());
		Assert.assertEquals(ical4jend, lookupResult.getEndDate().getDate());
		Assert.assertEquals(SchedulingAssistantAppointment.TRUE, lookupResult.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT));
		Assert.assertEquals(1, Integer.parseInt(lookupResult.getProperty(VisitorLimit.VISITOR_LIMIT).getValue()));
		Assert.assertEquals("testCreateEvent with Resource as owner", lookupResult.getDescription().getValue());
		
		oracleCalendarDao.cancelAppointment(owner, event);
		VEvent lookupResultAfterCancel = oracleCalendarDao.getExistingAppointment(owner, block);
		Assert.assertNull(lookupResultAfterCancel);
	}
	
	/**
	 * 
	 * @throws InputFormatException
	 * @throws ParseException
	 * @throws ParserException 
	 * @throws IOException 
	 */
	@Test
	public void testReflectSchedule() throws InputFormatException, ParseException, IOException, ParserException {
		// construct owner from traditional user account
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();
		user.setUsername("npblair");
		user.setCtcalxitemid("20000:01182");
		user.setDisplayName("NICHOLAS P BLAIR");
		user.setEmailAddress("nblair@doit.wisc.edu");
		user.setGivenName("NICHOLAS");
		user.setSurname("BLAIR");
		
		MockScheduleOwner owner = new MockScheduleOwner(user, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		owner.setPreference(Preferences.LOCATION, "meeting room");
		
		Date startDate = CommonDateOperations.getDateFormat().parse("20100801");
		Date endDate = CommonDateOperations.getDateFormat().parse("20100807");
		Set<AvailableBlock> availableBlocks = AvailableBlockBuilder.createBlocks("9:00 AM", "3:00 PM", "MWF", 
				startDate, endDate);
		AvailableSchedule schedule = new AvailableSchedule(availableBlocks);
		oracleCalendarDao.reflectAvailableSchedule(owner, schedule);
		
		// verify events stored
		Session session = null;
		try {
			session = this.oracleCalendarDao.getSession(owner.getCalendarAccount(), oracleCalendarDao.getOracleCalendarServerNode(owner.getCalendarAccount()));
		
			Calendar reflections = oracleCalendarDao.getExistingAvailableScheduleReflections(owner, startDate, endDate, session);
			ComponentList components = reflections.getComponents();
			Assert.assertEquals(3, components.size());
			
			List<String> uids = new ArrayList<String>();
			for(Object o: components) {
				Component c = (Component) o;
				Assert.assertEquals("Available 9:00 AM - 3:00 PM", c.getProperty(Summary.SUMMARY).getValue()); 
				uids.add(c.getProperty(Uid.UID).getValue());
			}
			
			this.oracleCalendarDao.removeAvailableScheduleReflections(owner, uids, session);
			
		} catch (StatusException e) {
			e.printStackTrace();
			Assert.fail("status exception thrown verifying reflectAvailableSchedule results");
		} finally {
			this.oracleCalendarDao.disconnectSessionQuietly(session);
		}
	}
	
	/**
	 * 
	 * @throws InputFormatException
	 * @throws ParseException
	 * @throws ParserException 
	 * @throws IOException 
	 */
	@Test
	public void testReflectScheduleResource() throws InputFormatException, ParseException, IOException, ParserException {
		OracleCalendarResourceAccount resourceAccount = new OracleCalendarResourceAccount();
		resourceAccount.setCtcalxitemid("20000:01516");
		resourceAccount.setResourceName("DOIT blair test");
		resourceAccount.setOracleGuid("200000151619869596736320");
		MockScheduleOwner owner = new MockScheduleOwner(resourceAccount, 1);
		owner.setPreference(Preferences.MEETING_PREFIX, "prefix");
		
		Date startDate = CommonDateOperations.getDateFormat().parse("20100801");
		Date endDate = CommonDateOperations.getDateFormat().parse("20100807");
		Set<AvailableBlock> availableBlocks = AvailableBlockBuilder.createBlocks("9:00 AM", "3:00 PM", "MWF", 
				startDate, endDate);
		AvailableSchedule schedule = new AvailableSchedule(availableBlocks);
		oracleCalendarDao.reflectAvailableSchedule(owner, schedule);
		
		// verify events stored
		Session session = null;
		try {
			session = this.oracleCalendarDao.getSession(owner.getCalendarAccount(), oracleCalendarDao.getOracleCalendarServerNode(owner.getCalendarAccount()));
		
			Calendar reflections = oracleCalendarDao.getExistingAvailableScheduleReflections(owner, startDate, endDate, session);
			ComponentList components = reflections.getComponents();
			Assert.assertEquals(3, components.size());
			
			List<String> uids = new ArrayList<String>();
			for(Object o: components) {
				Component c = (Component) o;
				Assert.assertEquals("Available 9:00 AM - 3:00 PM", c.getProperty(Summary.SUMMARY).getValue()); 
				uids.add(c.getProperty(Uid.UID).getValue());
			}
			
			this.oracleCalendarDao.removeAvailableScheduleReflections(owner, uids, session);
			
		} catch (StatusException e) {
			e.printStackTrace();
			Assert.fail("status exception thrown verifying reflectAvailableSchedule results");
		} finally {
			this.oracleCalendarDao.disconnectSessionQuietly(session);
		}
	}
}
