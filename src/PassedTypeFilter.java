import java.io.File;
import javax.swing.filechooser.FileFilter;

public class PassedTypeFilter extends FileFilter {
    //all this just to look for one particular type.
	//used like: JFileChooser(currDir).setFileFilter(new PassedTypeFilter("txt", "Text files"));

	private String fileExtn, fileDescrip;
	
    public PassedTypeFilter(String passedExtn, String passedDescrip) {
    	fileExtn = passedExtn.toLowerCase();
    	fileDescrip = passedDescrip;
    }
	
	public boolean accept(File f) {
        if (f.isDirectory()) return true;
        
        String fextention = null;
        String fname = f.getName();
        int dotPos = fname.lastIndexOf('.');
        if (dotPos > 0 &&  dotPos < fname.length() - 1) 
                fextention = fname.substring(dotPos+1).toLowerCase();       
        if (fextention != null) {
            if (fextention.equals(fileExtn)) return true;
            else return false;
        }
        return false;
    }

    //The description of this filter
    public String getDescription() {
        return fileDescrip;
    }

}
