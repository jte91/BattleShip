import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.sound.sampled.*;
//import java.io.*;
//import java.net.*;

public class BattleShip extends JFrame {
  private PlayerPanel playerPanel;
  private ComputerPanel computerPanel;
  private int gridSize = 10;
  private int cellSize = 30;
  private int numShips = 10;
  private int[][] gameGrid;
  private boolean[][] hitGrid;
  private boolean[][] missedGrid;
  private static final int NONE = 0;
  private static final int SHIP = -1;
  private JButton newButton;
//  private JButton resetButton;
//  private JPanel drawingPanel;
  private JLabel statusLabel;
  private int[][] playerModel = new int[10][10];
  private int[][] computerModel = new int[10][10];
//  private String host = "localhost";
  private JLabel timerLabel;
  private TimerThread timer = null;
  private Toolkit toolkit;
  private volatile boolean running = false;
//  private PrintWriter out = null;
//  private boolean yourTurn = false;
  private boolean addingShips = false;
  private int shipCount = 0;
  private boolean debug = false;
  private volatile boolean lost = false;
  private volatile boolean won = false;
  private volatile boolean hit = false;
  private volatile boolean miss = false;
  private Color defaultBackground;
  private boolean started = false;
  private Point firstPoint = new Point(0,0);
  private Point secondNextPoint = new Point(0,0);
  private Point thirdNextPoint = new Point(0,0);
  private JPanel secondNextCell = null;
  private JPanel thirdNextCell = null;
  public static int numRows = 10;
  public static int numCols = 10;
  public static int playerShips;
  public static int computerShips;
//  public static String[][] grid = new String[numRows][numCols];
//  public static int[][] missedGuesses = new int[numRows][numCols];

  public BattleShip() {
    super("BattleShip");

    ActionHandler ah = new ActionHandler();
    MouseHandler mh = new MouseHandler();
//    MouseHandler mj = new MouseHandler();
    toolkit = getToolkit();

    JPanel buttonPanel = new JPanel();
//    buttonPanel.setLayout(new GridLayout(2, 1));
    add(buttonPanel, BorderLayout.NORTH);

//    JPanel timerPanel = new JPanel();
//    buttonPanel.add(timerPanel);

//    timerLabel = new JLabel();
//    timerPanel.add(timerLabel);

    JPanel mainPanel=new JPanel();
    mainPanel.setLayout(new GridLayout(1,2));
    add(mainPanel);

    playerPanel = new PlayerPanel();
    playerPanel.setPreferredSize(new Dimension(gridSize * cellSize+5, gridSize * cellSize + 1));
    playerPanel.addMouseListener(mh);
    mainPanel.add(playerPanel);

    computerPanel = new ComputerPanel();
    computerPanel.setPreferredSize(new Dimension(gridSize * cellSize, gridSize * cellSize + 1));
    computerPanel.addMouseListener(mh);
    mainPanel.add(computerPanel);

    // attackPanel = new GamePanel();
    // JPanel newPanel2 = new JPanel();

    newButton = new JButton("**** Welcome to Battle Ship game: click to start ****");
    newButton.addActionListener(ah);
    buttonPanel.add(newButton);

//    gamePanel.setPreferredSize(new Dimension(gridSize * cellSize, gridSize * cellSize + 1));
//    gamePanel.addMouseListener(mh);
//    add(gamePanel);

    // attackPanel.setPreferredSize(new Dimension(gridSize * cellSize, gridSize * cellSize + 1));
    // attackPanel.addMouseListener(mj);
    // add(attackPanel);

    JPanel statusPanel = new JPanel();
    add(statusPanel, BorderLayout.SOUTH);

    statusLabel = new JLabel(" ");
    statusPanel.add(statusLabel);

    hitGrid=new boolean[gridSize][gridSize];
//    hitGrid=new int[gridSize][gridSize];
//    deployPlayerShips();
//    createBoard();
    setDefaultCloseOperation(EXIT_ON_CLOSE);
//    setResizable(false);
    pack();
    setVisible(true);
  }

  public void deployPlayerShips() {
    statusLabel.setText("Deploy your ships:");
    // Deploying five ships for player
    BattleShip.playerShips = 5;
    for (int i = 1; i <= BattleShip.playerShips; i++) {
      System.out.println("You can't place two or more ships on the same location");
      System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
    }
    // printOceanMap();
  }

