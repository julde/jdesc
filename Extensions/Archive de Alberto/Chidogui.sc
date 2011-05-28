//agregamos a la clase SynthDesc una funcion
+SynthDesc {
	
	//funcion makeChido, el groupid es si quieren agregar el sinte a un grupo en especifico
	//out para la salida
	makeChido{|groupid=1,out=0|
		this.makeChidoGui(groupid,out);
	}
	
	//ora si llamamos a la funcion
	makeChidoGui{|groupid,out|
		var w, s, startButton, sliders,boxes;
		var id, cmdPeriodFunc;
		var usefulControls, numControls,audioControls;
		var getSliderValues, gui,auxcolor,font,fontColor,count;
		

		s = Server.default;
		
		gui = GUI.current;
	//Yo uso arcade classic, pero puedes poner el quieras
		//font=Font("ArcadeClassic",12);
		//usaremos el default
		font=Font(Font.defaultSansFace,12);
		
		//por seguridad contamos que se haya creado almenos un slider
		count=0;
		audioControls=List.new();
		
		//aqui buscamos todos los controladores que vamos usar y los separamos por audio y de parametros
		usefulControls = controls.select {|controlName, i|
			var ctlname,audioFound=false;
			ctlname = controlName.name.asString;
			//out,fade o in van a ser parametros diferentes a un slider
			((ctlname == "out") || (ctlname == "fade") || (ctlname == "in")).if({audioControls.add(controlName);},{audioFound=true});
			( (ctlname != "in") && audioFound && (msgFuncKeepGate or: { ctlname != "gate" }))
			
		};

		numControls = usefulControls.size;
		sliders = Array.newClear(numControls);
		boxes=Array.newClear(audioControls.size);
		
		// aqui creamos colores aleatorios
		auxcolor=Color.rand;
		auxcolor=[[auxcolor,auxcolor.complementary,Color.black],[auxcolor,Color.black,Color.white],[auxcolor,Color.white,Color.black],[Color.black,auxcolor,Color.white],[Color.white,auxcolor,Color.black]].choose;
		fontColor=auxcolor[2];
		
		//aqui tenemos la ventana, usamos el nombre del sinte como la ventana
		w = gui.window.new("--"+name+"--", Rect(20, 400, 410, numControls * 18 + 28),true);
		w.view.decorator = FlowLayout(w.view.bounds);
		//usamos un gradiente para el background, puede ser otra cosa, talvez no funcione en linux
		w.view.background=Gradient(auxcolor[0],auxcolor[1],\h,12);
		
		
		// Un boton especifico para prender y apagar cosas
		startButton = gui.button.new(w, 60 @ 15);
		startButton.states = [
			["Start", Color.black, Color.green],
			["Stop", Color.white, Color.red]
		];
		startButton.font_(font);
		
		//aqui recolectamos el tipo de controlSpec para los sliders
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
		
		//definimos acciones a tomar para el botn start
		startButton.action = {|view|
				if (view.value == 1) {
					// empieza
					if(id.isNil) { id = s.nextNodeID };
					s.sendBundle(s.latency, ["/s_new", name, id,1, groupid] ++ getSliderValues.value);
				};
				if (view.value == 0) {
					if (this.hasGate) {
						//si tiene una gate el sinte, matarlo lentamente
						s.sendMsg("/n_set", id, "gate", 0);
					}{
						//matar violentamente
						s.sendMsg("/n_free", id);
					};
					id = nil;
				};
		};
		//Aqui lidiamos con los controladores "audio"
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
			
			//si son controladores de salida o entrada, les vamos a crear un NumberBox
			((ctlname2 == "out") || (ctlname2 == "in")).if({
				boxes[i] = gui.ezNumber.new(w,110 @ 15,ctlname2, spec, 
					{ |ez| 
						if(id.notNil) { s.sendMsg("/n_set", id, ctlname, ez.value) }
					},out);
				boxes[i].font_(font);
				boxes[i].setColors(stringColor:fontColor);
			},{
				//al fade le damos un valor default
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

		
		// creamos controles para todos los parametros que no son "audio"
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
				//creamos un slider
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
			
		
		// Que apague el sinte (botno == apagado) cuando se usa el stop
		cmdPeriodFunc = { startButton.value = 0; };
		CmdPeriod.add(cmdPeriodFunc);
		
		// Para el sonido y matalo cuando se cierra la ventana
		w.onClose = {
			if(id.notNil) {
				s.sendMsg("/n_free", id);
			};
			CmdPeriod.remove(cmdPeriodFunc);
		};
		//si hay un slider, hacemos la ventana de acuerdo al numero de sliders
		(count > 0).if({w.bounds_(Rect(20, 400, 410, (numControls-count) * 18 + 28))});
		w.front; // pon la ventan al frente
	}
	
}