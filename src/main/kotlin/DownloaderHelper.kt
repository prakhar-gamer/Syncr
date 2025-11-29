import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object DownloaderHelper {
    //find location to download the stuff
    private val USER_HOME = System.getProperty("user.home")
    private val ROOT_FOLDER = File(USER_HOME, "SyncrDownloads")
    private val TEMP_FOLDER = File(ROOT_FOLDER, "_TEMP")

    //make sure the folder is created
    fun setup() {
        if (!TEMP_FOLDER.exists()) {
        val create = TEMP_FOLDER.mkdirs()
            if (create){
                println("TEMP FOLDER IS CREATED")
            }
        }
    }

    //gets the Temp folder path
    fun getTempPath(): String {
        return TEMP_FOLDER.absolutePath
    }

    fun download(driver:ChromeDriver, url: String): File? {

        val filesBefore = TEMP_FOLDER.listFiles()?.toSet() ?: emptySet()
        var downloadedFile: File? = null

        try {

            driver.get(url)
            Thread.sleep(3000)

            var attempts = 0

            while (attempts < 20) {
                Thread.sleep(500)

                val filesNow = TEMP_FOLDER.listFiles()?.toSet() ?: emptySet()
                val files = filesNow-filesBefore

                if (files.isNotEmpty()) {
                    val newest = files.maxByOrNull { it.lastModified() } //Stores the file in a list that is by the most recent, the one we downloaded is the most recent

                    if (newest != null && !newest.name.endsWith(".crdownload") && !newest.name.endsWith(".tmp")) {
                        downloadedFile = newest
                        println("FILE WAS DOWNLOADED !!!!!  LFFGGGGG")
                        break
                    }
                }
                attempts++
            }
        }
        catch (e: Exception) {
            println("Error Downloading, ${e.message}")
        }
        return downloadedFile
    }

    fun clearTempFolder() {
        if (TEMP_FOLDER.exists()) {
            TEMP_FOLDER.listFiles()?.forEach {
                it.deleteRecursively()
            }
            println("FOLDER CLEANED")
        }
    }
}