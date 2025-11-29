import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import io.github.bonigarcia.wdm.WebDriverManager
//notes are ass cause im going litterly crazy and wanna be done but this makes one driver for the entire program

object SyncrDriveManager {
    private var internalDriver: ChromeDriver? = null

    val driver: ChromeDriver
        get() = internalDriver ?: createDriver().also { internalDriver = it }


    fun createDriver(): ChromeDriver {
        WebDriverManager.chromedriver().setup()

        val options = ChromeOptions()
        val userHome = System.getProperty("user.home")
        val botProfilePath = "$userHome\\SyncrBotData"
        options.addArguments("user-data-dir=$botProfilePath")
        options.addArguments("--headless=new")
        options.addArguments("--window-size=1920,1080")

        val config = HashMap<String, Any>() //IDK WHAT A HASHMAP IS
        config["download.default_directory"] = DownloaderHelper.getTempPath()
        config["download.prompt_for_download"] = false
        config["profile.default_content_setting_values.automatic_downloads"] = 1 //ts might genuinly the worst name i've ever seen in my life
        options.setExperimentalOption("prefs", config)


        val newDriver = ChromeDriver(options)
        internalDriver = newDriver
        return newDriver
    }

    fun quit() {
        internalDriver?.quit()
        internalDriver = null
    }

}