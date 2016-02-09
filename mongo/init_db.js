use newsfeed;
var authorsPool = ["minkov", "gosho", "pesho", "maya", "tosho"]
var hashtags = ["mongo", "nosql", "newsfeed", "justtaged", "whatever"]
for(var author in authorsPool){
	db.user.insert(
			{
				username: authorsPool[author], 
				password: "plaintext", 
				avatar: [], 
				silenced: []
			}
		);
}

function shuffle(array) {
	  var copy = [], n = array.length, i;

	  while (n) {
	    i = Math.floor(Math.random() * n--);
	    copy.push(array[i]);
	  }

	  return copy;
	}

function someAuthorsIds(){
	var someIds = [];
	shuffled = shuffle(authorsPool);

	rand = Math.floor(Math.random() * authorsPool.length);
	for(var i = 0; i < rand; i++){
		someIds.push(DBRef("user", db.user.findOne({username: authorsPool[i]})["_id"]));
	}

	return someIds;
}

function someMentions(){
	var result = "";
	var randAuthors = Math.floor(Math.random() * authorsPool.length);
	for(var i = 0; i < randAuthors; i++){
		result += " @" + authorsPool[i]; 
	}
	
	return result;
}

function someTags(){
	var result = "";
	var randTags = Math.floor(Math.random() * hashtags.length);
	for(var i = 0; i < randTags; i++){
		result += " #" + hashtags[i]; 
	}
	
	return result;
}

