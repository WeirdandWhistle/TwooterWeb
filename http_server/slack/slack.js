console.log("loaded!");
const Domain = window.location.origin;

function sendData(){
	messageData = document.getElementById("messageData");
	
	text = messageData.value;
	
	
	fetch(Domain+"/api/slackbot/post-message",{
		method:"POST",
		headers:{
			"Content-type":"text/json"
		},
		body: JSON.stringify({
			text
		})
	}).then(rep=>{
		if(!rep.ok){
			console.log("not good :sad-face:");
		}
		return rep.json();
	}).then(rep=>{
		console.log(rep);
	})
	messageData.value = '';
}

function mainPage(){
	console.log(";");
	window.location.assign(Domain+"/");
	console.log(Domain+"/");
}