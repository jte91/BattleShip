
//   public BattleShip() {
//     super("BattleShip");

//     ActionHandler ah = new ActionHandler();
//     MouseHandler mh = new MouseHandler();
//     toolkit = getToolkit();

//     JMenuBar jmb = new JMenuBar();
//     setJMenuBar(jmb);

//     JMenu networkMenu = new JMenu("Network");
//     jmb.add(networkMenu);

//     hostItem = new JMenuItem("Host Game...");
//     hostItem.addActionListener(ah);
//     networkMenu.add(hostItem);

//     joinItem = new JMenuItem("Join Game...");
//     joinItem.addActionListener(ah);
//     networkMenu.add(joinItem);

//     quitItem = new JMenuItem("Quit Game");
//     quitItem.addActionListener(ah);
//     quitItem.setEnabled(false);
//     networkMenu.add(quitItem);

//     JPanel buttonPanel = new JPanel();
//     buttonPanel.setLayout(new GridLayout(2,1));
//     add(buttonPanel, BorderLayout.NORTH);

//     JPanel timerPanel=new JPanel();
//     buttonPanel.add(timerPanel);

//     timerLabel=new JLabel();
//     timerPanel.add(timerLabel);

//     //error occured
//     gamePanel=new JPanel();
//     JPanel newPanel=new JPanel();
//     buttonPanel.add(newPanel);

//     newButton=new JButton("New Game");
//     newButton.addActionListener(ah);
//     newPanel.add(newButton);

//     resetButton = new JButton("Reset");
//     resetButton.addActionListener(ah);
//     buttonPanel.add(resetButton);

//     drawingPanel = new JPanel() {
//       public void paintComponent(Graphics g) {
//         super.paintComponent(g);
//         for (int j = 0; j < 20; j++) {
//           for (int i = 0; i < 20; i++) {
//             if (model[i][j] == 1) {
//               g.setColor(Color.BLACK);
//               g.fillRect(20 * i, 20 * j, 20, 20);
//             }
//           }
//         }
//         g.setColor(Color.GRAY);
//         for (int i = 0; i <= 20; i++) {
//           g.drawLine(0, 20 * i, 400, 20 * i);
//           g.drawLine(20 * i, 0, 20 * i, 400);
//         }
//       }
//     };
//     drawingPanel.setPreferredSize(new Dimension(401, 401));
//     drawingPanel.addMouseListener(mh);
//     add(drawingPanel);

//     statusLabel = new JLabel(" ");
//     add(statusLabel, BorderLayout.SOUTH);

//     setDefaultCloseOperation(EXIT_ON_CLOSE);
//     pack();
//     setVisible(true);
//   }

//   private class ActionHandler implements ActionListener {
//     public void actionPerformed(ActionEvent e) {
//       if (e.getSource() == resetButton) {
//         for (int j = 0; j < 20; j++) {
//           for (int i = 0; i < 20; i++) {
//             model[i][j] = 0;
//           }
//         }
//         if (out != null)
//           out.println("reset");
//         drawingPanel.repaint();
//       } else if (e.getSource() == hostItem) {
//         String s = JOptionPane.showInputDialog(BattleShip.this, "Enter the port to use", "" + port);
//         if (s == null)
//           return;
//         port = Integer.parseInt(s);
//         yourTurn = false;
//         hostItem.setEnabled(false);
//         joinItem.setEnabled(false);
//         quitItem.setEnabled(true);
//         new Server().start();
//       } else if (e.getSource() == joinItem) {
//         String s = JOptionPane.showInputDialog(BattleShip.this, "Enter the hostname", "" + host);
//         if (s == null)
//           return;
//         host = s;
//         s = JOptionPane.showInputDialog(BattleShip.this, "Enter the port to use", "" + port);
//         if (s == null)
//           return;
//         port = Integer.parseInt(s);
//         yourTurn = true;
//         hostItem.setEnabled(false);
//         joinItem.setEnabled(false);
//         quitItem.setEnabled(true);
//         new Client().start();
//       } else if (e.getSource() == quitItem) {
//         connected = false;
//         if (serverSocket != null) {
//           try {
//             serverSocket.close();
//             serverSocket = null;
//           } catch (IOException ioe) {
//             System.out.println(ioe);
//           }
//         }
//         hostItem.setEnabled(true);
//         joinItem.setEnabled(true);
//         quitItem.setEnabled(false);
//         statusLabel.setText("Game ended.");
//         if (out != null)
//           out.println("quit");
//       }
//     }
//   }

//   private class MouseHandler extends MouseAdapter {
//     public void mousePressed(MouseEvent e) {
//       if (out == null)
//         return;
//       if (!connected)
//         return;
//       if (!yourTurn)
//         return;
//       int i = e.getX() / 20;
//       int j = e.getY() / 20;
//       model[i][j] = 1;
//       out.println(i + "," + j);
//       drawingPanel.repaint();
//       yourTurn = false;
//       statusLabel.setText("Opponent's turn.");
//     }
//   }

