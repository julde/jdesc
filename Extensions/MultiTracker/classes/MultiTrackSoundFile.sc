MultiTrackSoundFile : SoundFile {


	var < startFrame=0, < name, <> changeNameOnModification = true, <>basename, <numFramesInUse;
	var <> relativePath;

	*openReadAndClose{ arg pathName;
		var file;
		file = MultiTrackSoundFile(pathName);
		if(file.openRead(pathName)){^file.init}{^nil}
	}
	

	
	name_{|inname|
		name = inname;
		this.changed(\name);
	}
	
	init{|instartFrame, inNumFrames|
		startFrame = instartFrame ? 0;
		numFramesInUse = inNumFrames ? numFrames;
		if(changeNameOnModification){
			this.name_(this.nameFromSettings);
		};
		relativePath = path; // not relative by default
		this.close;	
	}
	
	startFrame_{|frame|
		startFrame = frame.clip(0, numFrames-1);
		numFramesInUse = numFramesInUse - startFrame;
		if(changeNameOnModification){
			this.name_(this.nameFromSettings);
		}
	}
	
	numFramesInUse_{|frames|
		numFramesInUse = frames.clip(1,numFrames);
		if(changeNameOnModification){
			this.name_(this.nameFromSettings);
		
		}		
	}
	
	//overriding path to st basename
	path_{|apath|
		path = apath;
		basename = path.basename;
		if(changeNameOnModification){
			this.name_(this.nameFromSettings);
		}		
	}

	nameFromSettings{
		if(startFrame == 0 and: (numFrames == numFramesInUse)){
			^basename
		};
		^"%_%_%".format(basename, startFrame, numFramesInUse);
	}

	duration { ^numFramesInUse/sampleRate }
	duration_{|dur|
		this.numFramesInUse_(dur*sampleRate);
	}
	
	startTime{ ^startFrame/sampleRate}
	startTime_{|time|
		this.startFrame_(time*sampleRate)
	}
	
	copyWithNewSettings{|startTime, duration|
		var copy;
		copy = this.deepCopy;
		^copy.startTime_(startTime).duration_(duration);
	}
	
	setRelativePathTo{|rel|
		relativePath = this.path.relativePathTo(rel);
	}
	
	*initClass{
		//used in multiTrackPlayer
		16.do{|i|
			SynthDef.writeOnce("multiTracker_varDiskRead_" ++ (i+1).asString,{ arg out = 0, i_bufnum=0,pchRatio=1.0, amp=1;
				var env;
				
				Out.ar(out,
					PlayBuf.ar(1,i_bufnum,pchRatio,loop: 1.0) *amp
				)
			});
			};	
	
	}
	
	copyFileToPath{|path|
	
	}
	
	
	
	
	/*
	defined in another file
	asSnapshot
	*/
	
// use  .multiTrackPlayer instead !
//	// provide a buffer for DiskIn
//	getCueBuffer{|server, condition, doneAction|
//		var buffersize = 32768, action;
//		if(server.serverRunning.not){^this};
//		action = {
//			doneAction.value; 		
//			if(condition.notNil){condition.test=true; condition.signal};
//		};
//
//		^Buffer.cueSoundFile(server, this.path, this.startFrame, this.numChannels, buffersize, action);
//	}
//	
//	/*Synths defined in SoundFileHDSynthi*/
//	
//	play{|server| // aka preview;
//		var condition = Condition.new, b, synth;
//		server = server ? Server.default;
//		if(server.serverRunning.not){^this};
//		
//		r{
//			b = this.getCueBuffer(server, condition); 
//			condition.wait;
//			
//			synth = Synth("multiTracker_diskin_" ++ numChannels.asString, [\bufnum, b]);
//			r{
//				this.duration.wait;
//				synth.free;
//				b.close;b.free;
//			}.play;
//		}.run;
//	}	
	
}