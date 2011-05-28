/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/
+ Env {

	addToSynthiArgs{|synthi, symbol|
//		this.debug(symbol);
		synthi.argEnvs = synthi.argEnvs.add(symbol);
		synthi.argEnvs = synthi.argEnvs.add(this);
	
	}
	
	cutToTime{|time| //was cutEnv
		var pointTime, lev, times;
		pointTime = this.getEnvPoint(time);
		lev = this.levels.copyRange(pointTime[0], this.levels.size-1);
		lev = [pointTime[2]] ++ lev;
		this.levels = lev;
		times = [pointTime[1]] ++ this.times.copyRange(pointTime[0], this.times.size-1);
		this.times = times;
	}
	
	getEnvPoint{|time|
		var tempDur = 0, timeToNextPoint = 0, val=0;
		this.times.do({ arg delta, i;
			if(tempDur > time){
				if(this.shapeNumber(this.curves) == 2){
					val = (time / (tempDur - time)).linexp(0,1,this.levels[i], this.levels[i-1]);
				}{
					val = (time / (tempDur - time)).linlin(0,1,this.levels[i], this.levels[i-1]);
				};
				(this.shapeNumber(this.curves) == 2);
				^[i, tempDur - time, val];
			};
			tempDur = tempDur + delta;
		});	
		if(this.shapeNumber(this.curves) == 2){
			val = (time / (tempDur - time)).linexp(0,1,
				this.levels[this.times.size], this.levels[this.times.size-1]);
		}{
			val = (time / (tempDur - time)).linlin(0,1,
				this.levels[this.times.size], this.levels[this.times.size-1]);
		};
		^[this.times.size, tempDur - time, val];
	}	
	
	synthiMessage{|nodeId, mapeToSymbol, nodeIdToMap, bus, server|
		var env;
		env = this.asArray;
		//experimental path responder ...
		server = server ? Server.default;

		OSCpathResponder(server.addr, ["/n_end", nodeIdToMap], 
				{ arg time, resp, msg;  
					server.sendBundle(server.latency, ["/n_free", nodeId]);
					bus.free;
					resp.remove;} 
			).add;
			
		^[[9, \synthi_env_256, 
			nodeId, 2, nodeIdToMap, \out, bus.index], 
						[\n_setn, nodeId, \env, env.size]++env,
						[\n_map, nodeIdToMap, mapeToSymbol, bus.index]]
	}
}

+ SimpleNumber {

	addToSynthiArgs{|synthi, symbol|
		synthi.usedArgs = synthi.usedArgs.add(symbol);
		synthi.usedArgs = synthi.usedArgs.add(this);

	}
	cutToTime{}
	
}

+ Symbol {
	addToSynthiArgs{|synthi, symbol|
		synthi.argEnvs = synthi.argEnvs.add(symbol);
		synthi.argEnvs = synthi.argEnvs.add([this]);
	}
	
	synthiMessage{|nodeId, mapeToSymbol, nodeIdToMap, bus|
//		this.debug(\synthiMes);
		^[[9, this, nodeId, 2, nodeIdToMap, \out, bus.index], 
		[\n_map, nodeIdToMap, mapeToSymbol, bus.index]];	
	}
	cutToTime{}
}


+ Array {
	addToSynthiArgs{|synthi, symbol|
		synthi.argEnvs = synthi.argEnvs.add(symbol);
		synthi.argEnvs = synthi.argEnvs.add(this);

	}

	synthiMessage{|nodeId, mapeToSymbol, nodeIdToMap, bus|
		var def;
		if(this.size < 2){
			^this[0].synthiMessage(nodeId, mapeToSymbol, nodeIdToMap, bus);
		};
		this[1] = this[1] ++  [\out, bus.index];
		def = Synthi(this[0],this[1], nodeIdToMap, \addBefore, play:false).prepareForPlay.message;
		^(def ++ [[\n_map, nodeIdToMap, mapeToSymbol, bus.index]]);

	
	}	
	cutToTime{}
}

+ Bus {
	addToSynthiArgs{|synthi, symbol|
		synthi.argEnvs = synthi.argEnvs.add(symbol);
		synthi.argEnvs = synthi.argEnvs.add(this);
	}

	synthiMessage{|nodeId, mapeToSymbol, nodeIdToMap, bus|
		^[[14, nodeIdToMap, mapeToSymbol, this.index]]; //"/n_map"
	}
	
	// fixme: this is a hack
	cutToTime{}


}



+ Nil {

	type{
		\not_Connected
	}
	settings{
		^[]
	}
}