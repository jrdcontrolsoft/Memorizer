

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

public class Memorizer extends JFrame {
	
	/*
	 * A program to memorize a screenfull of text:
	 * press the button, more letters are removed.
	 */
	
	private PopupErrorHandler allErrors = new PopupErrorHandler();
	private String defaultTxt = "\nUse the File menu to load the text file you wish to memorize.";
	private ArrayList<String> lines;
	private JTextPane textPane = new JTextPane();
	private String fontFace = "Monospaced";	//"SansSerif";
	private int fontSize = 12;
	private double charDivisor = 10; 
	private String currDir = getDefProgDir();
	
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
            	File fpath = getOneFileFromUser(currDir, null
            		, new PassedTypeFilter("txt", "Text files"));
            	if (fpath==null) return;
            	
            	BufferedReader br = openTextInput(fpath.toString(), allErrors);
            	if (br==null) return;
            	//load in file text
            	textPane.setText("");
            	lines = new ArrayList<String>();
            	String fileLine = readTextInput(br, allErrors);
            	while (fileLine!=null) {
            		lines.add(fileLine);
            		textPane.setText(textPane.getText() + fileLine + "\n");
            		fileLine = readTextInput(br, allErrors);
            	}
            	charDivisor = 10;	//reset back to beginning
            	boolean closedWorked = closeTextInput(br, allErrors);            	
            	/* while (!closedWorked) JOptionPane.showMessageDialog(null
            		, "That's weird--can't close!", "FILE CLOSING"
            		, JOptionPane.PLAIN_MESSAGE); */
            	textPane.setCaretPosition(0);
            }
        };
        Action quit = new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
            	closeProgram();
            }
        };
        MouseAdapter resetText = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//reset to original text loaded from file (or default)
				if (lines!=null) {
	            	textPane.setText("");
	            	for (int i=0; i<lines.size();i++) {
	            		textPane.setText(textPane.getText() 
	            			+ lines.get(i) + "\n");
	            	}
	            	charDivisor = 10;
	            	textPane.setCaretPosition(0);
	            	textPane.invalidate();
				} else {
					textPane.setText(defaultTxt);
				}
			}
        };
        Action mono = new AbstractAction("Monospaced") {
            public void actionPerformed(ActionEvent e) {
            	fontFace = "Monospaced";
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action serif = new AbstractAction("Serif") {
            public void actionPerformed(ActionEvent e) {
            	fontFace = "Serif";
                textPane.setFont(new Font(fontFace, Font.PLAIN, fontSize));
            	textPane.invalidate();
            }
        };
        Action sserif = new AbstractAction("SansSerif") {
            public void actionPerformed(ActionEvent e) {
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
        /*
        //add keystrokes to quickly increase/decrease font
        InputMap textInputMap = textPane.getInputMap();
        //InputMap textInputMap = knockoutBtn.getInputMap();
        //Ctrl-"+" to increase the font
        KeyStroke tempkey = KeyStroke.getKeyStroke(KeyEvent.VK_ADD, Event.CTRL_MASK);
        textInputMap.put(tempkey, incSize);
        //Ctrl-"-" to decrease the font
        tempkey = KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, Event.CTRL_MASK);
        textInputMap.put(tempkey, decSize);
        */
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
    	textPane.setCaretPosition(0);
		if (charDivisor>1) charDivisor--;
    }
    
    //button to remove more letters
	private JButton knockoutBtn;	//, resetBtn;
	private JButton setupKnockoutBtn() {
		knockoutBtn = new JButton("Click for next step");
		knockoutBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteLetters();
			}
		});
		return knockoutBtn;
	}
	
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
    	// call with (currDir, this, new PassedTypeFilter("txt", "Text files")));
		JFileChooser LoadChooser = new JFileChooser(passedDir);
    	if (passedFilt!=null) {
    		LoadChooser.setFileFilter(passedFilt);
    	}
    	int LoadChoice = LoadChooser.showOpenDialog(parentComp);
    	if (LoadChoice==JFileChooser.APPROVE_OPTION) {
    		currDir = LoadChooser.getCurrentDirectory().getPath();
    		return LoadChooser.getSelectedFile();
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
		//add(textPane);
		add(new JScrollPane(textPane));	//but text should really be too short to scroll
		//add(BorderLayout.SOUTH, setupKnockoutPnl());
		add(BorderLayout.PAGE_END, setupKnockoutBtn());
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
