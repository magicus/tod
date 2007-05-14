import javafx.ui.*;
import javafx.ui.canvas.*;
import javafx.ui.filter.*;
import java.awt.Dimension;
import tod.gui.JavaFXBridge;

class Home
{
//	attribute selectedItem: Item;
	attribute toto: Integer;
}

var home:Home = Home;
var buzyt = "Hole";
home.toto = 0;
println("Hola");
println(home.toto);
println(buzyt);

class Item extends CompositeNode
{
	attribute title: String;
	attribute icon: Image?;
	
	attribute x: Number;
	attribute y: Number;
	attribute w: Number;
	attribute h: Number;
	
	operation frame(x:Number, y: Number, w:Number, h:Number);
}

println(buzyt);

operation foo()
{
	println(buzyt);
}

function Item.composeNode() = 
	Group
	{
		transform: bind translate(x, y)
		onMouseClicked: operation(e:CanvasMouseEvent)
				{
					println(buzyt);
					println("mouse click (rect)");
					println(home);
//					home.toto++;
//					home.selectedItem = this;
				}		
		content: 
		[
			Rect 
			{
				x: 0
				y: 0
				height: bind w
				width: bind h
				arcHeight: 20
				arcWidth: 20
				fill: cyan
				stroke: purple
				strokeWidth: 2
				onMouseClicked: operation(e:CanvasMouseEvent)
				{
					println("mouse click (rect)");
					println(home);
//					home.toto++;
//					home.selectedItem = this;
				}
				
			},
			Rect
			{
				x: 50
				y: 50
				height: bind w-50
				width: bind h-50
				fill: yellow
			},
			View
			{
				size: bind new Dimension (w, h-40)
				antialias: true
				antialiasText: true
				content: Label
				{
					text: bind "<html> <div align='center'> {title} </div> </html>"
					font: Font {size: 30}
				}
			}
			
		]
	};

operation Item.frame(x:Number, y: Number, w:Number, h:Number)
{
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;
}

var _items = [
	Item {title: "Item1"},
	Item {title: "Item2"},
	Item {title: "Item3"},
	Item {title: "Item4"},
	Item {title: "Item5"},
	Item {title: "Item6"}
];

operation createMenu(items:Item*)
{
	var g = Group;
	var n = sizeof items;
	for (item in items)
	{
		var i:Integer = indexof item;
		var row:Integer = (i/2).intValue();
		var col:Integer = i%2;
		
		item.frame(col*100, row*100, 100, 100);
		insert item into g.content;
	}
	return g;
}

var panel = Canvas
{
	width: 800
	height: 600
	content: Group
	{
		transform: []
		content: createMenu(_items)
	}
};

JavaFXBridge.put(panel.getComponent());

