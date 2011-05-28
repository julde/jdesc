/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/
SoundFileViewData {
	var <> block, <> startFrame, <>numFrames, <> numChannels, <> sampleRate, <> data, <> name, <>xZoom;
	
	*fromView{|sndview|
		^super.new.block_(64)
			.startFrame_(sndview.startFrame)
			.numFrames_(sndview.numFrames) //FIXME : does this make sense?
			.numChannels_(sndview.soundfile.numChannels)
			.sampleRate_(sndview.soundfile.sampleRate)
			.data_(sndview.data)
			.name_(sndview.soundfile.path)
			.xZoom_(sndview.xZoom.postln)
	}

}

SoundFileDataCollector{
	classvar <>all;
	classvar <> initialized = false;
	
	*init{
		all = IdentityDictionary.new;
		initialized = true;
	}
	
	*put{|sndview|
		var data;
		data = SoundFileViewData.fromView(sndview);
		all.put(data.name.asSymbol, data);
	}
	
	*at{|path|
		
		^all.at(path.asSymbol)
	}
	
	*set{|view, startFrame, numFrames|
		var data, resampleFrames, dataset, numFramesBlocked, startFrameBlocked;
		data = SoundFileDataCollector.at(view.soundfile.path);
//	this.debug([\first,startFrame, numFrames]);
		
		startFrame = startFrame ? 0;
		numFrames = numFrames ? view.soundfile.numFrames;
//	this.debug([startFrame, numFrames]);
		numFramesBlocked = (numFrames /data.block).asInt;
		startFrameBlocked = (startFrame / data.block).asInt;

		dataset = data.data.copyRange(startFrameBlocked.asInt, 
									numFramesBlocked + startFrameBlocked);
		view.elasticMode_(0);
//		dataset.postln;
		//FIXME data.startFrame.postln; something wrong with the startframe !
//		[data.block, 0, data.numChannels, data.sampleRate].postln;
		view.setData(dataset, data.block, 0, data.numChannels, data.sampleRate);
		resampleFrames = numFrames/data.block;
		view.elasticMode_(1);
		
		view.updateData;
		view.zoomAllOut;
//	this.debug([\startframe,startFrame ,\resampleframes, resampleFrames, \numframes, numFrames, \startFrameBlocked, startFrameBlocked, \datasize, data.data.size, \numFramesBlocked,numFramesBlocked]);
//		view.xZoom = data.xZoom;
		view.refresh;

	}
	
}