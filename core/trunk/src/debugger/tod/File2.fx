import javafx.ui.*;
import tod.gui.JavaFXBridge;

class ButtonClickModel 
{
	attribute numClicks: Number;
}

var model = new ButtonClickModel();
        		
var panel = GridPanel 
{
	border: EmptyBorder { top: 30 left: 30 bottom: 30 right: 30 }
	rows: 2
	columns: 1
	vgap: 10
	cells:
	[Button 
		{
			text: "I'm a button too!"
			mnemonic: I
			action: operation() {model.numClicks++; }
		},
	Label 
		{
			text: bind "Number of button blops: {model.numClicks}"
		}]
};

JavaFXBridge.put(panel.getComponent());
