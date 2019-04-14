package teamrtg.passableleaves.asm;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public final class PLCollisionHandler {
    private PLCollisionHandler() {}

    private static final DamageSource DAMAGESOURCE_FALLINTOLEAVES = new DamageSource("fallintoleaves") {
        @SuppressWarnings("deprecation")
        @Override public ITextComponent getDeathMessage(EntityLivingBase entity) {
            return I18n.canTranslate("death.fallintoleaves")
                ? new TextComponentTranslation("death.fallintoleaves", entity.getDisplayName())
                : entity.getDisplayName().appendText(" fell into leaves and was impaled by branches");
        }
    };

    private static float   fallDamageReduction      = 0.5f;
    private static int     fallDamageThreshold      = 20;
    private static double  speedReductionHorizontal = 0.75d;
    private static double  speedReductionVertical   = 0.75d;


    public static void setFallDamageReduction(final float val) {
        fallDamageReduction = (val < 0.0f) ? 0.0f : ((val > 1.0f) ? 1.0f : val);
    }

    public static void setFallDamageThreshold(final int val) {
        fallDamageThreshold = (val < 5) ? 5 : ((val > 255) ? 255 : val);
    }

    public static void setSpeedReductionHorizontal(final double val) {
        speedReductionHorizontal = (val < 0.05d) ? 0.05d : ((val > 1.0d) ? 1.0d : val);
    }

    public static void setSpeedReductionVertical(final double val) {
        speedReductionVertical = (val < 0.05d) ? 0.05d : ((val > 1.0d) ? 1.0d : val);
    }

    // This method name has to match the method that is called in #transform, above
    @SuppressWarnings({"unused", "WeakerAccess"})
    public static void onEntityCollidedWithLeaves(World world, BlockPos pos, IBlockState state, Entity entity) {

        if (entity instanceof EntityLivingBase) {

            EntityLivingBase livingEntity = (EntityLivingBase)entity;

            // play a sound when an entity falls into leaves; do this before altering motion
            if (livingEntity.fallDistance > 3f) {
                entity.playSound(SoundEvents.BLOCK_GRASS_BREAK, SoundType.PLANT.getVolume() * 0.6f, SoundType.PLANT.getPitch() * 0.65f);
            }
            // play a sound when an entity is moving through leaves (only play sound every 5 ticks as to not flood sound events)
            else if (world.getTotalWorldTime() % 8 == 0 && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ)) {
                entity.playSound(SoundEvents.BLOCK_GRASS_HIT, SoundType.PLANT.getVolume() * 0.5f, SoundType.PLANT.getPitch() * 0.45f);
            }

            // reduce movement speed when inside of leaves, but allow players/mobs to jump out of them
            if (!livingEntity.isJumping) {
                entity.motionX *= speedReductionHorizontal;
                entity.motionY *= speedReductionVertical;
                entity.motionZ *= speedReductionHorizontal;
            }

            // modify falling damage when falling into leaves
            if (livingEntity.fallDistance > fallDamageThreshold) {
                livingEntity.fallDistance -= fallDamageThreshold;
                PotionEffect pe = livingEntity.getActivePotionEffect(MobEffects.JUMP_BOOST);
                int amount = MathHelper.ceil(livingEntity.fallDistance * fallDamageReduction * ((pe == null) ? 1.0f : 0.9f));
                livingEntity.attackEntityFrom(DAMAGESOURCE_FALLINTOLEAVES, amount);
            }

            // reset fallDistance
            if (livingEntity.fallDistance > 1f) { livingEntity.fallDistance = 1f; }

            // Riding a mob won't protect you; Process riders last
            if (entity.isBeingRidden()) {
                for (Entity ent : entity.getPassengers()) {
                    onEntityCollidedWithLeaves(world, pos, state, ent);
                }
            }
        }
    }
}
