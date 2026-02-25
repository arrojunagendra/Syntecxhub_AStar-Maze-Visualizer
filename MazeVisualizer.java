import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

class Cell {
    int row, col;
    boolean wall = false;
    boolean visited = false;
    Cell parent = null;

    Cell(int r, int c) {
        row = r; col = c;
    }
}

public class MazeVisualizer extends JPanel {

    static final int ROWS = 20;
    static final int COLS = 20;
    static final int SIZE = 30;

    Cell[][] grid = new Cell[ROWS][COLS];

    MazeVisualizer() {
        setPreferredSize(new Dimension(COLS * SIZE, ROWS * SIZE));
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int r = e.getY() / SIZE;
                int c = e.getX() / SIZE;
                if (r >= 0 && c >= 0 && r < ROWS && c < COLS) {
                    grid[r][c].wall = !grid[r][c].wall;
                    repaint();
                }
            }
        });
        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                grid[i][j] = new Cell(i, j);
    }

    void startAStar() {
        new Thread(() -> runAStar()).start();
    }

    void runAStar() {
        PriorityQueue<Cell> open = new PriorityQueue<>(Comparator.comparingInt(c -> c.row + c.col));
        boolean[][] closed = new boolean[ROWS][COLS];

        Cell start = grid[0][0];
        Cell goal = grid[ROWS-1][COLS-1];

        open.add(start);

        while (!open.isEmpty()) {
            Cell current = open.poll();

            if (current == goal) {
                drawPath(current);
                return;
            }

            closed[current.row][current.col] = true;
            current.visited = true;
            repaint();

            try { Thread.sleep(50); } catch(Exception ex){}

            int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};

            for (int[] d : dirs) {
                int nr = current.row + d[0];
                int nc = current.col + d[1];
                if (nr>=0 && nc>=0 && nr<ROWS && nc<COLS) {
                    Cell neighbor = grid[nr][nc];
                    if (!neighbor.wall && !closed[nr][nc]) {
                        neighbor.parent = current;
                        open.add(neighbor);
                    }
                }
            }
        }
    }

    void drawPath(Cell node) {
        while (node != null) {
            node.visited = false;
            node.wall = false;
            repaint();
            try { Thread.sleep(50); } catch(Exception ex){}
            node = node.parent;
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                Cell c = grid[i][j];
                if (c.wall) g.setColor(Color.BLACK);
                else if (c.visited) g.setColor(Color.CYAN);
                else g.setColor(Color.WHITE);

                g.fillRect(j * SIZE, i * SIZE, SIZE, SIZE);

                g.setColor(Color.GRAY);
                g.drawRect(j * SIZE, i * SIZE, SIZE, SIZE);
            }
        }
        g.setColor(Color.GREEN);
        g.fillRect(0,0,SIZE,SIZE);
        g.setColor(Color.RED);
        g.fillRect((COLS-1)*SIZE,(ROWS-1)*SIZE,SIZE,SIZE);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("A* Maze Visualizer");
        MazeVisualizer viz = new MazeVisualizer();

        JButton runBtn = new JButton("Start");
        runBtn.addActionListener(e -> viz.startAStar());

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            for (int i = 0; i < ROWS; i++)
                for (int j = 0; j < COLS; j++)
                    viz.grid[i][j] = new Cell(i,j);
            viz.repaint();
        });

        JPanel panel = new JPanel();
        panel.add(runBtn);
        panel.add(resetBtn);

        frame.add(panel, BorderLayout.SOUTH);
        frame.add(viz, BorderLayout.CENTER);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}