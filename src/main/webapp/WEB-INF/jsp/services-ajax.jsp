<?xml version="1.0" encoding="utf-8"?>
<jsp:root
	xmlns="http://www.w3.org/1999/xhtml"
	xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:c="http://java.sun.com/jsp/jstl/core"
	xmlns:sec="http://www.springframework.org/security/tags"
	version="2.0">

	<jsp:directive.page pageEncoding="utf-8" contentType="text/html; utf-8" />

	<c:choose>
		<c:when test="${empty services.processes}">
			<div id="noServices" class="ui-widget">
			<div class="ui-state-error ui-corner-all" style="margin-top: 20px; padding: 0 .7em;">
			<p><span class="ui-icon ui-icon-alert"
				style="float: left; margin-right: .3em;"><!-- --></span> <strong>Warning!</strong>
			No processes defined for this server.
				<sec:authorize ifAllGranted="ROLE_ADMIN">
					Please update your configuration using the gear icon above.
				</sec:authorize>
				<sec:authorize ifNotGranted="ROLE_ADMIN,ROLE_USER">
					Please log in as an administrator and update your configuration.
				</sec:authorize>
			</p>
			</div>
			</div>
		</c:when>
		<c:otherwise>
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
						<col style="width: 20px" />
						<col style="width: 150px" />
					</colgroup>
					<tfoot>
						<tr>
							<td colspan="5"><c:out value="${updateTime}" /></td>
							<td colspan="3"><div id="refreshing"></div></td>
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
									<c:when test="${not empty process.properties}">
										<div id="info_${process.windowTitle}" title="Information"
											class="infoButton ui-state-default ui-corner-all ui-state-disabled"><span
											class="ui-icon ui-icon-info"></span></div>
									</c:when>
									<c:otherwise>
										<div id="info_${process.windowTitle}" title="No data"
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
										<div id="${process.windowTitle}"
											title="start: [${process.startCommand}]"
											class="startButton ui-state-default ui-corner-all ui-state-disabled"><span
											class="ui-icon ui-icon-play"></span></div>
									</c:when>
									<c:otherwise>
										<div id="${process.windowTitle}"
											title="Started: [${process.startCommand}]"
											class="button ui-state-default ui-corner-all ui-state-disabled"><span
											class="ui-icon ui-icon-play"></span></div>
									</c:otherwise>
								</c:choose></td>
								<td><c:out value="${process.windowTitle}" /></td>
								<td><c:if test="${process.log != null}">
									<c:choose>
										<c:when test="${process.log.full}">
											<div id="log_${process.windowTitle}"
												title="View logs: [${process.log.pathfilename}]"
												class="viewLogButton ui-state-default ui-state-disabled ui-corner-all"><span
												class="ui-icon ui-icon-document"></span></div>
										</c:when>
										<c:otherwise>
											<div id="log_${process.windowTitle}" title="No logs to view"
												class="button ui-state-default ui-state-disabled ui-corner-all"><span
												class="ui-icon ui-icon-document"></span></div>
										</c:otherwise>
									</c:choose>
								</c:if></td>
								<td><c:if
									test="${process.log != null and process.log.full}">${process.log.lastModified}</c:if></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</c:otherwise>
	</c:choose>
</jsp:root>