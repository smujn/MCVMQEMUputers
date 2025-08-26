package mcvmcomputers.client.gui.setup.pages;

import mcvmcomputers.client.gui.setup.GuiSetup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;

public abstract class SetupPage {
	protected final GuiSetup setupGui;
	protected final TextRenderer textRender;
	protected final MinecraftClient minecraft;
	
	public SetupPage(GuiSetup setupGui, TextRenderer textRender) {
		this.setupGui = setupGui;
		this.textRender = textRender;
		this.minecraft = MinecraftClient.getInstance();
	}
	
	public abstract void render(int mouseX, int mouseY, float delta);
	public abstract void init();
}
