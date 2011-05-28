
// sometimes you just need a chronometer
Chronometer {
var <>w, <>x, <>y, <>clockField ;
var <>r, <>startTime, <>title ;

*new { arg runner, x = 30, y = 120, title ="Tempus fugit" ; 
^super.new.initChronometer(x, y, title) 
}

initChronometer { arg aX, aY, aTitle ;
x = aX ;
y = aY ;  
title = aTitle ;
startTime = thisThread.seconds ;
this.createGUI(x, y, aTitle) ;
} 


createGUI { arg x = 10, y = 120, title =  "Tempus fugit" ;
w = GUI.window.new(title, Rect(x, y, 200, 60)) ;
clockField = GUI.staticText.new(w, Rect(5,5, 190, 30))
.align_(\center)
.stringColor_(Color(1.0, 0.0, 0.0))
.background_(Color(0,0,0))
.font_(GUI.font.new("Optima", 24)) ; 
r = Task.new({ arg i ; 
loop({ 
clockField.string_((thisThread.seconds-startTime).asTimeString) ;
1.wait })// a clock refreshing once a second
}).play(AppClock) ;
w.front ;
w.onClose_({
r.stop ;
}) ;

}


}


