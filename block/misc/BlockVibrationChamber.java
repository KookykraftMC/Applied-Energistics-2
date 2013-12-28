package appeng.block.misc;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.block.AEBaseBlock;
import appeng.client.texture.ExtraTextures;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileVibrationChamber;
import appeng.util.Platform;

public class BlockVibrationChamber extends AEBaseBlock
{

	public BlockVibrationChamber() {
		super( BlockVibrationChamber.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.PowerGen ) );
		setTileEntiy( TileVibrationChamber.class );
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player.isSneaking() )
			return false;

		if ( Platform.isServer() )
		{
			TileVibrationChamber tc = getTileEntity( w, x, y, z );
			if ( tc != null && !player.isSneaking() )
			{
				Platform.openGUI( player, tc, ForgeDirection.getOrientation(side),GuiBridge.GUI_VIBRATIONCHAMBER );
				return true;
			}
		}

		return true;
	}

	@Override
	public Icon getBlockTexture(IBlockAccess w, int x, int y, int z, int s)
	{
		Icon ico = super.getBlockTexture( w, x, y, z, s );
		TileVibrationChamber tvc = getTileEntity( w, x, y, z );

		if ( tvc != null && tvc.isOn && ico == getRendererInstance().getTexture( ForgeDirection.SOUTH ) )
		{
			return ExtraTextures.BlockVibrationChamberFrontOn.getIcon();
		}

		return ico;
	}

	@Override
	public void randomDisplayTick(World w, int x, int y, int z, Random r)
	{
		if ( !Configuration.instance.enableEffects )
			return;

		AEBaseTile tile = getTileEntity( w, x, y, z );
		if ( tile instanceof TileVibrationChamber )
		{
			TileVibrationChamber tc = (TileVibrationChamber) tile;
			if ( tc.isOn )
			{
				float f1 = (float) x + 0.5F;
				float f2 = (float) y + 0.5F;
				float f3 = (float) z + 0.5F;

				ForgeDirection forward = tc.getForward();
				ForgeDirection up = tc.getUp();

				int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
				int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
				int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

				f1 += forward.offsetX * 0.6;
				f2 += forward.offsetY * 0.6;
				f3 += forward.offsetZ * 0.6;

				float ox = r.nextFloat();
				float oy = r.nextFloat() * 0.2f;

				f1 += up.offsetX * (-0.3 + oy);
				f2 += up.offsetY * (-0.3 + oy);
				f3 += up.offsetZ * (-0.3 + oy);

				f1 += west_x * (0.3 * ox - 0.15);
				f2 += west_y * (0.3 * ox - 0.15);
				f3 += west_z * (0.3 * ox - 0.15);

				w.spawnParticle( "smoke", (double) f1, (double) f2, (double) f3, 0.0D, 0.0D, 0.0D );
				w.spawnParticle( "flame", (double) f1, (double) f2, (double) f3, 0.0D, 0.0D, 0.0D );
			}
		}
	}

}