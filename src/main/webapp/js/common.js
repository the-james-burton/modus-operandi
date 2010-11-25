/**
 * Global variable storing the mark-up for a generic error message.
 */
var errorMarkup;

/**
 * The context directory for this web application.
 */
var contextDir = '/';

/**
 * Global array containing event handlers to be invoked when a page refresh
 * is successful.
 */
var __refreshSuccessHandlers = new Array();
function __addEventToRefreshSuccess(handler) {
	__refreshSuccessHandlers.push(handler);
}

function __runAllRefreshSuccessHandlers() {
	for (i=0; i < __refreshSuccessHandlers.length; i++) {
		__refreshSuccessHandlers[i].call();
	}
}

function __generic__error(XMLHttpRequest, textStatus, errorThrown) {
	$(this.ui).removeClass('ui-state-highlight').addClass('ui-state-error');
	__hideRefresh();
}

function __refresh__success(data, textStatus) {
	$('#refreshButton').removeClass('ui-state-active');
	$('#serviceDetails').html(data);
	__runAllRefreshSuccessHandlers();
}

function __refresh__error(XMLHttpRequest, textStatus, errorThrown) {
	$('#refreshButton').addClass('ui-state-error');
	$('#resultsTableContainer').empty().append(errorMarkup);
	__hideRefresh();
}

function __showRefresh() {
	$('#refreshing').addClass('refreshing-now');
}
function __hideRefresh() {
	$('#refreshing').removeClass('refreshing-now');
}

function refresh(type) {
	$('#refreshButton').addClass('ui-state-active').removeClass('ui-state-error');
	var url = contextDir + 'services.view';
	$.ajax( {
		url : url,
		type : type,
		data : {
			pid : -1,
			windowTitle : '',
			rand : Math.random(),
			ajax : true
		},
		cache : false,
		success : __refresh__success,
		error : __refresh__error
	});
	__showRefresh();
}

$(function() {
	errorMarkup = $('#errorMessage > div');
	setInterval(function() { refresh('GET');}, 10000);
	
	$('#validXHTML').fadeIn('slow');
	__addEventToRefreshSuccess(__hideRefresh);
});