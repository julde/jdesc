/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

MultiTrackArgViewSelector{
	var window, <>parent, minBox, maxBox, listView, scalings, numberboxes, curvesPopUp,argbuttons;
	var <curve;
	var <> isGlobalArgView = false;
	
	*new{|mobject|
		^super.new.parent_(mobject).gui;
	}
	
	remove{
		if(window.notNil){ window.close};
	}
	
	gui{
		var controlnames, controlname,synthdescnames, synthdescindex ;
		
		synthdescnames = SynthDescLib.global.synthDescs.keys.asArray.sort;
		synthdescindex = synthdescnames.indexOf(parent.model.object.defName.asSymbol);
		window = SCWindow("show Args: " ++ parent.model.name, Rect(500, 600, 250, 440))
			.onClose_({window = nil; parent.argViewSelector_(nil)});
		window.view.decorator = FlowLayout(window.view.bounds);
		forBy(0, parent.model.args.size-1, 2, { arg i, j;
			controlnames = controlnames.add(parent.model.args[i]);
		});
		JtGui.makeSmallButton(window,parent.model.name,parent.model.name, width: 240)				.action_({|v| parent.hideEnvelope;})
				.resize_(2);
		JtGui.makePopUpMenu(window, 240)	
			.items_(synthdescnames)
			.action_({|v| v.value_(synthdescindex); 
				"setting the SynthDef is not supported yet".warn;})
			.value_(synthdescindex)
			.resize_(2);		
		window.view.decorator.nextLine;	
		JtGui.makeSmallText(window, 
			"select Env: enter: show, e: open", width: 240);
		window.view.decorator.nextLine;
		listView = JtGui.makeList(window, 240, 5)
			.items_([])
			.action_({|v| 
				var max, min, argval, indx;
				indx = parent.model.args.indexOf(v.items[v.value]);
				if(indx.isNil){^v};
				argval = parent.model.args[indx + 1];
				if(argval.isKindOf(Env)){
					max = argval.levels.maxItem;
					min = argval.levels.minItem;
					if(max == min){
						max = max + (0.5*max); 
						min = min - (0.5*min);
						if(max == 0){
							max = 1.0;
						};
					};
					curvesPopUp.value_(argval.shapeNumber(argval.curves));

				}{
//					max = argval * 2.0;
//					min = argval * 0.0;
//				
				};
				maxBox.value_(max); 
				minBox.value_(min); 
//				parent.showEnvelope(listView.items[listView.value], 
//									minBox.value, maxBox.value);


			})
			.enterKeyAction_({|v| 
				parent.showEnvelope(v.items[v.value], minBox.value, maxBox.value)})
			.resize_(2)
			.keyDownAction_{|view, char, modifiers, unicode,keycode|
					var w, ed;
					if(char === $e)
					{
						// make a basic editor
						parent.editEnvelope(view.items[view.value]);

					};
				view.defaultKeyDownAction(char, modifiers, unicode);
			};
		window.view.decorator.nextLine;
		JtGui.makeSmallText(window, "max scale: ");
		maxBox = JtGui.makeNumberBox(window, 0).action_({ 
			parent.showEnvelope(listView.items[listView.value], minBox.value, maxBox.value)});
		window.view.decorator.nextLine;
		JtGui.makeSmallText(window, "min scale: ");
		minBox = JtGui.makeNumberBox(window, 0).action_({ 
			parent.showEnvelope(listView.items[listView.value], minBox.value, maxBox.value)});
		window.view.decorator.nextLine;
		JtGui.makeSmallText(window, "curves: ");
		curvesPopUp = JtGui.makePopUpMenu(window)
			.items_([\step,  \linear, \exponential, \sine, \welch, "()", \squared, \cubed ])
			.value_(1)
			.action_({|v| 
				curve = v.items[v.value];
				parent.model.setCurves(listView.items[listView.value]*2, curve);
			});
		
			window.view.decorator.nextLine;
		JtGui.makeSmallText(window, 
			"set values :", resize:true);			 
			window.view.decorator.nextLine;
		this.createArgPopUp(window);
			
	}
	
	createArgPopUp{|win|
			var controlname, controlval, popupval, popupItems=[\Value, \Env, \Bus];
			forBy(0, parent.model.args.size-1, 2, {arg i, j;
				controlname = parent.model.args[i];
				controlval = parent.model.args[i+1];
//				this.debug( controlval);
//				this.debug(controlval.symbolIdentity);
				popupval = popupItems.indexOf(controlval.symbolIdentity);
				
				JtGui.makeSmallText(window, controlname);
				numberboxes = numberboxes.add(
						JtGui.makeNumberBox(window)
							.resize_(2)
							.action_({|v|
//								this.debug(argbuttons.indexOf(v).value);
								if(argbuttons[numberboxes.indexOf(v)].value == 0){
									parent.model
										.setNumberValueFor(numberboxes.indexOf(v)*2,
											 v.value);
								};
								if(argbuttons[numberboxes.indexOf(v)].value == 2){
									parent.model
										.setBusFor(numberboxes.indexOf(v)*2, v.value.asInt);
								};					
	
							});
					);
					
				argbuttons = argbuttons.add(
					JtGui.makePopUpMenu(window, 60)
					.items_(popupItems)
					.value_(popupval)
					.action_({|v| 
						var items, indexOfEnvList;
						/* Number as Argument */
						if(v.value == 0){
							numberboxes[argbuttons.indexOf(v)].enabled_(true);
							indexOfEnvList = listView.items.indexOf(
								parent.model.args[argbuttons.indexOf(v)*2].asSymbol);
							if(indexOfEnvList.notNil){
								items = listView.items.copy;
								items.removeAt(indexOfEnvList);
								listView.items = items;
							};
							//parent.model.setNumberValueFor(argbuttons.indexOf(v)*2);
	
						};
						/* Envelope as Argument */
						if(v.value == 1){
							listView.items = listView.items.add(
								parent.model.args[argbuttons.indexOf(v)*2].asSymbol
							);
							numberboxes[argbuttons.indexOf(v)].enabled_(false);
							parent.model.setEnvelopeFor(argbuttons.indexOf(v)*2, curve);
	
						};
						/* Bus as Argument */
						if(v.value == 2){
							numberboxes[argbuttons.indexOf(v)].enabled_(true);
							indexOfEnvList = listView.items.indexOf(
								parent.model.args[argbuttons.indexOf(v)*2].asSymbol);
							if(indexOfEnvList.notNil){
								items = listView.items.copy;
								items.removeAt(indexOfEnvList);
								listView.items = items;
							};
	
							//parent.model.setBusFor(argbuttons.indexOf(v)*2);					
						};
				}));
				if(controlval.isKindOf(Env)){
					numberboxes[j].enabled_(false);
					curvesPopUp.value_(
						parent.model.args[i+1].shapeNumber(parent.model.args[i+1].curves));
					listView.items = listView.items.add(parent.model.args[i].asSymbol);
								numberboxes[j].value_(parent.model.controls[j].defaultValue);						
				};
				
				if(controlval.isKindOf(Number)){
					numberboxes[j].value_(parent.model.args[i+1].round(0.001));
				};
				if(controlval.isKindOf(Bus)){
					numberboxes[j].value_(parent.model.args[i+1]);
				};
				window.view.decorator.nextLine;
			});
	
	}
	
	front{
		if(window.isNil){this.gui};
		listView.focus;
		window.front;
	}
	
	setParent{|pt|
		parent = pt;
	}
}

			
MultiTrackSoundFileArgView : MultiTrackArgViewSelector
{
	gui{
		var index;
		super.gui;
		index = parent.model.args.indexOf(\i_bufnum);
		if(index.notNil){
			numberboxes[index/2].enabled_(false);
			argbuttons[index/2].enabled_(false);
		}
	}
}

