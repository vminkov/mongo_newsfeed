var currentPage = 0;
var authorsCache = {};

function updateAuthorsData(messages){
	if(!messages || !messages.length){
		currentPage--;
		reloadFeed();
		return;
	}
	
	var authorsIds = [];
	for(eachMessage in messages){
		authorsIds.push(messages[eachMessage].authorId);
	}
	
	$.ajax({
		url : "/user/profile/batch",
		method : "GET",
		data: {ids:authorsIds},
		headers : {
			"Authorization" : readCookie("sessionId")
		},
		success : function(data){
			authorsCache = data;
			
			for(eachMessage in messages){
				$('#' + messages[eachMessage]._id + ' td:first-child').html(getAuthorCell(messages[eachMessage].authorId));
			}
			
			$(document).off('click', ".silenceButton");
			$(document).on('click', ".silenceButton",function(e){
	            e.preventDefault(); 
				var authorId = $(this).parents(".messageRow").children("td.authorCell").children("div").attr('class');
				silence(authorId);
			});
			$(".authorCell").hover(function(){
					$(this).find(".glyphicon-remove-circle").show();
				},
				function(){
					$(this).find(".glyphicon-remove-circle").hide();
			});
		},
		error : function(data) {
			alert(JSON.parse(data.responseText)["message"]);
		}
	});
}	

function getAuthorCell(authorId){
	var avatar = "";
	if(authorsCache[authorId] && authorsCache[authorId].avatar){
		avatar = "<img class='avatar' style='height: 30px; width: 30px;' src='data:image/jpeg;base64," + authorsCache[authorId].avatar + "'/>";
	}

	return "<div class='" + authorId + "'>"
		+ avatar 
		+ "<span style='margin-left: 8px;'><a href='/profile.html?userId=" + authorId + "'>" + authorsCache[authorId].username + "</a></span>"
		+ "<a class='silenceButton' href='#'><span style='display: none;' class='glyphicon glyphicon-remove-circle'/></a>"
		+ "</div>";
}

function getUserCell(username){
	return "<div style='margin: 5px;'>"
		+ "<span style='margin-left: 8px;'><a href='/profile.html?user=" + username + "'>" + username + "</a></span>"
		+ "</div>";
}

function htmlDecode(input){
	  var e = document.createElement('div');
	  e.innerHTML = input;
	  return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
	}


function displayMessageText(message){
	var tagsPattern = /\B#[a-z0-9_-]+/gi;
	var htmlDecoded = htmlDecode(message);
	if(htmlDecoded){
		var tags = htmlDecoded.match(tagsPattern);
		for(each in tags){
			message = message.replace(tags[each], "<a href='/hashtag.html?tag=" + 
					tags[each].substring(1) + "'>" + tags[each] + "</a>"); 
		}
	}

	var mentionsPattern = /\B@[a-z0-9_-]+/gi;
	var mentions = message.match(mentionsPattern);
	for(each in mentions){
		message = message.replace(mentions[each], "<a href='/profile.html?user=" + 
				mentions[each].substring(1) + "'>" + mentions[each] + "</a>"); 
	}
	
	return "<td>" + message + "</td>";
}

function displayLikes(message){
	var messHtml = "";
	messHtml += "<td class='likesCell'>";
	if(message["liked"]){
		messHtml += "<a class='unlike' href='#'><span class='glyphicon glyphicon-thumbs-down'/></a>"; 
	}else{
		messHtml += "<a class='like' href='#'><span class='glyphicon glyphicon-thumbs-up'/></a>"; 
	} 
	
	messHtml += "<a href='#' style='margin-left: 8px;' class='likesCount'>" + message.likes.length + "</a>";
	
	var likesUsers = "<div style='margin: 5px;'>People who liked it:</div><hr></hr>";
	for(var each in message.likes){
		likesUsers += getUserCell(message.likes[each]);
	}

	messHtml += '<div class="modal fade" tabindex="-1" role="dialog">\
					<div class="modal-dialog">\
						<div class="modal-content">\
							' + likesUsers + '\
						</div>\
					</div>\
				</div>';

	return messHtml + "</td>";
}

function displayMessages(messages) {
	var body = $('#feed > tbody');
	body.children().remove();

	for (mess in messages) {
		if(!authorsCache[messages[mess].authorId]){
			authorsCache[messages[mess].authorId] = {username: messages[mess].author, authorId: messages[mess].authorId};
		}
		
		var messHtml = "<tr class='messageRow' id='" + messages[mess]["_id"] + "'>";
		messHtml += "<td class='authorCell' style='vertical-align: middle;'>" + getAuthorCell(messages[mess].authorId) + "</td>";
		messHtml += "<td>" + displayMessageText(messages[mess].text) + "</td>";
		messHtml += displayLikes(messages[mess]);
		
		var dateInUTC = new Date(messages[mess].date + 2 * 3600 * 1000).toISOString();
		messHtml += "<td style='width: 80px;'><div class='date'>" + prettyDate(dateInUTC) + "</div></td>";
		
		messHtml += "</tr>";
		
		body.append(messHtml);
	}
	
	$(".like").click(function(e){
        e.preventDefault(); 
		var messageId = $(this).parents(".messageRow").attr('id');
		vote(messageId, 'like');
	});
	$(".unlike").click(function(e){
        e.preventDefault(); 
		var messageId = $(this).parents(".messageRow").attr('id');
		vote(messageId, 'unlike');
	});
	$(".likesCell").hover(function(){
			$(this).find(".icon-question-sign").show();
		},
		function(){
			$(this).find(".icon-question-sign").hide();
	});	
	$(".likesCount").click(function(e){
        e.preventDefault();
        $(this).next().modal('toggle');
	});
	
	updateAuthorsData(messages);
}

function vote(messageId, vote){
	$.ajax({
		url : "/message/" + messageId + "/" + vote,
		method : "POST",
		headers : {
			"Authorization" : readCookie("sessionId")
		},
		success : reloadFeed,
		error : function(data) {
			alert(JSON.parse(data.responseText)["message"]);
		}
	});
}

function silence(userId){
	$.ajax({
		url : "/user/" + userId + "/silence",
		method : "POST",
		headers : {
			"Authorization" : readCookie("sessionId")
		},
		success : reloadFeed,
		error : function(data) {
			alert(JSON.parse(data.responseText)["message"]);
		}
	});
}

function postMessage(messageText) {
	$.ajax({
		url : "/message",
		method : "POST",
		contentType: "application/json",
		data : JSON.stringify({
			text : messageText
		}),
		headers : {
			"Authorization" : readCookie("sessionId")
		},
		success : reloadFeed,
		error : function(data) {
			alert(JSON.parse(data.responseText)["message"]);
		}
	});
}

$(document).ready(function(){
	$("#message").keypress(function (e) {
		  if (e.which == 13) {
			  postMessage(this.value);
			  this.value = "";
			  return false;
		  }
	});
	$("#previousPage").click(function(){
		if(currentPage > 0){
			currentPage--;
			reloadFeed();
		}
	});
	$("#nextPage").click(function(){
			currentPage++;
			reloadFeed();
	});
});
