GrainPicCloud {

	var <>edgePoints, <boundingRect, <inside, <>settings, <>dur, <>page, pauseRatio, playRatio,
		lowPitchRatio, hiPitchRatio, <>high, <>low;
	
	
	*new { arg pointArr, pageSize, duration, hi, lo, conductor;
		^super.new.init(pointArr, pageSize, duration, hi, lo, conductor);
	}
	
	
	init { arg pointArr, pageSize, duration, hi, lo, conductor;
	
		var minX, minY, maxX, maxY, x, y, count;
	
		// Determine the minimums and maximums to find a bounding rectangle
	
	 	edgePoints = pointArr;
	  
	  	minX = maxX = pointArr.first.x;
	  	minY = maxY = pointArr.first.y;
	  
	  	edgePoints.do ({ arg next;
	  	
	  		x = next.x;
	  		y = next.y;
	  		
	  		if (x < minX, { minX = x; },
	  		
	  			{if ( x > maxX, { maxX = x;  }); });

	  		if (y < minY, { minY = y; },
	  		
	  			{if ( y > maxY, { maxY = y;  }); });
	  
	  	});
	  	
	  	boundingRect = Rect.fromPoints(Point(minX, minY), Point (maxX, maxY));
	  	
		// set some of our objects to external CVs
		
	  	dur = duration;
	  	page = pageSize;
	  	
	  	high = hi;
	  	low = lo;
	  	
	  	
	  	// get an array of ranges of Y values for every X
	  	
	  	inside = [];
	  	
	  	count = minX;
	  	{count <= maxX}. while({
	  	
	  		inside = inside.add([count, this.discoverYsForX(count)]);
	  		count = count + 1;
	  	});
	  	


	  	if (conductor.notNil, {
	  	
	  		settings = conductor;
	  		
	  	} , {
	  	
			// describe a default settings object
		  	settings = Conductor.make({ arg cond, db, pan, density, grainDur;
	  	
		  		var div, frameDur, lowFreq, highFreq, dur;
		  	
			  	db.sp(-20, -100, 20);
			  	pan.spec_(\pan);
			  	density.sp(10, 0.001, 100);
		  		grainDur.sp(0.01509, 0.001, 1);
	  		
	  			lowFreq = \rest;
	  		
	  		
	  			cond.pattern_(

	  				Pbind(
	  			
	  					\dur, Prout({
	  						inside.size.do({
	  							dur = this.getPixelDur;
	  							dur = dur + dur.sum3rand;
	  							dur.yield;
	  						});
	  					}), 
	  					
	  					
	  					[\highfreq, \lowfreq, \div], Prout({

	  						var arr, freqRange, loopArr, popped, left;
	  							
							div =1;
	  							
	  							
	  						left = boundingRect.left;
	  							
	  						inside.do({ arg boundArr, count;
	  							arr = this.getFreqs(left + count);
	  							highFreq = arr.first;
	  							lowFreq = arr.last;
	  							div = lowFreq.size;
	  							[highFreq, lowFreq, div].yield;
	  						});
	  					}),
	  					  				
	  				
	  					\grainDur, grainDur,
	  					\density, Pfunc({arg evt; density.value / evt[\div]}),
	  					\db,  db,
	  					\pan, pan,
	  					\legato, 2, //1.1,
	  				
	  				
	  					\instrument, \sineGrains
	  				
	  				)
	  			);
	  		});
	  	});
	  	
	  }
	  
	getPause {
	
		// length of puase from starting at the left hand side of the page 
		// and this object playing
	
		^(((boundingRect.left - page.left) / page.right) * dur.value);
	}
	  
	isInside { arg point;
	  
	  	// is this point within the cloud?
	  
	  	var x, range, y, hi, low, found;
	  
	  	if (boundingRect.containsPoint(point), {
	  	
			found = false;

	  		x = point.x;
	  		y = point.y;
	  		
	  		range = inside[x - boundingRect.left].last.copy;
	  		if ( range.notNil, {
	  		
	  			{range.size > 0}. while({
	  			
	  				hi = range.pop;
	  				low = range.pop;
	  				if ((hi >= y) && (low <= y), {
	  				
	  					found = true;
	  				});
	  			});
	  		});
	  		
	  		^found;
	  	});
	  	
	  	^false;
	}
	  
	getRanges { arg x;

		// what are the Y values for any given X
	
		var range;
	
		if (x >= boundingRect.left && x <= boundingRect.right, {
		
			range = inside[x - boundingRect.left].last.copy;
		});

		^range;
	}
	
	getFreqs { arg x;
	

		// what are the frequency limits for any given X?

		var range, freqRange, his, lows;
		
		range = this.getRanges(x);
		
		freqRange = high.value - low.value;
		// Y's are upside down from how you would expect
		range = page.bottom - range;
	  	range = ((range / page.bottom) * freqRange) + low.value;

		// make sure there's an even number
		if ((range. size % 2) != 0, {
			range = range ++ range.last;
		});
		
		// return an array of highs and an array of lows
		his = []; lows = [];
		{range.size > 0}.while({
			lows = lows.add(range.pop);
			his = his.add(range.pop);
		});

		^[his, lows];
		//^range;
	}
	
	makeHiLowDivArr {
	
		var x, arr, hilo, div;
		
		arr = [];
		x = boundingRect.left;
		
		{x <= boundingRect.right}. while ({
		
			hilo = this.getFreqs(x);	
			div = hilo.last.size;
			arr = arr.add([hilo.first, hilo.last, div]);
			
			x = x+1;
			
		});
		
		^arr;
	}
	
	getPixelDur {
	

		// how long does each X last?
		//(dur.value / (page.right - page.left)).postln;
		^(dur.value / (page.right - page.left));
		
	}
		
	  
	discoverYsForX { arg x;
	
		// 'private' method to find Ys for any X

		var returnVal, crossings, y, next, slope, yarr;
		
		returnVal = [];
		yarr = [];
	
		if (x < boundingRect.left || x > boundingRect.right, {

			 yarr = nil;
	  	}, {
	  	
		  		edgePoints.do({ arg point, index;
		  		
		  			// the next point
		  			next = edgePoints[(index + 1) % edgePoints.size];
	  		
	  		
	  				// is our x included in this line segment?
	  				//["discoverYsForX next.x", next.x, "x", x].postln;
	  				
	  				if (((point.x < x) && (next.x >= x)) ||
	  					((point.x > x) && (next.x <= x)), {
	  					
	  						// find the equation for the line described by the two edge points
	  						
	  						slope = (next.y - point.y) / (next.x - point.x);
	  						
	  						// line equation:  y - point.y = slope * (x - point.x)
	  						
	  						// if we give our x, find y on the line
	  						// then add it to the array
	  						
	  						yarr = yarr.add((slope * (x - point.x)) + point.y);
	  						
	  					});
	  				});
	  				
	  				// sort the array
	  				yarr = yarr.sort;
	  				// the y cordinates in the array can now be thought of as pairs.
	  				// the first one in the pair is the high part of a range
	  				// and the second one is the low part of a range
	  				
	  		});
	  		
	  		// if yar is nil, the given x falls outside of our bounding box
	  		// if the array is empty, then there are no points within the shape for the given x
	  		// otherwise, the array is pairs as described above
	  		
	  		^yarr;
	  		
	  }
	  
	  play {
	  
	  	// Tell our settings object, a Conductor, to play
	  	^settings.play;
	  }
	  
	  pause {
	  	// Tell our settings object to pause
	  	^settings.pause;
	  }
	  
	  stop {
	  	// Tell our settings object to stop
	  	^settings.stop;
	  }
	  
}

