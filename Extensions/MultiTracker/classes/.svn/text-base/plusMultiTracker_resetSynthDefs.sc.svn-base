+ MultiTracker{

	*resetSynthDefs{
	
		SynthDef(\multiTracker_diskin_1, { arg gate=1, out=0, attack=0.01, decay=0.1, i_bufnum=0, amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(1, i_bufnum) * amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack, 1, decay), gate, doneAction:2)			)
		}).store;
		SynthDef(\multiTracker_diskin_2, { arg gate=1, out=0, attack=0.01, decay=0.1, i_bufnum=0, amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(2, i_bufnum)* amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack, 1, decay), gate, doneAction:2)			)
		}).store;					
		SynthDef(\multiTracker_diskin_3, { arg gate=1, out=0, attack=0.01, decay=0.1, i_bufnum=0, amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(2, i_bufnum)* amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack, 1, decay), gate, doneAction:2)			)
		}).store;	
		SynthDef(\multiTracker_diskin_4, { arg gate=1, out=0, attack=0.01, decay=0.1, i_bufnum=0, amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(4, i_bufnum)* amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack, 1, decay), gate, doneAction:2)			)
		}).store;		
		SynthDef(\multiTracker_diskin_5, { arg gate=1, out=0, attack=0.01, decay=0.1,i_bufnum=0, amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(5, i_bufnum)* amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack,1,decay), gate, doneAction:2)			)
		}).store;	
		SynthDef(\multiTracker_diskin_8, { arg gate=1, out=0, attack=0.01, decay=0.1, i_bufnum=0,amp=0.7;	
			var soundout;		
			soundout = DiskIn.ar(8, i_bufnum)* amp;
			OffsetOut.ar(out, 		
				soundout * EnvGen.kr(Env.asr(attack, 1, decay), gate, doneAction:2)			)
		}).store;	
	
	}

}