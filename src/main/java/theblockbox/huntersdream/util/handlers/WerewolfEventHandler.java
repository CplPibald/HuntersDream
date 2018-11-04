package theblockbox.huntersdream.util.handlers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import theblockbox.huntersdream.Main;
import theblockbox.huntersdream.event.TransformationEvent.TransformationEventReason;
import theblockbox.huntersdream.event.WerewolfTransformingEvent.WerewolfTransformingReason;
import theblockbox.huntersdream.init.CapabilitiesInit;
import theblockbox.huntersdream.init.SoundInit;
import theblockbox.huntersdream.init.TransformationInit;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.Transformation;
import theblockbox.huntersdream.util.exceptions.UnexpectedBehaviorException;
import theblockbox.huntersdream.util.helpers.ChanceHelper;
import theblockbox.huntersdream.util.helpers.EffectivenessHelper;
import theblockbox.huntersdream.util.helpers.GeneralHelper;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.helpers.WerewolfHelper;
import theblockbox.huntersdream.util.interfaces.IInfectOnNextMoon;
import theblockbox.huntersdream.util.interfaces.IInfectOnNextMoon.InfectionStatus;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformation;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformationPlayer;
import theblockbox.huntersdream.util.interfaces.transformation.IWerewolf;

@Mod.EventBusSubscriber(modid = Reference.MODID)
public class WerewolfEventHandler {
	public static final int[] LEVELS_WITH_EXTRA_HEARTS = { 7, 8, 9, 10, 11, 12 };

