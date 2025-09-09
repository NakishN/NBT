package com.nbt;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class NBTFormatter {

    private final NBTConfig.DisplayFormat format;
    private final boolean useColors;

    public NBTFormatter(NBTConfig.DisplayFormat format, boolean useColors) {
        this.format = format;
        this.useColors = useColors;
    }

    public List<Component> format(CompoundTag tag, int indent) {
        List<Component> result = new ArrayList<>();

        if (tag.isEmpty() && !NBTConfig.showEmptyTags) {
            return result;
        }

        switch (format) {
            case HUMAN_READABLE:
                formatHumanReadable(tag, result, indent);
                break;
            case JSON:
                formatAsJson(tag, result);
                break;
            case SNBT:
                formatAsSNBT(tag, result);
                break;
            case MINIMAL:
                formatMinimal(tag, result, indent);
                break;
        }

        return result;
    }

    public String formatForClipboard(CompoundTag tag) {
        switch (format) {
            case JSON:
                return convertToJson(tag);
            case SNBT:
                return tag.toString();
            case MINIMAL:
                return formatMinimalString(tag);
            default:
                List<Component> components = format(tag, 0);
                StringBuilder sb = new StringBuilder();
                for (Component comp : components) {
                    sb.append(comp.getString()).append("\n");
                }
                return sb.toString().trim();
        }
    }

    private void formatHumanReadable(CompoundTag tag, List<Component> result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value == null) continue;

            MutableComponent line = Component.literal(indentStr + key + ": ");
            if (useColors) {
                line = line.withStyle(ChatFormatting.GOLD);
            }

            String valueStr = formatTagValue(value);
            ChatFormatting valueColor = useColors ? getValueColor(value) : ChatFormatting.WHITE;
            line.append(Component.literal(valueStr).withStyle(valueColor));

            result.add(line);
        }
    }

    private void formatAsJson(CompoundTag tag, List<Component> result) {
        String json = convertToJson(tag);
        String[] lines = json.split("\n");

        for (String line : lines) {
            result.add(Component.literal(line).withStyle(ChatFormatting.AQUA));
        }
    }

    private void formatAsSNBT(CompoundTag tag, List<Component> result) {
        String snbt = tag.toString();

        if (snbt.length() > 80) {
            int pos = 0;
            while (pos < snbt.length()) {
                int endPos = Math.min(pos + 80, snbt.length());
                String chunk = snbt.substring(pos, endPos);
                result.add(Component.literal(chunk).withStyle(useColors ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
                pos = endPos;
            }
        } else {
            result.add(Component.literal(snbt).withStyle(useColors ? ChatFormatting.YELLOW : ChatFormatting.WHITE));
        }
    }

    private void formatMinimal(CompoundTag tag, List<Component> result, int indent) {
        String indentStr = "  ".repeat(indent);

        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value == null) continue;

            String line = indentStr + key + ": " + formatTagValue(value);
            result.add(Component.literal(line).withStyle(useColors ? getValueColor(value) : ChatFormatting.GRAY));
        }
    }

    private String formatMinimalString(CompoundTag tag) {
        StringBuilder sb = new StringBuilder();

        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value != null) {
                sb.append(key).append(": ").append(formatTagValue(value)).append("\n");
            }
        }

        return sb.toString().trim();
    }

    private String formatTagValue(Tag tag) {
        switch (tag.getId()) {
            case Tag.TAG_STRING:
                StringTag stringTag = (StringTag) tag;
                return "\"" + stringTag.getAsString() + "\"";
            case Tag.TAG_BYTE:
                return tag.toString() + "b";
            case Tag.TAG_SHORT:
                return tag.toString() + "s";
            case Tag.TAG_INT:
                return tag.toString();
            case Tag.TAG_LONG:
                return tag.toString() + "L";
            case Tag.TAG_FLOAT:
                return tag.toString() + "f";
            case Tag.TAG_DOUBLE:
                return tag.toString() + "d";
            case Tag.TAG_COMPOUND:
                CompoundTag compound = (CompoundTag) tag;
                return compound.isEmpty() ? "{}" : "{" + compound.size() + " элементов}";
            case Tag.TAG_LIST:
                ListTag list = (ListTag) tag;
                return list.isEmpty() ? "[]" : "[" + list.size() + " элементов]";
            case Tag.TAG_BYTE_ARRAY:
                ByteArrayTag byteArray = (ByteArrayTag) tag;
                return "[B; " + byteArray.size() + " байт]";
            case Tag.TAG_INT_ARRAY:
                IntArrayTag intArray = (IntArrayTag) tag;
                return "[I; " + intArray.size() + " int]";
            case Tag.TAG_LONG_ARRAY:
                LongArrayTag longArray = (LongArrayTag) tag;
                return "[L; " + longArray.size() + " long]";
            default:
                return tag.toString();
        }
    }

    private String convertToJson(CompoundTag tag) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");

        boolean first = true;
        for (String key : tag.getAllKeys()) {
            if (!first) json.append(",\n");
            first = false;

            json.append("  \"").append(key).append("\": ");
            Tag value = tag.get(key);
            json.append(convertTagToJsonValue(value));
        }

        json.append("\n}");
        return json.toString();
    }

    private String convertTagToJsonValue(Tag tag) {
        switch (tag.getId()) {
            case Tag.TAG_STRING:
                StringTag stringTag = (StringTag) tag;
                return "\"" + stringTag.getAsString().replace("\"", "\\\"") + "\"";
            case Tag.TAG_BYTE:
            case Tag.TAG_SHORT:
            case Tag.TAG_INT:
            case Tag.TAG_LONG:
            case Tag.TAG_FLOAT:
            case Tag.TAG_DOUBLE:
                return tag.toString();
            case Tag.TAG_COMPOUND:
                return convertToJson((CompoundTag) tag);
            case Tag.TAG_LIST:
                ListTag list = (ListTag) tag;
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) json.append(", ");
                    json.append(convertTagToJsonValue(list.get(i)));
                }
                json.append("]");
                return json.toString();
            default:
                return "\"" + tag.toString().replace("\"", "\\\"") + "\"";
        }
    }

    private ChatFormatting getValueColor(Tag tag) {
        switch (tag.getId()) {
            case Tag.TAG_STRING:
                return ChatFormatting.GREEN;
            case Tag.TAG_BYTE:
            case Tag.TAG_SHORT:
            case Tag.TAG_INT:
            case Tag.TAG_LONG:
                return ChatFormatting.BLUE;
            case Tag.TAG_FLOAT:
            case Tag.TAG_DOUBLE:
                return ChatFormatting.AQUA;
            case Tag.TAG_COMPOUND:
                return ChatFormatting.YELLOW;
            case Tag.TAG_LIST:
                return ChatFormatting.LIGHT_PURPLE;
            case Tag.TAG_BYTE_ARRAY:
            case Tag.TAG_INT_ARRAY:
            case Tag.TAG_LONG_ARRAY:
                return ChatFormatting.DARK_PURPLE;
            default:
                return ChatFormatting.WHITE;
        }
    }
}