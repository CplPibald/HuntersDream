package theblockbox.huntersdream.entity.renderer;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import theblockbox.huntersdream.entity.model.ModelLycanthropeBiped;
import theblockbox.huntersdream.util.enums.Transformations;
import theblockbox.huntersdream.util.helpers.TransformationHelper;

public class RenderLycantropheBiped extends RenderLivingBase<EntityPlayer> {

	public RenderLycantropheBiped(RenderManager manager) {
		super(manager, new ModelLycanthropeBiped(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPlayer entity) {
		return Transformations.WEREWOLF.getTextures()[TransformationHelper.getCap(entity).getTextureIndex()];
	}

	@Override
	protected void applyRotations(EntityPlayer entityLiving, float p_77043_2_, float rotationYaw, float partialTicks) {
		super.applyRotations(entityLiving, p_77043_2_, rotationYaw, partialTicks);
	}
}