function escapeHtml(unsafe) {
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;")}

for(var i = 0; i < 500; i++){
	var author = db.user.findOne({username: authorsPool[Math.floor(Math.random() * authorsPool.length)]});
	db.message.insert({
		author: 
			DBRef("user", author._id),
			//{$id: author._id, $ref: "user"},
		text: escapeHtml("text message " + i + " mentions" + someMentions() + " " + someTags()),
		date: new Date(new Date() - (Math.random() * 84600 * 1000 * 7)),
		likes: someAuthorsIds()
	})
}

db.message.createIndex(
	{
		date: -1
	}
)
		
db.message.createIndex(
	{
		text: "text"
	}
)


function getFeed(){
	return db.message.find().sort({date: -1}).limit(20);
}

function whatever(){
	return db.message.find({text: {$regex: 'reg.ex', $options: "ims"}});
}

function removeField(){
	return db.user.update({username:"veliko"}, {$unset: "somefield"})
}

function executeCode(){
	return db.user.update({username:"veliko"}, {password: {$where: function(value){md5hash(value)}}})
}

function isPeshoSilenced(){
	return db.user.find({username: "veliko"}, {silenced: {$elemMatch: DBRef("user", ObjectId("56ba12fe1fe155c1100e4a2e"))} })
}

function addSilenced(){
	db.user.update({username:"veliko"}, {$push:{silenced: DBRef("user", ObjectId("56ba12fe1fe155c1100e4a2e"))}})
}

function removeSilencedWOMaya(){
	db.user.update({username:'veliko'}, {$pull:{silenced: {$ne: DBRef("user", ObjectId("56ba12fe1fe155c1100e4a2e"))}}})
}

function randomizeMessagesTimes(maxDaysBack){
	db.message.find({}).forEach(function(mydoc) {
		  db.message.update({_id: mydoc._id}, {$set: {date: new Date(new Date() - (Math.random() * 84600 * 1000 * maxDaysBack))}})
	})
}

function string2Bin(str) {
	  var result = [];
	  for (var i = 0; i < str.length; i++) {
	    result.push(str.charCodeAt(i) + "");
	  }
	  return result;
	}

function setSomeAvatars(){
	db.user.find({}).forEach(function(mydoc) {
		  db.user.update({_id: mydoc._id}, {$set: {avatar: BinData(0,"aVZCT1J3MEtHZ29BQUFBTlNVaEVVZ0FBQUNZQUFBQXJDQVlBQUFBVW8vcHdBQUFBQm1KTFIwUUEvd0QvQVArZ3ZhZVRBQUFBQ1hCSVdYTUFBQ" +
 "XNUQUFBTEV3RUFtcHdZQUFBQUIzUkpUVVVINEFJSkVCWTQ5ZVFEOFFBQUNObEpSRUZVV01QdG1GMk1WT1VaeDMvUGM4N3M3QWZzeml5SWdIenNzSWFJSlREUWlOWkVabFpybTJpQ2FOdWtpZDdnVFMrTWRxSDBvaGV" +
 "GWFM3YktKQTJOalZwNWFJM1hGUjIwL1NpU1pFVms4WmdaV1l4L1VocllMZGlHMjFrUnFBQ08zUGVweGZ2bWJNenM3TXJUVzlNMHpjNU9UUG4vZm8vMy8vM0ZZQ0RCdysrZC9iczJlRno1ODRoUUtBaFNJaHpEZ0JWR" +
 "UNLSUlrSUJNN2k3RDQ3c1djT1hOOXpreGU0WEdCOGZSMFF3cytTZHlXUTRmUGd3TDF6Y1QxMGdVbkFDQUdLZ0pvaUJZRGdCaS90QTBGZGVlZVVaRVJsKzU1MTNDRFZBUmYzT0ZnR0dBS3FLaXh3YVFOMmdEM2hncUp" +
 "lZDY3cVEyalhlZU9NTm1wdVpBVkN0VmpsdzRBQVBYeWp3cDJmTG1JREZHL3RITVZHY1NCTW9BRU1IQndkLyt1YWJieEpGRWFwS01nOUhJS0Rpc0tpR2lDRUlLV0IxTnp5eE0wTWYxd0FvbDhzdGdFUWtlWnNaWjgrZ" +
 "UpaL1A4OE9CTVdSd0U0WmlCQmlDUVJzbzMvVHk1Y3U5YjczMUZsMWhDdWZjL0tLQTRNQWNtS0VhVURkSUE0OXM2V1B6WUowME4vanJNK2VvVnFzdGdCb0EyOTlqWTJOcy9abmo5M3QrTzQ5QUhBMDlOdHJKN2EraUo" +
 "wK2VCRWo4S1VFczdXSUlhU2RzNkJHK3NyV0hmcjFPNEc0eFBUM2RZa0tSRHVJM3RabVpHVVpHUm5qNjB1TXd1SEVCS0lESnlVbjAzWGZmUlpGRXFzU1NUV1lSRVJRaGplUFJML1N3b2QvUkd6alVYQXV3WnUwa2F5M" +
 "ENkSEp5a3JWSFArYms5bGRiT3daelRFeE1vTFc1T1Z3YmFvbmxTRXdEYUJTeHVSOTJiUkJXZGdGek5YQXdOVFcxcEliYWdUYURyVmFyN051M2o4ZktSVDc4OWlWdnh2V0h2Q0p1emMwUmlDYU9pdG1DeGRSZ0dZNzc" +
 "xME11RTlBck5WTGlFSjEzL01VMDFFbGo3ZXRQVFUyUnkrWDRRV2FNNDhlUGU1ZFF4SkpvaklFMXpOY0FtM0tPcmN1TlozZWxlV0puaG1WekZWSmE0M2Q3WG1ka1pLU2pwaEpCMjM1MzZ1L1VOQWlDRm9kWDlkb2psb" +
 "FNkc1Z5TTRXV3djL01kcE8wV29kUXhzd1grMVI2WjJXeDJVYjliQ3BTM2tzMm52RFo5ZzBFWHhvREJ0bHlXZGRrVVFlMEdnbVBPdkFtYWMxYjdob1ZDZ1RObnpqQTBOTFFBMUdkRnI3WkU0Z0xVampUR3FpNmxjTjl" +
 "hd3B1ZjBFV051dmtKNVhLNUpWZTFiMWdvRkNnVUNwUktKY2JHeGxwU1Nuc3k3Z2lzVTVjWXBNd2h3RU83N3VTTzdqblM3aVlCRGpNSU56L003T3pza2liSzUvT0lDQU1EQXh3NmRJaFNxY1RJeUVqTG1NVk1xdExSa" +
 "ktEeGhIWExVK3dZN3FHM2RvMDBjOVFCVWZqTm11Y1hTTjJ1c1dLeG1Id1RFZkw1UEtkUG4rYm8wYU5rTXBtbFRkbFJXNERpVUdEN3BtN1c5ZFRwNDFPd09nUWVlSE9hNkNSMVBwOWZZQ3F6Q0JGaGRIU1VpeGZmWSs" +
 "vZVBmUGxyOGxYVzN5czJZU0tJd0pXOWtKdWhaSExLR0g5WDlTQktQTFRHb3lpM1VjYUlKdE5abWJNWHBwQlpENERaTE1yT1BYYUpLKy9Qc1hHb2FFRnZxY2RzelZHQU56UkN3L21CdWl6cTRoRU1WY1RCRjNBS05wQ" +
 "mJ0Kyt2VVVMSTQ4OHpQNzkrNmxVUG9sSE9CREh5RWlCVW1tYTBkSFJGck56NXN3Wks1VksxcVdCcFNXd3RJaUZncTBRN0xsdDNUWjdjSTE5ZWlCbFYxL0FidXpIYm42bnk4cmxzc1dWeXJNV0VST1JsbStsVXNrYXJ" +
 "WcXRlbG9vV0NZemFLZE9uVEt6S082TnpNVy95K1d5NWZQNWhBbFpzOFM3ZCs5R1JOaXhZd2Y5L2YwdFpqRXpjcmtjVTFOVDdOdTNiMUhIeldhelhMbHlKZmsvTVRIQmsxOTdFaHlvajJ2Mjd0M0RTOGVPa3RzNHRHR" +
 "CtzV1BIb0YzU2RrMzhKOThiVDdGWU5PZWNtWms1NTJ4OGZOd2FzUzVnRXZQRGJIYkFqcjEwMU13UFRlWlk3QjkydTg5bkFXcU1PWHo0Y010R3hXTFJoTkFUVnRFbTR1cUI3dGl4dzg2ZlAyL05qZjhHeEdMUHhNUkV" +
 "5eWFaVE1ZRU5SU1QxQWFUOE01NVlBMndZS09qbzFhcFZHNFBXRHZBMndGODVjcVZSR09sVXFtcEx6UUc5eHJETDFzdzhIalRkelZGVEZEYk9MVEpYcHY0cGVsU25La1RDMWdzUFRUYTBOQVEyV3cybWQrY2lJVTY5T" +
 "jZEeURLaWxkOUUxbzhqNlkyQXc0bGh3T3pzTEUvdC9ZYlBZNTJxL1dJY3FoUDQ1djdtak45S2pkU0hmOCtXNUZRazNadXc5VWRnNWRNSWZmSGhKd0ljMnFoWmkybW10YVRZa216VXpDZ1VDaTJnRzlSYmNOQzlIblZ" +
 "4VFpNQWh5QUVrUDBxdHZFSTFuZGZYTGlWc0ZLcEFEQTlQVTJsVXFGYXJTWlNsc3RsS3BVS3FzcWxTNWVZbVpueGg5K21FMVVuUnRFc1VGSWhBRWx0d29uRnNlaHdHQ2FHUklLRnE1QzF6MkhYeS9EeEw1QWtBbTZEd" +
 "kRXUGF3amduRU5WRTNERllyR0Z5ODlUYjRVN3Y0VXUveEptUnY5QUR4TXZQOFMrNzc3TjdEK3FTU2tVTVNTNlJkaUpFaS9GTWh2Zk1wbk1Bck8xdDJicUxZQjFyY2FKSVFUczNKS2h1R3MxMDc5K2xCZFAvSVVqeHk" +
 "rZ2hCaDFuSFNqSjA2Y1lHcHFLbEg1VXFBV0kzV0xhYnIxYU9lUTlEQ1lBa3JoL2xXWXdQTGwzWXcvdjVYenYzcU1iZmNPK0QwMG9MVlcrdXNNaXNYZENJNkJ6Q0Q1L0U2RU9rNmd1SHNrSmlRT0U1OGFmSzF6SFlsS" +
 "0xwZGpabWJHLyttOUY5WitENGxwNldzL0tmREVJMnM4RmJJSUpNQ0FILzM4ejR6OWVEckdrc0RUK2ZzRTh3QlFBZWVIK01rUkV2ZFpQTjVQZDBzZjR3YWZRck5QeFRjK1J1WDgxeGxZM3AwY0Z3MkhtS2ZIRnorNDB" +
 "jeGdGUkhud1ZqTWx3Q2NOWGJBTEFJREV3OUt4Q0ZpZnRGRnpKbVlQNzBCeEdFWXVidVdNYkNzRzJtYUowNko0bXVxVFhmMUVOcTY3NE9ybzdWWlhEUUgwWWRRK3lmcVFvZyt3dFUrV3Boc1krQ2VUa1lkZzJaQjVVa" +
 "HY5Tk5FMkhaUEJwRTZXSWlKcndpbUlVSERUWWdJNlI0R1ZWeDByNzhMY0lLSytlUW5FVW9LWndMdUt0VGVSeU9MalpqQzNib0lkaDBJdmVhYzg1cHR4aCtmVVNWY2tWRDN3b05yd1FYenB5QUxrY2FsbmdsSWlsQXR" +
 "ST29SVHRYbkVZMXdUa0FWc3k1dlBqVUlsb0hlRGRJTjFQeFJwVzh6V0EwaDVjZDF1Q09idjVqeGdwb0pYOXpjRCtvRmtEaHVHajRxc2RiQ3dnT3J3RUxPdlAzM2VTa2tCRmREeE5jM01mK1llQUFDbUhPb3FQK21Eb" +
 "kZCREtaaFJsMTRnMlJkcURsMlA3QTYxcWJ6b016bk5zVDdyNWpPWi81R3lGLzY0Qm96NzMrS0JjcUZQM3hJOWJwaEZuSDFhcDBMZjZ3UXFVUGpxTHg0K1NaLysrQjZzdGpTQ2Rxbm1HMWJWbENlZkF3eGg0bGZSMHd" +
 "UWDRNUVp6UUQrM3cxNVhQYS9nL3Nmd2JZdndFNDhrbXcvbzNHL3dBQUFBQkpSVTVFcmtKZ2dnPT0="
 )}})
	})
}
