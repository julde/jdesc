/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

/* we don't know anything about the MultiTracker we're in */
// to do: use event instead of object with inside all the args
MultiTrackObject  {
	var <>object , < time, < duration, < track;
	var <>group, < args, < isSelected=false;
	var player, status;
	var undo;
	var <>maxDuration = 16777215, <>startTime=0;
	
	*new{|obj|
		^super.newCopyArgs(obj);
	}
	
	
	//might need some modification in subclass
	args_{|argsin|
		args = argsin;
	}
	
	asArrayName{ ^this.name}
	
	multiTrackGui{
		var new;
		new = this.multiTrackGuiClass.new(this);
		this.addDependant(new);
		^new
	}
	
	removeViews{
		var view;
		dependantsDictionary.at(this).copy.do({ arg item;
//			view = view.add(item.view.remove);
			item.remove(true);
		});
//		"rmoveView".debug(this);
		this.release;
		^view
	}
	
	cmdPeriod{
		this.stop
	}
	
	dependants{
		^dependantsDictionary.at(this);
	}
	
	multiTrackGuiClass{
		^MultiTrackObjectGui
	}
	
	//implement in subclass
	name{
		^""
	}
	
	prepareForPlay{|nodeOrGroup, startTime=0, duration, bus|
		if(startTime < this.time){
			startTime = 0.0
		}{
			startTime = startTime - this.time;
		};		
	}
	
	//fixme change order?
	play{|nodeOrGroup, startTime=0, dur, clock, bus|
		CmdPeriod.add(this);
	}
	
	stop{
		CmdPeriod.remove(this);
	}
	
	asScore{
		^nil
	}
	
	releaseMessage{
		^nil
	}
	
	storeOn{|stream|
		stream << this.class.name << ".new(";
		object.storeOn(stream);
		stream << ")";
		stream << ".time_(";
		time.storeOn(stream);
		stream << ").duration_(";
		duration.storeOn(stream);
		stream << ").track_(";
		track.storeOn(stream);
		stream << ").group_(";
		group.storeOn(stream);
		stream << ").args_(";
		this.args.storeOn(stream);
		stream << ")";

	}
	
	argViewSelectorClass{
		^MultiTrackArgViewSelector
	}
		
	help{
		MultiTrackerHelpCenter.helpStrings[this.class.name.asSymbol].newTextWindow;
	}
	
	select{
		isSelected = true;
		this.changed(\select)
	}
	
	unselect{
		isSelected = false;
		this.changed(\unselect)
	}	
	asMultiTrackObject{|track, time, duration, args|
		this.track_(track).time_(time).duration_(duration);
		if(args.notNil){this.args_(args)};
		^this
	}
	
	initAfterCopy{
	//soundfile needs a new player ...
	}
	
	//taken from model
	changed { arg what ... moreArgs;
		var theDependants;
		theDependants = dependantsDictionary.at(this);
		theDependants.do({ arg item;
			item.update(this, what, *moreArgs);
		});
	}
	// not used directly: call from MultiTracker:split ->this
	split{|multitracker,time|
		//shorten current object: duration: splittime - start
		//add object at splittime with duration: oldduration - newDuration
		var newDur, oldDur;
		oldDur = this.duration;
		newDur = time - this.time;
		this.duration_(newDur);
		this.changed(\duration);
		multitracker.put(this.track, time, oldDur - newDur, this.deepCopy, this.args.deepCopy);
		this.changed(\split);
	}
	
	time_{|in|
		time = in.clip(0, 16rffffff);
		this.changed(\time)
	}

	track_{|in|
		track = in;
		this.changed(\track)
	}
	
	duration_{|in|
		duration = in.clip(0, maxDuration - startTime);
		this.changed(\duration);
	}
	
	moveRelative{|dt|
		this.time = this.time + dt;
	}
	
}


CommentMultiTrackObject : MultiTrackObject {
	
	name{
		^object
	}
	
	play{|nodeOrGroup, startTime=0, dur, clock|
	
	}
	
	stop{
	}
	
	multiTrackGuiClass{
		^CommentMultiTrackObjectGui
	}
}