  public void createBoard() {
    if (timer != null)
      timer.interrupt();
    hitGrid = new boolean[gridSize][gridSize];
    missedGrid = new boolean[gridSize][gridSize];
    gameGrid = new int[gridSize][gridSize];
    lost = false;
    won = false;
    started = false;
    statusLabel.setText("Right now, sea is empty");
    timerLabel.setText("Time:      0:00.0");
    int x;
    int y;
    for (int i = 0; i < numShips; i++) {
      do {
        x = (int) (Math.random() * (gridSize - 1) + .5);
        y = (int) (Math.random() * (gridSize - 1) + .5);
      } while (gameGrid[x][y] != NONE);
      gameGrid[x][y] = SHIP;
    }
    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        if (gameGrid[i][j] == SHIP)
          continue;
      }
    }
  }

  private class PlayerPanel extends JPanel 
  {
    public void paintComponent(Graphics g) 
    {
      super.paintComponent(g);

      g.setColor(Color.CYAN);
      g.fillRect(0,0,gridSize*cellSize,gridSize*cellSize);
      g.setColor(Color.BLACK);
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(0, i * cellSize, cellSize*gridSize, i * cellSize);
      }
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(i * cellSize , 0, i * cellSize ,cellSize*gridSize);
      }
      g.setColor(Color.LIGHT_GRAY);
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {
          if (playerModel[i][j] == SHIP) 
          {
            g.fillRect(i * cellSize+1, j * cellSize+1, cellSize-1,cellSize-1);
          }
        }
      }
    }
  }

  private class ComputerPanel extends JPanel 
  {
    public void paintComponent(Graphics g) 
    {
      super.paintComponent(g);
      g.setColor(Color.BLACK);
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(0, i * cellSize, cellSize*gridSize, i * cellSize);
      }
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(i * cellSize , 0, i * cellSize ,cellSize*gridSize);
      }
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {

          if (hitGrid[i][j] == true) 
          {
            g.setColor(Color.RED);
            g.fillRect(i * cellSize, j * cellSize, 20, 20);
          } else 
          {
            g.setColor(Color.BLUE);
            // g.fillRect(i*cellSize,j*cellSize,20,20);
          }
        }
      }
    }
  }

  /*
  public void getComp2(Point newPoint)
    {
        secondNextCell = (JPanel) this.getComponentAt(newPoint);
    }
    public void getComp3(Point newPoint)
    {
        thirdNextCell = (JPanel) this.getComponentAt(newPoint);
    }
    protected JPanel getCell()
    {

        JPanel firstCell = new JPanel();
        firstCell.setBorder(BorderFactory.createLineBorder(Color.blue, 2));
        firstCell.setPreferredSize(new Dimension(20, 20)); // for demo purposes only
        firstCell.setBackground(Color.black);

        firstCell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) 
                {

                    firstPoint = firstCell.getLocation();
                    double xPos = (firstPoint.getX()/20+1);
                    int x = (int) xPos;
                    double yPos = (firstPoint.getY()/20+1);
                    int y = (int) yPos;

                    double xPos2 = (firstPoint.getX()/20+2);
                    int x2 = (int) xPos2;
                    double yPos2 = (firstPoint.getY()/20+1);
                    int y2 = (int) yPos2;

                    double xPos3 = (firstPoint.getX()/20+3);
                    int x3 = (int) xPos3;
                    double yPos3 = (firstPoint.getY()/20+1);
                    int y3 = (int) yPos3;


                    System.out.print("\nLocation (X: " + x + " Y: " + y + ")");

                    secondNextPoint = new Point((int)(firstPoint.getX()+20),(int)(firstPoint.getY()));
                    thirdNextPoint = new Point((int)(firstPoint.getX()+40),(int)(firstPoint.getY()));
                    Coordinate a = new Coordinate(x,y);
                    Coordinate b = new Coordinate(x2,y2);
                    Coordinate c = new Coordinate(x3,y3);

                    getComp2(secondNextPoint);
                    getComp3(thirdNextPoint);
                    // BattleShip.addShip(a,b,c); // Create new ship object
                    // draw();
                
                }
      
        });
        return firstCell;
    }
*/

  public class Coordinate 
  {
    int x;
    int y;
    public Coordinate(int x, int y)
    {
        this.x=x;
        this.y=y;
    }
    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    //compare coordinate objects
    public boolean compareCoord(Coordinate coordinate)
    {
        if(coordinate.getX() == this.x && coordinate.getY() == this.y)
        {
            return true;
        }
        return false;
    }
    public String toString()
    {
        return "\nX=" + x + " and Y=" + y;
    }
}

  private class ActionHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // createBoard();
      // gamePanel.repaint();
      //deployPlayerShips();
      
      playerModel = new int[10][10];
      computerModel = new int[10][10];
      addingShips=true;
      shipCount=0;
      playerPanel.repaint();
      statusLabel.setText("Deploy your ships:");
    }
  }

  private class MouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      int i = e.getX() / cellSize;
      int j = e.getY() / cellSize;
      if(addingShips)
      {
        playerModel[i][j]=SHIP;
        shipCount++;
        if(shipCount==numShips)
        {
          addingShips=false;
          statusLabel.setText("Your Turn");
          return;
        }
        playerPanel.repaint();
      }
/*
      if (e.getButton() == MouseEvent.BUTTON1)
      {
        if (gameGrid[i][j] == SHIP) {
          statusLabel.setText("You Hit a Ship!");
          hitGrid[i][j] = true;
        } else {
          statusLabel.setText("You Missed!");
          defaultBackground = getBackground();
          setBackground(Color.BLUE);
        }
      }
      model[i][j] = 1;
//      out.println(i + "," + j);
      playerPanel.repaint();
//      yourTurn = false;
      statusLabel.setText("Opponent's turn.");
*/
    }
  }

  private class TimerThread extends Thread {
    public void run() {
      float msecs = 0;
      long base = System.currentTimeMillis();
      try {
        while (running) {
          sleep(20);
          long curr = System.currentTimeMillis();
          int delt = (int) (curr - base) / 100;
          int tenths = (int) (delt % 10);
          int mins = (int) (delt / 600);
          int secs = (int) ((delt / 10) % 60);
          timerLabel.setText("Time: " + String.format("%6d:%02d.%01d", mins, secs, tenths));
        }
      } catch (InterruptedException ie) {
      }
    }
  }

  public static void main(String[] args) {
    new BattleShip();
  }
}
