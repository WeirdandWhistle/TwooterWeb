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
		console.log(rep);
	});
}