package a.star;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

class MyPanel extends JPanel implements ActionListener {
    int x;
    int y;
    int pos;
    public MyPanel(LayoutManager layout) {
        super(layout);
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
        g.fillRect(k, k, AStar.GRID_WIDTH * AStar.NODE_SPACE, AStar.GRID_HEIGHT * AStar.NODE_SPACE);
        g.setColor(new Color(100, 100, 100, 150));
        if (AStar.finished) {
            ArrayList<Node> path = new ArrayList<Node>();
            g.setColor(Color.WHITE);
            for(int y = 0; y < AStar.nodes.length; y++) {
                for(int x = 0; x < AStar.nodes[0].length; x++) {
                    Node n = AStar.nodes[y][x];
                    if(!n.walkable) {
                        g.fillRect((int)n.x + k, (int)n.y + k, AStar.NODE_SPACE, AStar.NODE_SPACE);
                    }
                }
            }
            g.setColor(Color.BLACK);
            Node n = AStar.end;
            g.fillRect((int) n.x + k, (int) n.y + k, AStar.NODE_SPACE, AStar.NODE_SPACE);
            path.add(n);
            while (AStar.cameFrom.containsKey(n)) {
                n = AStar.cameFrom.get(n);
                g.fillRect((int) n.x + k, (int) n.y + k, AStar.NODE_SPACE, AStar.NODE_SPACE);
                path.add(n);
            }
            if(pos == -1) {
                pos = path.size()-1;
            }
            
            double speed = 5;
            g.setColor(new Color(150, 255, 150));
            g.fillOval(x + k, y + k, AStar.NODE_SPACE, AStar.NODE_SPACE);
            if(pos > 0) {
                Node current = path.get(pos);
                Node next = path.get(pos-1);
                
                double xv = (next.x - current.x);
                double yv = (next.y - current.y);
                double mag = Math.sqrt(Math.pow(xv, 2) + Math.pow(yv, 2));
                xv *= speed/mag;
                yv *= speed/mag;
                x += xv;
                y += yv;
                
                if(x >= next.x && y >= next.y) {
                    pos--;
                }
            }
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font("TimesRoman", Font.BOLD, 30)); 
        g.drawString(Long.toString(AStar.t1-AStar.t0), 200, 30);
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}

class NodeComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        if (n1.fScore < n2.fScore) {
            return -1;
        }
        if (n1.fScore > n2.fScore) {
            return 1;
        }
        return 0;
    }
}

class Node {
    public boolean walkable;
    public double x, y, gScore, fScore;

    public Node() {
        walkable = true;
        gScore = 999999999.99;
    }
    public Node(boolean b) {
        this();
        walkable = b;
    }

    public Rectangle toRect() {
        return new Rectangle((int) x, (int) y, AStar.NODE_SPACE, AStar.NODE_SPACE);
    }

