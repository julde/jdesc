/* 	(c) Jan Trutzschler 
**	Terms of the GNU General Public License apply.
*/

MTrackGuiOptions {
	var < background, <>font, <visible = true, <>height=30;
	
	visible_{|flag|
		visible = flag;
		this.changed(\visible, flag);
	}
	
	background_{|color|
		background = color;
		this.changed(\background, color);
		
	}
}