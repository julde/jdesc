/*
	KeyCodeResponder.tester
	
*/

+ MultiTracker {

	/* this one is global */
	
	defaultKeyCodeResponder{|view| //view is a MultiTrackerWindow
		/*
			for now: if there has to be a specific talkback for a key
			use this.changed(\key) to pass it on to the talkbackController in
			MultiTracker:enableTalkback
			KeyCodeResponder.tester;
		*/
		var k = KeyCodeResponder.new;
		//  i
		k.register(   34  ,   false, false, false, false, {
			this.changed(\speakTime)
		});	
		//  f
		k.register(   3  ,   false, false, false, false, {
			this.changed(\speakFocus);
		});		
		//  53 ESC
		k.register(   53  ,   false, false, false, false, {
			Speech.channels[0].stop;
		});
		
		k.register(   53  ,   false, false, false, false, {
			Speech.channels[0].stop;
		});
				
		k.register(   49  ,   false, false, false, false, {
			if(this.isPlaying){
				this.stop;
				this.server.stopRecording		
			}{
				this.playAndRecord(this.currentTime);
			}
		});
		//  arrow (<-)
		k.register(   123  ,   false, false, false, false, {
			if(this.snapToGrid){
				this.currentTime_(this.currentTime + (this.grid* -1));
			}{
				this.currentTime_(this.currentTime + (0.1 * -1));
			};	
		});	
		//  arrow (->)
		k.register(   124  ,   false, false, false, false, {
			if(this.snapToGrid){
				this.currentTime_(this.currentTime + (this.grid* 1));
			}{
				this.currentTime_(this.currentTime + (0.1 * 1));
			};		
		});	
		// shift arrow (<-)

		k.register(   123  ,   true, false, false, false, {
			this.returnToStart;
			this.changed(\returnToStart);
			
		});			
		// shift ctrl arrow (<-)

		k.register(   123  ,   true, false, false, true, {
			this.currentTime_(0.0);
			this.changed(\returnToBeginning)
			
		});
		
		//  control `
		k.register(   50  ,   false, false, false, true, {
			//fixme need to find current view and set it as 3rd arg here:
			JTFocusSwitcher([\soundfiles, \timeline], [view.tools,view]);
		});	
		
		//  .
		k.register(   47  ,   false, false, false, false, {
			var lastfocus = view.findFocus;
//			this.debug(lastfocus);
			if(lastfocus.notNil){ view.guiControl.currentTimeSetter.doneAction_{lastfocus.focus; this.changed(\speakTime)}
			}{ view.guiControl.currentTimeSetter.doneAction_{this.changed(\speakTime)}};
			view.guiControl.currentTimeSetter.focus;
		});		
			
		/* selection stuff is definded in MultiTrackerSelectionManager */	
		//  option 124 ->
		k.register(   124  ,   false, false, true, false, {
			tools[\selections].nextObjectOnCurrentTrack;
		});
		//  option 124 <-
		k.register(   123  ,   false, false, true, false, {
			tools[\selections].previousObjectOnCurrentTrack;
		});
		

		//  option control 1 - 0
		6.do{|i|
			k.register(   18+i  ,   false, false, true, false, {
				tools[\selections].currentObjectOnTrack(i+1);
	
			});
		};

		//  control 0
		k.register(   29  ,   false, false, true, false, {
				tools[\selections].currentObjectOnTrack(0);
		
		});		
													
		^k;
	}
	
	controlMethods{
		^#[play, stop, currentTime_]
	}
	

}


