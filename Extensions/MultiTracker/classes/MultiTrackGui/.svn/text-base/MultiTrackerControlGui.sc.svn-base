//MultiTrackerFileMenu {
//
//	*new{|multitracker|
//		var 	menuItems = [\save, \load, \render];	
//
//		SCPopUpMenu(window,Rect(10, 10, 20, 20))
//		.items_(menuItems)
//		.action_({|v| this.doFileMenuAction(v); v.value_(0)});
//	
//	}
//
//}


MultiTrackerControlGui {
//currently not used
	var <>model, window, menuItems;
	
	*new{|multitracker|
		^super.new.model_(multitracker).init
	}
	init{
		model.addDependant(this);
		menuItems = [\save, \load, \render];	
		this.guify;	
	}
	
	guify{
		window =SCWindow("<control: " ++ model.name ++ ">",Rect(189, 323, 267, 71));
		SCButton(window,Rect(10, 40, 20, 20))
			.states_([["|<", Color.black, Color.blue]])
			.action_({|v| model.currentTime = 0;});
			
		SCButton(window,Rect(40, 40, 50, 20))
			.states_([[">", Color.black, Color.blue], ["||", Color.black, Color.red]])
			.action_({|v| if(v.value == 1){model.play;}{model.stop;}});
		SCButton(window,Rect(100, 40, 20, 20))
			.states_([[">|", Color.black, Color.blue]])		
			.action_({|v| "<MultiTrackerControlGui> not yet implemented".inform;});

		SCPopUpMenu(window,Rect(10, 10, 20, 20))
			.items_(menuItems)
			.action_({|v| this.doFileMenuAction(v); v.value_(0)});
		this.front;
	}
	
	doFileMenuAction{|view|
		var state;
		state = view.items[view.value];
		if(state === \save){^this};
		if(state === \load){^this};
		if(state === \render){this.openRenderDialog;^this};
	
	}
	
	front{
		window.front;
	}
	
	openRenderDialog{
		^MultiTrackerRenderDialog(this);
	}
	render{|path, rate, format, channels|
		model.render(path, rate, sampleFormat:format, numChannels:channels);
	}
	onClose{
		window.close;
	}
}


MultiTrackerRenderDialog {
		var <>model;
		
	*new{|multiTrackerControlGui|
		^super.new.model_(multiTrackerControlGui).init;
	}
	
	init{
		this.guify;
	
	}
	
	guify{
		var win, width, okwidth, pathview, sampleratebox, formatmenu, channlebox, mPath, headermenu, tpath;
		width = 367;
		okwidth = width*0.25;
		tpath = model.currentPathForRendering ? "./";
		win = SCWindow("render", Rect(128, 296, 367, 188), false);
		win.view.decorator = FlowLayout(Rect(0,0,win.view.bounds.width,win.view.bounds.height));
		SCStaticText(win,150@20).string_("bounce to disk...");
		win.view.decorator.nextLine;
		SCStaticText(win,100@20).string_("path:");
		pathview = SCTextView(win,200@20).string_(tpath).font_(Font("Monaco", 9)).enterInterpretsSelection_(false);
		{pathview.textBounds_(Rect(0,0,400,30))}.defer(0.5);
		SCButton(win,50@20).states_([["choose", Color.black, Color.gray(0.5, 0.1)]])
		.action_{
			CocoaDialog.savePanel({arg path;  //doesn'r give me paths?
						pathview.string_(path);
						mPath = path;
			
			},{
			
			});
//			CocoaDialog.getPaths({arg paths; 
//						pathview.string_(paths[0].asString)
//						});
					
		};
		win.view.decorator.nextLine;
		SCStaticText(win,100@20).string_("sampleRate:");
		sampleratebox = SCNumberBox(win,100@20).value_(44100);
		
		win.view.decorator.nextLine;
		SCStaticText(win,100@20).string_("sampleFormat:");
		formatmenu = SCPopUpMenu(win,100@20).items_(["flaot32", "int24","int16"]);
		
		win.view.decorator.nextLine;
		
		SCStaticText(win,100@20).string_("sampleFormat:");
		headermenu = SCPopUpMenu(win,100@20).items_(["AIFF", "WAV"]);
		
		win.view.decorator.nextLine;
		
		SCStaticText(win,100@20).string_("numChannels:");
		channlebox = SCNumberBox(win,100@20).value_(2);
		
		win.view.decorator.nextLine;
		win.view.decorator.shift(okwidth,0);
		SCButton(win,okwidth@20).states_([["cancel", Color.black, Color.gray(0.5, 0.1)]])
		.action_({win.close});
		
		SCButton(win,okwidth@20).states_([["render", Color.black, Color.gray(0.5, 0.1)]])
			.action_({
			model.render(pathview.string, sampleratebox.value, headerFormat: headermenu.items[headermenu.value],sampleFormat: formatmenu.items[formatmenu.value], numChannels: channlebox.value);
				win.close;});
		win.front;

}
	

}