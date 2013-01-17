/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.minelord.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTextField;

import net.minelord.data.ModPack;
import net.minelord.data.Settings;
import net.minelord.gui.LaunchFrame;
import net.minelord.log.Logger;
import net.minelord.util.DownloadUtils;
import net.minelord.util.ErrorUtils;
import net.minelord.util.FileUtils;
import net.minelord.util.OSUtils;

public class PrivatePackDialog extends JDialog {
	private JTextField textField = new JTextField();
	private JButton btnRemove = new JButton("Remove");
	private JButton btnAdd = new JButton("Add");
	private JButton btnCancel = new JButton("Cancel");
	private JEditorPane editorPane = new JEditorPane();

	public PrivatePackDialog() {
		super(LaunchFrame.getInstance(), true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		getContentPane().setLayout(null);
		setBounds(300, 300, 300, 250);
		setTitle("Private Pack Interface");
		setResizable(false);

		textField.setBounds(10, 137, 264, 30);
		textField.setColumns(10);
		add(textField);

		btnAdd.setBounds(10, 178, 82, 23);
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(DownloadUtils.staticFileExists(textField.getText() + ".xml") && !textField.getText().isEmpty()) {
					Logger.logInfo("Adding: " + textField.getText());
					ModPack.loadXml(textField.getText() + ".xml");
					Settings.getSettings().addPrivatePack(textField.getText());
					Settings.getSettings().save();
					setVisible(false);
				} else {
					ErrorUtils.tossError("Invalid Private Pack.");
				}
			}
		});
		add(btnAdd);
		getRootPane().setDefaultButton(btnAdd);

		btnCancel.setBounds(192, 178, 82, 23);
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(btnCancel);

		editorPane.setBounds(10, 11, 264, 115);
		editorPane.setEditable(false);
		editorPane.setHighlighter(null);
		editorPane.setContentType("text/html");
		editorPane.setText("Type in the private code that you get from the private pack owner and click add, the pack will then be in your pack list. It will automatically load on startup. If you wish to remove a private pack, type the code again, and click remove.");
		add(editorPane);

		btnRemove.setBounds(102, 178, 80, 23);
		btnRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> codes = Settings.getSettings().getPrivatePacks();
				if(codes.contains(textField.getText())) {
					Settings.getSettings().removePrivatePack(textField.getText());
					Settings.getSettings().save();
					try {
						for(ModPack pack : ModPack.getPackArray()) {
							if(pack.getParentXml().equalsIgnoreCase(textField.getText() + ".xml")) {
								FileUtils.delete(new File(OSUtils.getDynamicStorageLocation(), "ModPacks/" + pack.getDir()));
							}
						}
						ModPack.removePacks(textField.getText() + ".xml");
						FileUtils.delete(new File(OSUtils.getDynamicStorageLocation(), "ModPacks/" + textField.getText() + ".xml"));
						LaunchFrame.getInstance().modPacksPane.sortPacks();
					} catch (IOException e) {
						e.printStackTrace();
					}
					Logger.logInfo(textField.getText() + " successfully removed.");
					textField.setText("");
					setVisible(false);
				} else {
					Logger.logInfo("Incorrect pack name.");
				}
			}
		});
		add(btnRemove);
	}
}
