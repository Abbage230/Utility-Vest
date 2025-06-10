package dev.satherov.utilityvest.common.menu;

import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

@NothingNull
public class UVInventoryMenu extends UVVestMenu {

    public UVInventoryMenu(int containerId, Inventory inventory, int rows) {
        super(getMenuProvider(rows), containerId, inventory, rows);
    }

    private static MenuType<?> getMenuProvider(int rows) {
        return switch (rows) {
            case 1 -> UVRegistry.INVENTORY_MENU_ONE.get();
            case 2 -> UVRegistry.INVENTORY_MENU_TWO.get();
            case 3 -> UVRegistry.INVENTORY_MENU_THREE.get();
            case 4 -> UVRegistry.INVENTORY_MENU_FOUR.get();
            case 5 -> UVRegistry.INVENTORY_MENU_FIVE.get();
            default -> throw new IllegalArgumentException("Invalid row count: " + rows);
        };
    }

    @Override
    protected void addVestSlots(Inventory inventory, IItemHandler handler, int yOffset) {

        // Vest Inventory
        for (int j = 0; j < this.rows; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new SlotItemHandler(handler, (this.rows * 9) + k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        super.addVestSlots(inventory, handler, yOffset);
    }
}
