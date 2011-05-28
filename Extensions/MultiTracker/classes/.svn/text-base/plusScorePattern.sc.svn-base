/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

+ Pattern {

//asScoreStreamPlayer{|protoEvent|
//		^ScoreStreamPlayer(this.asStream, protoEvent);
//
//}
//	
//asScore{|numberOfEvents=100, protoEvent|
//
//	^this.asScoreStreamPlayer(protoEvent).read(numberOfEvents)
//}	

test{|times=100|
	var stream;
	stream = this.asStream;
	times.do{
		stream.next.postln;
	}
	}

}