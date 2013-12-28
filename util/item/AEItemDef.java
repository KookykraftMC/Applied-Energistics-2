package appeng.util.item;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEItemDef
{

	public int myHash;

	public int def;

	public Item item;
	public int damageValue;

	public int dspDamage;
	public int maxDamage;

	public IAETagCompound tagCompound;

	@SideOnly(Side.CLIENT)
	public String displayName;

	@SideOnly(Side.CLIENT)
	public List tooltip;

	public AEItemDef copy()
	{
		AEItemDef t = new AEItemDef();
		t.def = def;
		t.item = item;
		t.damageValue = damageValue;
		t.dspDamage = dspDamage;
		t.maxDamage = maxDamage;
		return t;
	}

	@Override
	public boolean equals(Object obj)
	{
		AEItemDef def = (AEItemDef) obj;
		return def.damageValue == damageValue && def.item == item && tagCompound == def.tagCompound;
	}

	public int getDamageValueHack(ItemStack is)
	{
		return Item.blazeRod.getDamage( is );
	}

	public boolean isItem(ItemStack otherStack)
	{
		// hackery!
		int dmg = getDamageValueHack( otherStack );

		if ( item == otherStack.getItem() && dmg == damageValue )
		{
			if ( (tagCompound != null) == otherStack.hasTagCompound() )
				return true;

			if ( tagCompound != null && otherStack.hasTagCompound() )
				return Platform.NBTEqualityTest( (NBTBase) tagCompound, otherStack.getTagCompound() );

			return true;
		}
		return false;
	}
}