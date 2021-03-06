package io.github.keheck.window.dialogs;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class DialogLoadProjFilter extends FileFilter
{
    @Override
    public boolean accept(File f) { return f.isDirectory(); }

    @Override
    public String getDescription() { return "Root folder of the project"; }
}
