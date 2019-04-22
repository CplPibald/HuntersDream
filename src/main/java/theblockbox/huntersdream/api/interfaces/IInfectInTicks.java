package theblockbox.huntersdream.api.interfaces;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.apache.commons.lang3.Validate;
import theblockbox.huntersdream.api.Transformation;
import theblockbox.huntersdream.util.annotations.CapabilityInterface;

@CapabilityInterface
public interface IInfectInTicks {
    /**
     * get time in ticks set until infection (final value)
     */
    public int getTime();

    public void setTime(int time);

    public int getTimeUntilInfection();

    public void setTimeUntilInfection(int time);

    public Transformation getInfectionTransformation();

    public void setInfectionTransformation(Transformation transformation);

    public boolean currentlyInfected();

    public void setCurrentlyInfected(boolean infected);

    public static class InfectInTicks implements IInfectInTicks {
        private int time = -1;
        private int timeUntilInfection = -1;
        private Transformation infectionTransformation = Transformation.HUMAN;
        private boolean currentlyInfected = false;

        @Override
        public int getTime() {
            return this.time;
        }

        @Override
        public void setTime(int time) {
            this.time = time;
            this.timeUntilInfection = time;
        }

        @Override
        public int getTimeUntilInfection() {
            return this.timeUntilInfection;
        }

        @Override
        public void setTimeUntilInfection(int timeUntilInfection) {
            this.timeUntilInfection = timeUntilInfection;
        }

        @Override
        public Transformation getInfectionTransformation() {
            return this.infectionTransformation;
        }

        @Override
        public void setInfectionTransformation(Transformation transformation) {
            Validate.notNull(transformation, "The transformation isn't allowed to be null");
            this.infectionTransformation = transformation;
        }

        @Override
        public boolean currentlyInfected() {
            return this.currentlyInfected;
        }

        @Override
        public void setCurrentlyInfected(boolean currentlyInfected) {
            this.currentlyInfected = currentlyInfected;
        }
    }

    public static class InfectInTicksStorage implements Capability.IStorage<IInfectInTicks> {
        public static final String TIME = "time";
        public static final String TIME_UNTIL_INFECTION = "timeuntilinfection";
        public static final String INFECTION_TRANSFORMATION = "infectiontransformation";
        public static final String CURRENTLY_INFECTED = "currentlyinfected";

        @Override
        public NBTBase writeNBT(Capability<IInfectInTicks> capability, IInfectInTicks instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger(IInfectInTicks.InfectInTicksStorage.TIME, instance.getTime());
            compound.setInteger(IInfectInTicks.InfectInTicksStorage.TIME_UNTIL_INFECTION, instance.getTimeUntilInfection());
            compound.setString(IInfectInTicks.InfectInTicksStorage.INFECTION_TRANSFORMATION, instance.getInfectionTransformation().toString());
            compound.setBoolean(IInfectInTicks.InfectInTicksStorage.CURRENTLY_INFECTED, instance.currentlyInfected());
            return compound;
        }

        @Override
        public void readNBT(Capability<IInfectInTicks> capability, IInfectInTicks instance, EnumFacing side,
                            NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            instance.setTime(compound.getInteger(IInfectInTicks.InfectInTicksStorage.TIME));
            instance.setTimeUntilInfection(compound.getInteger(IInfectInTicks.InfectInTicksStorage.TIME_UNTIL_INFECTION));
            instance.setInfectionTransformation(Transformation.fromName(compound.getString(IInfectInTicks.InfectInTicksStorage.INFECTION_TRANSFORMATION)));
            instance.setCurrentlyInfected(compound.getBoolean(IInfectInTicks.InfectInTicksStorage.CURRENTLY_INFECTED));
        }
    }
}