	// use LivingDamage only for removing damage and LivingHurt for damage and
	// damaged resources
	@SubscribeEvent
	public static void onEntityHurt(LivingHurtEvent event) {
		EntityLivingBase attacked = event.getEntityLiving();
		if (event.getSource().getTrueSource() instanceof EntityLivingBase) {
			EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
			ITransformation transformationAttacker = TransformationHelper.getITransformation(attacker);

			if (transformationAttacker != null && WerewolfHelper.isTransformedWerewolf(attacker)) {
				// handle werewolf infection
				// if the werewolf can infect
				if (WerewolfHelper.canInfect(attacker)) {
					if (ChanceHelper.chanceOf(WerewolfHelper.getInfectionPercentage(attacker))) {
						// and the entity can be infected
						if (TransformationHelper.canChangeTransformation(attacked)
								&& TransformationHelper.canBeInfectedWith(TransformationInit.WEREWOLF, attacked)
								&& (!TransformationHelper.isInfected(attacked))) {
							// infect the entity
							WerewolfHelper.infectEntityAsWerewolf(attacked);
						}
					}
				}
				if (attacker instanceof EntityPlayer) {
					// fill hunger
					EntityPlayer player = (EntityPlayer) attacker;
					player.getFoodStats().addStats(1, 1);
				}
			}
		}

		if (attacked instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) attacked;
			if (!player.world.isRemote) {
				ITransformationPlayer cap = TransformationHelper.getCap(player);
				if (WerewolfHelper.isWerewolfTime(player.world) && !WerewolfHelper.isTransformed(player)
						&& (cap.getTransformation() == TransformationInit.WEREWOLF)
						&& WerewolfHelper.getIWerewolf(player).getTransformationStage() > 0) {
					// cancel event if damage source isn't magic (including poison) or event can
					// kill player
					event.setCanceled(
							(event.getSource() != DamageSource.MAGIC) || (event.getAmount() >= player.getHealth()));
				}
			}
		}
	}

	/**
	 * Called in
	 * {@link TransformationEventHandler#onEntityTick(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent)}
	 */
	public static void handleWerewolfInfection(EntityLivingBase entity) {
		if (entity.hasCapability(CapabilitiesInit.CAPABILITY_INFECT_ON_NEXT_MOON, null)) {
			IInfectOnNextMoon ionm = WerewolfHelper.getIInfectOnNextMoon(entity);
			if (ionm.getInfectionTransformation() == TransformationInit.WEREWOLF) {
				if (!WerewolfHelper.isWerewolfTime(entity.world)) {
					if (ionm.getInfectionStatus() == InfectionStatus.MOON_ON_INFECTION) {
						ionm.setInfectionStatus(InfectionStatus.AFTER_INFECTION);
					}
				} else if (WerewolfHelper.isWerewolfTime(entity.world)) {
					if (ionm.getInfectionStatus() == InfectionStatus.AFTER_INFECTION) {
						ionm.setInfectionStatus(InfectionStatus.NOT_INFECTED);
						ionm.setInfectionTick(-1);
						ionm.setInfectionTransformation(TransformationInit.HUMAN);
						// change transformation
						TransformationHelper.changeTransformation(entity, TransformationInit.WEREWOLF,
								TransformationEventReason.INFECTION);
					}
				}
			}
		}
	}

	// these methods are here for easier code understanding

	static void werewolfTimeTransformed(EntityPlayerMP player, ITransformationPlayer cap, IWerewolf werewolf) {
		WerewolfHelper.applyLevelBuffs(player);
	}

	static void werewolfTimeNotTransformed(EntityPlayerMP player, ITransformationPlayer cap, IWerewolf werewolf) {
		if (WerewolfHelper.canWerewolfTransform(player)) {
			if (werewolf.getTransformationStage() <= 0) {
				werewolf.setTimeSinceTransformation(player.ticksExisted);
				onStageChanged(player, werewolf, 1);
			}

			// every five seconds (20 * 5 = 100) one stage up
			int nextStage = MathHelper.floor(((player.ticksExisted - werewolf.getTimeSinceTransformation())) / 100.0D);

			if (nextStage > 6 || nextStage < 0) {
				werewolf.setTimeSinceTransformation(-1);
				werewolf.setTransformationStage(0);
				Main.getLogger().warn(
						"Has the ingame time been changed, did the player leave the world or did the player use wolfsbane? Player "
								+ player.getName() + "'s transformation stage (" + nextStage + ") is invalid");
				return;
			}
			if (nextStage > werewolf.getTransformationStage()) {
				onStageChanged(player, werewolf, nextStage);
			}
		}
	}

	static void notWerewolfTimeNotTransformed(EntityPlayerMP player, ITransformationPlayer cap, IWerewolf werewolf) {
		// currently does nothing
	}

	static void notWerewolfTimeTransformed(EntityPlayerMP player, ITransformationPlayer cap, IWerewolf werewolf) {
		if (werewolf.getTransformationStage() <= 0) {
			player.sendMessage(
					new TextComponentTranslation("transformations.huntersdream:werewolf.transformingBack.0"));
			WerewolfHelper.transformPlayer(player, false, WerewolfTransformingReason.FULL_MOON_END);
			PacketHandler.sendWerewolfTransformedMessage(player);
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 1200, 2));
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 1200, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 300, 4));
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 300, 0));
			// night vision for better blindness effect
			player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, false, false));
		}
	}

	/** Called when infection stage changes */
	private static void onStageChanged(EntityPlayer player, IWerewolf werewolf, int nextStage) {

		werewolf.setTransformationStage(nextStage);

		switch (werewolf.getTransformationStage()) {
		case 1:
			player.world.playSound(null, player.getPosition(), SoundInit.HEART_BEAT, SoundCategory.PLAYERS, 100, 1);
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 550, 1));
			player.addPotionEffect(new PotionEffect(MobEffects.POISON, 550, 0));
			break;
		case 2:
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 450, 1));
			break;
		case 3:
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 350, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 350, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 350, 255));
			break;
		case 4:
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 120, 0));
			break;
		case 5:
			// nothing happens
			break;
		case 6:
			player.world.playSound(null, player.getPosition(), SoundInit.WEREWOLF_HOWLING, SoundCategory.PLAYERS, 100,
					1);
			werewolf.setTimeSinceTransformation(-1);
			werewolf.setTransformationStage(0);
			WerewolfHelper.transformPlayer((EntityPlayerMP) player, true, WerewolfTransformingReason.FULL_MOON);
			break;
		default:
			throw new UnexpectedBehaviorException(
					"Stage " + werewolf.getTransformationStage() + " is not a valid stage");
		}
		if (werewolf.getTransformationStage() != 0) {
			player.sendMessage(new TextComponentTranslation(
					"transformations.huntersdream:werewolf.transformingInto." + werewolf.getTransformationStage()));
		}

	}

	@SubscribeEvent
	public static void onEntityItemPickup(EntityItemPickupEvent event) {
		if (WerewolfHelper.isTransformedWerewolf(event.getEntityPlayer()) && !event.getEntityPlayer().isCreative()) {
			event.setCanceled(true);
		}
	}

	// damage player and handle chat messages when player picks up item that is
	// effective against them
	@SubscribeEvent
	public static void onItemPickup(ItemPickupEvent event) {
		EntityItem originalEntity = event.getOriginalEntity();
		if (!originalEntity.world.isRemote) {
			String throwerName = originalEntity.getThrower();
			EntityPlayer player = event.player;
			ITransformationPlayer cap = TransformationHelper.getCap(player);
			Item item = event.getStack().getItem();
			if (EffectivenessHelper.effectiveAgainstTransformation(cap.getTransformation(), event.getStack())) {
				// now it is ensured that the item is effective against the player
				String msg = "transformations." + cap.getTransformation().toString() + ".";

				EntityPlayer thrower;
				if ((throwerName != null) && !(throwerName.equals("null")) && !(throwerName.equals(player.getName()))) {
					thrower = originalEntity.world.getPlayerEntityByName(throwerName);
					WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) player, msg + "fp.touched", player, item);
					WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) thrower, msg + "tp.touched", player, item);
				} else {
					WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) player, msg + "fp.picked", player, item);
					thrower = GeneralHelper.getNearestPlayer(player.world, player, 5);
					if (thrower != null) {
						WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) thrower, msg + "tp.picked", player, item);
					}
				}
			}
		}
	}

	// damage player and handle chat messages when player is clicked with item that
	// is effective against them
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.EntityInteractSpecific event) {
		EntityPlayer player = event.getEntityPlayer();
		if (event.getTarget() instanceof EntityLivingBase) {
			EntityLivingBase interactedWith = (EntityLivingBase) event.getTarget();
			if (!(WerewolfHelper.isTransformedWerewolf(interactedWith))) {
				Item item = event.getItemStack().getItem();
				Transformation transformation = TransformationHelper.getTransformation(interactedWith);
				if (EffectivenessHelper.effectiveAgainstTransformation(transformation, event.getItemStack())) {
					if (!player.world.isRemote) {
						String msg = "transformations." + transformation.toString() + ".";
						WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) player, msg + "tp.touched",
								interactedWith, item);
						if (interactedWith instanceof EntityPlayer) {
							WerewolfHelper.sendItemPickupMessage((EntityPlayerMP) interactedWith, msg + "fp.touched",
									interactedWith, item);
						}
					}
					// we don't want to open any gui, so we say that this interaction was a success
					event.setCancellationResult(EnumActionResult.SUCCESS);
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerSleep(PlayerSleepInBedEvent event) {
		EntityPlayer player = event.getEntityPlayer();
		if (TransformationHelper.getTransformation(player) == TransformationInit.WEREWOLF) {
			event.setResult(EntityPlayer.SleepResult.OTHER_PROBLEM);
			if (!player.world.isRemote)
				player.sendStatusMessage(new TextComponentTranslation(Reference.MODID + ".werewolfNotAllowedToSleep"),
						true);
		}
	}

	public static void addHeartsToPlayer(EntityPlayer player, double extraHalfHearts) {
		IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		attribute.setBaseValue(attribute.getBaseValue() + extraHalfHearts);
	}
}
