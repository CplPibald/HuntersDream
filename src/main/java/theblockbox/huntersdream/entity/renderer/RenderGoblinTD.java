package theblockbox.huntersdream.entity.renderer;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import theblockbox.huntersdream.entity.EntityGoblinTD;
import theblockbox.huntersdream.entity.model.ModelGoblinTD;
import theblockbox.huntersdream.util.Reference;
import theblockbox.huntersdream.util.helpers.GeneralHelper;

public class RenderGoblinTD extends RenderLiving<EntityGoblinTD> {
	public static final ResourceLocation[] TEXTURES = new ResourceLocation[EntityGoblinTD.TEXTURES];

	static {
		for (int i = 0; i < EntityGoblinTD.TEXTURES; i++) {
			TEXTURES[i] = GeneralHelper.newResLoc(Reference.ENTITY_TEXTURE_PATH + "goblin" + i + ".png");
		}
	}

	public RenderGoblinTD(RenderManager manager) {
		super(manager, new ModelGoblinTD(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGoblinTD entity) {
		return TEXTURES[entity.getTexture()];
	}

	@Override
	protected void applyRotations(EntityGoblinTD entityLiving, float p_77043_2_, float rotationYaw,
			float partialTicks) {
		super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
	}
}