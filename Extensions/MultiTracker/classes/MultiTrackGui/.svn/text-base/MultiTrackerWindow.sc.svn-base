/* 	(c) 2006 Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

/*
layout:
0 - scroll slider
1 - timeline
2 - info / control
3 - track 1
4 - separator
5 - track 2
...

note :
MultiTrackerWindow-objects stores the gui
TrackLayout-trackObjects stores the views
MultiTrackerWindow.browse

model should respond to:
currentTime
snapToGrid
pixelPerSecond
play
stop

*/

MultiTrackerWindow : ObjectGui{
	var < bounds, <pixelPerSecond = 100, < title;
	var < window, timelineSelector,timelineSpec, timePoints, < canvas, <controlCanvas, <leftCanvas;
	var currentTimeText;
	var scrollbar, lastscroll = 0, <timeOffset = 0, yzoomSlider, xzoomSlider, trackScrollbar;
	var rowoffset = 1; // track starts with 1
	//currentTime should be in model
	var <> objects;
	var <usableRows;
	var tracksize = 30;
	var infoText, infoTime, infoDur, infoVal;
	var <>guiControl;
	var <>undoManager;
	var <controlGui;
	var objectsToClose;
	var <>currentMousePositionTime; //mouse over
	var layoutTrackOffset = 4; //reserved for other gui elements.
	var <>maxrows = 10;
	var <simpleController;
	var leftView;
	var <tools;
	var <keyCodeResponder;
	
	defaultModel{ // MultiTrackerWindow StandAlone!
		^(
			currentTime: 0,
			snapToGrid: false,
			pixelPerSecond: pixelPerSecond
		
		);
	
	}
	
	guiBody{|layout|

		//currentTime = 0.0;
		//this.makeWindow(layout);
		//this.front;
	}
	
	refresh{
		window.refresh;
	}
	
	scrollBarAction{|val|
		this.scrollToTime([0.0, model.maxTime, \lin, 1.0].asSpec.map(val));
		this.currentTime_(model.currentTime);		
				//this.scrollToTime(v.value.linlin(0.0, 1.0, 0.0, model.maxTime));

	}
		
	gui { arg lay, bounds ... args;
		var layout;
		if(window.notNil){window.front;^this};
		model = model ? this.defaultModel.addDependant(this);
		
		objects = IdentitySet.new;
		undoManager = UndoManager(this);		
		layout=this.guify(lay,bounds);
		layout.front;
		simpleController = this.getSimpleController(model);
		keyCodeResponder = model.defaultKeyCodeResponder(this);
		window.view.keyDownAction_(keyCodeResponder);
		
	}
	
	
	getSimpleController{|model|
		var obj, row;
	
		 simpleController = SimpleController(model)
		 	.put(\time, {
		 		this.currentTime_(model.currentTime);
		 	})
		 	.put(\remove, {
				//FIXME: view is not removed from the TrackLayout set
				//fix: remove returns an array of views:
				model.undoMem = model.lastRemovedObject;
				this.removeObject(model.lastRemovedObject);		 	})
		 	.put(\add, {
				obj = model.lastAddedObject;
				this.place(obj.time, obj.track,obj.multiTrackGui)		 	})							 	
		 	.put(\pps, {			
		 		this.pixelPerSecond_(model.pixelPerSecond);
			})		 	
			.put(\moveToTrack, {
				obj = model.lastAddedObject;	
				this.moveObjectToTrack(obj, obj.track);		 	});

	}
	
	removeAll{
		canvas.layout.removeAll;
	}

//	changeModel{|invo|
//		model.perform(invo);
//	}
	
	guify{|layout|
		var scrollview;
		if(model.respondsTo(\tracks)){
			maxrows = layoutTrackOffset + model.tracks.size * 2;
		};
		usableRows = (6, 8 .. 100);
		bounds = bounds ? Rect(0, 30, SCWindow.screenBounds.width, 600);

		
		if(model.respondsTo(\name)){	
			title = title ? "< " ++ model.name ++ " > rev.75";
		}{
			title = title ? "<unititle mtracker> rev.75";
		};
		if(layout.isNil){
			layout = SCWindow.new(title ,bounds);
			controlCanvas = SCCompositeView(layout, Rect(0, 0, 1400, 40)); 
			controlCanvas.decorator_(ScrollFlowLayout(controlCanvas.bounds));

			scrollview = SCScrollView(layout, Rect(0, 50, 150, layout.bounds.height-50))
				.resize_(1)
				.hasBorder_(true);			
			leftCanvas = SCCompositeView(scrollview, Rect(0, 0, 150, layout.bounds.height-50)); 
			leftCanvas.decorator_(ScrollFlowLayout(leftCanvas.bounds))
				.resize_(5);
			
			scrollview = SCScrollView(layout, Rect(150, 50, layout.bounds.width, layout.bounds.height-50))
				.resize_(5)
				.hasBorder_(true);
			canvas =  SCCompositeView(scrollview, Rect(0, 0, pixelPerSecond*360, 800)); 
			canvas.decorator = 
				TrackLayout(bounds, 20, maxrows, pixelPerSecond: pixelPerSecond);
			canvas.decorator.rowObjects[0].isStatic = true;
			canvas.decorator.rowObjects[1].isStatic = true;
//			canvas.background_(Color.white);
			

		};
		window = layout;
		
		layoutTrackOffset.do{|i| this.setRowSize(i, 20)};



		layout.onClose_({this.onClose});
		this.addFileMenu(controlCanvas);
		controlCanvas.decorator.nextLine;
		
		this.addTimeLine;
		this.addDropTracks;
		this.addControlInfo;
		
		this.setTimelineSpec;

		if(model.respondsTo(\currentTime)){
			this.currentTime_(model.currentTime);
		}{
			this.currentTime_(0);
		};
		
		if(model.respondsTo(\tracks)){

			model.tracks.do({|track, i|
				if(track.guiOptions.visible){
					track.do({|obj|
						this.place(obj.time,obj.track,obj.multiTrackGui)
					});
				}{
				//a little hack: set the tracksize to 1 or 0
					this.setRowSize(usableRows[i], 0);
				};
			});
		}	;
		/* uncomment for fast machines ? */

//		controlGui = MultiTrackerControlGui(model);
//		objectsToClose = objectsToClose.add(controlGui);

		window.acceptsMouseOver_(true);
		timelineSelector.focus;
	
//		scrollbar = SCSlider(window, Rect(0,0,window.view.bounds.width-40, 20)).resize_(2);
//		scrollbar.action_({|v| 
//				this.scrollBarAction(v.value);
//			})
//			.resize_(8)
//			.bounds_(
//			//scrollbar.bounds.moveTo(0,window.bounds.height-24)
//			Rect(0, window.bounds.height-30, window.view.bounds.width-73, 14)
//		).background_(Color.white).knobColor_(Color.black);
		
//		this.addZoomSliders(canvas);	
		if(model.respondsTo(\pixelPerSecond)){
			this.pixelPerSecond_(model.pixelPerSecond);
		};				
		
		this.initTools(leftCanvas);
		^layout;
	}
	
/*track layout:
	0 scroll
	1 timeline
	2 drop
	3 track
	4 drop
	...
	(8 usable tracks for now)

*/	

	initTools{|canvas|
		tools = MultiTrackToolsGui(model.tools, this, canvas);
	}
	
//	setLeftCanvas{|in|
//		tools[\soundFiles];
//		
//	}

	addZoomSliders{|canvas|
	//	trackScrollbar = SCSlider(window, Rect(0,0,window.view.bounds.width-40, 20)).resize_(2);
//		trackScrollbar.action_({|v| 
//			"not implemented yet".postln;
//			//sould resize track 0  - x?
//		});
//		trackScrollbar.bounds_(
//			//scrollbar.bounds.moveTo(0,window.bounds.height-24)
//			Rect(window.view.bounds.width-20, 70, 14, window.view.bounds.width-75-70)
//		).background_(Color.white).knobColor_(Color.black);	
			yzoomSlider = SCSlider(canvas, Rect(canvas.bounds.width-70,0,40, 20))
			.action_({|v| 
				model.pixelPerSecond_(v.value.linexp(0,1,1000, 20));
			})
			.resize_(9)
			.bounds_(
			Rect(canvas.bounds.width-65, window.bounds.height-30, 40, 14)
			).background_(Color.white).knobColor_(Color.grey);
		xzoomSlider = SCSlider(canvas, Rect(canvas.bounds.width-70,0,40, 20))
			.action_({|v| 
				this.resizeTracks(v.value.linlin(0,1,10, 700));
				scrollbar.bounds_(Rect(0, canvas.bounds.height-20, canvas.bounds.width-73, 14));
				yzoomSlider.bounds_(Rect(canvas.bounds.width-65, canvas.bounds.height-20, 40, 14));
				xzoomSlider.bounds_(Rect(canvas.bounds.width-20, canvas.bounds.height-30-40, 14, 40));
				trackScrollbar.bounds_(Rect(canvas.bounds.width-20, 70, 14, canvas.bounds.width-75-70));				
			})
			.value_(tracksize.linlin(10,700, 0, 1))
			.resize_(9)
			.bounds_(
				Rect(canvas.bounds.width-20, canvas.bounds.height-30-40, 14, 40)
			).background_(Color.white).knobColor_(Color.grey);
	}
	
	addFileMenu{|canvas|
		var 	menuItems = [\file, \open, \save, 'save as'];
//		canvas.decorator.currentRow_(0);	
		SCPopUpMenu(canvas,Rect(10, 10, 80, 20))
		.items_(menuItems)
		.canFocus_(false)
		.action_({|v| this.doFileMenuAction(v); v.value_(0)});
	
	}
	
	doFileMenuAction{|view|
		var state;
		state = view.items[view.value];
		if(state === \save){model.saveDialog;^this};
		if(state === \open){model.openDialog; ^this};
		if(state === 'save as'){model.save; ^this};

//		if(state === \render){this.openRenderDialog;^this};
//		if(state === \addSoundFile){model.putSoundFileDialog; ^this};
		
	}

	openRenderDialog{
		^MultiTrackerRenderDialog(model);
	}
	
	onClose{
		window = nil; 
		this.remove;
		objectsToClose.do{|it|it.onClose};
	}
	
	findFocus{
		^window.view.findFocus
	}
	
	keyDown{| v, char, modifiers, unicode,keycode|	
		^keyCodeResponder.value(v, char, modifiers, unicode,keycode);
		//defined in MultiTracker:defaultKeyCodeResponder
		
		/*
		if(char === $v){
			model.putCopyMemOnCurrentTime;
		};
		
		if(char === $o){
			model.putSoundFileDialog
		};
		if(char === $p){
			Document(model.name.asString, model.asArrayCompileString ++ 
				".asMultiTracker(" ++ model.name.asCompileString ++ ")");
		};		
		if(char === $ ){
			if(model.isPlaying){
				model.stop;
			}{
				model.play(model.currentTime)
			}
			^this;
		};
		if(char === $r){
			if(model.isPlaying){
				model.stop;
				model.server.stopRecording
			}{
				model.playAndRecord(model.currentTime);
			}
			^this;
		};
		if(char === $<){model.currentTime_(0.0)};
		if(char === $=){this.zoomAllOut};

		if (unicode == 16rF700) { 
			this.zoomOut;
//			this.scrollToTime(
//				(model.currentTime - ((window.view.bounds.width / model.pixelPerSecond) * 0.5)
//				).clip(0, model.maxTime * 2)
//			);			
			^this
		};
		if (unicode == 16rF703){
			//move_right.
			this.moveTimeCursor(\right);
		};
		if (unicode == 16rF701){ 
			this.zoomIn;
//			this.scrollToTime(
//				(model.currentTime - ((window.view.bounds.width / model.pixelPerSecond) * 0.5)
//				).clip(0, model.maxTime * 2)
//			);
			^this;
		};
		if (unicode == 16rF702) {
			this.moveTimeCursor(\left);
			};
		*/

	}
	
	moveTimeCursor{|dir|
		var mul;
		if(dir === \right)
		{
			mul = 1;
		}{
			mul = -1;
		};
		if(model.snapToGrid){
			model.currentTime_(model.currentTime + (model.grid* mul));
		}{
			model.currentTime_(model.currentTime + (0.1 * mul));
		};
		this.currentTime_(model.currentTime);
		
	}
	
	
	
	zoomIn{
		this.pixelPerSecond_((pixelPerSecond*0.9).clip(0,1000));
		model.pixelPerSecond_(this.pixelPerSecond);
	}
	
	zoomOut{
		this.pixelPerSecond_((pixelPerSecond*1.1).clip(0,1000));
		model.pixelPerSecond_(this.pixelPerSecond);
	
	}
		
//	globalKeyDown{| v, char, modifiers, unicode,keycode|
//		this.debug("MultiTrackerWindow:globalKeyDown depricated");
//		if(char === $<){model.currentTime_(0.0)};	
//		if(char == $ ){
//			if(model.isPlaying){
//				model.stop;
//			}{
//				model.play(model.currentTime)
//			}
//			^this;
//		};
//		if(char == $+){
//			this.increaseTrackSize;
//
//		};
//		if(char == $-){
//			this.decreaseTrackSize;
//
//		};
//		if(char == $W){
//			objects.do{|it| it.update(nil,\toggleWaveFormView);};
//		};
//		if(char === $E){
//			objects.do{|it| it.update(nil,\toggleEditView);};
//		};		
//		if(char === $U){
//			model.gui;
//			model.removeDependant(this);
//			window.close;
//		};		
//
//	}
	
	undo{
		undoManager.undo;
	}
	
	addControlInfo{
		guiControl = MultiTrackGuiControl(model, this);
	}
	
	addTimeLine{|seconds|
		//display seconds
		var txt, arr;
		seconds = seconds ? 1280;
		arr = Array.fill(seconds, 1.0);
		canvas.decorator.currentRow_(1);
		timePoints = SCMultiSliderView(canvas, canvas.bounds).
			valueThumbSize_(1.0).indexThumbSize_(1).gap_(this.pixelPerSecond - 1).
			readOnly_(true).isFilled_(true).fillColor_(Color.grey(0.5,0.5)).value_(arr).resize_(2);
		//canvas.view.decorator.currentRow_(1);		
		currentTimeText = SCStaticText(canvas, Rect(0,0, 100, 0)).string_(0.0.asString);
		timelineSelector = SCEnvelopeView(canvas,canvas.bounds).value_([[0.2],[0.0]]).resize_(2).
			thumbHeight_(100).thumbWidth_(2)
			.action_({|v|
				this.setTimelineSpec;
				v.selectIndex(0);
				model.currentTime = timelineSpec.map(v.value[0][0]) - timeOffset;
				currentTimeText.string_(model.currentTime.round(0.001).asTimeString);
						window.refresh;

			})
// fixme: why is this keyDownAction not bubbled to topview? keyDown
			.keyDownAction_({|v, char, modifiers, unicode, keycode|
				this.keyDown(v, char, modifiers, unicode, keycode);
//				this.debug("timelineSelector.keyDownAction");

			})
			.mouseUpAction_({|v| v.selectIndex(0)})
			.selectIndex(0).fixedSelection_(true);
		//timelineSelector.metaAction_({|
		this.setRowSize(3, 2);	
		this.setRowSize(4, 2);	
		this.setRowSize(5,2)

	}
	
	addDropTracks{
		var j = 0, tr, numtracks;
		if(model.respondsTo(\tracks)){
			numtracks = model.tracks.size;
		}{
			numtracks =1;
		};
		//tr = [1,2,3,4,5,6,7,8];
		
		numtracks.do({|i|
			var row = usableRows[i];
			j = j + 1;
			canvas.decorator.currentRow_(row);
			canvas.decorator.rowObjects[row].isStatic = true;
			objectsToClose = objectsToClose.add(MTrackGui(this, i, row));
			this.setRowSize(row, tracksize);
			this.setRowSize(row+1,3)
		});
	
	}
	
	drawCurrentDrag{|x|
		window.drawHook = {
//				timePoint = model.currentTime * this.pixelPerSecond + 1;
				Pen.use {
					Pen.width = 1;
						Pen.beginPath;
						Pen.moveTo(Point(x, 21));
						Pen.lineTo(Point(x, canvas.bounds.height));//window.view.decorator.rowsize));
						Pen.stroke;
				};
				//fixme check bounds ..
//				scrollbar.bounds_(scrollbar.bounds.moveTo(0,window.bounds.height-20));
//				yzoomSlider.bounds_(yzoomSlider.bounds.moveTo(window.bounds.width-65,window.bounds.height-20));
//				xzoomSlider.bounds_(xzoomSlider.bounds.moveTo(canvas.bounds.width-20, window.bounds.height-30-40));
//				trackScrollbar.bounds_(Rect(window.view.bounds.width-20, 70, 14, window.view.bounds.width-75-70));				
				
//				window.bounds.postln;
	
			};
//			window.refresh;
//			this.setDrawHook;
//			window.drawHook = {};	
		}
	
	
	//do not use ... setDrawHook
	setDrawHook{
		window.drawHook = {
		//	var timePoint;
//			timePoint = model.currentTime * this.pixelPerSecond + 1;
//			Pen.use {
//				Pen.width = 1;
//					Pen.beginPath;
//					Pen.moveTo(Point(timePoint, 21));
//					Pen.lineTo(Point(timePoint, window.bounds.width));
//					Pen.stroke;
//			};
//			window.bounds.postln;
		};
//		window.refresh;	
	}
	
	scrollToTime{|atime|
		timeOffset = -1 * atime;
		objects.do({|obj|
			this.moveObjectToTime(obj.view, obj.model.time + timeOffset);
			this.resizeObjectDur(obj.view, obj.model.duration);
			obj.update(obj.view.bounds, \viewBounds);
	
		});
		this.setTimelineSpec;
//		this.currentTime_(model.currentTime);
//		this.debug([\scrollToTime, atime])
	}
	
	
	front{
		window.front;
		^this
	}
	
	focus{
		timelineSelector.focus
	}
	
	remove{
		objects.do({|obj|
			obj.remove(true);
		});
		model.removeDependant(this);
		undoManager.remove;
	}
	
	setTimelineSpec{ 
		var from, to;
		//from = timeOffset;
		from = 0;
		to = (canvas.bounds.width / this.pixelPerSecond);
		timelineSpec = [from, to].asSpec;	
	}
	
	zoomAllOut{ // resize to fit
		var maxtime=0, dur;
		model.tracks.do{|track|
			track.data.asArray.do{|obj|
				dur = (obj.time + obj.duration);
				if(dur > maxtime){
					maxtime = dur;
				}
			};
		};
		model.pixelPerSecond_(canvas.bounds.width/ maxtime);
		this.pixelPerSecond_(model.pixelPerSecond); //fixme this is in if the model does not send a changer 

	}
	
	currentTime_{|time|
		//this.model.currentTime_(time);

		
		{
			var timelinevalue;
			this.setTimelineSpec;
			timelineSelector.index_(0);
			timelinevalue  = timelineSpec.unmap(time + timeOffset);
			if(timelinevalue >= 1){
				this.scrollToTime(time);
				this.setTimelineSpec;
				timelineSelector.index_(0);
				timelinevalue  = timelineSpec.unmap(time + timeOffset);
			};	
			if(timelinevalue <= 0){
				this.scrollToTime(time);
				this.setTimelineSpec;
				timelineSelector.index_(0);
				timelinevalue  = timelineSpec.unmap(time + timeOffset);
			};				
			timelineSelector.value_([[timelinevalue],[0.0]]);
			//(0.4);
			currentTimeText.string_(time.round(0.001).asTimeString);
			//window.refresh;
		
		}.defer;
	}
	
	resizeTracks{|size|	
		model.tracks.do{|track,i|
			if(track.guiOptions.visible){
				this.setRowSize(usableRows[i], size);
				track.guiOptions.height_(size);
			}{				
				this.setRowSize(usableRows[i], 0);
			}
		}	
	}
	
	increaseTrackSize{
		tracksize = tracksize + 5;
		this.resizeTracks(tracksize);
	}
	
	decreaseTrackSize{
		tracksize = tracksize - 5;
		if(tracksize < 0){tracksize = 0};
		this.resizeTracks(tracksize);
	}
	
	/***
	*** patch through ...
	***/
		
	setRowSize{|which, size|
		canvas.decorator.setRowSize(which, size);
	}
	
	moveObjectToRow{| obj, row, oldrow|
		canvas.decorator.moveObjectToRow(obj, row, oldrow);
	}
	
	//fix me ! please 
	moveObjectToTrack{|obj, track|
		canvas.decorator.moveObjectsArrayToRow(obj.dependants.array, usableRows[track]);
//		this.debug(obj.dependants.array);
	}

	moveObjectToLeftBy{|obj, left|
		canvas.decorator.moveObjectToLeftBy(obj, left);
	}
	
	moveObjectToTime{|obj, time|
		canvas.decorator.moveObjectToTime(obj, time);
	}
	
	resizeObjectDur{|obj, dur|
		canvas.decorator.resizeObjectDur(obj, dur);
	}
	
	pixelPerSecond_{|pps|
		if(window.notNil){
			canvas.decorator.pixelPerSecond_(pps);
		};
		pixelPerSecond = pps;
		timePoints.gap_(pps-1);
		this.setTimelineSpec;
		this.currentTime_(model.currentTime); //reset timedisplay
		objects.do({|obj|
			this.moveObjectToTime(obj.view, obj.model.time);
			this.resizeObjectDur(obj.view, obj.model.duration);
			obj.update(obj.view.bounds, \viewBounds);
		});
	}
	
	removeObject{|obj|
		var row, views, objguis;
		row =  obj.track + 9; 
		objguis = obj.dependants;
		canvas.decorator.removeMatchingObjectFromRow(row, views);
		objguis.do({|o|
			objects.remove(o);
		});
		views = obj.removeViews;		
	}
	
	place{|time, track, object|
		var item, viewObject;
		//TODO: need to set the duration here as well !
		canvas.decorator.currentRow_(usableRows[track]);

		viewObject = object.gui(canvas, this, time);
		objects.add(object);
		object.usableRows_(usableRows);
		viewObject.focus;
		this.setInfo(object.model);
	}
	
	setInfoString{|str, time, dur, value=0|
		time = time.asTimeArray;
//		str = "info: " ++ str;
		infoText.string_(str);
		infoTime.value_(time[1]);
		infoDur.value_(dur);
		infoVal.value_(value);
		
	}
	
	setInfo{|object, value|
		guiControl.setObject(object, value);
	}
	
//	undo{|what|
//		var obj;
//		if(what===\remove){
//			obj = model.lastRemovedObject;
//			model.put(obj.track, obj.time, obj.duration, obj);
//		}
//	}

// moved update to simpleController
	
//	update{|model, changer|
//		var obj, row;
//	
//		if(changer === \time){
//			this.currentTime_(model.currentTime)
//		};
//		if(changer === \pps){
//			this.pixelPerSecond_(model.pixelPerSecond);
//		};
//		if(changer === \remove){	
//		//FIXME: view is not removed from the TrackLayout set
//		//fix: remove returns an array of views:
//			model.undoMem = model.lastRemovedObject;
//			this.removeObject(model.lastRemovedObject);
//
//		};
//		if(changer === \add){	
//			obj = model.lastAddedObject;
//			this.place(obj.time, obj.track,obj.multiTrackGui)
//		};
//		//moveToTrack
//		if(changer === \moveToTrack){
//			obj = model.lastAddedObject;	
//			this.moveObjectToTrack(obj, obj.track);
//		};
//
//	}
}

