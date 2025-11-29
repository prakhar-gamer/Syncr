
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue



@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    val backgroundColor1 = Color(0x7ACDFFDE) // purple
    val backgroundColor2 = Color(0xFFFFFFFF) // blue
    val blobColor = Color(0x7ABEEACE) // color scheme is bad gotta change it l8tr
    val textColor = Color(0xFF2F2F2F)


    //The Application Window
    Window(onCloseRequest = { SyncrDriveManager.quit()
        exitApplication() }
        , title = "Syncr") {

        var currentScreen by remember { mutableStateOf(0) } // This variable is only ran once, and is basically the scene manager

        if (currentScreen == 0) { // scene 0 = Start Screen
            StartScreen(backgroundColor1, backgroundColor2, blobColor, textColor, onContinueClicked = {
                currentScreen = 1
            })
        } else { // scene 2 is the to do scene, right now Just else if, but
            //Future scenes will be, Setting, Google Classroom Scraper + Time Estimator.
            println("switched")
            ToDoScreen()
        }
    }
}

