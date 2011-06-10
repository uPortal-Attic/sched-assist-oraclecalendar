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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import oracle.calendar.sdk.Api;
import oracle.calendar.sdk.Api.StatusException;




/**
 * The purpose of this class is to facilitate the use of the Oracle
 * Calendar SDK in multiple web applications in one instance of Tomcat.
 * 
 * The requirements for using the Oracle CSDK is to load the correct 
 * native library for the platform using System.loadLibrary.
 * 
 * The Java Native Interface (JNI) requires that System.loadLibrary be called
 * once and only once for any particular native library.
 * 
 * In order to meet these requirements, this class is compiled and deployed
 * in the shared classloader for Tomcat (CATALINA_HOME/shared/lib).
 * 
 * Classes that need to use this library should subclass this class.
 * This class should never be instantiated itself (hence it is marked abstract).
 * 
 * If the value of the {@link System} property "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.useOracleApiInit"
 * evaluates to true, then the static initializer will call {@link Api#init(String, String)}, using the values of
 * the "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkConfigFile" and "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkLogFile"
 * as the arguments. If neither are specified, default values will be chosen (csdk-default.ini included in distribution, 
 * SystemUtils.getJavaIoTmpDir() for the location of the log file).
 * 
 * If the value of the {@link System} property "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.useOracleApiInit"
 * evaluates to false, {@link System#loadLibrary(String)} will be used to load the Oracle CSDK and Oracle's 
 *  {@link Api#init(String, String)} WILL NOT be called.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleCalendarSDKSupport.java 2246 2010-06-30 18:57:24Z npblair $
 */
public abstract class OracleCalendarSDKSupport {

	static {
		final String useOracleApiInitProperty = "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.useOracleApiInit";
		final String useOracleApiInitValue = System.getProperty(useOracleApiInitProperty, "true");
		
		if(!Boolean.parseBoolean(useOracleApiInitValue)) {
			System.out.println(new Date() + " - OracleCalendarSDKSupport - Using direct System.loadLibrary call to initialize CSDK");
			System.loadLibrary("csdkjni");
			System.out.println(new Date() + " - OracleCalendarSDKSupport - Direct System.loadLibrary call to initialize CSDK completed successfully");
		} else {
			System.out.println(new Date() + " - OracleCalendarSDKSupport - Using Oracle Api.init(String, String) to initialize CSDK");
			final String configProperty = "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkConfigFile";
			final String logProperty = "org.jasig.schedassist.oraclecalendar.OracleCalendarSDKSupport.csdkLogFile";

			boolean usingDefault = false;
			String csdkConfigFilePath = System.getProperty(configProperty);
			if(CommonsUtils.isBlank(csdkConfigFilePath) ) {
				csdkConfigFilePath = "csdk-default.ini";
				usingDefault = true;
			}

			File javaTmpDir = new File(System.getProperty("java.io.tmpdir"));
			
			final String resolvedConfigPath;
			// 1st attempt: treat as absolute path
			File csdkConfigFile = new File (csdkConfigFilePath);
			if(csdkConfigFile.exists() && csdkConfigFile.canRead()) {
				resolvedConfigPath = csdkConfigFile.getAbsolutePath();
			} else {
				// 2nd attempt: load from classpath
				ClassLoader ourClassLoader = Thread.currentThread().getContextClassLoader();
				URL configFileUrl = ourClassLoader.getResource(csdkConfigFilePath);
				if(null == configFileUrl) {
					throw new OracleCalendarSDKUnavailableError("The value for System property '" + configProperty + "' (" + csdkConfigFilePath + ") does not exist.");
				}
				if("jar".equals(configFileUrl.getProtocol())) {
					// need to extract the file and write out to java.io.tmpdir
					String fileBaseName = CommonsUtils.getName(csdkConfigFilePath);
					
					File outputFile = new File(javaTmpDir.getAbsolutePath(), fileBaseName);
					outputFile.deleteOnExit();
					try {
						CommonsUtils.copyURLToFile(configFileUrl, outputFile);
						resolvedConfigPath = outputFile.getAbsolutePath();
						System.out.println(new Date() + " - Your value for System property '" + configProperty + "' (" + csdkConfigFilePath + ") has been written to a temporary file: " + resolvedConfigPath);
					} catch (IOException e) {
						throw new OracleCalendarSDKUnavailableError("An IOException occurred extracting your value for '" + configProperty + "' (" + csdkConfigFilePath + ")", e);
					}
				} else if("file".equals(configFileUrl.getProtocol())) {
					resolvedConfigPath = configFileUrl.getPath();
				} else {
					throw new OracleCalendarSDKUnavailableError("Unsupported protocol for '" + configProperty + "' (" + csdkConfigFilePath + ").");
				}
				csdkConfigFile = new File(resolvedConfigPath);
				if(!csdkConfigFile.exists() || !csdkConfigFile.canRead()) {
					throw new OracleCalendarSDKUnavailableError("The value for System property '" + configProperty + "' (" + csdkConfigFilePath + ") does not exist or cannot be read.");
				}
			}

			// configPath successfully loaded
			if(usingDefault) {
				System.out.println(new Date() + " - OracleCalendarSDKSupport - using default CSDK configuration");
			} else {
				System.out.println(new Date() + " - OracleCalendarSDKSupport - found CSDK configuration file: " + resolvedConfigPath);
			}

			final String defaultLog = javaTmpDir.getAbsolutePath() + "/csdk.log";
			String csdkLogFilePath = System.getProperty(logProperty, defaultLog);
			if(CommonsUtils.isBlank(csdkLogFilePath) ) {
				throw new OracleCalendarSDKUnavailableError("System property '" + logProperty + "' not set");
			}
			File csdkLogFile = new File (csdkLogFilePath);
			try {
				CommonsUtils.touch(csdkLogFile);
			} catch (IOException e) {
				throw new OracleCalendarSDKUnavailableError("The value for System property '" + logProperty + "' (" + csdkLogFilePath + ") cannot be written.");
			}
			System.out.println(new Date() + " - OracleCalendarSDKSupport - Oracle CSDK log file: " + csdkLogFilePath);

			try {
				System.out.println(new Date() + " - OracleCalendarSDKSupport - calling Api#init for Oracle CSDK, configPath: " + resolvedConfigPath);
				Api.init(resolvedConfigPath, csdkLogFilePath);
				System.out.println(new Date() + " - OracleCalendarSDKSupport - Api#init for Oracle CSDK complete");
			} catch (StatusException e) {
				System.err.println(new Date() + " - caught StatusException from Api.init");
				e.printStackTrace(System.err);
				throw new OracleCalendarSDKUnavailableError(e);
			}
		}
	}
}
