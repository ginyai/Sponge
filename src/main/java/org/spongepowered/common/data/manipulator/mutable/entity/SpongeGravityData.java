/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableGravityData;
import org.spongepowered.api.data.manipulator.mutable.entity.GravityData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeGravityData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBooleanData;
import org.spongepowered.common.data.util.DataConstants;

public class SpongeGravityData extends AbstractBooleanData<GravityData, ImmutableGravityData> implements GravityData {

    public SpongeGravityData(boolean value) {
        super(GravityData.class, value, Keys.HAS_GRAVITY, ImmutableSpongeGravityData.class, DataConstants.DEFAULT_HAS_GRAVITY);
    }

    public SpongeGravityData() {
        this(DataConstants.DEFAULT_HAS_GRAVITY);
    }

    @Override
    public Value.Mutable<Boolean> gravity() {
        return getValueGetter();
    }
}

