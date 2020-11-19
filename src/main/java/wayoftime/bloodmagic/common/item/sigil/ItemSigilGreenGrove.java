package wayoftime.bloodmagic.common.item.sigil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import wayoftime.bloodmagic.api.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.util.helper.PlayerHelper;

public class ItemSigilGreenGrove extends ItemSigilToggleableBase
{
	public ItemSigilGreenGrove()
	{
		super("green_grove", 150);
	}

	@Override
	public boolean onSigilUse(ItemStack stack, PlayerEntity player, World world, BlockPos blockPos, Direction side, Vector3d vec)
	{
		if (PlayerHelper.isFakePlayer(player))
			return false;

		if (!world.isRemote && NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, SoulTicket.item(stack, world, player, getLpUsed())).isSuccess() && applyBonemeal(stack, world, blockPos, player))
		{
			world.playEvent(2005, blockPos, 0);
			return true;
		}

		return false;
	}

	@Override
	public void onSigilUpdate(ItemStack stack, World worldIn, PlayerEntity player, int itemSlot, boolean isSelected)
	{
		if (PlayerHelper.isFakePlayer(player))
			return;

		int range = 3;
		int verticalRange = 2;
		int posX = (int) Math.round(player.getPosX() - 0.5f);
		int posY = (int) player.getPosY();
		int posZ = (int) Math.round(player.getPosZ() - 0.5f);
		if (worldIn instanceof ServerWorld)
		{
			ServerWorld serverWorld = (ServerWorld) worldIn;
			for (int ix = posX - range; ix <= posX + range; ix++)
			{
				for (int iz = posZ - range; iz <= posZ + range; iz++)
				{
					for (int iy = posY - verticalRange; iy <= posY + verticalRange; iy++)
					{
						BlockPos blockPos = new BlockPos(ix, iy, iz);
						BlockState state = worldIn.getBlockState(blockPos);

//						if (!BloodMagicAPI.INSTANCE.getBlacklist().getGreenGrove().contains(state))
						{
							if (state.getBlock() instanceof IGrowable && state.getBlock() != Blocks.GRASS_BLOCK)
							{
								if (worldIn.rand.nextInt(50) == 0)
								{
									BlockState preBlockState = worldIn.getBlockState(blockPos);
									if (((IGrowable) state.getBlock()).canGrow(serverWorld, blockPos, preBlockState, worldIn.isRemote))
									{
										((IGrowable) state.getBlock()).grow(serverWorld, worldIn.rand, blockPos, state);

										BlockState newState = worldIn.getBlockState(blockPos);
										if (!newState.equals(preBlockState) && !worldIn.isRemote)
											worldIn.playEvent(2005, blockPos, 0);
									}
								}
							}
						}
					}
				}
			}
		}

	}

	private static boolean applyBonemeal(ItemStack stack, World worldIn, BlockPos pos, PlayerEntity player)
	{
		BlockState blockstate = worldIn.getBlockState(pos);
		int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, worldIn, pos, blockstate, stack);
		if (hook != 0)
			return hook > 0;
		if (blockstate.getBlock() instanceof IGrowable)
		{
			IGrowable igrowable = (IGrowable) blockstate.getBlock();
			if (igrowable.canGrow(worldIn, pos, blockstate, worldIn.isRemote))
			{
				if (worldIn instanceof ServerWorld)
				{
					if (igrowable.canUseBonemeal(worldIn, worldIn.rand, pos, blockstate))
					{
						igrowable.grow((ServerWorld) worldIn, worldIn.rand, pos, blockstate);
					}

				}

				return true;
			}
		}

		return false;
	}

}
