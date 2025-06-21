const Domain = window.location.origin;
function openNav(){
	document.getElementById("menu-bar").style.width = "min(15%,300px)";
	document.getElementById("menu-bar").style.left = "0";
}
function closeNav(){
	document.getElementById("menu-bar").style.width = "0";
	document.getElementById("menu-bar").style.left = "-10px";
}
function toggle(node){
	if(node.value === "true"){
		node.value = "false";
		return false;
	}else if(node.value === "false"){
		node.value = "true";
		return true;
	}
}
function toggleHeart(node){
	fill = toggle(node);
	
	if(fill){
		node.innerHTML = "&#9829;";
	}else{
		node.innerHTML = "&#x2661;";
	}
}
function reply(node){
	console.log("should add this system soon");
}
let twoot1 = document.getElementById("twoot=1");

console.log(twoot1);
console.log(twoot1.children);

function getInteractions(arr){
	
	for(let i = 0; i < arr.length;i++){
		//console.log(arr[i]);
		if(arr[i].className === "interactions"){
			return arr[i];
		}
	}
	return false;
	
}
function getBut(twoot,num){
	return getInteractions(twoot.children).children[num].children[0];
}
function runInteraction(but){
	let twoot = but.parentElement.parentElement.parentElement;
	
	if(but.value === "true"){
		but.value = "false";
		console.log("this is where u add a sytem to close things");
		if(but.name === "comment"){
			closeComment(twoot);
			//console.log("you should add a comment system! but right now i am working on the backend");
		}
		console.log("who the hell programed this! ohh...");
	}else{
		but.value = "true";
		if(but.name === "like"){
			getBut(twoot,1).value = "false";
		}
		else if(but.name === "dislike"){
			getBut(twoot,0).value = "false";
		}
		else if(but.name === "comment"){
			if(but.value === "true"){
				openComment(twoot,but);
			}
		}
	}
}
function openComment(twoot,but){
	console.log("opening comments for:",twoot);
	/* 
	twoot.innerHTML += `
	<div name="comments">
		<hr> 
		<div name="comments-container" class="comment-container">
			<div class="comment">
				<span>but there some place hed rather be very many things to talk about and def no reapting that would be stillybut there some place hed rather be very many things to talk about and def no reapting that would be stilly<br>
					<button class="heart name="heart" value="false" onclick="toggleHeart(this);">&#x2661;</button>
					<button class="reply" name="reply" onclick="reply(this);">&#11178;</button>
					<button class="other" name="other" title="other">&#10247;</button>
				</span>
				<div class="comment">
					<span>shoudl thread above<br>
						<button class="heart name="heart" value="false" onclick="toggleHeart(this);">&#x2661;</button>
						<button class="reply" name="reply" onclick="reply(this);">&#11178;</button>
						<button class="other" name="other" title="other">&#10247;</button>
					</span>
					<div class="comment">
					<span>shoudl thread above<br>
						<button class="heart name="heart" value="false" onclick="toggleHeart(this);">&#x2661;</button>
						<button class="reply" name="reply" onclick="reply(this);">&#11178;</button>
						<button class="other" name="other" title="other">&#10247;</button>
					</span>
				</div>
				</div>
				<div class="comment">
					<span>shoudl thread above<br>
						<button class="heart name="heart" value="false" onclick="toggleHeart(this);">&#x2661;</button>
						<button class="reply" name="reply" onclick="reply(this);">&#11178;</button>
						<button class="other" name="other" title="other">&#10247;</button>
					</span>
				</div>
				
			</div>
			<div class="comment">
				<span> this shoudl not thread<br>
					<button class="heart name="heart" value="false" onclick="toggleHeart(this);">&#x2661;</button>
					<button class="reply" name="reply" onclick="reply(this);">&#11178;</button>
					<button class="other" name="other" title="other">&#10247;</button>
				</span>
			</div>
			
		</div>
	</div>`;
	*/
	comments = twoot.children.comments;
	
}
function closeComment(twoot){
	console.log("close comments",twoot);
	twoot.removeChild(twoot.children.comments);
}
function addTwoot(json){
	let twoot = `
	<div id="${json.ID}" class="twoot">
		<div class="who">
			<img class="pfp" src="${Domain+"/img/pfp/"+json.name+".png"}">
			<span class="name">${json.name}</span> 
		</div>
		
		<label style="font-weight:bold; font-size:25px; word-wrap: break-word; word-break: break-word;">
		${json.title}
		</label>
		<br>
		<span>
			${json.message}
		</span>
		<hr>
		<div class="interactions">
						<label class="combo" name="like">
							<button class="butDecor" name="like" onclick="runInteraction(this);" value="false"></button>
							<label class="butLabel" target="like">&#x1F592;</label>
							<span>5M</span>
						</label>
						<label class="combo" name="dislike">
							<button class="butDecor" name="dislike" onclick="runInteraction(this);" value="false"></button>
							<label class="butLabel" target="dislike">&#x1F593</label>
							<span>3k</span>
						</label>
						<label></label>
						<label class="combo" name="comment">
							<button class="butDecor" name="comment" onclick="runInteraction(this);" value="false"></button>
							<label class="butLabel" target="comment">&#x1F5E8;</label>
							<span>comment</span>
						</label>
					</div>
	</div>
	
	`;
	
	let container =  document.getElementById("container");
	
	container.innerHTML += twoot;
}

fetch(Domain+"/api/twooter/twoot-length",{
	method:"GET"
}).then(rep=>{
	return rep.json();
}).then(rep=>{
	console.log(rep);

	if(rep.length >= 1){
		for(let i = 0; i<rep.length;i++){


			let lineTwoot = new URL(Domain);

			lineTwoot.pathname = "/api/twooter/line-twoot";

			lineTwoot.search = 'index='+i;
		
		fetch(lineTwoot,{
			method:"GET"
			
		}).then(out=>{
			return out.json();
		}).then(json=>{
			console.log('json',json);
			addTwoot(json);
		});
		}
	}
});



