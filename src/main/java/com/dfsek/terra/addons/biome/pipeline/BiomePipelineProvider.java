package com.dfsek.terra.addons.biome.pipeline;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.jafama.FastMath;
import org.jetbrains.annotations.NotNull;

import com.dfsek.terra.addons.biome.pipeline.api.BiomeHolder;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.noise.NoiseSampler;
import com.dfsek.terra.api.world.biome.TerraBiome;
import com.dfsek.terra.api.world.biome.generation.BiomeProvider;


public class BiomePipelineProvider implements BiomeProvider {
    private final LoadingCache<SeededVector, BiomeHolder> holderCache;
    private final BiomePipeline pipeline;
    private final int resolution;
    private final NoiseSampler mutator;
    private final double noiseAmp;
    
    public BiomePipelineProvider(BiomePipeline pipeline, Platform platform, int resolution, NoiseSampler mutator, double noiseAmp) {
        this.resolution = resolution;
        this.mutator = mutator;
        this.noiseAmp = noiseAmp;
        holderCache = CacheBuilder.newBuilder()
                                  .maximumSize(platform == null ? 32 : platform.getTerraConfig().getProviderCache())
                                  .build(
                                          new CacheLoader<>() {
                                              @Override
                                              public BiomeHolder load(@NotNull SeededVector key) {
                                                  return pipeline.getBiomes(key.x, key.z, key.seed);
                                              }
                                          }
                                        );
        this.pipeline = pipeline;
    }
    
    @Override
    public TerraBiome getBiome(int x, int z, long seed) {
        x += mutator.getNoiseSeeded(seed + 1, x, z) * noiseAmp;
        z += mutator.getNoiseSeeded(seed + 2, x, z) * noiseAmp;
        
        
        x = FastMath.floorToInt(FastMath.floorDiv(x, resolution));
        
        z = FastMath.floorToInt(FastMath.floorDiv(z, resolution));
        
        int fdX = FastMath.floorDiv(x, pipeline.getSize());
        int fdZ = FastMath.floorDiv(z, pipeline.getSize());
        return holderCache.getUnchecked(new SeededVector(fdX, fdZ, seed)).getBiome(x - fdX * pipeline.getSize(),
                                                                                   z - fdZ * pipeline.getSize());
    }
    
    private static final class SeededVector {
        private final int x;
        private final int z;
        private final long seed;
        
        private SeededVector(int x, int z, long seed) {
            this.x = x;
            this.z = z;
            this.seed = seed;
        }
        
        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + ((int) (seed ^ (seed >>> 32)));
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if(!(obj instanceof SeededVector)) return false;
            SeededVector that = (SeededVector) obj;
            
            return this.seed == that.seed && this.x == that.x && this.z == that.z;
        }
    }
}
