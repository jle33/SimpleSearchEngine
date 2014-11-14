package GUI;



import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.JTextField;

import SearchComponents.DiskEngine;
import SearchComponents.IndexWriter;
import SearchComponents.SearchEngine;
import SearchComponents.SyntaxCheck;

import java.awt.Font;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class GuiEngine extends JFrame implements ActionListener {
	//test
	//private Path dirPath;
	private JFrame frmSearchEngine;
	private JTextField directoryTextField, userQuery;
	private JPanel mainPanel, directoryPanel, userQueryPanel, indexStatisticsPanel;
	private JTextArea indexStatWindow;
	private JButton btnIndex, btnBack, btnChooseDirectoryToIndex, btnSearch, btnViewIndexStatistics;
	private JButton btnBackToSearch;
	private JPanel mainMenuPanel;
	private JButton btnBuildIndex;
	private JButton btnQueryIndexModes;
	private JPanel ReadIndexPanel;
	private JTextField dirTextField_ReadIndex;
	private JLabel lblEnterADirectoy;
	private JButton btnChooseDirectoryToRead;
	private JButton btnReadIndex;
	private JButton btnReadIndexToMainMenu;
	/* Launch the application.
	 */
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE); 
				//Setting look and feel for window's versions;
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				new GuiEngine();
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
		frmSearchEngine.setIconImage(Toolkit.getDefaultToolkit().getImage(GuiEngine.class.getResource(getImagePath())));
		frmSearchEngine.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSearchEngine.getContentPane().setLayout(null);
		frmSearchEngine.setBounds(150, 150, 977, 518);
		frmSearchEngine.setResizable(false);
		System.out.println("DEBUG" + "Retry");
		initializePanels();
		initializeDirectoryComponenets();
		initializeUserQueryComponents();
		initializeIndexStatComponents();
		initializeMainPanelComponenets();

		frmSearchEngine.getContentPane().add(mainPanel, BorderLayout.CENTER);
		frmSearchEngine.setLocationRelativeTo(null);
		frmSearchEngine.setVisible(true);
	}

	private void initializePanels(){
		mainPanel = new JPanel();
		directoryPanel = new JPanel();
		directoryTextField = new JTextField();
		userQueryPanel = new JPanel();
		indexStatisticsPanel = new JPanel();
	}

	private void initializeMainPanelComponenets(){
		mainPanel.setBounds(0, 0, 971, 490);
		mainPanel.setLayout(new CardLayout());
		
		mainMenuPanel = new JPanel();
		mainPanel.add(mainMenuPanel, "name_31040754599711");
		mainMenuPanel.setLayout(null);
		mainMenuPanel.setBackground(Color.WHITE);
		
		btnBuildIndex = new JButton("Build Index");
		btnBuildIndex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchPanels(directoryPanel, "directory_Panel");
			}
		});
		btnBuildIndex.setBounds(400, 241, 154, 26);
		mainMenuPanel.add(btnBuildIndex);
		
		btnQueryIndexModes = new JButton("Read and Query Index");
		btnQueryIndexModes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchPanels(ReadIndexPanel, "ReadIndex_Panel");
			}
		});
		btnQueryIndexModes.setBounds(400, 276, 154, 26);
		mainMenuPanel.add(btnQueryIndexModes);
		
		JLabel MainScreenLogo = new JLabel("Goclongle");
		MainScreenLogo.setFont(new Font("Bookman Old Style", Font.BOLD, 50));
		MainScreenLogo.setBounds(351, 78, 263, 108);
		mainMenuPanel.add(MainScreenLogo);
		
		JLabel lbMainMenu = new JLabel("Menu");
		lbMainMenu.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		lbMainMenu.setBounds(453, 203, 44, 27);
		mainMenuPanel.add(lbMainMenu);

		
		ReadIndexPanel = new JPanel();
		ReadIndexPanel.setLayout(null);
		ReadIndexPanel.setBackground(Color.WHITE);
		mainPanel.add(ReadIndexPanel, "name_39062028476335");
		
		dirTextField_ReadIndex = new JTextField();
		dirTextField_ReadIndex.setColumns(10);
		dirTextField_ReadIndex.setBounds(284, 211, 404, 20);
		ReadIndexPanel.add(dirTextField_ReadIndex);
		
		lblEnterADirectoy = new JLabel("Enter a directoy to read an index from or click the Choose Directory button below");
		lblEnterADirectoy.setBounds(284, 185, 404, 14);
		ReadIndexPanel.add(lblEnterADirectoy);
		
		btnChooseDirectoryToRead = new JButton("Choose Directory");
		btnChooseDirectoryToRead.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Path dirPath = getDirectory(frmSearchEngine);
				dirTextField_ReadIndex.setText(dirPath.toString());
				SearchEngine.setPath(dirPath);
			}
		});
		btnChooseDirectoryToRead.setBounds(348, 242, 128, 26);
		ReadIndexPanel.add(btnChooseDirectoryToRead);
		
		btnReadIndex = new JButton("Read Index");
		btnReadIndex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String indexName = dirTextField_ReadIndex.getText();
				System.out.println(indexName);
				DiskEngine.setDiskPositionalIndex(indexName);
				switchPanels(userQueryPanel, "userQuery_Panel");
			}
		});
		btnReadIndex.setBounds(500, 242, 128, 26);
		ReadIndexPanel.add(btnReadIndex);
		
		btnReadIndexToMainMenu = new JButton("Back");
		btnReadIndexToMainMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchPanels(mainMenuPanel, "mainMenu_Panel");
			}
		});
		btnReadIndexToMainMenu.setBounds(0, 0, 89, 23);
		ReadIndexPanel.add(btnReadIndexToMainMenu);
		
		//Add panels to main panel
		mainPanel.add(mainMenuPanel, "mainMenu_Panel");
		mainPanel.add(directoryPanel, "directory_Panel");
		mainPanel.add(userQueryPanel, "userQuery_Panel");
		mainPanel.add(indexStatisticsPanel, "indexStatistics_Panel");
		mainPanel.add(ReadIndexPanel, "ReadIndex_Panel");
		mainPanel.validate();
	}

	private void initializeDirectoryComponenets(){
		directoryPanel.setBackground(Color.WHITE);
		directoryPanel.setLayout(null);

		//Create indexing button
		btnIndex = new JButton("Index");
		btnIndex.addActionListener(this);
		btnIndex.setBounds(500, 242, 128, 26);

		//Create directory text field
		directoryTextField.setBounds(284, 211, 404, 20);
		directoryPanel.add(directoryTextField);
		directoryTextField.setColumns(10);

		//Create choose directory button
		btnChooseDirectoryToIndex = new JButton("Choose Directory");
		btnChooseDirectoryToIndex.setBounds(348, 242, 128, 26);
		btnChooseDirectoryToIndex.addActionListener(this);

		//Create label
		JLabel lblChooseADirectoy = new JLabel("Enter a directoy to index or click the Choose Directory button below");
		lblChooseADirectoy.setBounds(284, 185, 404, 14);

		//Add components to directory panel
		directoryPanel.add(lblChooseADirectoy);
		directoryPanel.add(btnChooseDirectoryToIndex);
		directoryPanel.add(btnIndex);
		
		JButton btnBackToMainMenu = new JButton("Back");
		btnBackToMainMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchPanels(mainMenuPanel, "mainMenu_Panel");
			}
		});
		btnBackToMainMenu.setBounds(0, 0, 89, 23);
		directoryPanel.add(btnBackToMainMenu);
	}

	private void initializeUserQueryComponents(){
		userQueryPanel.setBackground(Color.WHITE);
		userQueryPanel.setLayout(null);

		//Create user query text field
		userQuery = new JTextField();
		userQuery.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() ==  KeyEvent.VK_ENTER){
					viewResults();
				}
			}
		});
		userQuery.setBounds(241, 218, 429, 20);
		userQuery.setColumns(10);

		//Create search button
		btnSearch = new JButton("Search");
		btnSearch.setBounds(675, 217, 89, 23);
		btnSearch.addActionListener(this);

		//Create index statistics button
		btnViewIndexStatistics = new JButton("View Index Statistics");
		btnViewIndexStatistics.setBounds(390, 249, 148, 23);
		btnViewIndexStatistics.addActionListener(this);

		//Create back button
		btnBack = new JButton("Back");
		btnBack.addActionListener(this);
		btnBack.setBounds(0, 0, 89, 23);
		userQueryPanel.add(btnBack);

		//Create Goclongle label
		JLabel lblGoclongle = new JLabel("Goclongle");
		lblGoclongle.setBounds(402, 186, 106, 26);
		lblGoclongle.setFont(new Font("Bookman Old Style", Font.BOLD, 21));

		//Add components to user query panel
		userQueryPanel.add(lblGoclongle);
		userQueryPanel.add(userQuery);
		userQueryPanel.add(btnSearch);
		userQueryPanel.add(btnViewIndexStatistics);

	}

	private void initializeIndexStatComponents(){
		indexStatisticsPanel.setBackground(Color.WHITE);
		indexStatisticsPanel.setLayout(null);

		//Create back to search panel button
		btnBackToSearch = new JButton("Back to Search");
		btnBackToSearch.setBounds(0, 0, 960, 23);
		btnBackToSearch.addActionListener(this);
		indexStatisticsPanel.add(btnBackToSearch);

		//Create text area
		indexStatWindow = new JTextArea();
		indexStatWindow.setLineWrap(true);
		indexStatWindow.setBounds(0, 21, 960, 458);
		indexStatWindow.setFont(new Font("Arial", Font.PLAIN, 12));

		//Add components to index statistics panel
		indexStatisticsPanel.add(indexStatWindow);
	}

	private void switchPanels(JPanel curPanel, String layoutName){
		CardLayout cl = (CardLayout) (mainPanel.getLayout());
		cl.show(curPanel.getParent(), layoutName);
	}

	@Override
	public void actionPerformed(ActionEvent e){
		//Handle index button
		/*
		if(e.getSource() == btnIndex){
			//Pass directory path to index all text files
			Path path = Paths.get(directoryTextField.getText());

			try {
				SearchEngine.indexDirectory(path.toAbsolutePath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//Maybe have a working progress bar?
			//Change frame to userQuery
			switchPanels(userQueryPanel, "userQuery_Panel");
		}*/
		if(e.getSource() == btnIndex){
			String folder = directoryTextField.getText();
			IndexWriter writer = new IndexWriter(folder);
			try {
				/*
				 * TO-DO Put the buildIndex() in its seperate swing worker thread
				 * -It takes a long to time build an index
				 * -Have a loading screen or some indication that it is building the index
				 */
				LoadingDialogBox dialog = new LoadingDialogBox();
				//frmSearchEngine.setVisible(false);
				writer.buildIndex();
				//frmSearchEngine.setVisible(true);
				dialog.taskDone();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		//Handle back button
		else if(e.getSource() == btnBack){
			//switchPanels(directoryPanel, "directory_Panel");
			switchPanels(ReadIndexPanel, "ReadIndex_Panel");
		}
		//Handle choose directory button
		else if(e.getSource() == btnChooseDirectoryToIndex){
			Path dirPath = getDirectory(frmSearchEngine);
			String str = null;
			if(dirPath == null){
				str = "";
			}
			else {
				str = dirPath.toString();
			}
			directoryTextField.setText(str);

		}
		//Handle search button
		else if(e.getSource() == btnSearch){
			//Process user query and view results
			viewResults();
		}
		//Handle index statistics button
		else if(e.getSource() == btnViewIndexStatistics){
			indexStatWindow.setText(null);
			indexStatWindow.append(directoryTextField.getText() + "\n");
			indexStatWindow.append(SearchEngine.getStatistics());
			switchPanels(indexStatisticsPanel, "indexStatistics_Panel");
		}
		//Handle back to search button
		else if(e.getSource() == btnBackToSearch){
			//go back to search
			switchPanels(userQueryPanel, "userQuery_Panel");
		}
	}

	private Path getDirectory(JFrame parent){
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if(fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			return fc.getSelectedFile().toPath().toAbsolutePath();
		}
		return null;
	}

	private String getImagePath(){
		String imgPath = "/icon/GoClongle.png";
		return imgPath;
	}

	public void viewResults(){
		String query = userQuery.getText();
		//SearchEngine.processQuery(word);
		String status = SyntaxCheck.QuerySyntaxCheck(query);
		if(status.equals("Ok")){
			final List<String> queryResults = DiskEngine.processUserQuery(query);
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					createResultWindow(queryResults);
				}
			});
		}
		else
			JOptionPane.showMessageDialog(null, status, "Syntax Error", JOptionPane.INFORMATION_MESSAGE);
	}

	private void createResultWindow(List<String> queryResults){
		JFrame frame = new JFrame("Results : " + queryResults.size());
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(GuiEngine.class.getResource(getImagePath())));
		JComponent contentPane = new resultWindow(queryResults);
		contentPane.setOpaque(true);
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}
}
