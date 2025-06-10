package dev.satherov.utilityvest.datagen.assets;

import dev.satherov.utilityvest.UtilityVest;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.common.data.LanguageProvider;

import net.minecraft.data.PackOutput;

public class UVLanguageProvider extends LanguageProvider {

    public UVLanguageProvider(PackOutput output) {
        super(output, UtilityVest.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(UVLanguage.ITEM_GROUP, "Utility Vest");

        add(UVLanguage.NETWORK_SAVE_LOAD_FAILED, "Hotbar save / load payload failed: %s");
        add(UVLanguage.NETWORK_OPEN_MENU_FAILED, "Opening vest menu failed: %s");
        add(UVLanguage.NETWORK_RESTOCK_FAILED, "Restock payload failed: %s");

        add(UVLanguage.CONTAINER_UTILITY_VEST, "Utility Vest");
        add(UVLanguage.CONTAINER_FILTERS, "Filters");

        add(UVLanguage.TOOLTIP_VEST_FILTER, "Press Sneak + %s to open the filter menu");
        add(UVLanguage.TOOLTIP_VEST_INVENTORY, "Press %s to open the inventory menu");
        add(UVLanguage.TOOLTIP_VEST_HOTBAR, "Set filters via %s + a number key. Load filters via %s + a number key");
        add(UVLanguage.TOOLTIP_VEST_RESTOCK, "Press %s to insert items matching your filters into the vests inventory");

        add(UVLanguage.CHAT_LOADED, "Loaded Hotbar %s");
        add(UVLanguage.CHAT_SAVED, "Saved Hotbar %s");
        add(UVLanguage.CHAT_RESTOCKED, "Restocked vest inventory");

        add(UVRegistry.LEATHER_UTILITY_VEST.get(), "Leather Utility Vest");
        add(UVRegistry.IRON_UTILITY_VEST.get(), "Iron Utility Vest");
        add(UVRegistry.GOLD_UTILITY_VEST.get(), "Gold Utility Vest");
        add(UVRegistry.DIAMOND_UTILITY_VEST.get(), "Diamond Utility Vest");
        add(UVRegistry.NETHERITE_UTILITY_VEST.get(), "Netherite Utility Vest");

        add(UVLanguage.KEY_CATEGORY, "Utility Vest");
        add(UVLanguage.KEY_GUI, "Open Gui");
        add(UVLanguage.KEY_RESTOCK, "Restock");
        add(UVLanguage.KEY_LOAD, "Load Modifier");
        add(UVLanguage.KEY_SAVE, "Save Modifier");
    }

    private void add(UVLanguage lang, String translation) {
        add(lang.getTranslationKey(), translation);
    }
}
