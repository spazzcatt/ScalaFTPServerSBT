package StandardFTP

import java.io.{BufferedReader, File, InputStreamReader, PrintStream}
import java.net.Socket
import java.security.SecureRandom
import scala.io.StdIn.readLine
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.spec.KeySpec
import scala.xml._

object FTPClientObject {

  val USAGE_STRING = "USAGE:\n" +
    "help ->\t\t\t\t\t\tDisplays usage and syntax\n" +
    "search [filename] ->\t\tSearches FTP server directory for files that name matches 'filename'\n" +
    "request [filename] ->\t\tRequests file from FTP server (credentials may be required)\n" +
    "add [filepath] ->\t\t\tUploads the file at the given filepath to the FTPServer\n" +
    "delete [filename] ->\t\tDeletes the file with given 'filename' (requires credentials)\n" +
    "list [client | server] ->\tLists the current directory contents of client or server respectively\n" +
    "cat [filename] ->\t\t\tOutputs file contents to terminal"

  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def getCredentials(out: PrintStream, in: BufferedReader): Unit ={
    val random = new SecureRandom()
    val salt = new Array[Byte](16)
    random.nextBytes(salt)
    println()
    println("Credentials are required for this action\n") // WARNING: Password is not secure password hash
    println("Username:\t")
    val username = readLine()
    println("Password:\t")
    val password = readLine()
    val spec = new PBEKeySpec(password.toCharArray, salt, 65536, 128)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = factory.generateSecret(spec).getEncoded
    out.print(s"credentials_request username=$username password=${hash}")
    println("Hash Generated: " + hash.toString)

  }

  def main(args: Array[String]): Unit = {
    Thread.sleep(500)
    val socket = new Socket(args(0), args(1).toInt)
    val out = new PrintStream(socket.getOutputStream)
    val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
    println("FTP CLIENT START\n" +
      "-" * 20 + "\n" +
      "Developed for Scala Class COMP 4210 at Otterbein University\n" +
      "Developed by Connor May\n" +
      "Release date: April 2020\n" +
      "-" * 20 + "\n" +
      USAGE_STRING)
    println()
    print("Enter Command: ")
    var input = ""
    while(input != "quit"){
      input = readLine()
      val firstArg = input.split(" ")
      firstArg(0) match {
        case "cat" => {
          val source = scala.io.Source.fromFile(firstArg(1))
          if(source == null){
            System.err.println("Problem opening file... Try again.")
          }else{
            val fileContents = source.getLines mkString "\n"
            println("File Read: \n" + "-" * 20 + "\n" + fileContents)
          }
        }
        case "add" => {
          val source = scala.io.Source.fromFile(firstArg(1))
          if(source == null){
            System.err.println("Problem opening file... Try again.")
          }else{
            val fileContents = source.getLines mkString "\n"
            println("Writing File...")
            val message : =
              <message>
                <command>add</command>
                <filename>$firstArg(1)</filename>
                <filecontents>$fileContents</filecontents>
              </message>
            out.println(message)
            println("Wrote File. Awaiting Confirmation...")
            val confirmation = in.readLine()
            println(s"Received: $confirmation")
          }
        }
        case "list" => {
          println(s"Getting files from: ${firstArg(1)}")
          if(firstArg(1) == "client"){
            val currentFiles =  getListOfFiles(System.getProperty("user.dir"))
            println("Current Files In Client Directory:")
            currentFiles.foreach(f => println(s"${f.getName}"))
            println()

          }
        }
        case _ => println(USAGE_STRING)
      }
      println()
      print("Enter Command: ")
    }
  }



}
