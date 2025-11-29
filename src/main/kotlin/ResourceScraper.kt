import org.openqa.selenium.chrome.ChromeOptions
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor


data class Attachment (
    val name: String,
    val type: String,
    val url: String
)

fun resourceFinder(url: String, driver: ChromeDriver): List<Attachment> {

    val doneAttachment = mutableListOf<Attachment>()

    //opens the page
    driver.get(url)
    Thread.sleep(5000)

    try{
        //Responsible for finding Attachments on the page
        val avalAttachment = driver.findElements(By.cssSelector("a[aria-label^= 'Attachment:']"))
        //GC has 2 attachments, per student attachment because they have a thumbnail that uses that too, but that doesn't have the link so we can download later, so don't remove this, cuz this makes sure it's the right one
        val usableAttachment = avalAttachment.distinctBy { it.getAttribute("href") }

        for (attachment in usableAttachment){
            val name = attachment.getAttribute("aria-label")
            val originalUrl = attachment.getAttribute("href") //takes the url

            val result = when {
                name.contains("Google Docs") -> {
                    //makes it a downloadable link
                    val dlUrl = originalUrl.substringBefore("/edit") + "/export?format=txt"
                    Attachment(name, "Doc", dlUrl)
                }

                name.contains("Google Slides") -> {
                    val dlUrl = originalUrl.substringBefore("/edit") + "/export/txt"
                    Attachment(name, "Slides", dlUrl)
                }

                name.contains("PDF") -> {
                    //pdf got no direct download so gotta scrape a download for it later
                    val forceDownloadLink = forceDownloadLink(originalUrl)
                    Attachment(name, "Pdf", forceDownloadLink)
                }

                //microsoft are similar to pdf so gotta like manually download
                name.contains("Word") || name.contains("PowerPoint") -> {
                    //uses the id, and turns it into a download link
                    val forceDownloadLink = forceDownloadLink(originalUrl)
                    Attachment(name, "Ms_Office", forceDownloadLink)
                }

                else -> {
                    Attachment(name, "Link", originalUrl)
                }
            }

            doneAttachment.add(result)
            println("Found ${result.type}, ${result.name}, ${result.url}")
        }
    } catch (e: Exception) {
        println("Error getting the url, ${e.message}")
    }
    return doneAttachment
}

// this is for microsfot documents
fun forceDownloadLink(url: String) : String {
    //looks for the test after better the /d
    val pattern = "/d/([a-zA-Z0-9-_]+)".toRegex()
    val match = pattern.find(url)

    return if (match != null) {
        val id = match.groupValues[1]
        "https://drive.google.com/uc?export=download&id=$id"
    } else {
        url
    }
}


fun massResourceFinder(driver: ChromeDriver, url: String) {
    try {
        driver.get(url)
        Thread.sleep(3000)

        val allAtatchment = driver.findElements(By.cssSelector("a[aria-label^='Attachment:']"))
        for (i in allAtatchment){
            val title = i.getAttribute("aria-label")
            println("$title")
        }
    } catch (e: Exception) {
        println("error in finiding resource, ${e.message}")
    }
}