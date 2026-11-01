package dev.ryanhcode.sable.mixin.compatibility.scalablelux;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.compatibility.scalablelux.StarLightInterfaceExtension;
import dev.ryanhcode.sable.mixinterface.plot.lighting.LevelLightEngineExtension;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ScalableLux compatibility.
 * <p>
 * ScalableLux stores light inside the chunks themselves and finds them through {@code getAnyChunkNow}, which on the
 * server only looks at the vanilla chunk map. Plot chunks are owned by Sable, so they have to be resolved here.
 * <p>
 * Only applied when ScalableLux is installed (see {@code AbstractSableMixinPlugin}).
 */
@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightInterface", remap = false)
public abstract class StarLightInterfaceMixin implements StarLightInterfaceExtension {

    @Shadow(remap = false)
    @Final
    protected Level world;

    @Shadow(remap = false)
    @Final
    public LevelLightEngine lightEngine;

    @Shadow(remap = false)
    public abstract void lightChunk(ChunkAccess chunk, Boolean[] emptySections);

    @Shadow(remap = false)
    public abstract void removeChunkTasks(ChunkPos pos);

    /**
     * Links the ScalableLux light interface back to the {@link LevelLightEngine} that owns it.
     */
    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void sable$linkLightEngine(final LightChunkGetter lightAccess, final boolean hasSkyLight, final boolean hasBlockLight, final LevelLightEngine engine, final CallbackInfo ci) {
        if (engine != null) {
            ((LevelLightEngineExtension) engine).sable$setStarLight(this);
        }
    }

    /**
     * Resolves plot chunks for plot light engines.
     * The main (threaded) level light engine never sees plot chunks, as plots are lit by their own engine.
     */
    @Inject(method = "getAnyChunkNow", at = @At("HEAD"), cancellable = true, remap = false)
    private void sable$getPlotChunk(final int chunkX, final int chunkZ, final CallbackInfoReturnable<ChunkAccess> cir) {
        if (!(this.world instanceof ServerLevel)) {
            // The client chunk cache already resolves plot chunks
            return;
        }

        final SubLevelContainer container = SubLevelContainer.getContainer(this.world);

        if (container == null || !container.inBounds(chunkX, chunkZ)) {
            return;
        }

        if (this.lightEngine instanceof ThreadedLevelLightEngine) {
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(container.getChunk(new ChunkPos(chunkX, chunkZ)));
    }

    @Override
    public void sable$lightChunk(final ChunkAccess chunk) {
        final LevelChunkSection[] sections = chunk.getSections();
        final Boolean[] emptySections = new Boolean[sections.length];

        for (int i = 0; i < sections.length; i++) {
            emptySections[i] = sections[i] == null || sections[i].hasOnlyAir();
        }

        this.lightChunk(chunk, emptySections);
    }

    @Override
    public void sable$removeChunkTasks(final ChunkPos pos) {
        this.removeChunkTasks(pos);
    }
}
