package io.github.keheck.project.saveandload;

import io.github.keheck.Main;
import io.github.keheck.util.Directories;
import io.github.keheck.util.Log;
import io.github.keheck.tree.AbstractNavTreeNode;
import io.github.keheck.tree.NavTreeFile;
import io.github.keheck.tree.NavTreeFolder;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class Save
{
    /**
     * Saves the root directory and moves further on
     * @param project the name of the root directory
     */
    public static void save(String project)
    {
        cleanDirtyFile();

        File file = new File(Directories.projectsDir, project);
        Log.f1("Starting saving process of " + project);
        try
        {
            if(!file.exists())
                file.mkdirs();

            AbstractNavTreeNode root = (AbstractNavTreeNode)Main.navTree.getModel().getRoot();
            Enumeration<TreeNode> namespaces = root.children();

            while(namespaces.hasMoreElements())
            {
                TreeNode space = namespaces.nextElement();

                if(space instanceof NavTreeFolder)
                {
                    handleFolder((NavTreeFolder)space, file);
                }
                else if(space instanceof NavTreeFile)
                {
                    handleFile((NavTreeFile)space, file);
                }
            }
        }
        catch (IOException e)
        {
            Log.e("Failed to save project! Ending saving process...");
            e.printStackTrace();
        }

        Log.f1("Finished saving process! PRoject is saved at " + file.getAbsolutePath());
    }

    /**
     * Handles any folder encountered
     * @param node The node representing a folder
     * @param parentDir The parent directory of the foler
     * @throws IOException when an I/O error occurs
     */
    private static void handleFolder(NavTreeFolder node, File parentDir) throws IOException
    {
        Log.f2("Handling folder " + node.toString() + " and it's children");
        Enumeration<TreeNode> children = node.children();
        File currentDir = new File(parentDir, node.getName());
        currentDir.mkdirs();

        while(children.hasMoreElements())
        {
            TreeNode child = children.nextElement();

            if(child instanceof NavTreeFolder)
            {
                handleFolder((NavTreeFolder)child, currentDir);
            }
            else if(child instanceof NavTreeFile)
            {
                handleFile((NavTreeFile)child, currentDir);
            }
        }
    }

    /**
     * Handles any file encountered
     * @param node The node representing a file
     * @param parentDir the parent directory of the file
     * @throws IOException when an I/O error occurs
     */
    private static void handleFile(NavTreeFile node, File parentDir) throws IOException
    {
        Log.f2("Handling file " + node.toString());
        File fileDir = new File(parentDir, node.toString());
        ArrayList<String> lines = Main.texts.get(node);
        String wholeText = String.join(System.lineSeparator(), lines.toArray(new String[0]));

        if(!fileDir.exists())
            fileDir.createNewFile();

        Files.write(fileDir.toPath(), wholeText.getBytes());
    }

    /**
     * Clears the difference between the currently opened file
     * and the text mapped to the node representing the file
     */
    public static void cleanDirtyFile()
    {
        Log.f1("Cleaning the currently opened file...");
        JTextArea area = Main.editArea;
        AbstractNavTreeNode dirty = (AbstractNavTreeNode)Main.navTree.getLastSelectedPathComponent();

        //Will return true if the node represents a file
        if(dirty != null && dirty.getType().isLeaf())
        {
            ArrayList<String> savedText = Main.texts.get(dirty);
            ArrayList<String> actualText = new ArrayList<>(Arrays.asList(area.getText().split("\n")));

            if(!savedText.equals(actualText))
            {
                Log.f2("Opened file is dirty! GET CLEANED!!!");
                savedText.clear();
                savedText.addAll(actualText);
            }

            Main.texts.put((NavTreeFile)dirty, savedText);
        }
    }
}
