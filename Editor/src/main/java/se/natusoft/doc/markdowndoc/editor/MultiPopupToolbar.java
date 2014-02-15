package se.natusoft.doc.markdowndoc.editor;

import se.natusoft.doc.markdowndoc.editor.api.Editor;
import se.natusoft.doc.markdowndoc.editor.api.EditorFunction;
import se.natusoft.doc.markdowndoc.editor.api.ToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.List;

/**
 * A toolbar that will open a java.awt.Window for each toolbar button. It will open
 * when the mouse is at the top of the editor window and close if it leaves that area.
 */
public class MultiPopupToolbar implements ToolBar {

    //
    // Private Members
    //

    private Editor editor;

    private List<String> toolBarGroups = new LinkedList<>();

    private Map<String, List<EditorFunction>> functions = new HashMap<>();

    private List<JWindow> buttonWindows = new LinkedList<>();

    private boolean open = false;

    private int calculatedWidth = 0;

    private MouseMotionListener mouseMotionListener = null;

    //
    // Constructors
    //

    /**
     * Creates a new MultiPopupToolbar.
     */
    public MultiPopupToolbar() {}

    //
    // Methods
    //

    /**
     * Provides the toolbar with the editor it is associated.
     *
     * @param editor The associated editor provided.
     */
    public void attach(Editor editor) {
        this.editor = editor;

        this.mouseMotionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e) {
                int y = e.getYOnScreen();
                y = y - getParentFrame().getY() - 25;
                if (y <= getTopMargin() && e.getX() >= 0 && e.getX() <= getEditorWidth()) {
                    if (!isOpen()) {
                        int toolbarWidth = calculateWidth();
                        int x = getParentFrame().getX() + (getParentFrame().getWidth() / 2) - (toolbarWidth / 2);
                        open(getParentFrame(), x, getParentFrame().getY() + 25);
                    }
                }
                else {
                    if (isOpen()) {
                        close();
                    }
                }
            }
        };
        this.editor.addMouseMotionListener(this.mouseMotionListener);
    }

    /**
     * Removes the association with the editor.
     *
     * @param editor The editor to detach from.
     */
    @Override
    public void detach(Editor editor) {
        close();
        editor.removeMouseMotionListener(this.mouseMotionListener);
        this.editor = null;
        this.buttonWindows.clear();
    }


    /**
     * Convenience method to get information from associated editor.
     */
    private JFrame getParentFrame() {
        return this.editor.getGUI().getWindowFrame();
    }

    /**
     * Convenience method to get information from associated editor.
     */
    private int getTopMargin() {
        return this.editor.getTopMargin();
    }

    /**
     * Convenience method to get information from associated editor.
     */
    private int getEditorWidth() {
        return this.editor.getWidth();
    }

    /**
     * Opens the toolbar.
     *
     * @param parent The parent window.
     * @param x The X coordinate to open at.
     * @param y The Y coordinate to open at.
     */
    private void open(JFrame parent, int x, int y) {

        boolean create = this.buttonWindows.isEmpty();

        int width = 0;
        Iterator<JWindow> buttonIterator = this.buttonWindows.iterator();
        for (String group : this.toolBarGroups) {
            for (EditorFunction editorFunction : this.functions.get(group)) {
                JWindow buttonWindow;
                if (create) {
                    buttonWindow = new JWindow(parent);
                    buttonWindow.setLayout(new BorderLayout());
                    JComponent toolbarButton = editorFunction.getToolBarButton();
                    //toolbarButton.setSize(80, 80);
                    buttonWindow.add(toolbarButton, BorderLayout.CENTER);
                    this.buttonWindows.add(buttonWindow);
                }
                else {
                    buttonWindow = buttonIterator.next();
                }
                buttonWindow.setSize(buttonWindow.getPreferredSize());
                buttonWindow.setLocation(x, y);
                buttonWindow.setVisible(true);
                x += buttonWindow.getSize().width;
                width += buttonWindow.getSize().width;
            }
            x += 10;
            width += 10;
        }
        if (this.calculatedWidth == 0) {
            this.calculatedWidth = width;
        }

        this.open = true;
    }

    /**
     * Closes the toolbar.
     */
    private void close() {
        for (JWindow buttonWindow : this.buttonWindows) {
            buttonWindow.setVisible(false);
        }

        this.open = false;
    }

    /**
     * Calculates an estimated width until a real width is provided. When a real width is available it is returned instead.
     */
    private int calculateWidth() {
        int width = this.calculatedWidth;
        if (width == 0) {
            for (String group : this.toolBarGroups) {
                for (EditorFunction editorFunction : this.functions.get(group)) {
                    width = width + editorFunction.getToolBarButton().getPreferredSize().width;
                }
                width = width + 10;
            }
        }

        return width;
    }

    /**
     * @return true if toolbar is open.
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Adds a function to the toolbar.
     *
     * @param function The function to add.
     */
    public void addFunction(EditorFunction function) {
        if (!this.toolBarGroups.contains(function.getGroup())) {
            this.toolBarGroups.add(function.getGroup());
        }

        List<EditorFunction> groupFunctions = this.functions.get(function.getGroup());
        if (groupFunctions == null) {
            groupFunctions = new LinkedList<EditorFunction>();
            this.functions.put(function.getGroup(), groupFunctions);
        }

        groupFunctions.add(function);
    }

    /**
     * Creates the content of the toolbar. This cannot be done until all
     * functions have been provided.
     */
    @Override
    public void createToolBarContent() {
        // This is done by open() and thus nothing needs to be done here.
    }

    /**
     * Enables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to enable.
     */
    public void disableGroup(String group) {
        List<EditorFunction> functions = this.functions.get(group);
        if (functions != null) {
            for (EditorFunction function : functions) {
                function.getToolBarButton().setEnabled(false);
            }
        }
        else {
            throw new RuntimeException("Cannot disable non existent group '" + group + "'!");
        }
    }

    /**
     * Disables all buttons in the specified tool bar group.
     *
     * @param group The tool bar group to disable.
     */
    public void enableGroup(String group) {
        List<EditorFunction> functions = this.functions.get(group);
        if (functions != null) {
            for (EditorFunction function : functions) {
                function.getToolBarButton().setEnabled(true);
            }
        }
        else {
            throw new RuntimeException("Cannot enable non existent group '" + group + "'!");
        }
    }

}