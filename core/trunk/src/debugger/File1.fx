import javafx.ui.*;
import javafx.ui.canvas.*;
import javafx.ui.filter.*;
import java.awt.Dimension;


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

function Item.composeNode() = 
	Group
	{
		transform: bind translate(x, y)
		onMouseClicked: operation(e:CanvasMouseEvent)
				{
					println("mouse click (grp)");
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

Frame
{
	content: panel
	visible: true
}
