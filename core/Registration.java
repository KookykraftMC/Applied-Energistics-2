package appeng.core;

import ic2.api.energy.tile.IEnergySink;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.AEApi;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.spatial.ISpatialCache;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AEItemDefinition;
import appeng.block.grindstone.BlockCrank;
import appeng.block.grindstone.BlockGrinder;
import appeng.block.mac.BlockContainmentWall;
import appeng.block.mac.BlockCraftingAccelerator;
import appeng.block.mac.BlockHeatVent;
import appeng.block.mac.BlockPatternProvider;
import appeng.block.misc.BlockCharger;
import appeng.block.misc.BlockCondenser;
import appeng.block.misc.BlockInscriber;
import appeng.block.misc.BlockInterface;
import appeng.block.misc.BlockNetworkEmitter;
import appeng.block.misc.BlockPartitionEditor;
import appeng.block.misc.BlockQuartzCrystalizer;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.misc.BlockTinyTNT;
import appeng.block.misc.BlockVibrationChamber;
import appeng.block.networking.BlockCableBus;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockCreativeEnergyCell;
import appeng.block.networking.BlockDenseEnergyCell;
import appeng.block.networking.BlockEnergyAcceptor;
import appeng.block.networking.BlockEnergyCell;
import appeng.block.networking.BlockWireless;
import appeng.block.qnb.BlockQuantumLinkChamber;
import appeng.block.qnb.BlockQuantumRing;
import appeng.block.solids.BlockMatrixFrame;
import appeng.block.solids.BlockQuartz;
import appeng.block.solids.BlockQuartzChiseled;
import appeng.block.solids.BlockQuartzGlass;
import appeng.block.solids.BlockQuartzLamp;
import appeng.block.solids.BlockQuartzPillar;
import appeng.block.solids.OreQuartz;
import appeng.block.solids.OreQuartzCharged;
import appeng.block.spatial.BlockSpatialIOPort;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.core.features.AEFeature;
import appeng.core.features.IAEFeature;
import appeng.core.features.registries.entries.BasicCellHandler;
import appeng.core.features.registries.entries.CreativeCellHandler;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.debug.ToolDebugCard;
import appeng.helpers.AETrading;
import appeng.helpers.PartPlacement;
import appeng.helpers.QuartzWorldGen;
import appeng.helpers.TickHandler;
import appeng.items.ItemEncodedPattern;
import appeng.items.materials.ItemMaterial;
import appeng.items.materials.MaterialType;
import appeng.items.parts.ItemFacade;
import appeng.items.parts.ItemPart;
import appeng.items.parts.PartType;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.items.tools.ToolMemoryCard;
import appeng.items.tools.powered.ToolChargedStaff;
import appeng.items.tools.powered.ToolEntropyManipulator;
import appeng.items.tools.powered.ToolMassCannon;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.items.tools.quartz.ToolQuartzAxe;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.items.tools.quartz.ToolQuartzHoe;
import appeng.items.tools.quartz.ToolQuartzPickaxe;
import appeng.items.tools.quartz.ToolQuartzSpade;
import appeng.items.tools.quartz.ToolQuartzSword;
import appeng.items.tools.quartz.ToolQuartzWrench;
import appeng.me.cache.EnergyGridCache;
import appeng.me.cache.GridStorageCache;
import appeng.me.cache.P2PCache;
import appeng.me.cache.PathGridCache;
import appeng.me.cache.SpatialPylonCache;
import appeng.me.cache.TickManagerCache;
import appeng.me.storage.AEExternalHandler;
import appeng.recipes.ores.OreDictionaryHandler;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;

public class Registration
{

	final public static Registration instance = new Registration();

	public BiomeGenBase storageBiome;

	private Registration() {
	}

	final private Multimap<AEFeature, Class> featuresToEntities = ArrayListMultimap.create();

	public void PreInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register( OreDictionaryHandler.instance );

		Items items = AEApi.instance().items();
		Materials materials = AEApi.instance().materials();
		Parts parts = AEApi.instance().parts();
		Blocks blocks = AEApi.instance().blocks();

		Class materialClass = materials.getClass();
		for (MaterialType mat : MaterialType.values())
		{
			try
			{
				Field f = materialClass.getField( "material" + mat.name() );
				f.set( materials, addFeature( ItemMaterial.class, mat ) );
			}
			catch (Throwable err)
			{
				AELog.severe( "Error creating material: " + mat.name() );
				throw new RuntimeException( err );
			}
		}

		Class partClass = parts.getClass();
		for (PartType part : PartType.values())
		{
			try
			{
				Field f = partClass.getField( "part" + part.name() );
				f.set( parts, addFeature( ItemPart.class, part ) );
			}
			catch (Throwable err)
			{
				AELog.severe( "Error creating part: " + part.name() );
				throw new RuntimeException( err );
			}
		}

