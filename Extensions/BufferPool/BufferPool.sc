BufferPool {
	classvar instanceActive = false, mainWindowBounds, <instance, <>editor; // a class variable to assure a singleton (only one instance at a time)
	classvar >buttonColor, >destructiveButtonColor, >backgroundColor, >listsColor, >buttonTextColor, >destructiveButtonTextColor, >textBoxColor;
	var <poolsDict, <buffersDict ;
	var <presetsDictArchive, <poolsDictArchive;
	var <poolList, <bufferList;
	var <window;
	var <bufNameList, <tempBuffArray;
	var <currentBuffersState = true; //if true you are not in a pool
	var currentPreset, presetDisplay; //the curently loaded preset (without not overwriten changes)
	var server;
	
	
	*new {arg preset, server; 
		if (instanceActive.not, {^super.new.initBufferPool}, 
							{"You can not have more than one instance of BufferPool".error});
	}
	
	*initClass{
		buttonColor = Color.new255(106,106,126);
		buttonTextColor = Color.white;
		destructiveButtonColor = Color.new255(106,106,126);
		destructiveButtonTextColor = Color.white;
		backgroundColor = Gradient(Color.new255(168, 183, 194),Color.new255(39, 43, 52),  \v, 1280);
		listsColor = Gradient(Color.white, Color.new255(168,173,194), \v, 1280); 
		textBoxColor = Color.new255(168,173,194);
		mainWindowBounds = Rect(1015, 685, 318, 321);
		
		//Create the preferences folder if it doesn't exist
		
		"mkdir preferences".unixCmd;
		
		//default editor
		
		editor = "QuickTime Player";
	}
	
	*bounds { arg rect; mainWindowBounds = rect;
	
	}
		
	bounds { ^window.bounds
	}
		
	initBufferPool {
		var addFilesFunction, removePoolButton;
		
		//server = Server.default; // TO DO: I should make it work with any server
		instance = this;
		//the main window
		
		window = SCWindow.new("BufferPool",mainWindowBounds, resizable: false).front
					.userCanClose_(false)
				    .onClose_({// free all the buffers
				    tempBuffArray.do{arg i; i.free}; 
				    tempBuffArray = nil; instanceActive = false;
				    buffersDict.do{arg i; i.do{arg i; i.free}}; 
				    });
		
		//set the color of the background 
		window.view.background_(backgroundColor);
			
		//the button to close the window	    
		SCButton.new(window,Rect(248, 23, 42, 28))
				.canFocus_(false)
				.states_([ ["Close", Color.black, Color.new255(168,173,194)]])
				.action_{|v| 
					this.popUpWarning(
						{
						window.close;
						}
						, "Are you sure? If you close the window the buffers will be freed!!"
					);
				};
		
		//make the instances true
		instanceActive = true;
		
		// create the dictionaries
		
		//presetsDict = Dictionary.new;
		poolsDict = Dictionary.new;
		buffersDict = Dictionary.new;
		
		
			//get the archived pools (create the file if it doesn't exist)
			if(File.exists("preferences/pools.bpl").not,
			{
			File("preferences/pools.bpl","w").write("");
			poolsDictArchive = Dictionary.new;
			poolsDictArchive.writeArchive("preferences/pools.bpl");
			 },
			{poolsDictArchive = Object.readArchive("preferences/pools.bpl")}
			);
			
			//do the same for the presets
			if(File.exists("preferences/presets.bpl").not,
			{
			File("preferences/presets.bpl","w").write("");
			presetsDictArchive = Dictionary.new;
			presetsDictArchive.writeArchive("preferences/presets.bpl");
			 },
			{presetsDictArchive = Object.readArchive("preferences/presets.bpl")}
			);
		
			// ******************** // Presets
			
		presetDisplay = SCStaticText.new(window,Rect(89, 10, 135, 17))
			.align_(\centre)
			.string = "Presets";
		
		SCButton.new(window,Rect(159, 30, 66, 18))
			.canFocus_ (false)
			.states_([ [ "new" ,buttonTextColor, buttonColor] ])
			.action_{|v| 
					var window1 = SCWindow.new("",Rect((window.bounds.left + v.bounds.left)-60, (window.bounds.top + window.bounds.height) - 70 , 136, 70), resizable: false).front;
					window1.view.background_(textBoxColor);
					//currentBuffersState = false;
					SCStaticText.new(window1, Rect(12, 4, 100, 20))
						.string_("Choose a name: ")
						.action_{|v| };
					SCTextField.new(window1,Rect(12, 30, 111, 22))
						.focus
						.action_{|v| //the action of save button creates and stores the buffers in a list and also archive the pool
						
						if(presetsDictArchive.at(v.string.asSymbol).notNil,
						{"This name is in use! You have to destroy the preset in order to recreate it!".error; window1.close;},
						{
						this.newPreset(v.string, poolList.items); 
						currentPreset = v.string;
						presetDisplay.string = "Preset:"+currentPreset;
						window1.close;
						//tempBuffArray = nil;
						//bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
						//bufferList.items_(bufNameList)
						}
						)
						};
			};
		SCButton.new(window,Rect(89, 30, 66, 18))
			.canFocus_ (false)
			.states_([ [ "load",buttonTextColor, buttonColor ] ])
			.action_{|v| 				
				var window1 = SCWindow.new("",Rect((window.bounds.left)-148, (window.bounds.top), 148, 252), resizable: false).front;
				window1.view.background_(backgroundColor);
				SCStaticText.new(window1,Rect(11, 7, 100, 20))
					.string_("Double click:");		
				SCListView.new(window1,Rect(8, 29, 132, 214))
					.items_(presetsDictArchive.keys.asArray)
					.background_(listsColor)
					.mouseDownAction_{|view, x, y, modifiers, buttonNumber, clickCount|
						        if(clickCount == 2,{
						        	window.onClose.value;
						        	poolsDict.clear;
						        	poolList.clear;
						        	bufferList.items_([]);
						        	presetsDictArchive[view.item].do
							      	  {arg item;
										this.newPoolFromArchive(item); 
									  }
						        }); 
						        if(clickCount == 2,{AppClock.sched(0.2, 
						        					{
						        					window1.close;
						        					tempBuffArray = nil;
						        					currentPreset = view.item;
						        					presetDisplay.string = "Preset:"+currentPreset;
												bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
												bufferList.items_(bufNameList)
												})
						        			}
						        );         
							};
			};
		SCButton.new(window,Rect(89, 52, 66, 18))
			.canFocus_ (false)
			.states_([ [ "destroy" , destructiveButtonTextColor, destructiveButtonColor] ])
			.action_{|v| var name; 
				if(currentPreset.notNil){
					this.popUpWarning(
						{
						name = currentPreset.asSymbol;
						presetsDictArchive.removeAt(name);
						presetsDictArchive.writeArchive("preferences/presets.bpl")
						}
						, "WARNING: This will delete the preset!! FOR EVER!!"
				);
				}
			
			};
		SCButton.new(window,Rect(159, 52, 66, 18))
			.canFocus_ (false)
			.states_([ [ "overwrite" , destructiveButtonTextColor, destructiveButtonColor] ])
			.action_{|v| var name, bufferPaths, archive;
				if(currentPreset.notNil){
					this.popUpWarning(
						{
						this.newPreset(currentPreset, poolList.items); 
						}
						, "Are you sure re malaka?"
					);
				}
			
			};	
			
			// ******************** // Pools
			
		poolList = 
		SCListView.new(window,Rect(10, 80, 135, 171))
			.canFocus_ (false)
			.background_(listsColor)
			.action_{|v| bufferList.items_(poolsDict.at(v.item.asSymbol).collect({arg i; i.basename}));
						currentBuffersState = false;
			};
			
		SCButton.new(window,Rect(10, 270, 66, 18))
			.canFocus_ (false)
			.states_([ [ "load" ,buttonTextColor, buttonColor] ])
			.action_{|v| 				
				var window1 = SCWindow.new("",Rect((window.bounds.left)-148, (window.bounds.top), 148, 252), resizable: false).front;
				window1.view.background_(backgroundColor);
				SCStaticText.new(window1,Rect(11, 7, 100, 20))
					.string_("Double click:");		
				SCListView.new(window1,Rect(8, 29, 132, 214))
					.items_(poolsDictArchive.keys.asArray)
					.background_(listsColor)
					.mouseDownAction_{|view, x, y, modifiers, buttonNumber, clickCount|
						        if(clickCount == 2){
						        if(poolsDict.at(view.item.asSymbol).isNil,
						        {
								this.newPoolFromArchive(view.item); 
								 //window1.close;
								tempBuffArray = nil;
								bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
								bufferList.items_(bufNameList);
								//bufNameList = poolsDictArchive.at(view.item.asSymbol).collect{arg i; i.basename.asSymbol};
								//bufferList.items_(bufNameList);
								});
						
						        };        
							};
			};
		removePoolButton = SCButton.new(window,Rect(80, 270, 66, 18))
			.canFocus_ (false)
			.states_([ [ "remove",buttonTextColor, buttonColor ] ])
			.action_{|v| var name;
					
					name = poolList.item.asSymbol;
					poolsDict.removeAt(name.asSymbol); //was: put(name, nil);
					buffersDict[name].do{arg i; i.free};
					buffersDict.removeAt(name.asSymbol);
					poolList.items_(poolsDict.keys.asArray);
					bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
					bufferList.items_(bufNameList);
					if(bufNameList.isNil, {currentBuffersState = true});
					
			};
		SCButton.new(window,Rect(10, 290, 66, 18))
			.canFocus_ (false)
			.states_([ ["destroy", destructiveButtonTextColor, destructiveButtonColor] ])
			.action_{|v| var name; 
				if(poolList.item.notNil){
					this.popUpWarning(
						{
						name = poolList.item.asSymbol; 
						removePoolButton.action.value;
						poolsDictArchive.removeAt(name);
						poolsDictArchive.writeArchive("preferences/pools.bpl")
						}
						, "WARNING: This will delete the pool FOR EVER (and ever...)!!"
					);
				}
			
			};
		SCButton.new(window,Rect(80, 290, 66, 18))
			.canFocus_ (false)
			.states_([ ["overwrite", destructiveButtonTextColor, destructiveButtonColor] ])
			.action_{|v| var name, bufferPaths, archive;
				if(poolList.item.notNil){
					this.popUpWarning(
						{
						name = poolList.item.asSymbol;
						bufferPaths = poolsDict.at(poolList.item.asSymbol);
						archive = Object.readArchive("preferences/pools.bpl");
		
						poolsDict.put(name, bufferPaths);
						poolsDictArchive.put(name, bufferPaths);
						archive.put(name, bufferPaths);
						archive.writeArchive("preferences/pools.bpl");
						poolList.items_(poolsDict.keys.asArray)
						}, "Are you sure? No turning back from this one!!"
					);
				}
			
			};
			
			// ******************** // Buffers  
			
		// ------------------ the function for loading files into a pool -------------------------
		
		addFilesFunction = { var bufs; 
			CocoaDialog.getPaths({ arg paths;
			// *** Put you buffer paths in a List *** \\
			if(currentBuffersState == true, //if you are not in a pool
			{ //do that
				if(tempBuffArray.isNil,
				{tempBuffArray = paths},			
				{tempBuffArray = tempBuffArray.addAll(paths)}
				);
				bufNameList = tempBuffArray.collect{arg i; i.basename.asSymbol};
				bufferList.items_(bufNameList)
			},//else do that
			{
			bufs = paths.collectAs({ arg item, i; Buffer.read(Server.default,item) }, List);
			poolsDict.put(poolList.item, poolsDict.at(poolList.item).addAll(paths));
			buffersDict.put(poolList.item, buffersDict.at(poolList.item).addAll(bufs));
			bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
			bufferList.items_(bufNameList);
			//currentBuffersState = false;
			}
			);
	
			},{
				"cancelled".postln;
			});

		};
		
		// ******************** // 
		// ******************** // 
			
		bufferList = 
		SCListView.new(window,Rect(170, 80, 135, 171))
			.canFocus_ (false)
			.background_(listsColor)
			.action_{|v| bufferList.items(v.value.postln;);
			};
		
		//Adding an action for the double click
		bufferList.mouseDownAction_{|view, x, y, modifiers, buttonNumber, clickCount|
       
                                //var currentDir;
                                //currentDir = (poolsDict[poolList.item.asSymbol][bufferList.value].escapeChar($ )).dirname;
                                if(clickCount == 2){ //on double click open the file with the editor of choice
                                //get the path and replace the  spaces with \\ (cause you need it for unixCmd)
                                (("open -a"+"'/Applications/" ++ editor ++ ".app"++"'" + (poolsDict[poolList.item.asSymbol][bufferList.value]).escapeChar($ ))).unixCmd; //Peak\ Pro\ 5.2.app //Wave\ Editor.app
                                };   
                               //"Wave Editor".escapeChar($ )
//                                if(clickCount == 1 && (modifiers == 131330)){ 
//                                //get the path and replace the  spaces with \\ (cause you need it for unixCmd)
//                                "A backup copy of the file has been created".postln;
//                                ("mkdir"+currentDir++"/bpl_backup").unixCmd;                                
//                                ("cp" + (poolsDict[poolList.item.asSymbol][bufferList.value].escapeChar($ ).postln)+
//                                currentDir++"/bpl_backup").unixCmd;
//
//                                };                                       
        	};

			
		SCButton.new(window,Rect(170, 290, 66, 18))
			.canFocus_ (false)
			.states_([ [ "add file(s)" ,buttonTextColor, buttonColor] ])
			.action_{|v| addFilesFunction.value}; // the addFile function is defined above
			
		SCButton.new(window,Rect(240, 290, 66, 18))
			.canFocus_ (false)
			.states_([ [ "remove",buttonTextColor, buttonColor ] ])
			.action_{|v| //remove and free the buffers form the array (if activated) and the names of them from the list
			
					(bufferList.item.notNil).if //if we have an item in the list view selected!
					({
					if(currentBuffersState == false , //we are in in a pool
					{//"in a pool".postln;
					
					buffersDict[poolList.item].do{arg item, index; if(item.path.basename.asSymbol == bufferList.item.asSymbol,
											 {buffersDict[poolList.item].removeAt(index);
											  poolsDict[poolList.item].removeAt(index);
											  item.free; }
											);}
					},
					{//"NOT in a pool".postln;
					tempBuffArray.removeAllSuchThat({arg item; item.basename.asSymbol == bufferList.item});
					}
					);
					
					bufferList.items.removeAt(bufferList.value);
					bufferList.items_(bufferList.items)
					
					}, {"you have to select a buffer to remove!".postln;});

			};
			
		SCButton.new(window,Rect(170, 270, 66, 18))
			.canFocus_ (false)
			.states_([ ["clear",buttonTextColor, buttonColor] ])
			.action_{|v| if(currentBuffersState == true, //if you are not in a pool
								{
								//tempBuffArray.do{arg item; item.free; };
								tempBuffArray = nil;
								bufNameList = nil;
								bufferList.items_();
								},
								{
								tempBuffArray = nil;
								bufNameList = nil;
								bufferList.items_();
								currentBuffersState = true;
								"If you want to free the buffers of a pool you have to remove the pool itself !!".debug("WARNING");
								}
						)
			};
			
			// *******************  //
											
		SCButton.new(window,Rect(240, 270, 66, 18))
			.canFocus_ (false)
			.states_([ ["save",buttonTextColor, buttonColor] ])
			.action_{|v| 
					var window1 = SCWindow.new("",Rect((window.bounds.left + v.bounds.left)-60, window.bounds.top , 136, 70), resizable: false).front;
					window1.view.background_(textBoxColor);
					SCStaticText.new(window1, Rect(12, 4, 100, 20))
						.string_("Choose a name: ")
						.action_{|v| };
					SCTextField.new(window1,Rect(12, 30, 111, 22))
						.focus
						.action_{|v| //the action of save button creates and stores the buffers in a list and also archive the pool
						
						if(poolsDict.at(v.string.asSymbol).notNil || (poolsDictArchive.at(v.string.asSymbol).notNil), //or
						{"This name is in use! You have to destroy the pool in order to recreate it!".error; window1.close;},
						{
						this.newPool(v.string, tempBuffArray); 
						window1.close;
						tempBuffArray = nil;
						bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
						bufferList.items_(bufNameList)
						}
						)
						};
			};
		
			// ******************** //
		
		SCStaticText.new(window,Rect(10, 60, 100, 20))
			.string_("Pools")
			.action_{|v| };
		SCStaticText.new(window,Rect(264, 60, 100, 20))
			.string_("Buffers")
			.action_{|v| };
			
			// *********************** //
		
		
		
	}
	
	newPool {arg name, bufferPaths; var buffers, archive;
	
			buffers = bufferPaths.collectAs({ arg item, i; Buffer.read(Server.default,item) }, List);
			name = name.asSymbol;
			archive = Object.readArchive("preferences/pools.bpl");
			
			buffersDict.put(name, buffers);
			poolsDict.put(name, bufferPaths);
			poolsDictArchive.put(name, bufferPaths);
			archive.put(name, bufferPaths);
			archive.writeArchive("preferences/pools.bpl");
			poolList.items_(poolsDict.keys.asArray);
			currentBuffersState = false;
	
	}
	
	newPreset {arg name, pools; var archive;
	
			name = name.asSymbol;
			archive = Object.readArchive("preferences/presets.bpl");
			presetsDictArchive.put(name, pools);
			archive.put(name, pools);
			archive.writeArchive("preferences/presets.bpl");
			//poolList.items_(pools.asArray);
	
	}

	newPoolFromArchive{arg name; var bufferPaths, buffers;
		
			name = name.asSymbol;
			bufferPaths = poolsDictArchive[name];
			buffers = bufferPaths.collectAs({ arg item, i; Buffer.read(Server.default,item) }, List);
			
			buffersDict.put(name, buffers);
			poolsDict.put(name, bufferPaths);
			poolList.items_(poolsDict.keys.asArray);
			currentBuffersState = false;
	
	
	}
	
	get {arg name;
	
	^buffersDict.at(name.asSymbol)
	
	}
	
	popUpWarning { 
			arg action, string;
			var dialog = SCWindow.new("",Rect((window.bounds.left)-100, (window.bounds.top)+120, 280, 112), border: false).front;
			
			if(string.isNil, {string = "Are you sure?"});
			SCStaticText.new(dialog,Rect(30, 10, 220, 50))
				.string_(string) 
				.align_(\left);
			SCButton.new(dialog,Rect(30, 70, 100, 20))
				.states_([ [ "Do it", destructiveButtonTextColor, destructiveButtonColor] ])
				.action_{|v| action.value; dialog.close};
			SCButton.new(dialog,Rect(150, 70, 100, 20))
				.states_([ [ "No thanks",buttonTextColor, buttonColor] ])
				.action_{|v| dialog.close};
		
	}
	
	*numOfActiveBuffers{arg server = Server.default;
	
	^(Buffer.serverCaches[Server.default].size -1).clip(0, inf)
	
	}
	
	currentPools{ poolList.items.do{arg item; ("BufferPool.instance.get(" ++ "'"++ item.asString++"'"++")").postln;};
	}
	
	
	loadPreset { arg name;

				window.onClose.value;
		        	poolsDict.clear;
		        	poolList.clear;
		        	bufferList.items_([]);
		        	presetsDictArchive[name.asSymbol].do
			      {arg item;this.newPoolFromArchive(item)};
	      	  
        			AppClock.sched(0.2, 
        			{
        			tempBuffArray = nil;
        			currentPreset = name;
        			presetDisplay.string = "Preset:"+currentPreset;
				bufNameList = poolsDict.at(poolList.item).collect{arg i; i.basename.asSymbol};
				bufferList.items_(bufNameList)
				});
	}
	
	loadPool {
	}
	


}

//used during testing

//a.window.view.background_ (Color.new255(167,152, 139)) //#A7988B
//a.bufferList.background_ (Color.new255(189, 182 ,130)) //BDB682
//a.poolList.background_ (Color.new255(189,182,130))
//a = BufferPool.new;
//a.presetsDict;
//a.poolsDict[\fesh];
//a.buffersDict;
//
//a.poolList;
//a.presetList;
//a.bufferList.item
//
//a.tempBuffArray.size;
//a.bufNameList;
//BufferPool.numOfActiveBuffers; //this is for the default server
//BufferPool.instance.currentPools;
//BufferPool.instance.poolsDict
//BufferPool.instance.buffersDict
//BufferPool.instance.bufNameList
//BufferPool.instance.currentBuffersState