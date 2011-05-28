/* 	(c) 2006 Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

/**
	status: playing, isStopped, preparingForPlay;
	everyone takes care of its own stop time...
**/

MTrack { //might be better a subclass of Stream
	var <> data, <>group, obj, <server, <>prepareLatency, <>muted=false;
	var <> guiOptions, <>status;
	var <task;
	var <>currentTime; //provided by MultiTracker
	var currentObjectTime = 0;
	var <clock;
	var <>bus, <> numChannels = 2; // stereo by defaut for now
	var <>number;
	var <>currentObject; //currently selected
	
	*new{|prepareLatency, group, server, num|
		^super.new.init(prepareLatency, group, server).number_(num);
	}

	init{|late, gr, aserver|
		server = aserver;
		data = IdentitySet.new;
//		group = gr ? Group.new;
		prepareLatency = late;
		guiOptions = MTrackGuiOptions.new.background_(Color.blue(0.5, 0.1)).visible_(true);
		status = \stopped;
		currentTime = 0;
		bus = Bus(\audio,index:0,numChannels:numChannels);
	}
	
	add{|dat|
		data.add(dat);
		if(status === \isPlaying and: (dat.time > currentTime)){
//			this.debug([currentTime, dat.time]);

			//add it to the playing seq would be nice, but does not really work 
			//mPriorityQueue.put(dat.time - currentObjectTime, dat);
			//so just fork it. 
			this.forkRealTimeAdd(dat, dat.time - currentTime);
		};
	}
	
	forkRealTimeAdd{|obj, time|
		{obj.prepareForPlay(group, 0, obj.duration); time.wait; obj.play(group, 0, obj.duration,clock);}.fork(clock);
	}
	
	do{|func|
		data.do(func)
	}
	
	remove{|obj|
		obj.stop;
		data.remove(obj);
//		this.changed(\remove, obj); // speak
	}
	
	mute{
		muted = true;
	}
	unmute{
		muted = false;
	}
	
	/***
	***	task plays a MultiTrackObject
	***/
	prepareForPlay{|startTime=0, duration, clock|
		//FIXME need to see if the Group was allocated before / server was running to make it static
		group = group ? Group.new;
//		group = group ? 0.asGroup;
//		(type: \Group, id: [2,3,4,5,6], group: 0, addAction: 1).play;
//		server.sendMsg(group.newMsg);
		status = \preparingForPlay;
//		task = this.asTask(startTime, duration, clock);
//		server.sendMsg(group.newMsg.postln);
		
	}
	
	play{|startTime=0, duration, clockin, quant|
		var task;
		clock = clockin;
		//fixme
		task = this.asTask(startTime, duration, clockin);		task.play;
		if(duration.notNil){
			if(duration < inf){
				clock.sched(duration, {task.stop});
			}
		};	
//		task.play(clock, quant:quant);
		status = \isPlaying;		
	}
	
	stop{|force=false|
		this.stopLastPlayer;
		task.stop;
		//group.freeAll;
		data.do{|dat| dat.stop(force)};
//		this.debug(\stop);
		group.free;
		group = nil;
//		mPriorityQueue = nil;
		status = \isStopped;
	}
	
	stopLastPlayer{
		obj.stop;
	}
//	priorityQueue{
////		if(mPriorityQueue.isNil){
//			mPriorityQueue = this.asPriorityQueue;
////		};
//		^mPriorityQueue
//	}
// to do: clean up duration issue
	asTask{|startTime=0, dur = 42, clock, useCopy=false|
		var pq,t, firstObject, firstTime = -1, firstplaytime = 0, tdiff, foundObj=false;
		pq = this.asPriorityQueue;
		if(pq.isEmpty or: muted){^Task({})};
		pq.postpone(startTime * -1);
		dur = dur ? pq.array[pq.array.size-2];
//		this.debug(dur);
//		clock.sched(dur, {this.stop});
		t = Task({
			var last = 0, now = 0, objDur;
		
			while ({ pq.notEmpty },{
				now = pq.topPriority;
				this.debug(now);
//				this.debug(now);
				if((now + pq.array[1].duration).isNegative ){
					pq.pop;
					now = 0;
				}{
					objDur = pq.array[1].duration - now;
					if((objDur + now) > dur){objDur = objDur-(dur - now)};
					if(now.isNegative){ //cut the first object
						firstplaytime = now * -1;
						now = 0;
					};
//					this.debug([\objectDurIs, objDur]);

					currentObjectTime = now;
					if(useCopy){
						obj = pq.pop.deepCopy; //might be better to use a copy; changed 02/06/07
					}{
						obj = pq.pop; // using a copy is nice but gives problem with stop 
					};
					(now - last).wait; 
					this.debug(bus);
					obj.prepareForPlay(group, firstplaytime, objDur, bus);
	//				this.debug(\prepared);
	//				this.debug([now, stopTime]);
	
	//				(prepareLatency - server.latency).wait;
	//				("time is now: " ++ now.asString).postln;
					obj.play(group, firstplaytime, obj.duration, clock, bus);
					last = now;	
				};
			});
			pq = nil;
		});
		^t;
	
	}
	
	asPriorityQueue{
		var out;
		out = PriorityQueue.new;
		data.do({|tr|
			out.put(tr.time, tr);
		});
		^out;
	}
	
	at{|index|
		//might be a more efficient method?
		var pq;
		
		pq = this.asPriorityQueue;
		index.do{pq.pop};
		^pq.pop
	
	}
	
	select{|index|
		^this.at(index).select;
	}

	unselect{|index|
		^this.at(index).unselect;
	}
		
	asStream{
		var pq;
		pq = this.asPriorityQueue;
		if(pq.isEmpty or: muted){^Routine({})};
		^Routine({
			while ({ pq.notEmpty},{
				pq.pop.yield;
			});
			nil.yield
		});
	}
	//FIXME need to calc clock
	asScore{
		var r, x=1, s, score;
		s = [];
		group = Group.new;
		r = this.asStream;
		score = [[0,group.newMsg]];
		data.size.do{
			
			x = r.next;
			score = score ++ x.asScore;
			if(score.notNil)
			{
				s = s ++ score;
				
				if(x.releaseMessage.notNil){
					s = s ++ x.releaseMessage;
				};
			};
			x
		};
		^s;
	}
	
	asCompileString{
		var stream;
		stream = PrettyPrintStream.on(String(256));
		this.storeOn(stream);
		^stream.contents
	}
	
	storeOn{|stream|
		stream << this.class.name;
		this.storeParamsOn(stream);
		stream << ".data_(";
		stream.nl;stream.tab;
		this.data.storeOn(stream);
		stream << ")";
	
	}
	storeArgs{
		^[prepareLatency, group]
	}
}