		// very important block!
		blocks.blockMultiPart = addFeature( BlockCableBus.class );

		blocks.blockQuartzOre = addFeature( OreQuartz.class );
		blocks.blockQuartzOreCharged = addFeature( OreQuartzCharged.class );
		blocks.blockMatrixFrame = addFeature( BlockMatrixFrame.class );
		blocks.blockQuartz = addFeature( BlockQuartz.class );
		blocks.blockQuartzGlass = addFeature( BlockQuartzGlass.class );
		blocks.blockQuartzVibrantGlass = addFeature( BlockQuartzLamp.class );
		blocks.blockQuartzPiller = addFeature( BlockQuartzPillar.class );
		blocks.blockQuartzChiseled = addFeature( BlockQuartzChiseled.class );
		blocks.blockQuartzTorch = addFeature( BlockQuartzTorch.class );
		blocks.blockCharger = addFeature( BlockCharger.class );

		blocks.blockGrindStone = addFeature( BlockGrinder.class );
		blocks.blockCrankHandle = addFeature( BlockCrank.class );
		blocks.blockInscriber = addFeature( BlockInscriber.class );
		blocks.blockWireless = addFeature( BlockWireless.class );
		blocks.blockTinyTNT = addFeature( BlockTinyTNT.class );

		blocks.blockQuartzCrystalizer = addFeature( BlockQuartzCrystalizer.class );
		blocks.blockNetworkEmitter = addFeature( BlockNetworkEmitter.class );

		blocks.blockPatternProvider = addFeature( BlockPatternProvider.class );
		blocks.blockAssemblerFieldWall = addFeature( BlockContainmentWall.class );
		blocks.blockHeatVent = addFeature( BlockHeatVent.class );
		blocks.blockCraftingCPU = addFeature( BlockCraftingAccelerator.class );

		blocks.blockQuantumRing = addFeature( BlockQuantumRing.class );
		blocks.blockQuantumLink = addFeature( BlockQuantumLinkChamber.class );

		blocks.blockSpatialPylon = addFeature( BlockSpatialPylon.class );
		blocks.blockSpatialIOPort = addFeature( BlockSpatialIOPort.class );

		blocks.blockController = addFeature( BlockController.class );
		blocks.blockDrive = addFeature( BlockDrive.class );
		blocks.blockChest = addFeature( BlockChest.class );
		blocks.blockInterface = addFeature( BlockInterface.class );
		blocks.blockPartitioner = addFeature( BlockPartitionEditor.class );
		blocks.blockIOPort = addFeature( BlockIOPort.class );
		blocks.blockCondenser = addFeature( BlockCondenser.class );
		blocks.blockEnergyAcceptor = addFeature( BlockEnergyAcceptor.class );
		blocks.blockVibrationChamber = addFeature( BlockVibrationChamber.class );

		blocks.blockEnergyCell = addFeature( BlockEnergyCell.class );
		blocks.blockEnergyCellDense = addFeature( BlockDenseEnergyCell.class );
		blocks.blockEnergyCellCreative = addFeature( BlockCreativeEnergyCell.class );

		items.itemEncodedAsemblerPattern = addFeature( ItemEncodedPattern.class );

		items.itemCellCreative = addFeature( ItemCreativeStorageCell.class );

