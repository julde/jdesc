
+SynthDesc {
	

	makeMixer{|groupid=1,in=0|
		this.makeMixerGui(groupid,in);
	}
	
	makeMixerGui{|groupid,in|
		var w, s, startButton, sliders,boxes;
		var id, cmdPeriodFunc;
		var usefulControls, numControls,audioControls;
		var getSliderValues, gui,auxcolor,font,fontColor,count;

		s = Server.default;
		
		gui = GUI.current;
		
		font=Font("ArcadeClassic",12);
		
		count=0;
		audioControls=List.new();
		usefulControls = controls.select {|controlName, i|
			var ctlname,audioFound=false;
			ctlname = controlName.name.asString;
			((ctlname == "out") || (ctlname == "fade") || (ctlname == "mix")|| (ctlname == "in")|| (ctlname == "inextra")).if({audioControls.add(controlName);},{audioFound=true});
			( (ctlname != "in") && audioFound && (msgFuncKeepGate or: { ctlname != "gate" }))
			
		};

		numControls = usefulControls.size;
		sliders = Array.newClear(numControls);
		boxes=Array.newClear(audioControls.size);
		
		// make the window
		auxcolor=Color.rand;
		auxcolor=[[auxcolor,auxcolor.complementary,Color.black],[auxcolor,Color.black,Color.white],[auxcolor,Color.white,Color.black],[Color.black,auxcolor,Color.white],[Color.white,auxcolor,Color.black]].choose;
		fontColor=auxcolor[2];
		
		
		
		w = gui.window.new("--"+name+"--", Rect(800,200,200,160),true);
		w.view.decorator = FlowLayout(w.view.bounds);
		
		w.view.background=Gradient(auxcolor[0],auxcolor[1],\v,4);
		
		
		// add a button to start and stop the sound.
		startButton = gui.button.new(w, 60 @ 15);
		startButton.states = [
			["Start", Color.black, Color.green],
			["Stop", Color.white, Color.red]
		];
		startButton.font_(font);
		
		getSliderValues = {
			var envir;
			envir = ();
			usefulControls.do {|controlName, i|
				var ctlname;
				ctlname = controlName.name.asSymbol;
				envir.put(ctlname, sliders[i].value);
			};
			audioControls.do {|controlName, i|
				var ctlname;
				ctlname = controlName.name.asSymbol;
				envir.put(ctlname, boxes[i].value);
			};
			envir.use {
				msgFunc.valueEnvir
			};
		};
		
		
		startButton.action = {|view|
				if (view.value == 1) {
					// start sound
					if(id.isNil) { id = s.nextNodeID };
					s.sendBundle(s.latency, ["/s_new", name, id,1, groupid] ++ getSliderValues.value);
				};
				if (view.value == 0) {
					if (this.hasGate) {
						// set gate to zero to cause envelope to release
						s.sendMsg("/n_set", id, "gate", 0);
					}{
						s.sendMsg("/n_free", id);
					};
					id = nil;
				};
		};
		
		audioControls.do{|controlName,i|
		var ctlname, ctlname2, capname, spec;
			ctlname = controlName.name;
			ctlname2 = controlName.name.asString;
			capname = ctlname.copy; 
						
			ctlname = ctlname.asSymbol;
			if((spec = metadata.tryPerform(\at, \specs).tryPerform(\at, ctlname)).notNil) {
				spec = spec.asSpec
			} {
				spec = ctlname.asSpec;
			};
			if (spec.notNil) {
			boxes[i]=ctlname2.switch(
			"out",{ gui.ezNumber.new(w,60 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					},0,labelWidth:25)
					.font_(font)
					.setColors(stringColor:fontColor)
					},
			"in",{ gui.ezNumber.new(w,60 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					},in,labelWidth:25)
					.font_(font)
					.setColors(stringColor:fontColor)
					},
			"mix",{ EZKnob.new(w,120 @ 130,ctlname2,\amp, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					},1)
					.font_(font)
					.setColors(stringColor:fontColor)
					},
			"inextra",{ SmoothButton.new(w,60 @ 120)
					.states_([["0",fontColor,auxcolor[0]],["1",fontColor,auxcolor[1]]])
				 	.action_(
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					})
					.font_(Font("ArcadeClassic",60);)
					},
				{gui.ezNumber.new(w,80 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					}, controlName.defaultValue)
					.font_(font)
					.setColors(stringColor:fontColor)
					});

			}{postln(controlName.name+" is not present in metadata");
				count=count+1;
			}
		};

		
		// create controls for all parameters
		usefulControls.do {|controlName, i|
			var ctlname, ctlname2, capname, spec;
			ctlname = controlName.name;
			capname = ctlname.copy; 
			capname[0] = capname[0].toUpper;
			
			ctlname = ctlname.asSymbol;
			if((spec = metadata.tryPerform(\at, \specs).tryPerform(\at, ctlname)).notNil) {
				spec = spec.asSpec
			} {
				spec = ctlname.asSpec;
			};
			if (spec.notNil) {
				sliders[i] = gui.ezSlider.new(w, 400 @ 15, capname, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					}, controlName.defaultValue);
				sliders[i].font_(font);
				sliders[i].setColors(stringColor:fontColor);
				w.view.decorator.nextLine;	
			} {postln(controlName.name+" is not present in metadata");
				count=count+1;
			}
		};
			
		
		// set start button to zero upon a cmd-period
		cmdPeriodFunc = { startButton.value = 0; };
		CmdPeriod.add(cmdPeriodFunc);
		
		startButton.valueAction_(1);
		// stop the sound when window closes and remove cmdPeriodFunc.
		w.onClose = {
			if(id.notNil) {
				s.sendMsg("/n_free", id);
			};
			CmdPeriod.remove(cmdPeriodFunc);
		};
		(count > 0).if({w.bounds_(Rect(20, 400, 410, (numControls-count) * 18 + 28))});
		w.front; // make window visible and front window.
	}
	
}