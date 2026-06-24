package com.xeli.createvoidway.items;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

import static com.xeli.createvoidway.VoidwayMod.REGISTRATE;

public class RWItems {

	public static final ItemEntry<Item> VOID_STEEL_INGOT = ingredient("void_steel_ingot");
	public static final ItemEntry<Item> VOID_STEEL_SHEET = ingredient("void_steel_sheet");
	public static final ItemEntry<Item> POLISHED_AMETHYST = ingredient("polished_amethyst");
	public static final ItemEntry<Item> GRAVITON_TUBE = ingredient("graviton_tube");

	public static final ItemEntry<PortableVoidTerminalItem> PORTABLE_VOID_TERMINAL = REGISTRATE
			.item("portable_void_terminal", PortableVoidTerminalItem::new)
			.properties(p -> p.stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC))
			.register();

	private static ItemEntry<Item> ingredient(String name) {
		return REGISTRATE.item(name, Item::new)
				.register();
	}

	public static void register() {}

}
