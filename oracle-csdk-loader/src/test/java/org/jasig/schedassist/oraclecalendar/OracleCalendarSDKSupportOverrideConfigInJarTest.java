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

import java.net.URL;
import java.net.URLClassLoader;

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Session;

import org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Complex test case for {@link OracleCalendarSDKSupport}.
 * The testOverrideConfig method makes a new subclass of {@link OracleCalendarSDKSupport}
 * that has no implementation. This simply gets the class loaded, and as a result
 * it's static initializer gets run.
 * 
 * Once the initializer has been run, it attempts to use some of the Oracle Calendar
 * SDK Apis that require no server configuration; specifically calls to Session.getCapabilities.
 * 
 * Once the CSDK has been loaded and the initial tests have passed, a separate {@link ClassLoader} 
 * is created to try and replicate the problem calling System.loadLibrary from two isolated 
 * {@link ClassLoaders} in a servlet container. This part of the test asserts that an 
 * {@link UnsatisfiedLinkError} is thrown by the second call to System.loadLibrary.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarSDKSupportOverrideConfigInJarTest.java 1825 2010-03-04 16:57:46Z npblair $
 */
public class OracleCalendarSDKSupportOverrideConfigInJarTest {

	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testOverrideConfig() throws Exception {
		// csdk-test-injar.ini is located in src/test/resources/test-csdk-config-injar.jar
		System.setProperty("org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkConfigFile", "csdk-test-injar.ini");
		// instantiate anonymous inner subclass of OracleCalendarSDKSupport - gets static initializer invoked
		new OracleCalendarSDKSupport() {};
		
		Session session = new Session();
		String version = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_CSDK_VERSION);
		String capabVersion = session.getCapabilities(Api.CSDK_FLAG_NONE, Api.CSDK_CAPAB_VERSION);
		Assert.assertNotNull(version);
		Assert.assertNotNull(capabVersion);
		
		System.out.println("CSDK version: " + version);
		System.out.println("CSDK capab version:" + capabVersion);
		
		// csdkjni has been successfully loaded, 
		//now try to instantiate another class that calls System.loadLibrary("csdkjni")
		// with a different classloader
		URL jarUrl = this.getClass().getResource("/test-csdkjni-loader.jar");
		URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl  });
		Assert.assertNotSame(classLoader, ClassLoader.getSystemClassLoader());
		
		try {
			Class<?> clazz = classLoader.loadClass("DuplicateCSDKLoader");
			clazz.newInstance();
			Assert.fail("UnsatisfiedLinkError not thrown");
		} catch (UnsatisfiedLinkError e) {
			// success
		}
		
	}
	
}
