/* 	(c) 2006 Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/
TrackLayout{

	var < bounds, < rowsize, <maxRows, <topmargin, <pixelPerSecond, <rows;
	var <>currentRow, visibleRows;
	var <> left;
	var < rowStarts;
	var < rowObjects;
	var <>currentTime, <timePixelOffset, <timeAtOffsetPoint, <pixelAtOffsetPoint;
	
	//all times are set by seconds
	
	*new{|bounds, rowsize, maxRows, topmargin, pixelPerSecond|
		^super.newCopyArgs(bounds, rowsize, maxRows, topmargin).init
	}
	
	init{
		rowsize =  rowsize ? 40;
		topmargin = topmargin ? 0;
		maxRows = maxRows ? 18;
		
		currentTime = currentTime ? 0;
		timePixelOffset = timePixelOffset ? 0; //ofset to calc time eg for tracksettings 
		timeAtOffsetPoint = timeAtOffsetPoint ? 0; //scroll
		
		pixelPerSecond = pixelPerSecond ? 100;
		pixelAtOffsetPoint = timeAtOffsetPoint * pixelPerSecond;
		
		currentRow = 0;
		visibleRows = bounds.height / rowsize;
		rowStarts = Array.newClear(maxRows);
		maxRows.do({arg i;
			rowStarts.put(i, [rowsize, topmargin + (rowsize * i)])
		});
		left = 0;
		rowObjects = Array.newClear(maxRows);
		rowObjects.size.do({|i|rowObjects.put(i,TrackRow.new)});
	}

	place{|view, addToSet= true|
		var height, width,vbounds, top, set;
		//FIXME: some views are not removed properly after changing the track:
		if(view.isClosed){rowObjects.at(currentRow).remove(view); ^this};
		vbounds = view.bounds;
		width = vbounds.width;
		height = rowStarts[currentRow.clip(0, maxRows-1)][0];
		top = rowStarts[currentRow.clip(0, maxRows-1)][1];
		view.bounds = Rect(vbounds.left, top, width, height);
		if(addToSet){
			set = rowObjects.at(currentRow).add(view);
		};
	}
	
	rowsize_{|which, size|
		this.setRowSize(which, size);
		"TrackLayout:rowsize_ depricated".inform;
	}
	
	setRowSize{|which, size|
		var oldsize, difsize, top;
		top = 0;
		oldsize = rowStarts[which][0];
		difsize = size - oldsize ;
		top = rowStarts[which][1] + difsize;
		rowStarts.do({arg ht, i;
			if(i >= which and: {i<(maxRows-1)}){
			top = ht[0] + top;
			rowStarts[i+1].put(1, top);
			}
		});
		//size.postln; \top.post;top.postln;
		rowStarts[which].put(0, size);
		rowObjects.do({arg set, i;
			currentRow = i;
			//"row : ".post; i.post; " : ".post; set.postln;
			set.do({arg view;
				this.place(view, false);
			})
		});
	}

	
	removeAll{
		rowObjects.copy.do{|it| if(it.noClosed){it.remove}};
		rowObjects = Array.newClear(maxRows);
	}
	
	//fix me ! MultiTrackerWindow:moveObjectToTrack
	moveObjectsArrayToRow{| obj, row, oldrow|
		var view, foundview;
		if(oldrow.isNil){
			rowObjects.do({arg set, i;
				if(view.isNil){
					obj.do({|o|
						if(o.respondsTo(\view)){
							view = set.findMatch(o.view);

						};
						
						if(view.notNil){
							foundview = view;
							set.remove(view);
						}
						});
					};
				
			});
			
		}{
			"notImpleneted yet".postln;
		//	view = rowObjects[oldrow].findMatch(obj);
//			rowObjects[oldrow].remove(view);
		};

		rowObjects[row].add(foundview);
		//this should be optimized
		rowObjects.do({arg set, i;
			currentRow = i;
			set.do({arg view;
				this.place(view, false);
			})
		});
		
	}

	moveObjectToRow{| obj, row, oldrow|
		var view;
		if(oldrow.isNil){
			rowObjects.do({arg set, i;
				if(view.isNil){
					view = set.findMatch(obj);
					};
				if(view.notNil){
					set.remove(view);
				}
			});
		}{
			view = rowObjects[oldrow].findMatch(obj);
			rowObjects[oldrow].remove(view);
		};

		rowObjects[row].add(view);
		//this should be optimized
		rowObjects.do({arg set, i;
			currentRow = i;
			set.do({arg view;
				this.place(view, false);
			})
		});
		
	}
	
	removeMatchingObjectFromRow{|row, array|
		array.do({|obj|
			rowObjects[row].remove(obj);
		});
	}
	
	removeMatchingObject{|array|
		maxRows.do({|i|
			array.do({|obj|
				rowObjects[i].remove(obj);
			});
		});
	}
	
	moveObjectToLeft{|obj, left|
		var vbounds;
		vbounds = obj.bounds;
		obj.bounds = Rect(left, vbounds.top, vbounds.width, vbounds.height);
	}
	
	moveObjectToLeftBy{|obj, left|
		var vbounds;
		vbounds = obj.bounds;
		obj.bounds = Rect(vbounds.left + left, vbounds.top, vbounds.width, vbounds.height);
	}
	
	moveObjectToTime{|obj, time|
		var left;
//		this.debug("obj: %, time: %, pixelPerSecond: %".format(obj, time, pixelPerSecond));
		left = (time * pixelPerSecond) + timePixelOffset + pixelAtOffsetPoint;
		this.moveObjectToLeft(obj, left);		
	}
	
	resizeObjectDur{|obj, dur|
		var vbounds, width;
		vbounds = obj.bounds;
		width = dur * pixelPerSecond;
		obj.bounds = Rect(vbounds.left, vbounds.top, width, vbounds.height);
	}
	
	pixelPerSecond_{|pps|
		var old_pps, time, dur;
		old_pps = pixelPerSecond;
		pixelPerSecond = pps;
		rowObjects.do({| set, i|
			if(set.isStatic.not){
				set.do({|view|
					if(view.respondsTo(\time)){time = view.time}{				time = view.bounds.left / old_pps};
					this.moveObjectToTime(view, time);
					if(view.respondsTo(\duration)){dur = view.duration}{				dur = view.bounds.width / old_pps};
					this.resizeObjectDur(view, dur);
			})
			}
		});
		
	}
	
	timeAddOffsetPoint_{|time|
		timeAtOffsetPoint = time;
//		timeAtOffsetPoint.postln;
		pixelAtOffsetPoint = timeAtOffsetPoint * pixelPerSecond;
		rowObjects.do({| set, i|
			if(set.isStatic.not){
				set.do({|view|
					if(view.respondsTo(\time)){time = view.time}{						time = (view.bounds.left / pixelPerSecond) + timeAtOffsetPoint;
						//view.bounds = view.bounds.moveBy(pixelAtOffsetPoint, 0);
					};
					this.moveObjectToTime(view, time);
					})
			}
		})
	}
	
//	pixelAtOffsetPoint_{|pixel|
//		var dif;
//		dif = pixelAtOffsetPoint + pixel;
//		pixelAtOffsetPoint = pixel;
//		timeAtOffsetPoint = pixelAtOffsetPoint / pixelPerSecond;
//		
//		rowObjects.do({| set, i|
//			set.do({|view|
//				this.moveObjectToLeftBy(view, dif);
//				})
//		})
//	}


}
