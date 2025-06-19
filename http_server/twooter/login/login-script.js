const Domain = window.location.origin;

const tf = document.getElementById("username");
const sb = document.getElementById("login-but");

tf.addEventListener("keypress",(e)=>{
	if(e.key === "Enter"){sb.click();}
});

function login(){
	
	const bannedChars = "1234567890-=!@#$%^&*()_+`~[]{};':\",./<>?\\|";
	
	const bannedArr = bannedChars.split('');
	
	but = document.getElementById("login-but");
	username = document.getElementById("username");
	
	but.value = "loading";
	but.innerHTML = "loading...";
	but.style.border = "1px green solid";
	but.style.color = "green";
	but.style.animation = "loading 2s ease-in-out 50";
	
	let value = username.value;
	let good = true;
	
	for(let i = 0;  i < bannedArr.length; i++){
		if(value.includes(bannedArr[i])){
			good = false;
			break;
		}
	}
	
	if(good === false){
		but.style.border = "red 1px solid";
		but.style.color = "red";
		but.innerHTML = "nope!";
		setTimeout(()=>{
			but.innerHTML = "please remove all banned charecters";
		},2000);
		but.style.animation = "none";
		return;
	}else{
		but.disabled = true;
		but.style.cursor = "not-allowed";
	}
	
	username = value;
	
	fetch(Domain+"/api/twooter/login",{
		method: "PUT",
		headers:{
			"Content-type":"text/json"
		},
		body:JSON.stringify({
				username
			})
	}).then(rep=>{
		if(!rep.ok){
			console.log("very bad. indeed!");
		}
		return rep;
	}).then(rep=>{
		rep.json().then(data=>{
			console.log(data.name);
			
			if(data.hasOwnProperty("error")){
				but.style = null;
				if(data.error === "username"){
					
					but.style.border = "red 1px solid";
					but.style.color = "red";
					but.innerHTML = "nope!";
					but.style.animation = "none";
					setTimeout(()=>{
						but.innerHTML = "please remove all banned charecters";
					},2000);
					console.log("username error");
				}else{
					but.style.border = "red 1px solid";
					but.style.color = "red";
					but.innerHTML = "ERROR! try reloading";
				}
			}else{
				but.value = "done";
				but.innerHTML = "done!";
				but.style = null;
				window.location = Domain +"/twooter/" + data.location;
			}
		});
		
		
		
	});
	
}