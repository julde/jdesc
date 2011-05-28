SynthDef(\SinRHPF,{|out=0,fade=1.5,amp=1,gate=1,weight=200,base=200,sawfreq=4,sinfreq=1|
var sound,env,ctl;
env=EnvGen.kr(Env.asr(fade,1, fade, 1), gate, doneAction:2)*amp;
ctl = RHPF.kr(LFSaw.kr(sawfreq), SinOsc.kr(sinfreq, 0, 35, 40), 0.05);
sound=	SinOsc.ar(ctl * weight + base) * 0.2;
Out.ar(out,sound*env)
},metadata: (
type:\sine,
specs: (amp:\amp,fade:\fade,sawfreq:\freqsmall,sinfreq:\freqsmall,weight:\midfreq,base:\freq)))
