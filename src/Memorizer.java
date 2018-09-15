

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Memorizer extends JFrame {
	
	/*
	 * A program to memorize a screenfull of text:
	 * press the button, more letters are removed.
	 */
	
	private PopupErrorHandler allErrors = new PopupErrorHandler();
	private String defaultTxt = "\nLoad the text file you wish to memorize.";
	private ArrayList<String> lines;
	private JTextPane textPane = new JTextPane();
	private String fontFace = "Monospaced";	//"SansSerif";
	private int fontSize = 12;
	private double charDivisor = 10; 
	
	private class PopupErrorHandler {
		//show a popup window to the user with (default) error info
		public void handle(Exception passedExp) {
	        JOptionPane.showMessageDialog(null, passedExp.toString());
		}
	}
	
	//helper functions for menu item
    private JMenuItem createMenuItem(Action a) {
    	return createMenuItem(a, 0);
    }
    private JMenuItem createMenuItem(Action a, int mnemonic) {
        JMenuItem item = new JMenuItem(a);
        if (mnemonic!=0) item.setMnemonic(mnemonic);
        return item;
    }

    private JMenuBar setupMainMenu() {
    	//The main menu: File with Open & Exit
    	
    	Action open = new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) {
            	/* JOptionPane.showMessageDialog(null, "Open"
        			, "MENU SELECTION", JOptionPane.PLAIN_MESSAGE); */
            	File fpath = getOneFileFromUser(getDefProgDir(), null
            		, new PassedTypeFilter("txt", "Text files"));
            	/* JOptionPane.showMessageDialog(null, (fpath==null?"NULL":fpath)
        			, "FILE TO OPEN:", JOptionPane.PLAIN_MESSAGE); */
            	if (fpath==null) return;
            	
            	BufferedReader br = openTextInput(fpath.toString(), allErrors);
            	if (br==null) return;
            	//load in file text
            	textPane.setText("");
            	lines = new ArrayList<String>();
            	String fileLine = readTextInput(br, allErrors);
            	while (fileLine!=null) {
            		lines.add(fileLine);
            		//System.out.println(fileLine);
            		textPane.setText(textPane.getText() + fileLine + "\n");
            		fileLine = readTextInput(br, allErrors);
            	}
            	charDivisor = 10;	//reset back to beginning
            	boolean closedWorked = closeTextInput(br, allErrors);            	
            	/* while (!closedWorked) JOptionPane.showMessageDialog(null
            		, "That's weird--can't close!", "FILE CLOSING"
            		, JOptionPane.PLAIN_MESSAGE); */
            	
            }
        };
        Action quit = new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
            	/* JOptionPane.showMessageDialog(null, "Quit"
            		, "MENU SELECTION", JOptionPane.PLAIN_MESSAGE); */
            	closeProgram();
            }
        };
        MouseAdapter resetText = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
        		/* JOptionPane.showMessageDialog(null, "You clicked reset"
        			, "MENU SELECTION", JOptionPane.PLAIN_MESSAGE); */
				//reset to original text loaded from file (or default)
				if (lines!=null) {
	            	textPane.setText("");
	            	for (int i=0; i<lines.size();i++) {
	            		textPane.setText(textPane.getText() 
	            			+ lines.get(i) + "\n");
	            	}
	            	charDivisor = 10;
	            	textPane.invalidate();
				} else {
					textPane.setText(defaultTxt);
				}
			}
        };
        Action mono = new AbstractAction("Monospaced") {
            public void actionPerformed(ActionEvent e) {
        		/* JOptionPane.showMessageDialog(null, "Monospaced"
            		, "MENU SELECTION", JOptionPane.PLAIN_MESSAGE); */
            	fontFace = "Monospaced";
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action serif = new AbstractAction("Serif") {
            public void actionPerformed(ActionEvent e) {
        		/* JOptionPane.showMessageDialog(null, "Serif", "MENU SELECTION"
            		, JOptionPane.PLAIN_MESSAGE); */
            	fontFace = "Serif";
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action sserif = new AbstractAction("SansSerif") {
            public void actionPerformed(ActionEvent e) {
        		/* JOptionPane.showMessageDialog(null, "SansSerif", "MENU SELECTION"
            		, JOptionPane.PLAIN_MESSAGE); */
            	fontFace = "SansSerif";
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action incSize = new AbstractAction("Increase Size") {
            public void actionPerformed(ActionEvent e) {
            	fontSize++;
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action decSize = new AbstractAction("Decrease Size") {
            public void actionPerformed(ActionEvent e) {
            	if (fontSize>6) fontSize--;
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
    	
        JMenu flmenu = new JMenu("File");
        flmenu.setMnemonic(KeyEvent.VK_F);
        flmenu.add(createMenuItem(open, KeyEvent.VK_O));
        flmenu.add(createMenuItem(quit, KeyEvent.VK_X));
        JMenu rstmenu = new JMenu("Reset Text");
        rstmenu.addMouseListener(resetText);
        JMenu ftmenu = new JMenu("Font");
        ftmenu.setMnemonic(KeyEvent.VK_O);
        ftmenu.add(createMenuItem(mono));
        ftmenu.add(createMenuItem(serif));
        ftmenu.add(createMenuItem(sserif));
        ftmenu.add(createMenuItem(incSize));
        ftmenu.add(createMenuItem(decSize));
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(flmenu);
        menuBar.add(rstmenu);
        menuBar.add(ftmenu);
        return menuBar;
    }
    
    private void deleteLetters() {
    	//knock out letters at divisor rate: first 1/10, then 1/9...
    	//  ...to 1/2, then 1/1 (all)
    	
    	StringBuilder allChars = new StringBuilder(textPane.getText());
    	char c;
    	for (int i=0; i<allChars.length(); i++) {
    		c = allChars.charAt(i);
    		if (c!=' ' && c!='\n' && c!='\t'	//skip basic whitespace
    				&& Math.random()<(1.0/charDivisor)) {
        		allChars.setCharAt(i, ' ');
    		}
    	}
    	textPane.setText(allChars.toString());
		if (charDivisor>1) charDivisor--;
    }
    
    //button to remove more letters
	private JButton knockoutBtn;	//, resetBtn;
	private JButton setupKnockoutBtn() {
		knockoutBtn = new JButton("Click for next step");
		knockoutBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteLetters();
				//knockoutBtn.setText("Next step " + charDivisor);
			}
		});
		return knockoutBtn;
	}
	/*
	private JButton setupResetBtn() {
		resetBtn = new JButton("Reset text to beginning");
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		return resetBtn;
	}
	private JPanel setupKnockoutPnl() {
		JPanel knockoutPnl = new JPanel();
		knockoutPnl.setLayout(new BorderLayout());
		add(setupKnockoutBtn());
		add(BorderLayout.LINE_START,setupResetBtn());
		//setMaximumSize(new Dimension(Integer.MAX_VALUE,10));
		return knockoutPnl;
	}
	*/
	
    //file methods
	private String getDefProgDir() {
		try {
			return new File (".").getCanonicalPath();
		} catch (IOException e) {
			allErrors.handle(e);
			return "";
		}
	}
	
	private File getOneFileFromUser(String passedDir, Component parentComp
			, PassedTypeFilter passedFilt) {
    	// call (currDir, this, new PassedTypeFilter("txt", "Text files")));
		JFileChooser ImportChooser = new JFileChooser(passedDir);
    	if (passedFilt!=null) {
    		ImportChooser.setFileFilter(passedFilt);
    	}
    	int LoadChoice = ImportChooser.showOpenDialog(parentComp);
    	if (LoadChoice==JFileChooser.APPROVE_OPTION) {
    		return ImportChooser.getSelectedFile();
    	} else {
    		return null;
    	}
	}
	
	private synchronized BufferedReader openTextInput(String fileName
			, PopupErrorHandler noFileHandler) {
		try {
			BufferedReader inpStrFileStrm 
				= new BufferedReader(new FileReader(fileName));
			return inpStrFileStrm;
		} catch (FileNotFoundException e) {
			noFileHandler.handle(e);
        	return null;
		}
	}
	
	public synchronized String readTextInput(BufferedReader inpTextStrm
			, PopupErrorHandler ioHandler) {
		try {
    		String tempStr = inpTextStrm.readLine();
			return tempStr;
		} catch (IOException e) {
			ioHandler.handle(e);
        	return null;
		}
	}
	
	private synchronized boolean closeTextInput(BufferedReader inpTextStrm
			, PopupErrorHandler ioHandler) {
    	try {
    		inpTextStrm.close();
        	return true;
		} catch (IOException e) {
			ioHandler.handle(e);
        	return false;
		}
	}
	
	//program methods
	public void closeProgram() {
		Memorizer.this.dispose();	//disposing the Frame closes the program
	}
	
	@Override
	public void setVisible(boolean visibility) {
		//overriding standard method JUST to set a particular button focus
		super.setVisible(visibility);
		knockoutBtn.requestFocus();
	}
	
	
	public Memorizer() {
		//constructor
        setJMenuBar(setupMainMenu());
        textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
		textPane.setText(defaultTxt);
		textPane.setEditable(false);
		add(textPane);
		//add(BorderLayout.SOUTH, setupKnockoutPnl());
		add(BorderLayout.PAGE_END, setupKnockoutBtn());
		//add(BorderLayout.PAGE_START, setupResetBtn());
		//knockoutBtn.requestFocusInWindow();
		//getRootPane().setDefaultButton(knockoutBtn);
		//knockoutBtn.requestFocus();
	}
	
	public static void main(String[] args) {
		//main instances the program (all in a Swing JPanel)
		
		//Look and Feel: native Mac, Windows, or whatever 
    	try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
        	//swallow error: default Look-and-Feel will result anyway
        }
        
    	final Memorizer f = new Memorizer();
    	
		SwingUtilities.invokeLater(
			//the usual way to start a Swing program: stuff everything in a Runnable
			new Runnable() {
				public void run() {
					f.setTitle(f.getClass().getSimpleName());
					f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					f.setSize(800, 600);
					/*
					f.setIconImage(new ImageIcon(getClass().getResource(
						"bitmaps/progicon.gif")).getImage());
					*/
					f.setVisible(true);
				}
			}
		);
		
		
	}
	
}
