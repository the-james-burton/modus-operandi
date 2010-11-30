function __startAll__success(data, textStatus) {
	$('#startAllButton').removeClass('ui-state-active');
	$('#serviceDetails').html(data);
	__runAllRefreshSuccessHandlers();
}

function __startAll__error(data, textStatus) {
	$('#startAllButton').addClass('ui-state-error');
	$('#serviceDetails').html(errorMarkup);
	__hideRefresh();
}

function __stopAll__success(data, textStatus) {
	$('#stopAllButton').removeClass('ui-state-active');
	$('#serviceDetails').html(data);
	__runAllRefreshSuccessHandlers();
}

function __stopAll__error(data, textStatus) {
	$('#stopAllButton').addClass('ui-state-error');
	$('#serviceDetails').html(errorMarkup);
	__hideRefresh();
}

function refreshAll() {
	refresh('POST');
}
function startAll() {
	$('#startAllButton').addClass('ui-state-active').removeClass('ui-state-error');
	$("tr:has(div[class*=startButton])").removeClass('stopped')
	.addClass('starting');
	$.ajax( {
		url : contextDir,
		type : 'POST',
		data : ( {
			pid : -2,
			startAll : true,
			ajax : true
		}),
		cache : false,
		success : __startAll__success,
		error : __startAll__error
	});
	__showRefresh();
}

function stopAll(event) {
	event.preventDefault();
	event.stopPropagation();
	$('#stopAllButton').addClass('ui-state-active').removeClass('ui-state-error');
	$.ajax( {
		url : contextDir,
		type : 'POST',
		data : ( {
			pid : -2,
			stopAll : true,
			ajax : true
		}),
		cache : false,
		success : __stopAll__success,
		error : __stopAll__error
	});
	__showRefresh();
}

function configure(event) {
	if ($('#bulkInsertDialog').size() == 0) {
		var url= location.protocol + '//' + location.host + '/bulkInsert.view';
		var dialogOptions = {
			buttons: {
				OK : function() {
					var fileLocation = $.trim($('#fileLocation').removeClass('ui-state-error').val());
					if (fileLocation == '') {
						$('#fileLocation').addClass('ui-state-error');
						return;
					}
					__showRefresh();
					$.ajax( {
						url : url,
						type : 'POST',
						data : ( {
							fileName : fileLocation,
							ajax : true
						}),
						cache : false,
						success : function (data, textStatus, XMLHttpRequest) {
							if (data == 'OK') {
								
							}
							$('#bulkInsertDialog').html('File loaded successfully.').dialog({
									buttons: {
										OK : function() {
											$(this).dialog('close');
											$('#bulkInsertDialog').remove();
										}
									}
								}
							);
							__hideRefresh();
						},
						error : function(XMLHttpRequest, textStatus, errorThrown) {
							alert('no luck...');
							__hideRefresh();
						}
					});
				},
				Cancel : function() {
					$(this).dialog('close');
				}
			},
			open : function(event, ui) {
				$('#bulkInsertDialog').load(url);
			},
			modal: true,
			resizable: false,
			bgiframe: true,
			//height: '180',
			width: 330,
			title: 'Bulk Insert **Use With Care**'
		};
		$('body').append('<div id="bulkInsertDialog"><img src="/images/refreshing.gif" alt="loading"/></div>');
		$('#bulkInsertDialog').dialog(dialogOptions);
	} else {
		$('#bulkInsertDialog').dialog('open');
	}
}

function __setUpStop() {
	$("div[class*='stopButton']").click(function() {
		$(this).addClass('ui-state-active').addClass('ui-state-highlight').removeClass('ui-state-error');
		$.ajax( {
			'url' : contextDir,
			'type' : 'POST',
			'data' : ( {
				pid : this.id.substring('pid_'.length),
				windowTitle : '',
				ajax : true
			}),
			cache : false,
			success : __refresh__success,
			error : __generic__error,
			ui : this
		});
		__showRefresh();
	}).hover(function() {
		$(this).addClass('ui-state-hover');
	}, function() {
		$(this).removeClass('ui-state-hover');
	}).removeClass('ui-state-disabled');
}

function __setUpStart() {
	$("div[class*='startButton']").click(
			function() {
				$("tr:has(div[id='" + this.id + "'])").removeClass('stopped')
						.addClass('starting');
				$(this).addClass('ui-state-active').addClass('ui-state-highlight').removeClass(
						'ui-state-error');
				$.ajax( {
					url : contextDir,
					type : 'POST',
					data : ( {
						pid : 0,
						windowTitle : this.id,
						ajax : true
					}),
					cache : false,
					success : __refresh__success,
					error : __generic__error,
					ui : this
				});
				__showRefresh();
			}).hover(function() {
		$(this).addClass('ui-state-hover');
	}, function() {
		$(this).removeClass('ui-state-hover');
	}).removeClass('ui-state-disabled');
}


function __viewInfo__success(data, textStatus) {
	$('#viewInfo').empty().append(data);
	__hideRefresh();
}
function __viewInfo__open(event, ui, windowTitle) {
	$.ajax( {
		url : contextDir + 'viewinfo.view',
		type : 'POST',
		data : {
			windowTitle : windowTitle,
			ajax : true
		},
		cache : false,
		success : __viewInfo__success,
		error : __viewInfo__success
	});
	__showRefresh();

}
function __setUpViewInfo() {
	$("div[class*='infoButton']").click(
			function() {
				var windowTitle = this.id.substring('info_'.length);
				$(this).addClass('ui-state-active').addClass('ui-state-highlight');
				$('#viewInfo').remove();
				var ops = {
					buttons: {
						"Close": function() { $(this).dialog("close"); },
						"Copy to clipboard" : function() {
							var clipboardDataText = '';
							$('#viewInfo').find('tr').each(function(i, domElement) {
								var tds = $(domElement).find('td');
								clipboardDataText += tds[0].innerHTML + "\t" + tds[1].innerHTML +"\r\n";
							});
							if (window.clipboardData) {
								window.clipboardData.setData("Text", clipboardDataText);
							} else {
								alert(clipboardDataText);
							}
						}
					},
					modal: true,
					title: 'Info Dialog',
					height : 300,
					width : 700,
					open : function (event, ui) {
						__viewInfo__open(event, ui, windowTitle);
					}
				};
				$('#main').append('<div id="viewInfo"><img src="images/refreshing.gif"/></div>').find('#viewInfo').dialog(ops);
			}).hover(function() {
				$(this).addClass('ui-state-hover');
			}, function() {
				$(this).removeClass('ui-state-hover');
			}).removeClass('ui-state-disabled');
}

$(function() {
	/**
	 * Initialise the main buttons.
	 */
	$('#refreshButton').click(refreshAll).removeClass('ui-state-disabled');
	$('#startAllButton').click(startAll).removeClass('ui-state-disabled');
	$('#stopAllButton').click(stopAll).removeClass('ui-state-disabled');
	$('#configureButton').click(configure).removeClass('ui-state-disabled');

	//add new event handlers
	__addEventToRefreshSuccess(__setUpStop);
	__addEventToRefreshSuccess(__setUpStart);
	__addEventToRefreshSuccess(__setUpViewInfo);

	// hover states on the static widgets
	$('#dialog_link, ul#icons li').hover(function() {
		$(this).addClass('ui-state-hover');
	}, function() {
		$(this).removeClass('ui-state-hover');
	});
});
