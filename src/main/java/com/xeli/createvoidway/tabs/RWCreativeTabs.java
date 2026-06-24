package com.xeli.createvoidway.tabs;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.fluids.RWFluids;
import com.xeli.createvoidway.items.RWItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RWCreativeTabs {

	private static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister
			.create(Registries.CREATIVE_MODE_TAB, VoidwayMod.ID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BASE = TAB_REGISTER.register("base",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.createvoidway.base"))
					.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
					.icon(RWBlocks.VOID_MOTOR_OUTPUT::asStack)
					.displayItems((params, output) -> {
						output.accept(RWBlocks.VOID_STEEL_BLOCK);
						output.accept(RWItems.VOID_STEEL_INGOT);
						output.accept(RWItems.VOID_STEEL_SHEET);
						output.accept(RWBlocks.VOID_STEEL_SCAFFOLD);
						output.accept(RWBlocks.VOID_STEEL_LADDER);
						output.accept(RWBlocks.VOID_STEEL_BARS);
						output.accept(RWBlocks.VOID_CASING);
						output.accept(RWBlocks.VOID_MOTOR_OUTPUT);
						output.accept(RWBlocks.VOID_MOTOR_INPUT);
						output.accept(RWFluids.getBucketStack());
						output.accept(RWBlocks.VOID_CHEST_OUTPUT);
						output.accept(RWBlocks.VOID_CHEST_INPUT);
						output.accept(RWBlocks.VOID_TANK_OUTPUT);
						output.accept(RWBlocks.VOID_TANK_INPUT);
						output.accept(RWBlocks.VOID_BATTERY_OUTPUT);
						output.accept(RWBlocks.VOID_BATTERY_INPUT);
						output.accept(RWBlocks.VOID_TELEPORT_LINK);
						output.accept(RWBlocks.VOID_TELEPORT_PAD);
						output.accept(RWBlocks.VOID_PORTAL_FRAME);
						output.accept(RWBlocks.VOID_PORTAL_FLUID);
						output.accept(RWBlocks.VOID_PORTAL_STRESS);
						output.accept(RWBlocks.VOID_PORTAL_CONNECTOR);
						output.accept(RWBlocks.VOID_NODE_TERMINAL);
						output.accept(RWItems.PORTABLE_VOID_TERMINAL);
						output.accept(RWItems.POLISHED_AMETHYST);
						output.accept(RWItems.GRAVITON_TUBE);
						output.accept(RWBlocks.LSHAPED_GEARBOX);
						output.accept(RWBlocks.AMETHYST_TILES);
						output.accept(RWBlocks.SMALL_AMETHYST_TILES);
					})
					.build());

	public static void register(IEventBus modEventBus) {
		TAB_REGISTER.register(modEventBus);
	}
}
