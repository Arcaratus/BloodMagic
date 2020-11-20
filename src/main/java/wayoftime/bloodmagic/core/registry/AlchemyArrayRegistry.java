package wayoftime.bloodmagic.core.registry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import wayoftime.bloodmagic.impl.BloodMagicAPI;
import wayoftime.bloodmagic.recipe.RecipeAlchemyArray;
import wayoftime.bloodmagic.common.alchemyarray.AlchemyArrayEffect;
import wayoftime.bloodmagic.common.alchemyarray.AlchemyArrayEffectCrafting;

public class AlchemyArrayRegistry
{
	public static Map<ResourceLocation, AlchemyArrayEffect> effectMap = new HashMap<ResourceLocation, AlchemyArrayEffect>();

	public static boolean registerEffect(ResourceLocation rl, AlchemyArrayEffect effect)
	{
		boolean hadKey = effectMap.containsKey(rl);

		effectMap.put(rl, effect);

		return hadKey;
	}

	public static AlchemyArrayEffect getEffect(World world, ResourceLocation rl, RecipeAlchemyArray recipe)
	{
		if (effectMap.containsKey(rl))
		{
			return effectMap.get(rl).getNewCopy();
		}

		if (!recipe.getOutput().isEmpty())
		{
			// Return a new instance of AlchemyEffectCrafting
			return new AlchemyArrayEffectCrafting(recipe.getOutput());
		}

		return null;
	}

	public static AlchemyArrayEffect getEffect(World world, ItemStack input, ItemStack catalyst)
	{
		Pair<Boolean, RecipeAlchemyArray> array = BloodMagicAPI.INSTANCE.getRecipeRegistrar().getAlchemyArray(world, input, catalyst);
		if (array == null || array.getRight() == null || !array.getLeft())
			return null;

		return getEffect(world, array.getRight().getId(), array.getRight());
	}
}
