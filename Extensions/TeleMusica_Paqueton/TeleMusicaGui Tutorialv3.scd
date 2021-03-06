/*
* Colaboradores: 
* Dr. Roberto Morales Manzanares
* Benjamin Sanchez Lengeling (UG) (http://rorschach3punto0.wordpress.com/)
* Colectivo Radiador (UNAM): Alberto Cerro, Jaime Lobato, Mauro Herrera, IvÃ¡n Esquinca, Fernando LomelÃ­ 
* http://www.radiador.tk/
*/

s.queryAllNodes


/* Tutorial de la Clase TeleMusicaGui  */

/*
* Para varios equipos conectados a travÃ©s de un mismo modem y cuya Ip externa es la misma:
* Declaramos nuestro servior local con un puerto diferente al default, este caso 57115,
* porque al conectarnos a travÃ©s del mismo IP externo (en el caso de las redes domÃ©stica)
* es necesario diferenciar los equipos en Internet.
* Por ello asignamos diferentes puertos para que la informaciÃ³n sea transmitida 
* Es importante que el rango de puertos que se habilitan en el firewall del modem sea diferente
* para cada equipo de la red local
* Ejemplo: equipo1: 57110-57115  equipo2: 57116-57121
*/

(
Server.default = s = Server.local;
s = Server.new(\localhost,NetAddr("localhost",57110));
s.boot;

d=TelemusicaGui.new; //llamamos a la clase

d.addUser(\julio,"20.20.1.175",57110,"tu_cueva") //agregamos el usuario tu_nombre, debe de ser el primero, si eres el primero tambien agrega tu locacion, cambia "tu_cueva" por otro lugar



)

/*** Agregando Usuarios      ***/
//Hay dos maneras, se nesesita un nombre, un ip y opcionalmente un puerto
// 1- uno por uno:
d.addUser(\p5,"127.0.0.1",12000)

d.addUser(\rafa,"20.20.1.103", 57110)
// 2- varios a la vez, con un arreglo de informacion [nombre,ip,puerto], el puerto es opcional
d.addUser([[\lupita,"127.0.0.7"],[\copas,"127.0.0.8"],[\carmela,"127.0.0.9",57111]])

/*** Utilidades ***/
//Si quieres imprimir nombres, ips y puerto usa:
d.printIp; //para todos
d.printIp(\yo); //para un usuario en particular

//igualmente si solo nesesitas ip o puerto, esto es:
d.ip(\yo) //ip del usuario yo
d.port(\yo) //puerto del usuario yo

/***  Empezando el broadcast    ***/

d.startBroadcast // esto inicializa el BroadcastServer para mandar mensajes OSC, es nesesario!

c=d.bCaster  //si requieres directamente del BroadcastServer, usa el getter de la clase


/*** Mandando Madrolas Sonoras    ***/
//ejemplo de un sinte
~synth=SynthDef(\grainDely,{ |gate = 1,dur = 12,vol=1,freq= 440|
                     var env,in,dely,gran,trig,tidel,tidel2;
                  in = SinOsc.ar(freq*LFNoise0.kr(8),0,0.5);

                  tidel = LFNoise0.kr(dur/2,0.35,0.55);
                  dely = DelayL.ar(in,2,tidel);

                  env = EnvGen.kr(Env.asr(0.2,1,0.4),gate,doneAction:2);
                                     Out.ar(0,Pan2.ar(dely*env,0)*vol)
                                     });


d.castSynth(~synth) // mandamos el sinte a todos

//Hay dos maneras de crear un sitne:
//1- Nombre y id, tomando valores default
d.newSynth(\sinMod,1029) 
//2- Nombre, id y arreglo de valores inciales
d.newSynth(\grainDely,1029,[\freq,8000,\vol,0.2]) 

//Hay dos maneras de crear un sitne que ya esta corriendo:
//1- Cambiar un parametro
d.set(1029,\freq,3000) 

//2- Cambiar varios parametros
d.set(1029,[\freq,800,\vol,2.1]) 
//esto es lo mismo que usar :
c.sendBundle(c.latency, ["/n_set", 1029] ++ [\freq,800,\vol,2.1]);

//Hay dos maneras de matar un sinte:
// 1 - Fuerza bruta, si esta sonando puede tener un click
d.free(1029)
// 2 - Haciendo el argumento gate del sinte igual a cero, si tiene envolvente hace un fadeout
d.release(1029)

/*** Interfaz Grafica para sintes en telemusica ***/
//ejemplo de un sinte, este requiere metadata para ser efectivo

~synth=SynthDef(\sinMod,{ |out=0,gate = 1,fade = 12,amp=1,freq= 440,mod=2,mod2=4|
                     var env,sound;
                     env=EnvGen.kr(Env.asr(fade,1, fade, 1), gate, doneAction:2)*amp;
                  	  sound = SinOsc.ar([freq*SinOsc.kr(mod),freq*SinOsc.kr(mod2)]);
                     Out.ar(out,sound*env)
                     },metadata: (specs: (amp:\amp,fade:[0,30],freq:\freq,mod:[0,24],mod2:[0,240]))
                     ).add;  //notese el store, es nesesario apra crear un ventana,

d.castSynth(~synth) // mandamos el sinte a todos

//creamos la ventana, requiere el nombre, el objeto de telemusica y un id
SynthDescLib.global[\sinMod].makeTeleWindow(d,1029);


//La ventana usara el metadata para crear los sliders, y cada vez que son modificados mandara los mensajes correspondientes

/*** Manipulando la Interfaz ***/
//si le damos una variable nos regresara un diccionario con todos los controles,
//puedes modificar directamente la interfaz con esta variables y accediendo al control que quieras

//pedimos el diccionario (arreglo con nombres)
w=SynthDescLib.global[\sinMod].makeTeleWindow(d,1029);

//podemos pedir los nombres
v=w.keys.asArray
v[2] //es la amplitud
//modificamos la amplitud a 0.5 y denuevo 1.0
w[v[2]].valueAction_(0.5)                        
w[v[2]].valueAction_(1.0)

//con estas variables puedes conectar la interfaz con otras



/*** Mandando informacion a otros sistemas ***/
//Aparte de mandar mensajes a servidores de supercollider puedes mandar informacion a otros sistemas,
//en formas de mensajes OSC, estos son recibidos con el siguiente formato:
//    (  Nombre del mensaje, locacion, nombre, string )
//Nombre del mensje puede ser /string (para mensajes de texto de lo que quieras)
// Puede ser lo que sea...pero se recomienda que empieze con / , es la forma estandar de mandar mensajes
//Internamente se manejan los comandos usados para enviar mensajes: /d_recov,/s_new,/n _set


//Primero nesesitas crear una dirrecion hacia donde enviar mensajes osc
d.addRelayUser("127.0.0.1",12000)

//para enviar un mensaje usa el comando de la siguiente forma:

//d.sendVisualMsg( "nombre del mensaje", "string")

//por ejemplo:

d.sendRelayMsg("/string","TeleMusica empieza a transmitir");

//puede apagar y prender este sitema de mensajeo con:

d.relayOff()

d.relayOn()

//Para ver un ejemplo de como se usa esto, bajate Processing (www.processing.org)
//junto con la libreria oscP5, (link viene en el sketch)
//y corre el sketch TeleMusica_Relay



