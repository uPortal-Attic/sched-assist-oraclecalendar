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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.schedassist.IDelegateCalendarAccountDao;
import org.jasig.schedassist.model.ICalendarAccount;
import org.jasig.schedassist.model.IDelegateCalendarAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.TimeLimitExceededException;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;

import com.googlecode.ehcache.annotations.Cacheable;

/**
 * {@link IDelegateCalendarAccountDao} implementation that returns
 * {@link OracleCalendarResourceAccount}s.
 *  
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleLdapCalendarResourceAccountDaoImpl.java 2600 2010-09-16 17:42:57Z npblair $
 */
public class OracleLdapCalendarResourceAccountDaoImpl implements
IDelegateCalendarAccountDao {

	private Log LOG = LogFactory.getLog(this.getClass());

	static final String CN = "cn";

	protected static final String POSTALADDRESS = "postaladdress";
	protected static final String CTCALRESOURCECAPACITY = "ctcalresourcecapacity";
	protected static final String GIVENNAME = "givenname";
	protected static final String SN = "sn";
	protected static final String TELEPHONENUMBER = "telephonenumber";
	private static final String WILDCARD = "*";

	private LdapOperations ldapTemplate;
	private String baseDn = "o=isp";
	private OracleGUIDSource oracleGUIDSource;

	private long searchResultsLimit = 25L;
	private int searchTimeLimit = 5000;

	/**
	 * @param ldapTemplate the {@link LdapOperations} to set
	 */
	@Autowired
	public void setLdapTemplate(LdapOperations ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	/**
	 * @param oracleGUIDSource the oracleGUIDSource to set
	 */
	@Autowired
	public void setOracleGUIDSource(OracleGUIDSource oracleGUIDSource) {
		this.oracleGUIDSource = oracleGUIDSource;
	}

	/**
	 * @param baseDn the baseDn to set
	 */
	public void setBaseDn(String baseDn) {
		this.baseDn = baseDn;
	}
	/**
	 * @param searchResultsLimit the searchResultsLimit to set
	 */
	public void setSearchResultsLimit(long searchResultsLimit) {
		this.searchResultsLimit = searchResultsLimit;
	}
	/**
	 * @param searchTimeLimit the searchTimeLimit to set
	 */
	public void setSearchTimeLimit(int searchTimeLimit) {
		this.searchTimeLimit = searchTimeLimit;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#getDelegate(java.lang.String)
	 */
	@Cacheable(cacheName="delegateAccountCache")
	@Override
	public IDelegateCalendarAccount getDelegate(String accountName) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(CN, accountName));
		searchFilter.and(new LikeFilter(AbstractOracleCalendarAccount.CTCALXITEMID, WILDCARD));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter, null);
		IDelegateCalendarAccount resource = (IDelegateCalendarAccount) DataAccessUtils.singleResult(results);
		return resource;
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#getDelegate(java.lang.String, org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Cacheable(cacheName="delegateAccountCache")
	@Override
	public IDelegateCalendarAccount getDelegate(String accountName,
			ICalendarAccount owner) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(CN, accountName));
		searchFilter.and(new EqualsFilter(OracleCalendarResourceAccountAttributesMapper.RESOURCE_OWNER_USERNAME, owner.getUsername()));
		searchFilter.and(new LikeFilter(AbstractOracleCalendarAccount.CTCALXITEMID, WILDCARD));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter, owner);
		IDelegateCalendarAccount resource = (IDelegateCalendarAccount) DataAccessUtils.singleResult(results);
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#getDelegateByUniqueId(java.lang.String)
	 */
	@Cacheable(cacheName="delegateAccountCache")
	@Override
	public IDelegateCalendarAccount getDelegateByUniqueId(
			String accountUniqueId) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(AbstractOracleCalendarAccount.CTCALXITEMID, accountUniqueId));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter);
		IDelegateCalendarAccount resource = (IDelegateCalendarAccount) DataAccessUtils.singleResult(results);
		return resource;		
	}
	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#getDelegateByUniqueId(java.lang.String, org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Cacheable(cacheName="delegateAccountCache")
	@Override
	public IDelegateCalendarAccount getDelegateByUniqueId(
			String accountUniqueId, ICalendarAccount owner) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(AbstractOracleCalendarAccount.CTCALXITEMID, accountUniqueId));
		searchFilter.and(new EqualsFilter(OracleCalendarResourceAccountAttributesMapper.RESOURCE_OWNER_USERNAME, owner.getUsername()));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter, owner);
		IDelegateCalendarAccount resource = (IDelegateCalendarAccount) DataAccessUtils.singleResult(results);
		return resource;		
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#searchForDelegates(java.lang.String, org.jasig.schedassist.model.ICalendarAccount)
	 */
	@Override
	public List<IDelegateCalendarAccount> searchForDelegates(String searchText,
			ICalendarAccount owner) {
		String searchTextInternal = searchText.replace(" ", WILDCARD);
		if(!searchTextInternal.endsWith(WILDCARD)) {
			searchTextInternal += WILDCARD;
		}

		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new LikeFilter(CN, searchTextInternal));
		searchFilter.and(new EqualsFilter(OracleCalendarResourceAccountAttributesMapper.RESOURCE_OWNER_USERNAME, owner.getUsername()));
		searchFilter.and(new LikeFilter(AbstractOracleCalendarAccount.CTCALXITEMID, WILDCARD));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter, owner);
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.IDelegateCalendarAccountDao#searchForDelegates(java.lang.String)
	 */
	@Override
	public List<IDelegateCalendarAccount> searchForDelegates(String searchText) {
		String searchTextInternal = searchText.replace(" ", WILDCARD);
		if(!searchTextInternal.endsWith(WILDCARD)) {
			searchTextInternal += WILDCARD;
		}

		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new LikeFilter(CN, searchTextInternal));
		//searchFilter.and(new EqualsFilter(OracleCalendarResourceAccountAttributesMapper.RESOURCE_OWNER_USERNAME, owner.getUsername()));
		searchFilter.and(new LikeFilter(AbstractOracleCalendarAccount.CTCALXITEMID, WILDCARD));

		List<IDelegateCalendarAccount> results = executeSearchReturnList(searchFilter);
		return results;
	}
	
	/**
	 * 
	 * @param searchFilter
	 * @param owner
	 * @return
	 */
	protected List<IDelegateCalendarAccount> executeSearchReturnList(final Filter searchFilter) {
		return executeSearchReturnList(searchFilter, null);
	}

	/**
	 * 
	 * @param searchFilter
	 * @param owner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<IDelegateCalendarAccount> executeSearchReturnList(final Filter searchFilter, final ICalendarAccount owner) {
		SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(searchResultsLimit);
		searchControls.setTimeLimit(searchTimeLimit);
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		
		List<IDelegateCalendarAccount> results = Collections.emptyList();
		try {
			results = ldapTemplate.search(
				baseDn, 
				searchFilter.toString(), 
				searchControls, 
				new OracleCalendarResourceAccountAttributesMapper(this.oracleGUIDSource, owner));
			if(LOG.isDebugEnabled()) {
				LOG.debug("search " + searchFilter + " returned " + results.size() + " results");
			}
			
			Collections.sort(results, new DelegateDisplayNameComparator());
		} catch (SizeLimitExceededException e) {
			LOG.debug("search filter exceeded size limit (" + searchResultsLimit + "): " + searchFilter);
		} catch (TimeLimitExceededException e) {
			LOG.debug("search filter exceeded time limit(" + searchTimeLimit + " milliseconds): " + searchFilter);
		}
		return results;
	}

	/**
	 * Simple {@link Comparator} for {@link IDelegateCalendarAccount} that compares
	 * on the displayName field.
	 *
	 * @author Nicholas Blair, nblair@doit.wisc.edu
	 * @version $Id: OracleLdapCalendarResourceAccountDaoImpl.java 2600 2010-09-16 17:42:57Z npblair $
	 */
	private static class DelegateDisplayNameComparator implements Comparator<IDelegateCalendarAccount>{
		@Override
		public int compare(IDelegateCalendarAccount o1,
				IDelegateCalendarAccount o2) {
			return new CompareToBuilder().append(o1.getDisplayName(), o2.getDisplayName()).toComparison();
		}
	}
}
