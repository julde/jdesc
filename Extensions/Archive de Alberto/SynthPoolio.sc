/*
SynthDefPoolio - modificacion del quarks SynthPool,pero mas cool!
SynthDefPool.gui
*/
SynthPoolio {

	var <poolpath, <dict;
	var <lists, <>groupid,<name;

	//pedimos un nombre para la libreria, una locacion y el grupo donde se pondran los sintes 
	*new { |name,pool,group|
	  ^super.new.init(name,pool,group);
	}
	
	init {|label,pool,group|
	
	//cehacmos si el grupo existe, sino usamos el default
	if(group.notNil){
			groupid=group.nodeID;
	}{
	groupid=1;	
	"Server default used".postln;	
	};
	
	poolpath=pool;
	name=label;
	dict = IdentityDictionary.new;
	lists = Dictionary.new;
	//buscamos todos los archivos que se encuentren en la carpeta indicada  
	  (poolpath ++ "/*.scd").pathMatch.do{ |apath|
	  	// ponemos los nombres en un diccionario, pero con un valor 0 de default
	  	dict.put(apath.basename.splitext[0].asSymbol, 0); 	  };
	}
//Funcion que compila los archivos si no han sido llenados (valor 0)
	at { |key|

	  if(dict[key]==0){
	    dict[key] = thisProcess.interpreter.compileFile("%/%.scd".format(poolpath, key)).value;
	  };
	  
	  ^dict[key]
	}
//scanea todos los elementos de diccionarios	
	scanAll {
		dict.keysDo{|key| this[key]}
	}
	
//reinicializa todos los archivos	
	forget {
		dict.keysDo{|key| this[key] = 0}
	}
//la funcion del gui
	gui {
		var w, current, aSynth, cmdPeriodFunc;
		var font, auxcolor,fontColor;
		var listview,butview,synthlist,catlist;
		var buttons, mode,outBox;
//checamos que el servidor 
		Server.default.waitForBoot{
			//cargamos sintes ala memoria
			this.store;
	
	//colores
			auxcolor=Color.rand;
			auxcolor=[[auxcolor,auxcolor.complementary,Color.black],[auxcolor,Color.black,Color.white],[auxcolor,Color.white,Color.black],[Color.white,auxcolor,Color.black],[Color.white,auxcolor,Color.black]].choose;
			fontColor=auxcolor[2];
			//font=Font("ArcadeClassic",12);
			//font
			font=Font(Font.defaultSansFace,12);
//ventana
			w = Window.new("--"+name+"Poolio --", Rect(0, 0,400,300).center_(GUI.window.screenBounds.center),false);
			w.view.background=Gradient(auxcolor[0],auxcolor[1],\h,12);
			CmdPeriod.doOnce {w.close};
			
			listview=CompositeView(w,Rect(0,0,300,300));
			listview.decorator = FlowLayout(listview.bounds,gap:8@4);

			butview=CompositeView(w,Rect(300,0,100,300));
			butview.decorator = FlowLayout(butview.bounds,gap:2@10);
//toda la parte de las categorias, le metemos metodos para navegar con las teclas
			catlist=ListView.new(listview,140@280)
			.background_(Color.clear)
			.hiliteColor_(Color.green(alpha:0.6))
			.items_(lists.keys.asArray)
			.font_(font)
			.value_(1)
			.stringColor_(fontColor)
			.action_({|menu|
						synthlist.items_(lists[menu.item]);
						synthlist.value_(0);
						current=lists[menu.item][0]
				});
			
			catlist.keyUpAction_({|view,char,modifiers,unicode,keycode| 
				if(keycode == 36){buttons.at(mode).doAction};
				if(keycode == 124){synthlist.focus(true)}
				 });
//toda la aprte para la lista de sintes, esta depende de la categoria seleccionada
			synthlist=ListView.new(listview,140@280)				.background_(Color.clear)
			.hiliteColor_(Color.green(alpha:0.6))
			.font_(font)
			.stringColor_(fontColor)
			.action_({|menu| current=menu.item;})
			.enterKeyAction_({ buttons.at(mode).doAction});
						
			synthlist.keyUpAction_({|view,char,modifiers,unicode,keycode| 
				if(keycode ==123){catlist.focus(true)}
				 });
			
			catlist.valueAction_(0);
			current=lists[lists.keys.asArray[0]][0];
//una ventana para escoger la salida default
			outBox=EZNumber(butview,90@20,"Out: ",\audiobus,initVal:0,labelWidth:45)
					.setColors(stringColor:fontColor)
					.font_(font);
//botones posibles			
//yo uso, el defualt, el chidogui, uno percutivo y uno para ver el codigo del sinte
			PopUpMenu.new(butview, 90@20)
				.items_([" Default :"," Test"," Chido"," Perc","Source"])
				.action_({|menu| if(menu.value > 0){mode=menu.value};  })
				.stringColor_(fontColor)
				.font_(font);
			mode=1;
			
			buttons=List.new;
//Un boton para probar el sonido
			buttons.add( GUI.button.new(butview, 90@20)			.font_(font)	
			.states_([
					["Test", Color.black, Color(0.5, 0.7, 0.5)],
					["Stop", Color.white, Color(0.7, 0.5, 0.5)]
				]).action_{|widg|
					if(widg.value==0){
						if(aSynth.notNil){aSynth.free; aSynth=nil };
					}{
						aSynth = Synth(current);
						OSCresponderNode(Server.default.addr, '/n_end', { |time, resp, msg|
							if(aSynth.notNil and: {msg[1]==aSynth.nodeID}){
//checar que se murio el sinte
								{buttons.at(0).value_(0)}.defer;
							};
						}).add.removeWhenDone;
					}
				});
			cmdPeriodFunc = { buttons.at(0).value_(0); };
			CmdPeriod.add(cmdPeriodFunc);
			// mata el sonido si hacemos un stop
			w.onClose = {
				if(aSynth.notNil) {
					aSynth.free;
				};
				CmdPeriod.remove(cmdPeriodFunc);
			};
//El boton que llama a Chido.gui
			buttons.add(Button.new(butview, 90@20)
				.states_([["Chido"]])
				.action_{ SynthDescLib.global.at(current.asSymbol).makeChido(groupid,outBox.value)}
				.font_(font));
//El boton que llama a Perc.gui
			buttons.add(Button.new(butview, 90@20)
				.states_([["Perc"]])
				.action_{ SynthDescLib.global.at(current.asSymbol).makePerc(groupid,outBox.value)}
				.font_(font));
//El boton que abre el arhivo de codigo del sinte
			buttons.add(Button.new(butview, 90@20)
				.states_([["Source"]])
				.action_{ Document.open(poolpath +/+ current ++ ".scd") }
				.font_(font));		
			
			w.front;
		};

	}
//nombres en la libreria	
	defnames {
		^dict.keys
	}
//escribir archivos de definicion	
	writeDefFile { |dir|
		this.scanAll;
		dict.do{|def| def.writeDefFile(dir)};
	}
//lodeamos todo
	load { |server|
		this.scanAll;
		dict.do{|def| def.load(server)};
	}
	
//cargamos los archivos con store
	store { |libname=\global, completionMsg, keepDef = true, mdPlugin|
		var type;
		this.scanAll;
		dict.do{|def| 
			def.store(libname, completionMsg, keepDef, mdPlugin);
			//hacemos chequeo del tipo de Synte
			type=def.metadata[\type].asString;
			if(lists[type].isNil)
			{lists.put(type,List[def.name])}
			{lists[type].add(def.name)};
			};
			
	(lists.keys).do({|item,i|
		lists.put(item,lists[item].asArray)
	});

	}
//cargamos los archivos con memstore
	memStore { |libname=\global, completionMsg, keepDef = true|
		var count=0;
		this.scanAll;
		dict.do{|def|
		def.store(libname, completionMsg, keepDef)};
		
	}


} // end class
