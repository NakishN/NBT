package com.nbt;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(Nbt.MODID)
public class Nbt {

    public static final String MODID = "nbt";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static KeyMapping COPY_NBT_KEY;
    public static KeyMapping COPY_NBT_JSON_KEY;

    public Nbt(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerKeyMappings);

        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.CLIENT, NBTConfig.SPEC);

        LOGGER.info("NBT Tooltip мод загружен для Minecraft 1.21.3");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("NBT Tooltip: Общая настройка завершена");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new TooltipHandler());
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());

        LOGGER.info("NBT Tooltip: Клиентская часть настроена");
    }

    @SubscribeEvent
    public void registerKeyMappings(RegisterKeyMappingsEvent event) {
        COPY_NBT_KEY = new KeyMapping(
                "key.nbt.copy_nbt",
                GLFW.GLFW_KEY_C,
                "key.categories.nbt"
        );

        COPY_NBT_JSON_KEY = new KeyMapping(
                "key.nbt.copy_nbt_json",
                GLFW.GLFW_KEY_J,
                "key.categories.nbt"
        );

        event.register(COPY_NBT_KEY);
        event.register(COPY_NBT_JSON_KEY);

        LOGGER.info("Зарегистрированы клавиши для копирования данных предметов");
    }
}