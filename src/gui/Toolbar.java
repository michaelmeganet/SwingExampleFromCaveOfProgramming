package gui;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public class Toolbar extends JToolBar implements ActionListener{
	private JButton saveButton;
	private JButton refreshButton;
	
	private ToolbarListener textListener;
	private TextPanel textPanel;
	
	public Toolbar() {
		
		// Get rid of the border if you want the toolbar draggable
		setBorder(BorderFactory.createEtchedBorder());
		
		setFloatable(false);
		
		saveButton = new JButton();
		saveButton.setIcon(createIcon("/Images/Save16.gif"));
		saveButton.setToolTipText("Save");
		
		
		refreshButton = new JButton();
		refreshButton.setIcon(createIcon("/Images/Refresh16.gif"));
		refreshButton.setToolTipText("Refresh");
		
		
		saveButton.addActionListener(this);
		refreshButton.addActionListener(this);
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		add(saveButton);
		//addSeparator();
		add(refreshButton);
	}
	
	private ImageIcon createIcon(String path){
		URL url = getClass().getResource(path);
		
		if(url == null){
			System.err.println("Unable to load image: " + path);
		}
		
		ImageIcon icon = new ImageIcon(url);
		return icon;
		
	}
		
	public void setToolbarListener(ToolbarListener listener){
		this.textListener = listener;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		
		JButton clicked = (JButton) e.getSource();
				
		
		if(clicked == saveButton){
			if(textListener != null){
				try {
					textListener.saveEventOccured();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
		}else if(clicked == refreshButton){
			textListener.refreshEventOccured();
		}
		
	}
}
