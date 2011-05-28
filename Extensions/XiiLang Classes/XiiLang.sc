
/*

// XiiLang.new("project", key) - "project" stands for the name of the folder where soundfiles are kept. in this folder a _keymappings.ixi file can be found that maps letters to sounds. if there is no file, then mapping will be random

nully warp virotic

TODO: Fix sequence (DONE?)
TODO: Add note duration control
TODO: Add parameter effect control
TODO: Add a guitar synth

*/


XiiLang {	
	classvar globaldocnum;
	var doc, docnum, oncolor, offcolor, processcolor, proxyspace, groups;
	var agentDict, instrDict, effectDict; //, effectRegDict; //, codeDict;
	var scale, tuning, chosenscale, tonic;
	var midiclient, eventtype;
	
	*new { arg project = "default", key = "C"; // current doc and the project to use (folder of soundfiles)
		^super.new.initXiiLang( project, key );
		}
		
	initXiiLang {arg project, key;
		if(globaldocnum.isNil, {
			globaldocnum = 0;
		}, {
			globaldocnum = globaldocnum+1;
		});
		// the number of this document (allows for multiple docs using same variable names)
		docnum = globaldocnum; // this number is now added in front of all agent names
		scale = Scale.major.degrees.add(12); // this is degrees
		chosenscale = Scale.major; // this is scale representation
		tuning = \et12; // default tuning
		tonic = 60 + [\C, \Cs, \D, \Ds, \E, \F, \Fs, \G, \Gs, \A, \As, \B].indexOf(key.toUpper.asSymbol); // midinote 60 is the default
		oncolor = Color.white;
		offcolor = Color.green;
		processcolor = Color.yellow;
		agentDict = IdentityDictionary.new;
		MIDIClient.init(0, 1);
		midiclient = MIDIOut(0, MIDIClient.destinations[0].uid);
		eventtype = \note;
		this.makeEffectDict; // in a special method, as it has to be called after every cmd+dot
		this.envirSetup;
		instrDict = XiiLangInstr.new(project);
		proxyspace = ProxySpace.new.know_(true);
		groups = ();
		CmdPeriod.add({
			16.do({arg i; midiclient.allNotesOff(i) });
			this.makeEffectDict;
			groups = ();
			agentDict = IdentityDictionary.new;
			proxyspace = ProxySpace.new.know_(true);
		});
	}

	makeEffectDict {
		effectDict = IdentityDictionary.new; // the effects below need to be added to the initEffect method as well
		effectDict[\reverb] 	= {arg sig; (sig*0.6)+FreeVerb.ar(sig, 0.85, 0.86, 0.3)};
		effectDict[\reverbL] 	= {arg sig; (sig*0.6)+FreeVerb.ar(sig, 0.95, 0.96, 0.7)};
		effectDict[\reverbS] 	= {arg sig; (sig*0.6)+FreeVerb.ar(sig, 0.45, 0.46, 0.2)};
		effectDict[\delay]  	= {arg sig; sig + AllpassC.ar(sig, 1, 0.15, 1.3 )};
		effectDict[\lowpass] 	= {arg sig; RLPF.ar(sig, 1000, 0.2)};
		effectDict[\tremolo]	= {arg sig; (sig * SinOsc.ar(2.1, 0, 5.44, 0))*0.5};
		effectDict[\vibrato]	= {arg sig; PitchShift.ar(sig, 0.008, SinOsc.ar(2.1, 0, 0.11, 1))};
		effectDict[\techno] 	= {arg sig; RLPF.ar(sig, SinOsc.ar(0.1).exprange(880,12000), 0.2)};
		effectDict[\technosaw] 	= {arg sig; RLPF.ar(sig, LFSaw.ar(0.2).exprange(880,12000), 0.2)};
		effectDict[\distort] 	= {arg sig; (3111.33*sig.distort/(1+(2231.23*sig.abs))).distort*0.02};
		effectDict[\cyberpunk]	= {arg sig; Squiz.ar(sig, 4.5, 5, 0.1)};
		effectDict[\bitcrush]	= {arg sig; Latch.ar(sig, Impulse.ar(11000*0.5)).round(0.5 ** 6.7)};
		effectDict[\antique]	= {arg sig; LPF.ar(sig, 1700) + Dust.ar(7, 0.6)};
	}
			
	//set up document and the keydown control
	envirSetup {
		doc = Document.current;
		doc.name_("ixilang live coder - window" + docnum.asString);
		doc.background_(Color.black);
		doc.stringColor_(oncolor);
		doc.string_("");
		doc.font_(Font("Monaco",20));
		doc.promptToSave_(false);
		doc.onClose_({ 
			proxyspace.end(4);  // free all proxies
		});
		
		doc.keyDownAction_({|doc, char, mod, unicode, keycode | 
			var linenr, string;
			// evaluating code (the next line will use .isAlt, when that is available 
			if((mod & 524288 == 524288) && ((keycode==124)||(keycode==123)||(keycode==125)||(keycode==126)), { // alt + left or up or right or down arrow keys
				linenr = doc.string[..doc.selectionStart-1].split($\n).size;
				doc.selectLine(linenr);
				string = doc.selectedString;
				if(keycode==123, { // not 124, 125, 
					this.freeAgent(string);
				}, {
					this.opInterpreter(string);	
				});				
			});
		
		});
	}
	
	// the interpreter of thie ixi lang - here operators are overwritten
	opInterpreter {arg string;
		var operator; // the various operators of the language
		var methodFound = false;
		operator = block{|break| // better NOT mess with the order of the following...
			["group", "sequence", "->", "|", "[", "{", "future", ".", ">>", "<<", "))", "((", "tempo", 
			"scale", "tuning", "remind", "tonality", "instr", "tonic", "grid", "kill",  
			"doze", "perk", "nap", "shake", "swap", ">shift", "<shift", "inverse", "expand", "reverse", 
			"up", "down", "yoyo", "order"].do({arg op; 
				var c = string.find(op);
				if(c.isNil.not, {
					methodFound = true;
					break.value(op);
				}); 
			});
			if(methodFound == false, {"ixi lang error : METHOD NOT FOUND !!!".postln; });
		};
		
		switch(operator)
			{"->"}{ // necessary in order to have many agents using the same synthdef
				var mode;
				mode = block{|break| 
					["|", "[", "{"].do({arg op, i;							var c = string.find(op);
						if(c.isNil.not, {break.value(i)}); 
					});
				};
				switch(mode)
					{0} { this.parseScoreMode0(string) }
					{1} { this.parseScoreMode1(string) }
					{2} { this.parseScoreMode2(string) };
			}
			{"|"}{
				this.parseScoreMode0(string);
			}
			{"["}{
				this.parseScoreMode1(string);
			}
			{"{"}{
				this.parseScoreMode2(string);
			}
			{"future"}{
				// future 8:4 >> swap thor // in 8 seconds method is parsed (4 times)
				// future << thor // cancel the scheduling
				var command, commandstart, colon, seconds, times, agent, agentstart, agentend;
				var cursorPos;
				string = string.reject({ |c| c.ascii == 10 }); // get rid of char return
				// allow for some sloppyness in style
				string = string.replace("    ", " ");
				string = string.replace("   ", " ");
				string = string.replace("  ", " ");
				string = string++" "; // add a space in order to find end of agent (if no argument)
				if(string.contains(">>"), { // setting future event(s)
					commandstart = string.find(">");
					agentstart = string.findAll(" ")[3];
					agentend = string.findAll(" ")[4]; 
					agent = string[agentstart+1..agentend-1];
					agent = (docnum.asString++agent).asSymbol;
					colon = string.find(":");
					seconds = string[6..colon-1].asInteger;
					times = string[colon+1..commandstart-1].asInteger;
					command = string[commandstart+3..string.size-1];
					try{ // it'll only try to make a fork to slots in the dict that already exist, i.e. not groups
						agentDict[agent][2] =
							fork{ 
							times.do({arg i; 
								seconds.wait;
								{ 
								cursorPos = doc.selectionStart; // get cursor pos
								this.parseMethod(command); // do command
								doc.selectRange(cursorPos); // set cursor pos again
								}.defer;
							}) 
						}
					};
				}, { // removing future scheduling
					commandstart = string.find("<");
					agent = string[commandstart+2..string.size-1];
					agentDict[agent][2].stop;
					agentDict[agent][2] = nil;
				});
			}
			{">>"}{
				this.initEffect(string);
			}
			{"<<"}{
				this.removeEffect(string);
			}
			{"))"}{
				this.increaseAmp(string);
			}
			{"(("}{
				this.decreaseAmp(string);
			}
			{"tempo"}{
				var newtemp, time, op;
				var nrstart = string.find("o")+1;
				if(string.contains(":"), {
					string = string.tr($ , \);
					op = string.find(":");
					newtemp = string[nrstart..op].asInteger/60;
					time = string[op+1..string.size-1].asInteger;
					TempoClock.default.sync(newtemp, time);
				}, {
					newtemp = string[nrstart+1..string.size-1].asInteger / 60;
					TempoClock.default.tempo = newtemp;
				});
			}
			{"scale"}{
				var scalestart, scalestr;
				scalestart = string.find("e");
				scalestr = string.tr($ , \);
				scalestr = scalestr[scalestart+1..scalestr.size-1];
				chosenscale = ("Scale."++scalestr).interpret;
				chosenscale.tuning_(tuning.asSymbol);
				scale = chosenscale.semitones.add(12); // used to be degrees, but that doesn't support tuning
			}
			{"tuning"}{
				var tuningstart, tuningstr;
				tuningstart = string.find("g");
				tuningstr = string.tr($ , \);
				tuning = tuningstr[tuningstart+1..tuningstr.size-1];
				tuning = tuning.reject({ |c| c.ascii == 10 }); // get rid of char return
				chosenscale.tuning_(tuning.asSymbol);
				scale = chosenscale.semitones.add(12);
			}
			{"remind"}{
				this.getMethodsList;		
			}
			{"tonality"}{
				var doc;
				doc = Document.new;
				doc.name_("scales");
				doc.promptToSave_(false);
				doc.background_(Color.black);
				doc.stringColor_(Color.green);
				doc.bounds_(Rect(10, 500, 500, 800));
				doc.font_(Font("Monaco",16));
				doc.string_("Scales: " + ScaleInfo.scales.keys.asArray.sort.asCompileString
				+"\n\n Tunings: "+
				"et12, pythagorean, just, sept1, sept2, mean4, mean5, mean6, kirnberger, werkmeister, vallotti, young, reinhard, wcHarm, wcSJ");
			}
			{"instr"}{
				this.getInstrumentsList;
			}
			{"tonic"}{
				var tonicstart, tstring, tonicstr;
				tonicstart = string.find("c");
				tstring = string.tr($ , \);
				tonicstr = tstring[tonicstart+1..tstring.size-1];
				tonicstr = tonicstr.reject({ |c| c.ascii == 10 }); // get rid of char return
				tonic = tonicstr.asInteger;
			}
			{"grid"}{
				var cursorPos, gridstring, meter, grids, gridstart;
				cursorPos = doc.selectionStart; // get cursor pos
				meter = string[string.find(" ")..string.size-1].asInteger;
				if(meter == "grid", {meter = 1});
				gridstring = "";
				50.do({arg i; gridstring = gridstring++if((i%meter)==0, {"|"}, {" "})  });
				doc.string_(doc.string.replace(string, gridstring++"\n"));
			}
			{"kill"}{
				proxyspace.end;
				proxyspace = ProxySpace.new.know_(true);
			}
			{"group"}{
				var spaces, groupname, op, groupitems;
				"ixi lang : MAKING A GROUP : ".post;
				// allow for some sloppyness in style
				string = string.replace("    ", " ");
				string = string.replace("   ", " ");
				string = string.replace("  ", " ");
				string = string++" "; // add an extra space at the end
				string = string.reject({ |c| c.ascii == 10 }); // get rid of char return
				spaces = string.findAll(" ");
				groupname = string[spaces[0]+1..spaces[1]-1];
				groupname = (docnum.asString++groupname).asSymbol; // multidoc support
				op = string.find("->");
				groupitems = [];
				(spaces.size-1).do({arg i; 
					if(spaces[i] > op, { groupitems = groupitems.add( string[spaces[i]+1..spaces[i+1]-1].asSymbol ) }) 
				});
				groups.add(groupname -> groupitems);
				groups.postln;
			}
			{"sequence"}{
				var spaces, sequenceagent, op, seqagents, typecheck, firsttype, sa, originalstring, originalagents, fullscore;
				var notearr, durarr, instrarr, amparr, score, instrument, quantphase, newInstrFlag = false;
				typecheck = 0;
				notearr = [];
				durarr = [];
				amparr = [];
				instrarr = [];
				score = "";
				originalstring = string;
				"ixi lang : MAKING A SEQUENCE : ".postln;
				// allow for some sloppyness in style
				string = string.replace("    ", " ");
				string = string.replace("   ", " ");
				string = string.replace("  ", " ");
				string = string++" "; // add an extra space at the end
				string = string.reject({ |c| c.ascii == 10 }); // get rid of char return
				spaces = string.findAll(" ");
				sequenceagent = string[spaces[0]+1..spaces[1]-1];
				sa = sequenceagent;
				sequenceagent = (docnum.asString++sequenceagent).asSymbol; // multidoc support
				op = string.find("->");
				seqagents = [];
				originalagents = [];				
				(spaces.size-1).do({arg i; 
					if(spaces[i] > op, { 
						seqagents = seqagents.add((docnum.asString++string[spaces[i]+1..spaces[i+1]-1]).asSymbol);
						originalagents = originalagents.add(string[spaces[i]+1..spaces[i+1]-1]);
					});
				});
				[\seqagents, seqagents].postln;
				// check if all items are of same type
				firsttype = agentDict[seqagents[0]][1].mode;
				[\firsttype, firsttype].postln;
				seqagents.do({arg agent; typecheck = typecheck + agentDict[agent][1].mode; });
				
				// then merge their score into one string (see below the dict idea)
				if((typecheck/seqagents.size) != firsttype, {
					"ixi lang: ERROR! You are trying to mix playmodes".postln;
				}, {
					switch(firsttype)
						{0} {
							seqagents.do({arg agent, i;
								durarr = durarr ++ agentDict[agent][1].durarr;
								instrarr = instrarr ++ agentDict[agent][1].instrarr;
								score = score ++ agentDict[agent][1].score; // just for creating the score in the doc
								//[\test, agentDict[agent][1].scorestring].postln;
								//this.opInterpreter("doze " ++ originalagents[i]);
								//proxyspace[(docnum.asString++agent).asSymbol].stop; // kill the agent
							});
							fullscore = (sa++" -> |"++score++"|");
							if(agentDict[sequenceagent].isNil, {
								" creating a new Dict !!!!!!!!".postln;
								agentDict[sequenceagent] = [ (), ().add(\amp->0.3), nil];
							}, {
								if(agentDict[sequenceagent][1].scorestring.contains("{"), {newInstrFlag = true }); // free if { instr (Pmono is always on)
							}); // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine
								quantphase = agentDict[seqagents[0]][1].quantphase;
								agentDict[sequenceagent][1].scorestring = fullscore.asCompileString;
								agentDict[sequenceagent][1].instrname = "rhythmtrack";
								agentDict[sequenceagent][1].durarr = durarr;
								agentDict[sequenceagent][1].instrarr = instrarr;
								agentDict[sequenceagent][1].score = score;
								agentDict[sequenceagent][1].quantphase = quantphase;
								
							[\oooooooooAGENTDICT, agentDict[sequenceagent]].postln;
							doc.string_(doc.string.replace(originalstring, fullscore++"\n"));
							this.playScoreMode0(sequenceagent, durarr, instrarr, quantphase, newInstrFlag);
						}
						{1} {
							seqagents.do({arg agent, i;
								durarr = durarr ++ agentDict[agent][1].durarr;
								notearr = notearr ++ agentDict[agent][1].notearr;
								score = score ++ agentDict[agent][1].score; // just for creating the score in the doc
							});
							quantphase = agentDict[seqagents[0]][1].quantphase;
							instrument = agentDict[seqagents[0]][1].instrname;
							fullscore = (sa++" -> "++ instrument ++"["++score++"]");
							if(agentDict[sequenceagent].isNil, {
								" creating a new Dict !!!!!!!!".postln;
								agentDict[sequenceagent] = [ (), ().add(\amp->0.3), nil];
							}, {
								if(agentDict[sequenceagent][1].scorestring.contains("{"), {newInstrFlag = true }); // free if { instr (Pmono is always on)
							}); // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine

								agentDict[sequenceagent][1].scorestring = fullscore.asCompileString;
								agentDict[sequenceagent][1].instrname = instrument;
								agentDict[sequenceagent][1].durarr = durarr;
								agentDict[sequenceagent][1].notearr = notearr;
								agentDict[sequenceagent][1].score = score;
								agentDict[sequenceagent][1].quantphase = quantphase;
							[\oooooooooAGENTDICT, agentDict[sequenceagent]].postln;
							[sequenceagent, notearr, durarr, instrument, quantphase, newInstrFlag].postln;
							doc.string_(doc.string.replace(originalstring, fullscore++"\n"));

							this.playScoreMode1(sequenceagent, notearr, durarr, instrument, quantphase, newInstrFlag);
						}
						{2} {
							seqagents.do({arg agent, i;
								durarr = durarr ++ agentDict[agent][1].durarr;
								amparr = notearr ++ agentDict[agent][1].amparr;
								score = score ++ agentDict[agent][1].score; // just for creating the score in the doc
							});
							quantphase = agentDict[seqagents[0]][1].quantphase;
							instrument = agentDict[seqagents[0]][1].instrname;
							fullscore = (sa++" -> "++ instrument ++"{"++score++"}");
							if(agentDict[sequenceagent].isNil, {
								" creating a new Dict !!!!!!!!".postln;
								agentDict[sequenceagent] = [ (), ().add(\amp->0.3), nil];
							}, {
								if(agentDict[sequenceagent][1].scorestring.contains("{"), {newInstrFlag = true }); // free if { instr (Pmono is always on)
							}); // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine

								agentDict[sequenceagent][1].scorestring = fullscore.asCompileString;
								agentDict[sequenceagent][1].instrname = instrument;
								agentDict[sequenceagent][1].durarr = durarr;
								agentDict[sequenceagent][1].amparr = amparr;
								agentDict[sequenceagent][1].score = score;
								agentDict[sequenceagent][1].quantphase = quantphase;
							[\oooooooooAGENTDICT, agentDict[sequenceagent]].postln;
							[sequenceagent, notearr, durarr, instrument, quantphase, newInstrFlag].postln;
							doc.string_(doc.string.replace(originalstring, fullscore++"\n"));

							this.playScoreMode2(sequenceagent, amparr, durarr, instrument, quantphase, newInstrFlag);
						};
				});				
			}

			// methods (called verbs in ixi lang) dealt with in the parsMethod below (these change string in doc)
			{"doze"} {
				this.parseMethod(string);
			} 
			{"perk"}{
				this.parseMethod(string);			
			}
			{"nap"}{
				this.parseMethod(string);			
			}
			{"shake"}{
				this.parseMethod(string);
			}
			{"swap"}{
				this.parseMethod(string);
			}
			{">shift"}{
				this.parseMethod(string);
			}
			{"<shift"}{
				this.parseMethod(string);
			}
			{"inverse"}{
				this.parseMethod(string);
			}
			{"expand"}{
				this.parseMethod(string);
			}
			{"reverse"}{
				this.parseMethod(string);
			} 
			{"up"}{
				this.parseMethod(string);
			}
			{"down"}{
				this.parseMethod(string);
			}
			{"yoyo"}{
				this.parseMethod(string);
			}
			{"order"}{
				this.parseMethod(string);			
			}
			;
		}
	
	// method invoked on alt+left arrow, for easy freeing of an agent (line)
	freeAgent { arg string;
		var prestring, splitloc, agent, linenr;
		linenr = doc.string[..doc.selectionStart-1].split($\n).size;
		doc.selectLine(linenr);
		string = string.tr($ , \);
		splitloc = string.find("->");
		agent = string[0..splitloc-1]; // get the name of the agent
		agent = (docnum.asString++agent).asSymbol;
		proxyspace[agent].clear;
		agentDict[agent] = nil;
		{doc.stringColor_(Color.red, doc.selectionStart, doc.selectionSize)}.defer(0.1); // killed code is red
	}
	
	// method to parse the musical scores (other methods will do dot and effects)
	parseScoreMode0 {arg string;
		var agent, score, splitloc, endchar, agentstring, silenceicon, silences;
		var durarr, spacecount, instrarr, instrstring, quantphase, empty, outbus;
		var startWempty = false;
		var newInstrFlag = false;

		string = string.reject({arg char; (char==$-) || (char==$>) || (char.ascii == 10) }); // no need for this here
		//string = string.reject({arg char; (char.ascii == 10) }); // no need for this here
		splitloc = string.find("|");
		agentstring = string[0..splitloc-1].tr($ , \); // get the name of the agent
//		[\string, string, 				\OOOOOOOOOO].postln;
		agent = agentstring[0..agentstring.size-1];
		score = string[splitloc+1..string.size-1];
		endchar = score.find("|"); // the index (int) of the end op in the string

		silenceicon = score.find("!");
		// the silences after the array
		silences = if(silenceicon.isNil.not, { score[silenceicon+1..score.size-1].asInteger }, { 0 });
		score = score[0..endchar-1]; // get rid of the function marker
		agent = (docnum.asString++agent).asSymbol;

		if(agentDict[agent].isNil, {
			agentDict[agent] = [(), ().add(\amp->0.3), nil];
		}, {
			//"YEEEEEE  333333333".postln;
			if(agentDict[agent][1].scorestring.contains("{"), {"FREE !!!".postln;newInstrFlag = true }); // trick to free if the agent was { instr (Pmono is always on)
		}); // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine
		
		// ------------- the instrument -------------------
		instrstring = score.tr($ , \);
		instrarr = [];
		instrstring.collect({arg instr, i; instrarr = instrarr.add(instrDict[instr.asSymbol]) });
		// -------------    the score   -------------------
		quantphase=0;	
		spacecount = 0; 
		durarr = [];
		score.do({arg char, i;
			if((char==$ ) && (i==0), { startWempty = true; });
			if(char==$ , {
				spacecount = spacecount+1;
			}, {
				if(i != 0, { // not adding the the first instr
					durarr = durarr.add(spacecount+1); // adding to dur array + include the instr char
					spacecount = 0; 
				}); 
			});
			if(i==(score.size-1), { // fixing at the end of the loop
				durarr = durarr.add(spacecount+1); 
				if(startWempty, {
					quantphase = (durarr[0]-1)/8;
					empty = durarr.removeAt(0)-1; // minus the added instr (since we're only interested in spaces)
					durarr[durarr.size-1] = durarr.last+empty; // adding the last space(s) to the first space(s)
				});
			});
		});
		durarr[durarr.size-1] = durarr.last+silences; // adding silences to the end of the score
		durarr = durarr/8; // 8 is here an arbitrary number. Could be 10?

		agentDict[agent][1].mode = 0;
		agentDict[agent][1].quantphase = quantphase;
		agentDict[agent][1].durarr = durarr;
		agentDict[agent][1].instrarr = instrarr;
		agentDict[agent][1].score = score;
			agentDict[agent][1].scorestring = string.asCompileString;
			agentDict[agent][1].instrname = "rhythmtrack";

		{doc.stringColor_(oncolor, doc.selectionStart, doc.selectionSize)}.defer(0.1);  // if code is green (sleeping)
		"------    ixi lang: Created Percussive Agent : ".post; agent.postln; agentDict[agent].postln;
		this.playScoreMode0(agent, durarr, instrarr, quantphase, newInstrFlag); 
	}	
	
	// method to parse the musical scores (other methods will do dot and effects)
	parseScoreMode1 {arg string;
		var agent, score, scorestartloc, splitloc, endchar, agentstring, instrument, instrstring, transposition=0;
		var prestring, silenceicon, silences, postfixargs, newInstrFlag = false;
		var durarr, spacecount, notearr, notestring, quantphase, empty, outbus;
		var startWempty = false;
		var channelicon, midichannel;
		
		string = string.reject({ |c| c.ascii == 10 }); // get rid of char return
		channelicon = 0;
		
		scorestartloc = string.find("[");
		prestring = string[0..scorestartloc-1].tr($ , \); // get rid of spaces until score
		splitloc = prestring.find("->");
		if(splitloc.isNil, { // no assignment operator
			agent = prestring[0..prestring.size]; // get the name of the agent
			instrument = agent;
		}, {
			agent = prestring[0..splitloc-1]; // get the name of the agent
			instrument = prestring[splitloc+2..prestring.size];
		});
		agent = (docnum.asString++agent).asSymbol;
		/*
		if(agentDict[agent].isNil.not, {
			"YYEEE    ".post; agentDict[agent][1].postln;
			if(agentDict[agent][1].contains("{"), {"FREE !!!".postln;agentDict[agent].free;}); // trick to free if the agent was { instr (Pmono is always on)
		});
		*/
		string = string++" "; // add empty space at the end
		score = string[scorestartloc+1..string.size-1];
		endchar = score.find("]"); // the index (int) of the end op in the string
		silenceicon = score.find("!");
		channelicon = score.find("c");
		// the transposition after the array
		postfixargs = score[endchar+1..score.size-1].tr($ , \); // allowing for spaces
		// the silences after the array (and after the transposition if it's there)
		silences = if(silenceicon.isNil.not, { score[silenceicon+1..score.size-1].asInteger }, { 0 });
		if( (postfixargs[0]==$+) || (postfixargs[0]==$-), { // mul and div possible operators on timestretch
			transposition = postfixargs[0..if(silenceicon.isNil, {postfixargs.size-1}, {silenceicon})].asInteger; 
			//timestretch = postfixargs[1..if(silenceicon.isNil, {postfixargs.size-1}, {silenceicon})].asInteger; 
		});

		midichannel = if(channelicon.isNil.not, { score[channelicon+1..channelicon+3].asInteger - 1}, { 0 });
		[\postfixargs, postfixargs, \midichannel, midichannel].postln;
		
		score = score[0..endchar-1]; // get rid of the function marker
		//effectRegDict[agent] = (); // create a dict that keeps tracxk of effects
		
		if(agentDict[agent].isNil, {
			agentDict[agent] = [(), ().add(\amp->0.3), nil];
		}, {
			//"YEEEEEE  333333333".postln;
			if(agentDict[agent][1].scorestring.contains("{"), {"FREE !!!".postln;newInstrFlag = true }); // trick to free if the agent was { instr (Pmono is always on)
		}); // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine

		// ------------- the notes -------------------
		notestring = score.tr($ , \);
		notearr = [];
		notestring.collect({arg note, i;
			var scalenote, thisnote;
			thisnote = note.asString.asInteger;
			scalenote = scale[thisnote-1];  // start with 1 as fundamental
			if(scalenote.isNil, {scalenote = scale[(thisnote%scale.size)]+12}); // wrap the scale but add octave
			notearr = notearr.add(scalenote); 
		});
		// adding 59 to start with C (and user inputs are 1 as C, 3 as E, 5 as G, etc.)
		notearr = notearr + tonic + transposition; // if added after the score array
		// -------------    the score   -------------------
		quantphase=0;	
		spacecount = 0; 
		durarr = [];
		score.do({arg char, i;
			if((char==$ ) && (i==0), { startWempty = true; });
			if(char==$ , {
				spacecount = spacecount+1;
			}, {
				if(i != 0, { // not adding the the first instr
					durarr = durarr.add(spacecount+1); // adding to dur array + include the instr char
					spacecount = 0; 
				}); 
			});
			if(i==(score.size-1), { // fixing at the end of the loop
				durarr = durarr.add(spacecount+1); 
				if(startWempty, {
					quantphase = (durarr[0]-1)/8;
					empty = durarr.removeAt(0)-1; // minus the added instr (since we're only interested in spaces)
					durarr[durarr.size-1] = durarr.last+empty; // adding the last space(s) to the first space(s)
				});
			});
		});
		durarr[durarr.size-1] = durarr.last+silences; // adding silences to the end of the score
		durarr = durarr/8; // 8 is here an arbitrary number. Could be 10?

		agentDict[agent][1].mode = 1;
		agentDict[agent][1].quantphase = quantphase;
		agentDict[agent][1].durarr = durarr;
		agentDict[agent][1].notearr = notearr;
		agentDict[agent][1].score = score;
		agentDict[agent][1].scorestring = string.asCompileString;
		agentDict[agent][1].instrname = instrument;
		
		{doc.stringColor_(oncolor, doc.selectionStart, doc.selectionSize)}.defer(0.1);  // if code is green (sleeping)
		"------    ixi lang: Created Melodic Agent : ".post; agent.postln; agentDict[agent].postln;
		this.playScoreMode1(agent, notearr, durarr, instrument, quantphase, newInstrFlag, midichannel); 
	}	


	parseScoreMode2 {arg string;
		var agent, score, scorestartloc, splitloc, endchar, agentstring, instrument, instrstring, timestretch=1;
		var prestring, silenceicon, silences, postfixargs, newInstrFlag;
		var durarr, spacecount, amparr, ampstring, quantphase, empty, outbus;
		var startWempty = false;

		string = string.reject({ |c| c.ascii == 10 }); // get rid of char return

		scorestartloc = string.find("{");
		prestring = string[0..scorestartloc-1].tr($ , \); // get rid of spaces until score
		splitloc = prestring.find("->");
		if(splitloc.isNil, { // no assignment operator
			agent = prestring[0..prestring.size]; // get the name of the agent
			instrument = agent;
		}, {
			agent = prestring[0..splitloc-1]; // get the name of the agent
			instrument = prestring[splitloc+2..prestring.size];
		});
		agent = (docnum.asString++agent).asSymbol;
		string = string++" "; // add empty space at the end
		score = string[scorestartloc+1..string.size-1];
		endchar = score.find("}"); // the index (int) of the end op in the string
		// deal with the arguments
		postfixargs = score[endchar+1..score.size-1].tr($ , \); // allowing for spaces
		// the transposition after the array
		if( (postfixargs[0]==$*) || (postfixargs[0]==$/), { // mul and div possible operators on timestretch
			timestretch = postfixargs[1..if(silenceicon.isNil, {postfixargs.size-1}, {silenceicon})].asInteger; 
			if(postfixargs[0]==$/, {timestretch = timestretch.reciprocal; "division".postln; });
		});
		// the silences after the array (and after the transposition if it's there)
		silenceicon = postfixargs.find("!");		
		silences = if(silenceicon.isNil.not, { postfixargs[silenceicon+1..postfixargs.size-1].asInteger }, { 0 });
		score = score[0..endchar-1]; // get rid of the function marker

		// due to Pmono not being able to load a new instr, I check if it there is a new one
		if(agentDict[agent].isNil, { 
			agentDict[agent] = [(), ().add(\amp->0.3), nil];
		}, {			
			if(agentDict[agent][1].instrument != instrument, { 
				"NEW INSTR".postln; 
				newInstrFlag = true;
			}, {
				"SAME INSTR".postln; 
			 	newInstrFlag = false; 
			});	
		});  // 1st = effectRegistryDict, 2nd = scoreInfoDict, 3rd = placeholder for a routine
		
		// ------------- the envelope amps -------------------
		ampstring = score.tr($ , \);
		amparr = [];
		ampstring.do({arg amp; amparr = amparr.add(amp.asString.asInteger/10) });
		// -------------    the score   -------------------
		quantphase=0;	
		spacecount = 0; 
		durarr = [];
		score.do({arg char, i;
			if((char==$ ) && (i==0), { startWempty = true; });
			if(char==$ , {
				spacecount = spacecount+1;
			}, {
				if(i != 0, { // not adding the the first instr
					durarr = durarr.add(spacecount+1); // adding to dur array + include the instr char
					spacecount = 0; 
				}); 
			});
			if(i==(score.size-1), { // fixing at the end of the loop
				durarr = durarr.add(spacecount+1); 
				if(startWempty, {
					quantphase = (durarr[0]-1)/8;
					empty = durarr.removeAt(0)-1; // minus the added instr (since we're only interested in spaces)
					durarr[durarr.size-1] = durarr.last+empty; // adding the last space(s) to the first space(s)
				});
			});
		});
		durarr[durarr.size-1] = durarr.last+silences; // adding silences to the end of the score
		durarr = durarr/8; // 8 is here an arbitrary number. Could be 10?
		durarr = durarr*timestretch; // duration is stretched by timestretch var

		agentDict[agent][1].mode = 2;
		agentDict[agent][1].quantphase = quantphase;
		agentDict[agent][1].durarr = durarr;
		agentDict[agent][1].amparr = amparr;
		agentDict[agent][1].score = score;		
		agentDict[agent][1].scorestring = string.asCompileString;
		agentDict[agent][1].instrname = instrument;

		{doc.stringColor_(oncolor, doc.selectionStart, doc.selectionSize)}.defer(0.1); // if code is green (sleeping)
		"------    ixi lang: Created Concrete Agent : ".post; agent.postln; agentDict[agent].postln;
		this.playScoreMode2(agent, amparr, durarr, instrument, quantphase, newInstrFlag); 
	}	

	playScoreMode0 {arg agent, durarr, instrarr, quantphase, newInstrFlag;
		// ------------ play function --------------
		if(proxyspace[agent].isNeutral, { // check if the object exists alreay
			Pdef(agent, Pbind(
						\instrument, Pseq(instrarr, inf), 
						\dur, Pseq(durarr, inf)
			));
			proxyspace[agent].quant = [durarr.sum, quantphase, 0, 1];
			proxyspace[agent] = Pdef(agent);
			proxyspace[agent].play;
		},{
			if(newInstrFlag, { // only if instrument was {, where Pmono bufferplayer synthdef needs to be shut down
				proxyspace[agent].free; // needed in order to swap instrument in Pmono
				Pdef(agent, Pbind(
							\instrument, Pseq(instrarr, inf), 
							\dur, Pseq(durarr, inf)
				)).quant = [durarr.sum, quantphase, 0, 1];
				{ proxyspace[agent].play }.defer(0.5); // defer needed as the free above and play immediately doesn't work
			}, {	// default behavior
				Pdef(agent, Pbind(
							\instrument, Pseq(instrarr, inf), 
							\dur, Pseq(durarr, inf)
				)).quant = [durarr.sum, quantphase, 0, 1];
			});
		});
		Pdef(agent).set(\amp, agentDict[agent][1].amp); // proxyspace quirk: amp set from outside
	}
	
	playScoreMode1 {arg agent, notearr, durarr, instrument, quantphase, newInstrFlag, midichannel=0;
		if(instrument.asString=="midi", { eventtype = \midi }, { eventtype = \note });
		[\eventtype, eventtype].postln;
		
		// ------------ play function --------------
		if(proxyspace[agent].isNeutral, { // check if the object exists alreay
			Pdef(agent, Pbind(
						\instrument, instrument, // XXX instrument
						\type, eventtype,
						\midiout, midiclient,
						\chan, midichannel,
						\midinote, Pseq(notearr, inf), 
						\dur, Pseq(durarr, inf)
			));
			proxyspace[agent].quant = [durarr.sum, quantphase, 0, 1];
			proxyspace[agent] = Pdef(agent);
			proxyspace[agent].play;
		},{
			if(newInstrFlag, { // only if instrument was {, where Pmono bufferplayer synthdef needs to be shut down
				proxyspace[agent].free; // needed in order to swap instrument in Pmono
				Pdef(agent, Pbind(
						\instrument, instrument, // XXX instrument
						\type, eventtype,
						\midiout, midiclient,
						\chan, midichannel,
						\midinote, Pseq(notearr, inf), 
						\dur, Pseq(durarr, inf)
				)).quant = [durarr.sum, quantphase, 0, 1];
				{ proxyspace[agent].play }.defer(0.5); // defer needed as the free above and play immediately doesn't work
			}, { // default behavior
				Pdef(agent, Pbind(
						\instrument, instrument, // XXX instrument
						\type, eventtype,
						\midiout, midiclient,
						\chan, midichannel,
						\midinote, Pseq(notearr, inf), 
						\dur, Pseq(durarr, inf)
				)).quant = [durarr.sum, quantphase, 0, 1];
			});
		});
		Pdef(agent).set(\amp, agentDict[agent][1].amp); // proxyspace quirk: amp set from outside
	}

	playScoreMode2 {arg agent, amparr, durarr, instrument, quantphase, newInstrFlag; // arg agent, instrument, score, transposition, silences;
		// ------------ play function --------------
		if(proxyspace[agent].isNeutral, { // check if the object exists alreay
			Pdefn((agent++"durarray").asSymbol, Pseq(durarr, inf));
			Pdefn((agent++"amparray").asSymbol, Pseq(amparr, inf));
			Pdef(agent, Pmono(instrument, // XXX instrument
						\dur, Pdefn((agent++"durarray").asSymbol),
						\noteamp, Pdefn((agent++"amparray").asSymbol)
			));
			proxyspace[agent].quant = [durarr.sum, quantphase, 0, 1];
			proxyspace[agent] = Pdef(agent);
			proxyspace[agent].play;
		},{
			Pdefn((agent++"durarray").asSymbol, Pseq(durarr, inf)).quant = [durarr.sum, quantphase, 0, 1];
			Pdefn((agent++"amparray").asSymbol, Pseq(amparr, inf)).quant = [durarr.sum, quantphase, 0, 1];
			if(newInstrFlag, {
				proxyspace[agent].free; // needed in order to swap instrument in Pmono
				Pdef(agent, Pmono(instrument, // XXX instrument
							\dur, Pdefn((agent++"durarray").asSymbol),
							\noteamp, Pdefn((agent++"amparray").asSymbol)
				));
				{ proxyspace[agent].play }.defer(0.5); // defer needed as the free above and play immediately doesn't work
			});
		});
		Pdef(agent).set(\amp, agentDict[agent][1].amp); // proxyspace quirk: amp set from outside
	}

	initEffect {arg string;
		var splitloc, agentstring, endchar, agent, effect, effectFoundFlag;
		effectFoundFlag = false;
		string = string.tr($ , \); // get rid of spaces
		string = string.reject({ |c| c.ascii == 10 }); // get rid of char return
		splitloc = string.findAll(">>");		
		agent = string[0..splitloc[0]-1];
		agent = (docnum.asString++agent).asSymbol;
		string = string++$ ;
		endchar = string.find(" ");
		splitloc = splitloc.add(endchar);
		if(groups.at(agent).isNil.not, { // the "agent" is a group
			effect = string[splitloc[0]..splitloc.last];
			groups.at(agent).do({arg agentx, i;
				this.initEffect(agentx++effect); // recursive calling of same method
			});
		}, { // it is a real agent, not a group, then we ADD THE EFFECT
			(splitloc.size-1).do({arg i;
				effect = string[splitloc[i]+2..splitloc[i+1]-1];
				if(agentDict[agent][0][effect.asSymbol].isNil, { // experimental - only add if there is no effect
					agentDict[agent][0][effect.asSymbol] = agentDict[agent][0].size+1;// add 1 (the source is 1)
					if(effectDict[effect.asSymbol].isNil.not, {
						effectFoundFlag = true;
						proxyspace[agent][agentDict[agent][0].size] = \filter -> effectDict[effect.asSymbol];
					});
				});
			});
			"ixi lang : THIS AGENT/GROUP NOW HAS FOLLOWING EFFECTS : ".post; agentDict[agent][0].postln;
		});
	}
	
	removeEffect {arg string;
		var splitloc, agentstring, endchar, agent, effect, effectFoundFlag;
		effectFoundFlag = false;
		string = string.tr($ , \); // get rid of spaces
		string = string.reject({ |c| c.ascii == 10 });
 		splitloc = string.findAll("<<");
		agent = string[0..splitloc[0]-1];
		agent = (docnum.asString++agent).asSymbol;
		string = string++$ ;
		endchar = string.find(" ");
		splitloc = splitloc.add(endchar);
		if(groups.at(agent).isNil.not, { // the "agent" is a group
			effect = string[splitloc[0]..splitloc.last];
			groups.at(agent).do({arg agentx, i;
				this.removeEffect(agentx++effect); // recursive calling of same method
			});
		}, { // it is a real agent, not a group
			if(splitloc[0]==(endchar-2), { // remove all effects (if only << is passed)
				"REMOVE ALL EFFECTS".postln;
				10.do({arg i; proxyspace[agent][i+1] =  nil }); // remove all effects (10 max) (+1 as 0 is Pdef)
				agentDict[agent][0].clear;
			}, { // only remove the effects listed (such as agent<<reverb<<tremolo)
				(splitloc.size-1).do({arg i;
					effect = string[splitloc[i]+2..splitloc[i+1]-1];
					if(agentDict[agent][0][effect.asSymbol].isNil.not, { // if the effect exists
						proxyspace[agent][ (agentDict[agent][0][effect.asSymbol]).clip(1,10)] =  nil;
						agentDict[agent][0].removeAt(effect.asSymbol);
					});
				});
			});
			"THIS AGENT/GROUP NOW HAS FOLLOWING EFFECTS : ".post; agentDict[agent][0].postln;
		});
	}

	increaseAmp {arg string;
		var splitloc, agentstring, endchar, agent, effect, effectFoundFlag, amp;
		effectFoundFlag = false;
		string = string.tr($ , \); // get rid of spaces
		splitloc = string.find("))");
		agentstring = string[0..splitloc-1]; // get the name of the agent
		agent = agentstring[0..agentstring.size-1];
		agent = (docnum.asString++agent).asSymbol;
		if(groups.at(agent).isNil.not, { // the "agent" is a group
			groups.at(agent).do({arg agentx, i;
				this.increaseAmp(agentx++"))"); // recursive calling of this same method
			});
		}, {				
			amp = agentDict[agent][1].amp;
			amp = (amp + 0.05).clip(0, 2);
			agentDict[agent][1].amp = amp;
			Post << agentDict;
			Pdef(agent).set(\amp, amp);
		});
	}
	
	decreaseAmp {arg string;
		var splitloc, agentstring, endchar, agent, effect, effectFoundFlag, amp;
		effectFoundFlag = false;
		string = string.tr($ , \); // get rid of spaces
		splitloc = string.find("((");
		agentstring = string[0..splitloc-1]; // get the name of the agent
		agent = agentstring[0..agentstring.size-1];
		agent = (docnum.asString++agent).asSymbol;
		if(groups.at(agent).isNil.not, { // the "agent" is a group
			groups.at(agent).do({arg agentx, i;
				this.decreaseAmp(agentx++"(("); // recursive calling of this same method
			});
		}, {
			amp = agentDict[agent][1].amp;
			amp = (amp - 0.05).clip(0, 2);
			agentDict[agent][1].amp = amp;
			Post << agentDict;
			Pdef(agent).set(\amp, amp);
		});
	}

	parseMethod {arg string;
		var splitloc, methodstring, spaces, agent, method, pureagentname;
		var thisline, modstring, stringstart, allreturns, stringend, scorerange, score, scoremode, scorestringsuffix;
		var argument, transpsuffix, cursorPos;
		splitloc = string.find(" ");
		methodstring = string[0..splitloc-1].tr($ , \); // get the name of the agent
		method = methodstring[0..methodstring.size-1];
[\method______, method].postln; 
		string = string.reject({ |c| c.ascii == 10 }); // get rid of \n
		string = string++" "; // add a space to the end
		spaces = string.findAll(" ");
		agent = string[splitloc+1..spaces[1]-1];
		pureagentname = agent; // the name of the agent is different in code (0john) and in doc (john) (for multidoc support)
		agent = (docnum.asString++agent).asSymbol;
[\agent___________, agent].postln;
		if( spaces.size > 1, { argument = string[spaces[1]..spaces[spaces.size-1]] }); // is there an argument?
[\argument___________, argument].postln;
		
		// HERE CHECK IF IT'S A GROUP THEN PERFORM A DO LOOP
		if(groups.at(agent).isNil.not, { // the "agent" is a group
			groups.at(agent).do({arg agentx, i;
				this.parseMethod(method+agentx+argument); // recursive calling of same method
			});
		}, { // it is a real agent, not a group
			// -------- find the line in the document -----------
			allreturns = doc.string.findAll("\n");
			// the following checks if it's exactly the same agent name (and not confusing joe and joel)
			block{ | break |
			doc.string.findAll(pureagentname).do({arg loc, i;
				stringend = allreturns[allreturns.indexOfGreaterThan(loc)];
				if(doc.string[(loc+pureagentname.size)..stringend].contains("->"), {
					stringstart = loc; //doc.string.find(pureagentname);
					if(pureagentname == doc.string[stringstart..doc.string.findAll("->")[doc.string.findAll("->").indexOfGreaterThan(stringstart+1)]-1].tr($ , \), {
						break.value; //  exact match found and we break loop, leaving stringstart and stringend with correct values
					});
				});
			});
			};
			thisline = doc.string[stringstart..stringend];
			// -------- detect which mode it is ---------------
			if(thisline.find("|").isNil.not, {
				scorerange = thisline.findAll("|");
				transpsuffix = thisline[scorerange[1]+1..thisline.size-1]; // if transposition is added
				scoremode = 0;
			});
			if(thisline.find("[").isNil.not, {
				scorerange = [];
				scorerange = scorerange.add(thisline.find("["));
				scorerange = scorerange.add(thisline.find("]"));
				transpsuffix = thisline[scorerange[1]+1..thisline.size-1]; // if transposition is added
				scoremode = 1;
			});
			if(thisline.find("{").isNil.not, {
				scorerange = [];
				scorerange = scorerange.add(thisline.find("{"));
				scorerange = scorerange.add(thisline.find("}"));
				scoremode = 2;
			});
			score = thisline[scorerange[0]+1..scorerange[1]-1];
			scorestringsuffix = switch(scoremode) {0}{"|"++transpsuffix}{1}{"]"++transpsuffix}{2}{"}\n"};

		// -------------   Perform methods - the ixi lang verbs

			switch(method) 
				{"doze"} {// pause stream
					Pdef(agent).set(\amp, 0);
			 		doc.stringColor_(offcolor, stringstart, stringend-stringstart);
				}
				{"perk"} { // restart stream
					Pdef(agent).set(\amp, agentDict[agent][1].amp);
			 		doc.stringColor_(oncolor, stringstart, stringend-stringstart);
				}
				{"nap"} { // pause for either n secs or n secs:number of times
					var napdur, separator, times, on;
					on = true;
					separator = argument.find(":");
					if(separator.isNil.not, { // it contains a on/off order
						napdur = argument[0..separator-1].asInteger;
						// round to even, so it doesn't leave the stream off
						times = argument[separator+1..argument.size-1].asInteger.round(2);
					 	{
					 		(times*2).do({ // times two as the interface is that it should nap twice 
					 			if(on, {
					 				proxyspace[agent].objects[0].array[0].mute;
							 		{doc.stringColor_(offcolor, stringstart, stringend-stringstart)}.defer;
					 				on = false;
					 			}, {
					 				proxyspace[agent].objects[0].array[0].unmute;
							 		{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
					 				on = true;
					 			});
						 		napdur.wait;
				 			});
					 	}.fork;
					}, { // it is just a nap for n seconds and then reawake
					 	{
							argument = argument.asInteger;
							// methodFoundFlag = true;
				 			proxyspace[agent].objects[0].array[0].mute;
							 		{doc.stringColor_(offcolor, stringstart, stringend-stringstart)}.defer;
					 		argument.wait;
				 			proxyspace[agent].objects[0].array[0].unmute;
							 		{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
					 	}.fork
				 	});
				}
				{"shake"} { 
					// -------- perform the method -----------
					score = score.scramble;
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"swap"} { 
					var instruments;
					// -------- perform the method -----------
					instruments = score.reject({arg c; c==$ }).scramble;
					score = score.collect({arg char; if(char!=$ , {instruments.pop}, {" "}) });
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						//{2} { this.parseScoreMode2(modstring, agentDict[agent][2][1]) };
				}
				{">shift"} {
					argument = argument.asInteger;
					// -------- perform the method -----------
					score = score.rotate(if((argument==0) || (argument=="") || (argument==" "), {1}, {argument}));
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"<shift"} {
					argument = argument.asInteger;	
					// -------- perform the method -----------
					score = score.rotate(if((argument==0) || (argument=="") || (argument==" "), {-1}, {argument.neg}));
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"inverse"} { 
					var temparray;
					// -------- perform the method -----------
					temparray = [];
					score.do({arg char; temparray = temparray.add( if(char==$ , {nil}, {char.asString.asInteger}) ) });
					temparray = temparray.collect({arg val; if(val.isNil.not, { abs(val-8) }) });
					score = "";
					temparray.do({arg item; score = score++if(item==nil, {" "}, {item.asString}) });
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { "ixi lang : Verb not applicable in this mode".postln; }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"expand"} { // has to behave differently as it adds characters
					var tempstring;
					argument = argument.asInteger;
					// -------- perform the method -----------
					if(argument.isNil || (argument==" "), {1}, {argument});
					tempstring = "";
					score.do({arg char; 
						tempstring = tempstring++char;
						if(char!=$ , {
							argument.do({ tempstring = tempstring++" " });
						});
					});
					score = tempstring;
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					doc.string_(doc.string.replace(thisline, modstring));
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				/*
				{"sortc"} {  // sort and clip if items extend out of time (NOT WORKING YET)
					var tempstring;//, scoresize;
					argument = argument.asInteger;
					// -------- perform the method -----------
					if(argument.isNil || (argument==" "), {1}, {argument});
					//scoresize = score.size;
					score = score.tr($ , \); // get rid of the white spaces
					tempstring = "";
					(score.size/argument).ceil.do({arg i; 
						tempstring = tempstring++score[i];
						argument.do({ tempstring = tempstring++" " });
					});
					score = tempstring;
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"sorte"} {  // sort and expand the size (time) of the score, if needed
					var tempstring;//, scoresize;
					//methodFoundFlag = true;
					argument = argument.asInteger;
					// -------- perform the method -----------
					if(argument.isNil || (argument==" "), {1}, {argument});					//scoresize = score.size;
					score = score.tr($ , \); // get rid of the white spaces
					tempstring = "";
	
					score.size.do({arg i; 
						tempstring = tempstring++score[i];
						argument.do({ tempstring = tempstring++" " });
					});
					score = tempstring;
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				*/
				{"reverse"} { // reverse
					// -------- perform the method -----------
					score = score.reverse;
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { this.parseScoreMode1(modstring) }
					//	{2} { this.parseScoreMode2(modstring, agentDict[agent][2][1]) };
				}
				{"up"} { // all instruments uppercase
					// -------- perform the method -----------
					score = score.toUpper;								// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { "ixi lang : Verb not applicable in this mode".postln; }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"down"} { // all instruments lowercase
					// -------- perform the method -----------
					score = score.toLower;								// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { "ixi lang : Verb not applicable in this mode".postln; }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"yoyo"} { // swaps lowercase and uppercase randomly
					// -------- perform the method -----------
					score = score.collect({arg char; 0.5.coin.if({char.toUpper},{char.toLower})});
					// -------- put it back in place ----------
					modstring = thisline[0..scorerange[0]]++score++scorestringsuffix;
					modstring = modstring.reject({ |c| c.ascii == 10 }); // get rid of \n
					fork{
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(processcolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.string_( modstring, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
						0.3.wait;
						cursorPos = doc.selectionStart; // get cursor pos
						{doc.stringColor_(oncolor, stringstart, stringend-stringstart)}.defer;
						doc.selectRange(cursorPos); // set cursor pos again
					};
					// -------  interpret new code ------------
					switch(scoremode)
						{0} { this.parseScoreMode0(modstring) }
						{1} { "ixi lang : Verb not applicable in this mode".postln; }
						{2} { "ixi lang : Verb not applicable in this mode".postln; };
				}
				{"order"} { // put things in order in time

				}
				{"remind"} {
					this.getMethodsList;		
				};
		});
	}	
	
	getMethodsList {
		var doc;
		doc = Document.new;
		doc.name_("ixi lang lingo");
		doc.promptToSave_(false);
		doc.background_(Color.black);
		doc.stringColor_(Color.green);
		doc.bounds_(Rect(10, 500, 650, 800));
		doc.font_(Font("Monaco",16));
		doc.string_("
	    --    ixi lang lingo	   --
 
 -----------  score modes  -----------
 |		: percussive score
 [		: melodic score
 {		: concrete score (samples)
  
 -----------  operators  -----------
 ->		: score assigned to an agent (sometimes necessary)
 >> 		: set effect
 << 		: delete effect
 ))		: increase amplitude
 ((		: decrease amplitude
 tonic	: set the tonic
 
  -----------  methods  -----------
 doze 	: pause performer
 perk 	: resume performer
 nap		: pause performer for n seconds : n times
 shake 	: randomise the score
 swap 	: swap instruments in score
 >shift 	: right shift array n slot(s)
 <shift 	: left shift array n slot(s)
 inverse	: inverse the melody
 expand	: expand the score with n nr. of silence(s)
 reverse	: reverse the order
 order	: organise in time 
 up		: to upper case (in rhythm mode)
 down	: to lower case (in rhythm mode)
 yoyo	: switch upper and lower case (in rhythm mode)
 	
 -----------  commands  -----------
 tempo	: set tempo in bpm (accelerando op: tempo:time)
 future	: set events in future (arg sec:times)
 group	: define a group
 sequence	: define a sequence
 scale	: set scale
 tuning 	: set tuning
 grid  	: draw a line every n spaces 
 remind	: get this document
 instr 	: info on available instruments
 tonality	: info on available scales and tunings
 kill	: stop all sounds in window
  
 -----------  effects  -----------
 reverb
 reverbS
 reverbL
 delay
 distort
 cyberpunk
 bitcrush
 techno
 technosaw
 antique  
 lowpass
 tremolo
 vibrato
	");
	}
	
	getInstrumentsList {
		var doc;
		doc = Document.new;
		doc.name_("ixi lang instruments");
		doc.promptToSave_(false);
		doc.background_(Color.black);
		doc.stringColor_(Color.green);
		doc.bounds_(Rect(10, 500, 500, 800));
		doc.font_(Font("Monaco",16));
		doc.string_("
	    --    ixi lang instruments    --
 
 ------  melodic instruments  ------\n\n"++
XiiLangInstr.getMelodicInstr
++
"\n\n ------  percussive instruments  ------\n"
++
XiiLangInstr.getPercussiveInstr
	);
	}
	
}



+ String { // this is from wslib  /Lang/Improvements/extString-collect.sc
	
	collect { | function | 
		// this changes functionality of some other methods (like .tr) too
		var result = "";
		this.do {|elem, i| result = result ++ function.value(elem, i); }
		^result;
		}
}		
