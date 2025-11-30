package com.nnpg.glazed.modules.main;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.HashSet;
import java.util.Set;

public class LightDebugger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How far to scan for light levels.")
        .defaultValue(15)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Integer> lightThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("light-threshold")
        .description("Anything below this light level is considered unsafe.")
        .defaultValue(7)
        .min(0)
        .max(15)
        .sliderMax(15)
        .build()
    );

    private final Setting<Boolean> showSafe = sgGeneral.add(new BoolSetting.Builder()
        .name("show-safe")
        .description("Highlights blocks that are safe (light level >= threshold).")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showUnsafe = sgGeneral.add(new BoolSetting.Builder()
        .name("show-unsafe")
        .description("Highlights blocks that are unsafe (light level < threshold).")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> safeSideColor = sgRender.add(new ColorSetting.Builder()
        .name("safe-side-color")
        .description("Side color for safe lit blocks.")
        .defaultValue(new SettingColor(0, 255, 0, 40))
        .build()
    );

    private final Setting<SettingColor> safeLineColor = sgRender.add(new ColorSetting.Builder()
        .name("safe-line-color")
        .description("Line color for safe lit blocks.")
        .defaultValue(new SettingColor(0, 255, 0, 200))
        .build()
    );

    private final Setting<SettingColor> unsafeSideColor = sgRender.add(new ColorSetting.Builder()
        .name("unsafe-side-color")
        .description("Side color for unsafe dark blocks.")
        .defaultValue(new SettingColor(255, 0, 0, 40))
        .build()
    );

    private final Setting<SettingColor> unsafeLineColor = sgRender.add(new ColorSetting.Builder()
        .name("unsafe-line-color")
        .description("Line color for unsafe dark blocks.")
        .defaultValue(new SettingColor(255, 0, 0, 200))
        .build()
    );

    public LightDebugger() {
        super(Categories.World, "light-debugger", "Highlights safe and unsafe light levels.");
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        Set<BlockPos> rendered = new HashSet<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);

                    BlockState state = mc.world.getBlockState(pos);
                    if (state.getBlock() == Blocks.AIR) continue;

                    int light = mc.world.getLightLevel(pos);

                    boolean safe = light >= lightThreshold.get();
                    boolean unsafe = light < lightThreshold.get();

                    if (safe && !showSafe.get()) continue;
                    if (unsafe && !showUnsafe.get()) continue;

                    if (rendered.contains(pos)) continue;
                    rendered.add(pos);

                    Box box = new Box(pos);

                    if (safe) {
                        RenderUtils.box(event, box, safeSideColor.get(), safeLineColor.get(), true);
                    } else {
                        RenderUtils.box(event, box, unsafeSideColor.get(), unsafeLineColor.get(), true);
                    }
                }
            }
        }
    }
}
