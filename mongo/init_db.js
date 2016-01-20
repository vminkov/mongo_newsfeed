use newsfeed;
db.user.insert(
	{
		username: "veliko", 
		password: "plaintext", 
		avatar: "somebase64==", 
		silenced: [
		           {$id: "another", $ref: "user"},
		           {$id: "and another", $ref: "user"}
		]
	}
)
db.message.insert(
	{
		author: {$id: "veliko's id", $ref: "user"}, 
		text: "my text message", 
		date: new Date(), 
		likes: [{$id: "another", $ref: "user"}]
	}
)

db.message.createIndex(
	{
		date: -1
	}
)
		
db.message.createIndex(
	{
		author: "text"
	}
)

var authorsPool = ["minkov", "gosho", "pesho", "maya", "tosho"]
for(var author in authorsPool){
	db.user.insert(
			{
				username: authorsPool[author], 
				password: "plaintext", 
				avatar: "somebase64==", 
				silenced: []
			}
		);
}

function shuffle(array) {
	  var copy = [], n = array.length, i;

	  // While there remain elements to shuffle…
	  while (n) {

	    // Pick a remaining element…
	    i = Math.floor(Math.random() * n--);

	    // And move it to the new array.
	    copy.push(array[i]);
	  }

	  return copy;
	}

function someAuthorsIds(){
	var someIds = [];
	shuffled = shuffle(authorsPool);

	rand = Math.floor(Math.random() * authorsPool.length);
	for(var i = 0; i < rand; i++){
		someIds.push({$id: db.user.findOne({username: authorsPool[rand]})["_id"], $ref: "user"});
	}

	return someIds;
}

for(var i = 0; i < 500; i++){
	var author = db.user.findOne({username: authorsPool[Math.floor(Math.random() * authorsPool.length)]});
	db.message.insert({
		author: {$id: author._id, $ref: "user"},
		text: "text message " + i,
		date: new Date(),
		likes: someAuthorsIds()
	})
}

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
	return db.user.find({username: "veliko"}, {silenced: {$elemMatch: "pesho"} })
}

function addSilenced(){
	db.user.update({username:"veliko"}, {$push:{silenced: "maya"}})
}

function removeSilencedWOMaya(){
	db.user.update({username:'veliko'}, {$pull:{silenced: {$ne: "maya"}}})
}

