package GUI;

import SearchComponents.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.*;

public class resultWindow extends JPanel implements ActionListener, MouseListener{
	private JList<String> list;
	private DefaultListModel<String> listModel;
	private JButton btnNewQuery;
	private JTextField searchField;
	
	public resultWindow() {
		setLayout(new BorderLayout());
		listModel = new DefaultListModel<String>();
		list = new JList<String>(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		list.addMouseListener(this);
		List<String> results = SearchEngine.getqueryResult();
		list.setVisibleRowCount(results.size());
		
		for(String docs : results){
			listModel.addElement(docs);
		}
		JScrollPane listScrollPane = new JScrollPane(list);
		listScrollPane.setPreferredSize(new Dimension(480, 360));
		JButton btnNewQuery = new JButton("Close");
		btnNewQuery.addActionListener(this);
		btnNewQuery.setEnabled(true);
		btnNewQuery.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(btnNewQuery);

		add(listScrollPane, BorderLayout.CENTER);
		add(resultPanel, BorderLayout.PAGE_END);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource() == btnNewQuery){
			//Figure out how to close just this window with the button
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		//JList list = (JList)e.getSource();
		if(e.getClickCount() == 2){
			int index = list.locationToIndex(e.getPoint());
			if(index >= 0){
				if(Desktop.isDesktopSupported()){
					String doc = list.getModel().getElementAt(index);
					String path = SearchEngine.getPath().toString();
					Path filePath = Paths.get(path + "\\" + doc).toAbsolutePath();
					try {
						Desktop.getDesktop().open(filePath.toFile());
					} catch (IOException e1) {
						System.out.println("No such file.");
					}
				}
			}
			
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//Do nothing
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//Do nothing
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//Do nothing
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//Do nothing
	}
}
