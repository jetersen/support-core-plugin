/*
 * The MIT License
 *
 * Copyright (c) 2018, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.jenkins.support.filter;

import com.cloudbees.jenkins.support.util.WordReplacer;
import hudson.Functions;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import edu.umd.cs.findbugs.annotations.NonNull;
import net.jcip.annotations.Immutable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a mapping from some original string to a replacement. Useful both as an individual ContentFilter as well
 * as a persistable class for consistent anonymization mappings.
 *
 * @see ContentMappings
 * @since TODO
 */
@Immutable
@Restricted(NoExternalUse.class)
public class ContentMapping implements ContentFilter {
    private static final String ALT_SEPARATOR = " » ";

    private final String original;
    private final String replacement;
    private final int hashCode;

    private final String[] originals;
    private final String[] replacements;

    private ContentMapping(@NonNull String original, @NonNull String replacement) {
        this.original = original;
        this.replacement = replacement;

        // add flavors of the original string to replace, avoid add when equals
        String slashChangedInOriginal = original.replace("/", ALT_SEPARATOR);
        Set<String> originalsSet = new HashSet<>(4);
        originalsSet.add(original);
        originalsSet.add(Functions.escape(original));
        originalsSet.add(slashChangedInOriginal);
        originalsSet.add(Functions.escape(slashChangedInOriginal));
        originals = originalsSet.toArray(new String[0]);

        // create the replacement array with the same length as the resulting originals
        replacements = new String[originals.length];
        for(int i = 0; i < replacements.length; i++) {
            replacements[i] = replacement;
        }

        this.hashCode = original.hashCode();
    }

    /**
     * Constructs a ContentMapping using an original and replacement value.
     */
    public static ContentMapping of(@NonNull String original, @NonNull String replacement) {
        return new ContentMapping(original, replacement);
    }

    /**
     * @return the original string to replace
     */
    public @NonNull String getOriginal() {
        return original;
    }

    /**
     * @return the replacement string that the originals are replaced with
     */
    public @NonNull String getReplacement() {
        return replacement;
    }

    @Override
    public @NonNull String filter(@NonNull String input) {
        return WordReplacer.replaceWordsIgnoreCase(input, originals, replacements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentMapping that = (ContentMapping) o;
        return Objects.equals(original, that.original);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private Object writeReplace() {
        SerializationProxy proxy = new SerializationProxy();
        proxy.original = original;
        proxy.replacement = replacement;
        return proxy;
    }

    private static class SerializationProxy {
        private String original;
        private String replacement;

        private Object readResolve() {
            return ContentMapping.of(original, replacement);
        }
    }
}
