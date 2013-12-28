package appeng.parts.automation;

import java.util.Random;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AECableType;
import appeng.client.texture.CableBusTextures;
import appeng.parts.PartBasicState;
import appeng.util.Platform;

public class PartLevelEmitter extends PartBasicState
{

	final int FLAG_ON = 4;

	@Override
	protected int populateFlags(int cf)
	{
		return cf | (isLevelEmitterOn() ? FLAG_ON : 0);
	}

	public PartLevelEmitter(ItemStack is) {
		super( PartLevelEmitter.class, is );
	}

	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( is.getIconIndex() );
		Tessellator.instance.startDrawingQuads();
		renderTorchAtAngle( 0, -0.5, 0 );
		Tessellator.instance.draw();
		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderInventoryBox( renderer );
	}

	double cenx;
	double ceny;
	double cenz;

	public void addVertexWithUV(double x, double y, double z, double u, double v)
	{
		Tessellator var12 = Tessellator.instance;

		x -= cenx;
		y -= ceny;
		z -= cenz;

		if ( side == ForgeDirection.DOWN )
		{
			y = -y;
			z = -z;
		}

		if ( side == ForgeDirection.EAST )
		{
			double m = x;
			x = y;
			y = m;
			y = -y;
		}

		if ( side == ForgeDirection.WEST )
		{
			double m = x;
			x = -y;
			y = m;
		}

		if ( side == ForgeDirection.SOUTH )
		{
			double m = z;
			z = y;
			y = m;
			y = -y;
		}

		if ( side == ForgeDirection.NORTH )
		{
			double m = z;
			z = -y;
			y = m;
		}

		x += cenx;// + orientation.offsetX * 0.4;
		y += ceny;// + orientation.offsetY * 0.4;
		z += cenz;// + orientation.offsetZ * 0.4;

		var12.addVertexWithUV( x, y, z, u, v );
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r)
	{
		if ( isLevelEmitterOn() )
		{
			ForgeDirection d = side;

			double d0 = (double) ((float) d.offsetX * 0.45F) + (double) (r.nextFloat() - 0.5F) * 0.2D;
			double d1 = (double) ((float) d.offsetY * 0.45F) + (double) (r.nextFloat() - 0.5F) * 0.2D;
			double d2 = (double) ((float) d.offsetZ * 0.45F) + (double) (r.nextFloat() - 0.5F) * 0.2D;

			world.spawnParticle( "reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );
		}
	}

	public void renderTorchAtAngle(double baseX, double baseY, double baseZ)
	{
		boolean isOn = isLevelEmitterOn();
		Icon icon = (isOn ? CableBusTextures.LevelEmitterTorchOn.getIcon() : is.getIconIndex());
		//
		cenx = baseX + 0.5;
		ceny = baseY + 0.5;
		cenz = baseZ + 0.5;

		baseY += 7.0 / 16.0;
		;
		double par10 = 0;
		// double par11 = 0;
		double Zero = 0;

		/*
		 * double d5 = (double)icon.func_94209_e(); double d6 = (double)icon.func_94206_g(); double d7 =
		 * (double)icon.func_94212_f(); double d8 = (double)icon.func_94210_h(); double d9 =
		 * (double)icon.func_94214_a(7.0D); double d10 = (double)icon.func_94207_b(6.0D); double d11 =
		 * (double)icon.func_94214_a(9.0D); double d12 = (double)icon.func_94207_b(8.0D); double d13 =
		 * (double)icon.func_94214_a(7.0D); double d14 = (double)icon.func_94207_b(13.0D); double d15 =
		 * (double)icon.func_94214_a(9.0D); double d16 = (double)icon.func_94207_b(15.0D);
		 */

		float var16 = icon.getMinU();
		float var17 = icon.getMaxU();
		float var18 = icon.getMinV();
		float var19 = icon.getMaxV();
		/*
		 * float var16 = (float)var14 / 256.0F; float var17 = ((float)var14 + 15.99F) / 256.0F; float var18 =
		 * (float)var15 / 256.0F; float var19 = ((float)var15 + 15.99F) / 256.0F;
		 */
		double var20 = (double) icon.getInterpolatedU( 7.0D );
		double var24 = (double) icon.getInterpolatedU( 9.0D );
		double var22 = (double) icon.getInterpolatedV( 6.0D + (isOn ? 0 : 1.0D) );
		double var26 = (double) icon.getInterpolatedV( 8.0D + (isOn ? 0 : 1.0D) );
		double var28 = (double) icon.getInterpolatedU( 7.0D );
		double var30 = (double) icon.getInterpolatedV( 13.0D );
		double var32 = (double) icon.getInterpolatedU( 9.0D );
		double var34 = (double) icon.getInterpolatedV( 15.0D );

		double var22b = (double) icon.getInterpolatedV( 9.0D );
		double var26b = (double) icon.getInterpolatedV( 11.0D );

		baseX += 0.5D;
		baseZ += 0.5D;
		double var36 = baseX - 0.5D;
		double var38 = baseX + 0.5D;
		double var40 = baseZ - 0.5D;
		double var42 = baseZ + 0.5D;
		double var44 = 0.0625D;
		double var422 = 0.1915D;
		double TorchLen = 0.625D;

		double toff = 0.0d;

		if ( !isOn )
		{
			toff = 1.0d / 16.0d;
		}

		Tessellator var12 = Tessellator.instance;
		if ( isOn )
		{
			var12.setColorOpaque_F( 1.0F, 1.0F, 1.0F );
			var12.setBrightness( 11 << 20 | 11 << 4 );
		}

		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) - var44, var20, var22 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) + var44, var20, var26 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) + var44, var24, var26 );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + TorchLen - toff, baseZ + par10 * (1.0D - TorchLen) - var44, var24, var22 );

		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) - var44, var24, var22b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) + var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) + var44, var24, var26b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) + var44, var20, var26b );
		this.addVertexWithUV( baseX + Zero * (1.0D - TorchLen) - var44, baseY + var422, baseZ + par10 * (1.0D - TorchLen) - var44, var20, var22b );

		this.addVertexWithUV( baseX + var44 + Zero, baseY, baseZ - var44 + par10, var32, var30 );
		this.addVertexWithUV( baseX + var44 + Zero, baseY, baseZ + var44 + par10, var32, var34 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY, baseZ + var44 + par10, var28, var34 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY, baseZ - var44 + par10, var28, var30 );

		this.addVertexWithUV( baseX - var44, baseY + 1.0D, var40, (double) var16, (double) var18 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY + 0.0D, var40 + par10, (double) var16, (double) var19 );
		this.addVertexWithUV( baseX - var44 + Zero, baseY + 0.0D, var42 + par10, (double) var17, (double) var19 );
		this.addVertexWithUV( baseX - var44, baseY + 1.0D, var42, (double) var17, (double) var18 );

		this.addVertexWithUV( baseX + var44, baseY + 1.0D, var42, (double) var16, (double) var18 );
		this.addVertexWithUV( baseX + Zero + var44, baseY + 0.0D, var42 + par10, (double) var16, (double) var19 );
		this.addVertexWithUV( baseX + Zero + var44, baseY + 0.0D, var40 + par10, (double) var17, (double) var19 );
		this.addVertexWithUV( baseX + var44, baseY + 1.0D, var40, (double) var17, (double) var18 );

		this.addVertexWithUV( var36, baseY + 1.0D, baseZ + var44, (double) var16, (double) var18 );
		this.addVertexWithUV( var36 + Zero, baseY + 0.0D, baseZ + var44 + par10, (double) var16, (double) var19 );
		this.addVertexWithUV( var38 + Zero, baseY + 0.0D, baseZ + var44 + par10, (double) var17, (double) var19 );
		this.addVertexWithUV( var38, baseY + 1.0D, baseZ + var44, (double) var17, (double) var18 );

		this.addVertexWithUV( var38, baseY + 1.0D, baseZ - var44, (double) var16, (double) var18 );
		this.addVertexWithUV( var38 + Zero, baseY + 0.0D, baseZ - var44 + par10, (double) var16, (double) var19 );
		this.addVertexWithUV( var36 + Zero, baseY + 0.0D, baseZ - var44 + par10, (double) var17, (double) var19 );
		this.addVertexWithUV( var36, baseY + 1.0D, baseZ - var44, (double) var17, (double) var18 );
	}

	boolean status = false;;

	private boolean isLevelEmitterOn()
	{
		if ( Platform.isClient() )
			return (clientFlags & FLAG_ON) == FLAG_ON;

		return false;
	}

	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer)
	{
		rh.setTexture( is.getIconIndex() );
		// rh.setTexture( CableBusTextures.ItemPartLevelEmitterOn.getIcon() );

		// rh.setBounds( 2, 2, 14, 14, 14, 16 );
		// rh.renderBlock( x, y, z, renderer );

		// rh.setBounds( 7, 7, 10, 9, 9, 15 );
		// rh.renderBlock( x, y, z, renderer );

		renderer.renderAllFaces = true;

		Tessellator tess = Tessellator.instance;
		tess.setBrightness( rh.getBlock().getMixedBrightnessForBlock( this.getHost().getTile().worldObj, x, y, z ) );
		tess.setColorOpaque_F( 1.0F, 1.0F, 1.0F );

		renderTorchAtAngle( x, y, z );

		renderer.renderAllFaces = false;

		rh.setBounds( 7, 7, 11, 9, 9, 12 );
		renderLights( x, y, z, rh, renderer );

		// super.renderWorldBlock( world, x, y, z, block, modelId, renderer );
	}

	@Override
	public void getBoxes(IPartCollsionHelper bch)
	{
		bch.addBox( 7, 7, 10, 9, 9, 16 );
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.SMART;
	}

	@Override
	public int cableConnectionRenderTo()
	{
		return 16;
	}

}