import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.io.*;

public class CheckersGame extends JFrame
{
  private JMenuItem hostItem;
  private JMenuItem joinItem;
  private JMenuItem quitItem;
  private JButton resetButton;
  private JPanel drawingPanel;
  private JLabel statusLabel;
  private int[][] model=new int[20][20];
  private String host="localhost";
  private int port=5000;
  private PrintWriter out=null;
  private boolean connected=false;
  private boolean yourTurn=false;
  private ServerSocket serverSocket;

  public CheckersGame()
  {
    super("Checkers Game");

    MouseHandler mh=new MouseHandler();
    ActionHandler ah=new ActionHandler();

    JMenuBar jmb=new JMenuBar();
    setJMenuBar(jmb);

    JMenu networkMenu=new JMenu("Network");
    jmb.add(networkMenu);

    hostItem=new JMenuItem("Host Game...");
    hostItem.addActionListener(ah);
    networkMenu.add(hostItem);

    joinItem=new JMenuItem("Join Game...");
    joinItem.addActionListener(ah);
    networkMenu.add(joinItem);

    quitItem=new JMenuItem("Quit Game");
    quitItem.addActionListener(ah);
    quitItem.setEnabled(false);
    networkMenu.add(quitItem);

    JPanel buttonPanel=new JPanel();
    add(buttonPanel,BorderLayout.NORTH);

    resetButton=new JButton("Reset");
    resetButton.addActionListener(ah);
    buttonPanel.add(resetButton);

    drawingPanel=new  JPanel()
    {
      public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        for(int j=0;j<20;j++)
        {
          for(int i=0;i<20;i++)
          {
            if(model[i][j]==1)
            {
              g.setColor(Color.BLACK);
              g.fillRect(20*i,20*j,20,20);
            }
            else if(model[i][j]==2)
            {
              g.setColor(Color.RED);
              g.fillRect(20*i,20*j,20,20);
            }
          }
        }
        g.setColor(Color.GRAY);
        for(int i=0;i<=20;i++)
        {
          g.drawLine(0,20*i,400,20*i);
          g.drawLine(20*i,0,20*i,400);
        }
      }
    };
    drawingPanel.setPreferredSize(new Dimension(401,401));
    drawingPanel.addMouseListener(mh);
    add(drawingPanel);

    statusLabel=new JLabel(" ");
    add(statusLabel,BorderLayout.SOUTH);

    setDefaultCloseOperation(EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent e)
    {
      if(e.getSource()==resetButton)
      {
        for(int j=0;j<20;j++)
        {
          for(int i=0;i<20;i++)
          {
            model[i][j]=0;
          }
        }
        if(out!=null) out.println("reset");
        drawingPanel.repaint();
      }
      else if(e.getSource()==hostItem)
      {
        String s=JOptionPane.showInputDialog(CheckersGame.this
                        ,"Enter the port to use",""+port);
        if(s==null) return;
        port=Integer.parseInt(s);
        yourTurn=false;
        hostItem.setEnabled(false);
        joinItem.setEnabled(false);
        quitItem.setEnabled(true);
        new Server().start();
      }
      else if(e.getSource()==joinItem)
      {
        String s=JOptionPane.showInputDialog(CheckersGame.this
                        ,"Enter the hostname",""+host);
        if(s==null) return;
        host=s;
        s=JOptionPane.showInputDialog(CheckersGame.this
                        ,"Enter the port to use",""+port);
        if(s==null) return;
        port=Integer.parseInt(s);
        yourTurn=true;
        hostItem.setEnabled(false);
        joinItem.setEnabled(false);
        quitItem.setEnabled(true);
        new Client().start();
      }
      else if(e.getSource()==quitItem)
      {
        connected=false;
        if(serverSocket!=null)
        {
          try
          {
            serverSocket.close();
            serverSocket=null;
          }
          catch(IOException ioe)
          {
            System.out.println(ioe);
          }
        }
        hostItem.setEnabled(true);
        joinItem.setEnabled(true);
        quitItem.setEnabled(false);
        statusLabel.setText("Game ended.");
        if(out!=null) out.println("quit");
      }
    }
  }

  private class MouseHandler extends MouseAdapter
  {
    public void mousePressed(MouseEvent e)
    {
      if(out==null) return;
      if(!connected) return;
      if(!yourTurn) return;
      int i=e.getX()/20;
      int j=e.getY()/20;
      model[i][j]=1;
      out.println(i+","+j);
      drawingPanel.repaint();
      yourTurn=false;
      statusLabel.setText("Opponent's turn.");
    }
  }

  private class Server extends Thread
  {
    public void run()
    {
      try
      {
        serverSocket=new ServerSocket(port);
        statusLabel.setText("Waiting for opponent to connect...");
        Socket s=serverSocket.accept();
        statusLabel.setText("Opponent's turn.");
        connected=true;
        BufferedReader in=new BufferedReader(new InputStreamReader(
                  s.getInputStream()));
        out=new PrintWriter(s.getOutputStream(),true);
        String line;
        while(connected&&(line=in.readLine())!=null)
        {
          if(line.equals("quit")) break;
          if(line.equals("reset"))
          {
            for(int j=0;j<20;j++)
            {
              for(int i=0;i<20;i++)
              {
                model[i][j]=0;
              }
            }
            drawingPanel.repaint();
            continue;
          }
          String[] sa=line.split(",");
          int i=Integer.parseInt(sa[0]);
          int j=Integer.parseInt(sa[1]);
          model[i][j]=2;
          drawingPanel.repaint();
          yourTurn=true;
          statusLabel.setText("Your turn.");
        }
        connected=false;
        statusLabel.setText("Game ended.");
        if(serverSocket!=null) serverSocket.close();
        serverSocket=null;
        s.close();
      }
      catch(IOException ioe)
      {
        System.out.println(ioe);
      }
      hostItem.setEnabled(true);
      joinItem.setEnabled(true);
      quitItem.setEnabled(false);
    }
  }

  private class Client extends Thread
  {
    public void run()
    {
      try
      {
        Socket s=new Socket(host,port);
        statusLabel.setText("Your turn.");
        connected=true;
        BufferedReader in=new BufferedReader(new InputStreamReader(
                  s.getInputStream()));
        out=new PrintWriter(s.getOutputStream(),true);
        String line;
        while(connected&&(line=in.readLine())!=null)
        {
          if(line.equals("quit")) break;
          if(line.equals("reset"))
          {
            for(int j=0;j<20;j++)
            {
              for(int i=0;i<20;i++)
              {
                model[i][j]=0;
              }
            }
            drawingPanel.repaint();
            continue;
          }
          String[] sa=line.split(",");
          int i=Integer.parseInt(sa[0]);
          int j=Integer.parseInt(sa[1]);
          model[i][j]=2;
          drawingPanel.repaint();
          yourTurn=true;
          statusLabel.setText("Your turn.");
        }
        connected=false;
        statusLabel.setText("Game ended.");
        s.close();
      }
      catch(IOException ioe)
      {
        System.out.println(ioe);
      }
      hostItem.setEnabled(true);
      joinItem.setEnabled(true);
      quitItem.setEnabled(false);
    }
  }

  public static void main(String[] args)
  {
    new CheckersGame();
  }
}