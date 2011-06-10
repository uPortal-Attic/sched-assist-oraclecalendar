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

import java.net.URISyntaxException;

import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.XProperty;

/**
 * Oracle does not store an {@link Attendee} property for resource accounts
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleResourceAttendee.java 2008 2010-04-26 15:18:09Z npblair $
 */
public final class OracleResourceAttendee extends XProperty {

	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;
	public static final String ORACLE_RESOURCE_ATTENDEE = "X-UW-AVAILABLE-RESOURCE-ATTENDEE";
	
	/**
	 * Construct an {@link XProperty} with the name {@link #ORACLE_RESOURCE_ATTENDEE}.
	 * @param attendee
	 */
	public OracleResourceAttendee(final Attendee attendee) {
		super(ORACLE_RESOURCE_ATTENDEE, attendee.getParameters(), attendee.getValue());
	}
	
	/**
	 * 
	 * @return an {@link Attendee} from the original parameterList and value
	 * @throws IllegalStateException if the original {@link Attendee} parameterList and value cannot be used to construct another
	 */
	public Attendee getAttendee() {
		Attendee attendee;
		try {
			attendee = new Attendee(this.getParameters(), this.getValue());
			return attendee;
		} catch (URISyntaxException e) {
			throw new IllegalStateException("failed to construct attendee after starting with valid values, value: " + this.getValue() + ", parameterList" + this.getParameters(), e);
		}
		
	}

}
