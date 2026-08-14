# MipMapPlus

Extended mipmaps for Minecraft: Java Edition 26.2.

MipMapPlus is an independent, client-side Fabric restoration of higher mipmap
levels for Minecraft 26.2. It lets high-resolution resource packs keep a deeper
mipmap chain instead of having the entire block atlas limited by one smaller
texture.

Created and maintained by
[arogorn993-hue](https://github.com/arogorn993-hue).

## What it does

- Restores mipmap choices beyond Minecraft's normal level-4 limit, through
  level 10.
- Prepares undersized block sprites for the selected mipmap chain while leaving
  textures that are already large enough at their native resolution.
- Reduces distant texture shimmer and visual noise, especially with
  high-resolution resource packs.
- Extends the mipmap control in both Minecraft's standard Video Settings and
  Sodium's Quality settings.
- Runs entirely on the client. It is not needed on a dedicated server.

## Recommended setup

The tested sweet spot is around **Mipmap Levels: 6**. It gives a strong
reduction in distant shimmer without the much larger atlas and memory cost of
the highest settings.

For the cleanest tested result, use level 6 with:

- [Sodium](https://modrinth.com/mod/sodium)
- [Salt's Anti-Aliasing](https://modrinth.com/mod/salts-anti-aliasing) set to
  **FSR3 Super Resolution**

Both companion mods are optional. The ideal level still depends on resource-pack
resolution, available GPU memory, display resolution, and personal preference.
If resource reloads fail or memory use is too high, lower the mipmap level.

## Choosing a mipmap level

Each level completes the chain for a matching square texture size:

| Mipmap level | Full chain |
| ---: | ---: |
| 4 | 16 -> 1 |
| 5 | 32 -> 1 |
| 6 | 64 -> 1 |
| 7 | 128 -> 1 |
| 8 | 256 -> 1 |
| 9 | 512 -> 1 |
| 10 | 1024 -> 1 |

MipMapPlus exposes level 10, but the block atlas must still fit within the GPU's
maximum texture dimension. A full pack dominated by 512px textures will
normally reach level 9 on a 32768px atlas. If the requested level cannot fit,
MipMapPlus falls back gracefully to the highest feasible level instead of
failing the resource reload. More VRAM alone does not raise the GPU's maximum
texture dimension.

## Installation

1. Install Fabric Loader for Minecraft 26.2.
2. Download MipMapPlus from
   [GitHub Releases](https://github.com/arogorn993-hue/MipMapPlus/releases) or
   its Modrinth page.
3. Put the MipMapPlus JAR in the client's `mods` folder.
4. Start Minecraft and choose a mipmap level in Video Settings. Begin with 6.

Changing the mipmap level reloads resources. Higher settings can take longer to
reload and use substantially more texture-atlas memory.

## Compatibility

- Minecraft: Java Edition 26.2
- Fabric Loader
- Client-side only
- Sodium integration is included when Sodium is installed
- Salt's Anti-Aliasing is optional; FSR3 Super Resolution was tested alongside
  MipMapPlus at mipmap level 6
- Do not install MipMapPlus alongside Better Mipmaps or another mod that changes
  the same extended-mipmap and atlas-stitching behavior

## Credits

MipMapPlus builds on the ideas and original implementation of
[sidit77's Better Mipmaps](https://github.com/sidit77/better-mipmaps). Thank you
to sidit77 for making Better Mipmaps available under the MIT License.

This is an independent Minecraft 26.2 restoration maintained at
[arogorn993-hue/MipMapPlus](https://github.com/arogorn993-hue/MipMapPlus).
It is not an official release by sidit77.

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for the original project's
MIT notice.

## Support and community

- [Report a bug](https://github.com/arogorn993-hue/MipMapPlus/issues/new?template=bug-report.yml)
- [Request a feature](https://github.com/arogorn993-hue/MipMapPlus/issues/new?template=feature-request.yml)
- [Ask questions or share results](https://github.com/arogorn993-hue/MipMapPlus/discussions)

See [SUPPORT.md](SUPPORT.md) for the information that makes texture and atlas
problems easier to reproduce.

## License

MipMapPlus is available under the [MIT License](LICENSE).

If MipMapPlus helps your game look better, you can optionally
[buy me a coffee](https://buymeacoffee.com/arogorn993hue).
