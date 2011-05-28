
TelemusicaGui{
	
	var <server, <address, <bCaster;
	var win,values;
	var relay;
	
    *new{ |svrArr, addrArr|
    		^super.new.init(svrArr, addrArr);    
	}

	init{
		
		relay=(
		on:false,
		address:List.new
		);
		
		values =(\first:true);
    		server = IdentityDictionary();
    		address = IdentityDictionary();
    		
    		
    		win=Window.new(" Tele-Servers ",Rect(0,800,510,4),false).front;
    		win.view.decorator = FlowLayout(win.view.bounds,gap:2@4);
    		
	}


	addUser{|array,dir, port= 57110,loc="Amsterdam"| 
		var addr,svr,name,look=false;
		if(size(array) > 0){
		
		win.setInnerExtent(win.bounds.width,win.bounds.height+(22*size(array)));

		array.do({|item|
			
			
			name=item[0];
			dir=item[1];
			if( size(item) > 2 ){port=item[2]}{port=57110};

			addr = NetAddr(dir, port);
		
			address.put(name,addr);
		
			svr = Server(name.asSymbol, addr);
			server.put(name,svr);
		
			svr.addLongWindow(win);
			win.view.decorator.nextLine;	
			
					if(values[\first]){
				values.put(\name,name.asString);
				values.put(\location,loc.asString);
				values[\first]=false;
				};
		
			});
		
		}
		{
		
		name=array;
		
		addr = NetAddr(dir, port);
		address.put(name,addr);
		
		svr = Server(name.asSymbol, addr);
		server.put(name,svr);
		
		win.setInnerExtent(win.bounds.width,win.bounds.height+22);
		svr.addLongWindow(win);
		win.view.decorator.nextLine;
			
		if(values[\first]){
		values.put(\name,name.asString);
		values.put(\location,loc.asString);
		values[\first]=false;
		};
		
		}
			
	}
	
	addRelayUser{|dir,port|
		relay[\on]=true;
		relay[\address].add(NetAddr.new(dir,port));
		this.addRelayGui(dir,port);
	}
	
	relayOff{
	relay[\on]=false;
	}
	
	relayOn{
	relay[\on]=true;
	}
		
	startBroadcast{
	var array=address.asArray;
	bCaster = BroadcastServer(\TeleMusica, array[0], nil, 0).addresses_(array).makeWindow;
	if(relay[\on]){this.sendRelayMsg("/string","TeleMusica conectado, empezando transmicion");}
	}
	
	printIp{|name|
	postln(" ");
	if(name.isNil){
	(address.keys).do({|item,i|
	post("[ "++item+" , ");
	post(address[item].ip+" , ");
	postln(address[item].port+" ]");
	});
	}
	{
	post("[ "++name+" , ");
	post(address[name].ip+" , ");
	postln(address[name].port+" ]");
	};
	
	}

	ip{|nombre|
	^address[nombre].ip		
	}
	
	port{|nombre|
	^address[nombre].port		
	}
	
	castSynth{|synthDef|
	post("Sinte "++synthDef.name++" mandado a: ");
		 server.do({|item|
	 				post(item);
	 				post(" ");
	 				synthDef.send(item)
	 			});
	 	postln(" ");
	 
	if(relay[\on]){this.sendRelayMsg("/d_recv",synthDef.name);}
		
	 }
	 
	newSynth{|name,id,values=nil|
		if(values.isNil)
		{
		bCaster.sendMsg(\s_new,name,id);
		if(relay[\on]){
			this.sendRelayMsg("/s_new",format("%,%",name,id))}
		}
		{
		bCaster.sendBundle(bCaster.latency, [\s_new,name,id, 0, 1] ++ values);
		if(relay[\on]){
		this.sendRelayMsg("/s_new",format("%,%,%",name,id,values))}
		};
		
	}
	
	set{|id,parameter,parametervalue|
		if(size(parameter) == 0)
		{bCaster.sendMsg(\n_set,id,parameter,parametervalue);
		if(relay[\on]){
			this.sendRelayMsg("/n_set",format("%,%,%",id,parameter,parametervalue))}
		
			}
		{bCaster.sendBundle(bCaster.latency,[\n_set,id]++parameter);
		if(relay[\on]){
			this.sendRelayMsg("/n_set",format("%,%",id,parameter))}
		
			};
	}
	
	free{|id|
	bCaster.sendMsg(\n_free,id);
					if(relay[\on]){
			this.sendRelayMsg("/n_free",format("%",id))}
		
	}
	
	release{|id|
	bCaster.sendMsg(\n_set,id,\gate,0);
			if(relay[\on]){
			this.sendRelayMsg("/n_release",format("%",id))}
		
	}
	
	sendRelayMsg{|cmd="/string",string|
	relay[\address].do({|item|
		item.sendMsg(cmd,values[\location],values[\name],string);
	});
	}
	
	addRelayGui{|ip,port|
	win.setInnerExtent(win.bounds.width,win.bounds.height+22);

	StaticText(win,500@18)
	.font_(Font("Monaco",15))
	.background_(Color.rand)
	.string_(format("  Relay ip: %    port: %",ip,port));
	}
}
       
                                                                                                                   