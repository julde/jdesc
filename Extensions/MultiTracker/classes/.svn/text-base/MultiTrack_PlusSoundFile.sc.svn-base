/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

/* Soundfile convenience functions
*/

+ MultiTracker {

	putSoundFile{|path, tracknum, time, duration, startFrame=0|
		var snd;
//		this.debug(duration);
		if(path.isNil){^this};
		snd = SoundFile.openRead(path.standardizePath);
		if(snd.isNil){("soundfile " ++ path ++ " not found").warn; ^this };
		snd.close;
		duration = duration ? snd.duration;
		time = time ? currentTime;
		tracknum = tracknum ? 0;
		this.put(tracknum, time, duration, snd.path, [\startFrame, startFrame, \amp, Env([1.0,1.0],[duration], \cubed)]);
	}

	putSoundFileDialog{
			CocoaDialog.getPaths({arg paths; 
				paths.do{|pa, i|
					this.putSoundFile(pa,i);
				};
			});
	}
}