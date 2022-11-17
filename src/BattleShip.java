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
      // for(int i=0;i<gridSize;i++)
      // {
      //   for(int j=0;j<gridSize;j++)
      //   { 
      //     if(uncoveredGrid[i][j])
      //     {
      //       g.setColor(Color.BLUE);
      //       if(gameGrid[i][j]>0) 
      //            g.drawString(""+gameGrid[i][j],i*cellSize+7,j*cellSize+15);
      //     }
      //     else
      //     {
      //       g.setColor(Color.GRAY);
      //       g.fillRect(i*cellSize,j*cellSize,20,20);
      //     }
      //     if(hit&&gameGrid[i][j]==SHIP)
      //     {
      //       if(uncoveredGrid[i][j]) g.setColor(Color.RED);
      //       else g.setColor(Color.BLUE);
      //       g.drawLine(i*cellSize+10,j*cellSize+8,i*cellSize+12,j*cellSize+4);
      //       g.fillOval(i*cellSize+5,j*cellSize+6,11,11);
      //       g.setColor(Color.WHITE);
      //       g.drawLine(i*cellSize+8,j*cellSize+10,i*cellSize+10,j*cellSize+10);
      //     }
      //     if(flaggedGrid[i][j])
      //     {
      //       g.setColor(Color.BLACK);
      //       g.drawLine(i*cellSize+5,j*cellSize+5,i*cellSize+5,j*cellSize+19);
      //       g.setColor(Color.RED);
      //       g.fillRect(i*cellSize+6,j*cellSize+6,10,6);
      //     }
      //     if(debug)
      //     {
      //       g.setColor(Color.LIGHT_GRAY);
      //       if(!uncoveredGrid[i][j]) 
      //               g.drawString(""+gameGrid[i][j],i*cellSize+6,j*cellSize+15);
      //     }
      //   }
      }
    //   for (int j = 0; j < 20; j++) {
    //     for (int i = 0; i < 20; i++) {
    //       if (model[i][j] == 1) {
            
    //         g.fillRect(20 * i, 20 * j, 20, 20);
    //       }
    //     }
    //   }
    //   g.setColor(Color.GRAY);
    //   for (int i = 0; i <= 20; i++) {
    //     g.drawLine(0, 20 * i, 400, 20 * i);
    //     g.drawLine(20 * i, 0, 20 * i, 400);
    //   }
    // };drawingPanel.setPreferredSize(new Dimension(601,601));drawingPanel.addMouseListener(mh);

    // add(drawingPanel);

    
    //}
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
      //     if (out != null)
      //       out.println("reset");
      //     drawingPanel.repaint();
      //   } else if (e.getSource() == hostItem) {
      //     String s = JOptionPane.showInputDialog(BattleShip.this, "Enter the port to use", "" + port);
      //     if (s == null)
      //       return;
      //     port = Integer.parseInt(s);
      //     yourTurn = false;
      //     hostItem.setEnabled(false);
      //     joinItem.setEnabled(false);
      //     quitItem.setEnabled(true);
      //     new Server().start();
      //   } else if (e.getSource() == joinItem) {
      //     String s = JOptionPane.showInputDialog(BattleShip.this, "Enter the hostname", "" + host);
      //     if (s == null)
      //       return;
      //     host = s;
      //     s = JOptionPane.showInputDialog(BattleShip.this, "Enter the port to use", "" + port);
      //     if (s == null)
      //       return;
      //     port = Integer.parseInt(s);
      //     yourTurn = true;
      //     hostItem.setEnabled(false);
      //     joinItem.setEnabled(false);
      //     quitItem.setEnabled(true);
      //     new Client().start();
      //   } else if (e.getSource() == quitItem) {
      //     connected = false;
      //     if (serverSocket != null) {
      //       try {
      //         serverSocket.close();
      //         serverSocket = null;
      //       } catch (IOException ioe) {
      //         System.out.println(ioe);
      //       }
      //     }
      //     hostItem.setEnabled(true);
      //     joinItem.setEnabled(true);
      //     quitItem.setEnabled(false);
      //     statusLabel.setText("Game ended.");
      //     if (out != null)
      //       out.println("quit");

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

    // private class Server extends Thread {
    //   public void run() {
    //     try {
    //       serverSocket = new ServerSocket(port);
    //       statusLabel.setText("Waiting for opponent to connect...");
    //       Socket s = serverSocket.accept();
    //       statusLabel.setText("Opponent's turn.");
    //       connected = true;
    //       BufferedReader in = new BufferedReader(new InputStreamReader(
    //           s.getInputStream()));
    //       out = new PrintWriter(s.getOutputStream(), true);
    //       String line;
    //       while (connected && (line = in.readLine()) != null) {
    //         if (line.equals("quit"))
    //           break;
    //         if (line.equals("reset")) {
    //           for (int j = 0; j < 20; j++) {
    //             for (int i = 0; i < 20; i++) {
    //               model[i][j] = 0;
    //             }
    //           }
    //           drawingPanel.repaint();
    //           continue;
    //         }
    //         String[] sa = line.split(",");
    //         int i = Integer.parseInt(sa[0]);
    //         int j = Integer.parseInt(sa[1]);
    //         model[i][j] = 2;
    //         drawingPanel.repaint();
    //         yourTurn = true;
    //         statusLabel.setText("Your turn.");
    //       }
    //       connected = false;
    //       statusLabel.setText("Game ended.");
    //       if (serverSocket != null)
    //         serverSocket.close();
    //       serverSocket = null;
    //       s.close();
    //     } catch (IOException ioe) {
    //       System.out.println(ioe);
    //     }
    //     hostItem.setEnabled(true);
    //     joinItem.setEnabled(true);
    //     quitItem.setEnabled(false);
    //   }
    // }

    // private class Client extends Thread {
    //   public void run() {
    //     try {
    //       Socket s = new Socket(host, port);
    //       statusLabel.setText("Your turn.");
    //       connected = true;
    //       BufferedReader in = new BufferedReader(new InputStreamReader(
    //           s.getInputStream()));
    //       out = new PrintWriter(s.getOutputStream(), true);
    //       String line;
    //       while (connected && (line = in.readLine()) != null) {
    //         if (line.equals("quit"))
    //           break;
    //         if (line.equals("reset")) {
    //           for (int j = 0; j < 20; j++) {
    //             for (int i = 0; i < 20; i++) {
    //               model[i][j] = 0;
    //             }
    //           }
    //           drawingPanel.repaint();
    //           continue;
    //         }
    //         String[] sa = line.split(",");
    //         int i = Integer.parseInt(sa[0]);
    //         int j = Integer.parseInt(sa[1]);
    //         model[i][j] = 2;
    //         drawingPanel.repaint();
    //         yourTurn = true;
    //         statusLabel.setText("Your turn.");
    //       }
    //       connected = false;
    //       statusLabel.setText("Game ended.");
    //       s.close();
    //     } catch (IOException ioe) {
    //       System.out.println(ioe);
    //     }
    //     hostItem.setEnabled(true);
    //     joinItem.setEnabled(true);
    //     quitItem.setEnabled(false);
    //   }
    // }

    public static void main(String[] args) 
    {
      new BattleShip();
    }
  }

