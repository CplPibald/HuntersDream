package theblockbox.huntersdream.gui;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import theblockbox.huntersdream.api.ParentSkill;
import theblockbox.huntersdream.api.Skill;
import theblockbox.huntersdream.util.handlers.PacketHandler;
import theblockbox.huntersdream.util.helpers.ClientHelper;
import theblockbox.huntersdream.util.helpers.GeneralHelper;
import theblockbox.huntersdream.util.helpers.TransformationHelper;
import theblockbox.huntersdream.util.interfaces.transformation.ITransformationPlayer;

public class GuiSkillTab extends GuiScreen {
	public static final int TEXTURE_WIDTH = 256;
	public static final int TEXTURE_HEIGHT = 190;
	public static final int TOOLTIP_COLOR = 11250603;
	public static final int RECT_SIZE = 24;
	// length of a connections of button and rect
	public static final int RECT_CONNECTION_LENGTH = 18;
	// length of a connection of two buttons
	public static final int BUTTON_CONNECTION_LENGTH = 22;
	public static final int MAX_TEXT_WIDTH = 200;
	private static final ResourceLocation TEXTURE = GeneralHelper.newResLoc("textures/gui/skills/skill_window.png");
	// tp = TransformationPlayer
	private final ITransformationPlayer tp;
	private float fade = 0.0F;
	private int rectMinX;
	private int rectMinY;
	private int rectMaxX;
	private int rectMaxY;
	private int rectMiddleX;
	private int rectMiddleY;

	public GuiSkillTab() {
		this.tp = TransformationHelper.getITransformationPlayer(Minecraft.getMinecraft().player);
	}

	@Override
	public void initGui() {
		super.initGui();
		int halfRectSize = GuiSkillTab.RECT_SIZE / 2;
		this.rectMiddleX = this.width / 2;
		this.rectMiddleY = this.height / 2 + 4;
		this.rectMinX = this.rectMiddleX - halfRectSize;
		this.rectMinY = this.rectMiddleY - halfRectSize;
		this.rectMaxX = this.rectMiddleX + halfRectSize;
		this.rectMaxY = this.rectMiddleY + halfRectSize;

		// TODO: Use different collection?
		Collection<Skill> skills = Skill.getAllSkills().stream().filter(Skill::isParentSkill)
				.filter(s -> s.isForTransformation(this.tp.getTransformation()))
				.collect(Collectors.toCollection(ArrayDeque::new));

		// I did not think of this myself
		// https://stackoverflow.com/questions/10152390/dynamically-arrange-some-elements-around-a-circle

		// radius of the circle
		double radius = halfRectSize + GuiSkillTab.RECT_CONNECTION_LENGTH;
		double angle = 0;
		double step = (Math.PI * 2) / skills.size();
		int xOffset = this.rectMiddleX - 8;
		int yOffset = this.rectMiddleY - 8;

		// create buttons
		int buttonId = 0;
		for(Skill s : skills) {
			ParentSkill skill = (ParentSkill) s;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);

			// create parent button
			int x = (int) (xOffset + radius * cos);
			int y = (int) (yOffset + radius * sin);
			GuiButtonSkill lastButton = new GuiButtonSkill(skill, buttonId++, x, y, null, this.fontRenderer, GuiSkillTab.MAX_TEXT_WIDTH);
			this.buttonList.add(lastButton);

			// create child buttons
			for(Skill childSkill : skill.getChildSkills()) {
				double r = radius + GuiSkillTab.BUTTON_CONNECTION_LENGTH * childSkill.getLevel();
				int childX = (int) (xOffset + r * cos);
				int childY = (int) (yOffset + r * sin);
				lastButton = new GuiButtonSkill(childSkill, buttonId++, childX, childY, lastButton,
						this.fontRenderer, GuiSkillTab.MAX_TEXT_WIDTH);
				this.buttonList.add(lastButton);
			}

			angle += step;
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.buttonList.clear();
		this.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		int drawX = (this.width - GuiSkillTab.TEXTURE_WIDTH) / 2;
		int drawY = (this.height - GuiSkillTab.TEXTURE_HEIGHT) / 2;

		// draw background
		this.mc.getTextureManager().bindTexture(GuiSkillTab.TEXTURE);
		this.drawTexturedModalRect(drawX, drawY, 0, 0, GuiSkillTab.TEXTURE_WIDTH, GuiSkillTab.TEXTURE_HEIGHT);

		// update buttons, draw connections and get hovered button
		GuiButtonSkill hoveredButton = null;
		for (GuiButton guiButton : this.buttonList) {
			if ((guiButton instanceof GuiButtonSkill) && guiButton.visible) {
				GuiButtonSkill button = (GuiButtonSkill) guiButton;
				// "draw button" (actually is only used for calculating things like if the
				// button is being hovered)
				button.drawButton(this.mc, mouseX, mouseY, partialTicks);

				// draw connection between button and rect
				ClientHelper.drawConnection(button.getMiddleX(), button.getMiddleY(), this.rectMiddleX, this.rectMiddleY,
						this.zLevel);

				// draw connection between button and child button
				GuiButtonSkill child = button.getChildButton();
				if(child != null) {
					ClientHelper.drawConnection(button.getMiddleX(), button.getMiddleY(), child.getMiddleX(),
							child.getMiddleY(), this.zLevel - 1);
				}

				// if the player is hovering over the button
				if (button.isMouseOver()) {
					// set the hovered button to this button (it will be drawn later)
					hoveredButton = button;
				}
			}
		}

		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		for(GuiButton button : this.buttonList) {
			// if the button is a skill button and is not hovered,
			if((button instanceof  GuiButtonSkill) && (button != hoveredButton)) {
				// draw it
				this.drawSkillButton((GuiButtonSkill) button);
			}
		}

		// draw rect (the one in the middle)
		ClientHelper.drawRect(this.rectMinX, this.rectMinY, this.rectMaxX, this.rectMaxY, this.zLevel);

		// draw xp
		String xp = String.valueOf(this.mc.player.experienceLevel);
		ClientHelper.drawCentralString(xp, this.rectMiddleX, this.rectMiddleY, 3141706, 1.5F, GuiSkillTab.RECT_SIZE,
				this.fontRenderer);

		// make everything darker if player hovers over button (fade gets set to 0 when
		// the player doesn't hover over something and thus the rectangle will become
		// completely transparent)
		Gui.drawRect(drawX + 8, drawY + 17, drawX + GuiSkillTab.TEXTURE_WIDTH - 12, drawY + GuiSkillTab.TEXTURE_HEIGHT - 8,
				MathHelper.floor(this.fade * 255.0F) << 24);

		// if no button is being hovered,
		if (hoveredButton == null) {
			// make the fade go away
			this.fade = MathHelper.clamp(this.fade - 0.04F, 0.0F, 1.0F);
		} else {
			// otherwise draw the button and its extra information
			this.drawHover(hoveredButton);
			// change the fade so that everything becomes darker (except for the button)
			this.fade = MathHelper.clamp(this.fade + 0.02F, 0.0F, 0.3F);
		}
	}

