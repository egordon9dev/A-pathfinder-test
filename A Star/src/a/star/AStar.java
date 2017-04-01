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
        setBackground(new Color(220, 190, 190));
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
        g.setColor(new Color(190, 160, 160));
        g.fillRect(k, k, AStar.GRID_WIDTH * AStar.NODE_SPACE, AStar.GRID_HEIGHT * AStar.NODE_SPACE);
        g.setColor(new Color(100, 100, 100, 150));
        for (Rectangle o : AStar.obstacles) {
            g.fillRect(o.x + k, o.y + k, o.width, o.height);
        }
        if (AStar.finished) {
            ArrayList<Node> path = new ArrayList<Node>();
            g.setColor(new Color(255, 100, 0, 150));
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
    public double x;
    public double y;
    public double gScore;
    public double fScore;

    public Node() {
        walkable = true;
        gScore = 9999999;
        fScore = 9999999;
    }
    public Node(boolean b) {
        this();
        walkable = b;
    }

    public Rectangle toRect() {
        return new Rectangle((int) x, (int) y, AStar.NODE_SPACE, AStar.NODE_SPACE);
    }

    public boolean isCloseTo(double a, double b) {
        return Math.abs(a - b) < 0.000001;
    }

    @Override
    public boolean equals(Object o) {
        Node n = (Node) o;
        return isCloseTo(n.x, x) && isCloseTo(n.y, y);
    }
}

public class AStar {

    public static boolean finished = false;
    public static final int GRID_WIDTH = 50;
    public static final int GRID_HEIGHT = 50;
    private static MyPanel panel;
    private static JFrame frame;
    private static Node nodes[][] = new Node[GRID_HEIGHT][GRID_WIDTH];
    public static Node end;
    public static HashMap<Node, Node> cameFrom;
    public static final int NODE_SPACE = 10;
    public static ArrayList<Rectangle> obstacles;

    private static void setupGUI() {
        panel = new MyPanel(new FlowLayout());
        frame = new JFrame();
        frame.add(panel);
        frame.setSize(556, 578);
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
        //return Math.abs(n2.x- n1.x) + Math.abs(n2.y - n1.y);
    }

    private static boolean isCol(Rectangle a, Rectangle b) {
        if (a.x + a.width <= b.x || a.x >= b.x + b.width
                || a.y + a.height <= b.y || a.y >= b.y + b.height) {
            return false;
        }
        return true;
    }
    private static Rectangle generateRandRect(int min, int max) {
        int sw = GRID_WIDTH*NODE_SPACE;
        int sh = GRID_HEIGHT*NODE_SPACE;
        int x = (int)(Math.random()*(sw-min-(2*NODE_SPACE)))+NODE_SPACE;
        int y = (int)(Math.random()*(sh-min-(2*NODE_SPACE)))+NODE_SPACE;
        int w = (int)(Math.random()*(sw-x-NODE_SPACE));
        int h = (int)(Math.random()*(sh-y-NODE_SPACE));
        if(w > max) w = max;
        if(h > max) h = max;
        return new Rectangle(x, y, w, h);
    }
    public static void main(String[] args) {
        obstacles = new ArrayList<Rectangle>();
        obstacles.add(new Rectangle(50, 50, 30, 30));
        for(int i = 0; i < 100; i++) {
            obstacles.add(generateRandRect(5, 10));
        }
        setupGUI();
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[i].length; j++) {
                nodes[i][j] = new Node();
                nodes[i][j].x = j * NODE_SPACE;
                nodes[i][j].y = i * NODE_SPACE;
            }
        }
        end = nodes[nodes.length - 1][nodes[0].length - 1];
        nodes[0][0].gScore = 0;
        nodes[0][0].fScore = dist(nodes[0][0], end);
        Comparator<Node> comparator = new NodeComparator();
        PriorityQueue<Node> openSet = new PriorityQueue<Node>(10, comparator);
        openSet.add(nodes[0][0]);
        cameFrom = new HashMap<Node, Node>();
        int loop_ctr = 0;
        while (openSet.size() > 0) {

            Node current = openSet.remove();
            if (current == end) {
                System.out.println("success");
                finished = true;
                return;
            }
            ArrayList<Node> neighbors = new ArrayList<Node>();
            Node leftNode = new Node(false);
            Node topLeftNode = new Node(false);
            Node topNode = new Node(false);
            Node topRightNode = new Node(false);
            Node rightNode = new Node(false);
            Node bottomRightNode = new Node(false);
            Node bottomNode = new Node(false);
            Node bottomLeftNode = new Node(false);
            boolean hasLeft = current.x >= NODE_SPACE;
            boolean hasTop = current.y >= NODE_SPACE;
            boolean hasRight = current.x <= (nodes[0].length - 2) * NODE_SPACE;
            boolean hasBottom = current.y <= (nodes.length - 2) * NODE_SPACE;
            int centerX = (int)current.x / NODE_SPACE;
            int centerY = (int)current.y / NODE_SPACE;
            int left = centerX - 1;
            int top = centerY - 1;
            int right = centerX + 1;
            int bottom = centerY + 1;

            if(hasLeft) {
                leftNode = nodes[centerY][left];
                neighbors.add(leftNode);
            }
            if(hasLeft && hasTop) {
                topLeftNode = nodes[top][left];
                neighbors.add(topLeftNode);
            }
            if(hasTop) {
                topNode = nodes[top][centerX];
                neighbors.add(topNode);
            }
            if(hasTop && hasRight) {
                topRightNode = nodes[top][right];
                neighbors.add(topRightNode);
            }
            if(hasRight) {
                rightNode = nodes[centerY][right];
                neighbors.add(rightNode);
            }
            if(hasRight && hasBottom) {
                bottomRightNode = nodes[bottom][right];
                neighbors.add(bottomRightNode);
            }
            if(hasBottom) {
                bottomNode = nodes[bottom][centerX];
                neighbors.add(bottomNode);
            }
            if(hasBottom && hasLeft) {
                bottomLeftNode = nodes[bottom][left];
                neighbors.add(bottomLeftNode);
            }

            for (Node n : neighbors) {
                for (Rectangle o : obstacles) {
                    if (isCol(o, n.toRect())) {
                        n.walkable = false;
                    }
                }
            }
            if(!leftNode.walkable) {
                topLeftNode.walkable = false;
                bottomLeftNode.walkable = false;
            }
            if(!topNode.walkable) {
                topLeftNode.walkable = false;
                topRightNode.walkable = false;
            }
            if(!rightNode.walkable) {
                topRightNode.walkable = false;
                bottomRightNode.walkable = false;
            }
            if(!bottomNode.walkable) {
                bottomLeftNode.walkable = false;
                bottomRightNode.walkable = false;
            }

            //System.out.println("loop " + loop_ctr);
            //loop_ctr++;
            for (int i = 0; i < neighbors.size(); i++) {
                Node n = neighbors.get(i);
                if (!n.walkable) {
                    n.walkable = true;
                    continue;
                }
                double tentative_gScore = current.gScore + dist(current, n);
                //not a better path
                if (tentative_gScore >= n.gScore) {
                    continue;
                }
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
