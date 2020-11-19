package wayoftime.bloodmagic.tile.container;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IntArray;
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;
import wayoftime.bloodmagic.tile.TileSoulForge;
import wayoftime.bloodmagic.api.will.IDemonWill;
import wayoftime.bloodmagic.api.will.IDemonWillGem;

public class ContainerSoulForge extends Container
{
	public final IInventory tileForge;
	public final IIntArray data;

//	public ContainerSoulForge(InventoryPlayer inventoryPlayer, IInventory tileForge)
//	{
//		this.tileForge = tileForge;
//
//	}

	public ContainerSoulForge(int windowId, PlayerInventory playerInventory, PacketBuffer extraData)
	{
		this((TileSoulForge) playerInventory.player.world.getTileEntity(extraData.readBlockPos()), new IntArray(5), windowId, playerInventory);
	}

	public ContainerSoulForge(@Nullable TileSoulForge tile, IIntArray data, int windowId, PlayerInventory playerInventory)
	{
		super(BloodMagicBlocks.SOUL_FORGE_CONTAINER.get(), windowId);
		this.tileForge = tile;
		this.setup(playerInventory, tile);
		this.data = data;
	}

	public void setup(PlayerInventory inventory, IInventory tileForge)
	{
		this.addSlot(new Slot(tileForge, 0, 8, 15));
		this.addSlot(new Slot(tileForge, 1, 80, 15));
		this.addSlot(new Slot(tileForge, 2, 8, 87));
		this.addSlot(new Slot(tileForge, 3, 80, 87));
		this.addSlot(new SlotSoul(tileForge, TileSoulForge.soulSlot, 152, 51));
		this.addSlot(new SlotOutput(tileForge, TileSoulForge.outputSlot, 44, 51));

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlot(new Slot(inventory, i, 8 + i * 18, 181));
		}
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 5)
			{
				if (!this.mergeItemStack(itemstack1, 6, 6 + 36, true))
				{
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(itemstack1, itemstack);
			} else if (index > 5)
			{
				if (itemstack1.getItem() instanceof IDemonWill || itemstack1.getItem() instanceof IDemonWillGem)
				{
					if (!this.mergeItemStack(itemstack1, 4, 5, false))
					{
						return ItemStack.EMPTY;
					}
				} else if (!this.mergeItemStack(itemstack1, 0, 4, false))
				{
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 6, 42, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.getCount() == 0)
			{
				slot.putStack(ItemStack.EMPTY);
			} else
			{
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return this.tileForge.isUsableByPlayer(playerIn);
	}

	private class SlotSoul extends Slot
	{
		public SlotSoul(IInventory inventory, int slotIndex, int x, int y)
		{
			super(inventory, slotIndex, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return itemStack.getItem() instanceof IDemonWillGem || itemStack.getItem() instanceof IDemonWill;
		}
	}

	private class SlotOutput extends Slot
	{
		public SlotOutput(IInventory inventory, int slotIndex, int x, int y)
		{
			super(inventory, slotIndex, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack stack)
		{
			return false;
		}
	}
}
