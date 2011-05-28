/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

MultiTrackTrigger{
	var <startTime, <stopTime, <> name;
	
	*new{|name, startTime, stopTime|
		^super.new.init(name, startTime, stopTime)
	}

	init{|na, startT, stopT|
		startTime = startT;
		stopTime = stopT;
		name = na;
	}
}


MultiTrackTriggerCollection {
	var <>parent, <> currentIndex = -1, < order, <>data;

	*new{|obj|
		^super.new.parent_(obj).order_(Order.new).data_(IdentityDictionary.new);
	}
	
	order_{|in|
		if(in.isKindOf(Order)){order = in; ^this};
//		if(in.isKindOf(Order)){order.array ^this};
	}
	
	put{|index, name, startTime, stopTime|
		data.put(name, MultiTrackTrigger(name, startTime,stopTime));
		order.put(index, name);
	}
	
	add{|name, startTime, stopTime|
		this.put(order.size, name, startTime, stopTime);
	}
	
	play{|which|
		var current;
		current = data.at(which);
		parent.play(current.startTime, current.stopTime);
	}
	
	playCurrent{
		var current;
		current = data.at(order[currentIndex]);	
		parent.play(current.startTime, current.stopTime);
	}
	
	playNext{
		var current;
		//parent.play;
		current = this.next;
		current ?? {"reached end".warn; ^this};
//		[current.startTime, current.stopTime].postln;
//		\play.postln;
		parent.play(current.startTime, current.stopTime);
	}
	
	playPrevious{
		var current;
		current = this.previous;
		parent.play(current.startTime, current.stopTime);
	}	
	
	next{
		this.increaseIndex;
		^data.at(order[currentIndex]);
	}
	
	previous{
		this.decreaseIndex;
		^data.at(order[currentIndex]);	
	}
	
	increaseIndex{
		currentIndex = (currentIndex+1).clip(0, order.size);
	}
	
	decreaseIndex{
		currentIndex = (currentIndex-1).clip(0, order.size);
	}

}