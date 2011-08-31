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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SortedSet;
import java.util.TimeZone;

import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.XProperty;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.jasig.schedassist.IAffiliationSource;
import org.jasig.schedassist.model.AffiliationImpl;
import org.jasig.schedassist.model.AppointmentRole;
import org.jasig.schedassist.model.AvailableBlock;
import org.jasig.schedassist.model.AvailableBlockBuilder;
import org.jasig.schedassist.model.AvailableVersion;
import org.jasig.schedassist.model.DefaultEventUtilsImpl;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.model.IScheduleOwner;
import org.jasig.schedassist.model.IScheduleVisitor;
import org.jasig.schedassist.model.Preferences;
import org.jasig.schedassist.model.SchedulingAssistantAppointment;
import org.jasig.schedassist.model.VisitorLimit;

/**
 * This class contains methods that help in the construction and interaction
 * with iCalendar events (as modeled by iCal4j).
 * 
 * @see VEvent
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleEventUtilsImpl.java 2923 2010-12-08 17:39:50Z npblair $
 */
public final class OracleEventUtilsImpl extends DefaultEventUtilsImpl {

	/**
	 * Date/time format for iCalendar.
	 */
	public static final String ICAL_DATETIME_FORMAT = "yyyyMMdd'T'HHmmss'Z'";
	
	/**
	 * Name of the {@link XParameter} Oracle adds to {@link Attendee} if the person
	 * wishes to be "shown as free" even though they have accepted.
	 */
	protected static final String ORACLE_SHOWASFREE = "X-ORACLE-SHOWASFREE";
	
	/**
	 * Name of the {@link XParameter} Oracle adds to {@link Attendee} to describe
	 * the person's internal Oracle ID.
	 */
	protected static final String ORACLE_GUID = "X-ORACLE-GUID";
	
	/**
	 * Name of the {@link XProperty} Oracle adds to events to describe
	 * various types (APPOINTMENT, DAY EVENT, DAILY NOTE)
	 */
	protected static final String ORACLE_EVENTTYPE = "X-ORACLE-EVENTTYPE";
	
	/**
	 * Name of the {@link XParameter} Oracle adds to {@link Attendee} properties
	 * to populate their "Personal Notes" field.
	 */
	protected static final String ORACLE_PERSONAL_COMMENTS = "X-ORACLE-PERSONAL-COMMENT";
	
	/**
	 * URL prefix for dars web application
	 */
	private static final String DARS_WEB_PREFIX = "https://dars.services.wisc.edu/?campus=";
	/**
	 * {@link Property} that signals the {@link VEvent} is an Oracle "DAILY NOTE".
	 */
	public static final Property ORACLE_APPOINTMENT_PROPERTY = new XProperty(ORACLE_EVENTTYPE, "APPOINTMENT");
	/**
	 * {@link Property} that signals the {@link VEvent} is an Oracle "DAILY NOTE".
	 */
	public static final Property ORACLE_DAILY_NOTE_PROPERTY = new XProperty(ORACLE_EVENTTYPE, "DAILY NOTE");
	/**
	 * {@link Property} that signals the {@link VEvent} is an Oracle "DAY EVENT".
	 */
	public static final Property ORACLE_DAY_EVENT_PROPERTY = new XProperty(ORACLE_EVENTTYPE, "DAY EVENT");

	// Commons-Lang provides a thread-safe replacement for SimpleDateFormat
	public static final FastDateFormat FASTDATEFORMAT = FastDateFormat.getInstance(ICAL_DATETIME_FORMAT, 
			TimeZone.getTimeZone("UTC"));

