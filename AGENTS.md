# Glintwein agent guide

## Project at a glance
- `glintwein` is a client-side Minecraft UI framework/mod, split by concern: shared engine in `common/`, Minecraft-version shims in `version/<mc-version>/`, and loader entrypoints in `mod/<version-loader>/`.
- The important shared runtime flow is: loader entrypoint -> `GlintweinHook` -> singleton `Glintwein` -> `UILayer`/`WindowManager` -> `Context` draw commands.
- External integration points are Fabric, Forge, Mojang mappings, LWJGL, JOML, FastUtil, Gson, and Yoga (`Yoga3_2_2` / `Yoga3_3_3`).

## Architecture to keep in mind
- `common/src/main/java/net/glintwein/Glintwein.java` owns global state, UI layers, `KVStore` load/save, and the optional `-Dglintwein.devtest` `DemoWindow`.
- `common/src/main/java/net/glintwein/platform/Platform.java` is the main abstraction boundary; version-specific implementations live in `version/1.16.5/...` and `version/1.21.4/...`.
- Loader modules only wire Minecraft events: see `mod/1.16.5-fabric/.../GlintweinFabricMod.java` and `mod/1.16.5-forge/.../GlintweinForgeMod.java`.
- Platform setup is order-sensitive: `GlintweinHook.init()` requires `PlatformProvider.set(...)` to have happened first.

## Repo-specific conventions
- Keep shared code Java 8 compatible; both shared and 1.16.5 codepaths target Java 8.
- Put Minecraft/version-specific code in `version/<ver>/...` rather than branching shared code.
- Mixin/resource names are versioned by module: `glintwein.mixins.json` for shared game hooks, plus loader-specific files like `glintwein-fabric.mixins.json`.
- UI input handling uses captured mouse coordinates from `GlobalUIState` to avoid scaling bugs; preserve that pattern in new event code.
- Window ordering is intentional: `WindowManager` walks windows back-to-front for mouse handling and moves a clicked window to the front.

## Build and debug workflows
- Root `settings.gradle` currently includes `:common`, `:version:1.16.5`, and `:mod:1.16.5-fabric`; `mod/1.16.5-forge` and `mod/1.21.4-fabric` are standalone Gradle projects with their own `gradlew.bat`.
- Useful build commands: `gradle :mod:1.16.5-fabric:build` from the root when Gradle is installed, `.
mod\1.16.5-forge\gradlew.bat build`, and `.
mod\1.21.4-fabric\gradlew.bat build`.
- For runtime debugging, inspect the `run/` directories and their `logs/` and `crash-reports/` outputs; mixin failures usually point at the corresponding `glintwein*.mixins.json` file.
- Font assets are generated, not hand-edited: use `font\gen.bat` and the notes in `font\README.md` when updating `common/src/main/resources/assets/fonts/*`.

## When changing code
- If you touch input, HUD rendering, or screen hooks, update both Fabric and Forge event wiring and the matching version platform classes.
- If behavior differs by Minecraft version, prefer the existing `Platform*`, `Mixin*`, or `Yoga*` split instead of adding conditionals in shared code.
- Reuse the existing draw pipeline (`Context`, `Draw*Builder`, render commands) and UI composition patterns (`Element`, `Window`, `UILayer`) rather than introducing a second framework.

