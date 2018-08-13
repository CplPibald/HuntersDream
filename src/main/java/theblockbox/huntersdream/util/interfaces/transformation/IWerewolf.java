package theblockbox.huntersdream.util.interfaces.transformation;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import theblockbox.huntersdream.util.annotations.CapabilityInterface;
import theblockbox.huntersdream.util.enums.Transformations;

@CapabilityInterface
/** For player werewolves */
public interface IWerewolf {

	public void setTimeSinceTransformation(int time);

	/** Returns the player.ticksExisted when the transformation has started */
	public int getTimeSinceTransformation();

	/** 0 is no stage and 6 is transformed */
	public int getTransformationStage();

	public void setTransformationStage(int stage);

	default public Transformations getTransformation() {
		return Transformations.WEREWOLF;
	}

	public static class Werewolf implements IWerewolf {
		private int timeSinceTransformation = -1;
		private int transformationStage = 0;
		// private boolean transformingBack = false;

		@Override
		public void setTimeSinceTransformation(int time) {
			this.timeSinceTransformation = time;
		}

		@Override
		public int getTimeSinceTransformation() {
			return this.timeSinceTransformation;
		}

		@Override
		public int getTransformationStage() {
			return this.transformationStage;
		}

		@Override
		public void setTransformationStage(int stage) {
			this.transformationStage = stage;
		}
	}

	public static class WerewolfStorage implements IStorage<IWerewolf> {
		public static final String TIME_SINCE_TRANSFORMATION = "timesincetransformation";
		public static final String TRANSFORMATION_STAGE = "transformationstage";
		// public static final String TRANSFORMING_BACK = "transformingback";

		@Override
		public NBTBase writeNBT(Capability<IWerewolf> capability, IWerewolf instance, EnumFacing side) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger(TIME_SINCE_TRANSFORMATION, instance.getTimeSinceTransformation());
			compound.setInteger(TRANSFORMATION_STAGE, instance.getTransformationStage());
			return compound;
		}

		@Override
		public void readNBT(Capability<IWerewolf> capability, IWerewolf instance, EnumFacing side, NBTBase nbt) {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			instance.setTimeSinceTransformation(compound.getInteger(TIME_SINCE_TRANSFORMATION));
			instance.setTransformationStage(compound.getInteger(TRANSFORMATION_STAGE));
		}
	}
}