MComment : CommentMultiTrackObject {

	
}
FunctionMultiTrackObject : MultiTrackObject {
	
	name{
		^object.asCompileString
	}
	asArrayName{ ^object}
	
	play{|nodeOrGroup, startTime=0, dur, clock|
		object.value(this, \play);
		clock.sched(dur, {this.stop(this, \stop)});

	}
	
	stop{
		object.value(this, \stop)	
	}

}
EventMultiTrackObject : MultiTrackObject {
	
	name{
		var n;
		n = object.name ? object.instrument ? "noname";
		^n;
	}
	asArrayName{ ^object}
	
	play{|nodeOrGroup, startTime=0, dur, clock, bus|
		object.play;
		clock.sched(dur, {this.stop;});
	}
	
	stop{
		object.stop;	
	}

}
//new Synthi loop bugfix now with Synth for testing
// obj is now [symbol, args]

SynthiMultiTrackObject : MultiTrackObject{
	var server, argEnvs, envIDs, usedArgs, argSynths, currentID;
	name {
		^object.defName.asSymbol;
	}
	
	args_{|argsin|
//		object.args_(argsin);
		super.args_(argsin);
	}
	args{
		^object.args
	}
	
	synthDefName{
		^object.asSymbol
	}
	
	controls{
		^object.controls
	}
	
	argViewSelectorClass{
		^MultiTrackArgViewSelector
	}
		
	prepareForPlay{|nodeOrGroup, startTime=0, duration|
//		if(startTime < this.time){
//			startTime = 0.0
//		}{
//			startTime = startTime - this.time;
//		};	
//		this.debug(nodeOrGroup);
//		object.group_(nodeOrGroup);
//		object.prepareForPlay(startTime, duration);
//		status = \readyForPlay;
	}
	
	play{|nodeOrGroup, startTime=0, dur, clock, bus|
		var synth;
		this.debug("dur: " ++ dur.asString);
		this.debug("need to implement bus here: SynthiMultiTrackObject:play");
//		synth = Synth(object[0], object[1].asKeyValuePairs);
// there is a bug in .synthi
		synth = object.deepCopy.synthi;
		synth.play;
//						this.debug(dur);

	if(dur>0){
//						this.debug(dur);

			SystemClock.sched(dur,  //- object.server.latency, 
				{
					//object.server.sendBundle(nil, [\n_set, id, \gate, 0]);
					this.debug(synth);

					synth.stop;
	
//					status = \isStopped;
//					this.stop;
				});
		};		
//		if(status !== \readyForPlay){
//			this.prepareForPlay(nodeOrGroup, startTime, dur);
//			this.debug("preparing in MultitrackObe");
//		};
//		object.play(startTime, dur);
//		super.play;
//		id = object.nodeID.deepCopy;
//		status = \playing;
		//this.debug("playing" ++ object.defName.asString);
		/*
		if(dur>0){
			clock.sched(dur,  //- object.server.latency, 
				{
					object.server.sendBundle(nil, [\n_set, id, \gate, 0]);
					status = \isStopped;
//					this.stop;
				});
		};
		currentID = object.nodeID;
		*/
	}
	
	stop{|force=false|
//		this.debug("blocked calling stop"); 
/*
		if(status === \isStopped){^this};
		super.stop;
		object.server.sendBundle(nil, [\n_set, currentID, \gate, 0]);

//		envs free themselves any problems here ? check for gate message ?
		if(force){ //force envelopes to free
			object.envIDs.do({|id|
//				this.debug(id);
				object.server.sendBundle(0.1, [\n_free, id])
				
			});
		};
		status = \isStopped;
*/		
	}

	setCurves{|which, curve|
			which = this.args.indexOf(which)+1;
			which = this.args[which];			
			if(which.isKindOf(Env)){
				which.curves_(curve);
			};
			this.debug("setCurve: " ++ which.asString ++ curve.asString);
	}
	
	//would be nice to store the last env somewhere
	
	setNumberValueFor{|which, val|
		this.debug(this.args[which+1]);
		this.args[which+1] = val ? this.controls[(which*0.5)].defaultValue;
		//FIXME: this is not efficient!!
		this.args_(this.args);
	
	}
	
	//turn a number arg iinto an env arg
	
	setEnvelopeFor{|which, curve|
		curve = curve ? \lin;
		this.debug(this.args[which+1]);
		
		this.args[which+1] = Env([this.args[which+1], this.args[which+1]],
							 [duration], curve);
		//FIXME: this is not efficient!!
		this.args_(this.args);

	}	
	
	setBusFor{|which, bus|
		this.debug(this.args[which+1]);
		
		this.args[which+1] = bus.asControlBus;
		//FIXME: this is not efficient!!
		this.args_(this.args);

	}	
	
	asScore{|group|
		this.prepareForPlay(group, startTime:time);
		^([[time] ++ object.message])
	}
	
	releaseMessage{
		if(object.hasGate.not){^nil};
		^([[time+duration] ++ [object.releaseMessage]])
	}
}
	
	



