
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.ScrollPaneConstants;

/**
 * GUI to draw robot path and current location of the robot. Uses mouse to
 * enter x,y coordinates and transmit commands from the GUI to the robot
 * via Bluetooth.
 * @author Corey Short, Phuoc Nguyen, Khoa Tran.
 * Updated docstring, comments, and separated constructor into modular methods. 5/25/14.
 * Created on 12/9/13.
 * Reference to Glassey OffScreenGrid.java and Milestone 5 sample GUI.
 */
public class MissionControlGUI extends JFrame implements CommListener {

	private JPanel contentPane, topPanel, centerPanel;
	private JTextField nameField, xField, yField, xField2, yField2; 
	private JTextField headingField, echoField, amountField, statusField;
	private JTextArea coordListTextArea;
	private JScrollPane scrollPane;
	private JLabel lblDataY, lblPoseY, lblAngle, lblHeading, timerLabel;
	private JLabel lblDataX, lblPoseX, lblEcho, lblStatus, lblStatusArea;
	private JButton stopButton, setPoseButton, gotoButton, map1Button, map2Button;
	private JButton fixButton, travelButton, rotateButton, rotateToButton, echoButton;
	private JButton connectButton, map3Button, grabBombButton;
	
	private GridControlCommunicator communicator = new GridControlCommunicator(this);
	private OffScreenDrawing oSGrid = new OffScreenDrawing();
	
	/**
	 * Launch the front-end Mission Control UI application.
	 * @author Short
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MissionControlGUI frame = new MissionControlGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Creates the multiple frames, components, and layouts needed for the MissionControl UI.
	 * 
	 * If a JButton or the map drawn in OffScreenDrawing.java is clicked by the user,
	 * the appropriate ActionListeners are implemented and communication to the NXT robot is
	 * sent with the respective data and message type from MessageType.java.
	 * 
	 * Data is passed to GridControlCommunicator.java to interact with 
	 * the NXT robot back-end.
	 * @author Corey Short
	 */
	public MissionControlGUI() {
		// Sets the title for the UI window and the bounds. 
		setTitle("Mission Control");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(20, 20, 1550, 800);
		
		// Creates the main JPanel, contentPane, with a BorderLayout to store
		// a JPanel in the North and Center sections of the UI.
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		// Creates a JPanel, topPanel, with a GridLayout to be added to 
		// the contentPane's NORTH section.
		topPanel = new JPanel();
		topPanel.setBounds(new Rectangle(0, 0, 240, 480));
		contentPane.add(topPanel, BorderLayout.NORTH);
		topPanel.setLayout(new GridLayout(3, 1, 0, 0));
		
		// Creates the JButtons, JTextFields, and ActionListener's to be
		// used in the topPanel section.
		createConnectPanelAndComponents();
		createMessageSendPanelAndComponents();
		createStatusPanelAndComponents();
		
		// Creates a JPanel, centerPanel, with a BorderLayout to be added to 
		// the contentPane's CENTER section.
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout(0,0));
		centerPanel.add(oSGrid);	// adds offScreenDrawing to center of UI.
		contentPane.add(centerPanel, BorderLayout.CENTER);
		
		// Creates the scrollable JTextArea, coordListTextArea, in the centerPanel's
		// EAST section to display the current robot position, heading, standard
		// deviation, objects mapped via the map left right or explore buttons,
		// and the get echo button.
		createEastPanelAndComponents();

		// Creates a JPanel, timerPanel, and the necessary private classes and methods
		// to add a timer to the centerPanel's NORTH section.
		createTimerPanelAndComponents();
		
		new GUIDisplayTimer(this).start(); // GUI timer
		
