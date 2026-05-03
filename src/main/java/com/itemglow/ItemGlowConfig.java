package com.itemglow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ItemGlowConfig {
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "itemglow.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public enum Anchor { ABOVE_HOTBAR, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum ArmorAnchor { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
    public enum BarStyle { SOLID, SEGMENTED, HEART }

    // Behavior
    public float fadeDelaySeconds = 4.0f;
    public boolean constantVisibility = false;
    public Anchor anchor = Anchor.ABOVE_HOTBAR;
    public int heightOffset = 0;
    public int sideOffset = 10;
    public float scale = 1.0f;
    public boolean enableSlideIn = true;
    public boolean enableSmoothBars = true;
    public boolean enableSwitchAnimation = true;

    // Armor
    public boolean showArmorHud = true;
    public ArmorAnchor armorAnchor = ArmorAnchor.BOTTOM_LEFT;
    public int armorHeightOffset = 40;
    public int armorSideOffset = 5;
    public float armorScale = 1.0f;

    // Style
    public BarStyle barStyle = BarStyle.SOLID;
    public int panelWidth = 140;
    public int panelHeight = 48;
    public float bgOpacity = 0.7f;
    public int accentColor = 0x3D92FF;
    public int textColor = 0xFFFFFF;
    public boolean forceNameColor = false;
    public int nameColorOverride = 0xFFFFFF;

    // Features
    public boolean showIcon = true;
    public boolean showOffHand = true;
    public boolean showEnchantments = true;
    public boolean durabilityFlash = true;
    public boolean showDurabilityBar = true;
    public boolean showDurabilityText = true;
    public boolean showStackCount = true;
    public boolean showCooldownArc = true;
    public boolean showApiLines = true;
    public boolean showBiomeDimension = true;
    public boolean showFoodPreview = true;
    public boolean showPotionPreview = true;
    public boolean showRepairCost = true;
    public int repairCostThreshold = 20;

    // NEW: Detailed Content Toggle
    public boolean showDetailedInfo = true;

    public static ItemGlowConfig load() {
        if (!CONFIG_FILE.exists()) return new ItemGlowConfig().save();
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ItemGlowConfig c = GSON.fromJson(reader, ItemGlowConfig.class);
            return c != null ? c : new ItemGlowConfig().save();
        } catch (Exception e) {
            return new ItemGlowConfig().save();
        }
    }

    public ItemGlowConfig save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception ignored) {}
        return this;
    }
}