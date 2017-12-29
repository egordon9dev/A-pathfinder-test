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
            while (AStar.parent.containsKey(n)) {
                n = AStar.parent.get(n);
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
    private boolean finished = false;
    public final int GRID_WIDTH = 200, GRID_HEIGHT = 200;
    private MyPanel panel;
    private JFrame frame;
    private Node nodes[][] = new Node[GRID_HEIGHT][GRID_WIDTH];
    
    private Node start, end;
    private HashMap<Node, Node> parent;
    public final int NODE_SPACE = 4;
    public long t0, t1;
    private void setupGUI() {
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

    private double dist(Node n1, Node n2) {
        return Math.sqrt(Math.pow(n2.x - n1.x, 2.0) + Math.pow(n2.y - n1.y, 2.0));
    }

    private boolean isCol(Rectangle a, Rectangle b) {
        return !(a.x + a.width <= b.x || a.x >= b.x + b.width || a.y + a.height <= b.y || a.y >= b.y + b.height);
    }
    private Node[] idSuccessors(Node cur) {
        Node successors[] = new Node[8];
        for(int i = 0; i < successors.length; i++) {
            successors[i] = null;
        }
        Node n00 = nodes[0][0];
        int cx = (int)(cur.x - n00.x) / NODE_SPACE, cy = (int)(cur.y - n00.y) / NODE_SPACE;
        if(parent.containsKey(cur)) {
            Node p = parent.get(cur);
            int dx = cx - (int)(p.x - n00.x)/NODE_SPACE, dy = cy - (int)(p.y - n00.y)/NODE_SPACE;
            if(dx > 1) dx = 1;
            if(dx < -1) dx = -1;
            if(dy > 1) dy = 1;
            if(dy < -1) dy = -1;
            Node neighbors[] = getNeighborsPrune(cx, cy, dx, dy);
            
            for(int i = 0; i < neighbors.length; i++) {
                Node jumpPt = jump(cx, cy, dx, dy);
                successors[i] = jumpPt;
            }
        } else {
            if(isWalk(cy, cx-1)) successors[0] = nodes[cy][cx-1];
            if(isWalk(cy-1, cx-1)) successors[1] = nodes[cy-1][cx-1];
            if(isWalk(cy-1, cx)) successors[2] = nodes[cy-1][cx];
            if(isWalk(cy-1, cx+1)) successors[3] = nodes[cy-1][cx+1];
            if(isWalk(cy, cx+1)) successors[4] = nodes[cy][cx+1];
            if(isWalk(cy+1, cx+1)) successors[5] = nodes[cy+1][cx+1];
            if(isWalk(cy+1, cx)) successors[6] = nodes[cy+1][cx];
            if(isWalk(cy+1, cx-1)) successors[7] = nodes[cy+1][cx-1];
        }
        return successors;
    }
    private boolean isWalk(int y, int x) {
        return y >= 0 && x >= 0 && y < nodes.length && x < nodes[0].length && nodes[y][x].walkable;
    }
    private Node jump(int cx, int cy, int dx, int dy) {
        int nx = cx + dx;
        int ny = cy + dy;
        Node next = nodes[ny][nx];
        if(!isWalk(ny, nx)) return null;
        if(next.equals(end)) return next;
        
        if(dx != 0 && dy != 0) { //diagonal
            //diagonal forced neighbor check
            if((!isWalk(ny, nx-dx) && isWalk(ny+dy, nx) && isWalk(ny+dy, nx-dx)) || (!isWalk(ny, nx+dx) && isWalk(ny+dy, nx) && isWalk(ny+dy, nx+dx))) {
                return next;
            }
            
            if(jump(nx, ny, dx, 0) != null || jump(nx, ny, 0, dy) != null) {
                return next;
            }
        } else {
            // vertical/horizontal forced neighbor check
            if( (!isWalk(ny, nx+1) && isWalk(ny+dy, nx+1)) || (!isWalk(ny, nx-1) && isWalk(ny+dy, nx-1)) || (!isWalk(ny+1, nx) && isWalk(ny+1, nx+dx)) || (!isWalk(ny-1, nx) && isWalk(ny-1, nx+dx))) {
                return next;
            }
        }
        return jump(nx, ny, dx, dy);
    }
    public Node[] getNeighborsPrune(int cx, int cy, int dx, int dy){
        Node[] neighbors = new Node[5];
        if (dx!=0 && dy!=0) { // moving diagonal
            // normal 3 neighbors
            if (isWalk(cy+dy, cx)) neighbors[0] = nodes[cy+dy][dx];
            if (isWalk(cy, cx+dx)) neighbors[1] = nodes[cy][cx+dx];
            if ((isWalk(cy+dy, cx) || isWalk(cy, cx+dx)) && isWalk(cy+dy, cx+dx)) neighbors[2] = nodes[cy+dy][cx+dx];
            // 2 forced neighbors
            if (!isWalk(cy, cx-dx) && isWalk(cy+dy, cx) && isWalk(cy+dy, cx-dx)) neighbors[3] = nodes[cy+dy][cx-dx];
            if (!isWalk(cy-dy, cx) && isWalk(cy, cx+dx) && isWalk(cy-dy, cx+dx)) neighbors[4] = nodes[cy-dy][cx+dx];
        } else {
            if (dx == 0){ // moving vertical
                if (isWalk(cy+dy, cx)){
                    // normal 1 neighbor
                    neighbors[0] = nodes[cy+dy][cx];
                    // 2 forced neighbors
                    if (!isWalk(cy, cx+1) && isWalk(cy+dy, cx+1)){
                        neighbors[1] = nodes[cy+dy][dx+1];
                    }
                    if (!isWalk(cy, cx-1) && isWalk(cy+dy, cx-1)){
                        neighbors[2] = nodes[cy+dy][cx-1];
                    }
                }
            } else { // moving horizontal
                if (isWalk(cy, cx+dx)){
                    // normal 1 neighbor
                    neighbors[0] = nodes[cy][cx+dx];
                    // 2 forced neighbors
                    if (!isWalk(cy+1, cx) && isWalk(cy+1, cx+dx)){
                        neighbors[1] = nodes[cy+1][cx+dx];
                    }
                    if (!isWalk(cy-1, cx) && isWalk(cy-1, cx+dx)){
                        neighbors[2] = nodes[cy-1][cx+dx];
                    }
                }
            }
        }
        return neighbors;
    }
    private ArrayList<Node> reconstructPath() {
        ArrayList<Node> path = new ArrayList<Node>();
        Node n = end;
        path.add(n);
        while(parent.containsKey(n)) {
            n = parent.get(n);
            path.add(n);
        }
        return path;
    }
    public ArrayList<Node> findPath() {
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
        start = nodes[0][0];
        end = nodes[nodes.length - 1][nodes[0].length - 1];
        start.gScore = 0;
        start.fScore = dist(start, end);
        Comparator<Node> comparator = new NodeComparator();
        PriorityQueue<Node> openSet = new PriorityQueue<Node>(10, comparator);
        HashMap<Node, Object> closedSet = new HashMap<Node, Object>();
        openSet.add(start);
        parent = new HashMap<Node, Node>();
        Node successors[];
        t0 = System.currentTimeMillis();
        t1 = Long.MAX_VALUE;
        while (openSet.size() > 0) {
            Node current = openSet.remove();
            if (current == end) {
                t1 = System.currentTimeMillis();
                System.out.println("success");
                finished = true;
                return reconstructPath();
            }
            closedSet.put(current, null);
            successors = idSuccessors(current);
            for (int i = 0; i < successors.length; i++) {
                Node n = successors[i];
                if(closedSet.containsKey(n)) continue;
                double tentative_gScore = current.gScore + dist(current, n);
                //not a better path
                if (tentative_gScore >= n.gScore) continue;
                
                //it's good. save it
                n.gScore = tentative_gScore;
                n.fScore = n.gScore + dist(n, end);
                openSet.add(n);
                parent.put(n, current);
            }
        }
        System.out.println("failure");
        finished = true;
    }
    public static void main() {
        AStar aStar = new AStar();
        aStar.findPath();
    }
}