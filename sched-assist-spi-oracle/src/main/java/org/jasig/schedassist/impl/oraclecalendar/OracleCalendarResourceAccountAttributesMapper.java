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

import org.jasig.schedassist.model.ICalendarAccount;
import org.springframework.ldap.core.AttributesMapper;

/**
 * {@link AttributesMapper} for {@link OracleCalendarResourceAccount}.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarResourceAccountAttributesMapper.java 2767 2010-10-05 19:10:25Z npblair $
 */
public final class OracleCalendarResourceAccountAttributesMapper implements AttributesMapper {
	
	protected static final String RESOURCE_OWNER_USERNAME = "wisceducalresourceownerid";
	private final OracleGUIDSource oracleGUIDSource;
	private final ICalendarAccount owner;
	/**
	 * 
	 */
	public OracleCalendarResourceAccountAttributesMapper(OracleGUIDSource oracleGUIDSource) {
		this.oracleGUIDSource = oracleGUIDSource;
		this.owner = null;
	}
	/**
	 * @param owner
	 */
	public OracleCalendarResourceAccountAttributesMapper(OracleGUIDSource oracleGUIDSource,
			ICalendarAccount owner) {
		this.oracleGUIDSource = oracleGUIDSource;
		this.owner = owner;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ldap.core.AttributesMapper#mapFromAttributes(javax.naming.directory.Attributes)
	 */
	@Override
	public Object mapFromAttributes(Attributes attributes)
		throws NamingException {
		OracleCalendarResourceAccount user = new OracleCalendarResourceAccount(owner);
		
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
			
			if(RESOURCE_OWNER_USERNAME.equals(lcAttributeName)) {
				user.setAccountOwnerUsername(value);
			} else if (AbstractOracleCalendarAccount.CTCALXITEMID.equals(lcAttributeName)) {
				user.setCtcalxitemid(value);
			} else if (OracleLdapCalendarResourceAccountDaoImpl.CN.equals(lcAttributeName)) {
				user.setResourceName(value);
			} else if (OracleLdapCalendarResourceAccountDaoImpl.POSTALADDRESS.equals(lcAttributeName)) {
				user.setLocation(value);
			} else if (AbstractOracleCalendarAccount.WISCEDUCALEMAIL.equals(lcAttributeName)) {
				user.setEmailAddress(value);
			}
			
		}
		user.setAttributes(attributesMap);
			
		String contactInfo = buildContactInformation(getAttributeValue(attributes, OracleLdapCalendarResourceAccountDaoImpl.GIVENNAME), 
				getAttributeValue(attributes, OracleLdapCalendarResourceAccountDaoImpl.SN), 
				getAttributeValue(attributes, OracleLdapCalendarResourceAccountDaoImpl.TELEPHONENUMBER));
		user.setContactInformation(contactInfo);
		
		
		String oracleGuid = this.oracleGUIDSource.getOracleGUID(user);
		user.setOracleGuid(oracleGuid);
		user.getAttributes().put(AbstractOracleCalendarAccount.ORACLE_GUID_ATTRIBUTE, oracleGuid);
		return user;
	}
	
	/**
	 * Get the specified attribute, or null.
	 * If the attribute is not empty, it's value is {@link String#trim()}'d.
	 * 
	 * @param attributes
	 * @param attributeName
	 * @return
	 * @throws NamingException 
	 */
	String getAttributeValue(Attributes attributes, String attributeName) throws NamingException  {
		Attribute attribute = attributes.get(attributeName);
		if(null != attribute) {
			String value = (String) attribute.get();
			if(null != value) {
				value = value.trim();
			}
			return value;
		}
		return null;
	}
	
	/**
	 * 
	 * @param givenName
	 * @param sn
	 * @param telephone
	 * @return
	 */
	String buildContactInformation(String givenName, String sn, String telephone) {
		StringBuilder contactInfo = new StringBuilder();
		contactInfo.append(givenName);
		contactInfo.append(" ");
		contactInfo.append(sn);
		contactInfo.append(", ");
		contactInfo.append(telephone);
		return contactInfo.toString();
	}
}