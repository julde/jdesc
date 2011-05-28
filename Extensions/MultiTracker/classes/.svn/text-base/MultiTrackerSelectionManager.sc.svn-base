/*
this class takes care of object selections

*/

MultiTrackerSelectionManager {
	var <> model;
	var <>selectedObjects;
	var <>currentTrack = 0, <>currentObjectIndex = 0;

	*new{|model|
		^super.new.model_(model).init;
	}

	init{
		currentObjectIndex = {0} ! model.tracks.size;
	}

	selectAt{|track, index, reset=true|
		currentTrack = track;
		currentObjectIndex[track] = index.clip(0, model[currentTrack].data.asArray.size-1);
		this.selectObject(model[track][currentObjectIndex[track]], reset)
	}
	
	selectObject{|object, reset=true|
		if(reset){this.clearSelectedObjects};
		if(object.isNil){^this};
		if(object.isSelected){^this};
		selectedObjects = selectedObjects.add(object);
		object.select;	
		this.changed(\selectObject, object); // speak
	}
	
	unselectObject{|obj|
		selectedObjects.remove(obj);
		obj.isSelected = false;
	}	
	
	clearSelectedObjects{
		selectedObjects.do{|obj| obj.unselect;};
		selectedObjects = [];
	}
	
	//move relative in time
	moveSelectedObjects{|dt|
		selectedObjects.do{|obj| obj.moveRelative(dt)};
	}	
		
	//move relative to another track
	changeRelativeTrackForSelectedObjects{|trackincrease|
		selectedObjects.do{|obj| model.moveObjectToTrack(obj, obj.track + trackincrease)};
	}	
	
	nextObjectOnCurrentTrack{
		this.selectAt(currentTrack, currentObjectIndex[currentTrack]+1);
	}
	
	currentObjectOnTrack{|track|
		currentTrack = track;
		this.selectAt(currentTrack, currentObjectIndex[currentTrack]);
	}
	
	previousObjectOnCurrentTrack{
		this.selectAt(currentTrack, currentObjectIndex[currentTrack]-1);
	}	

	addNextObjectOnCurrentTrack{
		this.selectAt(currentTrack, currentObjectIndex[currentTrack]+1, false);
	}
	
	addPreviousObjectOnCurrentTrack{
		this.selectAt(currentTrack, currentObjectIndex[currentTrack]-1, false);
	}	
	
	hasGui{
		^false
	}
}