GrainPicCursor {

	var <startTime, >offset, >duration, >distance;

	*new { arg offset = 0, dur = 0, dist = 0;
	
		^super.new.init(offset, dur, dist);
	}

	init { arg offset = 0, dur = 0, dist = 0;
	
		startTime = Main.elapsedTime;
		duration = dur;
		distance = dist;
		this.offset = offset;
	}
	
	reset { arg offset, dur, dist;

		startTime = Main.elapsedTime;

		if (offset.notNil, {
			this.offset = offset;});
		if (dur.notNil, {
			duration = dur; });
		if (dist.notNil, {
			distance = dist;
		});

	}
		
	timeRunning {
	
		^ (Main.elapsedTime - startTime);
	}
	
	getX { arg dur, dist;
	
		var pixelDur, elapsed;
	
		if (dur.notNil, {
			duration = dur;
		});
		
		if (dist.notNil, {
			distance = dist;
		});
		
		if (((duration.isNil) || (distance.isNil)), {
			^0;
		});
		
		pixelDur = duration / distance;
		elapsed = (Main.elapsedTime - startTime);
		
		^ ((elapsed / pixelDur) + offset);
	}
	
}
		
GrainPicController {

	var <hiPitch, <lowPitch, <>playAction, <>stopAction, <duration, editSelect, >callMode;
	
	*new { arg playfunc, stopfunc;
		^super.new.init(playfunc);
	}
	
	
	init { arg playfunc, stopfunc;

		var playButton, stopButton, freqRange, vertOffset, horOffset, controlWindow;

		vertOffset = 20;
		horOffset = 20;
	
		playAction = playfunc;
		stopAction = stopfunc;
	
	
		controlWindow = SCWindow.new("Control Window");
		playButton = SCButton(controlWindow, Rect(20, vertOffset, 50, 20));
		stopButton = SCButton(controlWindow, Rect(90, vertOffset, 50, 20));
		editSelect = SCPopUpMenu(controlWindow, Rect(160, vertOffset, 120, 20));
		vertOffset = vertOffset + 40;
		hiPitch = ControlValue(\widefreq, 2000);
		lowPitch = ControlValue(\widefreq, 300);
	
		SCStaticText(controlWindow, Rect(20, vertOffset, 50, 20)).string_("freq");
		SCRangeSlider(controlWindow, Rect(70, vertOffset, 150, 20)).connect([lowPitch, hiPitch]);
		SCNumberBox(controlWindow, Rect (225, vertOffset, 50, 20)).connect(lowPitch);
		SCNumberBox(controlWindow, Rect(280, vertOffset, 50, 20)).connect(hiPitch);
		vertOffset = vertOffset + 40;
	
		duration = CV.new.sp(60, 1, 600, 0);
		SCStaticText(controlWindow, Rect(20, vertOffset, 50, 20)).string_("dur");
		SCSlider(controlWindow, Rect( 70, vertOffset, 150, 20)).connect(duration);
		SCNumberBox(controlWindow, Rect(225, vertOffset, 50, 20)).connect(duration);
		vertOffset  = vertOffset + 40;


		playButton.states = [["[ > ]"]]; //, ["[ || ]"]];
		stopButton.states = [["[ [] ]"]];
	
		playButton.action = { arg state;
			playAction.value(state);
		};
	
		stopButton.action = { arg state;
			stopAction.value(state); 
		};

	
		editSelect.items = ["Draw", "-", "Edit Single", "Select", "Cut (", "Copy (", "Paste ("];
		editSelect.background_(Color.white);
		editSelect.action = { arg sel;
	
			//["value", butt.value].postln;
	
			stopAction.value;

			if (sel.value == 0, { // draw
		
				"[Draw]".postln;
			
				if (callMode.notNil, {
					callMode.value (true, false);
				});
				//drawMode = true;
				//editMode = false;
			//	"true".postln;
			
			}, {
		
				if (callMode.notNil, {
					callMode.value (false);
				});
				//drawMode = false;
			//	"false".postln;
		
				if (sel.value == 2, { // edit
					"[Edit]".postln;
					if (callMode.notNil, {
						callMode.value (false, true);
					});

					//editMode = true;

				});
		
			});
		};
		
		controlWindow.bounds_(Rect(0, 0, 350, vertOffset));
	
		controlWindow.front;
	
	}
	
	setEditState { arg state;
	
		editSelect.value = state;
	}
	
	setEditMode { arg mode;
	
		// 0 = normal
		// 1 = something selected
		// 2 = something in clipboard
		
		if (mode == 0, {
			editSelect.items = ["Draw", "-", "Edit Single", "Select", "Cut (", "Copy (", 
							"Paste ("];
			editSelect.value = 2;
		}, {
			if (mode == 2, {
			
				editSelect.items = ["Draw", "-", "Edit Single", "Select", "Cut", "Copy", 
								"Paste ("];
				editSelect.value = 3;
			}, {
			
				editSelect.items = ["Draw", "-", "Edit Single", "Select", "Cut (", "Copy (", 
							"Paste"];

				
			});
		});
	}
	
}


