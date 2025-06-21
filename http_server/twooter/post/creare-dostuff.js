const Domain = window.origin;
function post(){
	console.log(document.getElementById("content").innerHTML.replaceAll("<div>","").replaceAll("</div>","<br>"));
	const titleIn = document.getElementById("title");
	const but = document.getElementById("post-but");
	
	
	if(titleIn.value.length < 1){
		titleIn.style.borderColor = "red";
		return;
	}else{
		titleIn.style.borderColor = "green";
	}
	but.style.cursor = "not-allowed";
	but.disabled = true;
	
	let message = document.getElementById("content").innerHTML;
	let title = titleIn.value;
	fetch(origin+"/api/twooter/twoot",{
		method:"POST",
		body: JSON.stringify({
			message,
			title
		})
	}).then(rep=>{
		return rep.json();
	}).then(rep=>{
		console.log("getting there",rep);
		
		fetch(Domain+"/api/twooter/get-twoot",{
			method:"GET",
			headers:{
				"ID":rep.ID
			}
		}).then(rep=>{
			return rep.json();
		}).then(json=>{
			console.log("get request",json);
		});
	});
	
	
}
function openNav(){
	document.getElementById("menu-bar").style.width = "min(15%,300px)";
	document.getElementById("menu-bar").style.left = "0";
}
function closeNav(){
	document.getElementById("menu-bar").style.width = "0";
	document.getElementById("menu-bar").style.left = "-10px";
}