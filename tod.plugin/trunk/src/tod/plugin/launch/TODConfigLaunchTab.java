/*
 * Created on Dec 10, 2006
 */
package tod.plugin.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import tod.core.config.TODConfig;
import tod.core.config.TODConfig.Item;
import tod.core.config.TODConfig.ItemType;
import zz.eclipse.utils.launcher.options.AbstractItemControl;
import zz.eclipse.utils.launcher.options.BooleanControl;
import zz.eclipse.utils.launcher.options.OptionsTab;
import zz.eclipse.utils.launcher.options.TextControl;

public class TODConfigLaunchTab extends OptionsTab<TODConfig.Item>
{
	private static final String MAP_NAME = "tod.plugin.launch.OPTIONS_MAP"; //$NON-NLS-1$
	
	private static final Map<ItemType, ItemTypeHandler> HANDLERS = initHandlers();

	public TODConfigLaunchTab()
	{
		super(TODConfig.ITEMS);
	}

	public String getName()
	{
		return "TOD options";
	}

	@Override
	protected String getMapName()
	{
		return MAP_NAME;
	}

	@Override
	public String getCaption(Item aItem)
	{
		return aItem.getName();
	}

	@Override
	protected String getDefault(Item aItem)
	{
		return aItem.getOptionString(aItem.getDefault());
	}

	@Override
	public String getDescription(Item aItem)
	{
		return aItem.getDescription();
	}

	@Override
	protected String getKey(Item aItem)
	{
		return aItem.getKey();
	}

	private static ItemTypeHandler getHandler(Item aItem)
	{
		ItemTypeHandler theHandler = HANDLERS.get(aItem.getType());
		if (theHandler == null) throw new RuntimeException("Handler not found for "+aItem+" ("+aItem.getType()+")");
		return theHandler;
	}
	
	@Override
	protected AbstractItemControl<Item> createControl(
			OptionsTab aOptionsTab,
			Composite aParent, 
			Item aItem)
	{
		return getHandler(aItem).createControl(aOptionsTab, aParent, aItem);
	}


	private static Map<ItemType, ItemTypeHandler> initHandlers()
	{
		Map<ItemType, ItemTypeHandler> theMap = new HashMap<ItemType, ItemTypeHandler>();
		
		theMap.put(TODConfig.ItemType.ITEM_TYPE_STRING, new ItemTypeHandler()
		{
			@Override
			public AbstractItemControl<Item> createControl(OptionsTab aOptionsTab, Composite aParent, Item aItem)
			{
				return new TextControl(aParent, aOptionsTab, aItem);
			}
		});
		
		theMap.put(TODConfig.ItemType.ITEM_TYPE_BOOLEAN, new ItemTypeHandler()
		{
			@Override
			public AbstractItemControl<Item> createControl(OptionsTab aOptionsTab, Composite aParent, Item aItem)
			{
				return new BooleanControl(aParent, aOptionsTab, aItem);
			}
		});
		
		return theMap;
	}
	
	/**
	 * Bridge between {@link ItemType} and SWT controls.
	 * @author gpothier
	 */
	private static abstract class ItemTypeHandler
	{
		public abstract AbstractItemControl<Item> createControl(
				OptionsTab aOptionsTab,
				Composite aParent, 
				Item aItem);
	}
}
