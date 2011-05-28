
//Sketch basico de recibir mensajes en osc de la clase TeleMusica
//Requeire al libreria oscP5
//bajatela de http://www.sojamo.de/oscP5
//Esta basado en uno d elos ejemplos de la referencia de esta libreria


import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress myRemoteLocation;

void setup() {
  size(400,400);
  frameRate(25);
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  
  /* myRemoteLocation is a NetAddress. a NetAddress takes 2 parameters,
   * an ip address and a port number. myRemoteLocation is used as parameter in
   * oscP5.send() when sending osc packets to another computer, device, 
   * application. usage see below. for testing purposes the listening port
   * and the port of the remote location address are the same, hence you will
   * send messages back to this sketch.
   */
  myRemoteLocation = new NetAddress("127.0.0.1",57120);
}


void draw() {
  background(0);  
}


/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage theOscMessage) {
  /* print the address pattern and the typetag of the received OscMessage */
  print("### received an osc message.");
   print(" addrpattern: "+theOscMessage.addrPattern());
    println(" typetag "+ theOscMessage.typetag());
    
  if(theOscMessage.checkTypetag("sss")){
    println("En vivo desde: "+theOscMessage.get(0).stringValue()); 
    println("Usuario: "+theOscMessage.get(1).stringValue()); 
    print(theOscMessage.addrPattern()+" : ");
    println("Mensajito-> "+theOscMessage.get(2).stringValue()); 
  }

  if(theOscMessage.checkTypetag("iis")){

    println("Msg: "+theOscMessage.get(0).intValue()); 
        println("Msg: "+theOscMessage.get(1).intValue()); 
            print(theOscMessage.addrPattern()+" : ");
                println("Msg-> "+theOscMessage.get(2).stringValue()); 
  }
  
}
