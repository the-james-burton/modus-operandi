var loginDialog;
function __onLogInSuccess() {
	window.location=contextDir + '?.rand=' + new Date().getTime();
	loginDialog.dialog("destroy");
}
function __onLogInFailure() {
	var speed = 200;
	$('#logInDialog').parents('.ui-dialog:first')
		.animate({"left": "+=10px"}, speed)
		.animate({"left": "-=20px"}, speed)
		.animate({"left": "+=20px"}, speed)
		.animate({"left": "-=20px"}, speed)
		.animate({"left": "+=10px"}, speed);
	//$('#logInDialog').parents('.ui-dialog:first').effect('shake', {}, speed);
	//__getLoginFrame().dialog('enable');
	$('#j_username').focus();
}
function __getLoginFrame() {
	if ($('#logInDialog').size() == 0) {
		$('body').append('<div id="logInDialog"><img src="/images/refreshing.gif" alt="loading"/></div>');
	}
	return $('#logInDialog');
}
function __onSubmit() {
	var url= location.protocol+'//'+location.host + '/' + $('#loginForm').attr('action');
	var method = $('#loginForm').attr('method');
	var data = {
		j_username : $('#j_username').val(),
		j_password : $('#j_password').val()
	};
	//$('#logInDialog').dialog('disable');
	//alert('wait..');
	$.ajax({
		type: method,
		url: url,
		async : false,
		cache : false,
		data: data,
		success: function(data) {
			__onLogInSuccess();
		},
		error: function(request, error) {
			__onLogInFailure();
		}
	});
}
function logInOut() {
	$(this).addClass('ui-state-active');
	if (loginDialog == null) {
		var dialogOptions = {
			buttons: {
				OK : function() {
					$('#logInOutButton').removeClass('ui-state-active');
					if ($('#loginForm').size() > 0) {
						__onSubmit();
					}
				},
				Cancel : function() {
					$('#logInOutButton').removeClass('ui-state-active');
					loginDialog.dialog('close');
				}
			},
			open : function(event, ui) {
				var url= location.protocol+'//'+location.host + '/login.view';
				__getLoginFrame().load(url);
			},
			modal: true,
			resizable: false,
			bgiframe: true,
			height: '180',
			title: 'Please enter your login details'
		};
		loginDialog = __getLoginFrame().dialog(dialogOptions);
	} else {
		loginDialog.dialog('open');
	}

}
$(function() {
	$('#logInOutButton').click(logInOut).hover(function() {
		$(this).addClass('ui-state-hover');
	}, function() {
		$(this).removeClass('ui-state-hover');
	});
});