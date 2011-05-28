MultiTrackSoundFilePlayer {
	var <file, switchtime=1, <status, <>server, <rate=1, <>startTimeFromTrack=0, duration, buffer, player, clock, synthDefName, spawnTask,stopRoutine;
	var <> group, < bus;

	*new{|aMultiTrackSoundFile, server|
		^super.new.init(aMultiTrackSoundFile, server);
	}
	
	
	init{|aMultiTrackSoundFile, aserver|
		server = aserver ? Server.default;
		file = aMultiTrackSoundFile;
		status= \stopped;
		group = 1.asGroup;
		bus = 0.asBus(\audio, file.numChannels, server);
	}
	
	startTime{
		^(file.startTime+startTimeFromTrack)
	}
	
	duration{
		^(file.duration - startTimeFromTrack)
	}
	
	rate_{|newrate|
		rate = newrate;
		clock.tempo_(newrate);
		player.set(\pchRatio, newrate);	
		
	}
	
	bus_{|in|
		bus = in.asBus(\audio, file.numChannels, server);
	}
	
	prepare{
	
		buffer = Buffer.read(server, file.path,
					this.startTime* file.sampleRate, file.sampleRate *switchtime*1.5);
	
		//buffer = Buffer(server, file.sampleRate *switchtime*1.5, file.numChannels);
//		server.makeBundle(0.1, {
//			buffer.allocMsg(
//				buffer.readMsg(file.path, this.startTime* file.sampleRate,44100 * file.sampleRate *switchtime*1.5,0, false)
//			)
//
//		});
		synthDefName = "multiTracker_varDiskRead_" ++ file.numChannels.asString;
		status = \prepared;
	}
	
	play{

//		ev.use({
		var synth, rotator, buff, tempoClock, loop=false, pchRatio = 1, soundFilePath;
		var sampleRate, numFrames;
		numFrames = file.numFrames;
		sampleRate = file.sampleRate;
		if(status != \prepared){ 
			this.debug("prepare in play multiTrackPlayer status: %".format(status.asString));
			this.prepare;
		};
//		this.debug(("path: % bufnum: %").format(file.path, buffer.bufnum));

		soundFilePath = file.path;
		buff = buffer;
		switchtime = switchtime;
		file = file;
		pchRatio = rate;
		player = synth;
		tempoClock = TempoClock(pchRatio);
		clock = tempoClock;
		player = synth;
		
		spawnTask = Routine({
			var o;
			var beat,rotator,cursor = 0;
			beat = this.startTime;
//			[\beat, beat].postln;
			//for startTime != 0 we need this to make sure we're not any looping further
			// should check here for rate changes though
			stopRoutine = r{
				(this.duration * rate.reciprocal).wait; 
				synth.free;
				synth = nil;
				player = nil;	
				if(buffer.notNil){buffer.free; buffer=nil};
//				"stopped by routine".postln;
				spawnTask.stop;
				status = \stopped;
			}.play;
			
			rotator = [
				//[1,2],
				{
//					"1 ".post;
					this.server.sendBundle(0.1,
						buff.readMsg(soundFilePath,o, sampleRate * switchtime)
					);
				},
				//[3,1],
				{
//					"2 ".post;
					this.server.sendBundle(0.1,
						buff.readMsg(soundFilePath, o, sampleRate * switchtime*0.5, sampleRate * switchtime),
						buff.readMsg(soundFilePath, o+(sampleRate *switchtime*0.5) , sampleRate * switchtime*0.5)
					);
				},
				//[2,3]
				{
//					"3 ".post;
					this.server.sendBundle(0.1,
						buff.readMsg(soundFilePath,o, sampleRate * switchtime, (sampleRate *switchtime*0.5))
					);
				}
			];
			// wait  beats
//			synth= player = Synth.basicNew(synthDefName, this.server);
//			 this.server.sendBundle(0.1, {synth.newMsg(args: [\i_bufnum, buff.bufnum, \pchRatio, pchRatio])});
			this.server.makeBundle(0.1, {player = synth = Synth(synthDefName, [\i_bufnum, buff.bufnum, \pchRatio, pchRatio, \out, bus.index], group)});
			switchtime.wait;
			beat = beat + switchtime + (switchtime*0.5);			
			
			//test
			
//			rotator.at(cursor).value;
			//enttest
			while({
				o = beat * sampleRate;
				o < numFrames
			},{
				rotator.at(cursor).value;
				cursor = (cursor + 1).wrap(0,2);
				switchtime.wait;
				beat = beat + switchtime;
			});
			if(synth.notNil){
				synth.free;
				synth = nil;
				player = nil;	
				if(buffer.notNil){buffer.free; buffer=nil};
				stopRoutine.stop;
				status = \stopped
			};
			
		});
		
		spawnTask.play(tempoClock);
		status = \playing;
		CmdPeriod.add(this);
//		});
	
	
	
	}
	stop{
		if(player.notNil){
			player.free;
			player = nil;
			if(buffer.notNil){buffer.free; buffer=nil};
			status = \stopped;
			spawnTask.stop;
			stopRoutine.stop;
			CmdPeriod.remove(this);
			
		}
	}
	
	cmdPeriod{
		this.stop;
	}
}