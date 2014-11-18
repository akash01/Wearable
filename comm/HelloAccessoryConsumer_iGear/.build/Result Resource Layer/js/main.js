/*    
 * Copyright (c) 2014 Samsung Electronics Co., Ltd.   
 * All rights reserved.   
 *   
 * Redistribution and use in source and binary forms, with or without   
 * modification, are permitted provided that the following conditions are   
 * met:   
 *   
 *     * Redistributions of source code must retain the above copyright   
 *        notice, this list of conditions and the following disclaimer.  
 *     * Redistributions in binary form must reproduce the above  
 *       copyright notice, this list of conditions and the following disclaimer  
 *       in the documentation and/or other materials provided with the  
 *       distribution.  
 *     * Neither the name of Samsung Electronics Co., Ltd. nor the names of its  
 *       contributors may be used to endorse or promote products derived from  
 *       this software without specific prior written permission.  
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS  
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT  
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR  
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT  
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,  
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT  
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,  
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY  
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT  
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE  
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
var SAAgent = null;
var SASocket = null;
var CHANNELID = 104;
var ProviderAppName = "HelloAccessoryProvider";

//imtiaj
var x=0,y=0,z=0, calibrationMood = 1, calibrationMaxCount = 10, calibrationCount = 0;
var tremorX=0, tremorY=0, tremorZ=0, xtremorFactor=1.25, ytremorFactor=10, ztremorFactor=0.7;
var xCoords = [0,0,0,0,0,0,0,0,0,0], yCoords = [0,0,0,0,0,0,0,0,0,0], zCoords = [0,0,0,0,0,0,0,0,0,0], coordsLength = 6;
var xRearAvgCoord=0, yRearAvgCoord=0, zRearAvgCoord=0, xFrontAvgCoord=0, yFrontAvgCoord=0, zFrontAvgCoord=0;
var leftCenterRight = 1, upDown = 6; //[0 1  2] = [left right center],  6 neutral state for 12 musical notes [0 1 ..5 neutral 8 9 ..12]
var verticalState = 0, horizontalState = 1; //0 = neutral, center


function createHTML(log_string)
{
	var log = document.getElementById('resultBoard');
	log.innerHTML = log.innerHTML + "<br> : " + log_string;
}

function onerror(err) {
	console.log("err [" + err + "]");
}

var agentCallback = {
	onconnect : function(socket) {
		SASocket = socket;
		alert("HelloAccessory Connection established with RemotePeer");
		createHTML("startConnection");
		SASocket.setSocketStatusListener(function(reason){
			console.log("Service connection lost, Reason : [" + reason + "]");
			disconnect();
		});
	},
	onerror : onerror
};

var peerAgentFindCallback = {
	onpeeragentfound : function(peerAgent) {
		try {
			if (peerAgent.appName == ProviderAppName) {
				SAAgent.setServiceConnectionListener(agentCallback);
				SAAgent.requestServiceConnection(peerAgent);
			} else {
				alert("Not expected app!! : " + peerAgent.appName);
			}
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
	},
	onerror : onerror
}

function onsuccess(agents) {
	try {
		if (agents.length > 0) {
			SAAgent = agents[0];
			
			SAAgent.setPeerAgentFindListener(peerAgentFindCallback);
			SAAgent.findPeerAgents();
		} else {
			alert("Not found SAAgent!!");
		}
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function connect() {
	if (SASocket) {
		alert('Already connected!');
        return false;
    }
	try {
		webapis.sa.requestSAAgent(onsuccess, function (err) {
			console.log("err [" + err.name + "] msg[" + err.message + "]");
		});
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function disconnect() {
	try {
		if (SASocket != null) {
			SASocket.close();
			SASocket = null;
			createHTML("closeConnection");
		}
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

function onreceive(channelId, data) {
	createHTML(data);
}

function fetch() {
	try {
		SASocket.setDataReceiveListener(onreceive);
		SASocket.sendData(CHANNELID, "Hello " +"X:" + zx + " Y:" + leftCenterRight + " Z:" + upDown);
	} catch(err) {
		console.log("exception [" + err.name + "] msg[" + err.message + "]");
	}
}

window.onload = function () {
    // add eventListener for tizenhwkey
    document.addEventListener('tizenhwkey', function(e) {
        if(e.keyName == "back")
            tizen.application.getCurrentApplication().exit();
    });
    
    //imtiaj
    window.addEventListener('devicemotion', function(e) {
		rotz = e.rotationRate.alpha ;
		rotx = e.rotationRate.beta ;
		roty = e.rotationRate.gamma ;
	
		//
		angleX = - (rotx * Math.PI / 180.0);
		angleY = - (roty * Math.PI / 180.0);
		angleZ = - (rotz * Math.PI / 180.0);
		
		ax = e.acceleration.x;
		ay = e.acceleration.y;
		az = e.acceleration.z;
		
		
		//X axis rotation
		xy = ay*Math.cos(angleX) - az*Math.sin(angleX);
		xz = ay*Math.sin(angleX) + az*Math.cos(angleX);
		xx = ax;
		//Y axis rotation
		yz = xz*Math.cos(angleY) - xx*Math.sin(angleY);
		yx = xz*Math.sin(angleY) + xx*Math.cos(angleY);
		yy = xy;
		
		//Z axis rotation		
		zx = yx*Math.cos(angleZ) - yy*Math.sin(angleZ);
		zy = yx*Math.sin(angleZ) + yy*Math.cos(angleZ); 
		zz = yz;
		
		//update data array
		for(var i = 1; i < coordsLength; i++)
		{
			xCoords[i-1] = xCoords[i];
			yCoords[i-1] = yCoords[i];
			zCoords[i-1] = zCoords[i];
		}
		xCoords[coordsLength-1] = zx;
		yCoords[coordsLength-1] = zy;
		zCoords[coordsLength-1] = zz;
		
		
		if(calibrationMood == 1)
		{
			calibrationCount++;
			tremorX = (tremorX + Math.abs(zx))/2;
			tremorY = (tremorY + Math.abs(zy))/2;
			tremorZ = (tremorZ + Math.abs(zz))/2;
			if(calibrationCount > calibrationMaxCount)
			{
				calibrationMood = 0;
				tremorX = tremorX * xtremorFactor;
				tremorY = tremorY * ytremorFactor;
				tremorZ = tremorZ * ztremorFactor;
			}
		}
		else
		{
			//update rear and front values
			xRearAvgCoord = yRearAvgCoord = zRearAvgCoord = xFrontAvgCoord = yFrontAvgCoord = zFrontAvgCoord = 0;
			for(var i = 1; i < coordsLength/2; i++)
			{
				xRearAvgCoord += xCoords[i];
				yRearAvgCoord += yCoords[i];
				zRearAvgCoord += zCoords[i];
			}
			for(var i = coordsLength/2; i < coordsLength; i++)
			{
				xFrontAvgCoord += xCoords[i];
				yFrontAvgCoord += yCoords[i];
				zFrontAvgCoord += zCoords[i];
			}
			
			xRearAvgCoord /= coordsLength;
			yRearAvgCoord /= coordsLength;
			zRearAvgCoord /= coordsLength;
			
			xFrontAvgCoord /= coordsLength;
			yFrontAvgCoord /= coordsLength;
			zFrontAvgCoord /= coordsLength;
			
			tmpY = Math.round(Math.abs(yRearAvgCoord - yFrontAvgCoord) / tremorY) ;
			//FSM for left right center
			switch(horizontalState)
			{
				case 1: //neutral, Center state, Mid octave
					if(tmpY > 0)
					{
						if(yFrontAvgCoord > 0) //left direction
						{
							horizontalState = 0; //left motion
						}
						else 
						{
							horizontalState = 2; //right motion
						}
							
					}
					break;
					
				case 0: //left state, Low octave
					if(tmpY > 0)
					{
						if(yFrontAvgCoord < 0) //right direction
							horizontalState = 1;
					}
					break;
					
				case 2: //right state, High Octave
					if(tmpY > 0)
					{
						if(yFrontAvgCoord > 0) //right direction
							horizontalState = 1;
					}
					break;					
			}
			
			leftCenterRight = horizontalState;
			
			//update leftRight and upDown
			
			tmpZ = Math.round(Math.abs(zRearAvgCoord - zFrontAvgCoord) / tremorZ) ;
			
			//FSM for right motion
			switch(verticalState)
			{
			case 0: //neutral state
				if(tmpZ > 0)
				{
					if(zFrontAvgCoord > 0) //down direction
					{
						tmpZ *= -1;
						verticalState = 1; //down motion
					}
					else 
						verticalState = 2; //up motion
				}
				break;
				
			case 1: //down motion
				if(tmpZ == 0)
					verticalState = 0; //neutral, no motion
				else if(zFrontAvgCoord > 0) //down direction
					tmpZ *= -1;
				break;
				
			case 2: //up motion
				if(tmpZ == 0)
					verticalState = 0; //neutral, no motion						
				break;		
				
			}
			//update left right center
			
			upDown += tmpZ;
			if(upDown > 12) upDown = 12;
			else if(upDown < 0) upDown = 0;
			
			
		}
				
	}); 

	window.addEventListener("deviceorientation", function(e){
		//document.getElementById("rotx").innerHTML ='alpha value '+ Math.round(e.alpha);
		/*betaElem.innerHTML = 'beta value '+ Math.round(e.beta);
		gammaElem.innerHTML = 'gamma value '+ Math.round(e.gamma);*/
	}, true);
    
    
    
};
