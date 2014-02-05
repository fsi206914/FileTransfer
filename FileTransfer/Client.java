import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Network File Transfer Application
 * Client.java
 * 
 * @author Liang Tang
 * @date Feb 2014
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Client{
	private static final long serialVersionUID = 1L;

	static Socket data = null;
	static Socket msg = null;
	static String hostname = null;

	static BufferedInputStream in = null;
	static BufferedOutputStream out = null;

	static File cd = new File(".");
	static File file = null;
	static String openFile = null;
	static String saveFile = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Client client = new Client("localhost");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void write() {
		try {
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
		} catch (IOException e) {
			System.out.println(e + "...");
		}
	}

	public static void cleanUp() {
		try {
			out.flush();
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println(e + "...");
		}
	}

	public void sendFile(File filename) {

		try {
			in = new BufferedInputStream(new FileInputStream(filename));
			out = new BufferedOutputStream(data.getOutputStream());
			write();
		} catch (IOException e) {
			System.out.println(e + "...");
		} finally {
			cleanUp();
		}
	}

	public void receiveFile(File filename) {

		try {
			// If the file parent folder does not exist, create it.
			if (filename.getParentFile() != null) {
				if (!filename.getParentFile().exists()) {
					filename.getParentFile().mkdirs();
				}
			}
			in = new BufferedInputStream(data.getInputStream());
			out = new BufferedOutputStream(new FileOutputStream(filename));
			write();

		} catch (IOException e) {
			System.out.println(e + "...");
		} finally {
			cleanUp();
		}
	}

	public void go(String action) {
		
		String option = null;
		try {
			data = new Socket(hostname, 4444);
			msg = new Socket(hostname, 4445);
			System.out.println("Client listening...");
		} catch (IOException e) {
			System.out.println("Could not listen " + e);
		}

		BufferedReader msgIn = null;
		PrintWriter msgOut = null;

		try {
			msgIn = new BufferedReader(new InputStreamReader(msg.getInputStream()));
			msgOut = new PrintWriter(msg.getOutputStream(), true);

			if (action.equals("Upload")) {
				System.out.println("Uploading the file now.");

				File file = new File(this.openFile);
				msgOut.println("u:" + file.getName() + ":" + saveFile);
				sendFile(file);

				// TODO issue with waiting for response
				if (msgIn.readLine().equals("200")) {
					System.out.println(file.getName() + " received by server.");
				} else {
					System.out.println("Unsuccessful... Please try again.");
				}

			} else if (action.equals("Download")) {
				System.out.println("Downloading the file now.");
				
				msgOut.println("d:" + openFile + ":" + saveFile);
				receiveFile(new File(saveFile));

			} else if (action.equals("List")) {

				if (option == "Download") {
					msgOut.println("l:" + openFile);
				} else if (option == "Upload") {
					msgOut.println("l:" + saveFile);
				}

				ObjectInputStream objIn = new ObjectInputStream(msg.getInputStream());

				try {
					File[] files = (File[]) objIn.readObject();
					// Disabled due it only working on local server
					//list.setCellRenderer(new ListIconRenderer());
				} catch (ClassNotFoundException e) {
					System.out.println(e + "...");
				}
			} else if (action.equals("Delete")) {

				if (option == "Download") {
					msgOut.println("e:" + openFile);
				} else if (option == "Upload") {
					msgOut.println("e:" + saveFile);
				}
				
				// TODO issue with waiting for response
				if (msgIn.readLine().equals("200")) {
					System.out.println("File deleted.");
				} else {
					System.out.println("Error: File/folder could not be deleted");
				}
			}

		} catch (IOException e) {
			System.out.println(e + "...");
		} catch (NullPointerException e) {
			System.out.println(e + "...");
			e.printStackTrace();
		} finally {
			try {
				data.close();
				msg.close();
			} catch (IOException e) {
				System.out.println(e + "...");
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public Client(String hostname) {
		this.hostname = hostname;
		controlConsole();
	}

	void controlConsole(){

		Scanner scanner = new Scanner(System.in);
		System.out.println(">> ");
		while (scanner.hasNext()) {
			System.out.println(">> ");
			String line = scanner.nextLine().trim();
			String[] fields = line.split(" ");
			String cmd = fields[0];

			if (cmd.compareTo("send") == 0) {
				switch (fields.length) {
					case 2: // ls job
						this.openFile = fields[1];
						this.saveFile = "/home/liang/Desktop/upload";
						go("Upload");
						break;
					default:
						System.out.println("Invalid command, type 'help' to see the mannul.");
						break;
				}

			}else if(cmd.compareTo("download") == 0) {
				switch (fields.length) {
					case 2: // ls job
						this.openFile = fields[1];
						this.saveFile = "/home/liang/Desktop/download";
						go("Download");
						break;
					default:
						System.out.println("Invalid command, type 'help' to see the mannul.");
						break;
				}

			} else if (cmd.compareTo("quit") == 0) {
				System.exit(0);
			} else if (cmd.compareTo("help") == 0) {

			}
		}
	}
}
