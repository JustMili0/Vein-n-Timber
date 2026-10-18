package net.justmili.vnt.core.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.function.Predicate;

// Stripped down tag util from Millie's Core Libraries. Temporary until I switch this to actually make this use the library
public class TagUtil {

    public static Predicate<BlockState> matchBlockTags(BlockState state, String namespace, String containsPath) {
        var matchingTags = new ArrayList<TagKey<Block>>();
        var tagIterator = state./*? if <26.1 {*//*getTags()*//*?} else {*/tags()/*?}*/.iterator();
        while (tagIterator.hasNext()) {
            var tag = tagIterator.next();
            var tagId = tag.location();
            if (tagId.getNamespace().equals(namespace) && tagId.getPath().startsWith(containsPath)) matchingTags.add(tag);
        }

        // Same block as the given one always has the tags, so comparing it first skips looking them up
        var block = state.getBlock();
        if (matchingTags.isEmpty()) return s -> s.is(block);
        if (matchingTags.size() == 1) {
            // .get(0); for possible 1.20.1 port (Java 17)
            return s -> s.is(block) || s.is(matchingTags.get(0));
        }

        return s -> {
            if (s.is(block)) return true;
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