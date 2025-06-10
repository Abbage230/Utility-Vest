package dev.satherov.utilityvest.common.item;

import dev.satherov.utilityvest.client.input.UVKeybindManager;
import dev.satherov.utilityvest.common.menu.UVFilterMenu;
import dev.satherov.utilityvest.common.menu.UVInventoryMenu;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Optional;

@NothingNull
public class UVVestItem extends Item {

    private final int maxBanks;

    public UVVestItem(Properties props, int maxBanks) {
        super(props.stacksTo(1));
        this.maxBanks = maxBanks;
    }

    public static ItemStack getVest(Player player, boolean checkHand) {
        return getVest(player.getInventory(), checkHand);
    }

    public static ItemStack getVest(Inventory inventory, boolean checkHand) {
        Player player = inventory.player;
        if (checkHand && player.getMainHandItem().getItem() instanceof UVVestItem) {
            return player.getMainHandItem();
        } else if (checkHand && player.getOffhandItem().getItem() instanceof UVVestItem) {
            return player.getOffhandItem();
        } else {
            return CuriosApi.getCuriosInventory(player)
                    .flatMap(inv -> {
                        var handler = inv.getEquippedCurios();
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack s = handler.getStackInSlot(i);
                            if (s.getItem() instanceof UVVestItem) return Optional.of(s);
                        }
                        return Optional.empty();
                    }).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_FILTER.translate(UVKeybindManager.GUI_KEY.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_INVENTORY.translate(UVKeybindManager.GUI_KEY.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_HOTBAR.translate(UVKeybindManager.SAVE.getKey().getDisplayName(), UVKeybindManager.SAVE.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_RESTOCK.translate(UVKeybindManager.RESTOCK.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null) {
            if (player.isCrouching()) {
                player.openMenu(this.getFilterMenu());
            } else {
                player.openMenu(this.getInventoryMenu());
            }
            return InteractionResultHolder.success(stack);

        }
        return super.use(level, player, hand);
    }

    public int getMaxBanks() {
        return maxBanks;
    }

    public MenuProvider getInventoryMenu() {
        return new SimpleMenuProvider((id, inventory, player) -> new UVInventoryMenu(id, inventory, getMaxBanks()), UVLanguage.CONTAINER_UTILITY_VEST.translate());
    }

    public MenuProvider getFilterMenu() {
        return new SimpleMenuProvider((id, inventory, player) -> new UVFilterMenu(id, inventory, getMaxBanks()), UVLanguage.CONTAINER_FILTERS.translate());
    }

    public static class VestInventory extends ComponentItemHandler {

        private final int splitPoint;

        public VestInventory(ItemStack stack, UVVestItem vest) {
            super(stack, UVRegistry.VEST_INVENTORY.get(), (vest.getMaxBanks() * 18));
            this.splitPoint = this.getSlots() / 2;
            if (this.splitPoint % 9 != 0) {
                throw new IllegalStateException("Invalid split size: " + splitPoint + " (must be multiple of 9)");
            }
        }

        public void saveHotbar(Player player, int index) {
            NonNullList<ItemStack> hotbar = getHotbar(player.getInventory());
            for (int i = 0; i < 9; i++) {
                this.setStackInSlot(i + index * 9, hotbar.get(i).copy());
            }

            player.inventoryMenu.broadcastChanges();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }

            player.displayClientMessage(UVLanguage.CHAT_SAVED.translate(index).withStyle(ChatFormatting.GRAY), true);
        }

        public void loadHotbar(Player player, int index) {
            NonNullList<ItemStack> hotbar = getHotbar(player.getInventory());
            for (ItemStack stack : hotbar) {
                if (this.matchFilter(stack)) {
                    ItemStack overflow = this.insertWithOverflow(stack);
                    if (overflow.isEmpty()) continue;
                    if (!player.getInventory().add(overflow)) {
                        player.drop(overflow, false);
                    }
                } else {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
            }

            for (int i = 0; i < 9; i++) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }

            int slot = 0;
            for (ItemStack stack : getFilters(index)) {
                if (!stack.isEmpty()) {
                    player.getInventory().setItem(slot, buildStackFromItems(stack, new ContainerItemHandler(this)));
                }
                slot++;
            }

            player.inventoryMenu.broadcastChanges();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }

            player.displayClientMessage(UVLanguage.CHAT_LOADED.translate(index).withStyle(ChatFormatting.GRAY), true);
        }


        public void collectItems(Player player) {
            Inventory inv = player.getInventory();

            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty()) continue;

                if (this.matchFilter(stack)) {
                    inv.setItem(i, insertWithOverflow(stack));
                }
            }

            player.displayClientMessage(UVLanguage.CHAT_RESTOCKED.translate().withStyle(ChatFormatting.GRAY), true);
        }

