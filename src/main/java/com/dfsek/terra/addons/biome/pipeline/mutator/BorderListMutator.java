/*
 * Copyright (c) 2020-2021 Polyhedral Development
 *
 * The Terra Core Addons are licensed under the terms of the MIT License. For more details,
 * reference the LICENSE file in this module's root directory.
 */

package com.dfsek.terra.addons.biome.pipeline.mutator;

import java.util.Map;

import com.dfsek.terra.addons.biome.pipeline.api.BiomeMutator;
import com.dfsek.terra.api.noise.NoiseSampler;
import com.dfsek.terra.api.util.collection.ProbabilityCollection;
import com.dfsek.terra.api.world.biome.TerraBiome;


public class BorderListMutator implements BiomeMutator {
    private final String border;
    private final NoiseSampler noiseSampler;
    private final ProbabilityCollection<TerraBiome> replaceDefault;
    private final String defaultReplace;
    private final Map<TerraBiome, ProbabilityCollection<TerraBiome>> replace;
    
    public BorderListMutator(Map<TerraBiome, ProbabilityCollection<TerraBiome>> replace, String border, String defaultReplace,
                             NoiseSampler noiseSampler, ProbabilityCollection<TerraBiome> replaceDefault) {
        this.border = border;
        this.noiseSampler = noiseSampler;
        this.replaceDefault = replaceDefault;
        this.defaultReplace = defaultReplace;
        this.replace = replace;
    }
    
    @Override
    public TerraBiome mutate(ViewPoint viewPoint, double x, double z, long seed) {
        TerraBiome origin = viewPoint.getBiome(0, 0);
        if(origin.getTags().contains(defaultReplace)) {
            for(int xi = -1; xi <= 1; xi++) {
                for(int zi = -1; zi <= 1; zi++) {
                    if(xi == 0 && zi == 0) continue;
                    TerraBiome current = viewPoint.getBiome(xi, zi);
                    if(current == null) continue;
                    if(current.getTags().contains(border)) {
                        if(replace.containsKey(origin)) {
                            TerraBiome biome = replace.get(origin).get(noiseSampler, x, z, seed);
                            return biome == null ? origin : biome;
                        }
                        TerraBiome biome = replaceDefault.get(noiseSampler, x, z, seed);
                        return biome == null ? origin : biome;
                    }
                }
            }
        }
        return origin;
    }
}
