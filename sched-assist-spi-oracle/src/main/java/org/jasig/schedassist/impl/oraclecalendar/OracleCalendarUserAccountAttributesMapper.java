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

import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

/**
 * {@link AttributesMapper} implementation.
 * 
 * mapFromAttributes returns an {@link CalendarUser} object.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarUserAccountAttributesMapper.java 2767 2010-10-05 19:10:25Z npblair $
 */
public final class OracleCalendarUserAccountAttributesMapper implements AttributesMapper {

	protected static final String USERNAME_ATTRIBUTE = "uid";
	protected static final String CALENDAR_UNIQUEID_ATTRIBUTE = AbstractOracleCalendarAccount.CTCALXITEMID;
	protected static final String EMAIL_ATTRIBUTE = AbstractOracleCalendarAccount.WISCEDUCALEMAIL;
	protected static final String DISPLAYNAME_ATTRIBUTE = "displayname";
	protected static final String GIVENNAME_ATTRIBUTE = "givenname";
	protected static final String SURNAME_ATTRIBUTE = "sn";
	
	private final OracleGUIDSource oracleGUIDSource;
	/**
	 * @param oracleGUIDSource
	 */
	public OracleCalendarUserAccountAttributesMapper(
			OracleGUIDSource oracleGUIDSource) {
		this.oracleGUIDSource = oracleGUIDSource;
	}

	/* (non-Javadoc)
	 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
	 */
	public Object mapFromAttributes(Attributes attributes) throws NamingException {
		OracleCalendarUserAccount user = new OracleCalendarUserAccount();	
		NamingEnumeration<String> attributeNames = attributes.getIDs();
		Map<String, String> attributesMap = new HashMap<String, String>();
		while(attributeNames.hasMore()) {
			String attributeName = attributeNames.next();
			Attribute attribute = attributes.get(attributeName);
			String value = (String) attribute.get();
			if(null != value) {
				value = value.trim();
			}
			final String lcAttributeName = attributeName.toLowerCase();
			attributesMap.put(lcAttributeName, value);
			
			
			if(USERNAME_ATTRIBUTE.equals(lcAttributeName)) {
				user.setUsername(value);
			} else if (CALENDAR_UNIQUEID_ATTRIBUTE.equals(lcAttributeName)) {
				user.setCtcalxitemid(value);
			} else if (EMAIL_ATTRIBUTE.equals(lcAttributeName)) {
				user.setEmailAddress(value);
			} else if (DISPLAYNAME_ATTRIBUTE.equals(lcAttributeName)) {
				user.setDisplayName(value);
			} else if (GIVENNAME_ATTRIBUTE.equals(lcAttributeName)) {
				user.setGivenName(value);
			} else if(SURNAME_ATTRIBUTE.equals(lcAttributeName)) {
				user.setSurname(value);
			}
		}
		user.setAttributes(attributesMap);
		
		if(user.getCalendarUniqueId() != null) {
			String oracleGuid = this.oracleGUIDSource.getOracleGUID(user);
			user.setOracleGuid(oracleGuid);
			user.getAttributes().put(AbstractOracleCalendarAccount.ORACLE_GUID_ATTRIBUTE, oracleGuid);
		}
		return user;
	}

}
