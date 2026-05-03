package com.itemglow;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class ItemGlowModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder b = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Item Glow Pro Settings"));
            ConfigEntryBuilder eb = b.entryBuilder();
            ItemGlowConfig c = ItemGlowMod.config;

            ConfigCategory behavior = b.getOrCreateCategory(Text.literal("HUD Behavior"));
            behavior.addEntry(eb.startBooleanToggle(Text.literal("Always Visible"), c.constantVisibility).setSaveConsumer(v -> c.constantVisibility = v).build());
            behavior.addEntry(eb.startFloatField(Text.literal("Fade Delay"), c.fadeDelaySeconds).setDefaultValue(4.0f).setSaveConsumer(v -> c.fadeDelaySeconds = v).build());
            behavior.addEntry(eb.startEnumSelector(Text.literal("Anchor Point"), ItemGlowConfig.Anchor.class, c.anchor).setSaveConsumer(v -> c.anchor = v).build());
            behavior.addEntry(eb.startIntField(Text.literal("Vertical Offset"), c.heightOffset).setDefaultValue(0).setSaveConsumer(v -> c.heightOffset = v).build());
            behavior.addEntry(eb.startIntField(Text.literal("Side Offset"), c.sideOffset).setSaveConsumer(v -> c.sideOffset = v).build());
            behavior.addEntry(eb.startFloatField(Text.literal("Scale"), c.scale).setDefaultValue(1.0f).setMin(0.1f).setMax(3.0f).setSaveConsumer(v -> c.scale = v).build());
            behavior.addEntry(eb.startBooleanToggle(Text.literal("Enable Slide-in Entrance"), c.enableSlideIn).setSaveConsumer(v -> c.enableSlideIn = v).build());
            behavior.addEntry(eb.startBooleanToggle(Text.literal("Enable Smooth Bar Transitions"), c.enableSmoothBars).setSaveConsumer(v -> c.enableSmoothBars = v).build());
            behavior.addEntry(eb.startBooleanToggle(Text.literal("Enable Item Switch Scroll"), c.enableSwitchAnimation).setSaveConsumer(v -> c.enableSwitchAnimation = v).build());

            ConfigCategory armor = b.getOrCreateCategory(Text.literal("Armor HUD"));
            armor.addEntry(eb.startBooleanToggle(Text.literal("Show Armor Hud"), c.showArmorHud).setSaveConsumer(v -> c.showArmorHud = v).build());
            armor.addEntry(eb.startEnumSelector(Text.literal("Anchor Point"), ItemGlowConfig.ArmorAnchor.class, c.armorAnchor).setSaveConsumer(v -> c.armorAnchor = v).build());
            armor.addEntry(eb.startIntField(Text.literal("Vertical Offset"), c.armorHeightOffset).setSaveConsumer(v -> c.armorHeightOffset = v).build());
            armor.addEntry(eb.startIntField(Text.literal("Side Offset"), c.armorSideOffset).setSaveConsumer(v -> c.armorSideOffset = v).build());
            armor.addEntry(eb.startFloatField(Text.literal("Armor Scale"), c.armorScale).setDefaultValue(1.0f).setSaveConsumer(v -> c.armorScale = v).build());

            ConfigCategory style = b.getOrCreateCategory(Text.literal("HUD Style"));
            style.addEntry(eb.startEnumSelector(Text.literal("Durability Bar Style"), ItemGlowConfig.BarStyle.class, c.barStyle).setSaveConsumer(v -> c.barStyle = v).build());
            style.addEntry(eb.startIntField(Text.literal("Panel Width"), c.panelWidth).setDefaultValue(140).setSaveConsumer(v -> c.panelWidth = v).build());
            style.addEntry(eb.startIntField(Text.literal("Panel Height"), c.panelHeight).setDefaultValue(48).setSaveConsumer(v -> c.panelHeight = v).build());
            style.addEntry(eb.startFloatField(Text.literal("Background Opacity"), c.bgOpacity).setDefaultValue(0.7f).setMin(0f).setMax(1f).setSaveConsumer(v -> c.bgOpacity = v).build());
            style.addEntry(eb.startColorField(Text.literal("Accent Bar Color"), c.accentColor).setDefaultValue(0x3D92FF).setSaveConsumer(v -> c.accentColor = v).build());
            style.addEntry(eb.startColorField(Text.literal("Text Color"), c.textColor).setDefaultValue(0xFFFFFF).setSaveConsumer(v -> c.textColor = v).build());
            style.addEntry(eb.startBooleanToggle(Text.literal("Use Custom Name Color"), c.forceNameColor).setSaveConsumer(v -> c.forceNameColor = v).build());
            style.addEntry(eb.startColorField(Text.literal("Name Color Override"), c.nameColorOverride).setDefaultValue(0xFFFFFF).setSaveConsumer(v -> c.nameColorOverride = v).build());

            ConfigCategory features = b.getOrCreateCategory(Text.literal("HUD Features"));
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Icon"), c.showIcon).setSaveConsumer(v -> c.showIcon = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Off-hand"), c.showOffHand).setSaveConsumer(v -> c.showOffHand = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Enchantments"), c.showEnchantments).setSaveConsumer(v -> c.showEnchantments = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Durability Flash Warning"), c.durabilityFlash).setSaveConsumer(v -> c.durabilityFlash = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Cooldown Arc"), c.showCooldownArc).setSaveConsumer(v -> c.showCooldownArc = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show API Subtitles"), c.showApiLines).setSaveConsumer(v -> c.showApiLines = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Biome & Dimension"), c.showBiomeDimension).setSaveConsumer(v -> c.showBiomeDimension = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Food Nutrition"), c.showFoodPreview).setSaveConsumer(v -> c.showFoodPreview = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Potion Effects"), c.showPotionPreview).setSaveConsumer(v -> c.showPotionPreview = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Repair Cost Warning"), c.showRepairCost).setSaveConsumer(v -> c.showRepairCost = v).build());
            features.addEntry(eb.startIntField(Text.literal("Repair Threshold"), c.repairCostThreshold).setDefaultValue(20).setSaveConsumer(v -> c.repairCostThreshold = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Detailed Tooltips"), c.showDetailedInfo).setSaveConsumer(v -> c.showDetailedInfo = v).build()); // NEW
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Durability Bar"), c.showDurabilityBar).setSaveConsumer(v -> c.showDurabilityBar = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Durability Numbers"), c.showDurabilityText).setSaveConsumer(v -> c.showDurabilityText = v).build());
            features.addEntry(eb.startBooleanToggle(Text.literal("Show Item Count"), c.showStackCount).setSaveConsumer(v -> c.showStackCount = v).build());

            b.setSavingRunnable(c::save);
            return b.build();
        };
    }
}