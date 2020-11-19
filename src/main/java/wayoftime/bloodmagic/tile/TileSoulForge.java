package wayoftime.bloodmagic.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ObjectHolder;
import wayoftime.bloodmagic.api.event.BloodMagicCraftedEvent;
import wayoftime.bloodmagic.impl.BloodMagicAPI;
import wayoftime.bloodmagic.api.recipe.RecipeTartaricForge;
import wayoftime.bloodmagic.tile.container.ContainerSoulForge;
import wayoftime.bloodmagic.util.Constants;
import wayoftime.bloodmagic.api.will.EnumDemonWillType;
import wayoftime.bloodmagic.api.will.IDemonWill;
import wayoftime.bloodmagic.api.will.IDemonWillConduit;
import wayoftime.bloodmagic.api.will.IDemonWillGem;

public class TileSoulForge extends TileInventory
		implements ITickableTileEntity, INamedContainerProvider, IDemonWillConduit
{
	@ObjectHolder("bloodmagic:soulforge")
	public static TileEntityType<TileSoulForge> TYPE;

	public static final int ticksRequired = 100;
	public static final double worldWillTransferRate = 1;

	public static final int soulSlot = 4;
	public static final int outputSlot = 5;

	// Input slots are from 0 to 3.

	public int burnTime = 0;

	public TileSoulForge(TileEntityType<?> type)
	{
		super(type, 6, "soulforge");
	}

	public TileSoulForge()
	{
		this(TYPE);
	}

	@Override
	public void deserialize(CompoundNBT tag)
	{
		super.deserialize(tag);

		burnTime = tag.getInt(Constants.NBT.SOUL_FORGE_BURN);
	}

	@Override
	public CompoundNBT serialize(CompoundNBT tag)
	{
		super.serialize(tag);

		tag.putInt(Constants.NBT.SOUL_FORGE_BURN, burnTime);
		return tag;
	}

	public final IIntArray TileData = new IIntArray()
	{
		@Override
		public int get(int index)
		{
			switch (index)
			{
			case 0:
				return burnTime;
			case 1:
				return ticksRequired;
			case 2:
				return 0;
			default:
				throw new IllegalArgumentException("Invalid index: " + index);
			}
		}

		@Override
		public void set(int index, int value)
		{
			throw new IllegalStateException("Cannot set values through IIntArray");
		}

		@Override
		public int size()
		{
			return 3;
		}
	};

	@Override
	public void tick()
	{
		if (!hasSoulGemOrSoul())
		{
			burnTime = 0;
			return;
		}

		double soulsInGem = getWill(EnumDemonWillType.DEFAULT);

		List<ItemStack> inputList = new ArrayList<>();

		for (int i = 0; i < 4; i++) if (!getStackInSlot(i).isEmpty())
			inputList.add(getStackInSlot(i));

		RecipeTartaricForge recipe = BloodMagicAPI.INSTANCE.getRecipeRegistrar().getTartaricForge(world, inputList);
		if (recipe != null && (soulsInGem >= recipe.getMinimumSouls() || burnTime > 0))
		{
			if (canCraft(recipe))
			{
				burnTime++;

				if (burnTime == ticksRequired)
				{
					if (!getWorld().isRemote)
					{
						double requiredSouls = recipe.getSoulDrain();
						if (requiredSouls > 0)
						{
							if (!getWorld().isRemote && soulsInGem >= recipe.getMinimumSouls())
							{
								consumeSouls(EnumDemonWillType.DEFAULT, requiredSouls);
							}
						}

						if (!getWorld().isRemote && soulsInGem >= recipe.getMinimumSouls())
							craftItem(recipe);
					}

					burnTime = 0;
				} else if (burnTime > ticksRequired + 10)
				{
					burnTime = 0;
				}
			} else
			{
				burnTime = 0;
			}
		} else
		{
			burnTime = 0;
		}
	}

	private boolean canCraft(RecipeTartaricForge recipe)
	{
		if (recipe == null)
			return false;

		ItemStack currentOutputStack = getStackInSlot(outputSlot);
		if (recipe.getOutput().isEmpty())
			return false;
		if (currentOutputStack.isEmpty())
			return true;
		if (!currentOutputStack.isItemEqual(recipe.getOutput()))
			return false;
		int result = currentOutputStack.getCount() + recipe.getOutput().getCount();
		return result <= getInventoryStackLimit() && result <= currentOutputStack.getMaxStackSize();

	}

	public void craftItem(RecipeTartaricForge recipe)
	{
		if (this.canCraft(recipe))
		{
			ItemStack currentOutputStack = getStackInSlot(outputSlot);

			List<ItemStack> inputList = new ArrayList<>();
			for (int i = 0; i < 4; i++) if (!getStackInSlot(i).isEmpty())
				inputList.add(getStackInSlot(i).copy());

			BloodMagicCraftedEvent.SoulForge event = new BloodMagicCraftedEvent.SoulForge(recipe.getOutput().copy(), inputList.toArray(new ItemStack[0]));
			MinecraftForge.EVENT_BUS.post(event);

			if (currentOutputStack.isEmpty())
			{
				setInventorySlotContents(outputSlot, event.getOutput());
			} else if (ItemHandlerHelper.canItemStacksStack(currentOutputStack, event.getOutput()))
			{
				currentOutputStack.grow(event.getOutput().getCount());
			}

			consumeInventory();
		}
	}

	@Override
	public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_)
	{
		assert world != null;
		return new ContainerSoulForge(this, TileData, p_createMenu_1_, p_createMenu_2_);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new StringTextComponent("Hellfire Forge");
	}

	public boolean hasSoulGemOrSoul()
	{
		ItemStack soulStack = getStackInSlot(soulSlot);

		if (!soulStack.isEmpty())
		{
			if (soulStack.getItem() instanceof IDemonWill || soulStack.getItem() instanceof IDemonWillGem)
			{
				return true;
			}
		}

		return false;
	}

	public double getProgressForGui()
	{
		return ((double) burnTime) / ticksRequired;
	}

	public double getWill(EnumDemonWillType type)
	{
		ItemStack soulStack = getStackInSlot(soulSlot);

		if (soulStack != null)
		{
			if (soulStack.getItem() instanceof IDemonWill
					&& ((IDemonWill) soulStack.getItem()).getType(soulStack) == type)
			{
				IDemonWill soul = (IDemonWill) soulStack.getItem();
				return soul.getWill(type, soulStack);
			}

			if (soulStack.getItem() instanceof IDemonWillGem)
			{
				IDemonWillGem soul = (IDemonWillGem) soulStack.getItem();
				return soul.getWill(type, soulStack);
			}
		}

		return 0;
	}

	public double consumeSouls(EnumDemonWillType type, double requested)
	{
		ItemStack soulStack = getStackInSlot(soulSlot);

		if (soulStack != null)
		{
			if (soulStack.getItem() instanceof IDemonWill
					&& ((IDemonWill) soulStack.getItem()).getType(soulStack) == type)
			{
				IDemonWill soul = (IDemonWill) soulStack.getItem();
				double souls = soul.drainWill(type, soulStack, requested);
				if (soul.getWill(type, soulStack) <= 0)
				{
					setInventorySlotContents(soulSlot, ItemStack.EMPTY);
				}
				return souls;
			}

			if (soulStack.getItem() instanceof IDemonWillGem)
			{
				IDemonWillGem soul = (IDemonWillGem) soulStack.getItem();
				return soul.drainWill(type, soulStack, requested, true);
			}
		}

		return 0;
	}

	public void consumeInventory()
	{
		for (int i = 0; i < 4; i++)
		{
			ItemStack inputStack = getStackInSlot(i);
			if (!inputStack.isEmpty())
			{
				if (inputStack.getItem().hasContainerItem(inputStack))
				{
					setInventorySlotContents(i, inputStack.getItem().getContainerItem(inputStack));
					continue;
				}

				inputStack.shrink(1);
				if (inputStack.isEmpty())
				{
					setInventorySlotContents(i, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public int getWeight()
	{
		return 50;
	}

	@Override
	public double fillDemonWill(EnumDemonWillType type, double amount, boolean doFill)
	{
		if (amount <= 0)
		{
			return 0;
		}

		if (!canFill(type))
		{
			return 0;
		}

		ItemStack stack = this.getStackInSlot(soulSlot);
		if (stack.isEmpty() || !(stack.getItem() instanceof IDemonWillGem))
		{
			return 0;
		}

		IDemonWillGem willGem = (IDemonWillGem) stack.getItem();
		return willGem.fillWill(type, stack, amount, doFill);
	}

	@Override
	public double drainDemonWill(EnumDemonWillType type, double amount, boolean doDrain)
	{
		ItemStack stack = this.getStackInSlot(soulSlot);
		if (stack.isEmpty() || !(stack.getItem() instanceof IDemonWillGem))
		{
			return 0;
		}

		IDemonWillGem willGem = (IDemonWillGem) stack.getItem();

		double drained = amount;
		double current = willGem.getWill(type, stack);
		if (current < drained)
		{
			drained = current;
		}

		if (doDrain)
		{
			drained = willGem.drainWill(type, stack, drained, true);
		}

		return drained;
	}

	@Override
	public boolean canFill(EnumDemonWillType type)
	{
		return true;
	}

	@Override
	public boolean canDrain(EnumDemonWillType type)
	{
		return true;
	}

	@Override
	public double getCurrentWill(EnumDemonWillType type)
	{
		return 0;
	}
}