// Synthi
/* original version */
/*
SynthiMultiTrackObject : MultiTrackObject{
	var server, argEnvs, envIDs, usedArgs, argSynths, currentID;
	
	name {
		^object.defName.asSymbol;
	}
	
	args_{|argsin|
		object.args_(argsin);
		super.args_(argsin);
	}
	args{
		^object.args
	}
	
	synthDefName{
		^object.asSymbol
	}
	
	controls{
		^object.controls
	}
	
	argViewSelectorClass{
		^MultiTrackArgViewSelector
	}
		
	prepareForPlay{|nodeOrGroup, startTime=0, duration|
		if(startTime < this.time){
			startTime = 0.0
		}{
			startTime = startTime - this.time;
		};	
		this.debug(nodeOrGroup);
		object.group_(nodeOrGroup);
		object.prepareForPlay(startTime, duration);
		status = \readyForPlay;
	}
	
	play{|nodeOrGroup, startTime=0, dur, clock|
		var id;
		this.debug(\play);
		if(status !== \readyForPlay){
			this.prepareForPlay(nodeOrGroup, startTime, dur);
			this.debug("preparing in MultitrackObe");
		};
		object.play(startTime, dur);
		super.play;
		id = object.nodeID.deepCopy;
		status = \playing;
		//this.debug("playing" ++ object.defName.asString);
		/*
		if(dur>0){
			clock.sched(dur,  //- object.server.latency, 
				{
					object.server.sendBundle(nil, [\n_set, id, \gate, 0]);
					status = \isStopped;
//					this.stop;
				});
		};
		currentID = object.nodeID;
		*/
	}
	
	stop{|force=false|
		this.debug("blocked calling stop"); 
/*
		if(status === \isStopped){^this};
		super.stop;
		object.server.sendBundle(nil, [\n_set, currentID, \gate, 0]);

//		envs free themselves any problems here ? check for gate message ?
		if(force){ //force envelopes to free
			object.envIDs.do({|id|
//				this.debug(id);
				object.server.sendBundle(0.1, [\n_free, id])
				
			});
		};
		status = \isStopped;
*/		
	}

	setCurves{|which, curve|
			which = this.args.indexOf(which)+1;
			which = this.args[which];			
			if(which.isKindOf(Env)){
				which.curves_(curve);
			};
			this.debug("setCurve: " ++ which.asString ++ curve.asString);
	}
	
	//would be nice to store the last env somewhere
	
	setNumberValueFor{|which, val|
		this.debug(this.args[which+1]);
		this.args[which+1] = val ? this.controls[(which*0.5)].defaultValue;
		//FIXME: this is not efficient!!
		this.args_(this.args);
	
	}
	
	//turn a number arg iinto an env arg
	
	setEnvelopeFor{|which, curve|
		curve = curve ? \lin;
		this.debug(this.args[which+1]);
		
		this.args[which+1] = Env([this.args[which+1], this.args[which+1]],
							 [duration], curve);
		//FIXME: this is not efficient!!
		this.args_(this.args);

	}	
	
	setBusFor{|which, bus|
		this.debug(this.args[which+1]);
		
		this.args[which+1] = bus.asControlBus;
		//FIXME: this is not efficient!!
		this.args_(this.args);

	}	
	
	asScore{|group|
		this.prepareForPlay(group, startTime:time);
		^([[time] ++ object.message])
	}
	
	releaseMessage{
		if(object.hasGate.not){^nil};
		^([[time+duration] ++ [object.releaseMessage]])
	}
}
*/

