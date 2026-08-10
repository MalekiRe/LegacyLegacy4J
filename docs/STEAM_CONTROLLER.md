# Steam Controller setup and diagnostics

Legacy4J 1.2.5 reads the virtual gamepad created by Steam Input. It does not require Steamworks and does not communicate with the Steam Controller directly.

## Recommended Steam Input layout

Add the launcher or instance to Steam as a non-Steam game and apply the **Gamepad** template:

- left pad/stick: Left Joystick
- right pad: Right Joystick (trackball behavior is optional)
- left/right triggers: Left Trigger and Right Trigger
- face buttons, bumpers, Back and Start: their matching gamepad controls
- directional pad: D-pad

Launch the instance from the Steam shortcut. Steam Input must create its virtual controller before LWJGL 2 initializes.

## Verify the device in Minecraft

Open **Help & Options → Controller Settings → Controller Mapping & Diagnostics**. The screen shows every controller reported by LWJGL, its axis/button counts, and the component assigned to each Legacy action. Use **Rescan Controllers** if Steam Input was enabled after the game started.

Axis values of `Auto` use name detection followed by the conventional XInput ordering. Every axis and button can be assigned explicitly when an older driver reports unusual names. The generated file is `.minecraft/config/legacy4j-1.2.5.properties`.

## Linux permissions

Steam's virtual controller is preferred because the JInput version bundled with Minecraft 1.2.5 may be unable to open modern `/dev/input/event*` devices. Permission-denied messages in the log mean the operating-system user cannot read those event nodes; they do not mean the mod failed to load. Launch through Steam first, or install the appropriate distribution-specific input/udev rules, then rescan.

If both triggers appear active or reversed, change **Combined Trigger Polarity**. If the virtual controller exposes separate trigger axes, assign **Left Trigger Axis** and **Right Trigger Axis** in the mapping screen instead.
