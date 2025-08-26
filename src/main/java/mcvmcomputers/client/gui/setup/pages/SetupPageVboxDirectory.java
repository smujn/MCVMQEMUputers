package mcvmcomputers.client.gui.setup.pages;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import mcvmcomputers.client.ClientMod;
import mcvmcomputers.client.gui.setup.GuiSetup;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class SetupPageVboxDirectory extends SetupPage{
	private TextFieldWidget vboxDirectory;
	private ButtonWidget next;
	private String vboxStatus;
	
	public SetupPageVboxDirectory(GuiSetup setupGui, TextRenderer textRender) {
		super(setupGui, textRender);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.textRender.draw(setupGui.translation(ClientMod.qemu ? "mcvmcomputers.setup.qemu_dir" : "mcvmcomputers.setup.vbox_dir"), setupGui.width/2-160, setupGui.height/2-20, -1);
		this.textRender.draw(vboxStatus, setupGui.width/2-160, setupGui.height/2+13, -1);
		this.textRender.draw(setupGui.translation("mcvmcomputers.setup.dontchange0"), setupGui.width/2-160, 60, -1);
		this.textRender.draw(setupGui.translation("mcvmcomputers.setup.dontchange1"), setupGui.width/2-160, 70, -1);
		this.vboxDirectory.render(mouseX, mouseY, delta);
	}
	
	private void next(ButtonWidget bw) {
		if(checkDirectory(vboxDirectory.getText())) {
			this.setupGui.virtualBoxDirectory = vboxDirectory.getText();
			this.setupGui.nextPage();
		}
	}
	
	private boolean checkDirectory(String s) {
		if(s.isEmpty()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_empty");
			next.active = false;
			return false;
		}
		File vboxDir = new File(s);
		if(!vboxDir.exists()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_empty");
			next.active = false;
			return false;
		}else if(vboxDir.isFile()) {
			vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notfound");
			next.active = false;
			return false;
		}else {
			if(ClientMod.qemu) {
				if(SystemUtils.IS_OS_WINDOWS) {
					if(!new File(vboxDir, "qemu-system-x86_64.exe").exists() || !new File(vboxDir, "qemu-system-i386.exe").exists()) {
						vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notqemu");
						next.active = false;
						return false;
					}
				}
			}else {
				if(SystemUtils.IS_OS_WINDOWS) {
					if(!new File(vboxDir, "vboxmanage.exe").exists() || !new File(vboxDir, "vboxwebsrv.exe").exists()) {
						vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notvbox");
						next.active = false;
						return false;
					}
				}else if(SystemUtils.IS_OS_MAC) {
					if(!new File(vboxDir, "VBoxManage").exists() || !new File(vboxDir, "vboxwebsrv").exists()) {
						vboxStatus = setupGui.translation("mcvmcomputers.input_dir_notvbox");
						next.active = false;
						return false;
					}
				}
			}
		}
		vboxStatus = setupGui.translation(ClientMod.qemu ? "mcvmcomputers.input_dir_yesqemu" : "mcvmcomputers.input_dir_yesvbox");
		next.active = true;
		return true;
	}

	@Override
	public void init() {
		next = new ButtonWidget(setupGui.width/2 - 40, setupGui.height - 40, 80, 20, setupGui.translation("mcvmcomputers.setup.nextButton"), (bw) -> this.next(bw));
		String dirText = this.setupGui.virtualBoxDirectory;
		if(vboxDirectory != null) {
			dirText = vboxDirectory.getText();
		}
		this.checkDirectory(dirText);
		vboxDirectory = new TextFieldWidget(this.textRender, setupGui.width/2 - 160, setupGui.height/2 - 10, 320, 20, "");
		vboxDirectory.setMaxLength(35565);
		vboxDirectory.setText(dirText);
		vboxDirectory.setChangedListener((s) -> checkDirectory(s));
		setupGui.addElement(vboxDirectory);
		setupGui.addButton(next);
	}

}
