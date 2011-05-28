/* 	(c) 2006 Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

// unfortunately this is broken ; -(

+ Symbol {

//	asMultiTrackObject{|track, time, duration, args|
//		var synthi;
//		synthi = Synthi(this, args, play:false);
//		^SynthiMultiTrackObject(synthi).time_(time).duration_(duration).track_(track).args_(args)
//	}
	asMultiTrackObject{|track, time, duration, args|
		var obj, ev;
		args = args ? ();
		if(args.isKindOf(Array)){
			ev = ();
			args.pairsDo{|dit, dat|
				ev.put(dit, dat);
			};
			args = ev;

		};
		
//		obj = [this, args];
		args.put(\instrument, this);
		obj = args;

		^SynthiMultiTrackObject(obj).time_(time).duration_(duration).track_(track).args_(args)
	}	

}

+ AbstractFunction {

	asMultiTrackObject{|track, time, duration, args|
		^FunctionMultiTrackObject(this).time_(time).duration_(duration).track_(track).args_(args)
	}

}

+ String {
	
	asMultiTrackObject{|track, time, duration, args|
		var path, name, bufnum, out;
		if(this.first == $!){
			^MComment(this).asMultiTrackObject(track, time, duration, args);
		};
		path  = this.pathMatch[0];
		if(path.isNil){("File not found: " ++ this).warn;^nil};
		args = args ? [\startFrame, 0];
		out = SoundFileFromDiskMultiTrackObject(MultiTrackSoundFile.openReadAndClose(path))
			.time_(time).duration_(duration).track_(track).args_(args);
		out.object.duration_(duration); //FIXME should be set in SoundFileFromDiskMultiTrackObject
		^out
	}
	
}


+ MultiTrackSoundFile {
	
	asMultiTrackObject{|track, time, duration, args|
		var path, name, bufnum, out;
//		if(path.isNil){("File not found: " ++ this).warn;^nil};
		out = SoundFileFromDiskMultiTrackObject(this)
			.time_(time).duration_(duration).track_(track).args_(args);
		out.object.duration_(duration); //FIXME should be set in SoundFileFromDiskMultiTrackObject
		^out
	}
	
}

+ MultiTracker{
	
	asMultiTrackObject{|track, time, duration, args|
		^MultiTrackerMultiTrackObject(this)
			.time_(time).duration_(duration).track_(track).args_(args)
	}

}

+ Pbind{
	
	asMultiTrackObject{|track, time, duration, args|
		^PbindMultiTrackObject(this)
			.time_(time).duration_(duration).track_(track).args_(args)
	}

}


+ Event {

	asMultiTrackObject{|track, time, duration, args|
		^EventMultiTrackObject(this)
			.time_(time).duration_(duration).track_(track).args_(args)
	}

}

+ Array{
	
	asMultiTracker{|name,aserver, aclock, numTracks|
		var m;
		if(this[0][0].respondsTo(\at)){
			numTracks = numTracks ? this.size;
			m = MultiTracker(name, aserver, aclock, numTracks: numTracks);

			this.do({|track, i|
			track.do{|val|
					if({val.last.isKindOf(Array) and: {val.size < 5}}.value or: 
						{val.last.isKindOf(Array).not and: {val.size < 4}.value}
					){
						m.put(i, val[0], val[1], val[2], val[3]);
					}{
						m.put(i, val[1], val[2], val[3], val[4]);
					
					};
				};
			});
		
		}{
			numTracks = numTracks ? this.size;
			m = MultiTracker(name,aserver, aclock,  numTracks: numTracks);

			this.do{|val|
				if({val.last.isKindOf(Array) and: {val.size < 5}}.value or: 
					{val.last.isKindOf(Array).not and: {val.size < 4}.value}
				){
					m.put(0, val[0], val[1], val[2], val[3]);
				}{
					m.put(val[0], val[1], val[2], val[3], val[4]);
				
				};
			};
		};
		^m
	
	}
}



+ MultiTracker{
	
	asArray{|mode|
		var stream, obj, tr, array, sa, idx;
		mode = mode ? \arrayPerTrack;
		array = [];
		this.tracks.do{|track|
			tr = [];
			stream = track.asStream;
			track.data.size.do{
				obj =  stream.next;
				sa = [obj.time, obj.duration, obj.asArrayName];
				obj.args !? {
					if(obj.args.indexOf(\i_bufnum).notNil){
						idx = obj.args.indexOf(\i_bufnum);
						2.do{obj.args.removeAt(idx)};
						//"removing argument: i_bufnum".warn
					};
					sa = sa.add(obj.args)	
				};
				tr = tr.add(sa);
			};
			if(tr.size>0){
				array = array.add(tr);
			};
		};
		^array
	}
	
	asArrayCompileString{
		var array, stream;
		stream = PrettyPrintStream.new;
		array = this.asArray;
		stream << "[";
		array.do{|arr, i|
			if (i != 0) { stream.comma.space; };
			
			stream << "[";
			stream.nl;
			stream << "/* track: " << i.asString << " *********/";
			stream.nl;

			arr.do{|a, j|
			if (j != 0) { stream.comma.nl; };	
				//a.storeOn(stream);
				stream << a.asCompileString;
				stream.nl;
				
			};
			stream.nl;
			stream << "]";
			

		};
			stream << "]";
	
		^stream.contents
	}

}


+ Array {
	storeItemsOn { | stream |
		this.do { | item, i |
			if (stream.atLimit) { ^this };
			if (i != 0) { stream.comma.space; };
			item.storeOn(stream);
		};
	}
}