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
  private int computerScore = 0;
  private int playerScore = 0;
  private int guessCount = 1;
  private int[][] gameGrid;
  private int[][] playerGuessGrid;
  private int[][] computerGuessGrid;
  private static final int NONE = 0;
  private static final int SHIP = 1;
  private static final int X = 1;
  private JButton newButton;
  private JLabel statusLabel;
  private int[][] playerModel;
  private int[][] computerModel;
  private JLabel timerLabel;
  private TimerThread timer = null;
  private Toolkit toolkit;
  private volatile boolean running = false;
  private boolean addingShips = false;
  private boolean detectingShips = false;
  private boolean computerAttack = false;
  private int shipCount = 0;
  private int guess = 0;
  private boolean debug = true;
  private volatile boolean lost = false;
  private volatile boolean won = false;
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
    add(buttonPanel, BorderLayout.NORTH);

    // JPanel timerPanel = new JPanel();
    // buttonPanel.add(timerPanel);

    // timerLabel = new JLabel();
    // timerPanel.add(timerLabel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayout(1, 2));
    add(mainPanel);

    playerPanel = new PlayerPanel();
    playerPanel.setPreferredSize(new Dimension(gridSize * cellSize + 5, gridSize * cellSize + 1));
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

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  public void deployPlayerShips() 
  {
    statusLabel.setText("Deploy your ships:");
    // Deploying five ships for player
    BattleShip.playerShips = 5;
    for (int i = 1; i <= BattleShip.playerShips; i++) 
    {
      System.out.println("You can't place two or more ships on the same location");
      System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
    }
  }

  public void createBoard() 
  {
    if (timer != null)
      timer.interrupt();
    computerModel = new int[gridSize][gridSize];
    lost = false;
    won = false;
    started = false;
    int x;
    int y;
    for (int i = 0; i < numShips; i++) 
    {
      do 
      {
        x = (int) (Math.random() * (gridSize - 1) + .5);
        y = (int) (Math.random() * (gridSize - 1) + .5);
      } while (computerModel[x][y] != NONE);
      computerModel[x][y] = SHIP;
    }
    printModels();
  }

  private void printModels() 
  {
    System.out.println();
    for (int j = 0; j < gridSize; j++) 
    {
      for (int i = 0; i < gridSize; i++) 
      {
        System.out.print(playerModel[i][j] + " ");
      }
      System.out.println();
    }
    System.out.println();
    for (int j = 0; j < gridSize; j++) 
    {
      for (int i = 0; i < gridSize; i++) 
      {
        System.out.print(computerModel[i][j] + " ");
      }
      System.out.println();
    }
  }

  private class PlayerPanel extends JPanel 
  {
    public void paintComponent(Graphics g) 
    {
      super.paintComponent(g);
      g.setColor(Color.CYAN);
      g.fillRect(0, 0, gridSize * cellSize, gridSize * cellSize);
      g.setColor(Color.BLACK);
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(0, i * cellSize, cellSize * gridSize, i * cellSize);
      }
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(i * cellSize, 0, i * cellSize, cellSize * gridSize);
      }
      if(playerModel==null) return;
      g.setColor(Color.LIGHT_GRAY);
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {
          if (playerModel[i][j] == SHIP) 
          {
            g.fillRect(i * cellSize + 1, j * cellSize + 1, cellSize - 1, cellSize - 1);
          }
        }
      }
      g.setColor(Color.GRAY);
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {
          if (computerGuessGrid[i][j]==1) 
          {
            if(playerModel[i][j]==SHIP) 
            {
              g.setColor(Color.RED);
              
              g.fillRect(i * cellSize + 1, j * cellSize + 1, cellSize - 1, cellSize - 1);
            }
            g.setColor(Color.BLACK);
            g.drawLine(i * cellSize , j * cellSize ,i* cellSize +cellSize,j* cellSize +cellSize);
            g.drawLine(i * cellSize , j * cellSize +cellSize,i* cellSize+cellSize,j* cellSize);
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
      g.fillRect(0, 0, gridSize * cellSize, gridSize * cellSize);
      g.setColor(Color.BLACK);
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(0, i * cellSize, cellSize * gridSize, i * cellSize);
      }
      for (int i = 0; i < gridSize + 1; i++) 
      {
        g.drawLine(i * cellSize, 0, i * cellSize, cellSize * gridSize);
      }
      if(computerModel==null) return;
      g.setColor(Color.GRAY);
      for (int i = 0; i < gridSize; i++) 
      {
        for (int j = 0; j < gridSize; j++) 
        {
          if (playerGuessGrid[i][j]==1) 
          {
            if(computerModel[i][j]==SHIP) 
            {
              g.setColor(Color.RED);
              g.fillRect(i * cellSize + 1, j * cellSize + 1, cellSize - 1, cellSize - 1);
            }
            g.setColor(Color.BLACK);
            g.drawLine(i * cellSize , j * cellSize ,i* cellSize +cellSize,j* cellSize +cellSize);
            g.drawLine(i * cellSize , j * cellSize +cellSize,i* cellSize+cellSize,j* cellSize);
          }
        }
      }
    }
  }

 

  private class ActionHandler implements ActionListener 
  {
    public void actionPerformed(ActionEvent e) 
    {
      if (e.getSource() == newButton) 
      {
        playerModel = new int[10][10];
        computerModel = new int[10][10];
        playerGuessGrid = new int[10][10];
        computerGuessGrid = new int[10][10];
        createBoard();
        addingShips = true;
        detectingShips = false;
        computerAttack = false;
        shipCount = 0;
        guess = 0;
        guessCount = 1;
        playerPanel.repaint();
        computerPanel.repaint();
        statusLabel.setText("Deploy your ships:");
      }
    }
  }

  private class MouseHandler extends MouseAdapter 
  {
    public void mousePressed(MouseEvent e) 
    {
      printModels();
      if (e.getSource() == playerPanel) 
      {
        int i = e.getX() / cellSize;
        int j = e.getY() / cellSize;
        if (addingShips) 
        {
          if(playerModel[i][j]==SHIP) return;
          playerModel[i][j] = SHIP;
          playerPanel.repaint();
          shipCount++;
          if (shipCount == numShips) 
          {
            addingShips = false;
            statusLabel.setText("Your Turn: ATTACK THEIR SHIPS!");
            detectingShips = true;
            return;
          }
        }
      }
      if (e.getSource() == computerPanel) 
      {
        int i = e.getX() / cellSize;
        int j = e.getY() / cellSize;
        if (computerModel[i][j] == SHIP) 
        {
          playerGuessGrid[i][j]=X;
          playerScore++;
          statusLabel.setText("PLAYER: " + playerScore + " COM: " + computerScore + " || " + "You Hit a Ship!");        
          computerPanel.repaint();
          guess++;
        } 
        else 
        {
          playerGuessGrid[i][j]=X;
          statusLabel.setText("PLAYER: " + playerScore + " COM: " + computerScore + " || " + "You Missed!");
          guess++;
        }
        computerPanel.repaint();
        int x;
        int y;
        
        do 
        {
          x = (int) (Math.random() * (gridSize - 1) + .5);
          y = (int) (Math.random() * (gridSize - 1) + .5);
        } while (playerGuessGrid[x][y] != NONE);
            computerGuessGrid[x][y]=X;
            guessCount++;
            playerPanel.repaint();
        if(playerModel[i][j] == SHIP)
        {
          computerScore++;
        }
      }
      if(playerScore == 10)
      {
        statusLabel.setText("You Won!");
      }
      else if(computerScore == 10)
      {
        statusLabel.setText("You Lost! :(");
      }  
    }
  }

  private class TimerThread extends Thread 
  {
    public void run() 
    {
      float msecs = 0;
      long base = System.currentTimeMillis();
      try 
      {
        while (running) 
        {
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
