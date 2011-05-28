/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

+ Pattern {
	//FIXME: this should be better a player Event ...
	static{|dur=0, method=\duration|
		var z,d,p;
		if(method === \duration){^this.staticMaxDur(dur)};
		z = this.asStream;
		d= ();
		dur.do{
			z.next(Event.default).keysValuesDo{|k,v, i|
				d[k] = d[k].add(v);
			}
		};		
		p=Array.newClear(d.size);
		d.keysValuesDo{|k,v, i|
			p[i] = [k, Pseq(v, v.size)];
		};
		p.flatten;
		^this.class.new(*p);
	}

	staticMaxDur{|dur=0|
		var z,d,p, time=0, nexter=0, new;
		z = this.asStream;
		d= ();
		while({time < dur and: nexter.notNil},{
			nexter = z.next(Event.default);
			if(nexter.notNil){
				nexter.keysValuesDo{|k,v, i|
					if(k === \dur){
						time = time + v;
//						this.debug(time);
					};
					if(time <= dur){
						d[k] = d[k].add(v);
					}
				}
			}
		});		
		p=Array.newClear(d.size);
		d.keysValuesDo{|k,v, i|
			p[i] = [k, Pseq(v, v.size)];
		};
		p.flat.flat;
		 new = this.class.new(*p);
		 new.patternpairs = new.patternpairs.flat;
		 ^new
	}
	
	getDuration{
	 	var durArray, index,patarr;
		patarr = this.patternpairs;
		index = patarr.indexOf(\dur);
		if(index.isNil){^nil};
		durArray = patarr[index+1];
		if(durArray.isKindOf(Pseq).not){
			"Pattern-getDuration works only with Pseq right now. Use Pattern-staticMaxDur to convert.".warn;
			^nil
		};
		^durArray.list.sum;
		
		
	}
}

/*
Pattern-play

z = Pbind(\dur, Pwhite(0.1,1,2), \degree, Pxrand([1,2,3],inf));
x = z.staticMaxDur(10);
x.getDuration;

x.asCompileString

x = z.asScore(100)
x.score
z.asStream
EventStreamPlayer(z.asStream, Event.parentEvents.default);

x = z.asEventStreamPlayer(Event.parentEvents.default)
x.next(0.0)
x.event
x.stream
z.asScore
z = z.static(10)
z.postcs
z = z.asStream
e = [];
Pattern-asScore
x = ( a: (b: 9))
x.a.b

ControlSpace.all[\mixer].envir[\amp_3]
x = Score.new;
z = Pbind(\dur, 0.1, \degree, Pxrand([1,2,3],inf));
z.asScore;
y = z.asScoreStreamPlayer
z = y.read
z.score
y.play
x.score

x.event

SoundFileView

Event.parentEvents.default

20.do{
e = e.add(z.next(Event.default))
};
d = ();
e.do{|ev|
	ev.keysValuesDo{|k,v, i|
		d[k] = d[k].add(v);
		
	}
};

p=Array.newClear(d.size);

d.keysValuesDo{|k,v, i|
	[k,v,i].postln;
	p[i] = [k,Pseq(v,v.size)];
};
p.flatten;

Pbind(*p);

*/