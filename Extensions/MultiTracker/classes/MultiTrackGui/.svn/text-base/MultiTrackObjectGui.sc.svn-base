/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

/*
	todo: MultiTrackObjectGui should be more abstract ( its object should be Object) 
	changing the scaling needs some fixes
*/
MultiTrackObjectGui  {

	var <background, selectionColor;	
	var <> model, view, infoPanel, editable=false, <>argViewSelector;
	var <>parent; //need the parent to set the infoView parent is the MultiTrackerWindow
	var <> usableRows;
	var <extraViews, currentView;
	var <mode, <lastEnvelope;
	var < currentEdit; // Association (key, index)
	var deltaX, resizing=false;
	var <>isSelected=false;
	var undo;
	var keyCodeResponder;
	
	*new{|model|
		^super.new.model_(model);
	}
	
	view{
		^currentView
	}
	
	refresh{
		parent.refresh;
	}
	
	background_{|color|
		view.background_(color);
	}
	removeExtraViews{
		extraViews.asArray.copy.do{|it| it.remove};
		extraViews = nil;
	}
		
	bounds{
		^view.bounds
	}
	
	bounds_{|in|
		view.bounds_(in)
	}
	
	focus{
		view.focus
	}
	
	gui{|win, multitrackergui, time|
		
		parent = multitrackergui;
		background = ColorBrowser.favorites.choose;//Color.blue(0.5, 0.5);
		selectionColor = background.add(Color.gray(0.3, 0.6));
		mode = \showName;
		keyCodeResponder = model.defaultKeyCodeResponder(this);

		view = SCEnvelopeView(win, Rect(0,0, 100, 200)).value_([[0.0],[0.0]])
			.select(0)
			.drawLines_(true)
			.drawRects_(true)
			.selectionColor_(Color.red)
			.setString(0,model.name)
			.fillColor_(Color.white)
			.background_(background)
			.setEditable(0, false)
			.thumbWidth_(160)
			.thumbHeight_(15)
			.focusColor_(Color.white.alpha_(0.0))
			.action_({this.setInfo})
			/*set the xposition of the view with control-mouse-down*/
			.metaAction_({|v|
				this.metaAction(v,  v.getProperty(\absoluteX));
			})
			.mouseMoveAction_({|v, x, y|
				if(mode == \showName){
					this.mouseMove(v, x);
				}
			})
			.mouseDownAction_({|v, x, y, mod, num, clicks|
				this.mouseDown(v, x, y, mod, num, clicks)
			})
			.keyDownAction_(keyCodeResponder)
			.canReceiveDragHandler_({|v| SCView.currentDrag.isKindOf(Color) or: v.defaultCanReceiveDrag;})
			.receiveDragHandler_({|v| if(SCView.currentDrag.isKindOf(Color)){v.background_(SCView.currentDrag)}{v.defaultReceiveDrag;} });		
		this.moveObjectToTime(view, time);
		this.resizeObjectDur;
		currentView = view;
		lastEnvelope = \amp;
		this.setupUndo;
		^view
		
	}
	
	remove{ arg removeView=false;
		model.removeDependant(this);
		if(removeView,{
			view.remove(true);
			view = nil;
			extraViews.asArray.do(_.remove);	
			extraViews = nil;	
		});
		if(argViewSelector.notNil){argViewSelector.remove};
	}
	
	select{
		view.background_(selectionColor).fillColor_(Color.new255(255, 140, 0));
		view.focus;
		isSelected = true;
	}

	unselect{
		if(view.notNil){
			view.background_(background).fillColor_(Color.white);
		};
		isSelected = false;
	}
		
	moveObjectToTime{|aview, time|
		parent.canvas.decorator.moveObjectToTime(aview, time);
	}
	
	resizeObjectDur{
		this.prResizeObjectDur(view);
		if(this.hasExtraViews){
			extraViews.asArray.do{|it|	
				this.prResizeObjectDur(it, model.duration);
			}
		}	
	}
	
	prResizeObjectDur{|aview|
		parent.resizeObjectDur(aview, model.duration);	
	}
	


/* undo stuff */
	setupUndo{
//		undo = UndoManager(this);
//		undo.register('setTime', 'getTime', model.time);
		parent.undoManager.register(this,'setTime', 'getTime', model.time);
		parent.undoManager.register(this,'setDuration', 'getDuration', model.duration);

	}
	
	undo{
		parent.undo
	}
		
	setTime{|time|
	//used by undo and update//
//		model.time = time;
		this.moveObjectToTime(view, time);
	}

	getTime{
	//used by undo only//
		^model.time
	}
	
	setDuration{|dur, doChange=false|
		var stTime=0;
		if(model.maxDuration.notNil)
		{
//			if(model.respondsTo(\startTime))
//			{
//				stTime = model.startTime;
////				this.debug(stTime);
//			};
//			dur = dur.clip(0, model.maxDuration - stTime);
		};
//		model.duration = dur;
		this.resizeObjectDur;
	}
	
	getDuration{
	//used by undo only//
		^model.duration
	}		
/* mouse moving */		

	mouseDown{|v,x,y,mod,clickcount|
		var shift = 131330, reset=true, selections;
		selections = parent.model.tools[\selections];
		if(mod == shift){reset=false};
		deltaX = x - v.bounds.left;
		selections.selectObject(this.model, reset);
		
		if(deltaX > (v.bounds.width*0.7))
		{
			resizing = true;
		}{
			resizing = false;
		};
		if(clickcount >1){this.showArgView};
		this.setInfo;
	}
	
	mouseMove{|v,x,y|
		this.moveObjectAbs(v,x);
	}
	
	metaAction{|v,x,y|
		this.moveObjectAbs(v,x);
	}
	
	moveObjectAbs{|v, xpos|
		var x, dx, dt, selections;
		selections = parent.model.tools[\selections];
		if(resizing){
			model.duration_( (xpos / parent.pixelPerSecond) - parent.timeOffset - model.time);
			this.changed(\setDuration);

		}{
			dx = (xpos - deltaX) - view.bounds.left;
			if(parent.model.snapToGrid){
				dx = dx.round(parent.model.grid * parent.pixelPerSecond);
			};
			dt = (dx / parent.pixelPerSecond);
			/* call moveObjectRel via parent */
			selections.moveSelectedObjects(dt);
		};
		this.setInfo;
	}

// moved to MultiTrackObject:moveRelative
	moveObjectRel{|dt|
		var xpos, x;
		model.time = model.time + dt;
		this.debug("moveObjectRel has moved! update the code ;-");
//		view.bounds = view.bounds.moveBy(dx,0);	
//fixme who needs this changed?
		this.changed(\setTime);
	}
	
	
	
/* key down */	
	//gets added in MultiTrackerWindow 
	keyDown{| v, char, modifiers, unicode,keycode|
		keyCodeResponder.value(v, char, modifiers, unicode, keycode)
	}

	splitAtCursor{
		var mtracker, startframe, numframes;
		mtracker =  parent.model;
		mtracker.split(model, mtracker.currentTime)
	}
		
	showArgView{
			if(argViewSelector.isNil){argViewSelector = model.argViewSelectorClass.new(this)};
			if(argViewSelector.class == model.argViewSelectorClass 
				and: (argViewSelector.isGlobalArgView))
			{
				argViewSelector.setParent(model);
			};
			argViewSelector.front;	
	}

	infoPanelClass{
		^MultiTrackObjectInfo
	}
	
	showName{
//		view
//			.setString(0,model.object.asString)
//			.setEditable(0, false)
//			.setThumbWidth(0, 60)
//			.setThumbHeight(0, 15)
//			.value_([[0.0],[0.0]])
//			.action_({this.setInfo});
//		editable = false;
		view.visible_(true);
		mode = \showName;
	}
	
	setInfo{|value|
		parent.setInfo(model, value);	
	}
	
	setInfoString{|value|
		this.setInfo(value);
//		parent.setInfoString("name : " ++ model.name ,
//			model.time, model.duration);
//		parent.setInfoString("name : " ++ model.name " ++ model.time.asString ++ " | dur: " ++ model.duration.asString ++ " | "++ str);
		this.debug("setInfoString depricated..");
	}
	
	setCurrentValue{|val|
		//currentEdit.key
		var index, vals, min, max, valy;
//		this.debug(val);

		index = model.args.indexOf(currentEdit.key);
		if(index.isNil){^this};
		vals = model.args[index+1];
		if(vals.isKindOf(Env).not){^this};
		vals.levels[currentEdit.value] = val;
	
//		if(model.args[index+1].curves == 2){
//			model.args[index+1].levels_(v.value[1].linexp(0.0, 1.0, min, max));
//		}{
//			model.args[index+1].levels_(v.value[1].linlin(0.0, 1.0, min, max));
//		};
				
		if(mode === \editEnv)
		{
			min = vals.levels.minItem;
			max = vals.levels.maxItem;
			if(vals.curves == 2){
				valy = vals.levels.explin(min, max, 0.0, 1.0);
			}{
				valy = vals.levels.linlin(min, max, 0.0, 1.0);
			};
			view.value = [view.value[0], valy];
		};
		
	}
	
	toggleEditView{
//		this.debug(\toggelEdit);

		if(mode === \showName){
			this.showEnvelope(lastEnvelope);
		}{
			this.showName;
		}
	}
	
	editEnvelope{|which|
		var index, env,w;
		index = model.args.indexOf(which);
		if(index.isNil){^this};
		env = model.args[index+1];
		if(env.isKindOf(Env).not){^this};
		w = SCWindow("Env Editor", Rect(200,200,300,200));
		SCEnvelopeEdit2(w, w.view.bounds.moveBy(20,20).resizeBy(-40,-40), env, 20).resize_(5);
		w.front;	
	}
	
	showEnvelope{|which, min, max, envView|
		var index, env, valx, valy, temp;
		//view.postln;
//		envView =  envView ? view;
//		if(mode !=  \editEnv){
			extraViews.asArray.copy.do{|it| it.remove};
//			};
		lastEnvelope = which;
		index = model.args.indexOf(which);
		if(index.isNil){^this};
		env = model.args[index+1];
		if(env.isKindOf(Env)){
			view.visible_(false);
		
			extraViews = SCEnvelopeEdit2(parent.window, view.bounds, env, 10, min, max)
				.background_(view.background)
				.keyDownAction_({|v, char, modifiers, unicode,keycode|
					this.keyDown(v, char, modifiers, unicode,keycode)
				})
				.editAction_({|v|
//					v.defaultAction(v);
					this.setInfo(v.currentLevel);
				})
				.mouseUpAction_({|v| 
				var envTime, val;
				valx = v.value[0] * model.duration;
				
				if(valx[0] > 0){
					val = v.value;
					val[0][0] = 0.0;
					v.value_(val);
					valx[0] = 0.0;
				};
				if(model.args[index+1].isKindOf(Env)){
					model.args[index+1] = extraViews.env;
					model.args[index+1].addToSynthiArgs(model.object, model.args[index].asSymbol);
				}
				});
			mode = \editEnv;
			
			^this;
		};
	}
	
	hideEnvelope{
		if(mode === \showName){^this};
		extraViews.asArray.(_.visible_(false));
		extraViews.asArray.copy.do{|it| it.remove};
		this.debug(\hideEnvelope);
		currentView = view;
		view.visible_(true);
		mode = \showName;
		this.refresh;

	//	extraViews = [];
	}	
	update{|changer, what, value|
		if(what === \toggleEditView){
			this.toggleEditView;
		};
		if(what === \time){
//			this.debug(model.time);
//			parent.canvas.decorator.moveObjectToTime(view, model.time);
			this.setTime(model.time);
			
		};
		if(what === \duration){
//			this.debug(model.time);
			this.resizeObjectDur;
				
		};
		if(what === \currentValue){
			this.setCurrentValue(value);
//			this.debug(value);
		};
		if(what === \select){
			this.select;
		};	
		if(what === \unselect){
			this.unselect;
		};	
		if(what === \track){
//			this.debug("chaged track!");
		};		
		if(what === \duration){
			this.setDuration(model.duration)
		};	
	}
	
	hasExtraViews{
		^extraViews.notNil
	}


}

