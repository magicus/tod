package tod.gui.view.dyncross;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicArrowButton;

import tod.core.database.structure.ILocationInfo;
import tod.core.database.structure.IBehaviorInfo.BytecodeRole;
import tod.gui.GUIUtils;
import tod.gui.Resources;
import tod.gui.components.intimacyeditor.IntimacyLevelEditor;
import tod.gui.eventlist.IntimacyLevel;
import tod.gui.seed.DynamicCrosscuttingSeed.Highlight;
import zz.utils.Utils;
import zz.utils.ui.UIUtils;
import zz.utils.ui.popup.ButtonPopupComponent;

/**
 * Intimacy editor for a single advice/aspect
 * @author gpothier
 */
class HighlightEditor extends JPanel
implements ChangeListener
{
	private static final Color[] COLORS =
	{ Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW, Color.GRAY };


	private static final Border BORDER = BorderFactory.createLineBorder(Color.BLACK);
	
	private final DynamicCrosscuttingView itsDynamicCrosscuttingView;
	
	/**
	 * The currently edited location
	 */
	private ILocationInfo itsLocation;
	
	/**
	 * The currently edited highlight.
	 */
	private Highlight itsHighlight;
	
	private Color itsSelectedColor;
	
	private JPanel itsSelectedColorPanel;
	private ButtonPopupComponent itsButton;

	private AbstractButton[] itsRoleCheckBoxes;
	
	private int itsChanging = 0;
	
	public HighlightEditor(DynamicCrosscuttingView aDynamicCrosscuttingView)
	{
		itsDynamicCrosscuttingView = aDynamicCrosscuttingView;
		createUI();
	}

	private void createUI()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		
		itsSelectedColorPanel = new JPanel(null);
		itsSelectedColorPanel.setPreferredSize(new Dimension(20, 20));

		add(itsSelectedColorPanel);

		JButton theButton = new JButton(Resources.ICON_TRIANGLE_DOWN.asIcon(9));
		theButton.setMargin(UIUtils.NULL_INSETS);
		
		itsButton = new ButtonPopupComponent(new MyPopup(), theButton);
		add(itsButton);

		JPanel theRolesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		itsRoleCheckBoxes = new AbstractButton[IntimacyLevel.ROLES.length];
		
		int i=0;
		for(BytecodeRole theRole : IntimacyLevel.ROLES)
		{
			itsRoleCheckBoxes[i] = new JToggleButton();
			ImageIcon theIcon = GUIUtils.getRoleIcon(theRole).asIcon(IntimacyLevelEditor.ROLE_ICON_SIZE);
			itsRoleCheckBoxes[i].setSelectedIcon(theIcon);
			itsRoleCheckBoxes[i].setIcon(new ImageIcon(GrayFilter.createDisabledImage(theIcon.getImage())));
			itsRoleCheckBoxes[i].setMargin(UIUtils.NULL_INSETS);
			itsRoleCheckBoxes[i].addChangeListener(this);
			
			theRolesPanel.add(itsRoleCheckBoxes[i]);
			i++;
		}
		
		add(theRolesPanel);
	}
	
	protected ILocationInfo getLocationInfo()
	{
		return itsLocation;
	}

	public void setLocationInfo(ILocationInfo aLocation)
	{
		itsLocation = aLocation;
	}
	
	public void setValue(Highlight aHighlight)
	{
		itsChanging++;
		itsHighlight = aHighlight;

		setSelectedColor(itsHighlight != null ? itsHighlight.getColor() : null);
		
		if (itsHighlight == null)
		{
			for (AbstractButton theCheckBox : itsRoleCheckBoxes) theCheckBox.setSelected(false);
		}
		else
		{
			int i=0;
			for(BytecodeRole theRole : IntimacyLevel.ROLES)
			{
				itsRoleCheckBoxes[i++].setSelected(itsHighlight.getRoles().contains(theRole));
			}
		}
		itsChanging--;
	}
	
	private void setSelectedColor(Color aColor)
	{
		itsSelectedColor = aColor;
		if (itsSelectedColor == null)
		{
			itsSelectedColorPanel.setBorder(null);
			itsSelectedColorPanel.setBackground(getBackground());
		}
		else
		{
			itsSelectedColorPanel.setBorder(BORDER);
			itsSelectedColorPanel.setBackground(itsSelectedColor);
		}
		
		for (AbstractButton theButton : itsRoleCheckBoxes) theButton.setEnabled(itsSelectedColor != null);
	}
	
	public Highlight getValue()
	{
		if (itsSelectedColor == null) return null;
		
		Set<BytecodeRole> theRoles = new HashSet<BytecodeRole>();
		int i=0;
		for(BytecodeRole theRole : IntimacyLevel.ROLES)
		{
			if (itsRoleCheckBoxes[i++].isSelected()) theRoles.add(theRole);
		}
		
		return new Highlight(itsSelectedColor, theRoles, itsLocation);
	}
	
	public void stateChanged(ChangeEvent aE)
	{
		assert itsChanging >= 0;
		if (itsChanging == 0)
		{
			Highlight theNewHighlight = getValue();
			if (Utils.different(theNewHighlight, itsHighlight))
			{
				itsHighlight = theNewHighlight;
				itsDynamicCrosscuttingView.setHighlight(itsLocation, itsHighlight);
			}
		}
	}
	
	private void selectColor(Color aColor)
	{
		itsChanging++;
		
		// By default select everything.
		if (itsSelectedColor == null && aColor != null)
		{
			for (AbstractButton theButton : itsRoleCheckBoxes) theButton.setSelected(true);
		}
		
		setSelectedColor(aColor);
		itsButton.hidePopup();
		
		itsChanging--;
		
		stateChanged(null);
	}

	private class MyPopup extends JPanel
	{
		public MyPopup()
		{
			createUI();
		}

		private void createUI()
		{
			setLayout(new BorderLayout());

			MouseListener theListener = new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					Object theSource = e.getSource();
					if (theSource instanceof JLabel) selectColor(null);
					else selectColor(((JPanel) theSource).getBackground());
				}
			};

			JLabel theDisableLabel = new JLabel("Disable");
			theDisableLabel.setBorder(BORDER);
			theDisableLabel.addMouseListener(theListener);

			JPanel theNorthPanel = new JPanel();
			theNorthPanel.add(theDisableLabel);
			add(theNorthPanel, BorderLayout.NORTH);

			JPanel theCenterPanel = new JPanel(new GridLayout(0, 5, 5, 5));

			for (Color theColor : COLORS)
			{
				JPanel theColorPanel = new JPanel(null);
				theColorPanel.setPreferredSize(new Dimension(20, 20));
				theColorPanel.setBackground(theColor);
				theColorPanel.setBorder(BORDER);
				theColorPanel.addMouseListener(theListener);

				theCenterPanel.add(theColorPanel);
			}

			add(theCenterPanel, BorderLayout.CENTER);
		}
	}


}