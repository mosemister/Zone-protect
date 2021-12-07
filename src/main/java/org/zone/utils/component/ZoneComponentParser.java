package org.zone.utils.component;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.zone.utils.component.parsers.ComponentPartParser;
import org.zone.utils.component.parsers.colour.ComponentColourParser;

import java.util.*;

public class ZoneComponentParser {

    public static final Set<ComponentPartParser> PARTS = new HashSet<>(Arrays.asList(new ComponentColourParser()));

    public static @NotNull String toString(Component component) {
        String message = PlainTextComponentSerializer.plainText().serialize(component);
        StringBuilder builder = new StringBuilder();
        for (ComponentPartParser part : PARTS) {
            if (part.hasTag(component)) {
                builder.append(part.withTag(component, message));
                break;
            }
        }
        builder.append(message);
        for (Component child : component.children()) {
            builder.append(toString(child));
        }
        return builder.toString();
    }

    public static @NotNull Component fromString(String string) {
        Integer tagStart = null;
        StringBuilder buffer = new StringBuilder();
        Component component = null;
        Map<ComponentPartParser, String> applyTo = new HashMap<>();

        for (int current = 0; current < string.length(); current++) {
            char character = string.charAt(current);
            if (character == '<' && tagStart == null) {
                String previous = buffer.toString();
                if (!previous.isEmpty()) {
                    Component toAdd = Component.text(previous);
                    for (Map.Entry<ComponentPartParser, String> entry : applyTo.entrySet()) {
                        toAdd = entry.getKey().withTag(entry.getValue(), toAdd);
                    }
                    if (component == null) {
                        component = toAdd;
                    } else {
                        component.append(toAdd);
                    }
                }
                applyTo.clear();
                buffer = new StringBuilder();
                tagStart = current;
                continue;
            }
            if (character == '>' && tagStart != null) {
                String tag = string.substring(tagStart, current);
                for (ComponentPartParser part : PARTS) {
                    if (part.hasTag(tag)) {
                        applyTo.put(part, tag);
                    }
                }
                tagStart = null;
                continue;
            }
            if (tagStart == null) {
                buffer.append(character);
            }
        }
        if (component == null) {
            throw new IllegalArgumentException("Failed to understand the text of " + string);
        }

        return component;
    }
}