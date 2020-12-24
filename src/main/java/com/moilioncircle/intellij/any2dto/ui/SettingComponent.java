package com.moilioncircle.intellij.any2dto.ui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.moilioncircle.intellij.any2dto.helper.IdeaUiHelper;

import javax.swing.*;

/**
 * @author trydofor
 * @since 2020-12-19
 */
public class SettingComponent {
    private JTabbedPane tnlTabRoot;
    private JPanel pnlTypeMapping;
    private JPanel pnlTmplInner;
    private JPanel pnlTmplOuter;
    private JPanel pnlConfig;
    private JLabel lblSourcePath;
    private JLabel lblPackageName;
    private JLabel lblWhereSave;
    private JLabel lblWhichType;
    private JLabel lblDtoName;
    private JLabel lblTextLineSep;
    private JLabel lblWordSeparator;
    public JPanel pnlRoot;
    public JTextField txtSourcePath;
    public JTextField txtPackageName;
    public JRadioButton rbtClipboard;
    public JRadioButton rbtSourcePath;
    public JRadioButton rbtInnerClass;
    public JRadioButton rbtOuterFile;
    public JButton btnLoadDefault;
    public JButton btnPluginHome;
    public JButton btnMeepoHelp;
    public JTextField txtDtoName;
    public JTextField txtTextLineSep;
    public JTextField txtTextWordSep;
    public JCheckBox ckbDtoPromote;
    public JCheckBox ckbLinePromote;
    public EditorTextField edtTypeMapping;
    public EditorTextField edtTmplInner;
    public EditorTextField edtTmplOuter;

    private final String typeMapping;
    private final String innerTmpl;
    private final String outerTmpl;
    private final Project project;

    public SettingComponent(Project project, String typeMapping, String innerTmpl, String outerTmpl) {
        this.project = project;
        this.typeMapping = typeMapping;
        this.innerTmpl = innerTmpl;
        this.outerTmpl = outerTmpl;
    }

    private void createUIComponents() {
        this.edtTypeMapping = IdeaUiHelper.createMappingEditor(typeMapping, project);
        this.edtTmplInner = IdeaUiHelper.createJavaEditor(innerTmpl, project);
        this.edtTmplOuter = IdeaUiHelper.createJavaEditor(outerTmpl, project);
    }
}
