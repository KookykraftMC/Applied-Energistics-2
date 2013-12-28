package appeng.client.gui;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.me.InternalSlotME;
import appeng.client.me.SlotME;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import cpw.mods.fml.common.network.PacketDispatcher;

public abstract class AEBaseGui extends GuiContainer
{

	protected List<InternalSlotME> meSlots = new LinkedList<InternalSlotME>();
	protected GuiScrollbar myScrollBar = null;

	public AEBaseGui(Container container) {
		super( container );
	}

	@Override
	public void initGui()
	{
		super.initGui();

		Iterator<Slot> i = inventorySlots.inventorySlots.iterator();
		while (i.hasNext())
			if ( i.next() instanceof SlotME )
				i.remove();

		for (InternalSlotME me : meSlots)
			inventorySlots.inventorySlots.add( new SlotME( me ) );
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();

		if ( myScrollBar != null )
		{
			int i = Mouse.getEventDWheel();
			if ( i != 0 )
				myScrollBar.wheel( i );
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotIdx, int ctrlDown, int key)
	{
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;

		if ( slot instanceof SlotME )
		{
			InventoryAction action = null;
			IAEItemStack stack = null;

			switch (key)
			{
			case 0: // pickup / set-down.
				action = ctrlDown == 1 ? InventoryAction.SPLIT_OR_PLACESINGLE : InventoryAction.PICKUP_OR_SETDOWN;
				stack = ((SlotME) slot).getAEStack();
				break;
			case 1:
				action = InventoryAction.SHIFT_CLICK;
				stack = ((SlotME) slot).getAEStack();
				break;

			case 3: // creative dupe:
				if ( player.capabilities.isCreativeMode )
				{
					IAEItemStack slotItem = ((SlotME) slot).getAEStack();
					if ( slotItem != null )
					{
						action = InventoryAction.CREATIVE_DUPLICATE;
						stack = slotItem;
					}
				}
				break;

			default:
			case 4: // drop item:
			case 6:
			}

			if ( action != null )
			{
				PacketInventoryAction p;
				try
				{
					p = new PacketInventoryAction( action, slotIdx, stack );
					PacketDispatcher.sendPacketToServer( p.getPacket() );
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}

			return;
		}

		super.handleMouseClick( slot, slotIdx, ctrlDown, key );
	}

	@Override
	public void drawScreen(int mouse_x, int mouse_y, float btn)
	{
		super.drawScreen( mouse_x, mouse_y, btn );

		boolean hasClicked = Mouse.isButtonDown( 0 );
		if ( hasClicked && myScrollBar != null )
			myScrollBar.click( this, mouse_x - guiLeft, mouse_y - guiTop );

		for (Object c : buttonList)
		{
			if ( c instanceof ITooltip )
			{
				ITooltip tooltip = (ITooltip) c;
				int x = tooltip.xPos(); // ((GuiImgButton) c).xPosition;
				int y = tooltip.yPos(); // ((GuiImgButton) c).yPosition;

				if ( x < mouse_x && x + tooltip.getWidth() > mouse_x )
				{
					if ( y < mouse_y && y + tooltip.getHeight() > mouse_y )
					{
						String msg = tooltip.getMsg();
						if ( msg != null )
							drawTooltip( x + 8, y + 4, 0, msg );
					}
				}
			}
		}
	}

	public void drawTooltip(int par2, int par3, int forceWidth, String Msg)
	{
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		String[] var4 = Msg.split( "\n" );

		if ( var4.length > 0 )
		{
			int var5 = 0;
			int var6;
			int var7;

			for (var6 = 0; var6 < var4.length; ++var6)
			{
				var7 = fontRenderer.getStringWidth( (String) var4[var6] );

				if ( var7 > var5 )
				{
					var5 = var7;
				}
			}

			var6 = par2 + 12;
			var7 = par3 - 12;
			int var9 = 8;

			if ( var4.length > 1 )
			{
				var9 += 2 + (var4.length - 1) * 10;
			}

			if ( this.guiTop + var7 + var9 + 6 > this.height )
			{
				var7 = this.height - var9 - this.guiTop - 6;
			}

			if ( forceWidth > 0 )
				var5 = forceWidth;

			this.zLevel = 300.0F;
			itemRenderer.zLevel = 300.0F;
			int var10 = -267386864;
			this.drawGradientRect( var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10 );
			int var11 = 1347420415;
			int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
			this.drawGradientRect( var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12 );

			for (int var13 = 0; var13 < var4.length; ++var13)
			{
				String var14 = (String) var4[var13];

				if ( var13 == 0 )
				{
					var14 = "\u00a7" + Integer.toHexString( 15 ) + var14;
				}
				else
				{
					var14 = "\u00a77" + var14;
				}

				this.fontRenderer.drawStringWithShadow( var14, var6, var7, -1 );

				if ( var13 == 0 )
				{
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
			itemRenderer.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}

	public abstract void drawBG(int offsetX, int offsetY, int mouseX, int mouseY);

	public abstract void drawFG(int offsetX, int offsetY, int mouseX, int mouseY);

	public void bindTexture(String base, String file)
	{
		ResourceLocation loc = new ResourceLocation( base, "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	public void bindTexture(String file)
	{
		ResourceLocation loc = new ResourceLocation( "appliedenergistics2", "textures/" + file );
		this.mc.getTextureManager().bindTexture( loc );
	}

	protected void drawItem(int x, int y, ItemStack is)
	{
		this.zLevel = 100.0F;
		itemRenderer.zLevel = 100.0F;

		GL11.glEnable( GL11.GL_LIGHTING );
		GL11.glEnable( GL12.GL_RESCALE_NORMAL );
		RenderHelper.enableGUIStandardItemLighting();
		itemRenderer.renderItemAndEffectIntoGUI( this.fontRenderer, this.mc.renderEngine, is, x, y );
		GL11.glDisable( GL11.GL_LIGHTING );

		itemRenderer.zLevel = 0.0F;
		this.zLevel = 0.0F;
	}

	@Override
	final protected void drawGuiContainerBackgroundLayer(float f, int x, int y)
	{
		int ox = guiLeft; // (width - xSize) / 2;
		int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		drawBG( ox, oy, x, y );
	}

	@Override
	final protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		int ox = guiLeft; // (width - xSize) / 2;
		int oy = guiTop; // (height - ySize) / 2;
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
		drawFG( ox, oy, x, y );
		if ( myScrollBar != null )
			myScrollBar.draw( this );
	}

}