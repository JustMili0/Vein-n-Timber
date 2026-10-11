package net.justmili.vnt.core.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.function.Predicate;

public class TagUtil {

    public static Predicate<BlockState> matchBlockTags(BlockState state, String namespace, String containsPath) {
        var matchingTags = new ArrayList<TagKey<Block>>();
        var tagIterator = state./*? if < 26.1 {*//*getTags()*//*?} else {*/tags()/*?}*/.iterator();
        while (tagIterator.hasNext()) {
            var tag = tagIterator.next();
            var tagId = tag.location();
            if (tagId.getNamespace().equals(namespace) && tagId.getPath().startsWith(containsPath)) matchingTags.add(tag);
        }

        if (matchingTags.isEmpty()) return s -> s.is(state.getBlock());
        return s -> {
            for (var tag : matchingTags) {
                if (s.is(tag)) return true;
            }
            return false;
        };
    }

    public static Predicate<BlockState> matchOreTags(BlockState state) {
        return matchBlockTags(state, "c", "ores/");
    }
}