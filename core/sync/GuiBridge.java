package appeng.core.sync;

import java.lang.reflect.Constructor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.IStorageMonitorable;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.client.gui.GuiNull;
import appeng.container.ContainerNull;
import appeng.container.implementations.ContainerChest;
import appeng.container.implementations.ContainerCondenser;
import appeng.container.implementations.ContainerDrive;
import appeng.container.implementations.ContainerGrinder;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerVibrationChamber;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileCondenser;
import appeng.tile.misc.TileVibrationChamber;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.util.Platform;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;

public enum GuiBridge implements IGuiHandler
{
	GUI_Handler(),

	GUI_GRINDER(ContainerGrinder.class, TileGrinder.class),

	GUI_CHEST(ContainerChest.class, TileChest.class),

	GUI_ME(ContainerMEMonitorable.class, IStorageMonitorable.class),

	GUI_DRIVE(ContainerDrive.class, TileDrive.class),

	GUI_VIBRATIONCHAMBER(ContainerVibrationChamber.class, TileVibrationChamber.class),

	GUI_CONDENSER(ContainerCondenser.class, TileCondenser.class);

	private Class Tile;
	private Class Gui;
	private Class Container;

	private GuiBridge() {
		Tile = null;
		Gui = null;
		Container = null;
	}

	/**
	 * I honestly wish I could just use the GuiClass Names myself, but I can't access them without MC's Server
	 * Exploding.
	 */
	private void getGui()
	{
		if ( Platform.isClient() )
		{
			String start = Container.getName();
			String GuiClass = start.replaceFirst( "container.", "client.gui." ).replace( ".Container", ".Gui" );
			if ( start.equals( GuiClass ) )
				throw new RuntimeException( "Unable to find gui class" );
			Gui = ReflectionHelper.getClass( this.getClass().getClassLoader(), GuiClass );
			if ( Gui == null )
				throw new RuntimeException( "Cannot Load class: " + GuiClass );
		}
	}

	private GuiBridge(Class _Container) {
		Container = _Container;
		Tile = null;
		getGui();
	}

	private GuiBridge(Class _Container, Class _Tile) {
		Container = _Container;
		Tile = _Tile;
		getGui();
	}

	public boolean CorrectTileOrPart(Object tE)
	{
		if ( Tile == null )
			throw new RuntimeException( "This Gui Cannot use the standard Handler." );

		return Tile.isInstance( tE );
	}

	public Object ConstructContainer(InventoryPlayer inventory, ForgeDirection side, Object tE)
	{
		try
		{
			Constructor[] c = Container.getConstructors();
			if ( c.length == 0 )
				throw new AppEngException( "Invalid Gui Class" );
			return c[0].newInstance( inventory, tE );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	public Object ConstructGui(InventoryPlayer inventory,ForgeDirection side, Object tE)
	{
		try
		{
			Constructor[] c = Gui.getConstructors();
			if ( c.length == 0 )
				throw new AppEngException( "Invalid Gui Class" );
			return c[0].newInstance( inventory, tE );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	@Override
	public Object getServerGuiElement(int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL&  0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 3];

		TileEntity TE = w.getBlockTileEntity( x, y, z );

		if ( TE instanceof IPartHost )
		{
			((IPartHost) TE).getPart( side );
			IPart part =  ((IPartHost) TE).getPart(side);
			if ( ID.CorrectTileOrPart(part) )
				return ID.ConstructContainer( player.inventory, side, part );
		}
		else
		{
		if ( ID.CorrectTileOrPart( TE ) )
			return ID.ConstructContainer( player.inventory, side, TE );
		}
		
		return new ContainerNull();
	}

	@Override
	public Object getClientGuiElement(int ID_ORDINAL, EntityPlayer player, World w, int x, int y, int z)
	{
		ForgeDirection side = ForgeDirection.getOrientation( ID_ORDINAL&  0x07 );
		GuiBridge ID = values()[ID_ORDINAL >> 3];

		TileEntity TE = w.getBlockTileEntity( x, y, z );
		
		if ( TE instanceof IPartHost )
		{
			((IPartHost) TE).getPart( side );
			IPart part =  ((IPartHost) TE).getPart(side);
			if ( ID.CorrectTileOrPart(part) )
				return ID.ConstructGui( player.inventory, side, part );
		}
		else
		{
			if ( ID.CorrectTileOrPart( TE ) )
				return ID.ConstructGui( player.inventory, side, TE );
		}
		
		return new GuiNull( new ContainerNull() );
	}

}