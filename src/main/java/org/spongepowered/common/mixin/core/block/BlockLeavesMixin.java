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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDecayableData;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeDecayableData;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeTreeData;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.registry.type.block.TreeTypeRegistryModule;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(BlockLeaves.class)
public abstract class BlockLeavesMixin extends BlockMixin {

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void impl$UpdateTickRandomlyFromWorldConfig(final CallbackInfo ci) {
        this.setTickRandomly(SpongeImpl.getGlobalConfigAdapter().getConfig().getWorld().getLeafDecay());
    }

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
    private boolean onUpdateDecayState(final net.minecraft.world.World worldIn, final BlockPos pos, final IBlockState state, final int flags) {
        final PhaseContext<?> currentContext = PhaseTracker.getInstance().getCurrentContext();
        final IPhaseState<?> currentState = currentContext.state;
        try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext()
                                           .source(new SpongeLocatableBlockBuilder()
                                               .world((World) worldIn)
                                               .position(pos.getX(), pos.getY(), pos.getZ())
                                               .state((BlockState) state)
                                               .build())) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return worldIn.setBlockState(pos, state, flags);
        }
    }

    /**
     * @author gabizou - August 2nd, 2016
     * @reason Rewrite to handle both drops and the change state for leaves
     * that are considered to be decaying, so the drops do not leak into
     * whatever previous phase is being handled in. Since the issue is that
     * the block change takes place in a different phase (more than likely),
     * the drops are either "lost" or not considered for drops because the
     * blocks didn't change according to whatever previous phase.
     *
     * @param worldIn The world in
     * @param pos The position
     */
    @Overwrite
    private void destroy(final net.minecraft.world.World worldIn, final BlockPos pos) {
        final IBlockState state = worldIn.getBlockState(pos);
        // Sponge Start - Cause tracking
        if (!((WorldBridge) worldIn).bridge$isFake()) {
            final PhaseContext<?> peek = PhaseTracker.getInstance().getCurrentContext();
            final IPhaseState<?> currentState = peek.state;
            try (final PhaseContext<?> context = currentState.includesDecays() ? null : BlockPhase.State.BLOCK_DECAY.createPhaseContext()
                .source(new SpongeLocatableBlockBuilder()
                    .world((World) worldIn)
                    .position(pos.getX(), pos.getY(), pos.getZ())
                    .state((BlockState) state)
                    .build())) {
                if (context != null) {
                    context.buildAndSwitch();
                }
                this.dropBlockAsItem(worldIn, pos, state, 0);
                worldIn.setBlockToAir(pos);
            }
            return;
        }
        // Sponge End
        this.dropBlockAsItem(worldIn, pos, state , 0);
        worldIn.setBlockToAir(pos);

    }

    private ImmutableTreeData impl$getTreeData(final IBlockState blockState) {
        final BlockPlanks.EnumType type;
        if (blockState.getBlock() instanceof BlockOldLeaf) {
            type = blockState.getValue(BlockOldLeaf.VARIANT);
        } else if (blockState.getBlock() instanceof BlockNewLeaf) {
            type = blockState.getValue(BlockNewLeaf.VARIANT);
        } else {
            type = BlockPlanks.EnumType.OAK;
        }

        final TreeType treeType = TreeTypeRegistryModule.getFor(type);

        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeTreeData.class, treeType);
    }

    private ImmutableDecayableData impl$getIsDecayableFor(final IBlockState blockState) {
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeDecayableData.class, blockState.getValue(BlockLeaves.DECAYABLE));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableTreeData.class.isAssignableFrom(immutable) || ImmutableDecayableData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableTreeData) {
            final TreeType treeType = ((ImmutableTreeData) manipulator).type().get();
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            if (blockState.getBlock() instanceof BlockOldLeaf) {
                if (treeType.equals(TreeTypes.OAK) ||
                        treeType.equals(TreeTypes.BIRCH) ||
                        treeType.equals(TreeTypes.SPRUCE) ||
                        treeType.equals(TreeTypes.JUNGLE)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockOldLeaf.VARIANT, type));
                }
            } else if (blockState.getBlock() instanceof BlockNewLeaf) {
                if (treeType.equals(TreeTypes.ACACIA) || treeType.equals(TreeTypes.DARK_OAK)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockNewLeaf.VARIANT, type));
                }
            }
            return Optional.empty();
        }
        if (manipulator instanceof ImmutableDecayableData) {
            final boolean decayable = ((ImmutableDecayableData) manipulator).decayable().get();
            return Optional.of((BlockState) blockState.withProperty(BlockLeaves.DECAYABLE, decayable));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.TREE_TYPE)) {
            final TreeType treeType = (TreeType) value;
            final BlockPlanks.EnumType type = TreeTypeRegistryModule.getFor(treeType);
            if (blockState.getBlock() instanceof BlockOldLeaf) {
                if (treeType.equals(TreeTypes.OAK) ||
                        treeType.equals(TreeTypes.BIRCH) ||
                        treeType.equals(TreeTypes.SPRUCE) ||
                        treeType.equals(TreeTypes.JUNGLE)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockOldLeaf.VARIANT, type));
                }
            } else if (blockState.getBlock() instanceof BlockNewLeaf) {
                if (treeType.equals(TreeTypes.ACACIA) || treeType.equals(TreeTypes.DARK_OAK)) {
                    return Optional.of((BlockState) blockState.withProperty(BlockNewLeaf.VARIANT, type));
                }
            }
            return Optional.empty();
        }
        if (key.equals(Keys.DECAYABLE)) {
            final boolean decayable = (Boolean) value;
            return Optional.of((BlockState) blockState.withProperty(BlockLeaves.DECAYABLE, decayable));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        final ImmutableList.Builder<ImmutableDataManipulator<?, ?>> builder = ImmutableList.builder();
        if (blockState.getProperties().containsKey(BlockLeaves.DECAYABLE)) {
            builder.add(impl$getIsDecayableFor(blockState));
        }
        if (blockState.getProperties().containsKey(BlockOldLeaf.VARIANT)) {
            builder.add(impl$getTreeData(blockState));
        } else if (blockState.getProperties().containsKey(BlockNewLeaf.VARIANT)) {
            builder.add(impl$getTreeData(blockState));
        }
        return builder.build();
    }

}