SoundFileFromDiskMultiTrackObject : SynthiMultiTrackObject{
	var fileArgs;
	var player;
	//object: MultiTrackSoundFile.
	
	args_{|argsIn|
		var newargs, indx;
		argsIn = argsIn ? [];
		server = Server.default;
		if(this.duration == -1 or: {this.duration > object.duration})
			{this.duration = object.duration};
		argsIn.do({|a, i|
//			if(a===\bufnum or: {a=== \i_bufnum}){
////			 	object.bufnum = argsIn[i+1];
//			 	fileArgs = fileArgs.add(\i_bufnum);
//			 	fileArgs = fileArgs.add(object.bufnum);
//			};
			if(a ===\startTime){
				this.startTime_(argsIn[i+1]);
			 	fileArgs = fileArgs.add(\startTime);
			 	fileArgs = fileArgs.add(argsIn[i+1]);
			};		
			if(a ===\startFrame){
//				this.startFrame_(argsIn[i+1]);
			 	fileArgs = fileArgs.add(\startFrame);
			 	fileArgs = fileArgs.add(argsIn[i+1]);
			 	this.startFrame_(argsIn[i+1]);
			};	

		});
//		indx = argsIn.indexOf(\bufnum);
//		if(indx.isNil){ indx = argsIn.indexOf(\i_bufnum)};
//		if(indx.isNil){ argsIn = argsIn.add(\i_bufnum); argsIn = argsIn.add(object.bufnum); };
		//object.synthDefName.asSymbol.postln;
		
		maxDuration = object.duration;
		
		super.args_(argsIn);
//		this.debug(argsIn);
//		args = argsIn;
	}
	
	startFrame_{|frame|
		object.startFrame_(frame);
		
	}
	
	synthDefName{
		^object.synthDefName.asSymbol
	}
	
	startTime_{|time|
		object.startTime_(time);
	}
	
	duration_{|indur|
		duration = indur;
		object.duration_(duration);
	}
	
	startTime{
		^object.startTime
	}
	
	endTime{
		^(object.startTime + this.duration)
	}
	
	setStartTime{|time, rescaleDur|
		rescaleDur = rescaleDur ? false;
		this.startTime_(time);
		if(rescaleDur){
			this.duration = (this.duration - time).clip(0, object.duration);
		}
	}
	
	playerMessage{|nodeOrGroup, id|
//	 ^[9, synthDefName, id, 0, nodeOrGroup.asTarget.nodeID, \i_bufnum, model.buffer.bufnum]
		^[9, object.synthDefName, id, 0, nodeOrGroup.asTarget.nodeID]
	}
	
	name{
		^object.name
	}
	
	argViewSelectorClass{
		"argViewSelectorClass currently not available".warn;
//		this.debug(\argview);
//		^MultiTrackSoundFileArgView
	}
	
	prepareForPlay{|nodeOrGroup, startTime=0, duration, bus|
		player = object.multiTrackPlayer;
		if(startTime < this.time){
			startTime = 0.0
		}{
			startTime = startTime - this.time;
		};
		if(status === \readyForPlay){^this};
	//	player.group_(nodeOrGroup);
//		player.bus_(bus);
		this.debug([\preparingHDSynthiObject, startTime, bus]);
		player.startTimeFromTrack = startTime;

		player.prepare;
		status = \readyForPlay;
	}	

	play{|group, startTime=0, dur, clock, bus|
//		super.play(nodeOrGroup, startTime, dur, clock, bus);
//		var player = 
		player
			.bus_(bus)
			.group_(group)
			.startTimeFromTrack_(startTime);

		this.debug([\instartTime, startTime, \playerStartTime,player.startTimeFromTrack]);
		status = \playing;
		player.play;
		//CmdPeriod.add(this);
	}
	
	stop{|force=false|
//		^this; //fix me
		this.debug("stop " ++ status.asString);
		
//		if(status !== \playing){^this};
		super.stop(force);
		player.stop;
		status = \isStopped;
		//CmdPeriod.remove(this);

	}
	
	multiTrackGuiClass{
		^SoundFileMultiTrackObjectGui
	}
	
	initAfterCopy{
	//soundfile needs a new player ...
		var serv;
		serv = object.server;
		object = SoundFileHDSynthi(object.path);
//		object.server = serv;
		object.args_(this.args);
//	this.debug("initAfterCopY" ++ serv.asString);
	}	
	
	asArrayName{
		^object.path
	}
	
	asScore{
		var bufmsg, bufmsgR, bufsize=256, numchan, buftime;
		numchan = object.info[0];
		object.prepareForScore(time);
		if(time < 0.1){buftime = 0.02; time=0.02; "adding 0.02 seconds to load buffer".warn;}{buftime = 0.1};
		bufmsg =[time-buftime, ["/b_alloc",  object.bufnum, bufsize, numchan,
					["/b_read", object.bufnum,  object.path, object.startFrame, bufsize, 0, 1]
					]]; 
//		bufmsg = [time-1, ["/b_allocRead",  object.bufnum, object.path, object.startFrame]];
//		bufmsgR =[time-1, ["/b_allocRead",  object.bufnum, object.path, object.startFrame, 512]]; 

		^([bufmsg,[time] ++ object.message])
	}
	
	releaseMessage{
		^([[time+duration+0.1, ["/b_close", object.bufnum], ["/b_free", object.bufnum]],[time+duration] ++ [object.releaseMessage]])
	}
	
	// not used directly: call from MultiTracker: split->this // need to override for frames
	split{|multitracker,time|
		//shorten current object: duration: splittime - start
		//add object at splittime with duration: oldduration - newDuration
		var newDur, oldDur, newObj, newObjDur;
		oldDur = this.duration;
		newDur = time - this.time;
		this.duration_(newDur);
		newObjDur = oldDur - newDur;
//		newObj = this.deepCopy;
//		newObj.startFrame_(newDur*object.sampleRate);
//		multitracker.put(this.track, time, oldDur - newDur, newObj);
//		this.debug([\startframe, object.startFrame + (newDur*object.sampleRate)]);
		multitracker.putSoundFile(object.path, this.track, time, 
				newObjDur, (newDur*object.sampleRate) + object.startFrame);

		multitracker.putSoundFile(object.path, this.track, this.time, 
				newDur, 0);
		multitracker.remove(this.track, this);
//		this.changed(\duration);
	}	
	
}

