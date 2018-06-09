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
package org.spongepowered.common.data.processor.value.entity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeMutableValue;

import java.util.Optional;

public class VehicleValueProcessor extends AbstractSpongeValueProcessor<net.minecraft.entity.Entity, EntitySnapshot, Value.Mutable<EntitySnapshot>> {

    public VehicleValueProcessor() {
        super(net.minecraft.entity.Entity.class, Keys.VEHICLE);
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof net.minecraft.entity.Entity;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof net.minecraft.entity.Entity) {
            net.minecraft.entity.Entity entity = ((net.minecraft.entity.Entity) container);
            if (entity.isRiding()) {
                final Entity vehicle = (Entity) entity.getRidingEntity();
                entity.dismountRidingEntity();
                return DataTransactionResult.successResult(new ImmutableSpongeValue<>(Keys.VEHICLE, vehicle.createSnapshot()));
            }
            return DataTransactionResult.builder().result(DataTransactionResult.Type.SUCCESS).build();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value.Mutable<EntitySnapshot> constructValue(EntitySnapshot defaultValue) {
        return new SpongeMutableValue<>(this.getKey(), defaultValue);
    }

    @Override
    protected boolean set(net.minecraft.entity.Entity container, EntitySnapshot value) {
        return ((Entity) container).setVehicle(value.restore().orElse(null));
    }

    @Override
    protected Optional<EntitySnapshot> getVal(net.minecraft.entity.Entity container) {
        Entity entity = (Entity) container.ridingEntity;
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.createSnapshot());
    }

    @Override
    protected Value.Immutable<EntitySnapshot> constructImmutableValue(EntitySnapshot value) {
        return new ImmutableSpongeValue<>(this.getKey(), value);
    }

}
