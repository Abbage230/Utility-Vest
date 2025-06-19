package dev.satherov.utilityvest.common.menu;


import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@NothingNull
public class UVFilterMenu extends UVVestMenu {

    public UVFilterMenu(int containerId, Inventory inventory, int rows) {
        super(getMenuProvider(rows), containerId, inventory, rows);
    }

    private static MenuType<?> getMenuProvider(int rows) {
        return switch (rows) {
            case 1 -> UVRegistry.FILTER_MENU_ONE.get();
            case 2 -> UVRegistry.FILTER_MENU_TWO.get();
            case 3 -> UVRegistry.FILTER_MENU_THREE.get();
            case 4 -> UVRegistry.FILTER_MENU_FOUR.get();
            case 5 -> UVRegistry.FILTER_MENU_FIVE.get();
            default -> throw new IllegalArgumentException("Invalid row count: " + rows);
        };
    }

    @Override
    protected void addVestSlots(Inventory inventory, UVVestCapability handler, int yOffset) {

        // Filter Slots
        for (int j = 0; j < this.rows; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new SlotItemHandler(handler.filters, k + j * 9, 8 + k * 18, 18 + j * 18) {

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }

                    @Override
                    public void onTake(Player player, ItemStack stack) {
                    }

                    @Override
                    public ItemStack remove(int amount) {
                        return ItemStack.EMPTY;
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }

                });
            }
        }

        super.addVestSlots(inventory, handler, yOffset);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < (this.rows * 9)) {
            final Slot slot = this.slots.get(slotId);

            if (clickType == ClickType.PICKUP || clickType == ClickType.PICKUP_ALL || clickType == ClickType.SWAP) {
                final ItemStack stack = this.getCarried();

                if (stack.getCount() > 0) {
                    slot.set(stack.copy());
                } else if (slot.getItem().getCount() > 0) {
                    slot.set(ItemStack.EMPTY);
                }
                return;
            }

            if (clickType == ClickType.QUICK_MOVE) {
                slot.set(ItemStack.EMPTY);
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }
}
