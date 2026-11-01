package dev.ryanhcode.sable.mixinterface.compatibility.scalablelux;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Implemented on ScalableLux's {@code ca.spottedleaf.starlight.common.light.StarLightInterface}, so Sable can drive
 * the ScalableLux light engine for plot chunks without a compile-time dependency on ScalableLux.
 */
public interface StarLightInterfaceExtension {

    /**
     * Fully (re)lights a chunk from scratch, including the sky emptiness map and edges with loaded neighbours.
     * Equivalent to what ScalableLux does for freshly generated chunks.
     */
    void sable$lightChunk(ChunkAccess chunk);

    /**
     * Drops any queued light work for a chunk that is being removed.
     */
    void sable$removeChunkTasks(ChunkPos pos);
}