    public boolean isCloseTo(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    @Override
    public boolean equals(Object o) {
        Node n = (Node) o;
        return isCloseTo(n.x, x) && isCloseTo(n.y, y);
    }
}

public class AStar {
    public static boolean finished = false;
    public static final int GRID_WIDTH = 200, GRID_HEIGHT = 200;
    private static MyPanel panel;
    private static JFrame frame;
    public static Node nodes[][] = new Node[GRID_HEIGHT][GRID_WIDTH];
    public static Node end;
    public static HashMap<Node, Node> cameFrom;
    public static final int NODE_SPACE = 4;
    public static long t0, t1;
    private static void setupGUI() {
        panel = new MyPanel(new FlowLayout());
        frame = new JFrame();
        frame.add(panel);
        frame.setSize(856, 878);// 56, 78
        frame.setResizable(false);
        frame.setTitle("A* Pathfinder by Ethan Gordon");
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

    private static double dist(Node n1, Node n2) {
        return Math.sqrt(Math.pow(n2.x - n1.x, 2.0) + Math.pow(n2.y - n1.y, 2.0));
    }

    private static boolean isCol(Rectangle a, Rectangle b) {
        return !(a.x + a.width <= b.x || a.x >= b.x + b.width || a.y + a.height <= b.y || a.y >= b.y + b.height);
    }
    public static void main(String[] args) {
        setupGUI();
        t0 = System.currentTimeMillis();
        t1 = Long.MAX_VALUE;
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[i].length; j++) {
                nodes[i][j] = new Node();
                nodes[i][j].x = j * NODE_SPACE;
                nodes[i][j].y = i * NODE_SPACE;
                
                if((i == 150 && j > 5 && j <= 150) || (j == 150 && i >= 5 && i <= 150)) nodes[i][j].walkable = false; // ~ 105ms
                /*
                if(j == 40 && i <= 100) nodes[i][j].walkable = false;
                if(i == 130 && j <= 100) nodes[i][j].walkable = false;
                if(j == 100 && i > 5 && i <= 130) nodes[i][j].walkable = false;
                if((i == 150 && j > 5 && j <= 150) || (j == 150 && i >= 0 && i <= 150)) nodes[i][j].walkable = false; // ~ 75ms
*/
            }
        }
        end = nodes[nodes.length - 1][nodes[0].length - 1];
        nodes[0][0].gScore = 0;
        nodes[0][0].fScore = dist(nodes[0][0], end);
        Comparator<Node> comparator = new NodeComparator();
        PriorityQueue<Node> openSet = new PriorityQueue<Node>(10, comparator);
        HashMap<Node, Object> closedSet = new HashMap<Node, Object>();
        openSet.add(nodes[0][0]);
        cameFrom = new HashMap<Node, Node>();
        ArrayList<Node> neighbors;
        Node leftNode = null, topLeftNode = null, topNode = null, topRightNode = null, rightNode = null, bottomRightNode = null, bottomNode = null, bottomLeftNode = null, prevNode = null;
        boolean hasLeft, hasTop, hasRight, hasBottom;
        int centerX, centerY, left, top, right, bottom;
        t0 = System.currentTimeMillis();
        t1 = Long.MAX_VALUE;
        while (openSet.size() > 0) {
            Node current = openSet.remove();
            if(cameFrom.containsKey(current)) prevNode = cameFrom.get(current);
            closedSet.put(current, null);
            if (current == end) {
                t1 = System.currentTimeMillis();
                System.out.println("success");
                finished = true;
                return;
            }
            neighbors = new ArrayList<Node>();
            centerX = (int)current.x / NODE_SPACE;
            centerY = (int)current.y / NODE_SPACE;
            left = centerX - 1;
            top = centerY - 1;
            right = centerX + 1;
            bottom = centerY + 1;
            hasLeft = left >= 0;
            hasTop = top >= 0;
            hasRight = right < nodes[0].length;
            hasBottom = bottom < nodes.length;
            if(hasLeft) {
                leftNode = nodes[centerY][left];
                if(leftNode.walkable) neighbors.add(leftNode);
            }
            if(hasLeft && hasTop) {
                topLeftNode = nodes[top][left];
                if(topLeftNode.walkable) neighbors.add(topLeftNode);
            }
            if(hasTop) {
                topNode = nodes[top][centerX];
                if(topNode.walkable) neighbors.add(topNode);
            }
            if(hasTop && hasRight) {
                topRightNode = nodes[top][right];
                if(topRightNode.walkable) neighbors.add(topRightNode);
            }
            if(hasRight) {
                rightNode = nodes[centerY][right];
                if(rightNode.walkable) neighbors.add(rightNode);
            }
            if(hasRight && hasBottom) {
                bottomRightNode = nodes[bottom][right];
                if(bottomRightNode.walkable) neighbors.add(bottomRightNode);
            }
            if(hasBottom) {
                bottomNode = nodes[bottom][centerX];
                if(bottomNode.walkable) neighbors.add(bottomNode);
            }
            if(hasBottom && hasLeft) {
                bottomLeftNode = nodes[bottom][left];
                if(bottomLeftNode.walkable) neighbors.add(bottomLeftNode);
            }
            for (int i = 0; i < neighbors.size(); i++) {
                Node n = neighbors.get(i);
                if(closedSet.containsKey(n)) continue;
                double tentative_gScore = current.gScore + dist(current, n);
                //not a better path
                if (tentative_gScore >= n.gScore) continue;
                //it's good. save it
                n.gScore = tentative_gScore;
                n.fScore = n.gScore + dist(n, end);
                openSet.add(n);
                cameFrom.put(n, current);
            }
        }
        System.out.println("failure");
        finished = true;
    }
}
