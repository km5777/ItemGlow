package com.itemglow.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGlowApi {
    private static final Map<Identifier, HudElement> ELEMENTS = new HashMap<>();
    private static final Map<Identifier, HudPanel> PANELS = new HashMap<>();
    private static Identifier activeProfile = Identifier.of("itemglow", "default");

    private static final Identifier FOOD_FULL = Identifier.ofVanilla("hud/food_full");
    private static final Identifier FOOD_HALF = Identifier.ofVanilla("hud/food_half");

    public static void registerElement(HudElement element) { ELEMENTS.put(element.getId(), element); }
    public static HudElement getElement(Identifier id) { return ELEMENTS.get(id); }

    public static void registerPanel(HudPanel panel) { PANELS.put(panel.getId(), panel); }
    public static HudPanel getPanel(Identifier id) { return PANELS.get(id); }

    public static void registerProfile(Identifier profileId) { activeProfile = profileId; }
    public static Identifier getActiveProfile() { return activeProfile; }

    public static record Pill(String text, int bg, int fg, boolean isFood, float alphaMult, float yOffset, boolean advanceSlot) {
        public Pill(String text, int bg, int fg, boolean isFood) {
            this(text, bg, fg, isFood, 1.0f, 0.0f, true);
        }
    }

    public static float getPct(ItemStack s) { 
        return s.isDamageable() ? (float)(s.getMaxDamage() - s.getDamage()) / s.getMaxDamage() : 1.0f; 
    }

    public static void addCyclePills(List<Pill> pills, List<String> items, int bg, int fg, float ageSinceEquip, float fadeDelaySeconds) {
        if (items.isEmpty()) return;
        int size = items.size();
        if (size == 1) {
            pills.add(new Pill(items.get(0), bg, fg, false));
            return;
        }

        float totalCycleTicks = (fadeDelaySeconds > 0 ? fadeDelaySeconds : 2.0f) * 20.0f;
        float ticksPerItem = totalCycleTicks / size;
        float animTicks = Math.min(5.0f, ticksPerItem * 0.2f);
        
        float currentTickInCycle = ageSinceEquip % totalCycleTicks;
        int idx = (int)(currentTickInCycle / ticksPerItem);
        if (idx >= size) idx = size - 1;
        
        float ticksInCurrentItem = currentTickInCycle - (idx * ticksPerItem);

        if (ticksInCurrentItem > ticksPerItem - animTicks) {
            float t = (ticksInCurrentItem - (ticksPerItem - animTicks)) / animTicks;
            int nextIdx = (idx + 1) % size;
            pills.add(new Pill(items.get(idx) + " • " + (idx + 1) + "/" + size, bg, fg, false, 1.0f - t, -t * 11.0f, false));
            pills.add(new Pill(items.get(nextIdx) + " • " + (nextIdx + 1) + "/" + size, bg, fg, false, t, (1.0f - t) * 11.0f, true));
        } else {
            pills.add(new Pill(items.get(idx) + " • " + (idx + 1) + "/" + size, bg, fg, false));
        }
    }

    public static void drawCustomPill(DrawContext ctx, MinecraftClient c, String t, int x, int y, int a, int mW, int bg, int fg) {
        int tw = c.textRenderer.getWidth(t); int fW = Math.min(tw, mW - 4);
        ctx.fill(x, y, x + fW + 4, y + 9, (a / 2 << 24) | (bg & 0xFFFFFF));
        drawScaledText(ctx, c, Text.literal(t), x + 2, y + 1, mW - 4, (a << 24) | (fg & 0xFFFFFF));
    }

    public static void drawScaledText(DrawContext ctx, MinecraftClient c, Text t, int x, int y, int mW, int col) {
        int w = c.textRenderer.getWidth(t);
        if (w > mW && mW > 0) {
            ctx.getMatrices().push();
            float s = (float) mW / w;
            ctx.getMatrices().translate(x, y, 0);
            ctx.getMatrices().scale(s, s, 1);
            ctx.drawText(c.textRenderer, t, 0, 0, col, true);
            ctx.getMatrices().pop();
        } else {
            ctx.drawText(c.textRenderer, t, x, y, col, true);
        }
    }

    public static int renderPills(DrawContext context, MinecraftClient client, ItemStack stack, List<Pill> pills, int x, int y, int maxW, float alpha) {
        int py = y;
        for (Pill p : pills) {
            int pAlpha = (int)(alpha * 255 * p.alphaMult);
            if (pAlpha > 5) {
                int drawY = py + (int)p.yOffset;
                drawCustomPill(context, client, p.text, x, drawY, pAlpha, maxW, p.bg, p.fg);
                
                if (p.isFood && stack.contains(DataComponentTypes.FOOD)) {
                    FoodComponent f = stack.get(DataComponentTypes.FOOD);
                    int fW = Math.min(client.textRenderer.getWidth(p.text), maxW - 4);
                    int iconX = x + fW + 8;
                    if (alpha > 0.95f) {
                        for (int i = 0; i < (f.nutrition() + 1) / 2; i++) {
                            context.drawGuiTexture((i * 2 + 1 < f.nutrition()) ? FOOD_FULL : FOOD_HALF, iconX + (i * 8), drawY, 9, 9);
                        }
                    }
                }
            }
            if (p.advanceSlot) py += 11;
        }
        return py - y;
    }

    public interface HudElement {
        Identifier getId();
        int render(DrawContext context, MinecraftClient client, ItemStack stack, int x, int y, int maxWidth, float alpha, float ageSinceEquip);
    }

    public static class HudPanel {
        private final Identifier id;
        private final List<HudElement> elements = new ArrayList<>();
        
        public HudPanel(Identifier id) { this.id = id; }
        public Identifier getId() { return id; }
        public void addElement(HudElement element) { elements.add(element); }
        public List<HudElement> getElements() { return elements; }
        
        public int render(DrawContext context, MinecraftClient client, ItemStack stack, int startX, int startY, int maxWidth, float alpha, float ageSinceEquip) {
            int currentY = startY;
            for (HudElement element : elements) {
                currentY += element.render(context, client, stack, startX, currentY, maxWidth, alpha, ageSinceEquip);
            }
            return currentY - startY;
        }
    }
}
