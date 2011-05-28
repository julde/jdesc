// MultiTrackSoundFilePoolGui
MultiTrackSoundFilePool{
/*
fixme: should just collect all soundfiles
MultiTrackSoundFile pool
model is the MultiTracker

pool is a IdentityDictionary: keys from Names, means also we cant add a soundfile two times:

soundfiles might be copied to a certain directory (path)

usually saved using MultiTrackSoundFilePool:asSnapshot

*/
	
	var <keyCodeResponder, <items, < currentItem, <>model, <>path, <>rootPath;
	var player; // for preview
	
	*new{|model|
		^super.new.model_(model).init
	}
	
	init{
		items = IdentityDictionary.new;
		rootPath = "/"; //no root path on default
//		fileLoader = FileLoader(path);
		
	}
	
	hasGui{
		^true
	}
	
	currentItem_{|it| //only pooled MultiTrackSoundFile
		currentItem = it;
		this.changed(\item)
	}
	
	currentName_{|it| // 
		this.currentItem_(items[it.asSymbol]);	
	}
	
	currentName{
		^currentItem.name
	}
	
	gui{|parent|
		^MultiTrackSoundFilePoolGui(this, parent)
	}
	
	paths{
		^items.collect{|it| it.path}.values.asArray
	}
	
	names{
		^items.collect{|it| it.name}.values.asArray.sort;
	}
	
	add{|aSoundfile|
		if(aSoundfile.isNil){^this}; //this way we don't have to worry that we add textfiles accidently
//		this.debug(items[aSoundfile.name.asSymbol].notNil);
		if(items[aSoundfile.name.asSymbol].notNil){^this};
		items.put(aSoundfile.name.asSymbol, aSoundfile);
		SimpleController(aSoundfile).put(\name, {this.changed(\names)});
		this.changed(\add);
		this.changed(\names);
		
	}
	
	addAll{|arrayOfFiles|
		arrayOfFiles.do{|it|
			if(it.notNil){
				items.put(it.name.asSymbol, it);
			};
		};
		this.changed(\names);
	}
	
	at{|aname|
		if(aname.isKindOf(Symbol)){
			^items.at(aname.asSymbol)
		}{
			^items[items.collect{|it| it.name}.values.asArray.sort[aname]]; //not really efficient ;-|
		}
	}
	
	readDirectory{|path|
		var sfs;
		sfs = path.standardizePath.pathMatch.collect{|it| MultiTrackSoundFile.openReadAndClose(it)};
		this.addAll(sfs);
		currentItem = sfs.last; // don't speak
		this.changed(\addedDir, path);
	}
	
	readFiles{|files| // array of paths
		var sfs;
		sfs = files.collect{|it| MultiTrackSoundFile.openReadAndClose(it)};
		this.addAll(sfs);
		currentItem = sfs.last; // don't speak
		this.changed(\addedFiles, files);	
	}
	
	readDirectoryWithDialog{
		CocoaDialog.getPaths({ arg path;
				this.readDirectory(path.at(0).dirname ++ "/*");
		});	
	}
	
	readFilesWithDialog{
		CocoaDialog.getPaths({ arg path;
			this.readFiles(path)
		});	
	}
	
	removeAt{|name|
		items.put(name.asSymbol, nil);
		this.changed(\names);	
		this.changed(\removedFiles, name); //speak	
	}
	
	removeCurrentItem{
		var allNames, index, newCurrent;
		allNames = this.names;
		index = allNames.indexOf(currentItem.name.asSymbol);
		newCurrent = items[allNames[index-1].asSymbol];
		items.put(currentItem.name.asSymbol, nil);
		currentItem = newCurrent;
	}
	
	/*
	defined in another file
	asSnapshot
	*/
}
