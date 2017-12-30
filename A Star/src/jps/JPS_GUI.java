package jps;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

class MyPanel extends JPanel implements ActionListener {
    private int x, y, pos;
    private JPS jps;
    private int gridWidth, gridHeight, nodeSpace;
    public ArrayList<Node> path;
    public MyPanel(LayoutManager layout, JPS jps) {
        super(layout);
        this.jps = jps;
        gridWidth = jps.getGridWidth();
        gridHeight = jps.getGridHeight();
        nodeSpace = jps.getNodeSpace();
        setFocusable(true);
        setBackground(Color.WHITE);
        setDoubleBuffered(true);

        Timer timer = new Timer(20, this);
        timer.start();
        
        x = 0;
        y = 0;
        pos = -1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final int k = 25;
        g.setColor(new Color(160, 160, 160));
        g.fillRect(k, k, gridWidth * nodeSpace, gridHeight * nodeSpace);
        g.setColor(new Color(100, 100, 100, 150));
        Node nodes[][] = jps.getNodes();
        g.setColor(Color.WHITE);
        for(int y = 0; y < nodes.length; y++) {
            for(int x = 0; x < nodes[0].length; x++) {
                Node n = nodes[y][x];
                if(!n.walkable) {
                    g.fillRect((int)n.x + k, (int)n.y + k, nodeSpace, nodeSpace);
                }
            }
        }
        if(path != null) {
            g.setColor(Color.BLACK);
            for(int i = 0; i < path.size(); i++) {
                Node n = path.get(i);
                g.fillRect((int) n.x + k, (int) n.y + k, nodeSpace, nodeSpace);
            }
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("TimesRoman", Font.BOLD, 30)); 
        g.drawString(Long.toString(jps.getDt()), 200, 30);
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}

public class JPS_GUI {
    private static MyPanel panel;
    public static void setupGUI(ArrayList<Node> path, JPS jps) {
        panel = new MyPanel(new FlowLayout(), jps);
        panel.path = path;
        JFrame frame = new JFrame();
        frame.add(panel);
        frame.setSize(856, 878);// 56, 78
        frame.setResizable(false);
        frame.setTitle("Jump Point Search (improved A*) Pathfinder");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.requestFocus();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
    }
    public static void main(String args[]) {
        JPS jps = new JPS(200, 200, 4);
        Node nodes[][] = jps.getNodes();
        ArrayList<Node> path = jps.findPath(nodes[0][0], nodes[nodes.length-1][nodes[0].length - 1]);
        setupGUI(path, jps);
    }
}