SoundFileMultiTrackObjectGui : MultiTrackObjectGui{
	var modus;
/*model is: SoundFileFromDiskMultiTrackObject */

//	gui{|win, multitrackergui, time|	
//			parent = multitrackergui;
//			
//			extraViews = SCSoundFileView(win, 100@200)
//			.elasticMode_(1)
//			.keyDownAction_({|v, char, modifiers, unicode,keycode|
//				view.keyDownAction(v, char, modifiers, unicode,keycode)
//			})
//			.metaAction_({|v| 
//				this.update(extraViews.bounds, \viewBounds);
//				v.drawsWaveForm_(false);
//				this.metaAction.value(v)})
//			.mouseUpAction_({|v| v.drawsWaveForm_(true)})
//			.readFile(model.object.path, 0, model.duration*model.object.info[2])
//			.gridOn_(false)
//			.visible_(false);
//		this.moveObjectToTime(extraViews, time);
//		this.resizeObjectDur(extraViews);			
//		super.gui(win, multitrackergui, time);
//	}

	
	showWaveForm{|win, bounds, read=true|
		if(modus === \soundFileView){^this};
		view.visible_(false);
		win.view.decorator.currentRow_(usableRows[model.track]);
		if(extraViews.notNil){
			this.removeExtraViews;
			/*should re-check for frame range */
	//		if(extraViews.isClosed.not){
//					
//					extraViews.bounds = bounds;
//					extraViews.visible_(true);
//					modus = \soundFileView;
////					this.update(extraViews.bounds, \viewBounds);
//					extraViews.focus;
//					currentView = extraViews;
//					this.setSoundFileViewData;		
//					^this;
//			}
		};
		extraViews = SCSoundFileView(win, bounds)
			.soundfile_(SoundFile(model.object.path))
			.keyDownAction_({|v, char, modifiers, unicode,keycode|
				this.keyDown(v, char, modifiers, unicode,keycode)}
				)
			.metaAction_({|v| 
				this.update(extraViews.bounds, \viewBounds);
				v.drawsWaveForm_(false);
				this.metaAction(v)})
			.mouseUpAction_({|v| v.drawsWaveForm_(true)})
			.gridOn_(false)
			.waveColors_({Color.white}!model.object.numChannels)
			.background_(this.background);		
			this.setSoundFileViewData;	
		//soundfileViewModel.trim(model.startTime, model.endTime);
		//soundfileViewModel.pixelPerSecond_(bounds.width/soundfileViewModel.duration);
//		extraViews = SoundFileViewGui.createMultiSliderView(win, bounds, soundfileViewModel,
//			nil, nil, 
//			view.keyDownAction, 
//			view.metaAction );
		
		modus = \soundFileView;
		this.update(view.bounds, \viewBounds);
		extraViews.focus;
		currentView = extraViews;
//		this.debug([\startFrame, model.object.startFrame]);
		
	}
	
	remove{ arg removeView=false;
		model.removeDependant(this);
		if(removeView,{
			if(extraViews.notNil){extraViews.remove; extraViews=nil};
			view.remove(true);
			view = nil;		
		});
		if(argViewSelector.notNil){argViewSelector.remove};
	}
	
	hideWaveForm{
	//	extraViews.do{|v| v.visible_(false); v.remove};
		view.bounds_(extraViews.bounds).visible_(true);
		this.removeExtraViews;
		currentView = view;
		modus = \showName;
	//	extraViews = [];
	}
	
	toggleWaveFormView{|read = true|
		if(modus === \soundFileView){
			this.hideWaveForm; 
			^this;
		}{
			this.showWaveForm(parent.window, view.bounds.deepCopy, read);
		}	
		
	}
	
	keyDown{| v, char, modifiers, unicode,keycode|
//		char.postln;
		if(char==$w){
			this.toggleWaveFormView(read:false);
			^this;
		};
		if(char==$W){
			this.toggleWaveFormView(read:true);
			^this;
		};		
		if(char==$e){
			if(argViewSelector.isNil){argViewSelector = model.argViewSelectorClass.new(this)};
			argViewSelector.front;
			^this;
		};
		
		if(char==$a){
			if(editable){
				view.addValue
			};
			^this
		};
		if(char==$s){
//			if(editable){
				this.splitAtCursor(v);
//			};
			^this
		};		
		this.setInfo;
		super.keyDown(v, char, modifiers, unicode,keycode);
	}	
	
	
	setSoundFileViewData{|read = false|
		var w, a;	
		
		if(this.hasExtraViews.not){^this};
		//lazy init collector ...
		if(SoundFileDataCollector.initialized.not){SoundFileDataCollector.init};
		if(SoundFileDataCollector.at(model.object.path).notNil and: read.not){
				SoundFileDataCollector.set(extraViews, 
					model.object.startFrame, model.duration * model.object.sampleRate);
		}{
		// fixme: because there's a bug in readWithTask in comb. with startframe, 
		// read the whole file and then set data again
		// fixme : currently the object does not know the right numFrames ... calc through duration
		
			extraViews.readWithTask(0, model.object.numFrames, doneAction:
				{|view| 
					SoundFileDataCollector.put(view);
					if(model.object.startFrame != 0){ //if we don't check here sc crashes on SCSoundFileView line 973 ! fixme ...
						SoundFileDataCollector.set(view, 
							model.object.startFrame, model.duration * model.object.sampleRate);
					};
					view.elasticMode_(1).refresh;
	
					});				
			};		
//		this.debug([\numFrames, model.object.numFrames, model.duration * model.object.sampleRate]);
//		this.debug([\startFrame,model.object.startFrame]);
						
	}
		
	resizeObjectDur{
		super.resizeObjectDur;
		this.setSoundFileViewData;
	}
	
	update{|changer, what|
		if(what===\viewBounds){
//			if(modus === \soundFileView){view.bounds = extraViews.bounds};
//				this.debug(\updatingViewBounds);

			if(view.bounds != changer){
				view.bounds = changer;
				extraViews.bounds = view.bounds;
				
			};

			if(extraViews.isNil){^this};
//			if(extraViews.bounds != changer){
				extraViews.bounds = changer;
//			};
			^this
			
		};
		if(what === \showWaveForm){
			this.showWaveForm(parent.window, view.bounds.deepCopy); ^this
		};
		if(what === \hideWaveForm){
			this.hideWaveForm; ^this
		};
//		what.postln;
		if(what === \toggleWaveFormView){
			this.toggleWaveFormView;
		};
		super.update(changer, what);
	}
	
}

MultiTrackerMultiTrackObjectGui : MultiTrackObjectGui{

	keyDown{| v, char, modifiers, unicode,keycode|

		if(char==$e){
//			if(argViewSelector.isNil){argViewSelector = model.object.gui};
			//argViewSelector.front;
			model.object.gui;
			^this;
		};
		
		this.setInfo;
	}


}
