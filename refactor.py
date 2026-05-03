import os

path = r'c:\Users\maxmi\Downloads\item-glow-template-1.21.1\src\main\java\com\itemglow\ItemGlowMod.java'
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

# 1. Pill public static
for i, l in enumerate(lines):
    if 'private record Pill' in l:
        lines[i] = l.replace('private record Pill', 'public static record Pill')

# 2. onInitializeClient additions
for i, l in enumerate(lines):
    if 'armorPeekKey = KeyBindingHelper.registerKeyBinding' in l:
        lines.insert(i+1, '''
        com.itemglow.api.ItemGlowApi.HudPanel mainPanel = new com.itemglow.api.ItemGlowApi.HudPanel(net.minecraft.util.Identifier.of("itemglow", "default"));
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.NameLineElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.CooldownArcElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.SubtitleLineElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.EnchantRowElement());
        mainPanel.addElement(new com.itemglow.elements.DefaultElements.DurabilityBarElement());
        com.itemglow.api.ItemGlowApi.registerPanel(mainPanel);
        com.itemglow.api.ItemGlowApi.registerProfile(net.minecraft.util.Identifier.of("itemglow", "default"));
''')
        break

# 3. getPct public static
for i, l in enumerate(lines):
    if 'private float getPct' in l:
        lines[i] = l.replace('private float', 'public static float')

# 4. addCyclePills public static
for i, l in enumerate(lines):
    if 'private void addCyclePills' in l:
        lines[i] = l.replace('private void', 'public static void')

# 5. drawCustomPill and drawScaledText public static
for i, l in enumerate(lines):
    if 'private void drawCustomPill' in l:
        lines[i] = l.replace('private void', 'public static void')
    if 'private void drawScaledText' in l:
        lines[i] = l.replace('private void', 'public static void')

# 6. Delete pills block and replace with API rendering
start_idx = -1
end_idx = -1
for i, l in enumerate(lines):
    if 'List<Pill> pills = new ArrayList<>();' in l and start_idx == -1:
        start_idx = i - 1 # include the comment above
    if '} else if (config.showStackCount && stack.getCount() > 1) {' in l:
        end_idx = i + 2 # include the stack count block end
        break

if start_idx != -1 and end_idx != -1:
    del lines[start_idx:end_idx+1]
    
    new_render_logic = '''
        int pw = config.panelWidth; int ph = config.panelHeight;
        float s = isOff ? config.scale * 0.85f : config.scale;
        int sw = (int)(pw * s); int sh = (int)(ph * s);
        int screenW = client.getWindow().getScaledWidth(); int screenH = client.getWindow().getScaledHeight();

        int x, y;
        if (isOff && config.anchor == com.itemglow.config.ItemGlowConfig.Anchor.ABOVE_HOTBAR) { x = (screenW / 2) - sw - 100; y = screenH - 45 - config.heightOffset - sh; }
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

        float pct = getPct(stack);
        float pulse = (pct < 0.20f && config.durabilityFlash) ? (net.minecraft.util.math.MathHelper.sin((client.player.age + pTicks) * 0.4f) + 1.0f) * 0.5f : 0f;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f - (pulse * 0.5f), 1.0f - (pulse * 0.5f), opacity);

        com.itemglow.api.ItemGlowApi.drawThemedOrFill(context, client, net.minecraft.util.Identifier.of("itemglow", "textures/gui/panel_background.png"), 0, 0, pw, ph, ((int)(255 * config.bgOpacity) << 24) | ((int)(pulse * 60) << 16));
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
'''
    lines.insert(start_idx, new_render_logic)

# 7. Add renderPills method
render_pills_method = '''
    public static int renderPills(net.minecraft.client.gui.DrawContext context, net.minecraft.client.MinecraftClient client, net.minecraft.item.ItemStack stack, java.util.List<Pill> pills, int x, int y, int maxW, float alpha) {
        int py = y;
        for (Pill p : pills) {
            int pAlpha = (int)(alpha * 255 * p.alphaMult);
            if (pAlpha > 5) {
                int drawY = py + (int)p.yOffset;
                if (client.getResourceManager().getResource(net.minecraft.util.Identifier.of("itemglow", "textures/gui/pill_bg.png")).isPresent()) {
                    int tw = client.textRenderer.getWidth(p.text); int fW = Math.min(tw, maxW - 4);
                    com.itemglow.api.ItemGlowApi.drawThemedOrFill(context, client, net.minecraft.util.Identifier.of("itemglow", "textures/gui/pill_bg.png"), x, drawY, fW + 4, 9, (pAlpha / 2 << 24) | (p.bg & 0xFFFFFF));
                    drawScaledText(context, client, net.minecraft.text.Text.literal(p.text), x + 2, drawY + 1, maxW - 4, (pAlpha << 24) | (p.fg & 0xFFFFFF));
                } else {
                    drawCustomPill(context, client, p.text, x, drawY, pAlpha, maxW, p.bg, p.fg);
                }
                
                if (p.isFood && stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
                    net.minecraft.component.type.FoodComponent f = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
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
'''
lines.insert(-1, render_pills_method)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(lines)
