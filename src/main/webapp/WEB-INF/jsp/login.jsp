<%@ include file="/WEB-INF/jsp/includes.jspf"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link type="text/css" href="css/smoothness/jquery-ui-1.7.2.custom.css"
	rel="stylesheet" />
<link type="text/css" href="css/style.css" rel="stylesheet" />
<script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>
<sec:authorize ifNotGranted="ROLE_USER,ROLE_ADMIN">
	
	<script type="text/javascript">
		<c:if test="${output eq 'failure'}">
			$(function() {
				parent.__onLogInFailure();
			});
		</c:if>
		$(function() {
			$('input').keyup(function(event) {
				if (event.keyCode == 13) {
					$('#loginForm').submit();
				}
				return true;
			});
		});
	</script>
	
</sec:authorize>
<sec:authorize ifAnyGranted="ROLE_USER,ROLE_ADMIN">
	<script type="text/javascript">
		$(function() {
			if (parent && parent.__onLogInSuccess) {
				parent.__onLogInSuccess();
			}
		});
	</script>
</sec:authorize>
<title>Login Form</title>
</head>
<body style="margin: 30px">
<sec:authorize ifNotGranted="ROLE_USER,ROLE_ADMIN">
	<div id="main" style="margin: 0px">
	<form id="loginForm" method="post" action="j_spring_security_check">
	<div><label for="j_username">Username<br />
	<input id="j_username" name="j_username" type="text" /></label> <br />
	<br />
	<label for="j_password">Password<br />
	<input id="j_password" name="j_password" type="password" /></label></div>
	</form>
	</div>
</sec:authorize>
<sec:authorize ifAnyGranted="ROLE_USER,ROLE_ADMIN">
You're in!
</sec:authorize>
</body>
</html>