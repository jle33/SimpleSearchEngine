package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.io.IOException;
import javax.swing.JLabel;
import java.awt.Color;

public class LoadingDialogBox extends JDialog {
	
	private static final String IMAGE_URL = "/icon/ajax-loader.gif";
	private final JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JLabel imgLoading;
	/**
	 * Create the dialog.
	 * @throws IOException 
	 */
	public LoadingDialogBox() throws IOException {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setVisible(true);
		setTitle("Building Index..");
		setBounds(100, 100, 456, 114);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			ImageIcon loading = new ImageIcon(this.getClass().getResource(IMAGE_URL));
			
			imgLoading = new JLabel();
			imgLoading.setIcon(loading);
			JLabel lblBuildingIndexPlease = new JLabel("Building Index Please Wait");
			lblBuildingIndexPlease.setFont(new Font("Times New Roman", Font.BOLD, 14));
			contentPanel.add(lblBuildingIndexPlease);
			contentPanel.add(imgLoading);
			
		}

		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(Color.WHITE);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Done");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if(arg0.getActionCommand().equals("OK")){
							dispose();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}
	
	private void pause(){
		contentPanel.remove(imgLoading);
	}
	
	public void taskDone(){
		//pause();
		okButton.setEnabled(true);	
	}
	
	

}
