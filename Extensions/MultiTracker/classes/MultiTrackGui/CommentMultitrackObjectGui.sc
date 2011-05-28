/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

CommentMultiTrackObjectGui : MultiTrackObjectGui {
	var modus;
	
//	gui{|win, multitrackergui, time|
//		super.gui(win, multitrackergui, time)
//	}
	
	keyDown{| v, char, modifiers, unicode,keycode|
		[char, modifiers, unicode,keycode].postln;
		if(char==$w){
			this.toggleTextView;
			^this;
		};
//		if(char==$e){
//			if(argViewSelector.isNil){argViewSelector = model.argViewSelectorClass.new(this)};
//			argViewSelector.front;
//			^this;
//		};

		this.setInfoString("");
		super.keyDown(v, char, modifiers, unicode,keycode);
	}
	
	
	showTextView{|win, bounds|
		if(modus === \textView){^this};
		view.visible_(false);
		win.view.decorator.currentRow_(usableRows[model.track]);
		if(extraViews.notNil){
			if(extraViews.isClosed.not){
					extraViews.visible_(true);
					modus = \textView;
					this.update(extraViews.bounds, \viewBounds);
					extraViews.focus;
					currentView = extraViews;
					
					^this;
			}
		};
		extraViews = SCTextView(win, bounds).string_(model.object)
		.keyDownAction_({|v,c,m,u|
		if ((c == 3.asAscii) || (c == $\r) || (c == $\n), { // enter key
				model.object_(v.string);
				});

			if(u == 23 and: {m==262145 or: (m==262401)}){ //ctrl-w
				[c,m,u].postln;
				model.object_(v.string);
				this.toggleTextView;
				
			}
		})
		;
		modus = \textView;
		this.update(view.bounds, \viewBounds);
		extraViews.focus;
		currentView = extraViews;
		
	}
	
	remove{ arg removeView=false;
		model.removeDependant(this);
		if(removeView,{
			if(extraViews.notNil){extraViews.remove};
			view.remove(true);
			view = nil;		
		});
		if(argViewSelector.notNil){argViewSelector.remove};
	}
	
	hideTextView{
	//	extraViews.do{|v| v.visible_(false); v.remove};
		extraViews.visible_(false);
		view.bounds_(extraViews.bounds).visible_(true);

//		{extraViews.remove}.defer;
		currentView = view;
		currentView.keyDownAction_({|v,c,m,u, keycode| this.keyDown(v,c,m,u, keycode)});
		modus = \envView;
	//	extraViews = [];
	}
	
	toggleTextView{
		if(modus === \textView){this.hideTextView; ^this;
		}{
			this.showTextView(parent.window, view.bounds.deepCopy);
		}	
		
	}
}