	public void drawHover(GuiButtonSkill hoveredButton){
		Skill skill = hoveredButton.getSkill();
		boolean hasSkill = this.tp.hasSkill(skill);

		// draw skill name, cost and description
		int textY = hoveredButton.y;
		int descriptionY = textY + this.fontRenderer.FONT_HEIGHT + hoveredButton.costHeight;

		// decide where the x position of the text should be. If the text is nearer
		// to the right side, draw it on the left, otherwise on the right
		int textX = hoveredButton.x + (GeneralHelper.isACloserThanB(hoveredButton.x, 0, this.width)
				? hoveredButton.width + 4 : -hoveredButton.highestWidth - 4);

		// draw the background for the description
		ClientHelper.drawRect(textX - 2, textY - 2,textX + hoveredButton.highestWidth, descriptionY
				+ hoveredButton.descriptionHeight + 2, this.zLevel);

		// if the player has the skill, the name is golden, otherwise black
		this.fontRenderer.drawString(hoveredButton.name, textX, textY, hasSkill ? 13666304 : GuiSkillTab.TOOLTIP_COLOR);
		// if the player can unlock the skill, the price is green, otherwise red
		this.fontRenderer.drawSplitString(hoveredButton.cost, textX, textY + hoveredButton.nameHeight,
				GuiSkillTab.MAX_TEXT_WIDTH, hasSkill ? GuiSkillTab.TOOLTIP_COLOR : (skill.canPlayerUnlockSkill(this.mc.player) ?
				47360 : 11141120));
		this.fontRenderer.drawSplitString(hoveredButton.description, textX, descriptionY, GuiSkillTab.MAX_TEXT_WIDTH, GuiSkillTab.TOOLTIP_COLOR);

		// draw the actual skill button
		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		this.drawSkillButton(hoveredButton);
	}

	/**
	 * Draws a skill button. Requires {@link TextureMap#LOCATION_BLOCKS_TEXTURE} to be
	 * bound in order to work correctly
	 */
	public void drawSkillButton(GuiButtonSkill button) {
		// if the player already has the skill, color it
		if (this.tp.hasSkill(button.getSkill())) {
			GlStateManager.color(208, 136, 0);
		} else {
			// otherwise reset the color
			GlStateManager.color(255, 255, 255);
		}
		this.drawTexturedModalRect(button.x, button.y, button.getSkill().getIconAsSprite(), button.width,
				button.height);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton) throws IOException {
		super.actionPerformed(guiButton);
		if(guiButton instanceof GuiButtonSkill){
			Skill skill = ((GuiButtonSkill) guiButton).getSkill();
			if(skill.canPlayerUnlockSkill(this.mc.player)){
				this.mc.displayGuiScreen(new GuiSkillBuyConfirmation(skill));
			}
		}
	}

	private class GuiSkillBuyConfirmation extends GuiYesNo {
		private final Skill skill;

		private GuiSkillBuyConfirmation(Skill skill){
			super(GuiSkillTab.this, I18n.format("huntersdream.permissionUnlockSkill",
					I18n.format(skill.getTranslationKeyName()), skill.getNeededExperienceLevels()), "", -1);
			this.skill = skill;
		}

		@Override
		protected void actionPerformed(GuiButton button) {
			if(button.id == 0) {
				PacketHandler.sendSkillUnlockMessage(this.mc.world, this.skill);
			}
			this.mc.displayGuiScreen(null);
		}
	}
}