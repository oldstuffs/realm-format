package io.github.portlek.realmformat.paper.upgrader.v1_17;

import io.github.portlek.realmformat.paper.upgrader.Upgrade;
import io.github.shiruka.nbt.CompoundTag;
import io.github.shiruka.nbt.primitive.StringTag;
import java.util.ArrayList;
import java.util.List;

public class WorldUpgrade1_17 implements Upgrade {

    @Override
    public void upgrade(SlimeLoadedWorld world) {
        for (SlimeChunk chunk : new ArrayList<>(world.getChunks().values())) {
            for (SlimeChunkSection section : chunk.getSections()) {
                if (section == null) {
                    continue;
                }

                List<CompoundTag> palette = section.getPalette().getValue();

                for (CompoundTag blockTag : palette) {
                    Optional<String> name = blockTag.getStringValue("Name");
                    CompoundMap map = blockTag.getValue();

                    // CauldronRenameFix
                    if (name.equals(Optional.of("minecraft:cauldron"))) {
                        Optional<CompoundTag> properties = blockTag.getAsCompoundTag("Properties");
                        if (properties.isPresent()) {
                            String waterLevel = blockTag.getStringValue("level").orElse("0");
                            if (waterLevel.equals("0")) {
                                map.remove("Properties");
                            } else {
                                map.put("Name", new StringTag("Name", "minecraft:water_cauldron"));
                            }
                        }
                    }

                    // Renamed grass path item to dirt path
                    if (name.equals(Optional.of("minecraft:grass_path"))) {
                        map.put("Name", new StringTag("Name", "minecraft:dirt_path"));
                    }
                }
            }
        }
    }

}
