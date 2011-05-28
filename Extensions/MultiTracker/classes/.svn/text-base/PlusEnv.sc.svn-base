+ Env {

	addBreakPoint{|level=0, dur=0.1, curve|
			var ttimes, tlevels, tcurves;
			ttimes = times.add(dur);
			tlevels = levels.add(level);
			if(curves.notNil and: curve.notNil){
				tcurves = curves.asArray.add(curve);
			}{
				tcurves = curves;
			};
			^Env(tlevels, ttimes, tcurves);
	}

	findBreakPoint{|time|
		var ttime=0;
		times.do{|t, i|
			ttime = ttime + t;
			if(ttime >= time){
				^i
			}
		};
		^times.size;
	}
	

	
	insertAtTime{|time,level=0, curve|
		var index, dur,ttimes, tlevels, tcurves;
		index = this.findBreakPoint(time);
		dur = time - times.keep(index).sum;
		ttimes = times.insert(index,dur);
		ttimes.put(index+1, ttimes[index+1]-dur);
		tlevels = levels.insert(index+1,level);
		if(curves.notNil and: curve.notNil){
			tcurves = curves.asArray.insert(index,curve);
		}{
			tcurves = curves;
		};
//			this.debug([tlevels, ttimes]);
			^Env(tlevels, ttimes, tcurves);			
	}
	
	insertBreakPoint{|index, level=0, dur=0.1, curve|
			var ttimes, tlevels, tcurves;
			ttimes = times.insert(index,dur);
			tlevels = levels.insert(index,level);
			if(curves.notNil and: curve.notNil){
				tcurves = curves.asArray.insert(index,curve);
			}{
				tcurves = curves;
			};
			^Env(tlevels, ttimes, tcurves);	
	}
	
	split{|time| //was cutEnv was cutToTime
		// returns an array of 2 Envs 
		// between point only linear interpolation for now ...
		var index, dur, ttimes, tlevels, tcurves, enva, envb, level, durrel;
		index = this.findBreakPoint(time);
		dur = time - times.keep(index).sum;
		ttimes = times.keep(index).add(dur);
		durrel = times[index] / (times[index] - dur);
		level = levels[index] - levels[index+1] / durrel + levels[index+1];
		
		tlevels = levels.keep(index+1).add(level);
		if(curves.isKindOf(Array))
		{
			tcurves = curves.keep(index).add(curves[index]);
		}{
			tcurves = curves;
		};
		enva = Env(tlevels, ttimes, tcurves);
		dur = times[index]-dur;

		ttimes = times.copyRange(index+1, times.size);
		tlevels = levels.copyRange(index+1, levels.size);
		if(curves.isKindOf(Array))
		{
			tcurves = curves.copyRange(index+1, curves.size);
		}{
			tcurves = curves;
		};
		if(dur>0){
			ttimes = ttimes.insert(0,dur);
			tlevels = tlevels.insert(0, level);
			if(curves.isKindOf(Array))
			{
				tcurves = tcurves.insert(0, curves[index+1]);
			}	
		};
		envb = Env(tlevels, ttimes, tcurves);
		^[enva, envb]

	}
		
}