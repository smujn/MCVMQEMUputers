package mcvmcomputers.client.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.math.NumberUtils;
import org.virtualbox_6_1.AccessMode;
import org.virtualbox_6_1.DeviceType;
import org.virtualbox_6_1.IMedium;
import org.virtualbox_6_1.IProgress;
import org.virtualbox_6_1.MediumVariant;

import io.netty.buffer.Unpooled;
import mcvmcomputers.client.ClientMod;
import mcvmcomputers.networking.PacketList;
import mcvmcomputers.utils.MVCUtils;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Language;
import net.minecraft.util.PacketByteBuf;;

public class GuiCreateHarddrive extends Screen{
	private TextFieldWidget hddSize;
	private String status;
	private State currentState = State.MENU;
	private final Language lang = Language.getInstance();
	
	private static final char COLOR_CHAR = (char) (0xfeff00a7);
	
	public enum State{
		MENU,
		CREATE_NEW,
		SELECT_OLD
	}
	
	public GuiCreateHarddrive() {
		super(new TranslatableText("Create Harddrive"));
	}
	
	public String translation(String in) {
		return lang.translate(in).replace("%c", ""+MVCUtils.COLOR_CHAR);
	}
	
	@Override
	public void init() {
		if(currentState == State.CREATE_NEW) {
			String s = "";
			if(hddSize != null) {
				s = hddSize.getText();
			}
			hddSize = new TextFieldWidget(this.font, this.width/2-150, this.height/2-10, 300, 20, "");
			hddSize.setText(s);
			hddSize.setChangedListener((st) -> hddSizeUpdate(st));
			this.children.add(hddSize);
			this.hddSizeUpdate(hddSize.getText());
			
			this.addButton(new ButtonWidget(this.width/2-100, this.height/2+50, 200, 20, translation("mcvmcomputers.vhd_setup.newvhd"), (wdgt) -> createNew(wdgt)));
			this.addButton(new ButtonWidget(this.width - 60, this.height - 30, 50, 20, translation("mcvmcomputers.vhd_setup.menu"), (wdgt) -> switchState(State.MENU)));
		}else if(currentState == State.MENU) {
			this.addButton(new ButtonWidget(this.width/2 - 100, this.height/2 - 12, 200, 20, translation("mcvmcomputers.vhd_setup.newvhd"), (wdgt) -> switchState(State.CREATE_NEW)));
			this.addButton(new ButtonWidget(this.width/2 - 100, this.height/2 + 12, 200, 20, translation("mcvmcomputers.vhd_setup.oldvhd"), (wdgt) -> switchState(State.SELECT_OLD)));
		}else {
			int lastY = 60;
			ArrayList<File> files = new ArrayList<>();
			for(File f : ClientMod.vhdDirectory.listFiles()) {
				if(ClientMod.qemu) {
					if(f.getName().endsWith(".qcow2")) {
						files.add(f);
					}
				}else {
					if(f.getName().endsWith(".vdi")) {
						files.add(f);
					}
				}
			}
			files.sort(new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for(File f : files) {
				this.addButton(new ButtonWidget(this.width/2 - 90, lastY, 180, 14, f.getName() + " | " + ((float)f.length()/1024f/1024f) + " " + translation("mcvmcomputers.vhd_setup.mb_used"), (wdgt) -> selectOld(wdgt)));
				this.addButton(new ButtonWidget(this.width/2 + 92, lastY, 14, 14, "x", (wdgt) -> removevhd(f.getName())));
				lastY += 16;
			}
			
			this.addButton(new ButtonWidget(this.width - 60, this.height - 30, 50, 20, translation("mcvmcomputers.vhd_setup.menu"), (wdgt) -> switchState(State.MENU)));
		}
	}
	
	private void switchState(State newState) {
		this.buttons.clear();
		this.children.clear();
		currentState = newState;
		this.init();
	}
	
	private void selectOld(ButtonWidget wdgt) {
		String fileName = wdgt.getMessage().split(" | ")[0];
		PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
		pb.writeString(fileName);
		ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_CHANGE_HDD, pb);
		minecraft.openScreen(null);
	}
	
	private void createNew(ButtonWidget wdgt) {
		if(!status.startsWith(COLOR_CHAR + "c")) {
			Long size = Long.parseLong(hddSize.getText())*1024L*1024L;
			int i = ClientMod.latestVHDNum;
			File vhd = null;
			if(ClientMod.qemu) {
				vhd = new File(ClientMod.vhdDirectory, "vhd" + i + ".qcow2");
				try {
					ProcessBuilder pb = ClientMod.createQemuProcess("qemu-img", "create", "-f", "qcow2", vhd.getAbsolutePath(), Integer.parseInt(hddSize.getText())+"M");
					Process process = pb.start();
					int status = process.waitFor();
				}catch(Exception ex) {}
			}else {
				vhd = new File(ClientMod.vhdDirectory, "vhd" + i + ".vdi");
				
				IMedium hdd = ClientMod.vb.createMedium("vdi", vhd.getPath(), AccessMode.ReadWrite, DeviceType.HardDisk);
				IProgress pr = hdd.createBaseStorage(size, Arrays.asList(MediumVariant.Standard));
				pr.waitForCompletion(-1);
			}
			
			try {
				ClientMod.increaseVHDNum();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			PacketByteBuf pb = new PacketByteBuf(Unpooled.buffer());
			pb.writeString(vhd.getName());
			ClientSidePacketRegistry.INSTANCE.sendToServer(PacketList.C2S_CHANGE_HDD, pb);
			minecraft.openScreen(null);
		}
	}
	
	private void removevhd(String name) {
		new File(ClientMod.vhdDirectory, name).delete();
		minecraft.openScreen(null);
	}
	
	private void hddSizeUpdate(String in) {
		if(NumberUtils.isDigits(in)) {
			long i = 0;
			try {
				i = Long.parseLong(in);
			}catch(NumberFormatException e) {
				status = translation("mcvmcomputers.input_parser_error");
				return;
			}
			if(i > 0) {
				if(i*1024*1024 < 0) { //Buffer overflow
					status = translation("mcvmcomputers.input_too_much").replace("%s", ""+Long.MAX_VALUE/1024L/1024L);
					return;
				}
				
				if(i*1024*1024 >= new File(".").getFreeSpace()) {
					status = translation("mcvmcomputers.vhd_setup.space");
					return;
				}else {
					status = translation("mcvmcomputers.vhd_setup.validspace");
					return;
				}
			}else {
				status = translation("mcvmcomputers.input_too_little").replace("%s", "0");
				return;
			}
		}else {
			status = translation("mcvmcomputers.input_nan");
			return;
		}
	}
	
	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.renderBackground();
		if(currentState == State.CREATE_NEW) {
			this.font.draw(status, this.width/2-150, this.height/2+13, -1);
			this.font.draw(translation("mcvmcomputers.vhd_setup.vhdsize"), this.width/2-150, this.height/2-20, -1);
			this.hddSize.render(mouseX, mouseY, delta);
		}else if(currentState == State.MENU) {
			String s = translation("mcvmcomputers.vhd_setup.setupnewvhd");
			this.font.draw(s, this.width/2 - this.font.getStringWidth(s)/2, this.height/2 - 30, -1);
		}
		super.render(mouseX, mouseY, delta);
	}

}
