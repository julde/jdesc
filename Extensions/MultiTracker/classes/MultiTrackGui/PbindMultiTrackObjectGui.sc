/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/
PbindMultiTrackObjectGui : MultiTrackObjectGui
{
	var currentSymbol, currentSpec, timesInView;
	gui{|win, multitrackergui, time|
		parent = multitrackergui;
		mode = \showName;
		view = SCEnvelopeView(win, Rect(0,0, 100, 200)).value_([[0.0],[0.0]])
			.select(0)
			.drawLines_(true)
			.drawRects_(true)
			.selectionColor_(Color.red)
			.setString(0,model.name)
			.fillColor_(Color.white)
			.strokeColor_(Color.blue(0.5, 0.0))
			.background_(Color.blue(0.5, 0.5))
			.setEditable(0, false)
			.thumbWidth_(3)
			.thumbHeight_(3)
			.action_({|v|
				var idx, pairs, ctime=0, valy;
				this.setInfoString(""); 
				pairs = model.object.patternpairs.flatten;
				idx = pairs.indexOf(currentSymbol);					if(idx.notNil){
					valy = v.value[1];
					pairs[idx+1].list[v.index] = currentSpec.map(valy[v.index]);					//FIXME optimize me! evtl set new time
					view.value_([timesInView, valy]);
				}
			})
			/*set the xposition of the view with control-mouse-down*/
			.metaAction_({|v|
				this.metaAction(v);
			})
			.keyDownAction_({|v, char, modifiers, unicode,keycode|
				this.keyDown(v, char, modifiers, unicode,keycode)
			});		
		this.moveObjectToTime(view, time);
		this.resizeObjectDur(view);
		currentView = view;
		lastEnvelope = \amp;
		if(model.showPianoRoll){
			this.setPbindData(view);
		}
		^view	
	
	}
	

	setPbindData{|view, sym|
	
		var win, sce, pat, patarr, durs, times, timespec, sympata, symspec, dursize;
		if(sym.isNil){^this};
		currentSymbol = sym;

		pat = model.object;
		patarr = pat.patternpairs;
		patarr.do{|item, i| 
			if(item === \dur){durs = patarr[i+1]};
			if(item === sym) {sympata = patarr[i+1]};
			};
		if(sympata.isNil){^this};
		sympata = sympata.list;
		symspec = [sympata.minItem, sympata.maxItem].asSpec;
		currentSpec = symspec;
		sympata = sympata.collect{|i| symspec.unmap(i)};
		//for pseq:
		durs = durs.list;
		times = Array.newClear(durs.size);
		times[0] = 0.0;
//		(durs.size-1).do{|i| 
//			var nextTime;
//			nextTime = times[i] + durs[i];
//			if(model.duration<nextTime){
//				times[i+1] = nextTime;
//			}{
//		}
//			dursize = i;

//};
				(durs.size-1).do{|i| times[i+1] = times[i] + durs[i]; dursize = i;};

//		durs.size-1.postln;
//		times = times.copyRange(0,dursize);
//		sympata = sympata.copyRange(0,dursize);
//		this.debug("FIXME: need to check duration in setPbindData: " ++ (model.duration - times.last).asString);
		//scale
//		this.debug(sympata);
		timespec = [0, times.last].asSpec;
		times.do{|time,i| times[i] = timespec.unmap(time)};
		view
			.setEditable(0, true)
			.thumbWidth_((view.bounds.width-2)/times.size)
			.thumbHeight_(2)
			.value_([times, sympata]);		
		timesInView = times;
	}
	
	resizeViewItems{|aview|
		if(timesInView.notNil){
			aview.thumbWidth_((view.bounds.width-2)/timesInView.size)
		};

	}
	update{|changer, what|
		if(what === \viewBounds){
			this.resizeViewItems(view);
		};
		super.update(changer, what);
	
	}
}