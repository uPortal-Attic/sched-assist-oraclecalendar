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


package org.jasig.schedassist.web.admin;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.pool.KeyedObjectPool;
import org.jasig.schedassist.oraclecalendar.OracleCalendarServerNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Administrative {@link Controller} for interacting with 
 * the Oracle Session {@link KeyedObjectPool}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleSessionPoolAdminController.java $
 */
@Controller
@RequestMapping("/admin/oracle-session-pool.html")
public class OracleSessionPoolAdminController {

	private KeyedObjectPool oracleSessionPool;
	private Map<String, OracleCalendarServerNode> serverNodes = new HashMap<String, OracleCalendarServerNode>();

	/**
	 * @param oracleSessionPool the oracleSessionPool to set
	 */
	@Autowired
	public void setOracleSessionPool(KeyedObjectPool oracleSessionPool) {
		this.oracleSessionPool = oracleSessionPool;
	}
	/**
	 * @param serverNodes the serverNodes to set
	 */
	@Resource(name="oracleCalendarNodeMap")
	public void setServerNodes(Map<String, OracleCalendarServerNode> serverNodes) {
		this.serverNodes = serverNodes;
	}


	/**
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping
	public String showPoolStatus(ModelMap model) {
		model.addAttribute("numActive", oracleSessionPool.getNumActive());
		model.addAttribute("numIdle", oracleSessionPool.getNumIdle());
		return "admin/oracle-session-pool-status";
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method=RequestMethod.POST, params="action=clear")
	public String clearPool() throws Exception {
		this.oracleSessionPool.clear();
		return "admin/oracle-session-pool-clear-complete";
	}
	
	/**
	 * 
	 * @param nodeId
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method=RequestMethod.POST, params="action=clearNode")
	public String clearSessionsForSpecificNode(@RequestParam String nodeId, ModelMap model) throws Exception {
		model.put("nodeId", nodeId);
		OracleCalendarServerNode node = serverNodes.get(nodeId);
		if(node != null) {
			this.oracleSessionPool.clear(node);
			model.addAttribute("success", true);
		} 
		
		return "admin/oracle-session-pool-clearNode-complete";
	}
}