	/**
	 * 
	 * @param affiliationSource
	 */
	public OracleEventUtilsImpl(IAffiliationSource affiliationSource) {
		super(affiliationSource);
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.DefaultEventUtilsImpl#attendeeMatchesPerson(net.fortuna.ical4j.model.Property, org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Override
	public boolean attendeeMatchesPerson(Property attendee,
			ICalendarAccount calendarAccount) {
		if(null == attendee) { 
			return false;
		}
		if( !Attendee.ATTENDEE.equals(attendee.getName()) && !OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE.equals(attendee.getName())) {
			return false;
		}
		
		Cn cn = (Cn) attendee.getParameter(Cn.CN);
		if(null == cn) {
			return false;
		}
		boolean cnResult = cn.getValue().equals(calendarAccount.getDisplayName());
		final String emailAddress = calendarAccount.getEmailAddress();
		if(null == emailAddress) {
			LOG.warn("returning false for calendarAccount with null email address in attendeeMatchesPerson: " + calendarAccount);
			return false;
		}
		URI mailTo = emailToURI(emailAddress);
		boolean mailResult = attendee.getValue().equals(mailTo.toString());

		return cnResult && mailResult;
	}

	/**
	 * Short-hand method to call {@link OracleEventUtilsImpl#constructAvailableAppointment(AvailableBlock, ScheduleOwner, String, ScheduleVisitor, String, String)}
	 * with null for the values of ownerGuid and visitorGuid.
	 * 
	 * @param block
	 * @param visitor
	 * @param owner
	 * @param eventDescription
	 * @return
	 */
	@Override
	public VEvent constructAvailableAppointment(final AvailableBlock block, final IScheduleOwner owner, final IScheduleVisitor visitor, 
			final String eventDescription) {
		return constructAvailableAppointment(block, owner, null, visitor, null, eventDescription);
	}

	/**
	 * Construct an iCalendar EVENT for the Available system.
	 * 
	 * The SUMMARY of the EVENT will start with the owner's MEETING_PREFIX preference and end with the full name of the visitor.
	 * The LOCATION of the EVENT will be set to the owner's location preference.
	 * The X-ORACLE-EVENTTYPE property will be set to "APPOINTMENT."
	 * The CLASS property will be set to "NORMAL".
	 * 
	 * If the owner and visitor represent the same person, only one ATTENDEE will be added, and will be marked with 
	 * {@link AppointmentRole#BOTH}.
	 * Otherwise, owner and visitor will be added as ATTENDEEs with the corresponding {@link AppointmentRole}.
	 * 
	 * The eventDescription argument will be added to the DESCRIPTION of the event. If the owner is detected as an academic advisor, and 
	 * the visitor is a student, the student's "wiscedustudentid" value will be appended to the DESCRIPTION.
	 * 
	 * @param block the selected {@link AvailableBlock} 
	 * @param owner the owner of the appointment
	 * @param ownerGuid the Oracle GUID for the owner (may be null)
	 * @param visitor the visitor to the appointment
	 * @param visitorGuid the Oracle GUID for the visitor (may be null)
	 * @param eventDescription text to enter into the DESCRIPTION property for the appointment
	 * @return
	 * @throws IllegalArgumentException if any of the arguments (except the guids) are null, or if the data is not parsed properly by iCal4j
	 */
	public VEvent constructAvailableAppointment(final AvailableBlock block, final IScheduleOwner owner, final String ownerGuid, 
			final IScheduleVisitor visitor, final String visitorGuid, final String eventDescription) {
		Validate.notNull(block, "available block cannot be null");
		Validate.notNull(owner, "schedule owner cannot be null");
		Validate.notNull(visitor, "schedule visitor cannot be null");

		try {
			VEvent event = new VEvent();
			event.getProperties().add(new DtStart(new DateTime(DefaultEventUtilsImpl.convertToICalendarFormat(block.getStartTime()))));
			event.getProperties().add(new DtEnd(new DateTime(DefaultEventUtilsImpl.convertToICalendarFormat(block.getEndTime()))));
			Attendee visitorAttendee = constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR, visitorGuid);
			Attendee ownerAttendee = constructAvailableAttendee(owner.getCalendarAccount(), AppointmentRole.OWNER, ownerGuid);
			if(owner.isSamePerson(visitor)) {
				// only add the person to attendee list once with X-UW-AVAILABLE-APPOINTMENT-ROLE=BOTH
				Attendee singleAttendee = constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.BOTH, visitorGuid);
				event.getProperties().add(singleAttendee);
			} else {
				//Attendee visitorAttendee = constructAvailableAttendee(visitor.getCalendarAccount(), AppointmentRole.VISITOR, visitorGuid);
				event.getProperties().add(visitorAttendee);

				ICalendarAccount ownerCalendarAccount = owner.getCalendarAccount();
				if(ownerCalendarAccount instanceof OracleCalendarResourceAccount) {
					// owner is a resource, must add ORGANIZER in addition to attendee
					OracleCalendarResourceAccount resourceAccount = (OracleCalendarResourceAccount) ownerCalendarAccount;
					Organizer ownerOrganizer = constructOracleResourceOrganizer(resourceAccount);
					event.getProperties().add(ownerOrganizer);
					
					OracleResourceAttendee resourceAttendee = constructOracleResourceAttendee(resourceAccount);
					event.getProperties().add(resourceAttendee);
				} 
				// add the owner with X-UW-AVAILABLE-APPOINTMENT-ROLE=OWNER
				//Attendee ownerAttendee = constructAvailableAttendee(owner.getCalendarAccount(), AppointmentRole.OWNER, ownerGuid);
				event.getProperties().add(ownerAttendee);
			}
			// add the oracle "X-ORACLE-EVENTTYPE"
			XProperty appointmentType = new XProperty(ORACLE_EVENTTYPE, "APPOINTMENT");
			event.getProperties().add(appointmentType);

			// add custom UW-AVAILABLE-APPOINTMENT and UW-AVAILABLE-VERSION
			event.getProperties().add(SchedulingAssistantAppointment.TRUE);
			event.getProperties().add(AvailableVersion.AVAILABLE_VERSION_1_2);
			// add X-Uw-AVAILABLE-VISITORLIMIT
			event.getProperties().add(new VisitorLimit(block.getVisitorLimit()));

			StringBuilder title = new StringBuilder();
			title.append(owner.getPreference(Preferences.MEETING_PREFIX));
			
			// update title with visitor name and add description only if visitorLimit == 1
			if(block.getVisitorLimit() == 1) {
				title.append(" with ");
				title.append(visitor.getCalendarAccount().getDisplayName());
				
				// build event description
				StringBuilder descriptionBuilder = new StringBuilder();
				descriptionBuilder.append(eventDescription);
				// if the owner is an advisor
				if(getAffiliationSource().doesAccountHaveAffiliation(owner.getCalendarAccount(), AffiliationImpl.ADVISOR)) {
					// and the visitor is a student
					String studentId = visitor.getCalendarAccount().getAttributeValue("wiscedustudentid");
					if(null != studentId) {
						// append the UW Student ID to the event description
						descriptionBuilder.append(" [UW Student ID: ");
						descriptionBuilder.append(studentId);
						descriptionBuilder.append("]");
						
						// add the DARS url for the student in the advisor's personal notes
						StringBuilder personalNotesBuilder = new StringBuilder();
						personalNotesBuilder.append("DARS reports for ");
						personalNotesBuilder.append(visitor.getCalendarAccount().getDisplayName());
						personalNotesBuilder.append(": ");
						personalNotesBuilder.append(DARS_WEB_PREFIX);
						personalNotesBuilder.append(studentId);
						//String darsUrlString = DARS_WEB_PREFIX + studentId;
						String commentsValue = new String(Base64.encodeBase64(personalNotesBuilder.toString().getBytes()));
						XParameter ownerPersonalNotes = new XParameter(ORACLE_PERSONAL_COMMENTS, commentsValue);
						ownerAttendee.getParameters().add(ownerPersonalNotes);
					}
				}
				Description description = new Description(descriptionBuilder.toString());
				event.getProperties().add(description);
			} 
			
			// finally add meeting title
			event.getProperties().add(new Summary(title.toString()));
			// add class (normal)
			event.getProperties().add(Clazz.PRIVATE);
			event.getProperties().add(new XProperty("X-ORACLE-CLASS", "NORMAL"));

			// check if block overrides meeting location
			final String blockMeetingLocationOverride = block.getMeetingLocation();
			if(StringUtils.isNotBlank(blockMeetingLocationOverride)) {
				event.getProperties().add(new Location(blockMeetingLocationOverride));
			} else {
				// set owner's preferred location (if set)
				final String preferredLocation = owner.getPreferredLocation();
				if(StringUtils.isNotBlank(preferredLocation)) {
					event.getProperties().add(new Location(preferredLocation));
				}
			}
			// add CONFIRMED status
			event.getProperties().add(Status.VEVENT_CONFIRMED);
			
			return event;
		} catch (ParseException e) {
			throw new IllegalArgumentException("caught ParseException creating event", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.DefaultEventUtilsImpl#constructAvailableAttendee(org.jasig.schedassist.model.ICalendarAccount, org.jasig.schedassist.model.AppointmentRole)
	 */
	@Override
	public Attendee constructAvailableAttendee(ICalendarAccount calendarUser, AppointmentRole role) {
		return constructAvailableAttendee(calendarUser, role, null);
	}
	/**
	 * Call the super's method, and add the following:
	 * <ul>
	 * <li>{@link #ORACLE_SHOWASFREE} parameter set to "BUSY".</li>
	 * <li>If calendarUserGuid argument is not blank, add it as a {@link #ORACLE_GUID} parameter.</li>
	 * </ul>
	 * 
	 * @param calendarUser
	 * @param role
	 * @param calendarUserGuid
	 * @return an {@link Attendee} in the format needed for the Available framework
	 */
	public Attendee constructAvailableAttendee(ICalendarAccount calendarUser, AppointmentRole role, String calendarUserGuid) {
		Attendee attendee = super.constructAvailableAttendee(calendarUser, role);
		attendee.getParameters().add(new XParameter(ORACLE_SHOWASFREE, "BUSY"));
		if(StringUtils.isNotBlank(calendarUserGuid)) {
			attendee.getParameters().add(new XParameter(ORACLE_GUID, calendarUserGuid));
		}
		return attendee;
	}

	/**
	 * 
	 * @param resource
	 * @return
	 */
	protected OracleResourceAttendee constructOracleResourceAttendee(OracleCalendarResourceAccount resource) {
		Attendee attendee = this.constructAvailableAttendee(resource, AppointmentRole.OWNER, resource.getOracleGuid());
		OracleResourceAttendee result = new OracleResourceAttendee(attendee);
		return result;
	}
	/**
	 * Construct an {@link Organizer} property in the same fashion Oracle would for a resource account.
	 * @param resource
	 * @return
	 */
	protected Organizer constructOracleResourceOrganizer(OracleCalendarResourceAccount resource) {
		ParameterList parameters = new ParameterList();
		parameters.add(new XParameter(ORACLE_GUID, resource.getOracleGuid()));
		parameters.add(new Cn(resource.getResourceName()));
		parameters.add(AppointmentRole.OWNER);
		Organizer organizer = new Organizer(parameters, emailToURI(resource.getEmailAddress()));
		return organizer;
	}

	/**
	 * This method defines our criteria for which {@link VEvent}s will cause a conflict
	 * (either a red/busy block in the visible schedule or cause ConflictExistsExceptions).
	 * 
	 * This method will return false for the following criteria:
	 * <ol>
	 * <li>The event does not have an {@link #ORACLE_APPOINTMENT_PROPERTY}.</li>
	 * <li>The calendarAccount is listed as an Attendee for the event and either is marked with {@link PartStat#NEEDS_ACTION} OR {@link #ORACLE_SHOWASFREE}.</li>
	 * </ol>
	 * 
	 * @param calendarAccount
	 * @param event
	 * @return true if the specified {@link VEvent} will cause a conflict for the {@link ScheduleOwner}
	 */
	@Override
	public boolean willEventCauseConflict(ICalendarAccount calendarAccount, VEvent event) {
		Property oracleEventType = event.getProperty(ORACLE_EVENTTYPE);
		if(null == oracleEventType || !ORACLE_APPOINTMENT_PROPERTY.equals(oracleEventType)) {
			// non-appointment events never cause conflict
			return false;
		}

		// check to see if the owner is the only attendee and has marked self show as free
		Property ownerAttendee = getAttendeeForUserFromEvent(event, calendarAccount);
		if(ownerAttendee != null) {
			if(isOracleShowAsFree(ownerAttendee) || DefaultEventUtilsImpl.isPartStatNeedsAction(ownerAttendee)) {
				// owner is an attendee with either ParticipationStatus at NEEDS_ACTION or is Oracle SHOW AS FREE
				return false;
			} else {
				// owner is an attendee with a status that means conflict 
				return true;
			}
		} else {
			Property resourceAttendee = event.getProperty(OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE);
			if(null != resourceAttendee) {
				return attendeeMatchesPerson(resourceAttendee, calendarAccount);
			}
		}
		
		// owner is not attending, return false
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.DefaultEventUtilsImpl#getAttendeeListFromEvent(net.fortuna.ical4j.model.component.VEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public PropertyList getAttendeeListFromEvent(VEvent event) {
		PropertyList attendees = super.getAttendeeListFromEvent(event);
		PropertyList oracleResourceAttendees = event.getProperties(OracleResourceAttendee.ORACLE_RESOURCE_ATTENDEE);
		attendees.addAll(oracleResourceAttendees);
		return attendees;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.model.DefaultEventUtilsImpl#convertBlockToReflectionEvent(org.jasig.schedassist.model.AvailableBlock)
	 */
	@Override
	protected VEvent convertBlockToReflectionEvent(AvailableBlock block) {
		VEvent event = super.convertBlockToReflectionEvent(block);
		event.getProperties().add(ORACLE_DAILY_NOTE_PROPERTY);
		event.getProperties().add(new XProperty("X-ORACLE-CLASS", "NORMAL"));
		return event;
	}

	/**
	 * Look for the {@link #ORACLE_SHOWASFREE}. experimental parameter ({@link XParameter}) on the 
	 * {@link Attendee} argument.
	 * 
	 * If present AND the value of that parameter is "FREE", return true.
	 * 
	 * @param attendee
	 * @return
	 */
	public static boolean isOracleShowAsFree(final Property attendee) {
		Validate.notNull(attendee);
		Parameter p = attendee.getParameter(ORACLE_SHOWASFREE);
		if(null != p && "FREE".equals(p.getValue())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param blocks
	 * @return
	 */
	public static net.fortuna.ical4j.model.Calendar convertBlocksToDayEvents(SortedSet<AvailableBlock> blocks) {
		ComponentList components = new ComponentList();
		SortedSet<AvailableBlock> combined = AvailableBlockBuilder.combine(blocks);
		for(AvailableBlock block : combined) {
			VEvent dayEvent = blockToDayEvent(block);
			components.add(dayEvent);
		}
		net.fortuna.ical4j.model.Calendar result = new net.fortuna.ical4j.model.Calendar(components);
		return result;
	}
	
	/**
	 * Convert an {@link AvailableBlock} into an all day event.
	 * The {@link Summary} of the returned {@link VEvent} will match the format:
	 <pre>
	 Available 9:00 AM - 3:00 PM
	 </pre>
	 * The returned {@link VEvent} will also have the Oracle Day Event XProperty
	 * 
	 * @see #ORACLE_DAILY_NOTE_PROPERTY
	 * @param block
	 * @return
	 */
	public static VEvent blockToDayEvent(final AvailableBlock block) {
		Date blockDate = DateUtils.truncate(block.getStartTime(), Calendar.DATE);
		StringBuilder eventSummary = new StringBuilder();
		eventSummary.append("Available ");
		SimpleDateFormat d = new SimpleDateFormat("h:mm a");
		eventSummary.append(d.format(block.getStartTime()));
		eventSummary.append(" - ");
		eventSummary.append(d.format(block.getEndTime()));
		VEvent result = new VEvent(new net.fortuna.ical4j.model.Date(blockDate), 
				new Dur(1, 0, 0, 0), 
				eventSummary.toString());
		result.getProperties().add(OracleEventUtilsImpl.ORACLE_DAILY_NOTE_PROPERTY);
		return result;
	}
}
