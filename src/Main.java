import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
// Варіант 3, 3

public class Main {

    public static void main(String[] args) {
        System.out.println(" Java Lab #6 ");
	// write your code here
        SwingUtilities.invokeLater(() -> {
            JFrame f=new JFrame("Об’єднана програма");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(900,700);

            JTabbedPane tabs=new JTabbedPane();
            tabs.addTab("Анімація", new BallsDepthAnimation());
            tabs.addTab("Матриці", new MatrixAnalyzerPanel());

            f.add(tabs);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

// --- Панель анімації ---
class BallsDepthAnimation extends JPanel implements ActionListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int FPS = 60;
    private static final double FOV_SCALE = 300.0;
    private static final double APPROACH_SPEED_Z = -80.0;
    private static final double RECEDE_SPEED_Z   = +80.0;
    private static final double Z_NEAR = 50.0;
    private static final double Z_FAR  = 800.0;
    private static final double BASE_RADIUS = 80.0;

    private double zApproach = Z_FAR;
    private double zRecede   = Z_NEAR;
    private final Timer timer;

    public BallsDepthAnimation() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(new Color(20, 22, 28));
        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBall(g2, PANEL_WIDTH/3, PANEL_HEIGHT/2, zApproach, Color.BLUE, Color.CYAN);
        drawBall(g2, 2*PANEL_WIDTH/3, PANEL_HEIGHT/2, zRecede, Color.RED, Color.PINK);

        g2.dispose();
    }

    private double perspectiveScale(double z) {
        return FOV_SCALE / (FOV_SCALE + z);
    }

    private void drawBall(Graphics2D g2, int cx, int cy, double z, Color fill, Color highlight) {
        double s = perspectiveScale(z);
        double r = BASE_RADIUS * s;
        int x = (int)(cx - r), y = (int)(cy - r), d = (int)(2*r);

        g2.setPaint(fill);
        g2.fillOval(x,y,d,d);
        g2.setPaint(highlight);
        g2.fillOval(x+d/4,y+d/4,d/2,d/2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double dt = 1.0 / FPS;
        zApproach += APPROACH_SPEED_Z * dt;
        zRecede   += RECEDE_SPEED_Z * dt;
        if (zApproach <= Z_NEAR) zApproach = Z_FAR;
        if (zRecede >= Z_FAR)    zRecede   = Z_NEAR;
        repaint();
    }
}

// --- Панель для матриць ---
class MatrixAnalyzerPanel extends JPanel {
    final private JTextField fileField;
    final private JButton loadButton;
    final private JTable tableA, tableB, tableX;

    public MatrixAnalyzerPanel() {
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout());
        fileField = new JTextField(25);
        loadButton = new JButton("Зчитати");
        top.add(new JLabel("Файл:"));
        top.add(fileField);
        top.add(loadButton);
        add(top, BorderLayout.NORTH);

        tableA = new JTable();
        tableB = new JTable();
        tableX = new JTable();

        JPanel tables = new JPanel(new GridLayout(3,1));
        tables.add(new JScrollPane(tableA));
        tables.add(new JScrollPane(tableB));
        tables.add(new JScrollPane(tableX));
        add(tables, BorderLayout.CENTER);

        loadButton.addActionListener(e -> loadData());
    }

    private void loadData() {
        String path = fileField.getText().trim();
        try (Scanner sc = new Scanner(new File("../m/" + path))) {
            int n = Integer.parseInt(sc.nextLine().trim());
            if (n <= 0 || n > 15) throw new MyNegativeMatrixException("Розмір матриці має бути 1–15");

            int[][] A = new int[n][n];
            int[][] B = new int[n][n];
            for (int i=0;i<n;i++) A[i] = parseRow(sc.nextLine(),n);
            for (int i=0;i<n;i++) B[i] = parseRow(sc.nextLine(),n);

            int[] X = new int[n];
            for (int i=0;i<n;i++) {
                boolean allNeg = true;
                for (int j=0;j<n;j++) {
                    if (A[i][j]>=0 || B[i][j]>=0) { allNeg=false; break; }
                }
                X[i] = allNeg?1:0;
            }

            showMatrix(tableA,A,"A");
            showMatrix(tableB,B,"B");
            showVector(tableX,X,"X");

        } catch(FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this,"Файл не знайдено","Помилка",JOptionPane.ERROR_MESSAGE);
        } catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,"Невірний формат даних","Помилка",JOptionPane.ERROR_MESSAGE);
        } catch(MyNegativeMatrixException ex) {
            JOptionPane.showMessageDialog(this,"Власне виключення: "+ex.getMessage(),"Помилка",JOptionPane.ERROR_MESSAGE);
        }
    }

    private int[] parseRow(String line,int n) {
        String[] parts=line.trim().split("\\s+");
        if(parts.length!=n) throw new NumberFormatException("Невірна кількість елементів");
        int[] row=new int[n];
        for(int i=0;i<n;i++) row[i]=Integer.parseInt(parts[i]);
        return row;
    }

    private void showMatrix(JTable t,int[][] M,String title) {
        DefaultTableModel model=new DefaultTableModel();
        for(int i=0;i<M.length;i++) model.addColumn("C"+(i+1));
        for(int[] row:M) model.addRow(Arrays.stream(row).boxed().toArray());
        t.setModel(model);
        t.setBorder(BorderFactory.createTitledBorder(title));
    }

    private void showVector(JTable t,int[] V,String title) {
        DefaultTableModel model=new DefaultTableModel();
        model.addColumn(title,Arrays.stream(V).boxed().toArray());
        t.setModel(model);
    }
}

// --- Власне виключення ---
class MyNegativeMatrixException extends ArithmeticException {
    public MyNegativeMatrixException(String msg){ super(msg); }
}
