package wayoftime.bloodmagic.ritual;

import net.minecraft.block.Blocks;
import wayoftime.bloodmagic.ritual.harvest.HarvestHandlerPlantable;
import wayoftime.bloodmagic.ritual.harvest.HarvestHandlerStem;
import wayoftime.bloodmagic.ritual.harvest.HarvestHandlerTall;
import wayoftime.bloodmagic.ritual.harvest.HarvestRegistry;

public class ModRituals
{
	public static void initHarvestHandlers()
	{
		HarvestRegistry.registerRangeAmplifier(Blocks.DIAMOND_BLOCK.getDefaultState(), 15);
		HarvestRegistry.registerRangeAmplifier(Blocks.GOLD_BLOCK.getDefaultState(), 10);
		HarvestRegistry.registerRangeAmplifier(Blocks.IRON_BLOCK.getDefaultState(), 6);

		HarvestRegistry.registerHandler(new HarvestHandlerPlantable());
		HarvestRegistry.registerHandler(new HarvestHandlerTall());
		HarvestRegistry.registerHandler(new HarvestHandlerStem());
	}
//
//    public static void initCuttingFluids() {
//        CrushingRegistry.registerCuttingFluid(new CrushingHandlerCuttingFluid(ItemCuttingFluid.FluidType.BASIC.getStack(), 250, 0.5));
//        CrushingRegistry.registerCuttingFluid(new CrushingHandlerCuttingFluid(ItemCuttingFluid.FluidType.EXPLOSIVE.getStack(), 25, 0.05));
//    }
}
