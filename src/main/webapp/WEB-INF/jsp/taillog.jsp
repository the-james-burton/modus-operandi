<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<%@ include file="/WEB-INF/jsp/includes.jspf"%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>${services.machine}</title>
    <link type="text/css" href="css/smoothness/jquery-ui-1.7.2.custom.css"
    rel="stylesheet" />
<link type="text/css" href="css/style.css" rel="stylesheet" />

    
    <style>
    #logContents {
	    white-space: pre;
	    font-size: 10px;
	    font-family: monospace;
	    background: #ccc;
	    width: 100%;
	    height: 750px;
	    overflow: scroll;
    }
    #logContents div {
        margin-bottom: 1px;
    }
    #logContents div span {
        margin-right: 8px;
        background-color: #eee;
        border-bottom: 1px black dashed;
    }
    input {
        width: 50px;
        text-align: right;
        border: 1px #ccc solid;;
    }
    label {
        float: left;
        margin-right: 5px;
        margin-left: 5px;
    }
    
    </style>
    <script type="text/javascript" src="js/jquery-1.3.2.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-1.7.2.custom.min.js"></script>
	<script>
	var viewLogRefreshId = null;
	var contextDir = '/';
	
	function __viewLog__success(data, textStatus) {
	    $('#logContents').empty().append(data);
	    var d = new Date();
	    var time = (d.getHours() < 10 ? '0' + d.getHours() : d.getHours())
	       + ':' + (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes())
	       + ':' + (d.getSeconds() < 10 ? '0' + d.getSeconds() : d.getSeconds())
	    
	    $('#lastUpdate').text('Last update: ' + time);
	    __hideRefresh();
	}

	function __viewLog__error(XMLHttpRequest, textStatus, errorThrown) {
	    $(this.ui).removeClass('ui-state-highlight').addClass('ui-state-error');
	    __hideRefresh();
	}

	function __viewLog__open(event, parent_ui, windowTitle) {
	      __showRefresh();
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
	}

	function __viewLogRange__open(event, parent_ui, windowTitle, startLine, endLine) {
	          __showRefresh();
	          if (viewLogRefreshId != null) {
		          clearInterval(viewLogRefreshId);
	          }
	        $.ajax( {
	            'url' : contextDir + 'viewlogrange.view',
	            'type' : 'GET',
	            'data' : ( {
	                'windowTitle' : windowTitle,
	                'startLine' : startLine,
	                'endLine' : endLine
	            }),
	            'cache' : false,
	            'success' : __viewLog__success,
	            'error' : __viewLog__error,
	            'ui' : parent_ui
	        });
	    }
    function __viewLogTail__open(event, parent_ui, windowTitle, lastLines) {
        __showRefresh();
        if (viewLogRefreshId != null) {
            clearInterval(viewLogRefreshId);
        }
      $.ajax( {
          'url' : contextDir + 'viewlogtail.view',
          'type' : 'GET',
          'data' : ( {
              'windowTitle' : windowTitle,
              'lastLines' : lastLines
          }),
          'cache' : false,
          'success' : __viewLog__success,
          'error' : __viewLog__error,
          'ui' : parent_ui
      });
  }
    function __viewLogFilter__open(event, parent_ui, windowTitle, filter) {
        __showRefresh();
        if (viewLogRefreshId != null) {
            clearInterval(viewLogRefreshId);
        }
      $.ajax( {
          'url' : contextDir + 'viewlogfilter.view',
          'type' : 'GET',
          'data' : ( {
              'windowTitle' : windowTitle,
              'filter' : filter
          }),
          'cache' : false,
          'success' : __viewLog__success,
          'error' : __viewLog__error,
          'ui' : parent_ui
      });
    }
	function getUrlVars()
	{
	    var vars = [], hash;
	    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
	    for(var i = 0; i < hashes.length; i++)
	    {
	        hash = hashes[i].split('=');
	        vars.push(hash[0]);
	        vars[hash[0]] = hash[1];
	    }
	    return vars;
	}

	$(function() {
		var windowTitle = getUrlVars()['windowTitle'];
		__viewLog__open(null, $('#logContents')[0], windowTitle);
        viewLogRefreshId = setInterval('__viewLog__open(null, null,\'' + windowTitle + '\')', 10000);

        $('#submit').click(function() {
            var start = $('#startLine').removeClass('ui-state-error');
            var end = $('#endLine').removeClass('ui-state-error');
            var tail = $('#lastLines').removeClass('ui-state-error');
            var filter = $('#filter').removeClass('ui-state-error');

            //determine type of search
            if ($('li label[for$=Line]').hasClass('ui-state-search-active')) {
	            if (start.val() > end.val() || start.val() == '' || end.val() == '') {
	                start.addClass('ui-state-error');
	                end.addClass('ui-state-error');
	            } else {
	            	__viewLogRange__open(null, start[0], windowTitle, start.val(), end.val());
	            }
            } else if ($('li label[for=filter]').hasClass('ui-state-search-active')) {
                filter.focus();
                if (filter.val() == '') {
                    filter.addClass('ui-state-error');
                } else {
                    __viewLogFilter__open(null, filter[0], windowTitle, filter.val());
                }
            } else {
                tail.focus();
                if (tail.val() == '') {
                    tail.addClass('ui-state-error');
                } else {
                    __viewLogTail__open(null, tail[0], windowTitle, tail.val());
                }
            }
        });
        $('#submitButton').removeClass('ui-state-disabled').hover(function() {
            $(this).addClass('ui-state-hover');
        }, function() {
            $(this).removeClass('ui-state-hover');
        });
        $('.line-input').removeClass('ui-state-disabled').hover(function() {
            $(this).addClass('ui-state-hover');
        }, function() {
            $(this).removeClass('ui-state-hover');
        });

        $('#startLine').focus(function() {
            $('li label[for$=Lines]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('li label[for$=filter]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('#lastLines').val('');
            $('#filter').val('');
            $('li label[for$=Line]').removeClass('ui-state-disabled').addClass('ui-state-search-active');
        });
        $('#endLine').focus(function() {
            $('li label[for$=Lines]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('li label[for$=filter]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('#lastLines').val('');
            $('#filter').val('');
            $('li label[for$=Line]').removeClass('ui-state-disabled').addClass('ui-state-search-active');
        });
        $('#lastLines').focus(function() {
            $('li label[for$=Line]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('li label[for$=filter]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('#startLine').val('');
            $('#endLine').val('');
            $('#filter').val('');
            $('li label[for$=Lines]').removeClass('ui-state-disabled').addClass('ui-state-search-active');
        });
        $('#filter').focus(function() {
            $('li label[for$=Line]').addClass('ui-state-disabled').removeClass('ui-state-search-active');
            $('li label[for$=Lines]').addClass('ui-state-disabled').removeClass('ui-state-search-active');;
            $('#startLine').val('');
            $('#endLine').val('');
            $('#lastLines').val('');
            $('li label[for$=filter]').removeClass('ui-state-disabled').addClass('ui-state-search-active');
        }).focus();
	});

	function __showRefresh() {
	    $('#refreshing').addClass('refreshing-now');
	}
	function __hideRefresh() {
	    $('#refreshing').removeClass('refreshing-now');
	}
	</script>
</head>



<body>
<div id="main">
<h1 title="${services.machine}">${services.environment} Process Monitor (LOGS)</h1>
<div id="refreshing"></div>

<ul id="icons" class="ui-widget ui-helper-clearfix">
    <li class="ui-state-default ui-corner-all ui-state-disabled line-input"><label for="startLine">Start line<br/><input id="startLine" name="startLine" type="text"/></label></li>
    <li class="ui-state-default ui-corner-all ui-state-disabled line-input"><label for="endLine">End line<br/><input id="endLine" name="endLine" type="text"/></label></li>
    <li class="ui-state-default ui-corner-all ui-state-disabled line-input"><label for="lastLines">Tail lines<br/><input id="lastLines" name="lastLines" type="text"/></label></li>
    <li class="ui-state-default ui-corner-all ui-state-disabled line-input"><label for="filter">Filter<br/><input id="filter" name="filter" type="text"/></label></li>
    <li id="submitButton" class="ui-state-default ui-corner-all ui-state-disabled"><span id="submit" class="ui-icon ui-icon-search"></span></li>
</ul>

<div id="lastUpdate">Last update: ...</div>
<div id="logContents">
${output}
</div>
</div>
</body>
</html>