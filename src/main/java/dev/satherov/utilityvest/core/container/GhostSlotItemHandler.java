package dev.satherov.utilityvest.core.container;

import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@NothingNull
public class GhostSlotItemHandler extends SlotItemHandler {

    private boolean ghost = false;

    public GhostSlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }

    @Override
    public ItemStack remove(int amount) {
        if (ghost) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = super.remove(amount);
        if (stack.isEmpty()) {
            setGhost(true);
            this.set(stack.copyWithCount(1));
        }
        return stack;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !ghost && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return !ghost && super.mayPickup(player);
    }
}
