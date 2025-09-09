package com.nbt;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Nbt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NBTConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Включить показ данных компонентов в подсказках")
            .define("enabled", true);

    public static final ForgeConfigSpec.BooleanValue REQUIRE_ADVANCED_TOOLTIPS = BUILDER
            .comment("Требовать включенные расширенные подсказки (F3+H) для показа данных")
            .define("requireAdvancedTooltips", true);

    public static final ForgeConfigSpec.IntValue MAX_TOOLTIP_LINES = BUILDER
            .comment("Максимальное количество строк данных для показа в подсказке одновременно")
            .defineInRange("maxTooltipLines", 10, 1, 50);

    public static final ForgeConfigSpec.DoubleValue SCROLL_SPEED = BUILDER
            .comment("Скорость прокрутки подсказок (строк в секунду)")
            .defineInRange("scrollSpeed", 2.0, 0.1, 10.0);

    public static final ForgeConfigSpec.BooleanValue COLOR_CODING = BUILDER
            .comment("Включить цветовое кодирование для разных типов данных")
            .define("colorCoding", true);

    public static final ForgeConfigSpec.EnumValue<DisplayFormat> TOOLTIP_FORMAT = BUILDER
            .comment("Формат отображения данных в подсказках")
            .defineEnum("tooltipFormat", DisplayFormat.HUMAN_READABLE);

    public static final ForgeConfigSpec.EnumValue<DisplayFormat> COPY_FORMAT = BUILDER
            .comment("Формат копирования данных в буфер обмена")
            .defineEnum("copyFormat", DisplayFormat.JSON);

    public static final ForgeConfigSpec.BooleanValue SHOW_EMPTY_TAGS = BUILDER
            .comment("Показывать предметы без дополнительных данных")
            .define("showEmptyTags", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean enabled;
    public static boolean requireAdvancedTooltips;
    public static int maxTooltipLines;
    public static double scrollSpeed;
    public static boolean colorCoding;
    public static DisplayFormat tooltipFormat;
    public static DisplayFormat copyFormat;
    public static boolean showEmptyTags;

    public enum DisplayFormat {
        HUMAN_READABLE("Читаемый формат"),
        JSON("JSON"),
        SNBT("SNBT"),
        MINIMAL("Минимальный");

        private final String displayName;

        DisplayFormat(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enabled = ENABLED.get();
        requireAdvancedTooltips = REQUIRE_ADVANCED_TOOLTIPS.get();
        maxTooltipLines = MAX_TOOLTIP_LINES.get();
        scrollSpeed = SCROLL_SPEED.get();
        colorCoding = COLOR_CODING.get();
        tooltipFormat = TOOLTIP_FORMAT.get();
        copyFormat = COPY_FORMAT.get();
        showEmptyTags = SHOW_EMPTY_TAGS.get();
    }
}