+ MultiTrackTools {

	defaultKeyCodeResponder{|view|
		var k = KeyCodeResponder.new;
		
		//  control `
		k.register(   50  ,   false, false, false, true, {
			JTFocusSwitcher([\soundfiles, \timeline], [view,view.parent]);
		});
		^k
	}
	

}
// MultiTrackSoundFilePoolGui
+ MultiTrackSoundFilePool{
	defaultKeyCodeResponder{|view|
		var k;
		k = KeyCodeResponder.new;
		//  control  a
		k.register(   0  ,   false, false, false, true, {
			model.put(0, model.currentTime, this.currentItem.duration,this.currentItem);
			this.changed(\speakAdd)
		});
				
		//  control-option-  a
		k.register(   0  ,   false, false, true, true, {
			var lastfocus, mwindow, doneAction;
			mwindow = view.parent.parent;
			lastfocus = mwindow.findFocus;
			model.put(0, model.currentTime, this.currentItem.duration,this.currentItem, speak:false);
			model.tools[\selections].selectObject(model.lastAddedObject);
			{model.changed(\speakText, 			
				"adding soundfile, please type in starting time and track number")}.defer(0.8);

			doneAction = {			
				model.changed(\speakAdd);
				view.focus;
				mwindow.guiControl.trackNumSetter.removeAction(thisFunction);};
			
//			this.debug(view.parent.parent);
//			this.debug(lastfocus);
//			if(lastfocus.notNil){ mwindow.guiControl.currentTimeSetter.doneAction_{lastfocus.focus; this.changed(\speakTime)}
//			}{ mwindow.guiControl.currentTimeSetter.doneAction_{this.changed(\speakTime)}};
			mwindow.guiControl.objectTimeSetter.focus;
			mwindow.guiControl.objectTimeSetter.doneAction_{mwindow.guiControl.trackNumSetter.focus};
			mwindow.guiControl.trackNumSetter.addAction(doneAction);
			

		});
		//  control 31(o)
		k.register(   31  ,   false, false, false, true, {
			this.readFilesWithDialog
		});	
		//  control-shift 31(o)
		k.register(   31  ,   true, false, false, true, {	
			this.readDirectoryWithDialog
		});
		//  p
		k.register(   35  ,   false, false, false, false, {
			if(player.isNil) {player = this.currentItem.multiTrackPlayer.play};
		});		
		//  51 del
		k.register(   51  ,   false, false, false, false, {
			this.removeAt(view.fileView.item);
		});		
		
//	//  124 ->
//	k.register(   124  ,   false, false, false, false, {
//		nil
//	});	
//	//  123 <-
//	k.register(   124  ,   false, false, false, false, {
//		nil
//	});		
//	//  126 arrow up
//	k.register(   124  ,   false, false, false, false, {
//	
//	});	
//	//  125 arrow down
//	k.register(   124  ,   false, false, false, false, {
//	
//	});					
		^k
	}
	
	defaultKeyUpKeyCodeResponder{
		
		var k;
		k = KeyCodeResponder.new;
		//  p
		k.register(   35  ,   false, false, false, false, {
			player.stop; player = nil;
		});
				
		^k
	}
	
}

