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

/**
 * {@link Error} that may be raised via the static
 * initializer in {@link OracleCalendarSDKSupport} if
 * necessary configuration parameters are missing
 * or could not be found.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarSDKUnavailableError.java 1818 2010-03-03 18:26:25Z npblair $
 */
public class OracleCalendarSDKUnavailableError extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = 53706L;

	/**
	 * 
	 */
	public OracleCalendarSDKUnavailableError() {
	}

	/**
	 * @param message
	 */
	public OracleCalendarSDKUnavailableError(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public OracleCalendarSDKUnavailableError(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OracleCalendarSDKUnavailableError(String message, Throwable cause) {
		super(message, cause);
	}

}
