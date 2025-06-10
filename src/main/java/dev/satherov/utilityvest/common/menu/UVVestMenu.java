package dev.satherov.utilityvest.common.menu;


import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@NothingNull
public abstract class UVVestMenu extends AbstractContainerMenu {

    protected final int rows;
    protected UVVestItem.VestInventory inventory;
    protected ItemStack vestStack;

    public UVVestMenu(MenuType<?> menuType, int containerId, Inventory inventory, int rows) {
        super(menuType, containerId);
        ItemStack stack = UVVestItem.getVest(inventory, true);
        this.rows = rows;
        if (stack.isEmpty()) {
            inventory.player.closeContainer();
            return;
        }
        this.vestStack = stack;


        IItemHandler handler = vestStack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler == null) {
            inventory.player.closeContainer();
            return;
        }

        if (handler instanceof UVVestItem.VestInventory inv) {
            this.inventory = inv;
        }

        int yOffset = (rows - 4) * 18;

        // Vest Inventory
        addVestSlots(inventory, handler, yOffset);
    }

    protected void addVestSlots(Inventory inventory, IItemHandler handler, int yOffset) {

        // Player Inventory
        for (int inv = 0; inv < 3; inv++) {
            for (int j1 = 0; j1 < 9; j1++) {
                this.addSlot(new Slot(inventory, j1 + inv * 9 + 9, 8 + j1 * 18, 103 + inv * 18 + yOffset));
            }
        }

        // Hotbar
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            this.addSlot(new Slot(inventory, hotbar, 8 + hotbar * 18, 161 + yOffset));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotItem = slot.getItem();
            stack = slotItem.copy();

            if (stack.getItem() instanceof UVVestItem) {
                return ItemStack.EMPTY;
            }

            if (index < this.rows * 9) {
                if (!this.moveItemStackTo(slotItem, this.rows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotItem, 0, this.rows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (slotItem.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotItem.getCount() == stack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotItem);
        }

        return stack;
    }

    public UVVestItem.VestInventory getInventory() {
        return inventory;
    }

    public int getRows() {
        return rows;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return !UVVestItem.getVest(player, true).isEmpty() && !player.isSpectator();
    }
}
