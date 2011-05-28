/* usually a multiTrackPlayer is an Event */
//todo check startTime
// new duration method not depending on duration

+ MultiTrackSoundFile {

/* credit for this goes to Felix Crucial. I picked up the buffer update function from VSFP */
	multiTrackPlayer{
	^MultiTrackSoundFilePlayer(this);
/*
(
//	file: MultiTrackSoundFile.openReadAndClose( "sounds/a11wlk01-44_1.aiff"),
	file: this,
	switchtime: 1,
	status: \stopped,
	server: Server.default,
	synthDef: nil,
	prepare: {|ev|
			ev.use{
			~buff = Buffer.read(~server, ~file.path,~startTime.value(ev)* ~file.sampleRate, ~file.sampleRate *~switchtime*1.5);
			~synthDefName = "multiTracker_varDiskRead_" ++ ~file.numChannels.asString;
				~status = \prepared;
			}
	},
	rate_:{|ev,newrate|
		ev.use{
			~clock.tempo_(newrate);
			~player.set(\pchRatio, newrate);
			~rate = newrate;
		}
	},
	rate: 1,
	startTime: {|ev| var out; ev.use{out = ~file.startTime+~startTimeFromTrack}; out},	startTimeFromTrack: 0,
	duration: {|ev| var out; ev.use{out = ~file.duration - ~startTimeFromTrack}; out},
	play:
	{|ev|
//		ev.use({
		var spawnTask, synth, rotator, buff, file, tempoClock, switchtime=4, loop=false, pchRatio = 1, soundFilePath;
		var sampleRate;
		sampleRate = ~file.sampleRate;
		if(~status != \prepared){ 
			this.debug("prepare in play multiTrackPlayer status: %".format(~status.asString));

			~prepare.value(currentEnvironment);
		};
		this.debug(("path: % bufnum: %").format(~file.path, ~buff.bufnum));

		soundFilePath = ~file.path;
		buff = ~buff;
		switchtime = ~switchtime;
		file = ~file;
		pchRatio = ~rate;
		~player = synth;
		tempoClock = TempoClock(pchRatio);
		~clock = tempoClock;
		
		
		spawnTask = Routine({
			var o;
			var beat,rotator,cursor = 0;
			beat = ~startTime.value(currentEnvironment);
			[\beat, beat].postln;
			//for startTime != 0 we need this to make sure we're not any looping further
			// should check here for rate changes though
			r{
				(~duration.value(currentEnvironment) * ~rate.reciprocal).wait; 
				synth.free;
				synth = nil;
				~player = nil;	
				buff.free;
				"stopped by routine".postln;
				~status = \stopped;
			}.play;
			
			rotator = [
				//[1,2],
				{
//					"1 ".post;
					~server.sendBundle(0.1,
						buff.readMsg(soundFilePath,o, sampleRate * switchtime, 0,false)
					);
				},
				//[3,1],
				{
//					"2 ".post;
					~server.sendBundle(0.1,
						buff.readMsg(soundFilePath, o, sampleRate * switchtime*0.5, sampleRate * switchtime, false),
						buff.readMsg(soundFilePath, o+(sampleRate *switchtime*0.5) , sampleRate * switchtime*0.5,0, false)
					);
				},
				//[2,3]
				{
//					"3 ".post;
					~server.sendBundle(0.1,
						buff.readMsg(soundFilePath,o, sampleRate * switchtime, (sampleRate *switchtime*0.5),false)
					);
				}
			];
			// wait  beats
			 ~server.makeBundle(0.1, {synth=~player=Synth(~synthDefName, [\i_bufnum, buff.bufnum, \pchRatio, pchRatio])});
			
			switchtime.postln.wait;
			beat = beat + switchtime + (switchtime*0.5);			

			while({
				o = beat * sampleRate;
				o < file.numFrames
			},{
				rotator.at(cursor).value;
				cursor = (cursor + 1).wrap(0,2);
				switchtime.wait;
				beat = beat + switchtime;
			});
			if(synth.notNil){
				synth.free;
				synth = nil;
				~player = nil;	
				buff.free;
				~status = \stopped
			};
			
		});
		
		spawnTask.play(tempoClock);
		~status = \playing;
//		});
	},
	stop:{
		if(~player.notNil){
			~player.free;
			~player = nil;
			~buff.free;
			~status = \stopped
			
		}
	}
	
).deepCopy;

*/
	}

}