import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.runBlocking
import java.io.File

object AiTimeEstimator {
    //ADD YOUR OWN!!!
    //TODO DELETE THIS BEFORE UPLOADING
    private const val API_KEY = ""

    private  val generativeModel = GenerativeModel(
        modelName="gemini-2.5-flash",
        apiKey = API_KEY
    )

    fun estimateTime(taskName: String, file: File?) : Int {
        val backupTime = 30
        val aiTime = try {
            val response = runBlocking {
                generativeModel.generateContent(
                    content {
                        text(""""your job is to give a strict time estimation algorithm for a high school Student who works at average pace" +
                                "TASK NAME: $taskName, " +
                                "Using your knowledge using the title, and if any attached file is given, please estimate the minutes required to finish this Make sure TO TAKE THE HIGHER TIME, THE STUDENT IS A SLOWER ONE, AND READING AT LIKE 40 WPM, HE TAKES A DECENT AMOUNT OF TIME DO NOT UNDERCUT, AND GO WITH A HIGHER TIME
                                Anything Math related will be caped at 90 mins, and the average assignment take 45 min
                                period packets typically take 8 horus, okay history period packet take max 8 hours, NO MORE, NO  2040 " +
                                "RULES:" +
                                "RETURN ONLY INTEGER NUMBER, " +
                                "DO NOT WRITE MINUTES" +
                                "if your not sure, just write 30" +
                                "" +
                                "Example response: 7" +
                                "no explanation or anything, just a number given to reflect the task""".trimIndent())

                        if (file != null && file.exists()) {
                            val fileExtension = file.extension.lowercase()
                            when (fileExtension) {
                                "pdf" -> {
                                    blob("application/pdf", file.readBytes())
                                }
                                "txt" -> {
                                    text("Assignment context:\n ${file.readText()}")
                                }
                                else -> {
                                    text("Another file has been attached with the name, ${file.name}, but isn't compatible rightnow, so use context clues from the name")
                                }
                            }
                        }
                    }
                )
            }
            val rawText = response.text?.trim() ?: ""

            val time = Regex("\\d+").find(rawText)?.value?.toInt()

            println(time)
            time ?: backupTime
        } catch (e: Exception) {
            println(e.message)
            backupTime
        }
        return aiTime
    }
}