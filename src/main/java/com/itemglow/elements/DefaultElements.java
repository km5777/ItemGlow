package com.itemglow.elements;

import com.itemglow.ItemGlowMod;
import com.itemglow.api.ItemGlowApi;
import com.itemglow.api.ItemGlowApi.Pill;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.component.type.SuspiciousStewEffectsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DefaultElements {

    public static class NameLineElement implements ItemGlowApi.HudElement {
        @Override public Identifier getId() { return Identifier.of("itemglow", "name_line"); }
        @Override public int render(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y, int mW, float alpha, float ageSinceEquip) {
            Text name = stack.getName();
            int nameColor = ItemGlowMod.config.textColor;
            if (ItemGlowMod.config.forceNameColor) {
                nameColor = ItemGlowMod.config.nameColorOverride;
                MutableText m = name.copy();
                name = m.setStyle(m.getStyle().withColor(nameColor).withItalic(false));
            } else {
                Integer rarityColor = stack.getRarity().getFormatting().getColorValue();
                if (rarityColor != null) nameColor = rarityColor;
            }
            int a = (int)(alpha * 255);
            if (a < 5) return 0;
            ItemGlowApi.drawScaledText(ctx, client, name, x, y, mW, (a << 24) | nameColor);
            return 11;
        }
    }

    public static class CooldownArcElement implements ItemGlowApi.HudElement {
        @Override public Identifier getId() { return Identifier.of("itemglow", "cooldown_arc"); }
        @Override public int render(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y, int mW, float alpha, float ageSinceEquip) {
            if (ItemGlowMod.config.showIcon && ItemGlowMod.config.showCooldownArc && client.player != null) {
                float cd = client.player.getItemCooldownManager().getCooldownProgress(stack.getItem(), 0f);
                if (cd > 0) {
                    ctx.fill(6, 6 + (int)(16 - 16 * cd), 8, 22, (200 << 24) | 0xFFFFFF);
                }
            }
            return 0;
        }
    }

    public static class SubtitleLineElement implements ItemGlowApi.HudElement {
        @Override public Identifier getId() { return Identifier.of("itemglow", "subtitle_line"); }
        @Override public int render(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y, int mW, float alpha, float ageSinceEquip) {
            if (!ItemGlowMod.config.showDetailedInfo) return 0;
            List<Pill> pills = new ArrayList<>();
            Item item = stack.getItem();
            
            if (stack.contains(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER)) {
                pills.add(new Pill("Bad Omen " + (stack.get(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER) + 1), 0x80400000, 0xFFFF5555, false));
            } else {
                Item.TooltipContext tctx = client.world != null ? Item.TooltipContext.create(client.world.getRegistryManager()) : Item.TooltipContext.DEFAULT;
                List<Text> lines = stack.getTooltip(tctx, client.player, TooltipType.BASIC);
                String info = "";
                for (int i = 1; i < lines.size(); i++) {
                    String t = lines.get(i).getString().trim();
                    if (!t.isEmpty()) { info = t; break; }
                }
                
                if (item == Items.COMPASS || item == Items.RECOVERY_COMPASS) {
                    if (client.world != null && client.player != null) {
                        client.world.getBiome(client.player.getBlockPos()).getKey().ifPresent(k -> {
                            String bName = Text.translatable("biome." + k.getValue().getNamespace() + "." + k.getValue().getPath()).getString();
                            pills.add(new Pill("Biome: " + bName, 0x80003366, 0xFF88AAFF, false));
                        });
                    }
                }
                
                if (item == Items.MILK_BUCKET) pills.add(new Pill("Clears all effects", 0x80DDDDDD, 0xFFFFFFFF, false));
                else if (item == Items.CHORUS_FRUIT) pills.add(new Pill("Teleports player", 0x80800080, 0xFFFF00FF, false));
                else if (item == Items.SPECTRAL_ARROW) pills.add(new Pill("Glowing (10s)", 0x80AAAA00, 0xFFFFFF55, false));
                else if (item == Items.TOTEM_OF_UNDYING) pills.add(new Pill("Revives from death", 0x80AA8800, 0xFFFFAA00, false));
                else if (item == Items.EXPERIENCE_BOTTLE) pills.add(new Pill("Drops experience", 0x8000AA00, 0xFF55FF55, false));
                else if (item == Items.WITHER_ROSE) pills.add(new Pill("Inflicts Wither", 0x80222222, 0xFF555555, false));
                
                if (stack.contains(DataComponentTypes.FIREWORKS)) {
                    FireworksComponent fw = stack.get(DataComponentTypes.FIREWORKS);
                    if (fw != null) pills.add(new Pill("Flight Duration: " + fw.flightDuration(), 0x80555555, 0xFFAAAAAA, false));
                }

                if (stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE)) {
                    if (!info.isEmpty()) pills.add(new Pill(info, 0x80006400, 0xFF55FF55, false));
                } else if (!info.isEmpty()) {
                    if (item == Items.GOAT_HORN) pills.add(new Pill(info, 0x8000008B, 0xFF5555FF, false));
                    else if (item == Items.PAINTING) {
                        int bw = 0, bh = 0;
                        if (stack.contains(DataComponentTypes.ENTITY_DATA)) {
                            var nbtComp = stack.get(DataComponentTypes.ENTITY_DATA);
                            if (nbtComp != null) {
                                var nbt = nbtComp.copyNbt();
                                if (nbt.contains("variant", 8)) {
                                    Identifier varId = Identifier.tryParse(nbt.getString("variant"));
                                    if (varId != null && client.world != null) {
                                        var registry = client.world.getRegistryManager().get(net.minecraft.registry.RegistryKeys.PAINTING_VARIANT);
                                        var variant = registry.get(varId);
                                        if (variant != null) {
                                            bw = variant.width() / 16;
                                            bh = variant.height() / 16;
                                        }
                                    }
                                }
                            }
                        }
                        if (bw > 0 && bh > 0) {
                            pills.add(new Pill(bw + "x" + bh + " Blocks", 0x80333333, 0xFFAAAAAA, false));
                        } else {
                            pills.add(new Pill(info.isEmpty() ? "Random Size" : info, 0x80333333, 0xFFAAAAAA, false));
                        }
                    }
                    else if (stack.contains(DataComponentTypes.TRIM)) pills.add(new Pill(info, 0x80333333, 0xFFAAAAAA, false));
                    else if (item instanceof BannerPatternItem) pills.add(new Pill(info, 0x80555555, 0xFFAAAAAA, false));
                    else if (item instanceof SmithingTemplateItem) pills.add(new Pill(info, 0x80333355, 0xFFAAAAFF, false));
                    else if (item == Items.FILLED_MAP) pills.add(new Pill(info, 0x80AA8844, 0xFFFFCCAA, false));
                }
            }
            return ItemGlowApi.renderPills(ctx, client, stack, pills, x, y, mW, alpha);
        }
    }

    public static class EnchantRowElement implements ItemGlowApi.HudElement {
        @Override public Identifier getId() { return Identifier.of("itemglow", "enchant_row"); }
        @Override public int render(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y, int mW, float alpha, float ageSinceEquip) {
            List<Pill> pills = new ArrayList<>();
            
            if (ItemGlowMod.config.showEnchantments) {
                var e = stack.isOf(Items.ENCHANTED_BOOK) ? stack.get(DataComponentTypes.STORED_ENCHANTMENTS) : stack.get(DataComponentTypes.ENCHANTMENTS);
                if (e != null && !e.isEmpty()) {
                    var entries = new ArrayList<>(e.getEnchantments());
                    if (entries.size() > 2) {
                        List<String> items = new ArrayList<>();
                        for (var entry : entries) items.add(entry.value().description().getString() + " " + e.getLevel(entry));
                        ItemGlowApi.addCyclePills(pills, items, 0x804B0082, 0xBF80FF, ageSinceEquip, ItemGlowMod.config.fadeDelaySeconds);
                    } else {
                        for (var entry : entries) pills.add(new Pill(entry.value().description().getString() + " " + e.getLevel(entry), 0x804B0082, 0xBF80FF, false));
                    }
                }
            }

            if (ItemGlowMod.config.showFoodPreview && stack.contains(DataComponentTypes.FOOD)) {
                FoodComponent f = stack.get(DataComponentTypes.FOOD);
                if (f != null) {
                    pills.add(new Pill("Food: " + f.nutrition() + " Sat: " + (int)f.saturation(), 0x80A0522D, 0xFFFFAA00, true));
                    var fEffects = f.effects();
                    if (fEffects != null && !fEffects.isEmpty()) {
                        List<String> effectItems = new ArrayList<>();
                        for (var entry : fEffects) {
                            StatusEffectInstance eff = entry.effect();
                            int ts = eff.getDuration() / 20;
                            String time = ts > 0 ? " (" + String.format("%d:%02d", ts/60, ts%60) + ")" : "";
                            String effectName = eff.getEffectType().value().getName().getString();
                            if (eff.getAmplifier() > 0) effectName += " " + (eff.getAmplifier() + 1);
                            String prob = entry.probability() < 1.0f ? " (" + (int)(entry.probability() * 100) + "%)" : "";
                            effectItems.add(effectName + time + prob);
                        }
                        ItemGlowApi.addCyclePills(pills, effectItems, 0x80552255, 0xBF80FF, ageSinceEquip, ItemGlowMod.config.fadeDelaySeconds);
                    }
                }
            }

            if (ItemGlowMod.config.showPotionPreview && stack.contains(DataComponentTypes.POTION_CONTENTS)) {
                PotionContentsComponent pot = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (pot != null) {
                    List<StatusEffectInstance> effects = new ArrayList<>();
                    for (StatusEffectInstance eff : pot.getEffects()) effects.add(eff);
                    if (effects.isEmpty()) {
                        pills.add(new Pill("No effects", 0x80111155, 0xFF5555FF, false));
                    } else if (effects.size() > 2) {
                        List<String> items = new ArrayList<>();
                        for (StatusEffectInstance eff : effects) {
                            int ts = eff.getDuration() / 20;
                            String time = ts > 0 ? " (" + String.format("%d:%02d", ts/60, ts%60) + ")" : "";
                            String effectName = eff.getEffectType().value().getName().getString();
                            if (eff.getAmplifier() > 0) effectName += " " + (eff.getAmplifier() + 1);
                            items.add(effectName + time);
                        }
                        ItemGlowApi.addCyclePills(pills, items, 0x80111155, 0xFF5555FF, ageSinceEquip, ItemGlowMod.config.fadeDelaySeconds);
                    } else {
                        for (StatusEffectInstance eff : effects) {
                            int ts = eff.getDuration() / 20;
                            String time = ts > 0 ? " (" + String.format("%d:%02d", ts/60, ts%60) + ")" : "";
                            String effectName = eff.getEffectType().value().getName().getString();
                            if (eff.getAmplifier() > 0) effectName += " " + (eff.getAmplifier() + 1);
                            pills.add(new Pill(effectName + time, 0x80111155, 0xFF5555FF, false));
                        }
                    }
                }
            }

            SuspiciousStewEffectsComponent stew = stack.get(DataComponentTypes.SUSPICIOUS_STEW_EFFECTS);
            if (stew != null && !stew.effects().isEmpty()) {
                pills.add(new Pill("Effect: " + stew.effects().get(0).effect().value().getName().getString(), 0x80552255, 0xBF80FF, false));
            }

            return ItemGlowApi.renderPills(ctx, client, stack, pills, x, y, mW, alpha);
        }
    }

    public static class DurabilityBarElement implements ItemGlowApi.HudElement {
        @Override public Identifier getId() { return Identifier.of("itemglow", "durability_bar"); }
        @Override public int render(DrawContext ctx, MinecraftClient client, ItemStack stack, int x, int y, int mW, float alpha, float ageSinceEquip) {
            if (!stack.isDamageable()) return 0;
            int a = (int)(alpha * 255);
            if (a < 5) return 0;
            float pct = ItemGlowApi.getPct(stack);
            int height = 0;
            
            if (ItemGlowMod.config.showDurabilityBar) {
                int c = pct > 0.5f ? 0xFF55FF55 : (pct > 0.2f ? 0xFFFFFF55 : 0xFFFF5555);
                ctx.fill(x, y + 2, x + mW, y + 4, (a / 2 << 24) | 0x333333);
                ctx.fill(x, y + 2, x + (int)(mW * pct), y + 4, (a << 24) | (c & 0xFFFFFF));
                height += 6;
            }
            if (ItemGlowMod.config.showDurabilityText) {
                String t = (stack.getMaxDamage() - stack.getDamage()) + " / " + stack.getMaxDamage();
                ItemGlowApi.drawScaledText(ctx, client, Text.literal(t), x, y + height + 2, mW, (a << 24) | 0xAAAAAA);
                height += 11;
            }
            return height > 0 ? height + 2 : 0;
        }
    }
}
