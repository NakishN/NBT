package com.nbt;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TooltipHandler {

    private static long lastScrollTime = 0;
    private static int scrollOffset = 0;
    private static ItemStack lastItem = ItemStack.EMPTY;

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!NBTConfig.enabled) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        if (NBTConfig.requireAdvancedTooltips && !mc.options.advancedItemTooltips) {
            return;
        }

        List<Component> dataLines = getItemDataComponents(stack);

        if (dataLines.isEmpty()) {
            if (NBTConfig.showEmptyTags) {
                event.getToolTip().add(Component.literal("Данные: пусто").withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        handleScrolling(stack);

        event.getToolTip().add(Component.empty());
        event.getToolTip().add(Component.literal("--- Данные компонентов ---").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));

        if (dataLines.size() > NBTConfig.maxTooltipLines) {
            displayScrollingTooltip(event.getToolTip(), dataLines);
        } else {
            event.getToolTip().addAll(dataLines);
        }

        if (dataLines.size() > NBTConfig.maxTooltipLines) {
            event.getToolTip().add(Component.literal("SHIFT: Пауза | ALT: Ускорить").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        if (Nbt.COPY_NBT_KEY != null) {
            String copyHint = String.format("Нажмите %s для копирования данных",
                    Nbt.COPY_NBT_KEY.getTranslatedKeyMessage().getString());
            event.getToolTip().add(Component.literal(copyHint).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    private List<Component> getItemDataComponents(ItemStack stack) {
        List<Component> lines = new ArrayList<>();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (!tag.isEmpty()) {
                lines.add(Component.literal("Custom Data:").withStyle(ChatFormatting.GOLD));
                NBTFormatter formatter = new NBTFormatter(NBTConfig.tooltipFormat, NBTConfig.colorCoding);
                lines.addAll(formatter.format(tag, 1));
            }
        }

        addComponentLine(stack, DataComponents.CUSTOM_NAME, "Имя", lines);
        addComponentLine(stack, DataComponents.DAMAGE, "Урон", lines);
        addComponentLine(stack, DataComponents.MAX_DAMAGE, "Макс. урон", lines);
        addComponentLine(stack, DataComponents.UNBREAKABLE, "Неразрушимость", lines);
        addComponentLine(stack, DataComponents.ENCHANTMENTS, "Чары", lines);
        addComponentLine(stack, DataComponents.LORE, "Описание", lines);

        return lines;
    }

    private <T> void addComponentLine(ItemStack stack, net.minecraft.core.component.DataComponentType<T> component,
                                      String displayName, List<Component> lines) {
        T value = stack.get(component);
        if (value != null) {
            MutableComponent line = Component.literal(displayName + ": ");
            if (NBTConfig.colorCoding) {
                line = line.withStyle(ChatFormatting.AQUA);
            }

            String valueStr = formatComponentValue(value);
            line.append(Component.literal(valueStr).withStyle(NBTConfig.colorCoding ? ChatFormatting.WHITE : ChatFormatting.GRAY));
            lines.add(line);
        }
    }

    private String formatComponentValue(Object value) {
        if (value instanceof Component component) {
            return component.getString();
        } else if (value instanceof List<?> list) {
            return "[" + list.size() + " элементов]";
        } else {
            String str = value.toString();
            return str.length() > 50 ? str.substring(0, 47) + "..." : str;
        }
    }

    private void handleScrolling(ItemStack currentItem) {
        String currentItemString = currentItem.toString();
        String lastItemString = lastItem.toString();
        if (!currentItemString.equals(lastItemString)) {
            scrollOffset = 0;
            lastItem = currentItem.copy();
        }

        long currentTime = System.currentTimeMillis();

        boolean isPaused = Screen.hasShiftDown();
        boolean isSpeedUp = Screen.hasAltDown();

        if (!isPaused && currentTime - lastScrollTime > getScrollDelay(isSpeedUp)) {
            scrollOffset++;
            lastScrollTime = currentTime;
        }
    }

    private long getScrollDelay(boolean speedUp) {
        double speed = NBTConfig.scrollSpeed;
        if (speedUp) speed *= 3.0;
        return (long) (1000.0 / speed);
    }

    private void displayScrollingTooltip(List<Component> tooltip, List<Component> dataLines) {
        int maxLines = NBTConfig.maxTooltipLines;
        int totalLines = dataLines.size();

        int startLine = scrollOffset % Math.max(1, totalLines - maxLines + 1);
        int endLine = Math.min(startLine + maxLines, totalLines);

        tooltip.add(Component.literal(String.format("(%d-%d из %d строк)",
                startLine + 1, endLine, totalLines)).withStyle(ChatFormatting.DARK_GRAY));

        for (int i = startLine; i < endLine; i++) {
            tooltip.add(dataLines.get(i));
        }
    }
}