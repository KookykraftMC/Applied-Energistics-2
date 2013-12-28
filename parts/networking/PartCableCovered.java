package appeng.parts.networking;

import java.util.EnumSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.client.texture.OffsetIcon;
import appeng.util.Platform;

public class PartCableCovered extends PartCable
{

	@MENetworkEventSubscribe
	public void channelUpdated(MENetworkChannelsChanged c)
	{
		getHost().markForUpdate();
	}

	@MENetworkEventSubscribe
	public void powerRender(MENetworkPowerStatusChange c)
	{
		getHost().markForUpdate();
	}

	public PartCableCovered(ItemStack is) {
		super( PartCableCovered.class, is );
	}

	@Override
	public Icon getTexture(AEColor c)
	{
		return getCoveredTexture( c );
	}

	@Override
	public AECableType getCableConnectionType()
	{
		return AECableType.COVERED;
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 5.0, 5.0, 5.0, 11.0, 11.0, 11.0 );

		if ( Platform.isServer() )
		{
			IGridNode n = getGridNode();
			if ( n != null )
				connections = n.getConnectedSides();
			else
				connections.clear();
		}

		for (ForgeDirection of : connections)
		{
			switch (of)
			{
			case DOWN:
				bch.addBox( 5.0, 0.0, 5.0, 11.0, 5.0, 11.0 );
				break;
			case EAST:
				bch.addBox( 11.0, 5.0, 5.0, 16.0, 11.0, 11.0 );
				break;
			case NORTH:
				bch.addBox( 5.0, 5.0, 0.0, 11.0, 11.0, 5.0 );
				break;
			case SOUTH:
				bch.addBox( 5.0, 5.0, 11.0, 11.0, 11.0, 16.0 );
				break;
			case UP:
				bch.addBox( 5.0, 11.0, 5.0, 11.0, 16.0, 11.0 );
				break;
			case WEST:
				bch.addBox( 0.0, 5.0, 5.0, 5.0, 11.0, 11.0 );
				break;
			default:
				continue;
			}
		}
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		GL11.glTranslated( -0.2, -0.3, 0.0 );

		rh.setTexture( getTexture( getCableColor() ) );
		rh.setBounds( 5.0f, 5.0f, 2.0f, 11.0f, 11.0f, 14.0f );
		rh.renderInventoryBox( renderer );
		rh.setTexture( null );
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( getTexture( getCableColor() ) );

		EnumSet<ForgeDirection> sides = connections.clone();

		boolean hasBuses = false;
		IPartHost ph = getHost();
		for (ForgeDirection of : EnumSet.complementOf( connections ))
		{
			IPart bp = ph.getPart( of );
			if ( bp instanceof IGridHost )
			{
				if ( of != ForgeDirection.UNKNOWN )
				{
					sides.add( of );
					hasBuses = true;
				}

				int len = bp.cableConnectionRenderTo();
				if ( len < 8 )
				{
					switch (of)
					{
					case DOWN:
						rh.setBounds( 6, len, 6, 10, 5, 10 );
						break;
					case EAST:
						rh.setBounds( 11, 6, 6, 16 - len, 10, 10 );
						break;
					case NORTH:
						rh.setBounds( 6, 6, len, 10, 10, 5 );
						break;
					case SOUTH:
						rh.setBounds( 6, 6, 11, 10, 10, 16 - len );
						break;
					case UP:
						rh.setBounds( 6, 11, 6, 10, 16 - len, 10 );
						break;
					case WEST:
						rh.setBounds( len, 6, 6, 5, 10, 10 );
						break;
					default:
						continue;
					}
					rh.renderBlock( x, y, z, renderer );
				}
			}
		}

		if ( sides.size() != 2 || !nonLinear( sides ) || hasBuses )
		{
			for (ForgeDirection of : connections)
			{
				renderCoveredConection( x, y, z, rh, renderer, channelsOnSide[of.ordinal()], of );
			}

			rh.setTexture( getTexture( getCableColor() ) );
			rh.setBounds( 5, 5, 5, 11, 11, 11 );
			rh.renderBlock( x, y, z, renderer );
		}
		else
		{
			Icon def = getTexture( getCableColor() );
			Icon off = new OffsetIcon( def, 0, -12 );
			for (ForgeDirection of : connections)
			{
				switch (of)
				{
				case DOWN:
				case UP:
					rh.setTexture( def, def, off, off, off, off );
					renderer.setRenderBounds( 5 / 16.0, 0, 5 / 16.0, 11 / 16.0, 16 / 16.0, 11 / 16.0 );
					break;
				case EAST:
				case WEST:
					rh.setTexture( off, off, off, off, def, def );
					renderer.uvRotateEast = renderer.uvRotateWest = 1;
					renderer.uvRotateBottom = renderer.uvRotateTop = 1;
					renderer.setRenderBounds( 0, 5 / 16.0, 5 / 16.0, 16 / 16.0, 11 / 16.0, 11 / 16.0 );
					break;
				case NORTH:
				case SOUTH:
					rh.setTexture( off, off, def, def, off, off );
					renderer.uvRotateNorth = renderer.uvRotateSouth = 1;
					renderer.setRenderBounds( 5 / 16.0, 5 / 16.0, 0, 11 / 16.0, 11 / 16.0, 16 / 16.0 );
					break;
				default:
					continue;
				}
			}

			renderer.renderStandardBlock( rh.getBlock(), x, y, z );
		}

		renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;
		rh.setTexture( null );
	}

}