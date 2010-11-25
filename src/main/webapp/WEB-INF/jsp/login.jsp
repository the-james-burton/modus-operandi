<?xml version="1.0" encoding="utf-8"?>
<jsp:root
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:sec="http://www.springframework.org/security/tags"
	version="2.0">

	<jsp:output omit-xml-declaration="yes" />
	<jsp:directive.page pageEncoding="utf-8" contentType="text/html; utf-8" />

	<div id="main" style="margin: 0px">
		<sec:authorize ifNotGranted="ROLE_USER,ROLE_ADMIN">
			<script type="text/javascript">
			$(function() {
				$('input').keyup(function(event) {
					if (event.keyCode == 13) {
						__onSubmit();
					}
					return true;
				});
			});
			</script>

			<form id="loginForm" method="post" action="j_spring_security_check">
				<div>
					<label for="j_username">Username<br />
						<input id="j_username" name="j_username" type="text" />
					</label>
					<br />
					<br />
					<label for="j_password">Password<br />
						<input id="j_password" name="j_password" type="password" />
					</label>
				</div>
			</form>
		</sec:authorize>
		<sec:authorize ifAnyGranted="ROLE_USER,ROLE_ADMIN">
			<script type="text/javascript">
				$(function() {
					if (parent &amp;&amp; parent.__onLogInSuccess) {
						parent.__onLogInSuccess();
					}
				});
			</script>
			<p>You're in!</p>
		</sec:authorize>
	</div>
</jsp:root>