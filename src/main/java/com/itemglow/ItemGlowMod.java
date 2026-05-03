package com.itemglow;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ItemGlowMod implements ClientModInitializer {
    public static ItemGlowConfig config = ItemGlowConfig.load();
    public static final List<ItemGlowAPI.ItemGlowProvider> API_PROVIDERS = new ArrayList<>();

    private static final Identifier FOOD_FULL = Identifier.ofVanilla("hud/food_full");
    private static final Identifier FOOD_HALF = Identifier.ofVanilla("hud/food_half");

    private static ItemStack mainStack = ItemStack.EMPTY;
    private static ItemStack outgoingStack = ItemStack.EMPTY;
    private static float mainTimer = 0f;
    private static float switchProgress = 1.0f;
    private static float visualMainPct = -1.0f;
    private static float outgoingOpacityMult = 0f;
    private static ItemStack offStack = ItemStack.EMPTY;
    private static float offTimer = 0f;
    private static float mainEquipAge = 0f;
    private static float offEquipAge = 0f;
    private static float outgoingEquipAge = 0f;

    public static KeyBinding armorPeekKey;



    @Override
    public void onInitializeClient() {
        armorPeekKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.itemglow.armor_peek", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.itemglow"));

        com.itemglow.api.ItemGlowApi.HudPanel mainPanel = new com.itemglow.api.ItemGlowApi.HudPanel(Identifier.of("itemglow", "default"));
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.NameLineElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.CooldownArcElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.SubtitleLineElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.EnchantRowElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.DurabilityBarElement());
        com.itemglow.api.ItemGlowApi.registerPanel(mainPanel);
        com.itemglow.api.ItemGlowApi.registerProfile(Identifier.of("itemglow", "default"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            ItemStack curMain = client.player.getMainHandStack();

            boolean isSameItem = curMain.getItem() == mainStack.getItem();
            boolean isSameName = curMain.getName().getString().equals(mainStack.getName().getString());
            boolean majorChange = !isSameItem || !isSameName || !curMain.getEnchantments().equals(mainStack.getEnchantments());

            if (!ItemStack.areItemsAndComponentsEqual(mainStack, curMain)) {
                if (majorChange) {
                    if (config.enableSwitchAnimation && (mainTimer > 0.05f || config.constantVisibility)) {
                        outgoingStack = mainStack.copy();
                        outgoingOpacityMult = config.constantVisibility ? 1.0f : MathHelper.clamp(mainTimer / 0.5f, 0, 1);
                        switchProgress = 0f;
                        outgoingEquipAge = mainEquipAge;
                    } else { outgoingStack = ItemStack.EMPTY; switchProgress = 1.0f; }
                    mainStack = curMain.copy();
                    mainTimer = config.fadeDelaySeconds;
                    mainEquipAge = client.player.age;
                    visualMainPct = com.itemglow.api.ItemGlowApi.getPct(mainStack);
                } else {
                    mainStack = curMain.copy();
                    if (!config.constantVisibility && (config.showDurabilityBar || config.showDurabilityText)) mainTimer = config.fadeDelaySeconds;
                }
            }
            ItemStack curOff = client.player.getOffHandStack();
            if (!ItemStack.areItemsAndComponentsEqual(offStack, curOff)) { offStack = curOff.copy(); offTimer = config.fadeDelaySeconds; offEquipAge = client.player.age; }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden || client.currentScreen != null) return;
            float delta = tickCounter.getLastFrameDuration() / 20f;
            if (switchProgress < 1.0f) switchProgress = MathHelper.clamp(switchProgress + (delta / 0.20f), 0, 1);
            if (config.constantVisibility) { mainTimer = config.fadeDelaySeconds; offTimer = config.fadeDelaySeconds; }
            else { mainTimer = Math.max(0, mainTimer - delta); offTimer = Math.max(0, offTimer - delta); }

            if (switchProgress < 1.0f && !outgoingStack.isEmpty() && !config.constantVisibility) renderHUD(drawContext, client, outgoingStack, 1.0f, false, tickCounter.getTickDelta(false), true);
            if (mainTimer > 0 && !mainStack.isEmpty()) renderHUD(drawContext, client, mainStack, mainTimer, false, tickCounter.getTickDelta(false), false);
            if (config.showOffHand && offTimer > 0 && !offStack.isEmpty()) renderHUD(drawContext, client, offStack, offTimer, true, tickCounter.getTickDelta(false), false);
            if (config.showArmorHud) renderArmorHUD(drawContext, client, tickCounter.getTickDelta(false));
        });
    }

    private float easeOutCubic(float x) { return (float) (1 - Math.pow(1 - x, 3)); }

    private void renderHUD(DrawContext context, MinecraftClient client, ItemStack stack, float timer, boolean isOff, float pTicks, boolean isOutgoing) {
        float ageSinceEquip = (client.player.age + pTicks) - (isOutgoing ? outgoingEquipAge : (isOff ? offEquipAge : mainEquipAge));
        float opacity = config.constantVisibility ? 1.0f : MathHelper.clamp(timer / 0.5f, 0, 1);
        float entranceEase = easeOutCubic(MathHelper.clamp((config.fadeDelaySeconds - timer) / 0.25f, 0, 1));
        float switchEase = easeOutCubic(switchProgress);
        if (!config.constantVisibility) {
            if (isOutgoing) opacity *= (1.0f - switchEase) * outgoingOpacityMult;
            else if (config.enableSlideIn && !isOff) opacity *= entranceEase;
        }
        int alpha = (int) (opacity * 255);
        if (alpha < 1) return;

        int pw = config.panelWidth; int ph = config.panelHeight;
        float s = isOff ? config.scale * 0.85f : config.scale;
        int sw = (int)(pw * s); int sh = (int)(ph * s);
        int screenW = client.getWindow().getScaledWidth(); int screenH = client.getWindow().getScaledHeight();

        int x, y;
        if (isOff && config.anchor == ItemGlowConfig.Anchor.ABOVE_HOTBAR) { x = (screenW / 2) - sw - 100; y = screenH - 45 - config.heightOffset - sh; }
        else { switch (config.anchor) {
            case ABOVE_HOTBAR -> { x = (screenW - sw) / 2; y = screenH - 45 - config.heightOffset - sh; }
            case TOP_LEFT -> { x = config.sideOffset; y = config.heightOffset; }
            case TOP_RIGHT -> { x = screenW - sw - config.sideOffset; y = config.heightOffset; }
            case BOTTOM_LEFT -> { x = config.sideOffset; y = screenH - sh - config.heightOffset; }
            default -> { x = screenW - sw - config.sideOffset; y = screenH - sh - config.heightOffset; }
        }}

        float animY = (!isOff && !config.constantVisibility) ? (isOutgoing ? -(switchEase * 15.0f) : ((1.0f - entranceEase) * 8.0f) + (config.enableSwitchAnimation ? (1.0f - switchEase) * 15.0f : 0)) : 0;
        context.getMatrices().push();
        context.getMatrices().translate(x, y + animY, 0);
        context.getMatrices().scale(s, s, 1);

        float pct = com.itemglow.api.ItemGlowApi.getPct(stack);
        float pulse = (pct < 0.20f && config.durabilityFlash) ? (MathHelper.sin((client.player.age + pTicks) * 0.4f) + 1.0f) * 0.5f : 0f;
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f - (pulse * 0.5f), 1.0f - (pulse * 0.5f), opacity);

        context.fill(0, 0, pw, ph, ((int)(255 * config.bgOpacity) << 24) | ((int)(pulse * 60) << 16));
        context.fill(0, 0, 2, ph, (255 << 24) | config.accentColor);

        int cL = 8;
        if (config.showIcon) {
            context.drawItem(stack, 6, 6);
            cL += 18;
        }

        com.itemglow.api.ItemGlowApi.HudPanel activePanel = com.itemglow.api.ItemGlowApi.getPanel(com.itemglow.api.ItemGlowApi.getActiveProfile());
        if (activePanel != null) {
            activePanel.render(context, client, stack, cL, 5, pw - cL - 10, opacity, ageSinceEquip);
        }

        context.getMatrices().pop();
        RenderSystem.disableBlend();
    }



    private void renderArmorHUD(DrawContext context, MinecraftClient client, float pTicks) {
        boolean hasAnyArmor = false;
        for (int i = 0; i < 4; i++) if (!client.player.getInventory().getArmorStack(i).isEmpty()) hasAnyArmor = true;
        if (!hasAnyArmor) return;
        boolean isPeeking = armorPeekKey.isPressed();
        int screenW = client.getWindow().getScaledWidth(); int screenH = client.getWindow().getScaledHeight();
        float s = config.armorScale;
        int x, y;
        switch (config.armorAnchor) {
            case TOP_LEFT -> { x = config.armorSideOffset; y = config.armorHeightOffset; }
            case TOP_RIGHT -> { x = screenW - 25 - config.armorSideOffset; y = config.armorHeightOffset; }
            case BOTTOM_RIGHT -> { x = screenW - 25 - config.armorSideOffset; y = screenH - 80 - config.armorHeightOffset; }
            default -> { x = config.armorSideOffset; y = screenH - 80 - config.armorHeightOffset; }
        }
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(s, s, 1);
        for (int i = 3; i >= 0; i--) {
            ItemStack armor = client.player.getInventory().getArmorStack(i);
            if (armor.isEmpty()) continue;
            float pct = com.itemglow.api.ItemGlowApi.getPct(armor);
            float pulse = (pct < 0.10f) ? (MathHelper.sin((client.player.age + pTicks) * 0.6f) + 1.0f) * 0.5f : 0f;
            RenderSystem.setShaderColor(1.0f, 1.0f - pulse, 1.0f - pulse, 1.0f);
            context.drawItem(armor, 0, 0);
            context.fill(0, 17, 16, 19, (255 << 24) | 0x222222);
            context.fill(0, 17, (int)(16 * pct), 19, (255 << 24) | (pct > 0.5f ? 0x55FF55 : pct > 0.2f ? 0xFFFF55 : 0xFF5555));
            if (isPeeking) {
                var en = armor.get(DataComponentTypes.ENCHANTMENTS);
                if (en != null) {
                    int ey = 0;
                    for (var entry : en.getEnchantments()) {
                        String name = entry.value().description().getString();
                        if (name.length() > 3) name = name.substring(0, 3).toUpperCase();
                        com.itemglow.api.ItemGlowApi.drawScaledText(context, client, Text.literal(name + " " + en.getLevel(entry)), 18, ey, 40, 0xBF80FF);
                        ey += 8;
                    }
                }
            }
            context.getMatrices().translate(0, 22, 0);
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.getMatrices().pop();
    }
}