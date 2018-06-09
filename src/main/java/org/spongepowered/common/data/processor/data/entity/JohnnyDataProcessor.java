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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.monster.EntityVindicator;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableJohnnyData;
import org.spongepowered.api.data.manipulator.mutable.entity.JohnnyData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeJohnnyData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeMutableValue;
import org.spongepowered.common.interfaces.entity.monster.IMixinVindicator;

import java.util.Optional;

public class JohnnyDataProcessor extends AbstractEntitySingleDataProcessor<EntityVindicator, Boolean, Value.Mutable<Boolean>, JohnnyData, ImmutableJohnnyData> {

    public JohnnyDataProcessor() {
        super(EntityVindicator.class, Keys.IS_JOHNNY);
    }

    @Override
    protected boolean set(EntityVindicator dataHolder, Boolean value) {
        ((IMixinVindicator) dataHolder).setJohnny(value);
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(EntityVindicator dataHolder) {
        return Optional.of(((IMixinVindicator) dataHolder).isJohnny());
    }

    @Override
    protected Value.Immutable<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(this.key, false, value);
    }

    @Override
    protected Value.Mutable<Boolean> constructValue(Boolean actualValue) {
        return new SpongeMutableValue<>(this.key, false, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected JohnnyData createManipulator() {
        return new SpongeJohnnyData();
    }
}
