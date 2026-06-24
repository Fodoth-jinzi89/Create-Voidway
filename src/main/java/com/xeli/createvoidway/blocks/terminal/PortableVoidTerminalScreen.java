package com.xeli.createvoidway.blocks.terminal;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalRequestListPacket;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalTeleportPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class PortableVoidTerminalScreen extends AbstractSimiContainerScreen<PortableVoidTerminalContainer> {

	private static final int ROWS = 6;

	private Button scrollUpButton;
	private Button scrollDownButton;
	private final List<NodeRowWidget> nodeRowWidgets = new ArrayList<>();
	private int scrollOffset;
	private boolean listRequested;

	public PortableVoidTerminalScreen(PortableVoidTerminalContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		this.imageWidth = 220;
		this.imageHeight = 166;
	}

	public void onNodesUpdated() {
		scrollOffset = Math.min(scrollOffset, Math.max(0, menu.getNodes().size() - ROWS));
		rebuildScrollButtons();
		rebuildNodeRows();
	}

	@Override
	protected void init() {
		super.init();
		if (!listRequested) {
			PacketDistributor.sendToServer(new PortableVoidTerminalRequestListPacket(menu.getHand(),
					menu.getNetworkKey()));
			listRequested = true;
		}

		addRenderableWidget(Button.builder(Component.translatable("createvoidway.void_node_terminal.teleport"),
				b -> sendTeleport()).bounds(leftPos + 8, topPos + 138, 100, 18).build());

		rebuildScrollButtons();
		rebuildNodeRows();
	}

	private void rebuildScrollButtons() {
		if (scrollUpButton != null) {
			removeWidget(scrollUpButton);
			scrollUpButton = null;
		}
		if (scrollDownButton != null) {
			removeWidget(scrollDownButton);
			scrollDownButton = null;
		}

		List<VoidNodeEntry> nodes = menu.getNodes();

		if (scrollOffset > 0)
			scrollUpButton = addRenderableWidget(Button.builder(Component.literal("\u25B2"), b -> scroll(-1))
					.bounds(leftPos + 200, topPos + 20, 12, 12).build());
		if (scrollOffset + ROWS < nodes.size())
			scrollDownButton = addRenderableWidget(Button.builder(Component.literal("\u25BC"), b -> scroll(1))
					.bounds(leftPos + 200, topPos + 20 + ROWS * 18, 12, 12).build());
	}

	private void rebuildNodeRows() {
		for (NodeRowWidget widget : nodeRowWidgets)
			removeWidget(widget);
		nodeRowWidgets.clear();

		List<VoidNodeEntry> nodes = menu.getNodes();
		for (int row = 0; row < ROWS; row++) {
			int index = scrollOffset + row;
			if (index >= nodes.size())
				break;
			NodeRowWidget widget = new NodeRowWidget(leftPos + 8, topPos + 22 + row * 18, 188, 16, index);
			nodeRowWidgets.add(widget);
			addRenderableWidget(widget);
		}
	}

	private void scroll(int delta) {
		scrollOffset = Math.max(0, Math.min(scrollOffset + delta, Math.max(0, menu.getNodes().size() - ROWS)));
		rebuildScrollButtons();
		rebuildNodeRows();
	}

	private void selectNode(int index) {
		menu.setSelectedIndex(index);
		rebuildNodeRows();
	}

	private void sendTeleport() {
		VoidNodeEntry selected = menu.getSelectedNode();
		if (selected == null || selected.currentTerminal())
			return;
		PacketDistributor.sendToServer(new PortableVoidTerminalTeleportPacket(menu.getHand(), menu.getNetworkKey(),
				selected.dimension(), selected.pos()));
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
		graphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF202020);
		graphics.drawString(font, title, leftPos + 8, topPos + 8, 0xFFFFFF, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		if (menu.getNodes().isEmpty())
			graphics.drawString(font, Component.translatable("createvoidway.void_node_terminal.no_nodes"),
					leftPos + 8, topPos + 24, 0xAAAAAA, false);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
	}

	private final class NodeRowWidget extends AbstractWidget {

		private final int nodeIndex;

		private NodeRowWidget(int x, int y, int width, int height, int nodeIndex) {
			super(x, y, width, height, Component.empty());
			this.nodeIndex = nodeIndex;
		}

		@Override
		protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			List<VoidNodeEntry> nodes = menu.getNodes();
			if (nodeIndex < 0 || nodeIndex >= nodes.size())
				return;

			VoidNodeEntry entry = nodes.get(nodeIndex);
			int bg = nodeIndex == menu.getSelectedIndex() ? 0xFF3A5F8A : 0xFF2A2A2A;
			graphics.fill(getX(), getY(), getX() + width, getY() + height, bg);
			if (isHovered())
				graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);

			String label = entry.displayName();
			if (entry.currentTerminal())
				label = label + " *";

			String distanceText = entry.distanceText().getString();
			int distanceWidth = font.width(distanceText);
			int maxNameWidth = width - distanceWidth - 12;
			label = font.plainSubstrByWidth(label, Math.max(0, maxNameWidth));

			graphics.drawString(font, label, getX() + 4, getY() + 4, 0xFFFFFF, false);
			graphics.drawString(font, distanceText, getX() + width - distanceWidth - 4, getY() + 4, 0xAAAAAA, false);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			selectNode(nodeIndex);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}

	}

}
