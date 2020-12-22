package com.moilioncircle.intellij.any2dto.ui;

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
    public JPanel pnlRoot;
    public JTextArea txtTypeMapping;
    public JTextArea txtTmplInner;
    public JTextArea txtTmplOuter;
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
}
