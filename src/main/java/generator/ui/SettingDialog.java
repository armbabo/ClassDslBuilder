/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package generator.ui;



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import generator.Config;

/**
 * Created by weilh on 2018/1/25.
 */
public class SettingDialog extends JFrame {
    private JCheckBox finalCheckBox;
    private JCheckBox overlayCheckBox;
    private JCheckBox getterCheckBox;
    private JButton oKButton;
    private JPanel container;
    private OnClickListner onClickListner;

    public interface OnClickListner {
        void onClick(Config config);
    }

    public void setOnClickListner(OnClickListner clickListner) {
        this.onClickListner = clickListner;
    }

    public SettingDialog() {
        super();
        setContentPane(container);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        oKButton.setEnabled(true);
        oKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onClickListner != null) {
                    setVisible(false);
                    dispose();
                    onClickListner.onClick(getConfig());
                }
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    public Config getConfig() {
        Config config = new Config();
        config.setCreateGetter(getterCheckBox.isSelected());
        config.setOverlay(overlayCheckBox.isSelected());
        config.setForceChangeToFinal(finalCheckBox.isSelected());
        return config;
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
