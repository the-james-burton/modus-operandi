<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<%@ include file="/WEB-INF/jsp/includes.jspf"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<link type="text/css" href="css/smoothness/jquery-ui-1.7.2.custom.css"
	rel="stylesheet" />
<link type="text/css" href="css/style.css" rel="stylesheet" />
<script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>
<script type="text/javascript" src="js/common.js?.rand=${random}"></script>
<sec:authorize ifAllGranted="ROLE_ADMIN">
<script type="text/javascript" src="js/admin-services.js?.rand=${random}"></script>
</sec:authorize>
<sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_USER">
<script type="text/javascript" src="js/user-services.js?.rand=${random}"></script>
</sec:authorize>
<sec:authorize ifNotGranted="ROLE_ADMIN,ROLE_USER">
<script type="text/javascript" src="js/login.js?.rand=${random}"></script>
</sec:authorize>
<script type="text/javascript" src="js/post-loading.js?.rand=${random}"></script>
<title>${services.environment} Process Monitor on ${services.machine}</title>
</head>
<body>
<div id="main">
<h1 title="${services.machine}">${services.environment} Process Monitor</h1>
<c:choose>
	<c:when test="${fn:length(services.processes) == 0}">
		<div class="ui-widget">
		<div class="ui-state-error ui-corner-all"
			style="margin-top: 20px; padding: 0 .7em;">
		<p><span class="ui-icon ui-icon-alert"
			style="float: left; margin-right: .3em;"></span> <strong>Warning!</strong>
		No processes defined for this server. Please check your configuration
		files.</p>
		</div>
		</div>
	</c:when>
	<c:otherwise>
		<ul id="icons" class="ui-widget ui-helper-clearfix">
			<li id="refreshButton" class="ui-state-default ui-corner-all ui-state-disabled"
				title="Refresh All"><span class="ui-icon ui-icon-refresh"></span></li>
			<li id="startAllButton" class="ui-state-default ui-corner-all ui-state-disabled"
				title="Start All"><span class="ui-icon ui-icon-play"></span></li>
			<li id="stopAllButton" class="ui-state-default ui-corner-all ui-state-disabled"
				title="Stop All"><span class="ui-icon ui-icon-stop"></span></li>
			<li id="logInOutButton" class="ui-state-default ui-corner-all"
				title="Log in"><span class="ui-icon ui-icon-locked"></span></li>
		</ul>
		<div id="resultsTableContainer"
			class="ui-widget ui-widget-content ui-corner-all"
			style="margin-top: 10px; padding: 3px;">
		<table id="resultsTable">
			<colgroup>
				<col style="width: 37px" />
				<col style="width: 37px" />
				<col style="width: 37px" />
				<col style="width: 37px" />
				<col style="width: 37px" />
				<col style="width: *" />
				<col style="width: 200px" />
				<col style="width: 20px" />
				<col style="width: 150px" />
			</colgroup>
			<tfoot>
				<tr>
					<td colspan="4"><c:out value="${updateTime}" /></td>
					<td colspan="5"><div id="refreshing"></div></td>
				</tr>
			</tfoot>
			<tbody>
				<c:forEach items="${services.processes}" var="process">
					<c:set var="statuscss" value="ui-state-default ui-corner-all ui-state-disabled" />
					<c:set var="iconcss" value="ui-icon ui-icon-circle-arrow-s" />
					<c:choose>
						<c:when test="${process.starting}">
							<c:set var="status" value="starting" />
							<c:set var="iconcss" value="ui-icon ui-icon-clock" />
						</c:when>
						<c:when test="${process.stopping}">
							<c:set var="status" value="stopping" />
							<c:set var="iconcss" value="ui-icon ui-icon-clock" />
						</c:when>
						<c:when test="${!process.running}">
							<c:set var="status" value="stopped" />
							<c:set var="statuscss"
								value="ui-state-default ui-state-error ui-corner-all" />
							<c:set var="iconcss" value="ui-icon ui-icon-circle-arrow-s" />
						</c:when>
						<c:when test="${process.running}">
							<c:set var="status" value="running" />
							<c:set var="iconcss" value="ui-icon ui-icon-circle-arrow-n" />
						</c:when>
					</c:choose>
					<tr class="${status}">
						<td><c:choose>
							<c:when test="${process.hasInfo}">
								<div id="info_${process.encodedWindowTitle}" title="Information"
									class="infoButton ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-info"></span></div>
							</c:when>
							<c:otherwise>
								<div id="info_${process.encodedWindowTitle}" title="No data"
									class="button ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-info"></span></div>
							</c:otherwise>
						</c:choose>
						</td>
						<td>
						<div class="${statuscss}" style="width: 16px"><span
							class="${iconcss}"></span></div>
						</td>
						<td><c:if test="${process.pid > 0}">${process.pid}</c:if></td>
						<td><c:choose>
							<c:when
								test="${process.pid > 0 and process.starting == false and process.stopping == false}">
								<div id="pid_${process.pid}" title="stop"
									class="stopButton ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-stop"></span></div>
							</c:when>
							<c:otherwise>
								<div title="Stopped"
									class="button ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-stop"></span></div>
							</c:otherwise>
						</c:choose></td>
						<td><c:choose>
							<c:when
								test="${process.pid == 0 and process.starting == false and process.stopping == false}">
								<div id="${process.encodedWindowTitle}"
									title="start: [${process.startCommand}]"
									class="startButton ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-play"></span></div>
							</c:when>
							<c:otherwise>
								<div id="${process.encodedWindowTitle}"
									title="Started: [${process.startCommand}]"
									class="button ui-state-default ui-corner-all ui-state-disabled"><span
									class="ui-icon ui-icon-play"></span></div>
							</c:otherwise>
						</c:choose></td>
						<td><c:out value="${process.windowTitle}" /></td>
						<td><c:if test="${process.createdOn != null}">${process.createdOn}</c:if></td>
						<td><c:if test="${process.log != null}">
							<c:choose>
								<c:when test="${process.log.full}">
									<div id="log_${process.encodedWindowTitle}"
										title="View logs: [${process.log.pathfilename}]"
										class="viewLogButton ui-state-default ui-state-disabled ui-corner-all"><span
										class="ui-icon ui-icon-document"></span></div>
								</c:when>
								<c:otherwise>
									<div id="log_${process.encodedWindowTitle}" title="No logs to view"
										class="button ui-state-default ui-state-disabled ui-corner-all"><span
										class="ui-icon ui-icon-document"></span></div>
								</c:otherwise>
							</c:choose>
						</c:if></td>
						<td><c:if
							test="${process.log != null && process.log.full}">${process.log.lastModified}</c:if></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		</div>
	</c:otherwise>
</c:choose>

<div id="validXHTML"><img src="images/maven-feather.png"/><img src="images/valid-xhtml10-blue.png"/></div>
</div>
<%@ include file="/WEB-INF/jsp/dialogues.jspf"%>
</body>
</html>