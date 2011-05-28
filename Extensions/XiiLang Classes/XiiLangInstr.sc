
XiiLangInstr {
	
	classvar instrDict;
	var project;
	var sampleNames, samplePaths, nrOfSampleSynthDefs;
	
	*new {| project |
		^super.new.initXiiLangInstr(project);
		}
		
	initXiiLangInstr {| argproject |		
		project = argproject;
	
		// ----------------------------------------------------------------------------------
		// ---------------------------- percussive instruments  -----------------------------
		// ----------------------------------------------------------------------------------
		
		// ---------------------- sample based instruments -----------------------------
		
		samplePaths = ("sounds/ixilang/"++project++"/*").pathMatch;
		sampleNames = samplePaths.collect({ |path| path.basename.splitext[0]});

		// 52 is the number of keys (lower and uppercase letters) supported
		nrOfSampleSynthDefs = if(sampleNames.size < 52, {52}, {sampleNames.size});

		nrOfSampleSynthDefs.do({arg i;
			var file, chnum, filepath;
			filepath = samplePaths.wrapAt(i);
			file = SoundFile.new;
			file.openRead(filepath);
			chnum = file.numChannels;
			file.close;
			SynthDef(sampleNames.wrapAt(i).asSymbol, {arg out=0, freq=440, amp=0.3, pan=0, noteamp=1, dur;
					var buffer, player, env, signal;
					buffer = Buffer.read(Server.default, filepath);
					player= Select.ar(noteamp,
						[ // playMode 2 - the sample player mode
						if(chnum==1, { 
						LoopBuf.ar(1, buffer, (freq.cpsmidi-60).midiratio, 1, 0, 0, 44100*60*10)!2 }, {
						LoopBuf.ar(2, buffer, (freq.cpsmidi-60).midiratio, 1, 0, 0, 44100*60*10)})
						* EnvGen.ar(Env.linen(0.0001, 60*60, 0.0001))
						, // playMode 1 - the rhythmic mode
						if(chnum==1, { 
						PlayBuf.ar(1, buffer, (freq.cpsmidi-60).midiratio)!2 }, {
						PlayBuf.ar(2, buffer, (freq.cpsmidi-60).midiratio)})
						* EnvGen.ar(Env.perc(0.01, 0.4))
						]);
					// I use DetectSilence rather than doneAction in Env.perc, as a doneAction in Env.perc
					// would also be running (in Select) thus killing the synth even in {} mode
					// I therefore add 0.02 so the 
					DetectSilence.ar(player, 0.001, 0.1, 2);
					//signal = player * amp * Lag.kr(noteamp, dur); // works better without lag
					signal = player * amp * noteamp;
					Out.ar(out, signal);
			}).memStore;
		
		});
		/*
		 Synth(\machine)
		*/

		// ---------------------- synthesized instruments -----------------------------
		
		SynthDef(\impulse, { arg out=0, pan=0, amp=1;
			var x, imp;
			imp = Impulse.ar(1);
			x = Pan2.ar(imp * EnvGen.ar(Env.perc(0.0000001, 0.2), doneAction:2), pan) * amp;
			Out.ar(out, LeakDC.ar(x));
		}).memStore;
		
		SynthDef(\kick,{ arg out=0, pan=0, amp=0.3, mod_freq = 2.6, mod_index = 5, sustain = 0.4, beater_noise_level = 0.025;
			var pitch_contour, drum_osc, drum_lpf, drum_env;
			var beater_source, beater_hpf, beater_lpf, lpf_cutoff_contour, beater_env;
			var kick_mix, freq = 80;
			pitch_contour = Line.kr(freq*2, freq, 0.02);
			drum_osc = PMOsc.ar(	pitch_contour,
						mod_freq,
						mod_index/1.3,
						mul: 1,
						add: 0);
			drum_lpf = LPF.ar(in: drum_osc, freq: 1000, mul: 1, add: 0);
			drum_env = drum_lpf * EnvGen.ar(Env.perc(0.005, sustain), 1.0, doneAction: 2);
			beater_source = WhiteNoise.ar(beater_noise_level);
			beater_hpf = HPF.ar(in: beater_source, freq: 500, mul: 1, add: 0);
			lpf_cutoff_contour = Line.kr(6000, 500, 0.03);
			beater_lpf = LPF.ar(in: beater_hpf, freq: lpf_cutoff_contour, mul: 1, add: 0);
			beater_env = beater_lpf * EnvGen.ar(Env.perc(0.000001, 1), doneAction: 2);
			kick_mix = Mix.new([drum_env, beater_env]) * 2 * amp;
			Out.ar(out, Pan2.ar(kick_mix, pan))
		}).memStore;

		SynthDef(\kick2, {	arg out=0, amp=0.3, pan=0;
			var env0, env1, env1m, son;
			
			env0 =  EnvGen.ar(Env.new([0.5, 1, 0.5, 0], [0.005, 0.06, 0.26], [-4, -2, -4]), doneAction:2);
			env1 = EnvGen.ar(Env.new([110, 59, 29], [0.005, 0.29], [-4, -5]));
			env1m = env1.midicps;
			
			son = LFPulse.ar(env1m, 0, 0.5, 1, -0.5) + WhiteNoise.ar(1);
			son = LPF.ar(son, env1m*1.5, env0);
			son = son + SinOsc.ar(env1m, 0.5, env0);
			
			son = son * 1.2;
			son = son.clip2(1);
			
			Out.ar(out, Pan2.ar(son * amp));
		}).memStore;		
				
		SynthDef(\kick3, { arg out=0, amp=0.3, pan=0, dur=0.35, high=150, low=33, phase=1.5;
			var signal;
			signal = SinOsc.ar(XLine.kr(high, low, dur), phase*pi, amp);
			//signal = signal * EnvGen.ar(Env.new([1,0],[dur]), gate, doneAction:2);
			signal = signal * EnvGen.ar(Env.perc(0.0001, 1.2), doneAction:2);
			Out.ar(out, Pan2.ar(signal, pan));
		}).memStore;
		
		SynthDef(\snare, {arg out=0, amp=0.3, pan=0, sustain = 0.04, drum_mode_level = 0.15,
			snare_level = 50, snare_tightness = 1200, freq = 305;
			var drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc, drum_mode_mix, drum_mode_env;
			var snare_noise, snare_brf_1, snare_brf_2, snare_brf_3, snare_brf_4, snare_reson;
			var snare_env, snare_drum_mix;
		
			drum_mode_env = EnvGen.ar(Env.perc(0.005, sustain), doneAction: 2);
			drum_mode_sin_1 = SinOsc.ar(freq*0.53, 0, drum_mode_env * 0.5);
			drum_mode_sin_2 = SinOsc.ar(freq, 0, drum_mode_env * 0.5);
			drum_mode_pmosc = PMOsc.ar(	Saw.ar(freq*0.85),
							184,
							0.5/1.3,
							mul: drum_mode_env*5,
							add: 0);
			drum_mode_mix = Mix.new([drum_mode_sin_1, drum_mode_sin_2, drum_mode_pmosc]) * drum_mode_level;
		// choose either noise source below
		//	snare_noise = WhiteNoise.ar(amp);
			snare_noise = LFNoise0.ar(9000, amp*0.8); // play with the frequency here
			snare_env = EnvGen.ar(Env.perc(0.0001, sustain), doneAction: 2);
			snare_brf_1 = BRF.ar(in: snare_noise, freq: 8000, mul: 0.5, rq: 0.1);
			snare_brf_2 = BRF.ar(in: snare_brf_1, freq: 5000, mul: 0.5, rq: 0.1);
			snare_brf_3 = BRF.ar(in: snare_brf_2, freq: 3600, mul: 0.5, rq: 0.1);
			snare_brf_4 = BRF.ar(in: snare_brf_3, freq: 2000, mul: snare_env, rq: 0.1);
			snare_reson = Resonz.ar(snare_brf_4, snare_tightness, mul: snare_level) ;
			snare_drum_mix = Mix.new([drum_mode_mix, snare_reson]) * amp;
			Out.ar(out, [snare_drum_mix, snare_drum_mix])
		}).memStore;
				
		SynthDef(\bar, {arg out = 0, pan=0, freq = 6000, sustain = 0.2, amp=0.3;
			var root_cymbal, root_cymbal_square, root_cymbal_pmosc;
			var initial_bpf_contour, initial_bpf, initial_env;
			var body_hpf, body_env;
			var cymbal_mix;
			
			root_cymbal_square = Pulse.ar(freq, 0.5, mul: 0.81);
			root_cymbal_pmosc = PMOsc.ar(root_cymbal_square,
							[freq*1.34, freq*2.405, freq*3.09, freq*1.309],
							[310/1.3, 26/0.5, 11/3.4, 0.72772],
							mul: 1,
							add: 0);
			root_cymbal = Mix.new(root_cymbal_pmosc);
			initial_bpf_contour = Line.kr(15000, 9000, 0.1);
			initial_env = EnvGen.ar(Env.perc(0.005, 0.1), 1.0);
			initial_bpf = BPF.ar(root_cymbal, initial_bpf_contour, mul:initial_env);
			body_env = EnvGen.ar(Env.perc(0.005, sustain, 1, -2), doneAction: 2);
			body_hpf = HPF.ar(in: root_cymbal, freq: Line.kr(9000, 12000, sustain),mul: body_env, add: 0);
			cymbal_mix = Mix.new([initial_bpf, body_hpf]) * amp;
			Out.ar(out, Pan2.ar(cymbal_mix, pan))
		}).memStore;

		SynthDef(\clap, {arg out=0, pan=0, amp=0.3, filterfreq=50, rq=0.01;
			var env, signal, attack,Ê noise, hpf1, hpf2;
			noise = WhiteNoise.ar(1)+SinOsc.ar([filterfreq/2,filterfreq/2+4 ], pi*0.5, XLine.kr(1,0.01,4));
			//noise = PinkNoise.ar(1)+SinOsc.ar([(filterfreq)*XLine.kr(1,0.01,3), (filterfreq+4)*XLine.kr(1,0.01,3) ], pi*0.5, XLine.kr(1,0.01,4));
			//signal = signal * SinOsc.ar(1,0.75);
			hpf1 = RLPF.ar(noise, filterfreq, rq);
			hpf2 = RHPF.ar(noise, filterfreq/2, rq/4);
			env = EnvGen.kr(Env.perc(0.003, 0.00035));
			signal = (hpf1+hpf2) * env;
			signal = CombC.ar(signal, 0.5, 0.03, 0.031)+CombC.ar(signal, 0.5, 0.03016, 0.06);
			//signal = Decay2.ar(signal, 0.5);
			signal = FreeVerb.ar(signal, 0.23, 0.15, 0.2);
			Out.ar(out, Pan2.ar(signal * amp, pan));
			DetectSilence.ar(signal, doneAction:2);
		}).memStore;
		
		SynthDef(\hat, { arg out=0, pan=0;
			var sig;
			// a release gate
			EnvGen.ar(Env.perc(0.00001, 2), doneAction: 2); 
			// the other env has problem with gate
			// (i.e. FAILURE n_set Node not found)
			sig = WhiteNoise.ar * EnvGen.ar(Env.perc(0.00001, 0.01));
			Out.ar(out, Pan2.ar(sig, pan));
		}).memStore;

		SynthDef(\cling, {arg out=0, amp=0.3, pan=0;
			var signal, env;
			env = EnvGen.ar(Env.perc(0.000001, 0.3), doneAction:2);
			signal = SinOsc.ar(3000).squared;
			Out.ar(out, Pan2.ar(signal*env, pan, amp));
		}).memStore;
		
		SynthDef(\cling2, {arg out=0, amp=0.3, pan=0;
			var signal, env;
			env = EnvGen.ar(Env.perc(0.000001, 0.5), doneAction:2);
			signal = LFSaw.ar(2000).squared;
			Out.ar(out, Pan2.ar(signal*env, pan, amp));
		}).memStore;
		


		// ----------------------------------------------------------------------------------
		// ------------------------------- melodic instruments  -----------------------------
		// ----------------------------------------------------------------------------------

/*
// a pattern to test the instruments
Pdef(\test, Pbind(\instrument, \fmsynth, \midinote, Prand([1, 2, 5, 7, 9, 3], inf) + 60, \dur, 0.8)).play;
*/

		// ---------------------- synthesized instruments -----------------------------
		
		// a crappy synth as to yet
		SynthDef(\fmsynth, {|out=0, freq=440, carPartial=8, modPartial=4.5, index=10, amp=0.3|
			var mod, car, env;
			// modulator frequency
			mod = SinOsc.ar(freq * modPartial, 0, freq * index );
			// carrier frequency
			car = SinOsc.ar((freq * carPartial) + mod, 0, amp );
			// envelope
			env = EnvGen.ar( Env.perc(0.01, 1), doneAction: 2);
			Out.ar( out, (car * env)!2)
		}).memStore;


		SynthDef(\bling, { arg out=0, pan=0, amp=0.3, freq=999;
			var x, y, env, imp;
			env = Env.perc(0.0000001, 0.5);
			imp = Impulse.ar(1);
			imp = Decay2.ar(imp, 0.01, 0.5, MoogFF.ar(VarSaw.ar(freq, 0.8, 0.5), freq*12, 3.6) );
			x = Pan2.ar(imp * EnvGen.ar(env, doneAction:2), pan) * amp*4;
			Out.ar(out, LeakDC.ar(x));
		}).memStore;

		/*
		SynthDef(\bass, {arg out, freq=220, amp=0.4;
			var env, signal;
			env = EnvGen.ar(Env.perc(0.01,2), doneAction:2);
			signal = SinOsc.ar([freq/2, (freq/2)+2], 0, amp) * env;
			Out.ar(out, signal*env);
		}).memStore;
		*/
		
		SynthDef(\bass, {arg out, freq=220, gate=1, amp=0.3;
			var env, signal;
			env = EnvGen.ar(Env.adsr(0.01,1.0, 0.6, 0.3), gate, doneAction:2);
			signal = MoogFF.ar(Saw.ar([freq/2, (freq/2)+0.8],  amp*2), freq*2, 3.4) * env;
			Out.ar(out, signal*env);
		}).memStore;


		/*
		 Synth(\bass, [\freq, 344])
		*/

		
		SynthDef(\moog, {arg out=0, freq=220, amp=0.3;
			var env, signal;
			env = EnvGen.ar(Env.perc(0.01, 0.6), doneAction:2);
			signal = MoogFF.ar(Saw.ar([freq, freq+2], amp), 7*freq, 3.3) * env;
			Out.ar(out, signal*env);
		}).memStore;

		/*
		 Synth(\moog, [\freq, 344])
		*/
		
		SynthDef(\bell, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, in, env;
		        env = EnvGen.kr(Env.perc(0.01, Rand(333, 666)/freq), doneAction:2);
		        x = Mix.ar([SinOsc.ar(freq, 0, 0.11), SinOsc.ar(freq*2, 0, 0.09)] ++
		        				Array.fill(6, {SinOsc.ar(freq*Rand(-5,5).round(0.125), 0, Rand(0.02,0.1))}));
		        //x = BPF.ar(x, freq, 4.91);
		        Out.ar(out, x!2*env*amp);
		}).memStore;
		/*
		 Synth(\bell, [\freq, 344])
		*/
		
		// rubbish! MdaPiano does not support microtonality !!! 
		SynthDef(\piano, { |out=0, freq=440, gate=1, amp=0.3|
			var sig = MdaPiano.ar(freq, gate, decay:0.9, release: 0.9, stereo: 0.3, sustain: 0);
			DetectSilence.ar(sig, 0.01, doneAction:2);
			Out.ar(out, sig * (amp*0.35));
		}).memStore;

		SynthDef(\clarinet, { |out=0, freq=440, gate=1, amp=0.3|
			var sig = StkClarinet.ar(freq, 44, 2, 77, 2, 88);
		     var env = EnvGen.kr(Env.perc(0.1, 1.1, amp), doneAction:2);
			Out.ar(out, sig * env* 0.6 !2 );
		}).memStore;

		SynthDef(\klang, {arg out=0, amp=0.3, t_trig=1, freq=100, rq=0.004;
			var env, signal;
			var rho, theta, b1, b2;
			b1 = 2.0 * 0.97576 * cos(0.161447);
			b2 = 0.9757.squared.neg;
			signal = SOS.ar(K2A.ar(t_trig), 1.0, 0.0, 0.0, b1, b2);
			signal = RHPF.ar(signal, freq, rq);
			signal = Decay2.ar(signal, 0.4, 0.8, signal);
			signal = Limiter.ar(Resonz.ar(signal, freq, rq*0.5), 0.9);
			DetectSilence.ar(signal, 0.01, doneAction:2);
			Out.ar(out, signal*(amp*6)!2);
		}).memStore;


		SynthDef(\elbass, {arg out=0, amp=0.3, t_trig=1, freq=100, rq=0.004;
			var env, signal;
			var rho, theta, b1, b2;
			b1 = 1.98 * 0.989999999 * cos(0.09);
			b2 = 0.998057.neg;
			signal = SOS.ar(K2A.ar(t_trig), 0.123, 0.0, 0.0, b1, b2);
			signal = RHPF.ar(signal, freq, rq) + RHPF.ar(signal, freq*0.5, rq);
			signal = Decay2.ar(signal, 0.4, 0.3, signal);

			DetectSilence.ar(signal, 0.01, doneAction:2);
			Out.ar(out, signal*(amp*0.65)!2);
		}).memStore;

		SynthDef(\marimba, {arg out=0, amp=0.3, t_trig=1, freq=100, rq=0.006;
			var env, signal;
			var rho, theta, b1, b2;
			b1 = 1.987 * 0.9889999999 * cos(0.09);
			b2 = 0.998057.neg;
			signal = SOS.ar(K2A.ar(t_trig), 0.3, 0.0, 0.0, b1, b2);
			signal = RHPF.ar(signal*0.8, freq, rq) + DelayC.ar(RHPF.ar(signal*0.9, freq*0.99999, rq*0.999), 0.02, 0.01223);
			signal = Decay2.ar(signal, 0.4, 0.3, signal);
			DetectSilence.ar(signal, 0.01, doneAction:2);
			Out.ar(out, signal*(amp*0.65)!2);
		}).memStore;

		SynthDef(\marimba2, {arg out=0, amp=0.3, t_trig=1, freq=100, rq=0.006;
			var env, signal;
			var rho, theta, b1, b2;
			b1 = 1.987 * 0.9889999999 * cos(0.09);
			b2 = 0.998057.neg;
			signal = SOS.ar(K2A.ar(t_trig), 0.3, 0.0, 0.0, b1, b2);
			signal = RHPF.ar(signal*0.8, freq, rq) + DelayC.ar(RHPF.ar(signal*0.9, freq*0.99999, rq*0.999), 0.02, 0.018223);
			//signal = Decay2.ar(signal, 0.4, 0.3, signal);
			signal = Decay2.ar(signal, 0.4, 0.3, signal*SinOsc.ar(freq)); // modulating
			DetectSilence.ar(signal, 0.01, doneAction:2);
			Out.ar(out, signal*(amp*0.65)!2);
		}).memStore;


		SynthDef(\wood, {arg out=0, amp=0.3, pan=0, t_trig=1, freq=100, rq=0.06;
			var env, signal;
			var rho, theta, b1, b2;
			b1 = 2.0 * 0.97576 * cos(0.161447);
			b2 = 0.9757.squared.neg;
			signal = SOS.ar(K2A.ar(t_trig), 1.0, 0.0, 0.0, b1, b2);
			//signal = RHPF.ar(signal, freq, rq);
			signal = Decay2.ar(signal, 0.4, 0.8, signal);
			signal = Limiter.ar(Resonz.ar(signal, freq, rq*0.5), 0.9);
			env = EnvGen.kr(Env.perc(0.00001, 1.1, amp), doneAction:2);
			Out.ar(out, Pan2.ar(signal, pan)*env);
		}).memStore;

		SynthDef(\xylo, { |out=0, freq=440, amp=0.3, pan=0|
			var sig = StkBandedWG.ar(freq, instr:1, mul:3);
		     var env = EnvGen.kr(Env.perc(0.01, 1.1, amp), doneAction:2);
			Out.ar(out, Pan2.ar(sig, pan, env));
		}).memStore;

		SynthDef(\softwg, { |out=0, freq=440, gate=1, amp=0.3, pan=0|
			var sig = StkBandedWG.ar(freq, instr:1, mul:3);
		     var env = EnvGen.kr(Env.perc(0.4, 1.1, amp), doneAction:2);
			Out.ar(out, Pan2.ar(sig, pan, env));
		}).memStore;

		SynthDef(\sines, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq, amp), doneAction:2);
		        x = Mix.ar(Array.fill(8, {SinOsc.ar(freq*IRand(1,10),0, 0.08)}));
		        x = LPF.ar(x, 20000);
		        x = Pan2.ar(x,pan);
		        Out.ar(out, x*env);
		}).memStore;
		
		SynthDef(\synth, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 220/freq), doneAction:2);
		        x = Mix.ar([FSinOsc.ar(freq, pi/2, 0.5), Pulse.ar(freq, Rand(0.3,0.5), 0.5)]);
		        x = LPF.ar(x, 20000);
		        x = Pan2.ar(x,pan);
		        Out.ar(out, LeakDC.ar(x)*env*amp*0.8);
		}).memStore;

		SynthDef(\string, {arg out=0, freq=440, pan=0, amp=0.3;
			var pluck, period, string;
			pluck = PinkNoise.ar(Decay.kr(Impulse.kr(0.005), 0.05));
			period = freq.reciprocal;
			string = CombL.ar(pluck, period, period, 4);
			string = LeakDC.ar(LPF.ar(Pan2.ar(string, pan), 12000)) * amp;
			DetectSilence.ar(string, doneAction:2);
			Out.ar(out, string)
		}).memStore;

		SynthDef(\drop, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.001, 1), doneAction:2);
		        x = Resonz.ar(PinkNoise.ar(1), freq*4, 0.005);
		        x = Pan2.ar(x,pan);
		        Out.ar(out, LeakDC.ar(x)*env*amp*70);
		}).memStore;
		
		SynthDef(\crackle, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.01, 1), doneAction:2);
		        x = Resonz.ar(Crackle.ar(1.95, 2), freq*4, 0.1);
		        x = Pan2.ar(x,pan);
		        Out.ar(out, LeakDC.ar(x)*env*amp*8);
		}).memStore;

		SynthDef(\glass, {arg out=0, freq=440, dur=1, amp=0.3, pan=0;
		        var x, env;
		        env = EnvGen.kr(Env.perc(0.0001, 1), doneAction:2);
		        x = Decay2.ar(Resonz.ar(Impulse.ar(1), freq*4, 0.005), 0.001, 0.74, 3);
		        x = Pan2.ar(x,pan);
		        Out.ar(out, LeakDC.ar(x)*env*amp*50);
		}).memStore;

		^this.makeInstrDict;
		
	}
	
	makeInstrDict{ // this is where keys are mapped to instruments (better done by hand and design)
	
		// if sounds folder contains a key mapping file, then it is used, 
		// else, the instrDict is create by putting random files onto the letters
		
		var file;
		\deb0.postln;
		if(Object.readArchive("sounds/ixilang/"++project++"/keymappings.ixi").isNil, {
		\deb1.postln;
			instrDict = IdentityDictionary.new;
			[\A, \a, \B, \b, \C, \c, \D, \d, \E, \e, \F, \f, \G, \g, \H, \h, \I, \i, \J, \j,
			\K, \k, \L, \l, \M, \m, \N, \n, \O, \o, \P, \p, \Q, \q, \R, \r, \S, \s, \T, \t,
			\U, \u, \V, \v, \W, \w, \X, \x, \Y, \y, \Z, \z].do({arg letter, i;
				instrDict[letter] = sampleNames.wrapAt(i).asSymbol;
			});
			"ixiLang-NOTE: No key mappings were found, they will be randomly generated".postln;
		}, {
				\deb2.postln;
			instrDict = Object.readArchive("sounds/ixilang/"++project++"/keymappings.ixi");
			\deb3.postln;	
		});

		^instrDict;
		
		
	/*
	// code used to generate the initial keymappings file
		// map the keys to the names of the soundfiles inside your project folder
		var instrDict, project; // project is the folder name
		
		instrDict = IdentityDictionary.new;
		project = "default";
		
		instrDict[\A] = \bellrip3;	
		instrDict[\a] = \insec3;
				
		instrDict[\B] = \bell;		
		instrDict[\b] = \bellrip;	
		
		instrDict[\C] = \click;	
		instrDict[\c] = \clap;	
		
		instrDict[\D] = \chainSpade;	
		instrDict[\d] = \dorje;		
		
		instrDict[\E] = \laekur;	
		instrDict[\e] = \bellrip2;	
		
		instrDict[\F] = \wood;
		instrDict[\f] = \firecrack;	

		instrDict[\G] = \insec2;	
		instrDict[\g] = \chain;
		
		instrDict[\H] = \heart;	
		instrDict[\h] = \hat;
		
		instrDict[\I] = \ice;	
		instrDict[\i] = \impulse;	

		instrDict[\J] = \knock;
		instrDict[\j] = \drr;
	
		instrDict[\K] = \cling2;	
		instrDict[\k] = \cling;	

		instrDict[\L] = \iron;	
		instrDict[\l] = \noinoi;	
		
		instrDict[\M] = \machine;	
		instrDict[\m] = \hapsi;	

		instrDict[\N] = \spark;	
		instrDict[\n] = \dalispark;		
		
		instrDict[\O] = \kick2;
		instrDict[\o] = \kick3;
		
		instrDict[\P] = \paper;	
		instrDict[\p] = \ironrip;	
		
		instrDict[\Q] = \snow;	
		instrDict[\q] = \snork;	

		instrDict[\R] = \pork;
		instrDict[\r] = \stretch;
			
		instrDict[\S] = \rocks;	
		instrDict[\s] = \insec;	
		
		instrDict[\T] = \xylo;	
		instrDict[\t] = \triplet;
		
		instrDict[\U] = \revbell;
		instrDict[\u] = \bell;	
		
		instrDict[\V] = \spade;	
		instrDict[\v] = \bee;	

		instrDict[\W] = \firespark;	
		instrDict[\w] = \firespark2;	
		
		instrDict[\X] = \bar;	
		instrDict[\x] = \snare;
		
		instrDict[\Y] = \camina1;	
		instrDict[\y] = \camina2;	
		
		instrDict[\Z] = \camina3;	
		instrDict[\z] = \camina4;	
		
		instrDict.writeArchive("sounds/ixilang/"++project++"/keymappings.ixi");

	*/

		
	}
	
	*getPercussiveInstr {
		var dict, string;
		dict = instrDict.getPairs;
		string = " ";
		dict.do({arg item, i; 
			string = string++item;
			if(i.even, {
				string = string++"  :  ";
			}, {
				string = string++"\n"++" ";
			});
		});
		^string;
	}

	*getMelodicInstr {
		^["bling", "piano", "elbass", "marimba", "marimba2", "clarinet", "klang", "wood", "xylo", "softwg", 
		"bass", "moog", "bell", "sines", "synth", "string", "drop", "crackle", "glass"].asCompileString
	}
	
}