package theblockbox.huntersdream.util.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import theblockbox.huntersdream.capabilities.CapabilityProvider;
import theblockbox.huntersdream.entity.EntityGoblinTD;
import theblockbox.huntersdream.init.CapabilitiesInit;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.enums.Transformations;
import theblockbox.huntersdream.util.handlers.PacketHandler.Packets;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.helpers.WerewolfHelper;
import theblockbox.huntersdream.util.interfaces.IInfectInTicks;
import theblockbox.huntersdream.util.interfaces.IInfectOnNextMoon;
import theblockbox.huntersdream.util.interfaces.IInfectOnNextMoon.InfectionStatus;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformationCreature;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformationPlayer;
import theblockbox.huntersdream.util.interfaces.transformation.IWerewolf;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class CapabilityHandler {
	public final static ResourceLocation TRANSFORMATION_PLAYER_CAPABILITIY = new ResourceLocation(Reference.MODID,
			"transformationplayer");
	public static final ResourceLocation TRANSFORMATION_CREATURE_CAPABILITY = new ResourceLocation(Reference.MODID,
			"transformationcreature");
	public static final ResourceLocation INFECT_IN_TICKS_CAPABILITY = new ResourceLocation(Reference.MODID,
			"infectinticks");
	public static final ResourceLocation INFECT_ON_NEXT_MOON = new ResourceLocation(Reference.MODID,
			"infectonnextmoon");
	public static final ResourceLocation WEREWOLF = new ResourceLocation(Reference.MODID, "werewolf");

	@SubscribeEvent
	public static void onCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();

		if (entity instanceof EntityPlayer) {
			event.addCapability(TRANSFORMATION_PLAYER_CAPABILITIY,
					new CapabilityProvider<ITransformationPlayer>(CapabilitiesInit.CAPABILITY_TRANSFORMATION_PLAYER));
			event.addCapability(WEREWOLF, new CapabilityProvider<IWerewolf>(CapabilitiesInit.CAPABILITY_WEREWOLF));
		} else if (entity instanceof EntityVillager) {
			event.addCapability(TRANSFORMATION_CREATURE_CAPABILITY, new CapabilityProvider<ITransformationCreature>(
					CapabilitiesInit.CAPABILITY_TRANSFORMATION_CREATURE));
		}

		if (entity instanceof EntityVillager || entity instanceof EntityGoblinTD || entity instanceof EntityPlayer
				|| entity instanceof ITransformationCreature) {
			event.addCapability(INFECT_IN_TICKS_CAPABILITY,
					new CapabilityProvider<IInfectInTicks>(CapabilitiesInit.CAPABILITY_INFECT_IN_TICKS));
			event.addCapability(INFECT_ON_NEXT_MOON,
					new CapabilityProvider<IInfectOnNextMoon>(CapabilitiesInit.CAPABILITY_INFECT_ON_NEXT_MOON));
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			IInfectInTicks iit = TransformationHelper.getIInfectInTicks(event.getEntityPlayer());
			iit.setCurrentlyInfected(false);
			iit.setInfectionTransformation(Transformations.HUMAN);
			iit.setTime(-1);
			IInfectOnNextMoon ionm = WerewolfHelper.getIInfectOnNextMoon(event.getEntityPlayer());
			ionm.setInfectionStatus(InfectionStatus.NOT_INFECTED);
			ionm.setInfectionTick(-1);
			ionm.setInfectionTransformation(Transformations.HUMAN);

			ITransformationPlayer transformationPlayer = TransformationHelper.getCap(event.getEntityPlayer());
			ITransformationPlayer oldTransformationPlayer = TransformationHelper.getCap(event.getOriginal());

			transformationPlayer.setXP(oldTransformationPlayer.getXP());
			transformationPlayer.setLevel(oldTransformationPlayer.getLevel());
			transformationPlayer.setTransformed(false);
			transformationPlayer.setTransformation(oldTransformationPlayer.getTransformation());
			transformationPlayer.setTextureIndex(oldTransformationPlayer.getTextureIndex());
			transformationPlayer.setRituals(oldTransformationPlayer.getRituals());

			Packets.TRANSFORMATION.sync(event.getEntityPlayer());
		}
	}
}