		items.itemCell1k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell1kPart, 1 );
		items.itemCell4k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell4kPart, 4 );
		items.itemCell16k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell16kPart, 16 );
		items.itemCell64k = addFeature( ItemBasicStorageCell.class, MaterialType.Cell64kPart, 64 );

		items.itemSpatialCell2 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell2SpatialPart, 2 );
		items.itemSpatialCell16 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell16SpatialPart, 16 );
		items.itemSpatialCell128 = addFeature( ItemSpatialStorageCell.class, MaterialType.Cell128SpatialPart, 128 );

		items.itemCertusQuartzKnife = addFeature( ToolQuartzCuttingKnife.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzWrench = addFeature( ToolQuartzWrench.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzAxe = addFeature( ToolQuartzAxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzHoe = addFeature( ToolQuartzHoe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzPick = addFeature( ToolQuartzPickaxe.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzShovel = addFeature( ToolQuartzSpade.class, AEFeature.CertusQuartzTools );
		items.itemCertusQuartzSword = addFeature( ToolQuartzSword.class, AEFeature.CertusQuartzTools );

		items.itemNetherQuartzKnife = addFeature( ToolQuartzCuttingKnife.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzWrench = addFeature( ToolQuartzWrench.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzAxe = addFeature( ToolQuartzAxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzHoe = addFeature( ToolQuartzHoe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzPick = addFeature( ToolQuartzPickaxe.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzShovel = addFeature( ToolQuartzSpade.class, AEFeature.NetherQuartzTools );
		items.itemNetherQuartzSword = addFeature( ToolQuartzSword.class, AEFeature.NetherQuartzTools );

		items.itemMassCannon = addFeature( ToolMassCannon.class );
		items.itemMemoryCard = addFeature( ToolMemoryCard.class );
		items.itemChargedStaff = addFeature( ToolChargedStaff.class );
		items.itemEntropyManipulator = addFeature( ToolEntropyManipulator.class );
		items.itemWirelessTerminal = addFeature( ToolWirelessTerminal.class );

		items.itemFacade = addFeature( ItemFacade.class );

		addFeature( ToolDebugCard.class );
	}

	private AEItemDefinition addFeature(Class c, Object... Args)
	{

		try
		{
			java.lang.reflect.Constructor[] con = c.getConstructors();
			Object obj = null;

			for (Constructor conItem : con)
			{
				if ( conItem.getParameterTypes().length == Args.length )
					obj = conItem.newInstance( Args );
			}

			if ( obj instanceof IAEFeature )
			{
				IAEFeature feature = (IAEFeature) obj;

				for (AEFeature f : feature.feature().getFeatures())
					featuresToEntities.put( f, c );

				feature.feature().register();

				return feature.feature();
			}
			else if ( obj == null )
				throw new RuntimeException( "No valid constructor found." );
			else
				throw new RuntimeException( "Non AE Feature Registered" );

		}
		catch (Throwable e)
		{
			AELog.severe( "Error with Feature: " + c.getName() );
			throw new RuntimeException( e );
		}
	}

	public void Init(FMLInitializationEvent event)
	{
		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerIEnergySink", IEnergySink.class );
		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerISidedInventory", ISidedInventory.class );
		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerIPowerEmitter", IPowerEmitter.class );
		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerIPowerReceptor", IPowerReceptor.class );
		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerIFluidHandler", IFluidHandler.class );

		TickRegistry.registerTickHandler( TickHandler.instance, Side.SERVER );
		TickRegistry.registerTickHandler( TickHandler.instance, Side.CLIENT );

		MinecraftForge.EVENT_BUS.register( TickHandler.instance );
		MinecraftForge.EVENT_BUS.register( new PartPlacement() );

		AEApi.instance().registries().gridCache().registerGridCache( ITickManager.class, TickManagerCache.class );
		AEApi.instance().registries().gridCache().registerGridCache( IEnergyGrid.class, EnergyGridCache.class );
		AEApi.instance().registries().gridCache().registerGridCache( IPathingGrid.class, PathGridCache.class );
		AEApi.instance().registries().gridCache().registerGridCache( IStorageGrid.class, GridStorageCache.class );
		AEApi.instance().registries().gridCache().registerGridCache( P2PCache.class, P2PCache.class );
		AEApi.instance().registries().gridCache().registerGridCache( ISpatialCache.class, SpatialPylonCache.class );

		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new AEExternalHandler() );

		AEApi.instance().registries().cell().addCellHandler( new BasicCellHandler() );
		AEApi.instance().registries().cell().addCellHandler( new CreativeCellHandler() );

		NetworkRegistry.instance().registerGuiHandler( AppEng.instance, GuiBridge.GUI_Handler );

	}

	public void PostInit(FMLPostInitializationEvent event)
	{
		// add to localizaiton..
		PlayerMessages.values();
		GuiText.values();

		Api.instance.partHelper.initFMPSupport();
		((BlockCableBus) AEApi.instance().blocks().blockMultiPart.block()).setupTile();

		if ( Configuration.instance.isFeatureEnabled( AEFeature.ChestLoot ) )
		{
			ChestGenHooks d = ChestGenHooks.getInfo( ChestGenHooks.MINESHAFT_CORRIDOR );
			d.addItem( new WeightedRandomChestContent( AEApi.instance().materials().materialCertusQuartzCrystal.stack( 1 ), 1, 4, 2 ) );
			d.addItem( new WeightedRandomChestContent( AEApi.instance().materials().materialCertusQuartzDust.stack( 1 ), 1, 4, 2 ) );
		}

		// add villager trading to black smiths for a few basic materials
		if ( Configuration.instance.isFeatureEnabled( AEFeature.VillagerTrading ) )
			VillagerRegistry.instance().registerVillageTradeHandler( 3, new AETrading() );

		if ( Configuration.instance.isFeatureEnabled( AEFeature.CertusQuartzWorldGen ) )
			GameRegistry.registerWorldGenerator( new QuartzWorldGen() );

	}
}