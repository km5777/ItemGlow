package com.itemglow;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.List;

public class ItemGlowAPI {
    // Other mods implement this interface
    public interface ItemGlowProvider {
        List<TooltipLine> getLines(ItemStack stack);
    }

    // Data class for API lines
    public record TooltipLine(Text text, int color, int priority) {}
}