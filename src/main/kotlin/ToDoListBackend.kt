import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

//Set Status
enum class TaskStatus { //what category it belongs in
    TODO, DOING, DONE
}

enum class TaskTag {
    MISSING, UPCOMING
}

//Base Setup for Task
@Serializable

//All possible data stored in a task
data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var className: String,
    var status: TaskStatus = TaskStatus.TODO,
    var tag: MutableList<TaskTag> = mutableListOf(),
    var time: Int? = null,
    var assignmentURL: String? = null,
    var isEstimating: Boolean = false // when true that means the AI process is going
)

class ToDoListBackend {
    private val tasks = mutableListOf<Task>()
    private val saveFile = File("syncr_tasks.json")

    //runs the moment this class is called
    init {
        loadTask()
    }

    //returns all tasks
    fun allTasks(): List<Task> {
        return tasks.toList()
    }


    //adds a task to the list
    fun addTask(title: String, className: String, time: Int? = null, url: String? = null, tag: List<TaskTag> = emptyList()) {
        //all the required info for a new task, status, estimating, and id are not needed for creation but can be manually edited
        val newTask =  Task(
            title = title,
            className = className,
            time = time,
            assignmentURL = url,
            tag = tag.toMutableList()
        )
        tasks.add(newTask)
        saveTask()
    }

    //allows to update the status for each task
    fun updateStatus(taskId: String, newStatus: TaskStatus) {
        val task = tasks.find { it.id == taskId}
        if (task != null) {
            task.status = newStatus
            saveTask()
        }
    }

    //FOR THE AI TIME
    fun updateTime(taskId: String, time: Int) {
        val task = tasks.find { it.id == taskId}
        if (task != null) {
            task.time = time
            saveTask()
        }
    }

    // deletes a task
    fun deleteTask(taskId: String) {
        tasks.removeIf { it.id == taskId}
        saveTask()
    }

    //mark the process of the AI loading
    fun setEstimating(taskId: String, loading: Boolean) {
        val task = tasks.find { it.id == taskId}
        if (task != null) {
            task.isEstimating = loading
            //doesn't need to save as this is just a temp while its finding only for like 30 sec
        }
    }


    fun saveTask() {
        try {
            //filters out the tasks that are done, as no longer need to be stored for the next time the app is opened
            val taskToSave = tasks.filter {it.status != TaskStatus.DONE}

            // stores as json
            val storedJson = Json { prettyPrint= true}
            val jsonString = storedJson.encodeToString(taskToSave)
            saveFile.writeText((jsonString))


        } catch (e: Exception) {
            println("Error in SAVING TASK, ${e.message}")
        }
    }

    //loads all the tasks in from the save file
    fun loadTask() {
        if (saveFile.exists()) {
            try {

                //unloads the json into the Task list that program knows
                val jsonString = saveFile.readText()
                val unloadedJson = Json.decodeFromString<List<Task>>(jsonString)
                tasks.clear()
                tasks.addAll(unloadedJson)

            } catch (e: Exception) {
                println("error loading tasks, ${e.message}")
            }
        }
    }
}