MultiTrackerMultiTrackObject : MultiTrackObject{

	name{
		^object.name;
	}
	
	multiTrackGuiClass{	
		^MultiTrackerMultiTrackObjectGui
	}
	
	prepareForPlay{|nodeOrGroup, startTime=0, duration|
		object.prepareForPlay(startTime);
		status = \readyForPlay;
	}	
	play{|nodeOrGroup, startTime=0, dur, clock|
		object.play(startTime, dur); //FIXME is dur ok?
		status = \playing;
		clock.sched(dur, {this.stop});
	
	}
	
	stop{|time|
		object.stop(time);
		status = \isStopped;

	}

}

PbindMultiTrackObject : MultiTrackObject{
	var <showPianoRoll=false;

	args_{|argsin|
		var newdur;
		newdur =  object.getDuration;
		if(newdur.notNil){
			this.duration =  newdur;
			showPianoRoll = true;
		}
		
	}
	
	name{
		^object.asString;
	}
	asArrayName{ ^object}
	
	multiTrackGuiClass{	
		^PbindMultiTrackObjectGui
	}
	
	prepareForPlay{|nodeOrGroup, startTime=0, duration|
//		object.prepareForPlay(startTime);
		status = \readyForPlay;
		player = object.asEventStreamPlayer;
	}	
	play{|nodeOrGroup, startTime=0, dur, clock|
		player.play; //FIXME is dur ok?
//		this.debug([object, dur, object]);
		status = \playing;
		clock.sched(dur, {this.stop;});
	
	}
	
	stop{|time|
		player.stop(time);
		status = \isStopped;

	}
	asScore{
		// FIXME need maxtime
		^object.asScore(duration,timeOffset: time).score
	}

	argViewSelectorClass{
//		this.debug(\argview);
		^MultiTrackPbindArgView
	}
}
