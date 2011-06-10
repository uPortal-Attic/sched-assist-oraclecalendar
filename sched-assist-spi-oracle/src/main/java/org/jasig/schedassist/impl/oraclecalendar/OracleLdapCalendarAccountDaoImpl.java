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
import org.jasig.schedassist.CalendarAccountNotFoundException;
import org.jasig.schedassist.ICalendarAccountDao;
import org.jasig.schedassist.model.ICalendarAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.ldap.SizeLimitExceededException;
import org.springframework.ldap.TimeLimitExceededException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.filter.OrFilter;

import com.googlecode.ehcache.annotations.Cacheable;

/**
 * LDAP backed implementation of {@link ICalendarAccountDao} for locating 
 * Oracle calendar accounts.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 * @version $Id: OracleLdapCalendarAccountDaoImpl.java 2759 2010-10-05 16:49:46Z npblair $
 */
public final class OracleLdapCalendarAccountDaoImpl implements ICalendarAccountDao {

	private Log LOG = LogFactory.getLog(this.getClass());

	private static final String WILD = "*";
	private LdapTemplate ldapTemplate;
	private String baseDn = "o=isp";
	private String calendarUniqueIdAttributeName = AbstractOracleCalendarAccount.CTCALXITEMID;
	private OracleGUIDSource oracleGUIDSource;
	private long searchResultsLimit = 25;
	private int searchTimeLimit = 5000;

	/**
	 * @param baseDn The baseDn to set.
	 */
	@Required
	public void setBaseDn(final String baseDn) {
		this.baseDn = baseDn;
	}

	/**
	 * @param ldapTemplate The ldapTemplate to set.
	 */
	@Autowired
	public void setLdapTemplate(final LdapTemplate ldapTemplate) {
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
	 * Default value is "ctcalxitemid".
	 * 
	 * @param calendarUniqueIdAttributeName The name of the attribute that stores the calendar server's unique ID.
	 */
	public void setCalendarUniqueIdAttributeName(final String calendarUniqueIdAttributeName) {
		this.calendarUniqueIdAttributeName = calendarUniqueIdAttributeName;
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
	 * @see org.jasig.schedassist.ICalendarAccountDao#getCalendarAccount(java.lang.String)
	 */
	@Cacheable(cacheName="userAccountCache")
	@Override
	public ICalendarAccount getCalendarAccount(String username) {
		EqualsFilter searchFilter = new EqualsFilter(OracleCalendarUserAccountAttributesMapper.USERNAME_ATTRIBUTE, username);
		return executeSearch(searchFilter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.ICalendarAccountDao#getCalendarAccount(java.lang.String, java.lang.String)
	 */
	@Cacheable(cacheName="userAccountCache")
	@Override
	public ICalendarAccount getCalendarAccount(String attributeName,
			String attributeValue) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(attributeName, attributeValue));
		// and guarantee our search returns users that have uids
		searchFilter.and(new LikeFilter(OracleCalendarUserAccountAttributesMapper.USERNAME_ATTRIBUTE, WILD));
		
		return executeSearch(searchFilter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.ICalendarAccountDao#getCalendarAccountFromUniqueId(java.lang.String)
	 */
	@Cacheable(cacheName="userAccountCache")
	@Override
	public ICalendarAccount getCalendarAccountFromUniqueId(
			String calendarUniqueId) {
		AndFilter searchFilter = new AndFilter();
		searchFilter.and(new EqualsFilter(calendarUniqueIdAttributeName, calendarUniqueId));
		// guarantee our search returns users that have uids
		searchFilter.and(new LikeFilter(OracleCalendarUserAccountAttributesMapper.USERNAME_ATTRIBUTE, WILD));

		return executeSearch(searchFilter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.schedassist.ICalendarAccountDao#searchForCalendarAccounts(java.lang.String)
	 */
	@Cacheable(cacheName="userAccountCache")
	@Override
	public List<ICalendarAccount> searchForCalendarAccounts(String searchText) {
		AndFilter searchFilter = new AndFilter();
		
		StringBuilder wildcardSearchText = new StringBuilder();
		wildcardSearchText.append(WILD);
		wildcardSearchText.append(searchText);
		wildcardSearchText.append(WILD);
		
		OrFilter orFilter = new OrFilter();
		orFilter.or(new LikeFilter(OracleCalendarUserAccountAttributesMapper.USERNAME_ATTRIBUTE, wildcardSearchText.toString()));
		orFilter.or(new LikeFilter(OracleCalendarUserAccountAttributesMapper.DISPLAYNAME_ATTRIBUTE, wildcardSearchText.toString()));
		
		searchFilter.and(orFilter);
		// guarantee we search for users that have calendar attributes
		searchFilter.and(new LikeFilter(calendarUniqueIdAttributeName, WILD));
		// guarantee we search for users with uids
		searchFilter.and(new LikeFilter(OracleCalendarUserAccountAttributesMapper.USERNAME_ATTRIBUTE, WILD));
		return executeSearchReturnList(searchFilter);
	}

	/**
	 * 
	 * @param searchFilter
	 * @return
	 * @throws CalendarAccountNotFoundException
	 */
	protected ICalendarAccount executeSearch(final Filter searchFilter) {		
		List<ICalendarAccount> results = executeSearchReturnList(searchFilter);
		ICalendarAccount result = DataAccessUtils.singleResult(results);
		if(result != null && LOG.isDebugEnabled()) {
			LOG.debug("search filter " + searchFilter.toString() + " success: " + result);
		}
		return result;
	}

	/**
	 * 
	 * @param searchFilter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<ICalendarAccount> executeSearchReturnList(final Filter searchFilter) {
		LOG.debug("searchFilter: " + searchFilter);
		SearchControls searchControls = new SearchControls();
		searchControls.setCountLimit(searchResultsLimit);
		searchControls.setTimeLimit(searchTimeLimit);
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		List<ICalendarAccount> results = Collections.emptyList();
		try {
			results = ldapTemplate.search(baseDn, 
					searchFilter.toString(), searchControls, 
					new OracleCalendarUserAccountAttributesMapper(this.oracleGUIDSource));
			if(LOG.isDebugEnabled()) {
				LOG.debug("search " + searchFilter + " returned " + results.size() + " results");
			}
			Collections.sort(results, new AccountComparator());
		} catch (SizeLimitExceededException e) {
			LOG.debug("search filter exceeded size limit (" + searchResultsLimit + "): " + searchFilter);
		} catch (TimeLimitExceededException e) {
			LOG.debug("search filter exceeded time limit(" + searchTimeLimit + " milliseconds): " + searchFilter);
		}
		return results;
	}

	private static class AccountComparator implements Comparator<ICalendarAccount> {

		@Override
		public int compare(ICalendarAccount o1, ICalendarAccount o2) {
			return new CompareToBuilder()
				.append(o1.getDisplayName(), o2.getDisplayName())
				.append(o1.getUsername(), o2.getUsername())
				.toComparison();
		}
		
	}
}
