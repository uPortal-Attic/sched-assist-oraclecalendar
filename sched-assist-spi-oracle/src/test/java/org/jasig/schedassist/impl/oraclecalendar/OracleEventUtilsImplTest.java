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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.time.DateUtils;
import org.easymock.EasyMock;
import org.jasig.schedassist.IAffiliationSource;
import org.jasig.schedassist.NullAffiliationSourceImpl;
import org.jasig.schedassist.model.AffiliationImpl;
import org.jasig.schedassist.model.AppointmentRole;
import org.jasig.schedassist.model.AvailabilityReflection;
import org.jasig.schedassist.model.AvailableBlock;
import org.jasig.schedassist.model.AvailableBlockBuilder;
import org.jasig.schedassist.model.AvailableSchedule;
import org.jasig.schedassist.model.CommonDateOperations;
import org.jasig.schedassist.model.DefaultEventUtilsImpl;
import org.jasig.schedassist.model.InputFormatException;
import org.jasig.schedassist.model.Preferences;
import org.jasig.schedassist.model.SchedulingAssistantAppointment;
import org.jasig.schedassist.model.VisibleSchedule;
import org.jasig.schedassist.model.VisibleScheduleBuilder;
import org.jasig.schedassist.model.VisitorLimit;
import org.jasig.schedassist.model.mock.MockCalendarAccount;
import org.jasig.schedassist.model.mock.MockScheduleOwner;
import org.jasig.schedassist.model.mock.MockScheduleVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Test harness for {@link OracleEventUtilsImpl}.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleEventUtilsImplTest.java 2923 2010-12-08 17:39:50Z npblair $
 */
public class OracleEventUtilsImplTest {

