import org.openqa.selenium.chrome.ChromeOptions
import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor


data class GCtask(
    val title: String,
    val className : String,
    val dueDate: String,
    val assignmentURL: String
)

fun scrapeAssignedTask(driver: ChromeDriver):  List<GCtask>{
    //makes the array
    val tasksed = mutableListOf<GCtask>()

    try {

       // println("Bot launched")

        //travels to the right website and waits 5 sec
        driver.get("https://classroom.google.com/a/not-turned-in/all")
        Thread.sleep(7000)

      //  println("Opening collapsable rows")
        try {
            val collapsableRow = driver.findElements(By.cssSelector("div[role='region'] button[aria-expanded='false']"))

            if (collapsableRow.isNotEmpty()) {
               // println("found the sneaky button")
                for (button in collapsableRow) {
                    button.click()
                    Thread.sleep(1000)
                    val tasks = driver.findElements(By.cssSelector("[data-include-stream-item-materials]")) //basically looks through the website to see which parts of the webstie has this tag.

                    for (task in tasks) {
                        //looks for common tag between tasks
                        try {
                            val paragraph = task.findElements(By.tagName("p"))
                            if (paragraph.size >= 2) {
                                //scrapes the Info
                                val title = paragraph[0].text.trim()
                                if (!title.isEmpty()) {
                                    val classTitle = paragraph[1].text
                                    val dateText = if (paragraph.size >= 3) {
                                        paragraph[2].text
                                    } else {
                                        "No due Date"
                                    }

                                    var url = "No Link Avaliable"
                                    try {
                                        val foundURL = task.findElement(By.xpath("./ancestor::a"))
                                        url = "${foundURL.getAttribute("href")}"
                                    }
                                    catch ( e: Exception){
                                        println("Error getting url, ${e.message}")
                                    }

                                    tasksed.add(GCtask(title, classTitle, dateText, url))

                                    println("found, $title, $classTitle, $dateText, $url")
                                }
                            }
                        } catch (e: Exception) {
                            println("error in extracting info from individual task, ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error in trying to open/manage collapasable row ${e.message}")
        }
        //println("Done!")
    } catch (e: Exception) {
        println("Error traveling the web, ${e.message}")
    }
    return tasksed
}

fun scrapeMissingTask(driver: ChromeDriver): List<GCtask> {
    val tasksed = mutableListOf<GCtask>()

    try {
        //Bot is launched from ts point onwards
        //println("Bot launched")
        //travels to the right website and waits 5 sec
        driver.get("https://classroom.google.com/a/missing/all")
        //Lets the website load
        Thread.sleep(7000)
        //println("Opening collapsable rows")

        //allows forceful clicking
        val js = driver as JavascriptExecutor
        val viewAllButton =
            driver.findElements(By.cssSelector("div[role='region'] button[aria-expanded='false']")) // helps with the collapsable row
        //println("${viewAllButton.size}") // Tells if it was found

        //for every collapsable it repeats this
        for (i in viewAllButton.indices) {
            try {
                val button = driver.findElements(By.cssSelector("div[role='region'] button[aria-expanded='false']"))
                val clickbutton = button[i]

                //force clicks the element to reveal all Assignments
                js.executeScript("arguments[0].click();", clickbutton)
                Thread.sleep(2000) //to load website

               // println("Trying to find view ALL button")
                val viewAllButton = driver.findElements(By.xpath("//span[text()='View all']/ancestor::button"))
              //  println("${viewAllButton.size}") //just troubleshooting check

                for (button in viewAllButton) {
                    if (button.isDisplayed) {
                        js.executeScript("arguments[0].click();", button) // Clicks the button forcefully
                       // println("found and clicked")
                        Thread.sleep(4000)
                    }
                }

                //tasks is basically the assignment identifier
                val tasks = driver.findElements(By.cssSelector("[data-stream-item-id]"))
                //checks task in the box
                for (task in tasks) {
                    try {
                        if (task.isDisplayed) {
                            //divides up the html into sections, that then I can specifically look for a section
                            val paragraph = task.findElements(By.tagName("p"))
                            if (paragraph.size >= 2) {
                                //scrapes the Info
                                val titleText = paragraph[0].text.trim()
                                if (!titleText.isEmpty()) {
                                    val classTitle = paragraph[1].text.trim()
                                    //checks if it's a duplicate, if it's then it's going to skip
                                    val duplicate =
                                        tasksed.any { it.title == titleText && it.className == classTitle }

                                        var dateText = "No Due Date"
                                        if (paragraph.size >= 3) {
                                            val rawText = paragraph[2].text
                                            if (rawText.contains("Due")) {
                                                dateText = rawText
                                            }
                                        }

                                        var url = "No Link Avaliable"
                                        try {
                                            val foundURL = task.findElement(By.xpath("./ancestor::a"))
                                            url = "${foundURL.getAttribute("href")}"
                                        }
                                        catch ( e: Exception){
                                            println("Error getting url, $e")
                                        }

                                        tasksed.add(GCtask(titleText, classTitle, dateText, url))
                                        println("Found $titleText, $classTitle, $dateText, $url ")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error with trying to get individual task, $e")
                    }
                }
            } catch (e: Exception) {
                println("error with trying to find the view all button, $e")
            }
        }
    }catch (e: Exception) {
        println("error launching the bot, $e")
    }
    return tasksed
}
