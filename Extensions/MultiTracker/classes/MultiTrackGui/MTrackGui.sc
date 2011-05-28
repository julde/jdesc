/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

//not quite a MTrackGui yet,  more a helper ...

MTrackGui {
	var <> parent, <>tracknum, <>row, model, view,<> dragAction; //parent is MultiTrackercanvas but dependant of MTrackGuiOptions
	
	
	*new{|parent, tracknum, row|
		^super.newCopyArgs(parent, tracknum, row).init
	}
	init{
		var canvas = parent.canvas, multitracker = parent.model;
		if(multitracker.respondsTo(\tracks)){
			model = multitracker.tracks[tracknum].guiOptions.addDependant(this);
		}{
			model = MTrackGuiOptions.new.background_(Color.white);
		};
		
		view = SCDragSink(canvas, canvas.bounds)
			.resize_(2).object_(tracknum.asString)
			.background_(model.background)
			.mouseOverAction_{|v, x, y| 
//					this.debug(multitracker);

				parent.currentMousePositionTime =parent.canvas.bounds.left+ parent.timeOffset + (x / multitracker.pixelPerSecond); 
				parent.drawCurrentDrag(x + v.bounds.left);
			}
			.mouseDownAction_{|v,x,y|
				multitracker.currentTime_(parent.timeOffset + (x / multitracker.pixelPerSecond));
			}
			.canReceiveDragHandler_{|v| v.focus;  parent.window.front;  true;}
			//todo move this action to a method 
			.action_({|v| 
				var tracknum, ptime;
				ptime = 	parent.currentMousePositionTime;
				canvas.decorator.rowObjects.do({|row, i|
					if(row.includes(v)){tracknum = parent.usableRows.indexOf(i)};
				});
				this.dragAction.(v.object, tracknum, ptime);
				v.object_(tracknum.asString);


			});
			this.dragAction_({|obj, tr, t| [obj, tr, t].postln;this.defaultDragAction(obj, tr, t)});
			
			
		}
		
		
		defaultDragAction{|object, tracknum, ptime|
				var multitracker = parent.model;

				if(object.isKindOf(Collection) and: (object.isKindOf(String).not)
					and: (object.isKindOf(Event).not)){
					multitracker.put(tracknum.asInt, ptime, 2, object[0],object[1]);
				}{
					if(object.isKindOf(Event) or: object.isKindOf(Symbol))
					{
						multitracker.put(tracknum.asInt, ptime, 2, object);
					}{				
					if(object.first == $!){
						multitracker.put(tracknum.asInt, ptime, 2, object);
					}{
						multitracker.putSoundFile(object, tracknum.asInt, ptime)
					}
					};
				};
			
		}
		
		onClose{
			this.remove;
		}
		
		//don't forget to close me..
		remove{
		
			model.removeDependant(this);
		}
		
		visible_{|flag|
			if(flag){
				parent.setRowSize(row,model.height);
			}{
				parent.setRowSize(row,0);			
			}
		}
		
		background_{|color|
			view.background = color;
		}
		
		update{|model, what, args|
			case(
			{what === \visible}, {this.visible_(args)},
			{what === \background}, {this.background_(args)}
		
			);

		
		}
	

}