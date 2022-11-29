import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.sound.sampled.*;
//import java.io.*;
//import java.net.*;

public class BattleShip extends JFrame 
{
  private PlayerPanel playerPanel;
  private ComputerPanel computerPanel;
  private int gridSize = 10;
  private int cellSize = 30;
  private int numShips = 10;
  private int guessCount = 1;
  private int[][] gameGrid;
  private boolean[][] hitGrid;
  private boolean[][] missedGrid;
  private static final int NONE = 0;
  private static final int SHIP = -1;
  private JButton newButton;
  private JLabel statusLabel;
  private int[][] playerModel = new int[10][10];
  private int[][] computerModel = new int[10][10];
  private JLabel timerLabel;
  private TimerThread timer = null;
  private Toolkit toolkit;
  private volatile boolean running = false;
//  private boolean yourTurn = false;
  private boolean addingShips = false;
  private boolean detectingShips = false;
  private int shipCount = 0;
  private int guess = 0;
  private boolean debug = true;
  private volatile boolean lost = false;
  private volatile boolean won = false;
  private volatile boolean hit = false;
  private volatile boolean miss = false;
  private boolean started = false;
  public static int numRows = 10;
  public static int numCols = 10;
  public static int playerShips;
  public static int computerShips;

  public BattleShip() {
    super("BattleShip");

    ActionHandler ah = new ActionHandler();
    MouseHandler mh = new MouseHandler();
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

    newButton = new JButton("**** Welcome to Battle Ship game: click to start ****");
    newButton.addActionListener(ah);
    buttonPanel.add(newButton);

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
    computerModel = new int[gridSize][gridSize];
    lost = false;
    won = false;
    started = false;
    int x;
    int y;
    for (int i = 0; i < numShips; i++) {
      do {
        x = (int) (Math.random() * (gridSize - 1) + .5);
        y = (int) (Math.random() * (gridSize - 1) + .5);
        
      } while (computerModel[x][y] != NONE);
      computerModel[x][y] = SHIP; 
    }      

    for (int i = 0; i < gridSize; i++) {
      for (int j = 0; j < gridSize; j++) {
        if (computerModel[i][j] == SHIP)
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
      g.setColor(Color.RED);
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {
          if (computerModel[i][j] == SHIP) 
          {
            //hitGrid[i][j] == true
            g.fillRect(i * cellSize+1, j * cellSize+1, cellSize-1,cellSize-1);
          } else 
          {
            g.setColor(Color.BLUE);
            // g.fillRect(i*cellSize,j*cellSize,20,20);
          }
          // if(debug)
          // {
          //   g.setColor(Color.CYAN);
          //   g.drawString(""+computerModel[i][j], i*cellSize+6, j*cellSize+15);
          // }
        }
      }
    }
  }

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
      detectingShips=false;
      shipCount=0;
      guess=0;
      playerPanel.repaint();
      statusLabel.setText("Deploy your ships:");
    }
  }

  private class MouseHandler extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      int i = e.getX() / cellSize;
      int j = e.getY() / cellSize;
      int guess = 0;
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
        detectingShips=true;
        playerPanel.repaint();
      }
      else if(detectingShips)
      {
        if(computerModel[i][j]==SHIP)
        {
          statusLabel.setText("You Hit a Ship!");
          guess++;
        }
        else
        {
          statusLabel.setText("You Missed!");
          guess++;
        }
        if(guess==guessCount)
        {
          detectingShips=false;
        }
        statusLabel.setText("Computer's Turn");
        computerPanel.repaint();
      }

//       model[i][j] = 1;
// //      out.println(i + "," + j);
//       playerPanel.repaint();
// //      yourTurn = false;
//       statusLabel.setText("Opponent's turn.");
// */
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
