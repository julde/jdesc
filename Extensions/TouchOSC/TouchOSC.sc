TouchOSC {

	var names,elements,commands,responder,itemresponders,accelresponder,bus1,bus2,bus3,bus4,
	accbus,currentbus,type,page,guiwin,tabs,p1,p2,p3,p4,editwin,actioncode,actions,file,
	togglestates,pushstates,sliderKnob,knobColors,keysBlack,keysWhite,guiItems,choice,ms16Array1,
	ms16Array2,ms24Array1,ms24Array2,guiresponders,keymaps,currentserver,phoneIP;
	
	*new { arg server, interface, gui=0;
	^super.new.init(server, interface, gui);
	}
	
	init { arg server, interface, gui=0;
	
		server = server ? Server.default;
		if(server.serverRunning,{
		
		currentserver = server;
		actions = IdentityDictionary.new;
		
		ms16Array1 = Array.newClear(16);
		ms16Array2 = Array.newClear(16);
		ms24Array1 = Array.newClear(24);
		ms24Array2 = Array.newClear(24);
		
		names = ['xy','fader','toggle','push','multifader/','multifader1/','multifader2/','rotary',
		'multitoggle/1/','multitoggle/2/','multitoggle/3/','multitoggle/4/','multitoggle/5/',
		'multitoggle/6/','multitoggle/7/','multitoggle/8/'];

		togglestates = [[" ",Color.white,Color.new255(60,179,113)],
						[" ",Color.white,Color.new255(139,62,47)]];
		pushstates = [[" ",Color.white,Color.new255(255,236,139)],
						[" ",Color.white,Color.new255(238,0,0)]];
		sliderKnob = Color.new255(28,134,238);
		knobColors = [Color.new255(139,26,26), Color.new255(0,139,69), Color.new255(36,36,36), 
					Color.new255(216,191,216)];
		keysBlack = [[" ",Color.white,Color.black],[" ",Color.white,Color.new255(238,0,0)]];
		keysWhite = [[" ",Color.white,Color.white],[" ",Color.white,Color.new255(238,0,0)]];
		

		if (gui == 0, {
		responder = Array.fill(4, {arg i; OSCresponderNode(nil, '/' ++ (i+1), {arg t,r,msg; 
		this.layer(msg[0].asString[1],0); });});
		},{
		responder = Array.fill(4, {arg i; OSCresponderNode(nil, '/' ++ (i+1), {arg t,r,msg; 
		this.layer(msg[0].asString[1],1); {tabs.focus(i);}.defer; });}); 
		});
		
		responder.do({arg item, i; item.add;});
	
		if ( interface.asSymbol == 'Beatmachine', {
			choice = 0;
			bus1 = Array.fill(16,{Bus.control(server)});
			bus2 = Array.fill(112,{Bus.control(server)});
			bus3 = Array.fill(11,{Bus.control(server)});
			bus4 = [Bus.control(server,2), Array.fill(5,{Bus.control(server)})].flatten(1);
			elements = [[0,2,2,12],[0,0,0,0,16,0,0,0,16,16,16,16,16,16],[0,0,5,0,0,0,0,6],
				[1,0,5]];
			type = 0;
			if (gui == 1, {this.makeBeatGUI});
		});
	
		if ( interface.asSymbol == 'Keys', {
			choice = 1;
			bus1 = Array.fill(12,{Bus.control(server)});
			bus2 = Array.fill(12,{Bus.control(server)});
			bus3 = Array.fill(13,{Bus.control(server)});
			elements = [[0,0,0,12],[0,0,0,12],[0,5,3,4,0,0,0,1]];
			type = 1;
			if (gui == 1, {this.makeKeysGUI});
		});
	
		if ( interface.asSymbol == 'Mix 16', {
			choice = 2;
			bus1 = [Bus.control(server,2), Array.fill(12,{Bus.control(server)})].flatten(1);
			bus2 = Array.fill(16,{Bus.control(server)});
			bus3 = Array.fill(16,{Bus.control(server)});
			bus4 = Array.fill(48,{Bus.control(server)});
			elements = [[1,4,3,5],[0,8,8],[0,8,8],[0,0,0,0,0,24,24]];
			type = 0;
			if (gui == 1, {this.makeMix16GUI});
		});
	
		if ( interface.asSymbol == 'Mix 2', {
			choice = 3;
			bus1 = Array.fill(17,{Bus.control(server)});
			bus2 = Array.fill(32,{Bus.control(server)});
			bus3 = Array.fill(2,{Bus.control(server,2)});
			elements = [[0,3,4,4,0,0,0,6],[0,0,0,0,0,16,16],[2]];
			type = 0;
			if (gui == 1, {this.makeMix2GUI});
		});
	
		if ( interface.asSymbol == 'Simple', {
			choice = 4;
			bus1 = Array.fill(9,{Bus.control(server)});
			bus2 = Array.fill(20,{Bus.control(server)});
			bus3 = [Bus.control(server,2), Array.fill(4,{Bus.control(server)})].flatten(1);
			bus4 = Array.fill(68,{Bus.control(server)});
			elements = [[0,5,4],[0,0,4,16],[1,0,4],[0,0,4,0,0,0,0,0,8,8,8,8,8,8,8,8]];
			type = 0;
			if (gui == 1, {this.makeSimpleGUI});
		});
		
		if (gui == 1, {this.layer('1',1)},{this.layer('1',0)});
		
		},{"Warning: Server not running. Please boot the server and try again".postln});
	}
	
	layer { arg laynum, hasGUI=0;
		itemresponders.do({arg item, i; item.remove; });
		if (laynum.asSymbol == '1', { page = 0; currentbus = bus1;});
		if (laynum.asSymbol == '2', { page = 1; currentbus = bus2;});
		if (laynum.asSymbol == '3', { page = 2; currentbus = bus3;});
		if (laynum.asSymbol == '4', { page = 3; currentbus = bus4;});
		
		commands = Array.fill(elements[page].size, { arg i; 
			if (elements[page][i] != 0,{
				Array.fill(elements[page][i], {arg iter; 
					if (((elements[page][i] == 1) && (type == 0)),{
						names[i];
						},{
						names[i] ++ (iter+1)
					}); 
				});
			});
		}).flatten(1).reject({arg item, i; item.isNil});
		
		if (elements[page][0] == 0,{
			itemresponders = Array.fill(commands.size, { arg i; 
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; currentbus[i].set(msg[1]);}); });
			},{
			itemresponders = [Array.fill(elements[page][0],{ arg i; 
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i], 
			{arg t,r,msg; currentbus[i].setn([msg[1],msg[2]]);}); }),
			Array.fill(commands.size - elements[page][0], { arg i; 
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i+elements[page][0]], 
			{arg t,r,msg; currentbus[i+elements[page][0]].set(msg[1]);}); });].flatten(1);
			});
					
		itemresponders.do({arg item, i; item.add; });
		if (hasGUI == 1, {this.mapGUI;});
	}	
	
	accelerometer { arg on;
		if (on == 1,{
		accbus = Bus.control(currentserver,3); 
		accelresponder = OSCresponderNode(nil, '/accxyz',
			{arg t,r,msg; accbus.setn([msg[1],msg[2],msg[3]]); }).add;
		},{accelresponder.remove; accbus.free;});
	}
	
	accelX {
		^accbus.index;
	}
	
	accelY {
		^(accbus.index + 1);
	}

	accelZ {
		^(accbus.index + 2);
	}

	layer1 { arg item,numChannels=1;
		^bus1[item].index;
	}
	
	layer2 { arg item,numChannels=1;
		^bus2[item].index;
	}
	
	layer3 { arg item,numChannels=1;
		^bus3[item].index;
	}
	
	layer4 { arg item,numChannels=1;
		^bus4[item].index;
	}
	
	disconnect {
		itemresponders.do({arg item, i; item.remove; });
		guiresponders.do({arg item, i; item.remove; });
		responder.do({arg item, i; item.remove; });
		bus1.do({arg item,i; item.free;});
		bus2.do({arg item,i; item.free;});
		bus3.do({arg item,i; item.free;});
		bus4.do({arg item,i; item.free;});
		if (accbus.notNil, {accelresponder.remove; accbus.free;});
	}
	
	guiAssign {
		[p1,p2,p3,p4].do({arg thing, iter; thing.do({arg item, i; 
		item.action_({this.editWindow(thing,i,iter); }); }); });
	}
	
	guiStore { arg path;
		file = File(path,"w");
		file.write(actions.asSortedArray.asCompileString);
		file.close;
	}
	
	guiRecall { arg path;
		file = File(path,"r");
		file.contents.interpret.do({arg item, i; actions[item[0]] = item[1];});
		file.close;
	}
	
	setIP { arg netAddr;
		phoneIP = netAddr;
	}
	
	send { arg item, val1, val2;
		if (phoneIP.notNil, {
			if (val2.isNil, {
				phoneIP.sendMsg('/' ++ (page+1) ++ '/' ++ commands[item], val1);
			},{
				phoneIP.sendMsg('/' ++ (page+1) ++ '/' ++ commands[item], val1, val2);
			});
		},{
			"Please use .setIP to set the IP address of your iPhone/iPod Touch".postln;
		});
	}
	
	sendLed { arg num, val;
		if (phoneIP.notNil, {
			phoneIP.sendMsg('/2/led' ++ num, val);
		},{
			"Please use .setIP to set the IP address of your iPhone/iPod Touch".postln;
		});
	}
	
	mainGUI { arg layout,numtabs;
		if (layout == 0,{
			guiwin = GUI.window.new("TouchOSC", Rect(400,300,410,285));
		},{
			guiwin = GUI.window.new("TouchOSC", Rect(400,300,285,410));
		});
		guiwin.view.background_(Color.black);
		guiwin.front;
		tabs = TabbedView(guiwin,guiwin.view.bounds,Array.series(numtabs,1,1));
		}
	
	makeBeatGUI {
		this.mainGUI(0,4);
		
		p1 = [Array.fill(2,{arg i; GUI.slider.new(tabs.views[0],
		Rect(10+(i*50),10,40,200)).knobColor_(sliderKnob)}), Array.fill(2,{arg i; 
		GUI.button.new(tabs.views[0], Rect(10+(i*50),220,40,40)).states_(togglestates); }), 
		Array.fill(3,{arg i; Array.fill(3, {arg iter; GUI.button.new(tabs.views[0],
		Rect(160+(iter*((230/3)+5)),10+((2-i)*((240/3)+5)),
		230/3,240/3)).states_(pushstates); }); }), Array.fill(3,{arg i; 
		GUI.button.new(tabs.views[0],Rect( 110,10+( (2-i) * (( 240/3 ) + 5 )),
		40,240/3)).states_(pushstates); })].flat;
		
		p2 = [ GUI.multiSliderView.new( tabs.views[1], Rect(15,165,381,95)).indexThumbSize_(21).gap_(3).isFilled_(true).colors_(sliderKnob,sliderKnob).value_(Array.fill(16,{0})), 
		Array.fill(6,{arg iter;Array.fill(16,{arg i; GUI.button.new(tabs.views[1],
		Rect(10+(i*((315/16)+5)),10+((5-iter)*((315/16)+5)),315/16, 
		315/16 )).states_(pushstates); });})].flat;
		
		p3 = [Array.fill(5,{arg i; GUI.button.new(tabs.views[2],Rect(358,10+((4-i)*52),
		42,42)).states_(togglestates);}),Array.fill(2,{arg iter; 
		Array.fill(3,{arg i; Knob.new(tabs.views[2],Rect(10+(i*115),20+(iter*125),
		110,110)).color_(knobColors); });})].flat;
		
		p4 = [GUI.slider2D.new(tabs.views[3],Rect(10,10,338,250)).knobColor_(sliderKnob), 
		Array.fill(5,{arg i; GUI.button.new(tabs.views[3],Rect(358,10+((4-i)*52),
		42,42)).states_(togglestates); })].flat;
		
		this.setActions;
	}
	
	makeKeysGUI {
		this.mainGUI(0,3);
		
		p1 = [Array.fill(7,{arg i; GUI.button.new(tabs.views[0],Rect(10+(i*((360/7)+5)),
		10,360/7,250)).states_(keysWhite);}),Array.fill(5,{arg i; 
		GUI.button.new(tabs.views[0],Rect([40,105,205,270,330][i],
		10,40,175)).states_(keysBlack); })].flat;
		
		p2 = [Array.fill(7,{arg i; GUI.button.new(tabs.views[1],Rect(10+(i*((360/7)+5)),
		10,360/7,250)).states_(keysBlack);}),Array.fill(5,{arg i; 		GUI.button.new(tabs.views[1],Rect([40,105,205,270,330][i],
		10,40,175)).states_(keysWhite);})].flat;

		p3 = [Array.fill(2,{arg i; GUI.slider.new(tabs.views[2],Rect(10+(i*55),
		10,40,250)).knobColor_(sliderKnob).value_(0.5)}),Array.fill(3,{arg i; 
		GUI.slider.new(tabs.views[2],Rect(120+(i*55),
		10,40,200)).knobColor_(sliderKnob).value_(1)}),Array.fill(3,{arg i; 
		GUI.button.new(tabs.views[2],Rect(120+(i*55),
		220,40,40)).states_(togglestates); }),Array.fill(2,{arg iter;Array.fill(2,{ arg 
		i;GUI.button.new(tabs.views[2],Rect(295+(i*50),155+((1-iter)*50),
		40,40)).states_(pushstates);});}),Knob.new(tabs.views[2], 
		Rect(280,10,120,120)).color_(knobColors).centered_(true).value_(0.5)].flat;
		
		this.setActions;
	}
	
	makeMix16GUI {
		this.mainGUI(0,4);
		p1 = [GUI.slider2D.new(tabs.views[0],Rect(160,10,240,150)).knobColor_(sliderKnob),
		Array.fill(3,{arg i; GUI.slider.new(tabs.views[0],Rect(10+(i*50),
		10,40,200)).knobColor_(sliderKnob)}), GUI.slider.new(tabs.views[0], 
		Rect(160,170,240,40)).knobColor_(sliderKnob), Array.fill(3,{arg i; 
		GUI.button.new(tabs.views[0],Rect(10+(i*50),220,40,40)).states_(togglestates); }), 
		Array.fill(5,{arg i; GUI.button.new(tabs.views[0],
		Rect(160+(i*50),220,40,40)).states_(pushstates); })].flat;

		p2 = [Array.fill(8,{arg i; GUI.slider.new(tabs.views[1],Rect(10+(i*50),
		10,40,200)).knobColor_(sliderKnob); }),Array.fill(8,{arg i; 
		GUI.button.new(tabs.views[1],Rect(10+(i*50),220,40,40)).states_(togglestates); })].flat;

		p3 = [Array.fill(8,{arg i; GUI.slider.new(tabs.views[2],Rect(10+(i*50),
		10,40,200)).knobColor_(sliderKnob); }),Array.fill(8,{arg i; GUI.button.new(tabs.views[2], 
		Rect(10+(i*50),220,40,40)).states_(togglestates); })].flat;

		p4 = Array.fill(2,{arg i; GUI.multiSliderView.new(tabs.views[3], Rect(10,10+(i*130),
		390,120)).indexThumbSize_(13.25).gap_(3).isFilled_(true).colors_(sliderKnob,sliderKnob).value_( Array.fill(24,{0.5})); });
		
		this.setActions;
	}
	
	makeMix2GUI {
		this.mainGUI(1,3);
		
		p1 = [Array.fill(2,{arg i; GUI.slider.new(tabs.views[0],Rect(100+(i*45),
		100,40,220)).knobColor_(sliderKnob); }), GUI.slider.new(tabs.views[0], 
		Rect(30,330,225,40)).knobColor_(sliderKnob).value_(0.5), Array.fill(2,{arg 
		iter; Array.fill(2,{arg i; GUI.button.new(tabs.views[0], Rect(100+(iter*45),10+(i*45),
		40,40)).states_(togglestates); });}), Array.fill(2,{arg i; 
		GUI.button.new(tabs.views[0], Rect(10+(i*45),280,40,40)).states_(pushstates); }), 
		Array.fill(2,{arg i; GUI.button.new(tabs.views[0], Rect(190+(i*45),
		280,40,40)).states_(pushstates); }), Knob.new(tabs.views[0], 
		Rect(10,10,85,85)).color_(knobColors).centered_(true).value_(0.5), Array.fill(2,{arg i; 
		Knob.new(tabs.views[0], Rect(10,100+(i*90),85,85)).color_(knobColors); }), 
		Knob.new(tabs.views[0], Rect(190,10,85,85)).color_(knobColors).centered_(true).value_(0.5), 
		Array.fill(2,{arg i; Knob.new(tabs.views[0], 
		Rect(190,100+(i*90),85,85)).color_(knobColors); })].flat;

		p2 = Array.fill(2,{arg i; GUI.multiSliderView.new(tabs.views[1], Rect(10,10+(i*195),265,180)).indexThumbSize_(13.75).gap_(3).isFilled_(true).colors_(sliderKnob,sliderKnob).value_( Array.fill(16,{0.5})); });

		p3 = Array.fill(2,{arg i; GUI.slider2D.new(tabs.views[2],
		Rect(10,10+(i*195),265,180)).knobColor_(sliderKnob).y_(1); });
		
		this.setActions;
	}
	
	makeSimpleGUI {
		this.mainGUI(1,4);
		
		p1 = [Array.fill(4,{arg i; GUI.slider.new(tabs.views[0],Rect(10+(i*68.75),
		60,58.75,260)).knobColor_(sliderKnob); }), GUI.slider.new(tabs.views[0], 
		Rect(10,10,265,40)).knobColor_(sliderKnob), Array.fill(4,{arg i; 
		GUI.button.new(tabs.views[0], Rect(10+(i*68.75),
		330,58.75,55)).states_(togglestates); })].flat;

		p2 = [Array.fill(4,{arg i; GUI.button.new(tabs.views[1],Rect(10+(i*68.75),
		330,58.75,55)).states_(togglestates); }),Array.fill(4,{arg iter;Array.fill(4,{arg i; 
		GUI.button.new(tabs.views[1],Rect(10+(i*68.75),10+(iter*68.75),
		58.75,58.75)).states_(pushstates); });})].flat;

		p3 = [GUI.slider2D.new(tabs.views[2], Rect(10,10,265,265)).knobColor_(sliderKnob).y_(1), 
		Array.fill(4,{arg i; GUI.button.new(tabs.views[2], Rect(10+(i*68.75),
		330,58.75,55)).states_(togglestates); })].flat;

		p4 = [Array.fill(4,{arg i; GUI.button.new(tabs.views[3],Rect(10+(i*68.75),
		330,58.75,55)).states_(togglestates); }),Array.fill(8, { arg iter; Array.fill(8,{arg i; 
		GUI.button.new(tabs.views[3],Rect(10+(iter*33.75),10+(i*33.75),
		28.75,28.75)).states_(pushstates); });})].flat;
		
		this.setActions;
	}
	
	editWindow { arg pagenum,item,realpage;
		pagenum[item].action_({ });
		editwin = GUI.window.new("Assign Action",Rect(400,300,400,200));
		editwin.view.background = Color.black;
		editwin.front;
		actioncode = GUI.textView.new(editwin,Rect(10,10,380,150));
		actioncode.string_(actions[(realpage.asSymbol ++ "_" ++ item.asSymbol).asSymbol]);
		GUI.button.new(editwin, Rect(150,165,100,30)).states_([["Assign", Color.white, 
		Color.grey(0.1)]]).action_({ actions[(realpage.asSymbol ++ "_" ++ item.asSymbol).asSymbol] 
		= actioncode.string;
		pagenum[item].action_({arg val; actions[(realpage.asSymbol ++ "_" ++ 
		item.asSymbol).asSymbol].interpret.value(val);}); 
		editwin.close;});
	}
	
	mapGUI {
		
		guiresponders.do({arg item, i; item.remove; });
		 
		if (((choice == 1) && (page == 2)),{
			//keyspage3
			guiresponders = Array.fill(commands.size, { arg i;
			if (((i>1) && (i<5)),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].valueAction_(1 - msg[1])}.defer; });
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].valueAction_(msg[1])}.defer; }); });
			}); 
			
		},{
		if (((choice == 1) && (page < 2)),{
			//keyspage1and2
			keymaps = [0,7,1,8,2,3,9,4,10,5,11,6];
			guiresponders = Array.fill(commands.size, { arg i;
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][keymaps[i]].valueAction_(msg[1])}.defer; });
			});
		},{
		if (((choice == 0) && (page == 1)),{
			//beatmachinepage2
			guiresponders = Array.fill(commands.size, { arg i;
			if ( (i < 16),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; ms16Array1.put(i,msg[1]); 
				{guiItems[1][0].valueAction_(ms16Array1)}.defer;});
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i-15].valueAction_(msg[1])}.defer; }); });
			}); 
		},{
		if (((choice == 2) && (page == 3)),{
			//mix16page4
			guiresponders = Array.fill(commands.size, { arg i;
			if ( (i<24),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; ms24Array1.put(i,msg[1]); 
				{guiItems[3][0].valueAction_(ms24Array1)}.defer;});
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; ms24Array2.put((i-24),msg[1]); 
				{guiItems[3][1].valueAction_(ms24Array2)}.defer;});});
			});
		},{
		if (((choice == 3) && (page == 1)),{
			//mix2page2
			guiresponders = Array.fill(commands.size, { arg i;
			if ( (i<16),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; ms16Array1.put(i,msg[1]); 
				{guiItems[1][0].valueAction_(ms16Array1)}.defer;});
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; ms16Array2.put((i-16),msg[1]); 
				{guiItems[1][1].valueAction_(ms16Array2)}.defer;}); });
			});
		},{
		if (((choice == 3) && (page == 2)),{
			//mix2page3
			guiresponders = Array.fill(commands.size, { arg i;
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].setXYActive(msg[1],(1-msg[2]))}.defer; });
			});
		},{
		if (((choice == 4) && (page == 2)),{
			//simplepage3
			guiresponders = Array.fill(commands.size, { arg i;
			if ((i == 0),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].setXYActive(msg[1],(1-msg[2]))}.defer; });
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].valueAction_(msg[1])}.defer; }); });
			});
		},{
			//allothers
			guiresponders = Array.fill(commands.size, { arg i;
			if(((elements[page][0] == 1) && (i == 0)),{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].setXYActive(msg[2],msg[1])}.defer; });
			},{
			OSCresponderNode(nil, '/' ++ (page+1) ++ '/' ++ commands[i],
			{arg t,r,msg; {guiItems[page][i].valueAction_(msg[1])}.defer; }); });
			});
		});});});});});});});	
		guiresponders.do({arg item, i; item.add; });
	}
	
	setActions {
		guiItems = [p1,p2,p3,p4];
		guiItems.do({arg thing, iter; thing.do({arg item, i; 
		actions[(iter.asSymbol ++ "_" ++ i.asSymbol).asSymbol] = "{}";
		item.action_({ arg val; actions[(iter.asSymbol ++ "_" ++ 
		i.asSymbol).asSymbol].interpret.value(val); }); }); });
	}
	
}