package io.github.ageuxo.Gastropodium.entity;

import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgeCrawler;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgeCrawlerMoveControl;
import io.github.ageuxo.Gastropodium.entity.pathing.BlockEdgePathNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicSlugEntity extends Animal implements BlockEdgeCrawler {

//    public float visXRot;
//    public float visZRot; TODO implement this

    public BasicSlugEntity(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new BlockEdgeCrawlerMoveControl(this);
    }

    public final AnimationState idleAnimationState = new AnimationState();
    public boolean isAttached;
    public Direction attachDirection;
    private int idleAnimationTimeout = 0;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide){
            setupAnimationStates();
        }
        ProfilerFiller profiler = this.level().getProfiler();
        profiler.push("slug_attach_check");
        if (!this.isAttached){
            boolean hasAttached = attach(this, this.blockPosition());
            this.isAttached = hasAttached;
//            this.setNoGravity(hasAttached);
        } else if (!canStandOn(this, this.level().getBlockState(this.getOnPos()), this.blockPosition(), this.attachDirection.getOpposite())){
            this.isAttached = false;
//            this.setNoGravity(false);
        }
        profiler.pop();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    private void setupAnimationStates(){
        if (this.idleAnimationTimeout <= 0){
            this.idleAnimationTimeout = this.random.nextInt(48) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float state;
        if (this.getPose() == Pose.STANDING){
            state = Math.min(pPartialTick * 6.0f, 1.0f);
        } else {
            state = 0.0f;
        }

        this.walkAnimation.update(state, 0.2f);

    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BreedGoal(this, 1.2D));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.2D, Ingredient.of(Tags.Items.CROPS_BEETROOT), false));
        this.goalSelector.addGoal(2, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 3.0F));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 1.0D));
    }

    public static AttributeSupplier.Builder createAttributes(){
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.FOLLOW_RANGE, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1D);
    }

    @Override
    protected @NotNull BlockEdgePathNavigation createNavigation(@NotNull Level pLevel) {
        return new BlockEdgePathNavigation(this, pLevel);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return ModEntities.BASIC_SLUG.get().create(pLevel);
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(Tags.Items.CROPS_BEETROOT);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    public boolean isAttached() {
        return this.isAttached;
    }

    @Override
    public Direction getAttachDirection() {
        return this.attachDirection;
    }

    @Override
    public void setAttachDirection(Direction direction) {
        this.attachDirection = direction;
    }

    @Override
    public @NotNull BlockPos getOnPos() {
        Direction direction = getAttachDirection() != null ? getAttachDirection() : Direction.DOWN;
        return this.blockPosition().relative(direction);
    }
}
