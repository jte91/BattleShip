import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class BattleShip extends JFrame 
{
  private JPanel gamePanel;
  private int gridSize = 10;
  private int cellSize = 50;
  private int numShips=10;
  private int[][] gameGrid;
  private boolean[][] uncoveredGrid;
  private boolean[][] flaggedGrid;
  private static final int NONE=0;
  private static final int SHIP=-1;
  private JMenuItem hostItem;
  private JMenuItem joinItem;
  private JMenuItem quitItem;
  private JButton newButton;
  private JButton resetButton;
  private JPanel drawingPanel;
  private JLabel statusLabel;
  private int[][] model = new int[10][10];
  private String host = "localhost";
  private JLabel timerLabel;
  private TimerThread timer=null;
  private Toolkit toolkit;
  private volatile boolean running = false;
  private int port = 5000;
  private PrintWriter out = null;
  private boolean connected = false;
  private boolean yourTurn = false;
  private ServerSocket serverSocket;
  private boolean debug = false;
  private volatile boolean lost = false;
  private volatile boolean won = false;
  private volatile boolean hit = false;
  private volatile boolean miss = false;

  private boolean started = false;
  // private AudioInputStream ais;
  // private AudioFormat[] fmt;
  public static int numRows = 10;
  public static int numCols = 10;
  public static int playerShips;
  public static int computerShips;
  public static String[][] grid = new String[numRows][numCols];
  public static int[][] missedGuesses = new int[numRows][numCols];

  public BattleShip() 
  {
    super("BattleShip");

    ActionHandler ah=new ActionHandler();
    MouseHandler mh=new MouseHandler();
    toolkit = getToolkit();

    // JMenuBar jmb = new JMenuBar();
    // setJMenuBar(jmb);

    // JMenu networkMenu = new JMenu("Network");
    // jmb.add(networkMenu);

    // hostItem = new JMenuItem("Host Game...");
    // hostItem.addActionListener(ah);
    // networkMenu.add(hostItem);

    // joinItem = new JMenuItem("Join Game...");
    // joinItem.addActionListener(ah);
    // networkMenu.add(joinItem);

    // quitItem = new JMenuItem("Quit Game");
    // quitItem.addActionListener(ah);
    // quitItem.setEnabled(false);
    // networkMenu.add(quitItem);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(2, 1));
    add(buttonPanel, BorderLayout.NORTH);

    JPanel timerPanel = new JPanel();
    buttonPanel.add(timerPanel);

    timerLabel = new JLabel();
    timerPanel.add(timerLabel);

    gamePanel = new GamePanel();
    JPanel newPanel = new JPanel();
    buttonPanel.add(newPanel);

    newButton = new JButton("**** Welcome to Battle Ships game ****");
    newButton.addActionListener(ah);
    newPanel.add(newButton);

    gamePanel.setPreferredSize(new Dimension(gridSize*cellSize, gridSize*cellSize+1));
    gamePanel.addMouseListener(mh);
    add(gamePanel);

    JPanel statusPanel=new JPanel();
    add(statusPanel,BorderLayout.SOUTH);

    statusLabel=new JLabel(" ");
    statusPanel.add(statusLabel);

    createBoard();
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    pack();
    setVisible(true);
  }

  public void createBoard() 
  {
    if (timer != null)
    timer.interrupt();
    gameGrid = new int[gridSize][gridSize];
    //uncoveredGrid=new boolean[gridSize][gridSize];
    //flaggedGrid=new boolean[gridSize][gridSize];
    lost = false;
    won = false;
    started = false;
    statusLabel.setText("Right now, sea is empty");
    timerLabel.setText("Time:      0:00.0");
    int x;
    int y;
    for(int i=0; i<numShips; i++)
    {
      do
      {
        x=(int)(Math.random() * (gridSize-1)+.5);
        y=(int)(Math.random() * (gridSize-1)+.5);
      }
      while(gameGrid[x][y] != NONE);
      gameGrid[x][y]=SHIP;
    }
    for(int i=0;i<gridSize;i++)
    {
      for(int j=0;j<gridSize;j++)
      {
        if(gameGrid[i][j]==SHIP) continue;
        // gameGrid[i][j]=countMines(i,j);
      }
    }
  }

  private void checkWin()
  {
    won=true;
    for(int i=0;i<gridSize;i++)
    {
      for(int j=0;j<gridSize;j++)
      {
        if(!uncoveredGrid[i][j]&&gameGrid[i][j]==SHIP) won=false;
      }
    }
  }

  private class GamePanel extends JPanel 
  {
    public void paintComponent(Graphics g) 
    {
      super.paintComponent(g);
      g.setColor(Color.BLACK);
      for(int i=0;i<gridSize+1;i++)
      {
        g.drawLine(0,i*cellSize,getWidth(),i*cellSize);
      }
      for(int i=0;i<gridSize-1;i++)
      {
        g.drawLine(i*cellSize+cellSize,0,i*cellSize+cellSize,getHeight());
      }
   
  }
    public void deployPlayerShips() 
    {
      statusLabel.setText("Deploy your ships:");
      // Deploying five ships for player
      BattleShip.playerShips = 5;
      for (int i = 1; i <= BattleShip.playerShips;) {
        statusLabel.setText("Enter X coordinate for your ship: ");
        //int x = input.nextInt();
        statusLabel.setText("Enter Y  coordinate for your ship: ");
        //int y = input.nextInt();

        //if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == " ")) {
          //grid[x][y] = "@";
          i++;
       // } else if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && grid[x][y] == "@")
          System.out.println("You can't place two or more ships on the same location");
        //else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols))
          System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
      }
      // printOceanMap();
    }

    private class ActionHandler implements ActionListener 
    {
      public void actionPerformed(ActionEvent e) 
      {
        //createBoard();
        // gamePanel.repaint();
        deployPlayerShips();
        statusLabel.setText("Deploy your ships:");
      }
    }


    private class MouseHandler extends MouseAdapter 
    {
      public void mousePressed(MouseEvent e) 
      {
        if (out == null)
          return;
        if (!connected)
          return;
        if (!yourTurn)
          return;
        if(lost) 
          return;
        if(won) 
          return;
        if(!started)
          {
            started=true;
            timer=new TimerThread();
            timer.start();
          }
        int i = e.getX() / cellSize;
        int j = e.getY() / cellSize;
        if(e.getButton()==MouseEvent.BUTTON1)
        {
          if(gameGrid[i][j]==SHIP) 
          {
            //uncoveredGrid[i][j]=true;
            //timer.interrupt();
            statusLabel.setText("You Hit a Ship!");
            //new SoundThread(BOOM).start();
          }
          else
          {
            statusLabel.setText("You Missed!");
          }
        }
        model[i][j] = 1;
        out.println(i + "," + j);
        gamePanel.repaint();
        //checkShipHit();
        yourTurn = false;
        statusLabel.setText("Opponent's turn.");
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


    public static void main(String[] args) 
    {
      new BattleShip();
    }
  }

