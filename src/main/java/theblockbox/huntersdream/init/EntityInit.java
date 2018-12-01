package theblockbox.huntersdream.init;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeForest;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import theblockbox.huntersdream.entity.EntityChair;
import theblockbox.huntersdream.entity.EntityGoblinTD;
import theblockbox.huntersdream.entity.EntityWerewolf;
import theblockbox.huntersdream.entity.renderer.RenderGoblinTD;
import theblockbox.huntersdream.entity.renderer.RenderWerewolf;
import theblockbox.huntersdream.util.helpers.GeneralHelper;

public class EntityInit {
	// using the same values used in
	// net.minecraft.entity.EntityTracker#track(Entity)
	public static final boolean VEL_UPDATES = true;
	public static final int TRACKING_RANGE = 80;
	public static final int UPDATE_FREQ = 3;
	private static int networkID = 0;

	public static void registerEntities(RegistryEvent.Register<EntityEntry> event) {
		event.getRegistry().registerAll(
				getEntityEntryBuilder("goblintd", EntityGoblinTD.class).egg(29696, 255)
						.tracker(TRACKING_RANGE, UPDATE_FREQ, VEL_UPDATES).build(),
				getEntityEntryBuilder("werewolf", EntityWerewolf.class)
						.tracker(TRACKING_RANGE, UPDATE_FREQ, VEL_UPDATES)
						.spawn(EnumCreatureType.CREATURE, 2, 5, 5,
								StreamSupport.stream(Biome.REGISTRY.spliterator(), false)
										.filter(b -> b instanceof BiomeForest).collect(Collectors.toSet()))
						.build(),
				getEntityEntryBuilder("chair", EntityChair.class).tracker(0, 1, false).build());
	}

	public static void registerEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGoblinTD.class, new IRenderFactory<EntityGoblinTD>() {
			@Override
			public Render<? super EntityGoblinTD> createRenderFor(RenderManager manager) {
				return new RenderGoblinTD(manager);
			}
		});
		RenderingRegistry.registerEntityRenderingHandler(EntityWerewolf.class, new IRenderFactory<EntityWerewolf>() {

			@Override
			public Render<? super EntityWerewolf> createRenderFor(RenderManager manager) {
				return new RenderWerewolf(manager);
			}
		});
	}

	/*
	 * How to make entities: - register it here - make a new renderer class - make
	 * new texture in textures/entity
	 */

	private static EntityEntryBuilder<Entity> getEntityEntryBuilder(String name, Class<? extends Entity> clazz) {
		return EntityEntryBuilder.create().entity(clazz).id(GeneralHelper.newResLoc(name), networkID++).name(name);
	}
}