// JPanel buttonPanel=new JPanel();
// buttonPanel.setLayout(new GridLayout(2,1));
// add(buttonPanel,BorderLayout.NORTH);

// JPanel timerPanel=new JPanel();
// buttonPanel.add(timerPanel);

// timerLabel=new JLabel();
// timerPanel.add(timerLabel);

// gamePanel=new GamePanel();
// JPanel newPanel=new JPanel();
// buttonPanel.add(newPanel);

// gamePanel.setPreferredSize(new Dimension(new Dimension(gridSize*cellSize
// ,gridSize*cellSize+1)));
// gamePanel.addMouseListener(mh);
// add(gamePanel);

// JPanel statusPanel=new JPanel();
// add(statusPanel,BorderLayout.SOUTH);

// statusLabel=new JLabel("Right now, sea is empty");
// statusPanel.add(statusLabel);

// createBoard();

// deployPlayerShips();

// deployComputerShips();
// do
// {
// Battle();
// }
// while (BattleShip.playerShips != 0 && BattleShip.computerShips != 0);

// setDefaultCloseOperation(EXIT_ON_CLOSE);
// setResizable(false);
// pack();
// setVisible(true);

// gameOver();
// }

// public void createBoard()
// {
// if(timer!=null) timer.interrupt();
// gameGrid=new int[gridSize][gridSize];
// lost=false;
// won=false;
// started=false;
// statusLabel.setText("Right now, sea is empty");
// timerLabel.setText("Time: 0:00.0");

