MultiTrackTools {
	var <>model, <tools;

	*new{|model|
		^super.new.model_(model).init
	}
	
	init{
		tools = ();
		tools.put(\soundFiles, MultiTrackSoundFilePool(model, "sounds/*")); //just testing...
		tools.put(\selections, MultiTrackerSelectionManager(model));
	}
	
	at{|key|
		^tools[key]
	}

}
