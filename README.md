# PlanNH

**PlanNH** is a Minecraft 1.7.10 mod for the GTNH pack that adds an interactive flowchart-based production planner. Visually map out recipe chains, configure machine overclocking, and compute throughput balances — all without leaving the game.

## Features

- **Interactive canvas** — pan & zoom, drag nodes, connect ports with edges
- **NEI integration** — press `F8` or click the `FC` sidebar button to open the flowchart, then `+` in any NEI recipe GUI to add it as a node
- **Production balancer** — topological-sort based backward propagation computes per-node operation counts, net item/fluid throughput, and total duration
- **Machine configuration** — per-node overclock and parallel settings with type-safe profile system:
- **Sticky notes** — add text annotations anywhere on the canvas

## Dependencies

- Minecraft **1.7.10** with Forge
- **NotEnoughItems** (required)
- **ModularUI2** (required)
- **GTNHLib** (required)
- **GT5-Unofficial** (optional — enables GregTech recipe extraction and overclock profiles)
- **EnderIO** (optional — enables EnderIO recipe extraction and speed profile)

## Building

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## License

MIT — see [LICENSE](LICENSE).
