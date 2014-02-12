import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;

import java.util.List;
import java.util.ArrayList;

public class FolderClient{
	
	File path;
	List<File> pathsList;
	BufferedInputStream in;
	BufferedOutputStream out;
	Socket data = null;
	Socket msg = null;
	String hostname = "localhost";


	public FolderClient(File path) throws Exception{
		this.path = path;
		pathsList = new ArrayList<File>();
		this.listAllFiles(pathsList, path);
	}

	public void listAllFiles(List<File> pathList, File path) throws Exception{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		if (path.isDirectory()){
			for (File f : path.listFiles()){
				if(f.isFile() == true){
					pathsList.add(f.getAbsoluteFile());
				}
				else{
					listAllFiles(pathsList, f);
				}
			}
		}
	}

	public void printAllFilePaths() throws Exception{
		if(pathsList.size() > 0)
			for(int i =0; i<pathsList.size(); i++)
				System.out.println(pathsList.get(i).getAbsolutePath());
		else
			throw new FileNotFoundException(path.getAbsolutePath());
	}

	public void write() {

		try {
			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
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

	public void cleanUp() {
		try {
			out.flush();
			in.close();
			out.close();
		} catch (IOException e) {
			System.out.println(e + "...");
		}
	}

	public void go() {



		BufferedReader msgIn = null;
		PrintWriter msgOut = null;

		try{

		System.out.println("Uploading the file now.");

		for(int i=0; i<pathsList.size(); i++){

			try{
				data = new Socket(InetAddress.getByName(hostname), 4444);
				msg = new Socket(InetAddress.getByName(hostname), 4445);
			} catch (IOException e) {
				System.out.println("Could not listen " + e);
			}


			msgIn = new BufferedReader(new InputStreamReader(msg.getInputStream()));
			msgOut = new PrintWriter(msg.getOutputStream(), true);

			File file = pathsList.get(i);
			String [] parts = file.getAbsolutePath().split("/");
			String saveFile = "./ReducerFiles/"+parts[parts.length-2] + parts[parts.length-1];
			msgOut.println("u:" + file.getName() + ":" + saveFile);
			sendFile(file);

			// issue with waiting for response
			if (msgIn.readLine().equals("200")) {
				System.out.println(file.getAbsolutePath() + " received by server.");
			} else {
				System.out.println("Unsuccessful... Please try again.");
			}
		}
		}catch(IOException ioe){
			ioe.printStackTrace();	
		}
	}


	public static void main(String [] args) throws Exception{

		File folder = new File("/home/liang/Desktop/AnotherDoop/tmp/mapper_output_job_503");
		FolderClient fc = new FolderClient(folder);
		//fc.printAllFilePaths();
		fc.go();
	}
}
