package dev.ryanhcode.sable.mixinterface.plot.lighting;

import dev.ryanhcode.sable.mixinterface.compatibility.scalablelux.StarLightInterfaceExtension;
import org.jetbrains.annotations.Nullable;

/**
 * Extra state attached to every {@link net.minecraft.world.level.lighting.LevelLightEngine}.
 * <p>
 * Light engine replacements (ScalableLux / Starlight) null out the vanilla {@code blockEngine} and {@code skyEngine}
 * fields, so we remember the constructor arguments ourselves instead of inferring them from those fields.
 */
public interface LevelLightEngineExtension {

    /**
     * @return if this light engine was created with block light enabled
     */
    boolean sable$hasBlockLight();

    /**
     * @return if this light engine was created with sky light enabled
     */
    boolean sable$hasSkyLight();

    /**
     * @return the ScalableLux light interface backing this engine, or null if ScalableLux is not installed
     */
    @Nullable StarLightInterfaceExtension sable$getStarLight();

    /**
     * Sets the ScalableLux light interface backing this engine.
     */
    void sable$setStarLight(@Nullable StarLightInterfaceExtension starLight);
}
