package dev.ryanhcode.sable.neoforge.gametest;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.plot.lighting.LevelLightEngineExtension;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.joml.Vector3d;

import static dev.ryanhcode.sable.neoforge.gametest.SableTestHelper.absolutePosition;
import static dev.ryanhcode.sable.neoforge.gametest.SableTestHelper.removeSubLevel;
import static dev.ryanhcode.sable.neoforge.gametest.SableTestHelper.spawnSubLevel;

/**
 * Checks that sub-level plots are lit correctly, with and without ScalableLux.
 */
@GameTestHolder(Sable.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LightingTest {

    @GameTest(template = "physicstest.continuouscollision", timeoutTicks = 100, batch = "sable_dm_lighting")
    public static void testPlotLighting(final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final ServerSubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            throw new IllegalStateException("Plot container not found in level");
        }

        final boolean scalableLux = ModList.get().isLoaded("scalablelux");
        final BlockPos light = BlockPos.ZERO;

        final ServerSubLevel subLevel = spawnSubLevel(container, absolutePosition(helper, new Vector3d(2.5, 4, 1.5)), accessor -> {
            accessor.setBlock(light, Blocks.GLOWSTONE.defaultBlockState(), 3);
        });

        final LevelPlot plot = subLevel.getPlot();
        final BlockPos center = plot.getCenterBlock();

        helper.runAtTickTime(5, () -> {
            final LevelLightEngine engine = plot.getLightEngine();
            final LevelLightEngineExtension extension = (LevelLightEngineExtension) engine;

            final String error = check(level, engine, extension, scalableLux, center.offset(light).east(), center.offset(light).below());
            removeSubLevel(container, subLevel);

            if (error != null) {
                helper.fail("Plot lighting (ScalableLux=" + scalableLux + "): " + error);
                return;
            }

            Sable.LOGGER.info("[sable-dm] Plot lighting test passed (ScalableLux={})", scalableLux);
            helper.succeed();
        });
    }

    private static String check(final ServerLevel level, final LevelLightEngine engine, final LevelLightEngineExtension extension,
                                final boolean scalableLux, final BlockPos litPos, final BlockPos shadedPos) {
        if (scalableLux != (extension.sable$getStarLight() != null)) {
            return "ScalableLux light interface " + (scalableLux ? "missing" : "unexpected");
        }

        if (!extension.sable$hasBlockLight()) {
            return "plot light engine has no block light";
        }

        final int blockLight = engine.getLayerListener(LightLayer.BLOCK).getLightValue(litPos);
        if (blockLight != 14) {
            return "expected block light 14 next to glowstone, got " + blockLight;
        }

        final int levelBlockLight = level.getBrightness(LightLayer.BLOCK, litPos);
        if (levelBlockLight != 14) {
            return "expected level block brightness 14 next to glowstone, got " + levelBlockLight;
        }

        if (level.dimensionType().hasSkyLight()) {
            if (!extension.sable$hasSkyLight()) {
                return "plot light engine has no sky light";
            }

            final int shadedSkyLight = engine.getLayerListener(LightLayer.SKY).getLightValue(shadedPos);
            if (shadedSkyLight != 14) {
                return "expected sky light 14 below glowstone, got " + shadedSkyLight;
            }

            final int openSkyLight = engine.getLayerListener(LightLayer.SKY).getLightValue(shadedPos.above(8));
            if (openSkyLight != 15) {
                return "expected sky light 15 above glowstone, got " + openSkyLight;
            }
        }

        final ClientboundLightUpdatePacketData packet = new ClientboundLightUpdatePacketData(new ChunkPos(litPos), engine, null, null);

        Sable.LOGGER.info("[sable-dm] ScalableLux={} packet block mask={} sky mask={} empty sky mask={}",
                scalableLux, packet.getBlockYMask(), packet.getSkyYMask(), packet.getEmptySkyYMask());

        if (packet.getBlockYMask().isEmpty()) {
            return "light packet for the plot chunk has no block light";
        }

        if (level.dimensionType().hasSkyLight() && packet.getSkyYMask().isEmpty()) {
            return "light packet for the plot chunk has no sky light";
        }

        return null;
    }
}
