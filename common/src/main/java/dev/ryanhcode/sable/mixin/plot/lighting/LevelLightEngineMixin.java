package dev.ryanhcode.sable.mixin.plot.lighting;

import dev.ryanhcode.sable.mixinterface.compatibility.scalablelux.StarLightInterfaceExtension;
import dev.ryanhcode.sable.mixinterface.plot.lighting.LevelLightEngineExtension;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remembers how a light engine was constructed, independent of which light engine implementation is in use.
 */
@Mixin(LevelLightEngine.class)
public class LevelLightEngineMixin implements LevelLightEngineExtension {

    @Unique
    private boolean sable$hasBlockLight;

    @Unique
    private boolean sable$hasSkyLight;

    @Unique
    private @Nullable StarLightInterfaceExtension sable$starLight;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sable$captureLayers(final LightChunkGetter lightChunkGetter, final boolean blockLight, final boolean skyLight, final CallbackInfo ci) {
        this.sable$hasBlockLight = blockLight;
        this.sable$hasSkyLight = skyLight;
    }

    @Override
    public boolean sable$hasBlockLight() {
        return this.sable$hasBlockLight;
    }

    @Override
    public boolean sable$hasSkyLight() {
        return this.sable$hasSkyLight;
    }

    @Override
    public @Nullable StarLightInterfaceExtension sable$getStarLight() {
        return this.sable$starLight;
    }

    @Override
    public void sable$setStarLight(final @Nullable StarLightInterfaceExtension starLight) {
        this.sable$starLight = starLight;
    }
}
