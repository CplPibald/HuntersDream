package theblockbox.huntersdream.entity;

import com.google.common.base.Predicate;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import theblockbox.huntersdream.entity.model.ModelLycanthropeBiped;
import theblockbox.huntersdream.entity.model.ModelLycanthropeQuadruped;
import theblockbox.huntersdream.util.enums.Transformations;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.helpers.WerewolfHelper;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformation;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformationEntityTransformed;

/**
 * A werewolf
 */
public class EntityWerewolf extends EntityMob implements ITransformationEntityTransformed, IEntityAdditionalSpawnData {
	/** the werewolf texture to be used */
	private int textureIndex;
	/** name of the entity the werewolf was before transformation */
	private String untransformedEntityName;
	public static final double SPEED = 0.5D;
	public static final Transformations TRANSFORMATION = Transformations.WEREWOLF;

	public EntityWerewolf(World worldIn, int textureIndex, String entityName) {
		super(worldIn);
		this.textureIndex = (textureIndex >= 0 && textureIndex < getTransformation().getTextures().length)
				? textureIndex
				: getTransformation().getRandomTextureIndex();
		this.untransformedEntityName = entityName;
		this.setSize(1F, ModelLycanthropeBiped.HEIGHT);
	}

	public EntityWerewolf(World worldIn, int textureIndex, EntityLivingBase entity) {
		this(worldIn, textureIndex, entity.getClass().getName());
	}

	public EntityWerewolf(World worldIn) {
		this(worldIn, TRANSFORMATION.getRandomTextureIndex(), "$bycap" + EntityVillager.class.getName());
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIAttackMelee(this, SPEED + 0.2D, false));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(4, new EntityAIBreakDoor(this));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.applyEntityAI();
	}

	protected void applyEntityAI() {
		this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
		Predicate<EntityCreature> predicateMob = input -> {
			return !(input instanceof EntityWerewolf);
		};
		Predicate<EntityPlayer> predicatePlayer = input -> {
			ITransformation transformation = TransformationHelper.getITransformation(input);
			return ((transformation.getTransformation() != Transformations.WEREWOLF)
					&& !(transformation.transformed()));
		};
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityCreature>(this, EntityCreature.class, 10,
				true, false, predicateMob));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10,
				true, false, predicatePlayer));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(TRANSFORMATION.getGeneralDamage());
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(SPEED);
	}

	@Override
	public SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_WOLF_GROWL;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource sound) {
		return SoundEvents.ENTITY_WOLF_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_WOLF_DEATH;
	}

	@Override
	public float getEyeHeight() {
		return ModelLycanthropeBiped.EYE_HEIGHT;
	}

	@Override
	public boolean isChild() {
		return false;
	}

	@Override
	protected float getSoundPitch() {
		return super.getSoundPitch() - 1.0F;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (ticksExisted % 40 == 0) {
			if (!world.isRemote) {
				if (!WerewolfHelper.isWerewolfTime(this)) {
					ITransformationEntityTransformed.transformBack(this);
				}
			}
		}
		if (isSprinting())
			setSize(ModelLycanthropeQuadruped.WIDTH, ModelLycanthropeQuadruped.HEIGHT);
		else
			setSize(1F, ModelLycanthropeBiped.HEIGHT);
	}

	@Override
	public Transformations getTransformation() {
		return TRANSFORMATION;
	}

	@Override
	public int getTextureIndex() {
		return textureIndex;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		ByteBufUtils.writeUTF8String(buffer, untransformedEntityName);
		buffer.writeInt(textureIndex);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		this.untransformedEntityName = ByteBufUtils.readUTF8String(buffer);
		this.textureIndex = buffer.readInt();
	}

	/**
	 * Returns the name of the werewolf's normal form
	 */
	@Override
	public String getEntityName() {
		return untransformedEntityName;
	}
}