//   private class Server extends Thread {
//     public void run() {
//       try {
//         serverSocket = new ServerSocket(port);
//         statusLabel.setText("Waiting for opponent to connect...");
//         Socket s = serverSocket.accept();
//         statusLabel.setText("Opponent's turn.");
//         connected = true;
//         BufferedReader in = new BufferedReader(new InputStreamReader(
//             s.getInputStream()));
//         out = new PrintWriter(s.getOutputStream(), true);
//         String line;
//         while (connected && (line = in.readLine()) != null) {
//           if (line.equals("quit"))
//             break;
//           if (line.equals("reset")) {
//             for (int j = 0; j < 20; j++) {
//               for (int i = 0; i < 20; i++) {
//                 model[i][j] = 0;
//               }
//             }
//             drawingPanel.repaint();
//             continue;
//           }
//           String[] sa = line.split(",");
//           int i = Integer.parseInt(sa[0]);
//           int j = Integer.parseInt(sa[1]);
//           model[i][j] = 2;
//           drawingPanel.repaint();
//           yourTurn = true;
//           statusLabel.setText("Your turn.");
//         }
//         connected = false;
//         statusLabel.setText("Game ended.");
//         if (serverSocket != null)
//           serverSocket.close();
//         serverSocket = null;
//         s.close();
//       } catch (IOException ioe) {
//         System.out.println(ioe);
//       }
//       hostItem.setEnabled(true);
//       joinItem.setEnabled(true);
//       quitItem.setEnabled(false);
//     }
//   }

//   private class Client extends Thread {
//     public void run() {
//       try {
//         Socket s = new Socket(host, port);
//         statusLabel.setText("Your turn.");
//         connected = true;
//         BufferedReader in = new BufferedReader(new InputStreamReader(
//             s.getInputStream()));
//         out = new PrintWriter(s.getOutputStream(), true);
//         String line;
//         while (connected && (line = in.readLine()) != null) {
//           if (line.equals("quit"))
//             break;
//           if (line.equals("reset")) {
//             for (int j = 0; j < 20; j++) {
//               for (int i = 0; i < 20; i++) {
//                 model[i][j] = 0;
//               }
//             }
//             drawingPanel.repaint();
//             continue;
//           }
//           String[] sa = line.split(",");
//           int i = Integer.parseInt(sa[0]);
//           int j = Integer.parseInt(sa[1]);
//           model[i][j] = 2;
//           drawingPanel.repaint();
//           yourTurn = true;
//           statusLabel.setText("Your turn.");
//         }
//         connected = false;
//         statusLabel.setText("Game ended.");
//         s.close();
//       } catch (IOException ioe) {
//         System.out.println(ioe);
//       }
//       hostItem.setEnabled(true);
//       joinItem.setEnabled(true);
//       quitItem.setEnabled(false);
//     }
//   }

