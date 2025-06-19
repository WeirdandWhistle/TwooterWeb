console.log("is a running in a external javascrip file!");
console.log("are cookie enabled: "+navigator.cookieEnabled);
let submited = false;
const Domain = window.location.origin;
function setCookie(name,value,life){
	const date = new Date();
	date.setTime(date.getTime() + (life * 24 * 60 * 60 * 1000));
	let expires = "expires="+date.toUTCString();
	document.cookie = `${name}=${value}; ${expires}; path=/`;
}
function delCookie(name){
	setCookie(name,null,null);
}
function getCookie(name){
	let out = null;
	const full = decodeURIComponent(document.cookie);
	cArray = full.split("; ");
	cArray.forEach(function(val){
		
		arr = val.split("=");
		//console.log(name);
		//console.log(arr);
		//console.log(arr[0]===name);
		//console.log(arr[1]);
		
		if(arr[0] === name){
			//console.log(arr[1]);
			out = arr[1];
		}
	});
	return out;
}
function popup(content,alive,spin){
	
	spin = spin || 5;
	if(alive){
		setTimeout(()=>{popupGone();},alive*1000 + 3000);
	}
	console.log("popup!");
	
	popupDiv = document.getElementsByClassName("popup");
	popupEffect = document.getElementsByClassName("popupAfter");
	
	
	if(content != undefined || content != null){
		popupDiv[0].innerHTML = content;
	}
	
	
	popupDiv[0].style.animationDuration = "3s";
	popupDiv[0].style.animationTimingFunction = "ease-in-out";
	popupDiv[0].style.animationName = "popUp";
	
	popupEffect[0].style.animationDuration = "3s";
	popupEffect[0].style.animationName = "popUp";
	popupEffect[0].style.animationTimingFunction = "ease-in-out";
	popupEffect[0].style.animationIterationCount = "1";
	
	popupDiv[0].addEventListener("animationend",()=>{
		console.log("popup finshed!");
		popupDiv[0].style.top = "50%";
	})
	popupEffect[0].addEventListener("animationend",()=>{
		console.log("popupEffect finshed!");
		
		
		popupEffect[0].style.animationDuration = spin+"s";
		popupEffect[0].style.animationIterationCount = "infinite";
		popupEffect[0].style.animationTimingFunction = "linear";
		popupEffect[0].style.animationName = "spin";
		
		popupEffect[0].style.top = "50%";
		
	})
	
	
}
function popupGone(){
	popupDiv = document.getElementsByClassName("popup");
	
	popupEffect = document.getElementsByClassName("popupAfter");
	
	
	popupDiv[0].style.animationDuration = "3s";
	popupDiv[0].style.animationTimingFunction = "ease-in-out";
	popupDiv[0].style.animationName = "goBye";
	
	popupEffect[0].style.animationDuration = "3s";
	popupEffect[0].style.animationName = "goBye";
	popupEffect[0].style.animationTimingFunction = "ease-in-out";
	popupEffect[0].style.animationIterationCount = "1";
	
	popupDiv[0].addEventListener("animationend",()=>{
		console.log("popup finshed!");
		popupDiv[0].style.top = "200%";
	})
	popupEffect[0].addEventListener("animationend",()=>{
		console.log("popupEffect finshed!");
		popupEffect[0].style.top = "200%";
	})
	
}
function openNav(){
	document.getElementById("sideNav").style.width = "min(15%,300px)";
}
function closeNav(){
	document.getElementById("sideNav").style.width = "0";
}
document.getElementById("submitButton").addEventListener("click",function(){
	var checkbox = document.getElementsByClassName("subBtnImg");
	if(!submited){
		textbox = document.getElementById("textbox");
		text = textbox.value;
		
		fetch(Domain+"/api/add_log",{
			method: "POST",
			body: JSON.stringify({
				text
			}),
			headers:{
				"Content-type": "application/json"
			}
		})
		.then(rep => {
			if(!rep.ok){
				console.log("going!");
			}
			console.log("gone!");
			return rep.json();
		}).then(data =>{
			console.log(data);
		});
		
		
	
			this.classList.add("animate");
			//document.getElementsByClassName("subBtnImg").style.opacity = "1.0";
			console.log(document.getElementsByClassName("subBtnImg"));
		
			//checkbox[0].style.opacity ="1.0";
			checkbox[0].classList.add("animate");
			submited = true;
			textbox.value = "";
			popup('<h1 class="center">congrats</h1><br> <p class="center"> you just sent data to the server(hopefully)</p>',5);
		
		
		
	}else{
		this.classList.remove("animate");
		checkbox[0].classList.remove("animate");
		//void element.offsetWidth;
		submited = false;
	}
	console.log("ran!");
});

document.getElementsByClassName("popup")[0].style.top = "200%";
document.getElementsByClassName("popupAfter")[0].style.top = "200%";
if(!getCookie("gotCookies")===true){
	popup();
}

console.log(getCookie("gotCookies"));

document.getElementsByClassName("okBtn")[0].addEventListener("click",()=>{
	popupGone();
	setCookie("gotCookies",true,30);
	});

var buttonState = 1;
function buttonClicked(){
	switch(buttonState){
		case 1:
		alert("you just clicked a button!");
		buttonState++;
		break;
		case 2:
		alert("you just clicked the button!");
		buttonState++;
		break;
		case 3:
		alert("you just clicked the button again!");
		buttonState++;
		break;
		case 4:
		alert("you just clicked the button again?");
		buttonState++;
		break;
		case 5:
		alert("WHY DO YOU KEEP CLICKING THE BUTTON?!?!");
		buttonState++;
		break;
		case 6:
		alert("Maybe the button wont work next time!");
		buttonState++;
		break;
		case 7:
		alert("Maybe I'll make it blow up the page!");
		buttonState++;
		break;
		case 8:
		window.location.assign("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
		buttonState=1;
		break;
	}
}