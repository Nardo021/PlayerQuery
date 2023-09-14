package cool.nardomc;

import javax.swing.*;
import java.awt.*;
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
    private JLabel nameLabel;
    private JLabel uuidLabel;
    private boolean state = false;
    private String resultName = null;

    public PlayerSearchApp() {
        setTitle("PlayerSearch");
        setSize(600, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(1, 2));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(2, 1));

        idTextField = new JTextField();
        JButton searchButton = new JButton("查询");
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        nameLabel = new JLabel("Name: ");
        uuidLabel = new JLabel("UUID: ");

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

        leftPanel.add(idTextField, BorderLayout.NORTH);
        leftPanel.add(searchButton, BorderLayout.CENTER);

        rightPanel.add(nameLabel);
        rightPanel.add(uuidLabel);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        add(mainPanel, BorderLayout.CENTER);
        add(new JScrollPane(resultTextArea), BorderLayout.SOUTH);
    }

    private String getPlayerInfo(String playerId) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) { // 2开头表示成功
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // 使用 org.json 解析 JSON 数据
                JSONObject json = new JSONObject(response.toString());
                String name = json.getString("name");
                String id = json.getString("id");

                state = true;
                resultName = name;
                return "获取成功\n玩家名字：" + name + "\nUUID：" + id; // 注意这里的标签更改

            } else if (responseCode >= 400 && responseCode < 500) { // 4开头表示失败
                state = false;
                return "获取失败，状态码：" + responseCode;
            } else {
                state = false;
                return "发生异常，状态码：" + responseCode;
            }
        } catch (IOException e) {
            state = false;
            return "发生异常：" + e.getMessage();
        }
    }

    private void updateLabels(String playerInfo) {
        if (playerInfo.contains("玩家名字：") && playerInfo.contains("UUID：")) { // 更新匹配条件
            int nameStart = playerInfo.indexOf("玩家名字：") + 6;
            int nameEnd = playerInfo.indexOf("\n", nameStart);
            String name = playerInfo.substring(nameStart, nameEnd);

            int uuidStart = playerInfo.indexOf("UUID：") + 5; // 更新UUID标签
            String uuid = playerInfo.substring(uuidStart);

            nameLabel.setFont(new Font("Arial", Font.BOLD, 14)); // 设置字体为Arial，粗体，字号14
            uuidLabel.setFont(new Font("Arial", Font.BOLD, 14)); // 设置字体为Arial，粗体，字号14

            nameLabel.setText("Name: " + resultName);
            uuidLabel.setText("UUID: " + uuid);
        } else {
            nameLabel.setText("Name: ");
            uuidLabel.setText("UUID: ");
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
