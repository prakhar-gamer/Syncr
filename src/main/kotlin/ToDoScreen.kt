import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.Button
import androidx.compose.material.Text
import org.openqa.selenium.chrome.ChromeDriver
import java.io.File


@ExperimentalComposeUiApi
@Composable
fun ToDoScreen() {
    //color config
    val backgroundColor1 = Color(0x7ACDFFDE) // purple
    val backgroundColor2 = Color(0xFFFFFFFF) // blue
    val textColor = Color(0xFF2F2F2F)
    val blobColor = Color(0x7ABEEACE)

    //color for the taskStatus background
    val todoColor = Color(0xD7EFB770)
    val doingColor = Color(0xEBFFEFAE)
    val doneColor = Color(0xD7EAF89C)

    //connects the backend tasks to this
    val backend = remember { ToDoListBackend() }
    var tasks by remember {mutableStateOf(backend.allTasks())}

    //refreshes the tasks on the board
    val onRefresh = { tasks = backend.allTasks()}

    //allows for the importing of tasks, AND the program dont freeze
    val scope = rememberCoroutineScope()
    var importing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        tasks = backend.allTasks()
        println(tasks)
        if (tasks.isEmpty()) {
            backend.addTask(
                title = "SYNCR TEST TASK",
                className = "Compose Desktop",
                url = "www.hackclub.com",
                tag = listOf(TaskTag.UPCOMING)
            )
            tasks = backend.allTasks()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GradientBackground(backgroundColor1, backgroundColor2) // might want to add the blob affect I don't know yet
        DynamicGradiantBackground(blobColor)
        //fadeInText("To do Scene", textColor, -250, 100, 100, FontWeight.Bold) // TEMP NOT NEEDED

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            resizeTitleText("To Do List", textColor, TextAlign.Center, FontWeight.Bold)

            Spacer(modifier = Modifier.height((10.dp)))

            Button(
                onClick = {
                    if (!importing) {
                        scope.launch {
                            importing = true
                            val newTasks = withContext(Dispatchers.IO) {
                                var result: List<Task> = emptyList()

                                importGoogleClassroom(
                                    backend = backend,
                                    onTasksImported = { imported -> result = imported }
                                )
                                result
                            }

                            tasks = newTasks
                            importing = false
                        }
                    }
                }
            ){
                Text(text = if (importing) "Importing..." else "Import from Classroom")
            }
            val estimateTimeForTask: suspend (Task) -> Int = { task ->
                val minutes = withContext(Dispatchers.IO) {
                    estimateTimeAI(task)
                }
            backend.updateTime(task.id, minutes)

            tasks = tasks.map {
                if (it.id == task.id) {
                    it.copy(time = minutes)
                } else {
                    it
                } }
                minutes
            }


            Row(modifier = Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                //column for todo
                StatusColumn(
                    title = "To Do",
                    tasks = tasks.filter { it.status == TaskStatus.TODO },
                    columnColor = todoColor,
                    modifier = Modifier.weight(1f),
                    onDelete = { task ->
                        // updates the UI
                        tasks = tasks.filter { it.id != task.id }
                        // backup
                        backend.deleteTask(task.id)
                        println("Deleted")
                    },
                    onStatusChange = { task, newStatus ->
                        // updates the UI
                        tasks = tasks.map {
                            if (it.id == task.id) it.copy(status = newStatus) else it
                        }
                        // backup/backend removes it
                        backend.updateStatus(task.id, newStatus)
                        println("CHANGED")
                    },
                    reqTimeEst = estimateTimeForTask
                )
                //column for doing
                StatusColumn(
                    title = "Doing",
                    tasks = tasks.filter { it.status == TaskStatus.DOING },
                    columnColor = doingColor,
                    modifier = Modifier.weight(1f),
                    onDelete = { task ->
                        tasks = tasks.filter { it.id != task.id }
                        backend.deleteTask(task.id)
                    },
                    onStatusChange = { task, newStatus ->
                        tasks = tasks.map {
                            if (it.id == task.id) it.copy(status = newStatus) else it
                        }
                        backend.updateStatus(task.id, newStatus)
                    },
                    reqTimeEst = estimateTimeForTask
                )

                //column for done
                StatusColumn(
                    title = "Done",
                    tasks = tasks.filter { it.status == TaskStatus.DONE },
                    columnColor = doneColor,
                    modifier = Modifier.weight(1f),
                    onDelete = { task ->
                        tasks = tasks.filter { it.id != task.id }
                        backend.deleteTask(task.id)
                    },
                    onStatusChange = { task, newStatus ->
                        tasks = tasks.map {
                            if (it.id == task.id) it.copy(status = newStatus) else it
                        }
                        backend.updateStatus(task.id, newStatus)
                    },
                    reqTimeEst = estimateTimeForTask
                )
            }
        }
    }
}

fun importGoogleClassroom(backend: ToDoListBackend, onTasksImported: (List<Task>) -> Unit) {
    val allowedGcClasses = setOf(
        "English III--period 3",
        "AP Physics",
        "Data Science - Per 1",
        "AP Calculus AB Fall Semester (25-26)",
        "AP Comp Sci A",
        "APUSH 25-26"
    )
    try {
        val driver = SyncrDriveManager.driver

        val missingAssignments = scrapeMissingTask(driver)
        val assignedAssignments = scrapeAssignedTask(driver)

        missingAssignments.filter{gc -> allowedGcClasses.contains(gc.className)}
            .forEach { gc ->
            backend.addTask(
                title = gc.title,
                className = gc.className,
                url = gc.assignmentURL,
                tag = listOf(TaskTag.MISSING)
            )
        }

        assignedAssignments.filter{gc -> allowedGcClasses.contains(gc.className)}
            .forEach { gc ->
            backend.addTask(
                title = gc.title,
                className = gc.className,
                url = gc.assignmentURL,
                tag = listOf(TaskTag.UPCOMING)
            )
        }
        onTasksImported(backend.allTasks())
    } catch (e: Exception) {
        println(e.message)
    }
}

fun estimateTimeAI(task: Task): Int {
    val driver = SyncrDriveManager.driver
    DownloaderHelper.setup()


    val url = task.assignmentURL
    var download: File? = null

    //gets the resources for the ai to parse to give a estimate
    if (url != null && url != "No Link Available") {
        try {
            val attachment = resourceFinder(url, driver)
            //takes only real files
            val realFiles = attachment.firstOrNull { it.type != "Link" }

            if (realFiles != null) {
                download = DownloaderHelper.download(driver, realFiles.url)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    val minutes = AiTimeEstimator.estimateTime(task.title, download)

    DownloaderHelper.clearTempFolder()

    return minutes
}