GrainPicScribble {

	 var startPoint, points, rect, clouds, window, tablet, drawMode,
	 	>hiPitch, <>lowPitch, >duration, control, editMode, cursor,		selectRect, player, >defaultSettings, <playAction, <stopAction;


	*new { arg scribbleSize = Rect(40,40,1000,600), defaultCloudSettings;

			^super.new.init(scribbleSize, defaultCloudSettings);
	}
	
	
	init { arg scribbleSize = Rect(40,40,1000,600), defaultCloudSettings;
	
		rect = scribbleSize;
		defaultSettings = defaultCloudSettings;
		
		window = SCWindow.new("GrainPic", Rect.newSides(rect.left, rect.top, 
					rect.right + 80, rect.bottom + 80));

		tablet = SCTabletView.new(window, rect);


		window.view.background_(Color.white);
		tablet.background = Color.clear;

		points = [];
		window.front;
		clouds = [];
		drawMode = true;
		editMode = false;
		
	
		window.drawHook = {
	
			var cx;

			// This is where we tell it how to draw our lines.
			// See the Pen helpfile for more information
	
			Color.black.set;

			// draw the shape in progress
		
			this.drawPoints.value;
	
			// draw the finished clouds
	
			this.drawClouds.value;
		
			// draw cursor if not nil
		
			if (cursor.notNil && player.notNil, {
		
				cx = cursor.getX;
		
				Color.red.set;
				Pen.beginPath;
				Pen.moveTo(Point(cx, rect.top));
				Pen.lineTo(Point(cx - 7, rect.top -10));
				Pen.lineTo(Point(cx + 7, rect.top - 10));
				Pen.lineTo(Point(cx, rect.top));
			
				Pen.fill;
				Pen.stroke;
				//"cursor".postln;
				//window.refresh;
			});
		
			if(selectRect.notNil, {
		
				this.drawRect.value;
			});
			
	
		};
	
	
		tablet.mouseUpAction = { arg  view,x,y,pressure,tiltx,tilty,deviceID, 
							buttonNumber,clickCount;

			// called when we lift the mousebutton

			var shape, point, cl;

			if (drawMode, {

				["up", x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount].postln;
					tablet.background = Color(x / 300,y / 300,tiltx,pressure);
				//t.visible = false;
		
				// make sure it's a circle
				// or comment these out for lines
				shape = points.pop;
				shape = shape ++ startPoint;
				//points = points.add(shape);
				
				cl = GrainPicCloud.new(shape, rect, duration, 
										hiPitch, lowPitch);
										
				if (defaultSettings.notNil, {
					cl.settings = defaultSettings.value(cl);
				});
				clouds = clouds.add(cl);

			});
		
			window.refresh;
		};

		tablet.mouseDownAction = { arg  view,x,y,pressure,tiltx,tilty,deviceID, 
								buttonNumber,clickCount;

			// called when we click the mousebutton
			var activeCloud;

			startPoint = this.makePoint(x, y);

			if (drawMode, {

				points = points.add( [ startPoint ]) ;

			}, {
	
				// else, if editMode, select one of the clouds
				if (editMode, {
					clouds.do({arg shape;
				
						if( shape.isInside(startPoint), {
					
							activeCloud = shape;
						});
					});
					
					if (activeCloud.notNil, {
				
						activeCloud.settings.show;
					});
				});		
		
			});


			window.refresh;
		};


		tablet.action = { arg  view,x,y,pressure,tiltx,tilty,deviceID, buttonNumber,clickCount;


			// called while moving the mouse aorund with the button depressed.
	
			var point, shape;

			if (drawMode, {
	
				point = this.makePoint(x, y);
				shape = points.pop;
				shape = shape ++ point;
				points = points.add(shape);

			});

			window.refresh;		
	
		};
		
		
		control = GrainPicController.new;
		duration = control.duration;
		hiPitch = control.hiPitch;
		lowPitch = control.lowPitch;
		control.callMode = { arg dr, ed;
			if (dr.notNil, {
				drawMode = dr;});
			if (ed.notNil, {
				editMode = ed;});
		};


		this.playAction = {

			var playArr, spause;
		
			playArr = [];
	
			//if (state == 1, { // play
		
			"[Play]".postln;
			editMode = false;
			drawMode = false;
			clouds.do({ arg shape, num;
				[num, shape.settings.players.first.pattern].postln;
				spause = shape.getPause;
				(spause < 0).if ({ spause = 0; });
				
				playArr = playArr ++ spause ++
					shape.settings.players.first.pattern;
				//playArr = playArr ++ 0 ++ shape.settings.players.first.pattern;
			});
			["playArr", playArr].postln;
			player = Ptpar(playArr, 1);
			player.play;
				//Task.new({
			{	
				var dist, wait;
					
				dist = rect.right - rect.left;
				
					
				cursor = GrainPicCursor(rect.left, duration.value, dist);
				(duration.value / dist).wait;
					
				//(dist).do({
				{ if(cursor.notNil, {
					cursor.timeRunning <= duration.value;
					}, { false; });
				}. while({	
					if (cursor.notNil, {
									//cursor = cursor +1;
						window.refresh;
						(duration.value / dist).wait;
					});
						
				});
				cursor = nil;
				editMode = true;
				control.setEditMode(0);
				window.refresh;
			}.fork(AppClock);
						//}).start;
				
				//});			
		
		};
		
		
		this.stopAction = {
			if (player.notNil, { player.stop; "stopped".postln;});
			cursor = nil;
			window.refresh;
			editMode = true;
			player = nil;
		};

	}
	
	playAction_ { arg func;
	
		playAction = func;
		control.playAction = func;
	}
		
	stopAction_ { arg func;
		stopAction = func;		
		control.stopAction = func;
	}
		
	drawClouds  { arg color = Color.black;
	
		color.set;

		clouds.do ({ arg shape;

			Pen.beginPath;
			Pen.moveTo(shape.edgePoints.first);

			shape.edgePoints.do({ arg next;

				//Pen.lineTo(Point(next.x + 40, next.y + 40));
				Pen.lineTo(next);
			});
			
			//Pen.lineTo(shape.first);

			Pen.stroke;
			Pen.fill;
		});
		
	}

	drawPoints  {arg color = Color.black;

		color.set;
		points.do ({ arg shape;
		
			Pen.beginPath;
			Pen.moveTo(shape.first);
			
			shape.do({ arg next;

				//Pen.lineTo(Point(next.x + 40, next.y + 40));
				Pen.lineTo(next);
			});
			
				//Pen.lineTo(shape.first);

			Pen.stroke;
			Pen.fill;
		});
	}
	
	drawRect  { arg color = Color.black;

		color.set;
	}

	stop {
	
		stopAction.value;
	}
	
	play {
	
		playAction.value;
	
	}
	
	
	makePoint { arg x, y;
	
		// Offset by 40 because the SCTablet View is offset by 40
	  	// Otherwise, all the drawings will be relative to the edge of the window and not
  		// the edge of the tablet
  
  		^Point(x + rect.left, y + rect.top);
	}
	
	addCloud { arg cloud;
	
		clouds = clouds.add(cloud);
	}
	
	makeCloud { arg pointArr;
	
		var cl;
	
		// make sure it's a circle
		(pointArr.first != pointArr.last).if ({
		
			pointArr = pointArr.add(pointArr.first);
		});
		
		// make sure not to overwrite last set of points incase we're drawning
		points = points.addFirst(pointArr);
		
		cl = GrainPicCloud.new(pointArr, rect, duration, 
										hiPitch, lowPitch);
										
		if (defaultSettings.notNil, {
				cl.settings = defaultSettings.value(cl);
		});
		clouds = clouds.add(cl);

		window.refresh;
	}
	
	bounds {
	
		^rect;
	}

}