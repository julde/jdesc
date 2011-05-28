+SynthDesc {
	
		makePerc{|groupid=1,out=0|
		this.makePercGui(groupid,out);
	}
	
	makePercGui{|groupid,out|
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
			((ctlname == "out") || (ctlname == "fade") || (ctlname == "in")).if({audioControls.add(controlName);},{audioFound=true});
			( (ctlname != "in") && audioFound && (msgFuncKeepGate or: { ctlname != "gate" }))
			
		};

		numControls = usefulControls.size;
		sliders = Array.newClear(numControls);
		boxes=Array.newClear(audioControls.size);
		
		// make the window
		auxcolor=Color.rand;
		auxcolor=[[auxcolor,auxcolor.complementary,Color.black],[auxcolor,Color.black,Color.white],[auxcolor,Color.white,Color.black],[Color.black,auxcolor,Color.white],[Color.white,auxcolor,Color.black]].choose;
		fontColor=auxcolor[2];
		
		
		
		w = gui.window.new("--"+name+"--", Rect(20, 400, 410, numControls * 18 + 28),true);
		w.view.decorator = FlowLayout(w.view.bounds);
		
		w.view.background=Gradient(auxcolor[0],auxcolor[1],\h,12);
		
		
		// add a button to start and stop the sound.
		startButton = gui.button.new(w, 60 @ 15);
		startButton.states = [
			["Start", Color.black, Color.green]
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
			envir.use {
				msgFunc.valueEnvir
			};
		};
		
		
		startButton.action = {|view|
					// start sound
					id = s.nextNodeID ;
					s.sendBundle(s.latency, ["/s_new", name, id,1, groupid] ++ getSliderValues.value);
					//s.sendMsg("/n_set",id,[\gate,0]);
					s
				
		};
		
		audioControls.do{|controlName,i|
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
			((ctlname2 == "out") || (ctlname2 == "in")).if({
				boxes[i] = gui.ezNumber.new(w,110 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					},out);
				boxes[i].font_(font);
				boxes[i].setColors(stringColor:fontColor);
			},{
							boxes[i] = gui.ezNumber.new(w,110 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					}, controlName.defaultValue);
				boxes[i].font_(font);
				boxes[i].setColors(stringColor:fontColor);
			});
			
			} {postln(controlName.name+" is not present in metadata");
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