// MultiTrackTools
MultiTrackToolsGui {
	var <>model, <>canvas, <current, resetY, <keyCodeResponder, <> parent, selPopup;

	*new{|model, parent, canvas|
		^super.new.model_(model).parent_(parent).init(canvas)
	}
	
	init{|pcanvas|
		keyCodeResponder = model.defaultKeyCodeResponder(this);
		selPopup = SCPopUpMenu(pcanvas, (pcanvas.bounds.width-10)@20)
			.items_(model.tools.keys.asArray ? [])
			.action_{|it| this.at(it.items[it.value])};
		pcanvas.decorator.nextLine;
		canvas = SCCompositeView(pcanvas, Rect(0, 0, 140, pcanvas.bounds.height-50)); 
		canvas.decorator  = ScrollFlowLayout(canvas.bounds);
		canvas.keyDownAction_(keyCodeResponder);
//		v.resize_(1);
		
		this.at(\soundFiles);
		keyCodeResponder = model.defaultKeyCodeResponder(this);
	}

	at{|key|
		if(model.tools[key].hasGui.not){ ^this}; // need to reset selPopup
		if(current.notNil){
			current.remove;
			canvas.decorator.reset;
		};
		current = model.tools[key].gui(this)
	}
	
	focus{
		current.focus
	}
	

	

	
}


// MultiTrackSoundFilePool

MultiTrackSoundFilePoolGui{

	var <>model, ctl, <>parent, <keyCodeResponder, <keyUpResponder;
	var <fileView, filePopUp;
	
	*new{|model, parent|
		^super.new.model_(model).parent_(parent).init
	}
	
	init{
		var pcanvas = parent.canvas;
		keyCodeResponder = model.defaultKeyCodeResponder(this);
		keyCodeResponder = keyCodeResponder ++ parent.keyCodeResponder;
		keyUpResponder = model.defaultKeyUpKeyCodeResponder;
		filePopUp = SCPopUpMenu(pcanvas, 100@20)
					.items_(["file", "add", "add Directory", "remove"])
					.action_{|v|
						model.perform(#[readFilesWithDialog, readDirectoryWithDialog, removeCurrentItem][v.value]);
						v.value_(0);
					}
					.keyDownAction_({ "keyDown disabled on this view".warn}); // no keyDown for now
		pcanvas.decorator.nextLine;			
		fileView = SCListView(pcanvas,pcanvas.bounds)
				.items_(model.names ? [])
				.keyDownAction_({|v, char, mod, uni, key|
//					if (unicode == 16rF702, {^nil});
					if (char == $ , { nil });
					if (char == $\r, { v.enterKeyAction.value(this); });
					if (char == $\n, { v.enterKeyAction.value(this);  });
					if (char == 3.asAscii, { v.enterKeyAction.value(this); });
					if (uni == 16rF700, {\a.postln; v.valueAction = v.value - 1;  });
					if (uni == 16rF703, {\b.postln; });
					if (uni == 16rF701, {\c.postln; v.valueAction = v.value + 1; });
					if (uni == 16rF702, {\d.postln; });
//					v.defaultKeyDownAction(char,mod,uni, key);
//					if(uni == 13){"return".postln}; //rtuern
//					if(uni == 3){"enter".postln}; // enter
					keyCodeResponder.value(v, char, mod, uni, key);
				})
				.keyUpAction_(keyUpResponder)
				.beginDragAction_({arg v;
					model.paths[v.value];
				})
				.action_{|v|
					if(v.item.notNil){
						model.currentItem_(model[v.item.asSymbol]); 
					}
				}
				.onClose_{ctl.remove};
				
		ctl = SimpleController(model).put(\path, {})
				.put(\names, {
					this.updateNames(model)
				});
	}

	updateNames{|model|
		fileView.items_(model.names ? []);
	}
	
	remove{
		fileView.remove;
	}
	
	focus{
		fileView.focus;
	}
}