        private ItemStack insertWithOverflow(ItemStack toInsert) {
            if (toInsert.isEmpty()) return ItemStack.EMPTY;
            ItemStack remainder = toInsert.copy();
            for (int i = splitPoint; i < this.getSlots(); i++) {
                ItemStack inSlot = this.getStackInSlot(i);
                if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, remainder)) {
                    remainder = this.insertItem(i, remainder, false);
                    if (remainder.isEmpty()) return ItemStack.EMPTY;
                }
            }

            for (int i = splitPoint; i < this.getSlots(); i++) {
                if (remainder.isEmpty()) break;
                if (this.getStackInSlot(i).isEmpty()) {
                    remainder = this.insertItem(i, remainder, false);
                }
            }

            return remainder;
        }

        private ItemStack buildStackFromItems(ItemStack template, IItemSourceHandler source) {
            if (template.isEmpty()) return ItemStack.EMPTY;

            ItemStack result = ItemStack.EMPTY;
            int neededAmount = template.getCount();

            for (int i = source.startIndex(); i < source.getSize(); i++) {
                ItemStack storedItem = source.getStackInSlot(i);
                if (storedItem.isEmpty()) continue;

                if (ItemStack.isSameItemSameComponents(template, storedItem)) {
                    if (result.isEmpty()) {
                        result = storedItem.copy();
                        int extracted = source.extractItem(i, Math.min(storedItem.getCount(), neededAmount));
                        result.setCount(extracted);
                    } else {
                        int remainingNeeded = neededAmount - result.getCount();
                        int extracted = source.extractItem(i, Math.min(storedItem.getCount(), remainingNeeded));
                        result.grow(extracted);
                    }

                    if (result.getCount() == neededAmount) {
                        return result;
                    }
                }
            }

            return result;
        }

        public boolean matchFilter(ItemStack stack) {
            return getFilters().stream().anyMatch(f -> ItemStack.isSameItemSameComponents(f, stack));
        }

        private NonNullList<ItemStack> getHotbar(Inventory inv) {
            return NonNullList.of(ItemStack.EMPTY,
                    inv.items.stream()
                            .limit(9)
                            .toArray(ItemStack[]::new));
        }

        public NonNullList<ItemStack> getItems() {
            return NonNullList.of(ItemStack.EMPTY,
                    this.getContents().stream()
                            .skip(splitPoint)
                            .toArray(ItemStack[]::new));
        }

        public NonNullList<ItemStack> getFilters(int index) {
            NonNullList<ItemStack> results = NonNullList.withSize(9, ItemStack.EMPTY);
            int startIndex = index * 9;

            for (int i = 0; i < 9; i++) {
                if (startIndex + i < splitPoint) {
                    results.set(i, getFilters().get(startIndex + i));
                }
            }

            return results;
        }

        public NonNullList<ItemStack> getFilters() {
            return NonNullList.of(ItemStack.EMPTY,
                    this.getContents().stream()
                            .limit(splitPoint)
                            .toArray(ItemStack[]::new));
        }

        private interface IItemSourceHandler {

            int startIndex();

            int getSize();

            ItemStack getStackInSlot(int slot);

            int extractItem(int slot, int amount);
        }

        private record ContainerItemHandler(VestInventory container) implements IItemSourceHandler {

            @Override
            public int startIndex() {
                return container.splitPoint;
            }

            @Override
            public int getSize() {
                return container.getSlots();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return container.getStackInSlot(slot);
            }

            @Override
            public int extractItem(int slot, int amount) {
                return container.extractItem(slot, amount, false).getCount();
            }
        }

        private record PlayerInventoryHandler(Inventory inventory) implements IItemSourceHandler {

            @Override
            public int startIndex() {
                return 0;
            }

            @Override
            public int getSize() {
                return inventory.getContainerSize();
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                return inventory.getItem(slot);
            }

            @Override
            public int extractItem(int slot, int amount) {
                return inventory.removeItem(slot, amount).getCount();
            }
        }
    }
}