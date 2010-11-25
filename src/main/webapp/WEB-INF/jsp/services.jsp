<?xml version="1.0" encoding="utf-8"?>
<jsp:root
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:sec="http://www.springframework.org/security/tags"
	xmlns:fn="http://java.sun.com/jsp/jstl/functions"
	version="2.0">

	<jsp:output doctype-root-element="html"
		doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
		omit-xml-declaration="no"/>

	<jsp:directive.page pageEncoding="utf-8" contentType="text/html; utf-8" />
	<html>
		<head>
			<title>${services.environment} Process Monitor on ${services.machine}</title>
			<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
			<link type="text/css" href="css/smoothness/jquery-ui-1.8.6.custom.css" rel="stylesheet" />
			<link type="text/css" href="css/style.css" rel="stylesheet" />
			<script type="text/javascript" src="js/jquery-1.4.2.min.js"><!-- --></script>
			<script type="text/javascript" src="js/jquery-ui-1.8.6.custom.min.js"><!-- --></script>
			<script type="text/javascript" src="js/common.js?.rand=${random}"><!-- --></script>
			<sec:authorize ifAllGranted="ROLE_ADMIN">
				<script type="text/javascript" src="js/admin-services.js?.rand=${random}"><!-- --></script>
			</sec:authorize>
			<sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_USER">
				<script type="text/javascript" src="js/user-services.js?.rand=${random}"><!-- --></script>
			</sec:authorize>
			<sec:authorize ifNotGranted="ROLE_ADMIN,ROLE_USER">
				<script type="text/javascript" src="js/login.js?.rand=${random}"><!-- --></script>
			</sec:authorize>
			<script type="text/javascript" src="js/post-loading.js?.rand=${random}"><!-- --></script>
		</head>
		<body>
			<div id="main">
			<h1 title="${services.machine}">${services.environment} Process Monitor</h1>
			<ul id="icons" class="ui-widget ui-helper-clearfix">
				<li id="refreshButton" class="ui-state-default ui-corner-all ui-state-disabled"
					title="Refresh All"><span class="ui-icon ui-icon-refresh"></span></li>
				<li id="startAllButton" class="ui-state-default ui-corner-all ui-state-disabled"
					title="Start All"><span class="ui-icon ui-icon-play"></span></li>
				<li id="stopAllButton" class="ui-state-default ui-corner-all ui-state-disabled"
					title="Stop All"><span class="ui-icon ui-icon-stop"></span></li>
				<li id="configureButton" class="ui-state-default ui-corner-all ui-state-disabled"
					title="Update Configuration"><span class="ui-icon ui-icon-gear"></span></li>
				<li id="logInOutButton" class="ui-state-default ui-corner-all"
					title="Log in"><span class="ui-icon ui-icon-locked"></span></li>
			</ul>
			<div id="serviceDetails">
				<jsp:directive.include file="/WEB-INF/jsp/services-ajax.jsp"/>
			</div>
			<div id="validXHTML"><img src="images/maven-feather.png" alt="Maven"/><img src="images/valid-xhtml10-blue.png" alt="Valid XHTML"/></div>
		</div>
		<jsp:directive.include file="/WEB-INF/jsp/dialogues.jspf"/>
		</body>
	</html>
</jsp:root>