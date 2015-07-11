package gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import controller.Controller;


public class MainFrame extends JFrame {
	
	

	private static final long serialVersionUID = 6575152134750353583L;
	private TextPanel textPanel;
	private Toolbar toolbar;
	private FormPanel formPanel;
	private JFileChooser fileChooser;
	private Controller controller;
	private TablePanel tablePanel;
	private  PrefsDialog prefsDialog;
	private Preferences prefs;
	private JSplitPane splitPane;
	private JTabbedPane tabPane;
	private MessagePanel messagePanel;
	
	
	public MainFrame(){
		super("Hello World");
		
		setLayout(new BorderLayout());//refer Border Layout here https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html
		
		toolbar = new Toolbar();
		textPanel = new TextPanel();//Second Component
		formPanel = new FormPanel();
		tablePanel = new TablePanel();
		prefsDialog = new PrefsDialog(this);
		tabPane = new JTabbedPane();
		messagePanel = new MessagePanel();
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPanel, tabPane);
		
		splitPane.setOneTouchExpandable(true);
		tabPane.addTab("Person Database", tablePanel);
		tabPane.addTab("Messages", messagePanel);
		
		
		prefs = Preferences.userRoot().node("db");
		
		controller = new Controller();
		
		tablePanel.setData(controller.getPeople());
		
		tablePanel.setPersonTableListener(new PersonTableListener(){
			public void rowDeleted(int row){
				controller.removePerson(row);
				System.out.println(row);
			}
		});
		
		prefsDialog.setPersonTableListener(new PrefsListener() {
			
			@Override
			public void preferencesSet(String user, String password, int port) {
				//System.out.println(user + ": " + password);
				prefs.put("user", user);
				prefs.put("password", password);
				prefs.putInt("port", port);
				
			}
		});
		
		String user = prefs.get("user", "");	
		String password = prefs.get("password", "");
		Integer port = prefs.getInt("port", 3306);
		prefsDialog.setDefaults(user, password, port);
		
		fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new PersonFileFilter());
		
		setJMenuBar (createMenuBar());
		
		
		toolbar.setToolbarListener(new ToolbarListener() {

			@Override
			public void saveEventOccured(){
				connect();
				
				try {
					controller.save();
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(MainFrame.this, "Unable to load from database.", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
				}

			}

			@Override
			public void refreshEventOccured() {
				//System.out.println("Refresh");
				connect();
				try {
					controller.load();
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(MainFrame.this, "Unable to save to database.", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
				}
				
				tablePanel.refresh();
			}
		});

		formPanel.setFormListener(new FormListener(){
			public void formEventOccurred(FormEvent e){
				controller.addPerson(e);// use Form listener to forward the parameters to Controller.java by e
				tablePanel.refresh();
			}
		
		});
		
		addWindowListener(new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("Window closing");
				controller.disconnect();
				System.out.println("controller disconnected");
				dispose();
				System.gc();
			}
			
		});
		

		
		//add(formPanel, BorderLayout.WEST);
		add(toolbar,BorderLayout.PAGE_START);
		add(splitPane, BorderLayout.CENTER);
		//add(tablePanel, BorderLayout.CENTER); //add self into Jpanel
		
		setMinimumSize(new Dimension(500, 400));
		setSize(600, 500);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}
	
	private void connect(){
		try {
			controller.connect();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(MainFrame.this, "Cannot connect to database.", "Database Connection Problem", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		
		
		JMenuItem exportDataItem = new JMenuItem("Export Data ...");
		JMenuItem importDataItem = new JMenuItem("Import Data ...");
		JMenuItem exitItem = new JMenuItem("Exit");
		
		
		fileMenu.add(exportDataItem);
		fileMenu.add(importDataItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
		JMenu windowMenu = new JMenu("Window");
		JMenu showMenu = new JMenu("show");
		JMenuItem prefsItem = new JMenuItem("Preferences...");		
		JCheckBoxMenuItem showFormItem = new JCheckBoxMenuItem("Person Form");
		showFormItem.setSelected(true);
		
		showMenu.add(showFormItem);
		windowMenu.add(showMenu);
		windowMenu.add(prefsItem);
		
		menuBar.add(fileMenu);
		menuBar.add(windowMenu);
		
		prefsItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				prefsDialog.setVisible(true);
				
			}
		});
		
		showFormItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent ev) {
				
				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) ev.getSource();
				
				if(menuItem.isSelected()){
					splitPane.setDividerLocation((int)formPanel.getMinimumSize().getWidth());
				}
				formPanel.setVisible(menuItem.isSelected());
			}
		});
		
		fileMenu.setMnemonic(KeyEvent.VK_F);
		exitItem.setMnemonic(KeyEvent.VK_X);
		
		prefsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		
		importDataItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		
		
		importDataItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
					try {
						controller.loadFromFile(fileChooser.getSelectedFile());
						tablePanel.refresh();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MainFrame.this, "Could not load data from file.", "Error",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
					
				}
				
			}
			
		});
		
		exportDataItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION){
					
					try {
						
						controller.saveToFile(fileChooser.getSelectedFile());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						JOptionPane.showMessageDialog(MainFrame.this, 
								"Could not save data to file.", "Error",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
				
			}
			
		});
		
		exitItem.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				/*String text = JOptionPane.showInputDialog(MainFrame.this, 
						"Enter your user name.",
						"Enter User Name", JOptionPane.OK_OPTION|JOptionPane.QUESTION_MESSAGE );	
				
				System.out.println(text);*/
				int action = JOptionPane.showConfirmDialog(MainFrame.this, 
						"Do you really want to exit this application?",
						"Confirm Exit", JOptionPane.OK_CANCEL_OPTION);	
				
				if(action == JOptionPane.OK_OPTION){
					
					//System.exit(0);
					WindowListener [] listeners = getWindowListeners();
					
					for(WindowListener listener: listeners){
						listener.windowClosing(new WindowEvent(MainFrame.this, 0));
					}
				}
				
			}
		});
		return menuBar;
	}

}