MultiTrackPbindArgView 
{

//parent is: PbindMultiTrackObjectGui

	var window, <>parent, minBox, maxBox, listView, scalings, numberboxes, curvesPopUp,argbuttons;
	var <curve;
	var <> isGlobalArgView = false;
	
	*new{|mobject|
		^super.new.parent_(mobject).gui;
	}
	
	remove{
		if(window.notNil){ window.close};
	}
	
	gui{
		var controlnames, controlname;
		window = SCWindow("show Args: " ++ parent.model.name, Rect(500, 600, 250, 400))
			.onClose_({window = nil; parent.argViewSelector_(nil)});
		window.view.decorator = FlowLayout(window.view.bounds);
		
		controlnames = parent.model.object.patternpairs.flatten.reject{|it| it.isKindOf(Symbol).not};
		
		JtGui.makeSmallButton(window,parent.model.name,parent.model.name, width: 240)				.action_({|v| parent.showName;})
				.resize_(2);
		window.view.decorator.nextLine;	
		JtGui.makeSmallText(window, 
			"show data for:");
		window.view.decorator.nextLine;
				
		listView = JtGui.makePopUpMenu(window, 240).items_(controlnames)
			.action_{|view| parent.setPbindData(parent.view, view.items[view.value])};
			
	}
	front{
		if(window.isNil){this.gui};
		listView.focus;
		window.front;
	}
	
	setParent{|pt|
		parent = pt;
	}	
	
}

//info not in use ...
//MultiTrackObjectInfo{
//	var window, <>parent, numberboxes;
//	
//	*new{|mobject|
//		^super.new.parent_(mobject.model).gui;
//	}
//	//FIXME: einstellungen kommen nnicht ducrh
//	gui{
//		var controlname;		
//		window = SCWindow("info: " ++ parent.object, Rect(500, 600, 200, 400))
//			.onClose_({window = nil});
//		window.view.decorator = FlowLayout(window.view.bounds);
//		forBy(0, parent.args.size-1, 2, { arg i, j;
//			controlname = parent.args[i];
//			JtGui.makeSmallText(window, controlname);
//			if(parent.args[i+1].isKindOf(SimpleNumber)){
//				numberboxes = numberboxes.add(
//					JtGui.makeNumberBox(window)
//						.value_(parent.args[i+1].round(0.001)).resize_(2)
//						.action_({|v|
//							parent.args.put(numberboxes.indexOf(v) * 2 + 1, v.value);
//						});
//				);
//			};
//			if(parent.args[i+1].isKindOf(EnvGen)){
////				numberboxes = numberboxes.add(
////					JtGui.makeNumberBox(window)
////						.value_(parent.args[i+1].round(0.001)).resize_(2)
////						.action_({|v|
////							parent.args.put(numberboxes.indexOf(v) * 2 + 1, v.value);
////							parent.args.postln;
////						});
////				);
//			};
//			window.view.decorator.nextLine;
//		});
//	}
//	
//
//	front{
//		if(window.isNil){this.gui};
//		window.front;
//	}
//}