/* gui is MultiTrackerWindow */

MultiTracker {
	var <>server, <> clock;
	var <currentTime = 0.0, <pixelPerSecond = 100, <lastTime;
	var <> grid = 0.1, <> snapToGrid=true;
	var <> tracks;
	var <> lastRemovedObject, <> lastAddedObject, <> copyMem, <>undoMem;
	var maximalTracks = 7;
	var <> maxTime = 120.0;
	var guiPlayerTask, audioTaskPlayer;
	var < isPlaying=false;
	var tasks;
	var <>name;
	var <> trigger;
	var <status;
	var <>prepareLatency=0.1; //add some more latency to prepareForPlay ...
	var <>group;
//	var resetCurrentTimeInGuiTask=false; //mhh ;>
	var loopRoutine;
	var duration = inf;
	var <> hasMixer = true;
	var <>currentPathForRendering;
	var <> keyCodeResponder;
	var <> talkbackController;
	var <lastStartPosition=0;
	var <tools;
	var <>toolbox;	
	var <> currentFolder;
	
	
	*new{|name, server, clock, numTracks=8|
		^super.new.init(name, server, clock, numTracks);
	}
	
	init{|name, aserver, aclock, numTracks|
		maximalTracks = numTracks;
		clock = aclock ? TempoClock(1); //need to sort out the clocks !
		this.server = aserver ? Server.default;
		this.name = name ? "untitled";
		tracks = Array.fill(maximalTracks,{|i|MTrack(prepareLatency,nil, server, i)});
//		pixelPerSecond = 100;
		trigger = MultiTrackTriggerCollection(this);
		if(SynthDescLib.global.synthDescs.size < 1){SynthDescLib.global.read};
		/* setup a default group for this MultiTracker -> MTracks should alloc suubgroups 
			this group is static (?)
		*/
		group = 0;
		tools =  MultiTrackTools(this);
	}
	
	guiClass{
		^MultiTrackerWindow
	}
	
	currentTime_{|time|
		if(time<0){time = 0};
		lastTime = currentTime;
		currentTime = time;
		tracks.do{|tr| tr.currentTime_(time)};
		this.changed(\time);
	}
	
	
	
	pixelPerSecond_{|pps|
		pixelPerSecond = pps;
		this.changed(\pps); 
	}
	
	zoomIn{
		this.pixelPerSecond_((pixelPerSecond*0.9).clip(0,1000));
	}
	
	zoomOut{
		this.pixelPerSecond_((pixelPerSecond*1.1).clip(0,1000));
	
	}

	put{|tracknum, time, duration, object, args| 
		if(time.respondsTo(\at)){this.put(tracknum,*time)};
		tracknum = tracknum.clip(0,maximalTracks);
		//this converts the object ...:
		object = object.asMultiTrackObject(tracknum, time, duration, args);
		if(object.object.respondsTo(\numChannels)){
			if(object.object.numChannels != this.at(tracknum).numChannels){
				this.changed(\warning, "putting a % channels objects on a % channels track".warn);
			};
		};
		//objects.put(time, object);
			
		this.add(tracknum, object);
		if(talkbackController.notNil){
			object.enableTalkback
		};
	}

	putCopyMemOnCurrentTime{
		var object, nargs;
		nargs = copyMem.args.deepCopy;
//		this.debug([\copyMem1, copyMem.args]);

		object = copyMem.asMultiTrackObject(copyMem.track, currentTime, 
					copyMem.duration, copyMem.args);
		object.initAfterCopy;
//		this.debug([\copyMem, copyMem.args]);
		this.add(copyMem.track, object);
		object.args_(nargs);

//		this.debug([\object, object.args]);
//		this.debug([\nargs, nargs]);

		
	}
	
	remove{|track, object|
		lastRemovedObject = object;
		tracks[track].remove(object);
		this.changed(\remove, object);
	}
	
	add{|track, object|
		track = track.clip(0,maximalTracks);
		object.track_(track);
		object.group_(tracks[track].group);
		// always try to add to our soundfilePool
		if(object.isKindOf(SoundFileFromDiskMultiTrackObject)){
			tools[\soundFiles].add(object.object);
		};		
		tracks[track].add(object);
		lastAddedObject = object;
		this.changed(\add);

	}
	
	
/* object specific */
	split{|object, time|
		//shorten current object: duration: splittime - start
		//add object at splittime with duration: oldduration - newDuration
		object.split(this, time);
	}		
	
	track{|num|
		^tracks[num];
	}
	
	at{|index|
		^tracks[index]
	}
	
	
	moveObjectToTrack {|object, track|
		tracks[object.track].remove(object);
		track = track.clip(0,maximalTracks);
		object.track_(track);
		object.group_(tracks[track].group);
		tracks[track].add(object);
		lastAddedObject = object;
		this.changed(\moveToTrack);
	}
	
	prepareForPlay{|startTime=0, induration, prepareObjects=false, resetTimer=true|
	/* starttime is absolute objects have to take care of their own rel. starttime themselves */
		var wait, loops;
		tasks = []; //fro now rebuild every time
		wait = 0.05;
//		this.debug(\preparing);
		duration = induration ? inf;
		//FIXME clock is not running error quickfix:
//		clock = TempoClock(1);
				
		if(startTime.notNil){this.currentTime_(startTime)}{this.currentTime_(0)};		
		if(guiPlayerTask.isNil){
			guiPlayerTask = Task({
				var time;
				if(duration == inf){loops = inf}{loops = floor(wait.reciprocal * duration)};
				time = currentTime;
//				(prepareLatency + server.latency).wait;
				(prepareLatency).wait;

				loops.do({

					this.currentTime_(time+0.25); // add some time to compensate gui latency defer
					wait.wait;
					time = time + wait;
//					if(loop and: (time>= (duration+startTime))){time = startTime}
				});
	//			this.stop;
			});
		}{
			if(resetTimer){guiPlayerTask.reset;};
		};
		if(prepareObjects){
			tracks.do{|tr| tr.do{|obj| 
				obj.prepareForPlay(tr.group, startTime, bus:tr.bus);


//				this.debug(obj);
			}}
		};		
//		tracks.size.do({|i|
//			tasks = tasks.add(tracks[i].asTask(startTime, induration, clock));
//			
//		});
		if(duration != inf){
			tasks = tasks.add(Task({duration.wait; this.stop(0,false); }));
		};		

//		tasks = tasks.add(guiPlayerTask);
		status = \readyForPlay;
//		this.debug(\prepared);


	}
	
	playAndRecord{|startTime, stopTime, quant=0|
		this.play(startTime, stopTime, quant=0);
//		{prepareLatency.wait; server.record}.fork;
	}
	
	play{|startTime, duration, quant=0, doneAction|
		var wait, loops;
		/* stopTime is depricated and should be removed .. ?*/
//	this.debug(\play);
//		loop = false;
		startTime = startTime ? currentTime;
//		if(isPlaying){"MultiTracker already playing".warn;^this};
		lastStartPosition = startTime;
		currentTime = startTime;

		r{		
			if(status !== \readyForPlay){
				
				this.prepareForPlay(startTime, duration);
//				this.debug("preparing in play");
				prepareLatency.wait;
	
			};
			guiPlayerTask.play;
		tracks.size.do({|i|
			tracks[i].play(startTime, duration, clock);
			
		});			
			tasks.do({|t| t.play(clock, quant:quant)});
			isPlaying = true;
			CmdPeriod.add(this);
			status = \playing;
			this.changed(\play);
		}.play;
	}
	
	start{
		this.play(0);
	}
	
	loop{|startTime, duration, quant=0|
		/* not optimized yet*/
		var time, loops;
//		loop = true;
		
		loops = duration * 0.1.reciprocal;
			this.prepareForPlay(startTime, duration, resetTimer:false); //loop:true);
		loopRoutine = r{
			inf.do{
				this.play(startTime, duration); //, doneAction:this.play(startTime, duration));
				(duration - prepareLatency).wait;
	//			this.prepareForPlay(startTime, duration, resetTimer:false);
				prepareLatency.wait;
				
	//			resetCurrentTimeInGuiTask = true;
				currentTime = startTime;
//				this.stop(0,false);	
		}};
		loopRoutine.play
	}
	
	stop{|wait=0, stopLoop=true|
		this.debug(\stop);
		SystemClock.sched(wait,{
		if(loopRoutine.notNil and: stopLoop){loopRoutine.stop; loopRoutine=nil};
			tasks.do({|t| t.stop});
			isPlaying = false;
			tracks.do{|t| t.stop(force:true)};
			status = \isStopped;
//			SoundFileHDSynthi.closeAndFreeBuffers;
			clock.clear;
			clock =TempoClock(1);
			this.changed(\stop);
			guiPlayerTask.stop.reset;
		});

	}	
	
	returnToStart{
		if(isPlaying){
			this.stop;
		};
		this.currentTime = lastStartPosition;
		if(isPlaying){
			this.play(this.currentTime);
		};		
	}
	
	cmdPeriod{
		this.stop;
		CmdPeriod.remove(this);	
	}
	//fixme: needs to be constructed in storeOn etc
	asCompileString{
		var stream;
		stream = PrettyPrintStream.on(String(256));
		this.storeOn(stream);
//		stream << ".name_(";
//		name.storeOn(stream);
//		stream << ")";
//		stream << ".tracks_(";
//		stream.nl;stream.tab;
//		tracks.storeOn(stream);
//		stream << ")";

		^stream.contents
	}
	storeArgs{
		^[name]	
	}
	storeModifiersOn{|stream|
		stream << ".name_(";
		name.storeOn(stream);
		stream << ")";
		stream << ".tracks_(";
		stream.nl;stream.tab;
		tracks.storeOn(stream);
		stream << ")";		
	}

	

/** NRT support
**/

	asScore{
		var score;
		score = [];
		tracks.do{|t|
			score = score ++ t.asScore;
		};
		//this.debug(score);
		score = Score(score);
		score.sort;
//		score.score_([[0.0, [\g_new, 1, 1, 0]]] ++ score.score);

		^score
	}
	

	render{|path, sampleRate=44100, headerFormat="AIFF", sampleFormat="float32", options, inputFilePath, numChannels=2|
		var score, oscpath;
		oscpath = path.dirname ++ "/" ++  path.basename.splitext[0] ++ ".osc";
		score = this.asScore;
		options = options  ?  ServerOptions.new.numOutputBusChannels = numChannels; 
		currentPathForRendering = path;
		score.recordNRT(oscpath, path, inputFilePath, sampleRate, headerFormat, sampleFormat, options, "; rm" + oscpath);
	}
	
//	save {|path|
//		var str, file;
//		str = this.asArrayCompileString ++ 
//				".asMultiTracker(" ++ this.name.asCompileString ++ ")";
//		file = File(path, "w");
//		file.write(str);
//		file.close;
//	}

	save {|path|
//		if(path
		this.asSnapshot.writeArchive(path);
	}

	saveDialog{
		CocoaDialog.savePanel({ arg path;
			currentFolder = path;
			this.save(path);
		},{
			"save canceled".postln;
		});
	}
	
	open {|path|
		this.setSnapshot(Event.readArchive(path));
	}

	openDialog{
		CocoaDialog.getPaths({ arg path;
			this.open(path[0]);
		},{
			"save canceled".postln;
		});
	}	
	

}
