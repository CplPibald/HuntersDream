package theblockbox.huntersdream.util.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import theblockbox.huntersdream.capabilities.CapabilityProvider;
import theblockbox.huntersdream.init.CapabilitiesInit;
import theblockbox.huntersdream.util.ExecutionPath;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.handlers.PacketHandler.Packets;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.interfaces.ITransformationPlayer;

@Mod.EventBusSubscriber
public class CapabilityHandler {
	public final static ResourceLocation TRANSFORMATION_PLAYER_CAPABILITIY = new ResourceLocation(Reference.MODID,
			"transformationPlayer");

	@SubscribeEvent
	public static void onCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(TRANSFORMATION_PLAYER_CAPABILITIY,
					new CapabilityProvider<ITransformationPlayer>(CapabilitiesInit.CAPABILITY_TRANSFORMATION_PLAYER));
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		ITransformationPlayer transformationPlayer = TransformationHelper.getCap(event.getEntityPlayer());
		ITransformationPlayer oldTransformationPlayer = TransformationHelper.getCap(event.getOriginal());

		transformationPlayer.setXP(oldTransformationPlayer.getXP());
		transformationPlayer.setTransformed(false);
		transformationPlayer.setTransformationID(oldTransformationPlayer.getTransformationInt());
		transformationPlayer.setTextureIndex(oldTransformationPlayer.getTextureIndex());

		Packets.TRANSFORMATION.sync(new ExecutionPath(), event.getEntityPlayer());
	}
}