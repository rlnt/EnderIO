package com.enderio.base.data.tags;

import com.enderio.base.api.EnderIO;
import com.enderio.base.common.tag.EIOTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class EIOBlockTagsProvider extends BlockTagsProvider {

    public EIOBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, EnderIO.NAMESPACE, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(EIOTags.Blocks.BLOCKS_CONDUCTIVE_ALLOY)
                .addTag(EIOTags.Blocks.BLOCKS_COPPER_ALLOY)
                .addTag(EIOTags.Blocks.BLOCKS_DARK_STEEL)
                .addTag(EIOTags.Blocks.BLOCKS_END_STEEL)
                .addTag(EIOTags.Blocks.BLOCKS_ENERGETIC_ALLOY)
                .addTag(EIOTags.Blocks.BLOCKS_PULSATING_ALLOY)
                .addTag(EIOTags.Blocks.BLOCKS_REDSTONE_ALLOY)
                .addTag(EIOTags.Blocks.BLOCKS_SOULARIUM)
                .addTag(EIOTags.Blocks.BLOCKS_VIBRANT_ALLOY);
    }
}
