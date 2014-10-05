package GUI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.CardLayout;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import java.awt.Color;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.nio.file.Path;

public class GuiEngine implements ActionListener {

	private Path dirPath;
	private JFrame frmSearchEngine, resultWindow;
	private JTextField directoryTextField, userQuery;
	private JPanel MainPanel, directoryPanel, userQueryPanel, resultPanel, indexStatisticsPanel;
	private JTextArea indexStatWindow;
	private JButton btnIndex, btnChooseDirectory, btnSearch, btnViewIndexStatistics, btnNewQuery;
	 /* Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE); 
				//Setting look and feel for window's versions;
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					
					GuiEngine window = new GuiEngine();
					window.frmSearchEngine.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuiEngine() {
		initializeComponents();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeComponents() {
		frmSearchEngine = new JFrame("Search Engine");
		frmSearchEngine.getContentPane().setLayout(null);
		frmSearchEngine.setBounds(150, 150, 977, 518);
		initializePanels();
		initializeMainPanelComponenets();
		frmSearchEngine.getContentPane().add(MainPanel);
		initializeDirectoryComponenets();
		initializeUserQueryComponents();
		initializeResultComponents();
		initializeIndexStatComponents();

		
	}
	
	private void initializePanels(){
		MainPanel = new JPanel();
		directoryPanel = new JPanel();
		directoryTextField = new JTextField();
		userQueryPanel = new JPanel();
		resultPanel = new JPanel();
		indexStatisticsPanel = new JPanel();
	}
	
	private void initializeMainPanelComponenets(){
		MainPanel.setBounds(0, 0, 960, 479);
		MainPanel.setLayout(new CardLayout());
		MainPanel.add(directoryPanel, "directory_Panel");
		MainPanel.add(userQueryPanel, "userQuery_Panel");
	}
	
	private void initializeDirectoryComponenets(){
		directoryPanel.setBackground(Color.WHITE);
		directoryPanel.setLayout(null);
		btnIndex = new JButton("Index");
		btnIndex.setBounds(500, 242, 128, 26);
		directoryPanel.add(btnIndex);
		
		
		directoryTextField.setBounds(284, 211, 404, 20);
		directoryPanel.add(directoryTextField);
		directoryTextField.setColumns(10);
		
		btnChooseDirectory = new JButton("Choose Directory");
		btnChooseDirectory.addActionListener(this);
		btnChooseDirectory.setBounds(343, 242, 128, 26);
		directoryPanel.add(btnChooseDirectory);
		
		JLabel lblChooseADirectoy = new JLabel("Enter a directoy to index or click the Choose Directory button below");
		lblChooseADirectoy.setBounds(284, 185, 404, 14);
		directoryPanel.add(lblChooseADirectoy);
	}
	
	private void initializeUserQueryComponents(){
		userQueryPanel.setBackground(Color.WHITE);
		userQueryPanel.setLayout(null);
		
		JLabel lblGoclongle = new JLabel("Goclongle");
		lblGoclongle.setBounds(402, 186, 106, 26);
		lblGoclongle.setFont(new Font("Bookman Old Style", Font.BOLD, 21));
		userQueryPanel.add(lblGoclongle);
		
		userQuery = new JTextField();
		userQuery.setBounds(241, 218, 429, 20);
		userQueryPanel.add(userQuery);
		userQuery.setColumns(10);
		
		btnSearch = new JButton("Search");
		btnSearch.setBounds(675, 217, 89, 23);
		userQueryPanel.add(btnSearch);
		
		btnViewIndexStatistics = new JButton("View Index Statistics");
		btnViewIndexStatistics.setBounds(402, 243, 148, 23);
		userQueryPanel.add(btnViewIndexStatistics);
	}
	
	private void initializeResultComponents(){
		resultPanel.setBackground(Color.GRAY);
		MainPanel.add(resultPanel, "result_Panel");
		resultPanel.setLayout(null);
		
		btnNewQuery = new JButton("New Query");
		btnNewQuery.setBounds(0, 0, 960, 23);
		resultPanel.add(btnNewQuery);
	}
	
	private void initializeIndexStatComponents(){
		indexStatisticsPanel.setBackground(Color.WHITE);
		MainPanel.add(indexStatisticsPanel, "indexStatistics_Panel");
		indexStatisticsPanel.setLayout(null);
		
		indexStatWindow = new JTextArea();
		indexStatWindow.setLineWrap(true);
		indexStatWindow.setBounds(10, 11, 940, 457);
		indexStatWindow.setFont(new Font("Arial", Font.PLAIN, 12));
		indexStatisticsPanel.add(indexStatWindow);
	}
	
	private void switchPanels(JPanel curPanel){
		frmSearchEngine.setContentPane(curPanel);
	}
	
	private JPanel getMainPanel(){
		return MainPanel;
	}
	
	private JPanel getDirectoryPanel(){
		return directoryPanel;
	}
	
	private JPanel getUserQueryPanel(){
		return userQueryPanel;
	}
	
	private JPanel getResultPanel(){
		return resultPanel;
	}
	
	private JPanel getIndexStatisticPanel(){
		return indexStatisticsPanel;
	}
	
	public void actionPerformed(ActionEvent e){
		//Handle index button
		if(e.getSource() == btnIndex){
			directoryTextField.getText();
			//index specified
		}
		
		if(e.getSource() == btnChooseDirectory){
			directoryTextField.setText(getDirectory(frmSearchEngine));
		}
	}
	
	private String getDirectory(JFrame parent){
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			return fc.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
}
