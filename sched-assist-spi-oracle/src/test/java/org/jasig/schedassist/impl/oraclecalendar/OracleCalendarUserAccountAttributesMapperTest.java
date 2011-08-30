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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.easymock.EasyMock;
import org.jasig.schedassist.model.ICalendarAccount;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarUserAccountAttributesMapperTest.java 2002 2010-04-23 17:33:38Z npblair $
 */
public class OracleCalendarUserAccountAttributesMapperTest {

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testControl() throws Exception {
		OracleGUIDSource mockGUIDSource = EasyMock.createMock(OracleGUIDSource.class);
		EasyMock.expect(mockGUIDSource.getOracleGUID(EasyMock.isA(ICalendarAccount.class))).andReturn("0123456789");
		EasyMock.replay(mockGUIDSource);
		
		NamingEnumeration<String> mockAttributeIds = EasyMock.createMock(NamingEnumeration.class);
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("uid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("ctcalxitemid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceducalemail");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("givenName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("sn");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("displayName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(false);
		EasyMock.replay(mockAttributeIds);
		
		Attributes mockAttributes = EasyMock.createMock(Attributes.class);
		EasyMock.expect(mockAttributes.getIDs()).andReturn(mockAttributeIds);
		EasyMock.expect(mockAttributes.get("uid")).andReturn(mockAttribute("username"));
		EasyMock.expect(mockAttributes.get("ctcalxitemid")).andReturn(mockAttribute("20000:12345"));
		EasyMock.expect(mockAttributes.get("wisceducalemail")).andReturn(mockAttribute("email@wisc.edu"));
		EasyMock.expect(mockAttributes.get("givenName")).andReturn(mockAttribute("FIRST"));
		EasyMock.expect(mockAttributes.get("sn")).andReturn(mockAttribute("LAST"));
		EasyMock.expect(mockAttributes.get("displayName")).andReturn(mockAttribute("FIRST M LAST"));
		EasyMock.replay(mockAttributes);
		
		OracleCalendarUserAccountAttributesMapper mapper = new OracleCalendarUserAccountAttributesMapper(mockGUIDSource);
		OracleCalendarUserAccount user = (OracleCalendarUserAccount) mapper.mapFromAttributes(mockAttributes);
		
		Assert.assertEquals("username", user.getUsername());
		Assert.assertEquals("20000:12345", user.getCalendarUniqueId());
		Assert.assertEquals("email@wisc.edu", user.getEmailAddress());
		Assert.assertEquals("FIRST LAST", user.getDisplayName());
		Assert.assertEquals("FIRST LAST", user.getGivenSurname());
		Assert.assertEquals("FIRST", user.getGivenName());
		Assert.assertEquals("LAST", user.getSurname());
		Assert.assertEquals("0123456789", user.getOracleGuid());
		
		EasyMock.verify(mockAttributeIds, mockAttributes, mockGUIDSource);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testAdvisor() throws Exception {
		OracleGUIDSource mockGUIDSource = EasyMock.createMock(OracleGUIDSource.class);
		EasyMock.expect(mockGUIDSource.getOracleGUID(EasyMock.isA(ICalendarAccount.class))).andReturn("0123456789");
		EasyMock.replay(mockGUIDSource);
		
		NamingEnumeration<String> mockAttributeIds = EasyMock.createMock(NamingEnumeration.class);
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("uid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("ctcalxitemid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceducalemail");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("givenName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("sn");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("displayName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceduadvisorflag");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceduisisadvisoremplid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(false);
		EasyMock.replay(mockAttributeIds);
		
		Attributes mockAttributes = EasyMock.createMock(Attributes.class);
		EasyMock.expect(mockAttributes.getIDs()).andReturn(mockAttributeIds);
		EasyMock.expect(mockAttributes.get("uid")).andReturn(mockAttribute("username"));
		EasyMock.expect(mockAttributes.get("ctcalxitemid")).andReturn(mockAttribute("20000:12345"));
		EasyMock.expect(mockAttributes.get("wisceducalemail")).andReturn(mockAttribute("email@wisc.edu"));
		EasyMock.expect(mockAttributes.get("givenName")).andReturn(mockAttribute("FIRST"));
		EasyMock.expect(mockAttributes.get("sn")).andReturn(mockAttribute("LAST"));
		EasyMock.expect(mockAttributes.get("displayName")).andReturn(mockAttribute("FIRST M LAST"));
		EasyMock.expect(mockAttributes.get("wisceduadvisorflag")).andReturn(mockAttribute("Y"));
		EasyMock.expect(mockAttributes.get("wisceduisisadvisoremplid")).andReturn(mockAttribute("01234567"));
		EasyMock.replay(mockAttributes);
		
		OracleCalendarUserAccountAttributesMapper mapper = new OracleCalendarUserAccountAttributesMapper(mockGUIDSource);
		OracleCalendarUserAccount user = (OracleCalendarUserAccount) mapper.mapFromAttributes(mockAttributes);
		
		Assert.assertEquals("username", user.getUsername());
		Assert.assertEquals("20000:12345", user.getCalendarUniqueId());
		Assert.assertEquals("email@wisc.edu", user.getEmailAddress());
		Assert.assertEquals("FIRST LAST", user.getDisplayName());
		Assert.assertEquals("01234567", user.getAttributes().get("wisceduisisadvisoremplid"));
		Assert.assertEquals("FIRST LAST", user.getGivenSurname());
		Assert.assertEquals("FIRST", user.getGivenName());
		Assert.assertEquals("LAST", user.getSurname());
		Assert.assertEquals("0123456789", user.getOracleGuid());
		
		EasyMock.verify(mockAttributeIds, mockAttributes, mockGUIDSource);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testStudent() throws Exception {
		OracleGUIDSource mockGUIDSource = EasyMock.createMock(OracleGUIDSource.class);
		EasyMock.expect(mockGUIDSource.getOracleGUID(EasyMock.isA(ICalendarAccount.class))).andReturn("0123456789");
		EasyMock.replay(mockGUIDSource);
		
		NamingEnumeration<String> mockAttributeIds = EasyMock.createMock(NamingEnumeration.class);
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("uid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("ctcalxitemid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceducalemail");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("givenName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("sn");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("displayName");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(true);
		EasyMock.expect(mockAttributeIds.next()).andReturn("wisceduisisstudentemplid");
		EasyMock.expect(mockAttributeIds.hasMore()).andReturn(false);
		EasyMock.replay(mockAttributeIds);
		
		Attributes mockAttributes = EasyMock.createMock(Attributes.class);
		EasyMock.expect(mockAttributes.getIDs()).andReturn(mockAttributeIds);
		EasyMock.expect(mockAttributes.get("uid")).andReturn(mockAttribute("username"));
		EasyMock.expect(mockAttributes.get("ctcalxitemid")).andReturn(mockAttribute("20000:12345"));
		EasyMock.expect(mockAttributes.get("wisceducalemail")).andReturn(mockAttribute("email@wisc.edu"));
		EasyMock.expect(mockAttributes.get("givenName")).andReturn(mockAttribute("FIRST"));
		EasyMock.expect(mockAttributes.get("sn")).andReturn(mockAttribute("LAST"));
		EasyMock.expect(mockAttributes.get("displayName")).andReturn(mockAttribute("FIRST M LAST"));
		EasyMock.expect(mockAttributes.get("wisceduisisstudentemplid")).andReturn(mockAttribute("01234567"));
		EasyMock.replay(mockAttributes);
		
		OracleCalendarUserAccountAttributesMapper mapper = new OracleCalendarUserAccountAttributesMapper(mockGUIDSource);
		OracleCalendarUserAccount user = (OracleCalendarUserAccount) mapper.mapFromAttributes(mockAttributes);
		
		Assert.assertEquals("username", user.getUsername());
		Assert.assertEquals("20000:12345", user.getCalendarUniqueId());
		Assert.assertEquals("email@wisc.edu", user.getEmailAddress());
		Assert.assertEquals("FIRST LAST", user.getDisplayName());
		Assert.assertEquals("FIRST LAST", user.getGivenSurname());
		Assert.assertEquals("FIRST", user.getGivenName());
		Assert.assertEquals("LAST", user.getSurname());
		Assert.assertEquals("01234567", user.getAttributes().get("wisceduisisstudentemplid"));
		Assert.assertEquals("0123456789", user.getOracleGuid());
		
		EasyMock.verify(mockAttributeIds, mockAttributes, mockGUIDSource);
	}
	
	/**
	 * 
	 * @param attributeValue
	 * @return
	 * @throws NamingException
	 */
	private Attribute mockAttribute(final String attributeValue) throws NamingException {
		Attribute attribute = EasyMock.createMock(Attribute.class);
		EasyMock.expect(attribute.get()).andReturn(attributeValue);
		EasyMock.replay(attribute);
		return attribute;
	}
}