	private OracleEventUtilsImpl eventUtils = new OracleEventUtilsImpl(new NullAffiliationSourceImpl());
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConvertToICalendarFormatControl() throws Exception {
		String output = DefaultEventUtilsImpl.convertToICalendarFormat(makeDateTime("20091006-1243"));
		Assert.assertEquals("20091006T174300Z", output);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConvertToICalendarFormatInvalid() throws Exception {
		try {
			DefaultEventUtilsImpl.convertToICalendarFormat(null);
			Assert.fail("expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
			// success
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmailToURIControl() throws Exception {
		URI result = DefaultEventUtilsImpl.emailToURI("user@host.com");
		Assert.assertEquals("mailto:user@host.com", result.toString());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmailToURIInvalid() throws Exception {
		try {
			DefaultEventUtilsImpl.emailToURI(null);
			Assert.fail("expected IllegalArgumentException not thrown");
		} catch (IllegalArgumentException e) {
			// success
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAttendeeOwnerControl() throws Exception {
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("someowner@wisc.edu");
		calendarAccount.setDisplayName("Some A Owner");
		calendarAccount.setSurname("Owner");
		calendarAccount.setGivenName("Some");
		
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount, 1);
		
		Attendee attendee = this.eventUtils.constructAvailableAttendee(owner.getCalendarAccount(), AppointmentRole.OWNER, "OWNER_GUID_123");
		Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
		Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
		Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
		AppointmentRole role = (AppointmentRole) attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
		Assert.assertEquals(AppointmentRole.OWNER, role);
		Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
		Assert.assertEquals("OWNER_GUID_123", attendee.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
		
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAttendeeVisitorControl() throws Exception {
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		Attendee attendee = this.eventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR, "VISITOR_GUID_123");
		Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
		Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
		Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
		AppointmentRole role = (AppointmentRole) attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
		Assert.assertEquals(AppointmentRole.VISITOR, role);
		Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
		Assert.assertEquals("VISITOR_GUID_123", attendee.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAttendeeBothRolesControl() throws Exception {
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		Attendee attendee =this.eventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.BOTH, "VISITOR_GUID_123");
		Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
		Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
		Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
		AppointmentRole role = (AppointmentRole) attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
		Assert.assertEquals(AppointmentRole.BOTH, role);
		Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
		Assert.assertEquals("VISITOR_GUID_123", attendee.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAttendingMatchesPerson() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		
		
		Attendee visitorAttendee = this.eventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR);
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(visitorAttendee, visitor.getCalendarAccount()));
		Assert.assertFalse(this.eventUtils.attendeeMatchesPerson(visitorAttendee, owner.getCalendarAccount()));
		
		Attendee ownerAttendee = this.eventUtils.constructAvailableAttendee(owner.getCalendarAccount(), AppointmentRole.OWNER);
		Assert.assertFalse(this.eventUtils.attendeeMatchesPerson(ownerAttendee, visitor.getCalendarAccount()));
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(ownerAttendee, owner.getCalendarAccount()));
		
		Attendee bothAttendee = this.eventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.BOTH);
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(bothAttendee, visitor.getCalendarAccount()));
		
		bothAttendee = this.eventUtils.constructAvailableAttendee(owner.getCalendarAccount(), AppointmentRole.BOTH);
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(bothAttendee, owner.getCalendarAccount()));
	}
	
	@Test
	public void testAttendingMatchesPersonResource() throws Exception {
		OracleCalendarResourceAccount resource = new OracleCalendarResourceAccount();
		resource.setCtcalxitemid("20000:00001");
		resource.setResourceName("DOIT blair test");
		resource.setOracleGuid("200000000011234567890");
		MockScheduleOwner owner = new MockScheduleOwner(resource, 1);
		owner.setPreference(Preferences.LOCATION, "some building, rm 101");
		
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		Attendee visitorAttendee = this.eventUtils.constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR);
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(visitorAttendee, visitor.getCalendarAccount()));
		Assert.assertFalse(this.eventUtils.attendeeMatchesPerson(visitorAttendee, owner.getCalendarAccount()));
		
		OracleResourceAttendee resourceAttendee = this.eventUtils.constructOracleResourceAttendee(resource);
		Assert.assertFalse(this.eventUtils.attendeeMatchesPerson(resourceAttendee, visitor.getCalendarAccount()));
		Assert.assertTrue(this.eventUtils.attendeeMatchesPerson(resourceAttendee, owner.getCalendarAccount()));
	}
	
	/**
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testAttendeeMatchesPersonNullEmailAddress() throws URISyntaxException {
		Attendee attendee = new Attendee("mailto:person@wisc.edu");
		attendee.getParameters().add(new Cn("SOME PERSON"));

		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setDisplayName("SOME PERSON");
		calendarAccount.setSurname("PERSON");
		calendarAccount.setGivenName("SOME");

		Assert.assertFalse(this.eventUtils.attendeeMatchesPerson(attendee, calendarAccount));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentControl() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				"OWNER-GUID-123",
				visitor, 
				"VISITOR-GUID-123",
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertEquals("VISITOR-GUID-123", attendee.getParameter("X-ORACLE-GUID").getValue());
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertEquals("OWNER-GUID-123", attendee.getParameter("X-ORACLE-GUID").getValue());
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
			
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentBlockOverridesLocation() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330", 1, "alternate location"),
				owner, 
				"OWNER-GUID-123",
				visitor, 
				"VISITOR-GUID-123",
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("alternate location", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertEquals("VISITOR-GUID-123", attendee.getParameter("X-ORACLE-GUID").getValue());
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertEquals("OWNER-GUID-123", attendee.getParameter("X-ORACLE-GUID").getValue());
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
			
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentNullGuids() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				null,
				visitor, 
				null,
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentOnlyVisitorHasGuid() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				null,
				visitor, 
				"VISITOR-GUID-123",
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertEquals("VISITOR-GUID-123", attendee.getParameter("X-ORACLE-GUID").getValue());
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentVisitorIsStudentOwnerNotAdvisor() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount();
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				null,
				visitor, 
				null,
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
				Parameter oraclePersonalNote = attendee.getParameter(OracleEventUtilsImpl.ORACLE_PERSONAL_COMMENTS);
				Assert.assertNull(oraclePersonalNote);
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentVisitorIsStudentOwnerIsAdvisor() throws Exception {
		// construct visitor
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("wiscedustudentid", "studentidnumber");
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount(attributes);
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
	
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner
		Map<String, String> attributes2 = new HashMap<String, String>();
		attributes2.put("wisceduadvisorflag", "Y");
		OracleCalendarUserAccount calendarAccount2 = new OracleCalendarUserAccount(attributes2);
		calendarAccount2.setEmailAddress("someowner@wisc.edu");
		calendarAccount2.setDisplayName("Some A Owner");
		calendarAccount2.setSurname("Owner");
		calendarAccount2.setGivenName("Some");
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount2, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		// need to construct an AffiliationSource that will mock the "advisor" scenario
		IAffiliationSource affiliationSource = EasyMock.createMock(IAffiliationSource.class);
		expect(affiliationSource.doesAccountHaveAffiliation(calendarAccount2, AffiliationImpl.ADVISOR)).andReturn(true);
		replay(affiliationSource);
		OracleEventUtilsImpl alternate = new OracleEventUtilsImpl(affiliationSource);
		
		VEvent availableAppointment = alternate.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				null,
				visitor, 
				null,
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description [UW Student ID: studentidnumber]", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("VISITOR".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else if ("OWNER".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:someowner@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Owner", attendee.getParameter("CN").getValue());
				Parameter oraclePersonalNote = attendee.getParameter(OracleEventUtilsImpl.ORACLE_PERSONAL_COMMENTS);
				Assert.assertNotNull(oraclePersonalNote);
				byte[] valueDecoded = Base64.decodeBase64(oraclePersonalNote.getValue().getBytes());
				Assert.assertEquals("DARS reports for Some Visitor: https://dars.services.wisc.edu/?campus=studentidnumber", new String(valueDecoded));
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
		verify(affiliationSource);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentVisitorOwnerSamePerson() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner from same account
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330"),
				owner, 
				null,
				visitor, 
				null,
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("BOTH".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConstructAvailableAppointmentVisitorLimit2() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		// construct owner from same account
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount, 1);
		owner.setPreference(Preferences.LOCATION, "Owner's office");
		
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330", 2),
				owner, 
				null,
				visitor, 
				null,
				"test event description");
		
		Assert.assertEquals("Appointment", availableAppointment.getSummary().getValue());
		Assert.assertEquals(null, availableAppointment.getDescription());
		Assert.assertEquals("Owner's office", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("2", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		for(Object o : attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if("BOTH".equals(appointmentRole.getValue())) {
				Assert.assertEquals("mailto:somevisitor@wisc.edu", attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals("Some Visitor", attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected value for appointment role: " + appointmentRole.getValue());
			}
		}
	}
	
	@Test
	public void testConstructAvailableAppointmentOwnerIsResource() throws Exception {
		// construct visitor
		OracleCalendarUserAccount calendarAccount = new OracleCalendarUserAccount();
		calendarAccount.setEmailAddress("somevisitor@wisc.edu");
		//calendarAccount.setDisplayName("Some A Visitor");
		calendarAccount.setSurname("Visitor");
		calendarAccount.setGivenName("Some");
		MockScheduleVisitor visitor = new MockScheduleVisitor(calendarAccount);
		
		OracleCalendarResourceAccount resource = new OracleCalendarResourceAccount();
		resource.setCtcalxitemid("20000:00001");
		resource.setResourceName("DOIT blair test");
		resource.setOracleGuid("200000000011234567890");
		
		MockScheduleOwner owner = new MockScheduleOwner(resource, 1);
		owner.setPreference(Preferences.LOCATION, "some building, rm 101");
		owner.setPreference(Preferences.MEETING_PREFIX, "Appointment");
		VEvent availableAppointment = this.eventUtils.constructAvailableAppointment(
				AvailableBlockBuilder.createBlock("20091006-1300", "20091006-1330", 1),
				owner, 
				visitor, 
				"test event description");
		
		Assert.assertEquals("Appointment with Some Visitor", availableAppointment.getSummary().getValue());
		Assert.assertEquals("test event description", availableAppointment.getDescription().getValue());
		Assert.assertEquals("some building, rm 101", availableAppointment.getLocation().getValue());
		Assert.assertEquals(makeDateTime("20091006-1300"), availableAppointment.getStartDate().getDate());
		Assert.assertEquals(makeDateTime("20091006-1330"), availableAppointment.getEndDate().getDate());
		Assert.assertEquals("TRUE", availableAppointment.getProperty(SchedulingAssistantAppointment.AVAILABLE_APPOINTMENT).getValue());
		Assert.assertEquals("1", availableAppointment.getProperty(VisitorLimit.VISITOR_LIMIT).getValue());
		Assert.assertEquals(Status.VEVENT_CONFIRMED, availableAppointment.getProperty(Status.STATUS));
		PropertyList attendeePropertyList = availableAppointment.getProperties(Attendee.ATTENDEE);
		// when constructed, an available appointment with a resource as the schedule owner, the attendee list will have 2 elements
		// oracle won't keep the 2nd attendee when stored, but that's immaterial to this test
		Assert.assertEquals(2, attendeePropertyList.size());
		
		for(Object o: attendeePropertyList) {
			Property attendee = (Property) o;
			Assert.assertEquals(PartStat.ACCEPTED, attendee.getParameter(PartStat.PARTSTAT));
			Assert.assertEquals(CuType.INDIVIDUAL, attendee.getParameter(CuType.CUTYPE));
			Assert.assertEquals(Rsvp.FALSE, attendee.getParameter(Rsvp.RSVP));
			Parameter appointmentRole = attendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
			if(AppointmentRole.VISITOR.equals(appointmentRole)) {
				Assert.assertEquals(OracleEventUtilsImpl.emailToURI(calendarAccount.getEmailAddress()).toString(), attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals(calendarAccount.getDisplayName(), attendee.getParameter("CN").getValue());
			} else if (AppointmentRole.OWNER.equals(appointmentRole)) {
				Assert.assertEquals(OracleEventUtilsImpl.emailToURI(resource.getEmailAddress()).toString(), attendee.getValue());
				Assert.assertNull(attendee.getParameter("X-ORACLE-GUID"));
				Assert.assertEquals(resource.getDisplayName(), attendee.getParameter("CN").getValue());
			} else {
				Assert.fail("unexpected appointment role: " + appointmentRole);
			}
		}
		
		
		// test the special XProperty created to persist the resource attendee
		PropertyList resourceAttendeePropertyList = availableAppointment.getProperties(OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE);
		Assert.assertEquals(1, resourceAttendeePropertyList.size());
		Property resourceAttendee = (Property) resourceAttendeePropertyList.get(0);
		Assert.assertEquals(PartStat.ACCEPTED, resourceAttendee.getParameter(PartStat.PARTSTAT));
		Assert.assertEquals(CuType.INDIVIDUAL, resourceAttendee.getParameter(CuType.CUTYPE));
		Assert.assertEquals(Rsvp.FALSE, resourceAttendee.getParameter(Rsvp.RSVP));
		Parameter resourceAppointmentRole = resourceAttendee.getParameter(AppointmentRole.APPOINTMENT_ROLE);
		Assert.assertEquals(AppointmentRole.OWNER, resourceAppointmentRole);
		Assert.assertEquals(OracleEventUtilsImpl.emailToURI(resource.getEmailAddress()).toString(), resourceAttendee.getValue());
		Assert.assertEquals("200000000011234567890", resourceAttendee.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals(resource.getDisplayName(), resourceAttendee.getParameter("CN").getValue());
		
	}
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testIsOracleShowAsFree() throws Exception {
		// test with no ex-params
		Attendee attendee = new Attendee(DefaultEventUtilsImpl.emailToURI("someone@wisc.edu"));
		Assert.assertFalse(OracleEventUtilsImpl.isOracleShowAsFree(attendee));
		
		// test with non-matching ex-params
		XParameter x1 = new XParameter("X-PARAM1", "v1");
		XParameter x2 = new XParameter("X-PARAM2", "v2");
		attendee.getParameters().add(x1);
		attendee.getParameters().add(x2);
		Assert.assertFalse(OracleEventUtilsImpl.isOracleShowAsFree(attendee));
		
		// test with matching ex-param, but set to BUSY
		XParameter x3 = new XParameter("X-ORACLE-SHOWASFREE", "BUSY");
		attendee.getParameters().add(x3);
		Assert.assertFalse(OracleEventUtilsImpl.isOracleShowAsFree(attendee));
		
		// now test successful state
		attendee = new Attendee(DefaultEventUtilsImpl.emailToURI("someoneelse@wisc.edu"));
		attendee.getParameters().add(x1);
		attendee.getParameters().add(x2);
		XParameter x4 = new XParameter("X-ORACLE-SHOWASFREE", "FREE");
		attendee.getParameters().add(x4);
		Assert.assertTrue(OracleEventUtilsImpl.isOracleShowAsFree(attendee));
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getAttendeeForUserFromEvent(VEvent, CalendarUser)} on an 
	 * event with 0 {@link Attendee}s, assert null return.
	 * @throws Exception
	 */
	@Test
	public void testGetAttendeeForUserFromEventNoAttendees() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");
		
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		
		Assert.assertNull(this.eventUtils.getAttendeeForUserFromEvent(someEvent, user));
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getAttendeeForUserFromEvent(VEvent, CalendarUser)} on an 
	 * event with 0 {@link Attendee}s, assert null return.
	 * @throws Exception
	 */
	@Test
	public void testGetAttendeeForUserFromEventControl() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");
		
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN DIFFERENT"));
		
