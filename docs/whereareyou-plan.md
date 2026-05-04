# WhereAreYou Position Sharing Mod Plan

## Summary
Fabric `26.1` / modid `whereareyou` survival-coop position sharing mod. The server is expected to have the mod installed. Position sharing is enabled by default and players can opt out. Commands are not used; Mod Menu, Cloth Config, and keybindings are the control surface.

## Core Behavior
- Client entrypoint, Mod Menu config screen, Cloth Config UI, server sync, HUD, overlay, and keybindings are part of the first implementation.
- Client settings are saved per server. Server settings are controlled by Minecraft OPs only.
- The server only sends data allowed by server settings. Players with sharing disabled are omitted.
- Sync interval defaults to `20 ticks`, with a `10` to `200` tick range, and can be changed live by OPs.
- Display targets are stored by UUID. New players default to HUD and Overlay enabled. The local player appears at the top and defaults to HUD/Overlay disabled.

## UI
- Cloth Config categories: Sharing, HUD, Overlay, Players, Locations, Admin.
- Sharing: own location sharing ON/OFF. Turning it OFF hides the player but still allows viewing others.
- HUD: global ON/OFF, icon/MCID/distance/coordinates/dimension toggles, position preset, X/Y offsets, max players `8`, sort by MCID or distance.
- Overlay: global ON/OFF, simple edge direction markers, max players uses the HUD limit, hidden for other dimensions.
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

## Deferred
- Overlay overlap handling.
- Stale position age display.
- Visual polish after in-game review.