// public void deployPlayerShips()
// {
// Scanner input = new Scanner(System.in);
// statusLabel.setText("Deploy your ships:");
// // Deploying five ships for player
// BattleShip.playerShips = 5;
// for (int i = 1; i <= BattleShip.playerShips;)
// {
// statusLabel.setText("Enter X coordinate for your ship: ");
// int x = input.nextInt();
// statusLabel.setText("Enter Y coordinate for your ship: ");
// int y = input.nextInt();

// if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == "
// ")) {
// grid[x][y] = "@";
// i++;
// } else if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && grid[x][y]
// == "@")
// System.out.println("You can't place two or more ships on the same location");
// else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols))
// System.out.println("You can't place ships outside the " + numRows + " by " +
// numCols + " grid");
// }
// printOceanMap();
// }

// public void deployComputerShips()
// {
// statusLabel.setText("Computer is deploying ships");
// // Deploying five ships for computer
// BattleShip.computerShips = 5;
// for (int i = 1; i <= BattleShip.computerShips;)
// {
// int x = (int) (Math.random() * 10);
// int y = (int) (Math.random() * 10);

// if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == "
// "))
// {
// grid[x][y] = "x";
// statusLabel.setText("ship DEPLOYED");
// i++;
// }
// }
// printOceanMap();
// }

// public void Battle() {
// playerTurn();
// computerTurn();

// printOceanMap();

// System.out.println();
// System.out.println("Your ships: " + BattleShip.playerShips + " | Computer
// ships: " + BattleShip.computerShips);
// System.out.println();
// }

// public void playerTurn() {
// statusLabel.setText("YOUR TURN");
// int x = -1, y = -1;
// do {
// statusLabel.setText("Enter X coordinate: ");
// statusLabel.setText("Enter Y coordinate: ");

// if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols)) // valid guess
// {
// if (grid[x][y] == "x") // if computer ship is already there; computer loses
// ship
// {
// System.out.println("Boom! You sunk the ship!");
// grid[x][y] = "!"; // Hit mark
// --BattleShip.computerShips;
// } else if (grid[x][y] == "@") {
// System.out.println("Oh no, you sunk your own ship :(");
// grid[x][y] = "x";
// --BattleShip.playerShips;
// ++BattleShip.computerShips;
// } else if (grid[x][y] == " ") {
// System.out.println("Sorry, you missed");
// grid[x][y] = "-";
// }
// } else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)) // invalid
// guess
// System.out.println("You can't place ships outside the " + numRows + " by " +
// numCols + " grid");
// } while ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)); // keep
// re-prompting till valid guess
// }

// public void computerTurn()
// {
// statusLabel.setText("COMPUTER'S TURN");
// // Guess co-ordinates
// int x = -1, y = -1;
// do {
// x = (int) (Math.random() * 10);
// y = (int) (Math.random() * 10);

// if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols)) // valid guess
// {
// if (grid[x][y] == "@") // if player ship is already there; player loses ship
// {
// statusLabel.setText("The Computer sunk one of your ships!");
// grid[x][y] = "x";
// --BattleShip.playerShips;
// ++BattleShip.computerShips;
// } else if (grid[x][y] == "x") {
// statusLabel.setText("The Computer sunk one of its own ships");
// grid[x][y] = "!";
// } else if (grid[x][y] == " ") {
// statusLabel.setText("Computer missed");
// // Saving missed guesses for computer
// if (missedGuesses[x][y] != 1)
// missedGuesses[x][y] = 1;
// }
// }
// } while ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)); // keep
// re-prompting till valid guess
// }

// public void gameOver() {
// System.out.println("Your ships: " + BattleShip.playerShips + " | Computer
// ships: " + BattleShip.computerShips);
// if (BattleShip.playerShips > 0 && BattleShip.computerShips <= 0)
// statusLabel.setText("You won!");
// else
// statusLabel.setText("You lost!");
// }

// public void printOceanMap() {
// System.out.println();
// // First section of Ocean Map
// System.out.print(" ");
// for (int i = 0; i < numCols; i++)
// System.out.print(i);
// System.out.println();

// // Middle section of Ocean Map
// for (int x = 0; x < grid.length; x++) {
// System.out.print(x + "|");

// for (int y = 0; y < grid[x].length; y++) {
// System.out.print(grid[x][y]);
// }

// System.out.println("|" + x);
// }

