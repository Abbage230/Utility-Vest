package dev.satherov.utilityvest.client.input;

import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.common.menu.UVVestMenu;
import dev.satherov.utilityvest.core.lang.UVLanguage;
import dev.satherov.utilityvest.network.OpenVestPayload;
import dev.satherov.utilityvest.network.RestockPayload;
import dev.satherov.utilityvest.network.SaveLoadPayload;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

public class UVKeybindManager {

    public static final KeyMapping GUI_KEY = register(UVLanguage.KEY_GUI, GLFW.GLFW_KEY_R);
    public static final KeyMapping RESTOCK = register(UVLanguage.KEY_RESTOCK, GLFW.GLFW_KEY_X);
    public static final KeyMapping LOAD = register(UVLanguage.KEY_LOAD, GLFW.GLFW_KEY_LEFT_CONTROL);
    public static final KeyMapping SAVE = register(UVLanguage.KEY_SAVE, GLFW.GLFW_KEY_LEFT_ALT);

    private static KeyMapping register(UVLanguage key, int keyCode) {
        return new KeyMapping(key.getTranslationKey(), keyCode, UVLanguage.KEY_CATEGORY.getTranslationKey());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (RESTOCK.consumeClick()) {
            PacketDistributor.sendToServer(new RestockPayload(player.isCrouching()));
        }

        if (GUI_KEY.consumeClick()) {
            if (player.containerMenu instanceof UVVestMenu) {
                player.closeContainer();
                return;
            } else {
                ItemStack stack = UVVestItem.getVest(player, true);
                if (!stack.isEmpty() && stack.getItem() instanceof UVVestItem vest) {
                    PacketDistributor.sendToServer(new OpenVestPayload(player.isCrouching(), vest.getMaxBanks()));
                }
            }
        }

        if (SAVE.isDown()) {
            for (int i = 0; i < 5; i++) {
                if (event.getKey() == GLFW.GLFW_KEY_1 + i && event.getAction() == GLFW.GLFW_PRESS) {
                    PacketDistributor.sendToServer(new SaveLoadPayload(true, i));
                    return;
                }
            }
        }

        if (LOAD.isDown()) {
            for (int i = 0; i < 5; i++) {
                if (event.getKey() == GLFW.GLFW_KEY_1 + i && event.getAction() == GLFW.GLFW_PRESS) {
                    PacketDistributor.sendToServer(new SaveLoadPayload(false, i));
                    return;
                }
            }
        }
    }
}

