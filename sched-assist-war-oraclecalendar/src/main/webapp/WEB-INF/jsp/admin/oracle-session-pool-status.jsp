<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.

--%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">
<head>
<title><spring:message code="application.name.admin"/> - <spring:message code="oracle.session.pool.status"/></title>
<%@ include file="/WEB-INF/jsp/themes/jasig/head-elements.jsp" %>
<script type="text/javascript" src="<c:url value="/js/jquery.lockSubmit.js"/>"></script>
<script type="text/javascript">
$(document).ready(function(){
	$(':submit').lockSubmit();
});
</script>
<style type="text/css">
.formborder {
border: 1px solid gray;
margin: 1em;
padding: 1em;
}
</style>

</head>

<body>
<%@ include file="/WEB-INF/jsp/themes/jasig/body-start.jsp" %>
<%@ include file="/WEB-INF/jsp/login-info.jsp" %>
<div id="content" class="main col">

<div class="info">
<span><spring:message code="oracle.session.pool.status"/></span>
<ul>
<li>${numIdle }&nbsp;<spring:message code="oracle.session.idle"/></li>
<li>${numActive }&nbsp;<spring:message code="oracle.session.active"/></li>
</ul>
</div>

<div id="clearForm" class="formborder">
<c:url var="clearPoolUrl" value="oracle-session-pool.html">
<c:param name="action" value="clear"></c:param>
</c:url>
<form:form action="${clearPoolUrl }" method="post"> 
<spring:message code="oracle.session.pool.clear"/>&nbsp;<input type="submit" value="Clear"/>
</form:form>
</div>

<div id="clearNodeForm" class="formborder">
<c:url var="clearNodeUrl" value="oracle-session-pool.html">
<c:param name="action" value="clearNode"></c:param>
</c:url>
<form:form action="${clearNodeUrl }" method="post">
<label for="nodeId"><spring:message code="oracle.session.pool.node.id"/>:&nbsp;</label> 
<input type="text" name="nodeId"/><br/>
<spring:message code="oracle.session.pool.clear.node"/>&nbsp;<input type="submit" value="Clear Sessions for this Node"/>
</form:form>
</div>

<a href="<c:url value="/admin/index.html"/>">&laquo;<spring:message code="return.to.admin.home"/></a>
</div> <!--  content -->

<%@ include file="/WEB-INF/jsp/themes/jasig/body-end.jsp" %>
</body>
</html>