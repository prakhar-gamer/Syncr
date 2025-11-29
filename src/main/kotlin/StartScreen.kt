import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StartScreen(color1: Color, color2: Color, blobcolor: Color, textColor: Color, onContinueClicked: () -> Unit) {
    //Top layer
    Box(modifier = Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() },
        indication = null){ // gets rid of visual hover default animation
        onContinueClicked()// temp checking how to see if it can udnderstood it got clicked
    }, contentAlignment = Alignment.Center){

        //Sets the scope for where it should track mouse movements, entire screen
        Box(modifier = Modifier.fillMaxSize())

        { //[BACK LAYER] The background Layer
            //Sets the Background gradiant with cool colors.
            GradientBackground(color1, color2)
            //This is blob that follows your cursor.
            DynamicGradiantBackground(blobcolor)
        }
        //FRONT LAYER (TITLE)
        fadeInText("Syncr", textColor, -150, 100, 120, FontWeight.Bold)
        //UNDERNEATH TEXT (SUBTEXT)
        fadeInText("The Student's Todo List", textColor, -50, 200, 50, FontWeight.Bold)
        //CTA TEXT
        fadeInText("Click anywhere to continue...", textColor, -5, 8000, 30, FontWeight.Thin)
    }
}