+ MultiTrackObject {
/* it's a long way to the MutliTracker since this Object doesn't know anything about it ;-|
	it's at: view.parent.model
*/

	defaultKeyCodeResponder{|view|
	var k;
	k = KeyCodeResponder.new;

	//  51 del
	k.register(   51  ,   false, false, false, false, {
		view.parent.model.remove(this.track, this);
	
	});
	
		// control i
		k.register(   34  ,   false, false, false, true, {
			this.changed(\speakTime)
		});
		
		// control .
		k.register(   47  ,   false, false, false, true, {
			var lastfocus = view;
			if(lastfocus.notNil){ 
				view.parent.guiControl.objectTimeSetter.doneAction_{
					lastfocus.focus; this.changed(\speakTime)}
			}{ 
				view.parent.guiControl.objectTimeSetter.doneAction_{
					this.changed(\speakTime)}};
			view.parent.guiControl.objectTimeSetter.focus;
		});			
		
	
	/* selection stuff is definded in MultiTrackerSelectionManager */	

			
	//  control 124 ->
	k.register(   124  ,   false, false, false, true, {
				//move_right.
//				this.time = this.time + view.parent.model.grid;
				view.parent.model.tools[\selections].moveSelectedObjects(view.parent.model.grid);
				view.setInfo;
	});	
	//  control 123 <-
	k.register(   123  ,   false, false, false, true, {
				//move_left.
//				this.time = this.time - view.parent.model.grid;
				view.parent.model.tools[\selections].moveSelectedObjects(view.parent.model.grid.neg);
				view.setInfo;
	});	

	//  option control 124 ->
	k.register(   124  ,   false, false, true, true, {
				//move_right.
		view.parent.model.tools[\selections].nextObjectOnCurrentTrack
	});	
	//  option control 123 <-
	k.register(   123  ,   false, false, true, true, {
				//move_left.
		view.parent.model.tools[\selections].previousObjectOnCurrentTrack
	});	
	
	//  shift option control 124 ->
	k.register(   124  ,   true, false, true, true, {
				//move_right.
		view.parent.model.tools[\selections].addNextObjectOnCurrentTrack
	});	
	//  shift option control 123 <-
	k.register(   123  ,   true, false, true, true, {
				//move_left.
		view.parent.model.tools[\selections].addPreviousObjectOnCurrentTrack
	});	
		
	//  arrow down
	k.register(   126  ,   false, false, false, true, {
				//move_right.
//				this.time = this.time + view.parent.model.grid;
				view.parent.model.tools[\selections].changeRelativeTrackForSelectedObjects(-1);
				view.setInfo;
	});	
	//  arrow up
	k.register(   125  ,   false, false, false, true, {
				//move_left.
//				this.time = this.time - view.parent.model.grid;
				view.parent.model.tools[\selections].changeRelativeTrackForSelectedObjects(1);
				view.setInfo;
	});	

	//  control y
	k.register(   16  ,   false, false, false, true, {
				view.splitAtCursor;
	});
	
	//   y
	k.register(   16  ,   false, false, false, false, {
				
				if(view.parent.model.currentTime > this.startTime){
//					this.debug("change dur");
					this.duration_(view.parent.model.currentTime - this.time);
				}
	});
	
	/*
	
	if(unicode==127){
				parent.model.remove(model.track, model);
				^this;
			};
			if (unicode == 16rF700) { 
				parent.model.moveObjectToTrack(model, model.track - 1) ;
				this.setInfo;				
				^this;
			};

			if (unicode == 16rF703){
				//move_right.
				v.bounds = v.bounds.moveBy(1, 0);
				model.time = (v.bounds.left / parent.pixelPerSecond) - parent.timeOffset;
				this.setInfo;
				^this;
			};
			if (unicode == 16rF701){ 
				parent.model.moveObjectToTrack(model, model.track +1);
				this.setInfo;
				^this;				
			};

			if (unicode == 16rF702) {
				v.bounds = v.bounds.moveBy(-1, 0);
				model.time = (v.bounds.left / parent.pixelPerSecond) - parent.timeOffset;
				this.setInfo;
				^this;				
				};
			if (char === $]) {
				model.duration = parent.model.currentTime - model.time;
				this.resizeObjectDur;
				this.setInfo;				
				^this;				
				};
			if (char === $[) {
				model.time = parent.model.currentTime;
				parent.canvas.decorator.moveObjectToTime(v, parent.model.currentTime);
				this.setInfo;				
				^this;				
				};
			if (char === $?) {
				model.help;
				^this;				
				};
			if(char === $c){
				parent.model.copyMem = model.deepCopy;
				^this;				
			};
			if(char === $v){
				parent.model.putCopyMemOnCurrentTime;
				^this;			
			};
			if(char === $i){
				model.inspect;
				^this;			
			};
			parent.globalKeyDown( v, char, modifiers, unicode,keycode);

//		if(char==$i){
//			if(infoPanel.isNil){infoPanel = this.infoPanelClass.new(this)};
//			infoPanel.front;
//			^this;
//		};
			if(char==$e){
				this.showArgView;
				^this;
			};
	
			if(char==$z){
			//fixme
				this.undo;
				^this;
			};
					
			if(char==$a){
				//handled default by SCEnvelopeView ..
				if(editable){
					view.addValue
				}
			};
			if(char==$s){
	//			if(editable){
					this.splitAtCursor(v);
	//			};
				^this
			};			
			this.setInfo;
*/

		^k
	}

}