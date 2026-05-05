# WhereAreYou Position Sharing Mod Plan

## Summary
Fabric `26.1` / modid `whereareyou` survival-coop position sharing mod. The server is expected to have the mod installed. Position sharing is enabled by default and players can opt out. Commands are not used; Mod Menu, Cloth Config, and keybindings are the control surface.

## Core Behavior
- Client entrypoint, Mod Menu config screen, Cloth Config UI, server sync, HUD, overlay, and keybindings are part of the first implementation.
- Client settings are saved per server. Server settings are controlled by Minecraft OPs only.
- The server only sends data allowed by server settings. Players with sharing disabled are omitted.
- Sync interval defaults to `20 ticks`, with a `1` to `200` tick range, and can be changed live by OPs.
- Display targets are stored by UUID. New players default to HUD and Overlay enabled. The local player appears at the top and defaults to HUD/Overlay disabled.

## UI
- Cloth Config categories: Sharing, HUD, Overlay, Players, Locations, Admin.
- Sharing: own location sharing ON/OFF. Turning it OFF hides the player but still allows viewing others.
- HUD: global ON/OFF, icon/MCID/distance/coordinates/dimension toggles, position preset, X/Y offsets, max players `8`, sort by MCID or distance, and group-by-dimension ON/OFF. Dimension text defaults OFF, group-by-dimension defaults ON, and when coordinates are shown and dimension data is available, coordinate text is tinted by target dimension.
- Overlay: global ON/OFF, distance-scaled labels that sit above same-dimension players' vanilla nameplates in screen space, screen-edge fallback for off-screen targets, content mode, size scale, background opacity, max players uses the HUD limit, hidden for other dimensions.
- Players: online players only, UUID-backed HUD/Overlay toggles, and bulk ON/OFF actions.
- Locations: online players only, MCID, coordinates, dimension, and distance. Distance is hidden across dimensions.
- Admin: OP-only server settings for feature enabled, default sharing, coordinate/distance/dimension permission, and sync interval.

## Keys
- Config screen key: `N`.
- Temporary display key: `B`.
- The display key controls HUD and Overlay together, and can be used as hold or toggle depending on client config.

## Safety
- If the server disables coordinate, distance, or dimension sharing, future packets omit that data and clients clear cached values for the disabled fields.
- No max distance limit.
- Death does not stop visibility.
- Display name is MCID.

## Current Implementation Notes
- Dependencies, entrypoints, networking payloads, server sync, per-server client config, OP-only server config, keybindings, HUD, initial overlay, and Mod Menu/Cloth Config integration are implemented.
- HUD rows render as measured columns so names, distances, coordinates, and dimensions stay visually aligned. HUD offset sliders update live and clamp to `-200..200`.
- Client settings are localized in English and Japanese through `en_us.json` and `ja_jp.json`.
- Players tab is table-style: online players only, skin face icon, MCID, HUD ON/OFF, Overlay ON/OFF, bulk display buttons, and the local player defaults to hidden and appears at the top.
- Locations tab is table-style: online/shared players, skin face icon, MCID, integer block coordinates, dimension, and same-dimension distance. Missing location data shows a no-location/hidden state.
- Server sync currently sends only sharing-enabled players and respects server-side coordinate, distance, and dimension permissions.
- Current overlay implementation exists but still needs focused design/testing: visible same-dimension players get distance-scaled labels above the vanilla nameplate in screen space using interpolated render positions, off-screen or unloaded targets use compact screen-edge markers, overlay content/scale/background opacity are configurable, close labels are nudged in stable MCID/UUID order to reduce overlap, and different dimensions are hidden from overlay. The current visual draft is a Vanilla+ party tag: close labels compact to icon/distance, mid/far labels show name plus distance with distance accented, and screen-edge markers prioritize compact direction/distance.
- HUD and overlay use a restrained Vanilla+ palette: soft black panels, white names, warm gold distance/accent text, and fixed dimension-tinted coordinate text. Overworld coordinates use muted gray, Nether uses strong red-orange, End uses strong purple, and unknown dimensions use pale blue.
- Players and Locations config tables use softer grid lines, row hover fills, ellipsized long names/text, and a two-row bulk-control layout so localized labels do not crowd the row.

## Recent Overlay/HUD Iteration Notes
- `src/main/java/net/misemise/client/render/WhereAreYouHud.java` is the active HUD and overlay implementation. The old world-space overlay path is no longer the focus; player-following labels are rendered in HUD/screen space from projected entity name-tag positions.
- Overlay content is distance-aware: close labels (`<= 12m`) compact to icon plus distance, mid labels (`12m..40m`) are the most readable, far labels (`40m..96m+`) shrink slightly, and edge markers stay compact.
- Overlay distance text is drawn separately from the player name and uses the accent color. This makes distance readable without making the whole label feel louder than vanilla nameplates.
- Screen-edge marker direction was corrected after in-game testing: horizontal direction uses `sx = -sin(relative)` and vertical direction uses `sy = cos(relative)`, based on the active camera yaw and camera position.
- Loaded players only get head-following labels when their projected point is stable and in front of the camera. Unloaded targets, off-screen targets, or unstable projections fall back to screen-edge markers to avoid labels appearing in strange screen positions at long distance.
- HUD list rows are now rendered as measured columns, not prejoined strings. Name and dimension columns are left-aligned; distance and coordinates are right-aligned so rows stay lined up even when MCIDs have different lengths.
- Latest Java verification after the UI polish edits: `.\gradlew compileJava --console plain` succeeded.

## Next Focus
- Continue in a new chat with overlay behavior and visual design as the main topic. Start by reading this file and `AGENTS.md`.
- In-game test the current HUD column layout with short, medium, and long player names. Check that distance, coordinates, and dimension columns line up at the user's preferred HUD scale.
- In-game test the current Vanilla+ color pass in bright, dark, Nether, and End environments. Confirm the warm distance accent is readable without becoming louder than vanilla nameplates.
- In-game test overlay labels at close, mid, and far distances. Confirm close compact labels are useful and not too sparse, and confirm mid/far labels feel readable without covering the world.
- In-game test screen-edge markers in all four directions, including targets above/below the crosshair and behind the player. If direction is still off, inspect the camera-relative fallback math in `WhereAreYouHud`.
- In-game test dense player groups. The current overlap resolver uses stable name/UUID ordering and vertical nudging, but it may still need tuning for close clusters.
- Tune overlay constants only after screenshots: close/mid/far distance thresholds, scale values, background opacity, label gaps, and edge marker size.

## Deferred
- More advanced overlay overlap handling if stable vertical nudging is not enough.
- Stale position age display.
- Additional visual polish after in-game review.