// // Last section of Ocean Map
// System.out.print(" ");
// for (int i = 0; i < numCols; i++)
// System.out.print(i);
// System.out.println();
// }

// private class GamePanel extends JPanel
// {
// public void paintComponent(Graphics g)
// {
// super.paintComponent(g);
// g.setColor(Color.LIGHT_GRAY);
// for(int i=0;i<gridSize+1;i++)
// {
// g.drawLine(0,i*cellSize,getWidth(),i*cellSize);
// }
// for(int i=0;i<gridSize-1;i++)
// {
// g.drawLine(i*cellSize+cellSize,0,i*cellSize+cellSize,getHeight());
// }
// for(int i=0;i<gridSize;i++)
// {
// for(int j=0;j<gridSize;j++)
// {
// if(uncoveredGrid[i][j])
// {
// g.setColor(Color.BLACK);
// if(gameGrid[i][j]>0)
// g.drawString(""+gameGrid[i][j],i*cellSize+7,j*cellSize+15);
// }
// else
// {
// g.setColor(Color.GRAY);
// g.fillRect(i*cellSize,j*cellSize,20,20);
// }
// if(lost&&gameGrid[i][j]==MINE)
// {
// if(uncoveredGrid[i][j]) g.setColor(Color.RED);
// else g.setColor(Color.BLACK);
// g.drawLine(i*cellSize+10,j*cellSize+8,i*cellSize+12,j*cellSize+4);
// g.fillOval(i*cellSize+5,j*cellSize+6,11,11);
// g.setColor(Color.WHITE);
// g.drawLine(i*cellSize+8,j*cellSize+10,i*cellSize+10,j*cellSize+10);
// }
// if(flaggedGrid[i][j])
// {
// g.setColor(Color.BLACK);
// g.drawLine(i*cellSize+5,j*cellSize+5,i*cellSize+5,j*cellSize+19);
// g.setColor(Color.RED);
// g.fillRect(i*cellSize+6,j*cellSize+6,10,6);
// }
// if(debug)
// {
// g.setColor(Color.LIGHT_GRAY);
// if(!uncoveredGrid[i][j])
// g.drawString(""+gameGrid[i][j],i*cellSize+6,j*cellSize+15);
// }
// }
// }
// }
// }

// private class ActionHandler implements ActionListener
// {
// public void actionPerformed(ActionEvent e)
// {
// createBoard();
// gamePanel.repaint();
// }
// }

// private class MouseHandler extends MouseAdapter
// {
// public void mouseClicked(MouseEvent e)
// {
// if(lost) return;
// if(won) return;
// if(!started)
// {
// started=true;
// timer=new TimerThread();
// timer.start();
// }
// int i=e.getX()/cellSize;
// int j=e.getY()/cellSize;
// if(e.getButton()==MouseEvent.BUTTON1)
// {
// if(gameGrid[i][j]==MINE)
// {
// uncoveredGrid[i][j]=true;
// lost=true;
// timer.interrupt();
// statusLabel.setText("You Lost!");
// new SoundThread(BOOM).start();
// }
// else
// {
// if(clearGrid(i,j)) new SoundThread(TAP).start();
// }
// }
// else if(e.getButton()==MouseEvent.BUTTON3)
// {
// if(!uncoveredGrid[i][j])
// {
// flaggedGrid[i][j]=!flaggedGrid[i][j];
// }
// else
// {
// if(countFlags(i,j)!=gameGrid[i][j]) return;
// clearNeighbors(i,j);
// }
// }
// gamePanel.repaint();
// checkWin();
// if(won)
// {
// timer.interrupt();
// statusLabel.setText("You Won!");
// }
// }
// }

// private class TimerThread extends Thread
// {
// public void run()
// {
// float msecs=0;
// long base=System.currentTimeMillis();
// try
// {
// while(!lost&&!won)
// {
// sleep(20);
// long curr=System.currentTimeMillis();
// int delt=(int)(curr-base)/100;
// int tenths=(int)(delt%10);
// int mins=(int)(delt/600);
// int secs=(int)((delt/10)%60);
// timerLabel.setText("Time: "+String.format("%6d:%02d.%01d"
// ,mins,secs,tenths));
// toolkit.sync();
// }
// }
// catch(InterruptedException ie)
// {}
// }
// }
// public static void main(String[] args)
// {
// new BattleShip();
// }
// }
