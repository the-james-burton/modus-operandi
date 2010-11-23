var viewLogRefreshId = null;
function __viewLog__success(data, textStatus) {
	$('#viewLogDialog').empty().append(data);
	__hideRefresh();
}

function __viewLog__error(XMLHttpRequest, textStatus, errorThrown) {
	$(this.ui).removeClass('ui-state-highlight').addClass('ui-state-error');
	__hideRefresh();
	$('#viewLogDialog').dialog('close');
}

function __viewLog__open(event, parent_ui, windowTitle) {
	$.ajax( {
		'url' : contextDir + 'taillog.view',
		'type' : 'GET',
		'data' : ( {
			'windowTitle' : windowTitle
		}),
		'cache' : false,
		'success' : __viewLog__success,
		'error' : __viewLog__error,
		'ui' : parent_ui
	});
	__showRefresh();
}

function __openLogInNewWindow(windowTitle) {
	var winOps = 'width=800,height=600,resizable=yes,location=no,directories=no,status=yes,menubar=now,copyhistory=no,scrollbars=yes';
	window.open(contextDir + 'viewlog.view?windowTitle=' + windowTitle, 'Logs' + windowTitle, winOps);
	$('#viewLogDialog').dialog('close');
}

function __setUpViewLog() {
	$("div[class*='viewLogButton']").click(
			function() {
				var windowTitle = this.id.substring('log_'.length);
				//window.open('taillog.view?windowTitle=' + windowTitle,'popupName','scrollbars=1,width=800,height=400');
				if (viewLogRefreshId != null) {
					clearTimeout(viewLogRefreshId);
				}
				$('#viewLogDialog').remove();
				var parent_ui = this;
				var ops = {
					buttons: {
						"Close": function() {
							$(this).dialog("close");
						},
						"Open in external window" :  function() {__openLogInNewWindow(windowTitle);}
					},
					modal: true,
					title: 'Log Dialog',
					height : 500,
					width : 700,
					open : function (event, ui) {
						__viewLog__open(event, parent_ui, windowTitle);
						viewLogRefreshId = setInterval('__viewLog__open(null, null,\'' + windowTitle + '\')', 10000);
						$('#viewLogDialog').find('.ui-widget-header').append('Hello world');
					},
					close : function (event, ui) {
						clearInterval(viewLogRefreshId);
					}
				};
				
				$('#main').append('<div id="viewLogDialog"><img src="images/refreshing.gif"/></div>').find('#viewLogDialog').dialog(ops);
				
			}).hover(function() {
				$(this).addClass('ui-state-hover');
			}, function() {
				$(this).removeClass('ui-state-hover');
			}).removeClass('ui-state-disabled');
}


function __onLogOut() {
	window.location=contextDir + '?.rand=' + new Date().getTime();
}
function logOut() {
	$(this).addClass('ui-state-active');
	var dialogOptions = {
		buttons: {
			"Ok": function() {
				$('#logInOutButton').removeClass('ui-state-active');
				
				$.ajax( {
					'url' : contextDir + 'process_logout',
					'type' : 'POST',
					
					'cache' : false,
					'success' : __onLogOut,
					'error' : __onLogOut
				});
				
				$('#logInOutButton').find('span')
				.removeClass('ui-icon-unlocked').addClass('ui-icon-locked');
				$(this).dialog("destroy");
				window.location = contextDir + '?.rand=' + new Date().getTime();
			},
			"Cancel" : function() {
				$('#logInOutButton').removeClass('ui-state-active');
				$(this).dialog("destroy");
			}
		
		},
		modal : true,
		resizable: false,
		title: 'Please log out'
	};
	$('#logOutDialog').remove();
	$('#main').append('<div id="logOutDialog">Would you really like to log out?</div>').find('#logOutDialog').dialog(dialogOptions);
}

function __checkValidSessionDialog(data, textStatus) {
	if (data != 'true') {
	$("#loggedOutDialog").dialog({
		bgiframe: true,
		resizable: false,
		height:140,
		modal: true,
		overlay: {
			backgroundColor: '#000',
			opacity: 0.5
		},
		buttons: {
			'Close': function() {
				$(this).dialog('close');
				window.location = contextDir + '?.rand=' + new Date().getTime();
			}
		}
	});
	}
}

function checkIsLoggedIn() {
	$.ajax( {
		'url' : contextDir + 'isloggedin.view',
		'type' : 'GET',
		'cache' : false,
		'success' : __checkValidSessionDialog,
		'error' : __checkValidSessionDialog
	});
}

$(function() {
	$('#logInOutButton').unbind('click', logOut).bind('click', logOut)
		.attr('title','Log out').find('span')
		.removeClass('ui-icon-locked')
		.addClass('ui-icon-unlocked')
		.removeClass('ui-state-active');
	
	__addEventToRefreshSuccess(__setUpViewLog);
	
	setInterval('checkIsLoggedIn()', 10000);
});