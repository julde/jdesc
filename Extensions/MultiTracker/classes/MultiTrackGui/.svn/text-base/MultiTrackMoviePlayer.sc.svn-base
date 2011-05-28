/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/
MultiTrackMoviePlayer {
	var <>path, <win, <view, <multiTracker, <alwaysOnTop= false, <isPlaying = false;
	
	*new{|path|
		^super.new.path_(path).init;
	}
	
	init {
		win = SCWindow("movie",Rect(0,0, 320, 334));
		view = SCMovieView(win,Rect(0,0, 320, 334));
		view.path_(path);
		view.resizeWithMagnification(1);
		view.showControllerAndAdjustSize(false);
		win.front;
		win.onClose_{ if(multiTracker.notNil){multiTracker.removeDependant(this)}};
		win.view.keyDownAction_{|v,c,m,u,k| if(c===$t){this.alwaysOnTop_(alwaysOnTop.not)};
			if(c===$1){view.resizeWithMagnification(1)};
			if(c===$2){view.resizeWithMagnification(2)};
			if(c===$3){view.resizeWithMagnification(3)};
			if(c===$4){view.resizeWithMagnification(4)};

		};
	}
	
	alwaysOnTop_{|bool|
		alwaysOnTop = bool;
		win.alwaysOnTop = bool;
	}
	
	multiTracker_{|mt|
		multiTracker = mt;
		mt.addDependant(this);
	}
	
	update{|changer, what|
		if(what === \time){
			{
				if(isPlaying.not){
					view.setProperty(\setCurrentTime, changer.currentTime)
				};
			}.defer;
		};
		if(what === \play){
			{view.start; isPlaying=true;}.defer(changer.prepareLatency);
		};
		if(what === \stop){
			{view.stop; isPlaying=false;}.defer;
		}
	}
}