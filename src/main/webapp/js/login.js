var loginDialog = null;

function __onLogInSuccess() {
	//$('head').append('<script type="text/javascript" src="js/services.js?.rand=${random}"></script>');
	window.location=contextDir + '?.rand=' + new Date().getTime();
	loginDialog.dialog("destroy");
}
function __onLogInFailure() {
	var speed = 200;
	loginDialog.parents('.ui-dialog:first')
		.animate({"left": "+=10px"}, speed)
		.animate({"left": "-=20px"}, speed)
		.animate({"left": "+=20px"}, speed)
		.animate({"left": "-=20px"}, speed)
		.animate({"left": "+=10px"}, speed);
	$(__getLoginForm()).find('#j_username').focus();
}
function __getLoginFrame() {
	if (document.getElementById('logInDialog').contentDocument) {
		return document.getElementById('logInDialog');
	} else {
		return document.frames['logInDialog'];
	}
}

function __getLoginForm() {
	var loginFrame = __getLoginFrame();
	if (loginFrame.contentDocument) {
		return loginFrame.contentDocument.getElementById('loginForm');
	} else {
		return loginFrame.document.getElementById('loginForm');
	}
}
function logInOut() {
	$(this).addClass('ui-state-active');
	if (loginDialog == null) {
		var dialogOptions = {
			buttons: {
				"Ok": function() {
					$('#logInOutButton').removeClass('ui-state-active');
					__getLoginForm().submit();
				},
				"Cancel" : function() {
					$('#logInOutButton').removeClass('ui-state-active');
					$(this).dialog("close");
				}
			},
			modal: true,
			resizable: false,
			bgiframe: true,
			title: 'Please enter your login details'
		};
		
		loginDialog = $('#logInDialog').dialog(dialogOptions);
	} else {
		loginDialog.dialog('open');
	}
	$(__getLoginForm()).find('#j_username').focus();
}
$(function() {
	$('#logInOutButton').click(logInOut).hover(function() {
		$(this).addClass('ui-state-hover');
	}, function() {
		$(this).removeClass('ui-state-hover');
	});
	$(__getLoginFrame()).load(function() {
		$(__getLoginForm()).find('#j_username').focus();
	});
});