package cool.nardomc;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class PlayerSearchApp extends JFrame {
    private JTextField idTextField;
    private JTextArea resultTextArea;
    private JPanel labelsPanel; // 使用一个面板包含Name和UUID
    private JLabel nameLabel;
    private JLabel uuidLabel;
    private JButton copyButton; // 复制按钮正方形，边长等于两行标签的长度
    private boolean state = false;
    private String resultName = null;
    private String resultUUID = null;

    public PlayerSearchApp() {
        setTitle("PlayerSearch");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // 设置主面板的背景颜色为深蓝色
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(32, 33, 35)); // #202123

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setOpaque(false);

        idTextField = new JTextField();
        JButton searchButton = new JButton("查询");
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        labelsPanel = new JPanel(new GridLayout(2, 1)); // 使用一个面板包含Name和UUID
        nameLabel = new JLabel("Name");
        uuidLabel = new JLabel("UUID");

        // 设置输入玩家用户名的输入框背景颜色为 #40414E
        idTextField.setBackground(new Color(64, 65, 78)); // #40414E
        idTextField.setForeground(Color.WHITE);

        // 设置按钮的背景颜色为 #444653
        searchButton.setBackground(new Color(68, 70, 83)); // #444653
        searchButton.setForeground(Color.BLACK);
        searchButton.setFocusPainted(false);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerId = idTextField.getText();
                if (!playerId.isEmpty()) {
                    String playerInfo = getPlayerInfo(playerId);
                    updateLabels(playerInfo);
                    resultTextArea.setText(playerInfo);
                } else {
                    resultTextArea.setText("请输入玩家ID");
                }
            }
        });

        topPanel.add(idTextField, BorderLayout.CENTER);
        topPanel.add(searchButton, BorderLayout.EAST);

        // 设置日志输出的背景颜色为深灰色
        resultTextArea.setBackground(new Color(42, 43, 49)); // #2A2B31
        resultTextArea.setForeground(Color.WHITE); // 设置日志文字颜色为白色

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);

        // 设置底部面板的背景颜色为深蓝色
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(32, 33, 35)); // #202123

        labelsPanel.setOpaque(false);
        nameLabel.setForeground(Color.WHITE);
        uuidLabel.setForeground(Color.WHITE);

        labelsPanel.add(nameLabel);
        labelsPanel.add(uuidLabel);

        // 添加复制按钮，正方形，边长等于两行标签的长度
        copyButton = new JButton("复制");
        int buttonSize = labelsPanel.getPreferredSize().height;
        copyButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        copyButton.setBackground(new Color(68, 70, 83)); // #444653
        copyButton.setForeground(Color.BLACK);
        copyButton.setFocusPainted(false);
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 复制变量 resultName 和 resultUUID 的值
                String copyText = "Name: " + resultName + "\nUUID: " + resultUUID;
                StringSelection stringSelection = new StringSelection(copyText);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                // 弹出提示框
                JOptionPane.showMessageDialog(PlayerSearchApp.this, "复制成功", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setOpaque(false);

        // 添加复制按钮到底部面板
        buttonsPanel.add(copyButton);

        bottomPanel.add(labelsPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(68, 70, 83)); // #444653

        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String getPlayerInfo(String playerId) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json = new JSONObject(response.toString());
                String name = json.getString("name");
                String id = json.getString("id");

                state = true;
                resultName = name;
                resultUUID = id;
                return "获取成功\nName：" + name + "\nUUID：" + id; // 更新标签文本

            } else if (responseCode >= 400 && responseCode < 500) {
                state = false;
                resultName = null;
                resultUUID = null;
                return "获取失败，状态码：" + responseCode;
            } else {
                state = false;
                resultName = null;
                resultUUID = null;
                return "发生异常，状态码：" + responseCode;
            }
        } catch (IOException e) {
            state = false;
            resultName = null;
            resultUUID = null;
            return "发生异常：" + e.getMessage();
        }
    }

    private void updateLabels(String playerInfo) {
        if (playerInfo.contains("Name：") && playerInfo.contains("UUID：")) {
            int nameStart = playerInfo.indexOf("Name：") + 5;
            int nameEnd = playerInfo.indexOf("\n", nameStart);
            int uuidStart = playerInfo.indexOf("UUID：") + 5;
            String uuid = playerInfo.substring(uuidStart);

            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            nameLabel.setForeground(Color.WHITE);
            uuidLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            uuidLabel.setForeground(Color.WHITE);

            nameLabel.setText("Name: " + resultName);
            uuidLabel.setText("UUID: " + resultUUID);
        } else {
            nameLabel.setText("Name");
            uuidLabel.setText("UUID");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PlayerSearchApp app = new PlayerSearchApp();
                app.setVisible(true);
            }
        });
    }
}
