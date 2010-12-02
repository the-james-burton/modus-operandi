<?xml version="1.0" encoding="utf-8"?>
<jsp:root
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:sec="http://www.springframework.org/security/tags"
	version="2.0">

	<jsp:output omit-xml-declaration="yes" />
	<jsp:directive.page pageEncoding="utf-8" contentType="text/html; utf-8" />

	<div id="bulkAction">
		<sec:authorize ifNotGranted="ROLE_ADMIN">
			<p>You must be logged in as ADMIN to see this resource.</p>
		</sec:authorize>
		<sec:authorize ifAnyGranted="ROLE_ADMIN">
			<p>This form allows you to populate your configuration from a Spring config XML.<br/><br/>
			This must define a <code>util:list</code> with <code>id="bulkUpdate"</code> containing <code>Process</code>
			objects, and a <code>util:list</code> with <code>id="properties"</code> containing <code>ConfigEntry</code> objects</p>
			<p>This operation will <em>replace</em> any existing data.</p>
			<form method="post" action="/bulkInsert.view">
				<label for="fileLocation">
					File Location:<br/>
					<input id="fileLocation" type="text" />
				</label>
			</form> 
		</sec:authorize>
	</div>
</jsp:root>