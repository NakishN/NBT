package com.nbt;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class KeyInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || mc.screen != null) return;

        if (Nbt.COPY_NBT_KEY != null && Nbt.COPY_NBT_KEY.consumeClick()) {
            copyItemDataToClipboard(false);
        }

        if (Nbt.COPY_NBT_JSON_KEY != null && Nbt.COPY_NBT_JSON_KEY.consumeClick()) {
            copyItemDataToClipboard(true);
        }
    }

    private void copyItemDataToClipboard(boolean forceJson) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            stack = player.getOffhandItem();
            if (stack.isEmpty()) {
                sendClientMessage("Нет предмета в руке для копирования данных");
                return;
            }
        }

        try {
            String dataString = formatItemDataForCopy(stack, forceJson);

            if (dataString.isEmpty()) {
                String emptyData = forceJson ? "{}" : "Нет данных";
                copyToClipboard(emptyData);
                sendClientMessage("Скопированы пустые данные предмета");
                return;
            }

            copyToClipboard(dataString);

            NBTConfig.DisplayFormat format = forceJson ? NBTConfig.DisplayFormat.JSON : NBTConfig.copyFormat;
            String formatName = format.getDisplayName();
            sendClientMessage(String.format("Данные предмета скопированы в формате %s (%d символов)",
                    formatName, dataString.length()));

        } catch (Exception e) {
            sendClientMessage("Ошибка при копировании данных: " + e.getMessage());
        }
    }

    private String formatItemDataForCopy(ItemStack stack, boolean forceJson) {
        StringBuilder result = new StringBuilder();

        NBTConfig.DisplayFormat format = forceJson ? NBTConfig.DisplayFormat.JSON : NBTConfig.copyFormat;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (!tag.isEmpty()) {
                NBTFormatter formatter = new NBTFormatter(format, false);
                result.append("Custom Data:\n");
                result.append(formatter.formatForClipboard(tag));
                result.append("\n\n");
            }
        }

        if (format == NBTConfig.DisplayFormat.JSON) {
            result.append(formatComponentsAsJson(stack));
        } else {
            result.append(formatComponentsAsText(stack));
        }

        return result.toString().trim();
    }

    private String formatComponentsAsJson(ItemStack stack) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        boolean hasAnyComponent = false;

        if (stack.has(DataComponents.CUSTOM_NAME)) {
            json.append("  \"custom_name\": \"").append(stack.get(DataComponents.CUSTOM_NAME).getString()).append("\",\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.DAMAGE)) {
            json.append("  \"damage\": ").append(stack.get(DataComponents.DAMAGE)).append(",\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.MAX_DAMAGE)) {
            json.append("  \"max_damage\": ").append(stack.get(DataComponents.MAX_DAMAGE)).append(",\n");
            hasAnyComponent = true;
        }

        if (hasAnyComponent) {
            json.setLength(json.length() - 2);
            json.append("\n");
        }

        json.append("}");
        return json.toString();
    }

    private String formatComponentsAsText(ItemStack stack) {
        StringBuilder text = new StringBuilder();
        text.append("Компоненты предмета:\n");

        boolean hasAnyComponent = false;

        if (stack.has(DataComponents.CUSTOM_NAME)) {
            text.append("Имя: ").append(stack.get(DataComponents.CUSTOM_NAME).getString()).append("\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.DAMAGE)) {
            text.append("Урон: ").append(stack.get(DataComponents.DAMAGE)).append("\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.MAX_DAMAGE)) {
            text.append("Макс. урон: ").append(stack.get(DataComponents.MAX_DAMAGE)).append("\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.UNBREAKABLE)) {
            text.append("Неразрушимость: да\n");
            hasAnyComponent = true;
        }

        if (stack.has(DataComponents.ENCHANTMENTS)) {
            text.append("Чары: ").append(stack.get(DataComponents.ENCHANTMENTS).toString()).append("\n");
            hasAnyComponent = true;
        }

        if (!hasAnyComponent) {
            text.append("Нет специальных компонентов\n");
        }

        return text.toString();
    }

    private void copyToClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    private void sendClientMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }
}