		oSGrid.textX = this.xField;
		oSGrid.textY = this.yField;
		
	}
	
	/**
	 * Creates a JPanel, connectPanel, with a FlowLayout to be added to the topPanel.
	 * Buttons to connect the the back-end NXT robot, view the current x and y coordinates
	 * of the robot position and heading, and fields to enter an amount to travel or
	 * send a ping to are implemented here. 
	 * No exceptions currently raised for user-error in data entry.
	 * @author Short
	 */
	private void createConnectPanelAndComponents() {
		JPanel connectPanel = new JPanel();
		topPanel.add(connectPanel);
		
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectButtonActionListener());
		connectPanel.setLayout(new FlowLayout());
		connectPanel.add(connectButton);
		
		nameField = new JTextField();
		nameField.setColumns(10);
		connectPanel.add(nameField);
		
		lblDataX = new JLabel("Data X");
		connectPanel.add(lblDataX);
		
		// Displays the x-coord of a mouse click on the offScreenDrawing UI.
		// Editable - Can be used by the GO TO, MAP LEFT, and MAP RIGHT buttons.
		// Automatically updates and displays the robot position when coordinates
		// are sent from the NXT robot to the PC UI.
		xField = new JTextField();
		xField.setColumns(10);
		connectPanel.add(xField);
		
		lblDataY = new JLabel("Data Y");
		connectPanel.add(lblDataY);
		
		// Displays the y-coord of a mouse click on the offScreenDrawing UI.
		// Editable - Can be used by the GO TO, MAP LEFT, and MAP RIGHT buttons.
		// Automatically updates and displays the robot position when coordinates
		// are sent from the NXT robot to the PC UI.
		yField = new JTextField();
		yField.setColumns(10);
		connectPanel.add(yField);
		
		lblAngle = new JLabel("Amount");
		connectPanel.add(lblAngle);
		
		// Editable - Can be used by the MAP EXPLORE, TRAVEL, and ROTATE buttons.
		amountField = new JTextField();
		amountField.setColumns(5);
		connectPanel.add(amountField);
		
		lblPoseX = new JLabel("Pose X");
		connectPanel.add(lblPoseX);
		
		// Used to enter the x-coordinate position for the Set Pose button.
		xField2 = new JTextField();
		xField2.setColumns(5);
		connectPanel.add(xField2);

		lblPoseY = new JLabel("Pose Y");
		connectPanel.add(lblPoseY);
		
		// Used to enter the y-coordinate position for the Set Pose button.
		yField2 = new JTextField();
		yField2.setColumns(5);
		connectPanel.add(yField2);
		
		lblHeading = new JLabel("Heading");
		connectPanel.add(lblHeading);
		
		// Used to enter the current robot heading for the Set Pose button.
		// Automatically updates and displays the robot heading when the heading
		// is sent from the NXT robot to the PC UI.
		headingField = new JTextField();
		headingField.setColumns(5);
		connectPanel.add(headingField);
		
		lblEcho = new JLabel("Enter echo");
		connectPanel.add(lblEcho);
		
		// Used to enter an angle to send a ping to based on the robot's current heading.
		echoField = new JTextField();
		echoField.setColumns(5);
		connectPanel.add(echoField);
	}
	
	/**
	 * Creates a JPanel, messegeSendPanel, with a FlowLayout to be added to the topPanel.
	 * The grabBomb, disconnect, stop, go to, map left, map right, map explore, set pose,
	 * fix, travel, rotate, rotate to, and get echo JButtons and ActionListener's are
	 * implemented here.
	 */
	private void createMessageSendPanelAndComponents() {
		JPanel messegeSendPanel = new JPanel();
		topPanel.add(messegeSendPanel);

		grabBombButton = new JButton("Grab Bomb");
		grabBombButton.addActionListener(new GrabBombButtonActionListener());
		messegeSendPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		messegeSendPanel.add(grabBombButton);
		
		JButton disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(new DisconnectButtonActionListener());
		messegeSendPanel.add(disconnectButton);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(new StopButtonActionListener());
		messegeSendPanel.add(stopButton);
		
		gotoButton = new JButton("GO TO");
		gotoButton.addActionListener(new GoToButtonActionListener());
		messegeSendPanel.add(gotoButton);
		
		map1Button = new JButton("map left");
		map1Button.addActionListener(new MapLeftButtonActionListener());
		messegeSendPanel.add(map1Button);
		
		map2Button = new JButton("map right");
		map2Button.addActionListener(new MapRightButtonActionListener());
		messegeSendPanel.add(map2Button);
		
		map3Button = new JButton("map explore");
		map3Button.addActionListener(new MapExploreButtonActionListener());
		messegeSendPanel.add(map3Button);
		
		setPoseButton = new JButton("Set Pose");
		setPoseButton.addActionListener(new SetPoseButtonActionListener());
		messegeSendPanel.add(setPoseButton);
		
		fixButton = new JButton("Fix");
		fixButton.addActionListener(new FixButtonActionListener());
		messegeSendPanel.add(fixButton);
		
		travelButton = new JButton("Travel");
		travelButton.addActionListener(new TravelButtonActionListener());
		messegeSendPanel.add(travelButton);
		
		rotateButton = new JButton("Rotate");
		rotateButton.addActionListener(new RotateButtonActionListener());
		messegeSendPanel.add(rotateButton);
		
		rotateToButton = new JButton("Rotate To");
		rotateToButton.addActionListener(new RotateToButtonActionListener());
		messegeSendPanel.add(rotateToButton);
		
		echoButton = new JButton("Get echo");
		echoButton.addActionListener(new EchoButtonActionListener());
		messegeSendPanel.add(echoButton);
	}
	
	/**
	 * Creates a JPanel, statusPanel, to be added to the topPanel.
	 * Used to keep track of last robot position or mapped object.
	 * Functionality kept; however, Coordinate List is more useful in application.
	 */
	private void createStatusPanelAndComponents() {
		JPanel statusPanel = new JPanel();
		topPanel.add(statusPanel);

		lblStatus = new JLabel("Status");
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 13));
		statusPanel.add(lblStatus);

		statusField = new JTextField();
		statusField.setEditable(false);
		statusField.setColumns(35);
		statusPanel.add(statusField);
	}

	/**
	 * Creates a JPanel, eastPanel, with a BoxLayout to be added to the centerPanel.
	 * Implements a JTextArea, coordListTextArea, to automatically scroll down and display
	 * the robot's updated postion, standard deviation, and the coordinates of objects
	 * mapped with any of the map butons or the get echo button.
	 */
	private void createEastPanelAndComponents() {
		JPanel eastPanel = new JPanel();
		eastPanel.setBackground(Color.black);
		eastPanel.setBorder(new EmptyBorder(100, 0, 0, 0));
		eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
		centerPanel.add(eastPanel, BorderLayout.EAST);
		
		lblStatusArea = new JLabel("Coordinate List");
		lblStatusArea.setForeground(Color.white);
		eastPanel.add(lblStatusArea);
		
		coordListTextArea = new JTextArea(15, 21);
		coordListTextArea.setBackground(Color.black);
		coordListTextArea.setForeground(Color.white);
		coordListTextArea.setEditable(false);
		
		scrollPane = new JScrollPane();
		scrollPane.setBackground(Color.black);
		scrollPane.setBorder(new EmptyBorder(10, 0, 50, 20));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
	        public void adjustmentValueChanged(AdjustmentEvent event) {  
	            event.getAdjustable().setValue(event.getAdjustable().getMaximum());  
	        }
	    });
		scrollPane.setViewportView(coordListTextArea);
		eastPanel.add(scrollPane);
	}
	
	/**
	 * Creates a JPanel, timerPanel, to be added to the centerPanel's NORTH section.
	 */
	private void createTimerPanelAndComponents() {
		timerLabel = new JLabel("", SwingConstants.CENTER);
		timerLabel.setForeground(Color.white);
		
		JPanel timerPanel = new JPanel();
		timerPanel.setBackground(Color.black);
	
		JLabel timerstatuslabel = new JLabel("time: ");
		timerstatuslabel.setForeground(Color.white);
		timerPanel.add(timerstatuslabel);
		timerPanel.add(timerLabel);
		
		JPanel centernorthpanel = new JPanel();
		centernorthpanel.setLayout(new BorderLayout());
		
		centerPanel.add(timerPanel, BorderLayout.NORTH);
	}
	
	/**
	 * Private class to display a timer on the UI for the final demonstration.
	 * A start button for this was not implemented and the timer automatically begins
	 * when the UI launches. 
	 * @author Short
	 */
	private class GUIDisplayTimer {
		private static final int TIMER_PERIOD = 1000;
		private MissionControlGUI control; // holds a reference to the Welcome class
		private int seconds = 0;
		private int minutes = 0;
			
		public GUIDisplayTimer(MissionControlGUI control) {
			this.control = control; // initializes the reference to the Welcome class.
		    String text = "" + minutes +":" + seconds;
		    control.setTimerLabelText(text);
		}

		public void start() {
			new Timer(TIMER_PERIOD, new ActionListener() {
				
		         @Override
		         public void actionPerformed(ActionEvent e) {
		        	 if (seconds == 60) {
		        		 minutes += 1;
		        		 seconds = 0;
		        	 }
		        	 seconds %= 6000;
		        	 seconds++;
		        	 String text = "" + minutes +":" + seconds;
		        	 control.setTimerLabelText(text);
		         }
		   }).start();
		}
	}
	
	/**
	 * A method to set the timer text.
	 * @param text
	 * @author Short
	 */
	public void setTimerLabelText(String text) {
		timerLabel.setText(text);
	}
	
	/**
	 * ActionListener that pairs the PC and NXT together.
	 * @author Short
	 */
	private class ConnectButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String name = nameField.getText();
			System.out.println("* Trying to connect to " + name);
			communicator.connect(name);
		}
	}
	
	/**
	 * ActionListener that disconnects the PC UI from the NXT.
	 * @author Short
	 */
	private class DisconnectButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String name = nameField.getText();
			System.out.println("* Trying to disconnect to " + name);
			communicator.sendDisconnect();
		}
	}
	
	/**
	 * ActionListener that sends the coordinates for the NXT to goto.
	 * @author Short
	 */
	private class GoToButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Send button pressed.");
			sendGoto();
		}
	}
	
	/**
	 * ActionListener that sets a pose on the GUI for the nxt.
	 * @author Short
	 */
	private class SetPoseButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Set pose button pressed.");
			sendSetPose();
		}
	}
	
	/**
	 * ActionListener that gets the echo distance to an object.
	 * @author Short
	 */
	private class EchoButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Echo button pressed.");
			sendEcho();
		}
	}
	
	/**
	 * ActionListener that gets the echo distance to an object.
	 * @author Short
	 */
	private class GrabBombButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Grab bomb button pressed.");
			sendGrabBomb();
		}
	}
	
	/**
	 * ActionListener that sends the command for the NXT to stop any of its action's.
	 * @author Short
	 */
	private class StopButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Stop button pressed.");
			sendStop();
		}
	}
	
	/**
	 * ActionListener that tells the NXT to travel in a straight line a given distance.
	 * @author Short
	 */
	private class TravelButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Travel button pressed.");
			sendTravel();
		}
	}
	
	/**
	 * ActionListener that rotates the NXT by a given amount.
	 * @author Short
	 */
	private class RotateButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Rotate button pressed.");
			sendRotate();
		}
	}
	
	/**
	 * ActionListener that rotates the NXT to a specified angle.
	 * @author Short
	 */
	private class RotateToButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Rotate To button pressed.");
			sendRotateTo();
		}
	}
	
	/**
	 * ActionListener that fixes the position of the NXT on the GUI.
	 * @author Short
	 */
	private class FixButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Fix button pressed.");
			sendFix();
		}
	}
	
	/**
	 * ActionListener that rotates scanner 90 degrees and draws a map of the
	 * wall detected on GUI.
	 */
	private class MapLeftButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Map left button pressed.");
			sendMapLeft();
		}
	}
	
	/**
	 * ActionListener that rotates scanner -90 degrees and draws a map of the 
	 * wall detected on GUI.
	 */
	private class MapRightButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Map right button pressed.");
			sendMapRight();
		}
	}
	/**
	 * ActionListener that rotates scanner from 90 to -90 and draws a 
	 * map of the wall detected on the GUI.
	 */
	private class MapExploreButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			System.out.println("Map explore button pressed.");
			sendMapExplore();
		}
	}
	
	
	
	/**
	 * Sends a ping to the communicator to get an echo distance.
	 */
	public void sendEcho() {
		float angle = 0;
		
		try {
			angle = Float.parseFloat(echoField.getText());
			System.out.println(" get angle " + angle);
		} catch (Exception e) {
			setMessage("Problem with Angle Field");
			return;
		}
		communicator.sendEcho(angle);
		repaint();
	}
	
	/**
	 * Sends a ping to the communicator to get an echo distance.
	 */
	public void sendGrabBomb() {
		communicator.sendGrabBomb();
		repaint();
	}
	
	/**
	 * Sends the destination to move to the communicator.
	 */
	public void sendGoto() {
		float x = 0;
		float y = 0;

		try {
			x = Float.parseFloat(xField.getText());
			System.out.println(" get x " + x);
		} catch (Exception e) {
			setMessage("Problem with X field");
			return;
		}

		try {
			y = Float.parseFloat(yField.getText());
			System.out.println(" get y " + y);
		} catch (Exception e) {
			setMessage("Problem  with Y field");
			return;
		}

		communicator.sendGoto(x, y);
		repaint();
	}

	/**
	 * Sends the pose to the communicator.
	 * Sends the x and y coordinates and the heading.
	 */
	public void sendSetPose() {
		float x = 0;
		float y = 0;
		float heading = 0;
		
		try {
			x = Float.parseFloat(xField2.getText());
			System.out.println(" get x " + x);
		} catch (Exception e) {
			setMessage("Problem with X field");
			return;
		}

		try {
			y = Float.parseFloat(yField2.getText());
			System.out.println(" get y " + y);
		} catch (Exception e) {
			setMessage("Problem  with Y field");
			return;
		}
		
		try {
			heading = Float.parseFloat(headingField.getText());
			System.out.println(" get heading " + heading);
		} catch (Exception e) {
			setMessage("Problem  with Heading field");
			return;
		}
		
		communicator.sendSetPose(x, y, heading);
		repaint();
	}
	
	/**
	 * Sends the stop command message to the communicator.
	 */
	public void sendStop() {
		communicator.sendStop();
		repaint();
	}
	
	/**
	 * Sends a fixed position to the communicator.
	 */
	public void sendFix() {
		communicator.sendFix();
		repaint();
	}
	
	/**
	 * Sends the communicator the map left message
	 */
	public void sendMapLeft() {
		float x = 0;
		float y = 0;

		try {
			x = Float.parseFloat(xField.getText());
			System.out.println(" get x " + x);
		} catch (Exception e) {
			setMessage("Problem with X field");
			return;
		}

		try {
			y = Float.parseFloat(yField.getText());
			System.out.println(" get y " + y);
		} catch (Exception e) {
			setMessage("Problem  with Y field");
			return;
		}
		
		communicator.sendMapLeft(x, y, 90f);
		repaint();
	}
	
	/**
	 * Sends the communicator the map right message
	 */
	public void sendMapRight() {
		float x = 0;
		float y = 0;

		try {
			x = Float.parseFloat(xField.getText());
			System.out.println(" get x " + x);
		} catch (Exception e) {
			setMessage("Problem with X field");
			return;
		}

		try {
			y = Float.parseFloat(yField.getText());
			System.out.println(" get y " + y);
		} catch (Exception e) {
			setMessage("Problem  with Y field");
			return;
		}
		communicator.sendMapRight(x, y, -90f);
		repaint();
	}
	
	/**
	 * Sends the communicator the map explore message
	 */
	public void sendMapExplore() {
		float angle = 0;
		try {
			angle = Float.parseFloat(amountField.getText());
			System.out.println(" get dist " + angle);
		}
		catch (Exception e) {
			setMessage("Problem with travel field");
			return;
		}
		
		communicator.sendMapExplore(angle);
		repaint();
	}
	
	/**
	 * Sends the communicator the distance to travel.
	 */
	public void sendTravel() {
		float dist = 0;
		try {
			dist = Float.parseFloat(amountField.getText());
			System.out.println(" get dist " + dist);
		}
		catch (Exception e) {
			setMessage("Problem with travel field");
			return;
		}
		communicator.sendTravel(dist);
		repaint();
	}
	
	/**
	 * Sends the communicator the angle to rotate.
	 */
	public void sendRotate() {
		float angle = 0;
		try {
			angle = Float.parseFloat(amountField.getText());
			System.out.println(" get angle " + angle);
		}
		catch (Exception e) {
			setMessage("Problem with Angle Field");
			return;
		}
		communicator.sendRotate(angle);
		repaint();
	}
	
	/**
	 * Sends the communicator the angle to rotate to.
	 */
	public void sendRotateTo() {
		float angle = 0;
		try {
			angle = Float.parseFloat(amountField.getText());
			System.out.println(" get angle " + angle);
		}
		catch (Exception e) {
			setMessage("Problem with Angle Field");
		}
		communicator.sendRotateTo(angle);
		repaint();
	}
	
	public void setInfo(String message) {
		statusField.setText(message);
	}
	
	/**
	 * Updates the status field message
	 */
	public void setMessage(String message) {
		statusField.setText(message);
	}
	
	/**
	 * Calls offScreenDrawing draw robot path 
	 */
	public void drawRobotPath(int x, int y, int heading) {
		oSGrid.drawRobotPath(x, y, heading);
	}
	
	/**
	 * Used to draw the crash on the GUI
	 */
	public void drawObstacle(int x, int y) {
		oSGrid.drawCrash(x, y);
	}
	
	/**
	 * Used to draw the walls on the GUI
	 * @param x
	 * @param y
	 * @param color
	 */
	public void drawWall(int x, int y, Color color) {
		oSGrid.drawWall(x, y, color);
	}
	
	/**
	 * Method to call offScreenDrawing and draw the standard deviation.
	 * @param x - the current x coordinate of the pose
	 * @param y - the current y coordinate of the pose
	 * @param sDevX - the standard deviation of x
	 * @param sDevY - the standard deviation of y
	 */
	public void drawStdDev(int x, int y, int sDevX, int sDevY) {
		oSGrid.drawStdDev(x, y, sDevX, sDevY);
	}
	
	/**
	 * Method used to draw the bomb of the GUI after the bomb has been captured.
	 * @param x - x coordinate of the bomb
	 * @param y - y coordinatwe of the bomb
	 */
	public void drawBomb(int x, int y) {
		oSGrid.drawBomb(x, y);
	}
	
	/**
	 * Updates the coordinate list with the current pose, standard deviation,
	 * and other messages sent back from the robot
	 * @param message
	 */
	public void updateCoordList(String message) {
		coordListTextArea.append(message + "\n");
	}
	
	/**
	 * Method to update the x and y fields with the robot's current pose
	 * @param x - x pose to update
	 * @param y - y pose to update
	 */
	public void updateXAndYDataFields(float x, float y) {
		xField.setText(Float.toString(x));
		yField.setText(Float.toString(y));
	}
	
}
