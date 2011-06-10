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

/**
 * Thrown when parsing the resulting iCalendar from Oracle fails (usually
 * wraps an iCal4j {@link net.fortuna.ical4j.data.ParserException}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarParserException.java 1909 2010-04-14 21:14:07Z npblair $
 */
public class OracleCalendarParserException extends RuntimeException {

	private static final long serialVersionUID = 53706L;
	
	private String agenda;
	
	/**
	 * 
	 */
	public OracleCalendarParserException() {
	}

	/**
	 * @param message
	 */
	public OracleCalendarParserException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OracleCalendarParserException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OracleCalendarParserException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 
	 * @param message
	 * @param agenda
	 * @param cause
	 */
	public OracleCalendarParserException(String message, String agenda, Throwable cause) {
		super(message, cause);
		this.agenda = agenda;
	}

	/**
	 * @return the agenda
	 */
	public String getAgenda() {
		return agenda;
	}

	/**
	 * @param agenda the agenda to set
	 */
	public void setAgenda(String agenda) {
		this.agenda = agenda;
	}
	
}
