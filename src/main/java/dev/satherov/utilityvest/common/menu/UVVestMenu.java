package dev.satherov.utilityvest.common.menu;


import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
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
    protected UVVestCapability capability;
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

        if (handler instanceof UVVestCapability cap) {
            this.capability = cap;
        }

        int yOffset = (rows - 4) * 18;

        addVestSlots(inventory, capability, yOffset);
    }

    private static ItemStack cloneStack(ItemStack stack, int size) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack copy = stack.copy();
        copy.setCount(size);
        return copy;
    }

    protected void addVestSlots(Inventory inventory, UVVestCapability handler, int yOffset) {

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
    public boolean moveItemStackTo(ItemStack stack, int start, int length, boolean reverse) {
        boolean successful = false;
        int i = !reverse ? start : length - 1;
        int iterOrder = !reverse ? 1 : -1;

        if (stack.isStackable()) {

            while (!stack.isEmpty() && (!reverse && i < length || reverse && i >= start)) {
                Slot slot = slots.get(i);
                ItemStack existingStack = slot.getItem();

                if (!existingStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, existingStack)) {
                    int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());

                    if (slot.mayPlace(cloneStack(stack, 1))) {
                        int remaining = maxStack - existingStack.getCount();
                        int toTransfer = Math.min(remaining, stack.getCount());

                        if (toTransfer > 0) {
                            stack.shrink(toTransfer);
                            existingStack.grow(toTransfer);
                            slot.set(existingStack);
                            successful = true;
                        }
                    }
                }
                i += iterOrder;
            }
        }

        if (!stack.isEmpty()) {
            i = !reverse ? start : length - 1;
            while (!stack.isEmpty() && (!reverse && i < length || reverse && i >= start)) {
                Slot slot = slots.get(i);
                ItemStack existingStack = slot.getItem();

                if (existingStack.isEmpty() && slot.mayPlace(cloneStack(stack, 1))) {
                    int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
                    int toTransfer = Math.min(maxStack, stack.getCount());

                    ItemStack newStack = stack.copy();
                    newStack.setCount(toTransfer);
                    slot.set(newStack);
                    stack.shrink(toTransfer);
                    successful = true;
                }
                i += iterOrder;
            }
        }

        return successful;
    }

    public UVVestCapability getCapability() {
        return capability;
    }

    public int getRows() {
        return rows;
    }

    @Override
    public boolean stillValid(Player player) {
        return !UVVestItem.getVest(player, true).isEmpty() && !player.isSpectator();
    }
}
