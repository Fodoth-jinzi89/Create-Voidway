# Create: Voidway

**Version:** `0.2.2` · Minecraft **1.21.1** · NeoForge

A Create addon (中文名：**机械动力：虚空通道**) that extends [Create Utilities](https://github.com/Duqueeee/create-utilities) with frequency-linked void logistics, a void node terminal, teleport pads, and stress/fluid-powered portals.

## About

Create: Voidway lets you move **stress**, **items**, **fluids**, and **energy (FE)** across distances without physical pipes or belts. Devices on the same **frequency channel** and **owner** form a void network. Many blocks consume **void transfer fluid** while operating and show an end-portal-style overlay when active.

This project is a fork of the unofficial NeoForge port by SmartStreamLabs, maintained by [qwer854645](https://github.com/qwer854645).

## Mod Contents

### Void logistics (input / output pairs)

Each resource type has dedicated **input** and **output** blocks. Set the same frequency on linked devices to transfer across the void.

| Type | Input | Output | Transfers |
|------|-------|--------|-----------|
| Stress | Void Motor Input | Void Motor Output | Rotational speed / SU |
| Items | Void Chest Input | Void Chest Output | Item stacks (filterable) |
| Fluids | Void Tank Input | Void Tank Output | Fluids (filterable); side windows show end-portal animation when open |
| Energy | Void Battery Input | Void Battery Output | Forge Energy (FE) |

All void I/O blocks support **frequency slots**, **owner assignment**, shaft connection on the bottom, and in-game **Ponder** tutorials.

### Void Node Terminal

A two-block multiblock terminal for managing your void network:

- Browse all linked nodes on the current frequency
- Rename nodes for easier identification
- Teleport directly to a selected node (player transport)
- Requires stress input and void transfer fluid while running

### Void teleport

- **Void Teleport Link** — pairs two teleport endpoints on a frequency
- **Void Teleport Pad** — instant entity transport between paired pads on the same channel

### Void portals

Build frame-based portals fueled by **stress** and **void transfer fluid**:

- **Void Portal Frame** — portal structure block
- **Void Portal Stress Port** — supplies rotational power to the portal
- **Void Portal Fluid Port** — supplies void transfer fluid
- **Void Portal Connector** — links portal segments
- **Void Portal** — the active portal block (formed in-world)

Portal shape detection, stress routing, and fluid consumption are configurable via mod config.

### Materials & decoration

Crafting and building blocks from the original Create Utilities set:

- **Void Steel** — ingot, sheet, block, scaffolding, ladder, bars
- **Void Casing** — encasing block for void-themed builds
- **Graviton Tube**, **Polished Amethyst**
- **Amethyst Tiles**, **Small Amethyst Tiles**

### Fluids

- **Void Transfer Fluid** — operating catalyst consumed by void devices, portal ports, and the node terminal

### Kinetics

- **L-Shaped Gearbox** — corner gearbox variant

### In-game guides (Ponder)

Ponder scenes cover void motors, chests, tanks, batteries, I/O pairing, the node terminal, teleport pads/links, portals, the L-shaped gearbox, and void transfer fluid.

## Requirements

| Component | Version |
|-----------|---------|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.x` (tested on `21.1.222`) |
| Create | `6.0.9+` |
| Java | `21` |

## Build

```bash
./gradlew build
```

Output JAR: `build/libs/createvoidway-0.2.2+mc1.21.1.jar`

Run the development client:

```bash
./gradlew runClient
```

## Credits

| Contributor | Role |
|-------------|------|
| [Duqueeee](https://github.com/Duqueeee) | Original **Create Utilities** |
| [SmartStreamLabs](https://github.com/SmartStreamLabs/Create-Utilities-Unofficial-Port-) | Unofficial NeoForge port |
| [qwer854645](https://github.com/qwer854645) | Create: Voidway fork and extensions |

Licensed under the [MIT License](LICENSE). The license retains copyright notices for Duqueeee (2023), SmartStreamLabs (2024), and qwer854645 (2026).

## AI-Assisted Development Disclosure

This project was created with AI assistance.
