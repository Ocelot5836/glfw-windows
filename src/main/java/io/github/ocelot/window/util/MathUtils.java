package io.github.ocelot.window.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class MathUtils {

    public static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }
}
