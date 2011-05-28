/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

MultiTrackGuiControl{

	//model is a MultiTracker
	var <>model;
	var <> currentObject;
	var infoText, <objectTimeSetter, <currentTimeSetter, infoDur, infoVal, ctrlTime, <trackNumSetter;
	
	
	*new{|model,parent|
		^super.new.model_(model).init(parent)
	}
	
	init{|parent| //canvas is now a CompositeView
		var canvas, x, tmp, onClose;
		canvas = parent.controlCanvas;
		onClose = canvas.onClose;

		SCStaticText(canvas, Rect(0,0, 100, 20)).string_("currentTime: " );
//		currentTimeSetter = SCStaticText(canvas, Rect(0,0, 100, 20)).string_(model.currentTime.round(0.001).asTimeString);

		currentTimeSetter = TimeSettingField(canvas, Rect(0,0, 160, 20))
						.value_(model.currentTime.round(0.001))
						.action_{|v| model.currentTime_(v.value)};
		
		ctrlTime = SimpleController(model).put(\time, {
		 		{currentTimeSetter.value_(model.currentTime.round(0.001))}.defer;
		 	});
		canvas.onClose_({onClose.value; ctrlTime.remove});

		infoText = SCStaticText(canvas, Rect(0,0, 100, 20)).string_("Object: time: " );
		x = 0;
		objectTimeSetter = TimeSettingField(canvas, Rect(x, 0, 160, 20))
			.action_({|v| currentObject.time_(v.value)});
		
		x = x+250;
		SCStaticText(canvas, Rect(x,0, 50, 20)).string_("duration: " );
		
		infoDur = SCNumberBox(canvas, Rect(x, 0, 30,20))
					.action_{|v| currentObject.duration_(v.value).changed(\duration)};
		infoDur.setProperty(\boxColor,Color.grey(0.5, 0.0));

		SCStaticText(canvas, Rect(x,0, 50, 20)).string_("track: " );
		
		trackNumSetter = SCLimitedNumberBox(canvas, Rect(x, 0, 30,20))
					.action_{|v| model.moveObjectToTrack(currentObject, v.value)};
		trackNumSetter.setProperty(\boxColor,Color.grey(0.5, 0.0));

		x = x + 32;
		SCStaticText(canvas, Rect(x,0, 50, 20)).string_("value: " );
		x = x+50;
		
		infoVal = SCNumberBox(canvas, Rect(x, 0, 60, 20))
					.action_{|v| currentObject.changed(\currentValue, v.value)};
		infoVal.setProperty(\boxColor,Color.grey(0.5, 0.0));

	}
	
	setObject{|obj, value=0|
//		var timeA;
//		if(currentObject !== obj){
//		currentObject.removeDependant(this);

			currentObject = obj;
//			timeA = obj.time.asTimeArray;
//	//		this.debug(timeA);
//			objectTimeSetter.do{|view,i| view.value_(timeA[i])};
			objectTimeSetter.value_(obj.time);
			infoDur.value_(obj.duration);
			trackNumSetter.value_(obj.track);
	//		currentObject.addDependant(this);
//		};
		infoVal.value_(value);
	}

}