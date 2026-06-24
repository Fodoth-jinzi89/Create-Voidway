package com.xeli.createvoidway.items;

import com.xeli.createvoidway.blocks.terminal.PortableVoidTerminalContainer;
import com.xeli.createvoidway.blocks.terminal.VoidNodeService;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalMultiblock;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalTileEntity;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.MenuProvider;

import java.util.List;

public class PortableVoidTerminalItem extends Item {

	public PortableVoidTerminalItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null)
			return InteractionResult.PASS;

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		ItemStack stack = context.getItemInHand();
		InteractionHand hand = context.getHand();
		VoidNodeTerminalTileEntity terminal = VoidNodeTerminalMultiblock.getTerminal(level, pos);

		if (player.isShiftKeyDown()) {
			if (terminal == null)
				return InteractionResult.PASS;
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			if (!(player instanceof ServerPlayer serverPlayer))
				return InteractionResult.SUCCESS;
			return VoidNodeService.bindPortableTerminal(serverPlayer, hand, terminal)
					? InteractionResult.SUCCESS
					: InteractionResult.FAIL;
		}

		if (level.isClientSide)
			return InteractionResult.SUCCESS;

		if (!(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.SUCCESS;

		return tryOpenPortableMenu(serverPlayer, hand, stack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.isClientSide)
			return InteractionResultHolder.success(stack);

		if (!(player instanceof ServerPlayer serverPlayer))
			return InteractionResultHolder.success(stack);

		return tryOpenPortableMenu(serverPlayer, hand, stack)
				? InteractionResultHolder.success(stack)
				: InteractionResultHolder.fail(stack);
	}

	private static boolean tryOpenPortableMenu(ServerPlayer player, InteractionHand hand, ItemStack stack) {
		if (!PortableVoidTerminalBinding.isBound(stack)) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.unbound"), true);
			return false;
		}

		NetworkKey networkKey = PortableVoidTerminalBinding.read(stack, player.registryAccess()).orElse(null);
		if (networkKey == null || !PortableVoidTerminalBinding.isComplete(networkKey)) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.unbound"), true);
			return false;
		}
		if (!PortableVoidTerminalBinding.canInteract(player, networkKey)) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.frequency_denied"),
					true);
			return false;
		}

		player.openMenu(createMenuProvider(player, hand, networkKey), buf -> {
			buf.writeEnum(hand);
			networkKey.writeToBuffer(buf);
		});
		return true;
	}

	private static MenuProvider createMenuProvider(ServerPlayer player, InteractionHand hand, NetworkKey networkKey) {
		return new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.translatable("item.createvoidway.portable_void_terminal");
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inv,
					Player menuPlayer) {
				PortableVoidTerminalContainer menu = new PortableVoidTerminalContainer(
						com.xeli.createvoidway.blocks.voidtypes.RWContainerTypes.PORTABLE_VOID_TERMINAL.get(),
						containerId, inv, hand, networkKey);
				VoidNodeService.sendPortableNodeList(player, hand, networkKey);
				return menu;
			}
		};
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		var registries = context.registries();
		if (registries == null) {
			tooltip.add(Component.translatable("createvoidway.portable_void_terminal.tooltip.unbound"));
			return;
		}
		PortableVoidTerminalBinding.read(stack, registries).filter(PortableVoidTerminalBinding::isComplete)
				.ifPresentOrElse(key -> {
					tooltip.add(Component.translatable("createvoidway.portable_void_terminal.tooltip.frequency",
							key.frequencies.get(true).getStack().getHoverName(),
							key.frequencies.get(false).getStack().getHoverName()));
				}, () -> tooltip.add(Component.translatable("createvoidway.portable_void_terminal.tooltip.unbound")));
	}

}
