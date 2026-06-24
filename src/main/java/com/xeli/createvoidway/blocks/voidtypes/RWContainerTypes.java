package com.xeli.createvoidway.blocks.voidtypes;

import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestContainer;
import com.xeli.createvoidway.blocks.voidtypes.chest.VoidChestScreen;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalScreen;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;

import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWContainerTypes {

	public static final MenuEntry<VoidChestContainer> VOID_CHEST =
			register("void_chest", VoidChestContainer::new, () -> VoidChestScreen::new);

	public static final MenuEntry<VoidNodeTerminalContainer> VOID_NODE_TERMINAL =
			register("void_node_terminal", VoidNodeTerminalContainer::new, () -> VoidNodeTerminalScreen::new);

	public static final MenuEntry<PortableVoidTerminalContainer> PORTABLE_VOID_TERMINAL =
			register("portable_void_terminal", PortableVoidTerminalContainer::new, () -> PortableVoidTerminalScreen::new);

	private static <C extends AbstractContainerMenu, S extends Screen & MenuAccess<C>> MenuEntry<C> register(
			String name, MenuBuilder.ForgeMenuFactory<C> factory, NonNullSupplier<MenuBuilder.ScreenFactory<C, S>> screenFactory) {
		return REGISTRATE.menu(name, factory, screenFactory).register();
	}

	public static void register() {}

}
