# MipMapPlus

MipMapPlus is an independent, client-side Fabric restoration of higher mipmap
levels for **Minecraft: Java Edition 26.2**.

Created and maintained by
[arogorn993-hue](https://github.com/arogorn993-hue).

Minecraft normally limits mipmaps to level 4. With high-resolution resource
packs, that can leave distant textures noisy or shimmery. MipMapPlus restores
higher choices and prepares undersized block sprites so one smaller texture does
not shorten the mipmap chain for the entire block atlas.

## Recommended setup

The tested sweet spot is around **Mipmap Levels: 6**. It provides a strong
visual improvement without the much larger atlas and memory cost of the highest
levels.

For the cleanest tested result, pair level 6 with:

- [Sodium](https://modrinth.com/mod/sodium)
- [Salt's Anti-Aliasing](https://modrinth.com/mod/salts-anti-aliasing) using
  **FSR3 Super Resolution**

Sodium and Salt's Anti-Aliasing are optional. Results and memory use vary with
your resource pack, GPU, and display resolution.

Do not install MipMapPlus alongside Better Mipmaps or another mod that changes
the same extended-mipmap and atlas-stitching behavior.

## Features

- Mipmap choices through level 10 in standard Video Settings
- Matching mipmap range in Sodium's Quality settings
- Automatic preparation of undersized block sprites
- Native-size handling for textures that are already large enough
- Graceful fallback to the highest level that fits the texture atlas
- Client-only operation; nothing is required on the server

Level 10 is the complete 1024 -> 1 chain. A full pack dominated by 512px
textures will normally reach level 9 on a 32768px atlas. The GPU's maximum
texture dimension remains a hard limit even when additional VRAM is available.

## Install

Install Fabric Loader for Minecraft 26.2, place the MipMapPlus JAR in your
client's `mods` folder, and select a mipmap level in Video Settings. Start at 6
and lower it if resource reloads fail or texture memory is limited.

## Credits and source

MipMapPlus builds on the ideas and original implementation of
[sidit77's Better Mipmaps](https://github.com/sidit77/better-mipmaps). Thank you
to sidit77 for publishing Better Mipmaps under the MIT License. MipMapPlus is an
independent Minecraft 26.2 restoration and is not an official sidit77 release.

Source, issue tracker, license, and third-party notices are available on
[GitHub](https://github.com/arogorn993-hue/MipMapPlus).

- [Report a bug](https://github.com/arogorn993-hue/MipMapPlus/issues/new?template=bug-report.yml)
- [Ask questions or share results](https://github.com/arogorn993-hue/MipMapPlus/discussions)

If MipMapPlus helps your game look better, you can optionally
[buy me a coffee](https://buymeacoffee.com/arogorn993hue).