		someEvent.getProperties().add(attendee);
		someEvent.getProperties().add(attendee2);
		
		Property result = this.eventUtils.getAttendeeForUserFromEvent(someEvent, user);
		Assert.assertNotNull(result);
		Assert.assertEquals("mailto:person@domain.edu", result.getValue());
		Assert.assertEquals("GIVEN SURNAME", result.getParameter(Cn.CN).getValue());
		
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getScheduleVisitorCount(someEvent)} on an 
	 * event with 0 {@link Attendee}s, assert 0 return.
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableVisitorCountFromEventNoAttendees() throws Exception {
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		
		Assert.assertEquals(0, this.eventUtils.getScheduleVisitorCount(someEvent));
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getScheduleVisitorCount(someEvent)} on an 
	 * event with 2 {@link Attendee}s (one owner, one visitor), assert return 1.
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableVisitorCountFromEventControl() throws Exception {
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		
		Attendee owner = new Attendee();
		owner.setValue("mailto:person@domain.edu");
		owner.getParameters().add(new Cn("OWNER SURNAME"));
		owner.getParameters().add(AppointmentRole.OWNER);
		
		Attendee visitor = new Attendee();
		visitor.setValue("mailto:visitor@domain.edu");
		visitor.getParameters().add(new Cn("VISITOR SURNAME"));
		visitor.getParameters().add(AppointmentRole.VISITOR);
		
		someEvent.getProperties().add(owner);
		someEvent.getProperties().add(visitor);
		
		Assert.assertEquals(1, this.eventUtils.getScheduleVisitorCount(someEvent));
		Assert.assertEquals(2, someEvent.getProperties(Attendee.ATTENDEE).size());
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getScheduleVisitorCount(someEvent)} on an 
	 * event with 41 {@link Attendee}s (one owner, 40 visitors), assert return 40.
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableVisitorCountFromEventLarge() throws Exception {
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		
		Attendee owner = new Attendee();
		owner.setValue("mailto:person@domain.edu");
		owner.getParameters().add(new Cn("OWNER SURNAME"));
		owner.getParameters().add(AppointmentRole.OWNER);
		someEvent.getProperties().add(owner);
		
		for(int i = 1; i <= 40; i++) {
			Attendee visitor = new Attendee();
			visitor.setValue("mailto:visitor" + i +"@domain.edu");
			visitor.getParameters().add(new Cn("VISITOR SURNAME"+i));
			visitor.getParameters().add(AppointmentRole.VISITOR);
			someEvent.getProperties().add(visitor);
		}
		Assert.assertEquals(40, this.eventUtils.getScheduleVisitorCount(someEvent));
		Assert.assertEquals(41,  someEvent.getProperties(Attendee.ATTENDEE).size());
	}
	
	/**
	 * Call {@link OracleEventUtilsImpl#getScheduleVisitorCount(someEvent)} on an 
	 * event with 41 {@link Attendee}s (one owner, 40 visitors). 
	 * 20 of the visitors will have PartStat set to DECLINED.
	 * 
	 * Assert return value of 20.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetAvailableVisitorCountFromEventPartStat() throws Exception {
		VEvent someEvent = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20091104-1030")),
				"some appointment");
		
		Attendee owner = new Attendee();
		owner.setValue("mailto:person@domain.edu");
		owner.getParameters().add(new Cn("OWNER SURNAME"));
		owner.getParameters().add(AppointmentRole.OWNER);
		someEvent.getProperties().add(owner);
		
		for(int i = 1; i <= 40; i++) {
			Attendee visitor = new Attendee();
			visitor.setValue("mailto:visitor" + i +"@domain.edu");
			visitor.getParameters().add(new Cn("VISITOR SURNAME"+i));
			visitor.getParameters().add(AppointmentRole.VISITOR);
			if(i % 2 == 0) {
				visitor.getParameters().add(PartStat.DECLINED);
			}
			someEvent.getProperties().add(visitor);
		}
		Assert.assertEquals(40, this.eventUtils.getScheduleVisitorCount(someEvent));
		Assert.assertEquals(41,  someEvent.getProperties(Attendee.ATTENDEE).size());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBlockToDayEventSmall() throws Exception {
		Date date = CommonDateOperations.parseDateTimePhrase("20091203-1500");
		AvailableBlock block = AvailableBlockBuilder.createSmallestAllowedBlock(date);
		
		VEvent dayEvent = OracleEventUtilsImpl.blockToDayEvent(block);
		Assert.assertEquals("Available 3:00 PM - 3:05 PM", dayEvent.getSummary().getValue());
		Assert.assertTrue(dayEvent.getProperties().contains(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY));
		net.fortuna.ical4j.model.Date expectedStartDate = new net.fortuna.ical4j.model.Date("20091203");
		Assert.assertEquals(expectedStartDate, dayEvent.getStartDate().getDate());
		Assert.assertEquals(1, dayEvent.getDuration().getDuration().getDays());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBlockToDayEventControl() throws Exception {
		AvailableBlock block = AvailableBlockBuilder.createBlock("20091203-0900", "20091203-1700");
		
		VEvent dayEvent = OracleEventUtilsImpl.blockToDayEvent(block);
		Assert.assertEquals("Available 9:00 AM - 5:00 PM", dayEvent.getSummary().getValue());
		Assert.assertTrue(dayEvent.getProperties().contains(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY));
		net.fortuna.ical4j.model.Date expectedStartDate = new net.fortuna.ical4j.model.Date("20091203");
		Assert.assertEquals(expectedStartDate, dayEvent.getStartDate().getDate());
		Assert.assertEquals(1, dayEvent.getDuration().getDuration().getDays());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConvertBlocksToDayEvents() throws Exception {
		SortedSet<AvailableBlock> blocks = AvailableBlockBuilder.createBlocks("12:00 PM",
				"11:00 PM", 
				"MWF", 
				CommonDateOperations.parseDatePhrase("20091201"),
				CommonDateOperations.parseDatePhrase("20091231"));
		
		Calendar dayEventCalendar = OracleEventUtilsImpl.convertBlocksToDayEvents(blocks);
		ComponentList components = dayEventCalendar.getComponents(VEvent.VEVENT);
		Assert.assertEquals(13, components.size());
		for(Object o : components) {
			VEvent event = (VEvent) o;
			Assert.assertEquals("Available 12:00 PM - 11:00 PM", event.getSummary().getValue());
			Assert.assertTrue(event.getProperties().contains(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY));
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWillCauseConflictControl() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");
		
		VEvent event = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1030")),
				"some conflicting appointment");
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN2 SURNAME2"));
		
		event.getProperties().add(attendee);
		event.getProperties().add(attendee2);
		event.getProperties().add(OracleEventUtilsImpl.ORACLE_APPOINTMENT_PROPERTY);
		
		Assert.assertTrue(this.eventUtils.willEventCauseConflict(user, event));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWillCauseConflictOracleBusy() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");
		
		VEvent event = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1030")),
			"some conflicting appointment");
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(new XParameter("X-ORACLE-SHOWASFREE", "BUSY"));
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN2 SURNAME2"));
		
		event.getProperties().add(attendee);
		event.getProperties().add(attendee2);
		event.getProperties().add(OracleEventUtilsImpl.ORACLE_APPOINTMENT_PROPERTY);
		
		Assert.assertTrue(this.eventUtils.willEventCauseConflict(user, event));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWillCauseConflictPartStatNeedsAction() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");
		
		VEvent event = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1000")),
				new net.fortuna.ical4j.model.DateTime(makeDateTime("20100405-1030")),
		"some conflicting appointment");
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(PartStat.NEEDS_ACTION);
		attendee.getParameters().add(new XParameter("X-ORACLE-SHOWASFREE", "BUSY"));
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN2 SURNAME2"));
		
		event.getProperties().add(attendee);
		event.getProperties().add(attendee2);
		event.getProperties().add(OracleEventUtilsImpl.ORACLE_APPOINTMENT_PROPERTY);
		
		Assert.assertFalse(this.eventUtils.willEventCauseConflict(user, event));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWillCauseConflictDayEvent() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");

		VEvent event = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDate("20100405")),
		"some conflicting appointment");
		
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(PartStat.NEEDS_ACTION);
		attendee.getParameters().add(new XParameter("X-ORACLE-SHOWASFREE", "BUSY"));
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN2 SURNAME2"));
		
		event.getProperties().add(OracleEventUtilsImpl.ORACLE_DAY_EVENT_PROPERTY);
		event.getProperties().add(attendee);
		event.getProperties().add(attendee2);
		
		Assert.assertFalse(this.eventUtils.willEventCauseConflict(user, event));
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWillCauseConflictDailyNote() throws Exception {
		Map<String, String> userAttributes = new HashMap<String, String>();
		OracleCalendarUserAccount user = new OracleCalendarUserAccount(userAttributes);
		user.setCtcalxitemid("10000:00001");
		user.setEmailAddress("person@domain.edu");
		user.setDisplayName("GIVEN M SURNAME");
		user.setUsername("person");
		user.setGivenName("GIVEN");
		user.setSurname("SURNAME");

		VEvent event = new VEvent(new net.fortuna.ical4j.model.DateTime(makeDate("20100405")),
		"some conflicting appointment");
		
		Attendee attendee = new Attendee();
		attendee.setValue("mailto:person@domain.edu");
		attendee.getParameters().add(PartStat.NEEDS_ACTION);
		attendee.getParameters().add(new XParameter("X-ORACLE-SHOWASFREE", "BUSY"));
		attendee.getParameters().add(new Cn("GIVEN SURNAME"));
		
		Attendee attendee2 = new Attendee();
		attendee2.setValue("mailto:person2@domain.edu");
		attendee2.getParameters().add(new Cn("GIVEN2 SURNAME2"));
		
		event.getProperties().add(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY);
		event.getProperties().add(attendee);
		event.getProperties().add(attendee2);
		
		Assert.assertFalse(this.eventUtils.willEventCauseConflict(user, event));
	}
	
	@Test
	public void testScheduleForReflection() throws InputFormatException, ParseException {
		SortedSet<AvailableBlock> blocks = AvailableBlockBuilder.createBlocks(
				"9:00 AM", "3:00 PM", "MWF", 
				makeDateTime("20100808-0000"), 
				makeDateTime("20100814-0000"));
		
		// expand to 30 minute blocks first
		blocks = AvailableBlockBuilder.expand(blocks, 30);
		List<net.fortuna.ical4j.model.Calendar> calendars = this.eventUtils.convertScheduleForReflection(new AvailableSchedule(blocks));
		Assert.assertEquals(1, calendars.size());
		net.fortuna.ical4j.model.Calendar calendar = calendars.get(0); 
		Assert.assertEquals(3, calendar.getComponents().size());
		ComponentList components = calendar.getComponents(VEvent.VEVENT);
		for(Object o : components) {
			VEvent event = (VEvent) o;
			Assert.assertEquals("Available 9:00 AM - 3:00 PM", event.getSummary().getValue());
			Assert.assertEquals(AvailabilityReflection.TRUE, event.getProperty(AvailabilityReflection.AVAILABILITY_REFLECTION));
			Assert.assertEquals(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY, event.getProperty(OracleEventUtilsImpl.ORACLE_EVENTTYPE));
		}
	}
	
	@Test
	public void testResourceOwnerGroupAppointment() throws IOException, ParserException {
		ClassPathResource resource = new ClassPathResource("org/jasig/schedassist/impl/oraclecalendar/resource-owner-group-appt.ics");
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(resource.getInputStream());
		Assert.assertNotNull(cal);
		ComponentList list = cal.getComponents();
		Assert.assertEquals(1, list.size());
		VEvent event = (VEvent) list.get(0);
		Assert.assertEquals("Resource Owner, Group Appointment", event.getSummary().getValue());
		PropertyList attendees = this.eventUtils.getAttendeeListFromEvent(event);
		Assert.assertEquals(2, attendees.size());
		for(Object o: attendees) {
			Property p = (Property) o;
			if(Attendee.ATTENDEE.equals(p.getName())) {
				// visitor
				Assert.assertEquals("mailto:mesdjian@wisctest.wisc.edu", p.getValue());
				
				
			} else if (OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE.equals(p.getName())) {
				// owner
				
			} else {
				Assert.fail("unexpected property in attendee list: " + p);
			}
		}
		
		MockCalendarAccount visitorAccount = new MockCalendarAccount();
		visitorAccount.setDisplayName("ARA MESDJIAN");
		visitorAccount.setEmailAddress("mesdjian@wisctest.wisc.edu");
		Assert.assertTrue(this.eventUtils.isAttendingAsVisitor(event, visitorAccount));
		
		OracleCalendarResourceAccount resourceAccount = new OracleCalendarResourceAccount();
		resourceAccount.setResourceName("DOIT Ara Testing 333");
		resourceAccount.setOracleGuid("803858C134BC57A9E04400144FAD412A");
		Assert.assertTrue(this.eventUtils.isAttendingAsOwner(event, resourceAccount));
	}
	
	/**
	 * The test is is focused on the results of an event containing data in Oracle's Personal Notes feature.
	 * Oracle Base64's the value of the attendee's Personal Notes and stores the result in the
	 * X-ORACLE-PERSONAL-COMMENT x parameter (and an RTF version in X-ORACLE-PERSONAL-COMMENT-RTF).
	 * 
	 * The Base64 algorithm sometimes outputs a equals sign ('=') at the end as a pad, 
	 * see http://en.wikipedia.org/wiki/Base64
	 * 
	 * The RFC for iCalendar includes the equals sign in the range of valid characters for a parameter.
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	@Test
	public void testOraclePersonalNotes() throws IOException, ParserException {
		ClassPathResource resource = new ClassPathResource("org/jasig/schedassist/impl/oraclecalendar/personal-notes-test.ics");
		
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(resource.getInputStream());
		Assert.assertNotNull(cal);
		ComponentList list = cal.getComponents();
		Assert.assertEquals(1, list.size());
		VEvent event = (VEvent) list.get(0);
		Assert.assertEquals("personal notes test", event.getSummary().getValue());
		Attendee attendee = (Attendee) event.getProperty(Attendee.ATTENDEE);
		
		ParameterList paramList = attendee.getParameters();
		Assert.assertEquals(10, paramList.size());
		Assert.assertEquals("e1xydGYxXHVjMCBteSBwZXJzb25hbCBub3Rlc30A", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT-RTF").getValue());
		Assert.assertEquals("bXkgcGVyc29uYWwgbm90ZXM=", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT").getValue());
		Assert.assertEquals("TRUE", paramList.getParameter("X-ORACLE-ISTIMEOK").getValue());
		Assert.assertEquals("FALSE", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT-ISDIRTY").getValue());
		Assert.assertEquals("BUSY", paramList.getParameter("X-ORACLE-SHOWASFREE").getValue());
		Assert.assertEquals("200000118219869582153896", paramList.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals("INDIVIDUAL", paramList.getParameter("CUTYPE").getValue());
		Assert.assertEquals("FALSE", paramList.getParameter("RSVP").getValue());
		Assert.assertEquals("NICHOLAS BLAIR", paramList.getParameter("CN").getValue());
		Assert.assertEquals("ACCEPTED", paramList.getParameter("PARTSTAT").getValue());
		
		Assert.assertEquals("mailto:nblair@doit.wisc.edu", attendee.getValue());
	}
	
	/**
	 * Related to {@link #testOraclePersonalNotes()}, this test validates an
	 * event with empty X-ORACLE-PERSONAL-COMMENT parameter.
	 * 
	 * @throws IOException
	 * @throws ParserException
	 */
	@Test
	public void testEmptyOraclePersonalNotes() throws IOException, ParserException {
		ClassPathResource resource = new ClassPathResource("org/jasig/schedassist/impl/oraclecalendar/personal-notes-test-empty.ics");
		
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(resource.getInputStream());
		Assert.assertNotNull(cal);
		ComponentList list = cal.getComponents();
		Assert.assertEquals(1, list.size());
		VEvent event = (VEvent) list.get(0);
		Assert.assertEquals("personal notes test", event.getSummary().getValue());
		Attendee attendee = (Attendee) event.getProperty(Attendee.ATTENDEE);
		
		ParameterList paramList = attendee.getParameters();
		Assert.assertEquals(10, paramList.size());
		Assert.assertEquals("AA==", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT-RTF").getValue());
		Assert.assertEquals("", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT").getValue());
		Assert.assertEquals("TRUE", paramList.getParameter("X-ORACLE-ISTIMEOK").getValue());
		Assert.assertEquals("FALSE", paramList.getParameter("X-ORACLE-PERSONAL-COMMENT-ISDIRTY").getValue());
		Assert.assertEquals("BUSY", paramList.getParameter("X-ORACLE-SHOWASFREE").getValue());
		Assert.assertEquals("200000118219869582153896", paramList.getParameter("X-ORACLE-GUID").getValue());
		Assert.assertEquals("INDIVIDUAL", paramList.getParameter("CUTYPE").getValue());
		Assert.assertEquals("FALSE", paramList.getParameter("RSVP").getValue());
		Assert.assertEquals("NICHOLAS BLAIR", paramList.getParameter("CN").getValue());
		Assert.assertEquals("ACCEPTED", paramList.getParameter("PARTSTAT").getValue());
		
		Assert.assertEquals("mailto:nblair@doit.wisc.edu", attendee.getValue());
	}
	
	/**
	 * 
	 * @throws ParserException 
	 * @throws IOException 
	 * @throws InputFormatException 
	 * @throws ParseException 
	 */
	@Test
	public void testResourceSchedule() throws IOException, ParserException, InputFormatException, ParseException {
		ClassPathResource resource = new ClassPathResource("org/jasig/schedassist/impl/oraclecalendar/resource-owner-schedule.ics");
		
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING, true);
		CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_UNFOLDING, true);
		CalendarBuilder builder = new CalendarBuilder();
		Calendar cal = builder.build(resource.getInputStream());
		Assert.assertNotNull(cal);
		
		Set<AvailableBlock> blocks = new TreeSet<AvailableBlock>();
		blocks.add(AvailableBlockBuilder.createBlock("20101108-1330", "20101108-1500"));
		blocks.add(AvailableBlockBuilder.createBlock("20101109-0930", "20101109-1300"));
		blocks.add(AvailableBlockBuilder.createBlock("20101111-0930", "20101111-1300"));
		blocks.add(AvailableBlockBuilder.createBlock("20101112-1330", "20101112-1600"));
		
		AvailableSchedule schedule = new AvailableSchedule(AvailableBlockBuilder.expand(blocks, 30));
		
		VisibleScheduleBuilder scheduleBuilder = new VisibleScheduleBuilder(this.eventUtils);
		
		OracleCalendarResourceAccount calendarAccount = new OracleCalendarResourceAccount();
		calendarAccount.setResourceName("MUS WARF Interviews");
		calendarAccount.setOracleGuid("94144ACB42663A86E04400144F2BD678");
		calendarAccount.setLocation("{5542 Humanities}");
		
		Assert.assertEquals("94144ACB42663A86E04400144F2BD678@email.invalid", calendarAccount.getEmailAddress());
		MockScheduleOwner owner = new MockScheduleOwner(calendarAccount, 1L);
		owner.setPreference(Preferences.MEETING_LIMIT, "1");
		owner.setPreference(Preferences.DURATIONS, "30");
		owner.setPreference(Preferences.MEETING_PREFIX, "WARF Interview");
		owner.setPreference(Preferences.LOCATION, "5542 Humanities");
		
		VisibleSchedule visibleSchedule = scheduleBuilder.calculateVisibleSchedule(makeDate("20101105"), 
				makeDate("20101119"), cal, schedule, owner);
		
		List<AvailableBlock> busy = visibleSchedule.getBusyList();
		Assert.assertEquals(9, busy.size());
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101108-1330", "20101108-1400")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101109-0930", "20101109-1000")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101109-1000", "20101109-1030")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101109-1030", "20101109-1100")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101109-1100", "20101109-1130")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101109-1130", "20101109-1200")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101111-1230", "20101111-1300")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101112-1500", "20101112-1530")));
		Assert.assertTrue(busy.contains(AvailableBlockBuilder.createBlock("20101112-1530", "20101112-1600")));
		List<AvailableBlock> free = visibleSchedule.getFreeList();
		Assert.assertEquals(13, free.size());
		
	}
	
	/**
	 * helper method to create java.util.Date objects from a String
	 * 
	 * @param dateTimePhrase format is "yyyyMMdd-HHmm"
	 * @return
	 * @throws ParseException
	 */
	private Date makeDateTime(String dateTimePhrase) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
		Date time = dateFormat.parse(dateTimePhrase);
		DateUtils.truncate(time, java.util.Calendar.SECOND);
		return time;
	}
	
	private Date makeDate(String datePhrase) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = dateFormat.parse(datePhrase);
		DateUtils.truncate(date, java.util.Calendar.DATE);
		return date;
	}
}
