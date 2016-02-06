function createCookie(name, value, days) {
    var expires;

    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
    } else {
        expires = "";
    }
    document.cookie = encodeURIComponent(name) + "=" + encodeURIComponent(value) + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = encodeURIComponent(name) + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
    return null;
}

function eraseCookie(name) {
    createCookie(name, "", -1);
}

function authenticate(){
	var sessionId = readCookie("sessionId");
	if (!sessionId) {
		window.location = "/login.html";
	} else {
		$.ajax({
			headers : {
				"Authorization" : sessionId
			},
			url : "/session",
			success: initNavbar,
			error : function() {
				window.location = "/login.html";
			}
		});
	}
}

function initNavbar(){
	$("body").prepend('\
			<nav class="navbar navbar-inverse navbar-fixed-top">\
    <div class="container">\
      <div class="navbar-header">\
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">\
          <span class="sr-only">Toggle navigation</span>\
          <span class="icon-bar"></span>\
          <span class="icon-bar"></span>\
          <span class="icon-bar"></span>\
        </button>\
        <a class="navbar-brand" href="/index.html">Newsfeed</a>\
      </div>\
      <div id="navbar" class="collapse navbar-collapse">\
        <ul class="nav navbar-nav">\
          <li><a href="index.html">Feed</a></li>\
          <li><a href="ratings.html">Ratings</a></li>\
          <li><a href="#signout" id="signoutRef">Sign out</a></li>\
        </ul>\
      </div><!--/.nav-collapse -->\
    </div>\
  </nav>');
	
	$("#signoutRef").click(function(){
		eraseCookie("sessionId");
		window.location = "/login.html";
	});
}
