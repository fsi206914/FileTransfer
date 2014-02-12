import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JobTracker {
  /**
   * extract the jar file into the system's ClassPath folder
   * 
   * @param jobid
   * @param jarpath
   * @return
   */
  public boolean extractJobClassJar(int jobid, String jarpath) {
	  try {
		  JarFile jar = new JarFile(jarpath);
		  Enumeration enums = jar.entries();

		  // find the path to which the jar file should be extracted
		  String destDirPath = "./userPath/";
		  File destDir = new File(destDirPath);
		  if (!destDir.exists()) {
			  destDir.mkdirs();
		  }

		  // copy each file in jar archive one by one
		  while (enums.hasMoreElements()) {
			  JarEntry file = (JarEntry) enums.nextElement();

			  File outputfile = new File(destDirPath + file.getName());
			  if (file.isDirectory()) {
				  outputfile.mkdirs();
				  continue;
			  }
			  InputStream is = jar.getInputStream(file);
			  FileOutputStream fos = null;
			  try {
				  fos = new FileOutputStream(outputfile);
			  } catch (FileNotFoundException e) {
				  outputfile.getParentFile().mkdirs();
				  fos = new FileOutputStream(outputfile);
			  }
			  while (is.available() > 0) {
				  fos.write(is.read());
			  }
			  fos.close();
			  is.close();
		  }
	  } catch (IOException e) {
		  // TODO : handle this exception if the jar file cannot be found
		  return false;
	  }

	  return true;
  }

  public static void main(String[] args) {

	JobTracker JT = new JobTracker();
	JT.extractJobClassJar( 12,"./wordcount.jar");
  }

}
