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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Bean that represents a single node on an Oracle Calendar server.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarServerNode.java 410 2008-10-13 21:19:53Z npblair $
 */
public class OracleCalendarServerNode {

	private String serverAddress;
	private String nodeName;
	private String sysopPassword;
	
	/**
	 * @return the serverAddress
	 */
	public String getServerAddress() {
		return serverAddress;
	}
	/**
	 * @param serverAddress the serverAddress to set
	 */
	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}
	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	/**
	 * @return the sysopPassword
	 */
	public String getSysopPassword() {
		return sysopPassword;
	}
	/**
	 * @param sysopPassword the sysopPassword to set
	 */
	public void setSysopPassword(String sysopPassword) {
		this.sysopPassword = sysopPassword;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("serverAddress", this.serverAddress)
			.append("nodeName", this.nodeName)
			.append("sysopPassword", "not displayed")
			.toString();
	}
	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(Object object) {
		if (!(object instanceof OracleCalendarServerNode)) {
			return false;
		}
		OracleCalendarServerNode rhs = (OracleCalendarServerNode) object;
		return new EqualsBuilder().append(this.serverAddress, rhs.serverAddress)
			.append(this.nodeName, rhs.nodeName)
			.append(this.sysopPassword, rhs.sysopPassword)
			.isEquals();
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(-442487307, -1727488997)
			.append(this.serverAddress)
			.append(this.nodeName)
			.append(this.sysopPassword)
			.toHashCode();
	}

	
	
}