//   public static void main(String[] args) {
//     new BattleShip();
//   }
// }
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class BattleShip extends JFrame {
  private JPanel gamePanel;
  private JMenuItem hostItem;
  private JMenuItem joinItem;
  private JMenuItem quitItem;
  private JButton newButton;
  private JButton resetButton;
  private JPanel drawingPanel;
  private JLabel statusLabel;
  private int[][] model = new int[20][20];
  private String host = "localhost";
  private Toolkit toolkit;
  private int port = 5000;
  private PrintWriter out = null;
  private boolean connected = false;
  private boolean yourTurn = false;
  private ServerSocket serverSocket;
  private boolean debug = false;
  // private TimerThread timer = null;
  private JLabel timerLabel;
  private AudioInputStream ais;
  private AudioFormat[] fmt;
  public static int numRows = 10;
  public static int numCols = 10;
  public static int playerShips;
  public static int computerShips;
  public static String[][] grid = new String[numRows][numCols];
  public static int[][] missedGuesses = new int[numRows][numCols];

  public static void main(String[] args) {
    System.out.println("**** Welcome to Battle Ships game ****");
    System.out.println("Right now, sea is empty\n");

    // Step 1 – Create the ocean map
    createOceanMap();

    // Step 2 – Deploy player’s ships
    deployPlayerShips();

    // Step 3 - Deploy computer's ships
    deployComputerShips();

    // Step 4 Battle
    do {
      Battle();
    } while (BattleShip.playerShips != 0 && BattleShip.computerShips != 0);

    // Step 5 - Game over
    gameOver();
  }

  public static void createOceanMap() {
    // First section of Ocean Map
    System.out.print("  ");
    for (int i = 0; i < numCols; i++)
      System.out.print(i);
    System.out.println();

    // Middle section of Ocean Map
    for (int i = 0; i < grid.length; i++) {
      for (int j = 0; j < grid[i].length; j++) {
        grid[i][j] = " ";
        if (j == 0)
          System.out.print(i + "|" + grid[i][j]);
        else if (j == grid[i].length - 1)
          System.out.print(grid[i][j] + "|" + i);
        else
          System.out.print(grid[i][j]);
      }
      System.out.println();
    }

    // Last section of Ocean Map
    System.out.print("  ");
    for (int i = 0; i < numCols; i++)
      System.out.print(i);
    System.out.println();
  }

  public static void deployPlayerShips() {
    Scanner input = new Scanner(System.in);

    System.out.println("\nDeploy your ships:");
    // Deploying five ships for player
    BattleShip.playerShips = 5;
    for (int i = 1; i <= BattleShip.playerShips;) {
      System.out.print("Enter X coordinate for your " + i + " ship: ");
      int x = input.nextInt();
      System.out.print("Enter Y coordinate for your " + i + " ship: ");
      int y = input.nextInt();

      if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == " ")) {
        grid[x][y] = "@";
        i++;
      } else if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && grid[x][y] == "@")
        System.out.println("You can't place two or more ships on the same location");
      else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols))
        System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
    }
    printOceanMap();
  }

  public static void deployComputerShips() {
    System.out.println("\nComputer is deploying ships");
    // Deploying five ships for computer
    BattleShip.computerShips = 5;
    for (int i = 1; i <= BattleShip.computerShips;) {
      int x = (int) (Math.random() * 10);
      int y = (int) (Math.random() * 10);

      if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols) && (grid[x][y] == " ")) {
        grid[x][y] = "x";
        System.out.println(i + ". ship DEPLOYED");
        i++;
      }
    }
    printOceanMap();
  }

  public static void Battle() {
    playerTurn();
    computerTurn();

    printOceanMap();

    System.out.println();
    System.out.println("Your ships: " + BattleShip.playerShips + " | Computer ships: " + BattleShip.computerShips);
    System.out.println();
  }

  public static void playerTurn() {
    System.out.println("\nYOUR TURN");
    int x = -1, y = -1;
    do {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter X coordinate: ");
      x = input.nextInt();
      System.out.print("Enter Y coordinate: ");
      y = input.nextInt();

      if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols)) // valid guess
      {
        if (grid[x][y] == "x") // if computer ship is already there; computer loses ship
        {
          System.out.println("Boom! You sunk the ship!");
          grid[x][y] = "!"; // Hit mark
          --BattleShip.computerShips;
        } else if (grid[x][y] == "@") {
          System.out.println("Oh no, you sunk your own ship :(");
          grid[x][y] = "x";
          --BattleShip.playerShips;
          ++BattleShip.computerShips;
        } else if (grid[x][y] == " ") {
          System.out.println("Sorry, you missed");
          grid[x][y] = "-";
        }
      } else if ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)) // invalid guess
        System.out.println("You can't place ships outside the " + numRows + " by " + numCols + " grid");
    } while ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)); // keep re-prompting till valid guess
  }

  public static void computerTurn() {
    System.out.println("\nCOMPUTER'S TURN");
    // Guess co-ordinates
    int x = -1, y = -1;
    do {
      x = (int) (Math.random() * 10);
      y = (int) (Math.random() * 10);

      if ((x >= 0 && x < numRows) && (y >= 0 && y < numCols)) // valid guess
      {
        if (grid[x][y] == "@") // if player ship is already there; player loses ship
        {
          System.out.println("The Computer sunk one of your ships!");
          grid[x][y] = "x";
          --BattleShip.playerShips;
          ++BattleShip.computerShips;
        } else if (grid[x][y] == "x") {
          System.out.println("The Computer sunk one of its own ships");
          grid[x][y] = "!";
        } else if (grid[x][y] == " ") {
          System.out.println("Computer missed");
          // Saving missed guesses for computer
          if (missedGuesses[x][y] != 1)
            missedGuesses[x][y] = 1;
        }
      }
    } while ((x < 0 || x >= numRows) || (y < 0 || y >= numCols)); // keep re-prompting till valid guess
  }

  public static void gameOver() {
    System.out.println("Your ships: " + BattleShip.playerShips + " | Computer ships: " + BattleShip.computerShips);
    if (BattleShip.playerShips > 0 && BattleShip.computerShips <= 0)
      System.out.println("Hooray! You won the battle :)");
    else
      System.out.println("Sorry, you lost the battle");
    System.out.println();
  }

  public static void printOceanMap() {
    System.out.println();
    // First section of Ocean Map
    System.out.print("  ");
    for (int i = 0; i < numCols; i++)
      System.out.print(i);
    System.out.println();

    // Middle section of Ocean Map
    for (int x = 0; x < grid.length; x++) {
      System.out.print(x + "|");

      for (int y = 0; y < grid[x].length; y++) {
        System.out.print(grid[x][y]);
      }

      System.out.println("|" + x);
    }

    // Last section of Ocean Map
    System.out.print("  ");
    for (int i = 0; i < numCols; i++)
      System.out.print(i);
    System.out.println();
  }
}