import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import kotlinx.coroutines.launch
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.items


// Reusable UI Stuff

@Composable
fun GradientBackground(color1: Color, color2: Color) {
    Box(
        modifier = Modifier.fillMaxSize().background(  //sets the size of the background to the entire screen
            Brush.linearGradient( //makes it a gradient
                colors = listOf(  // sets the gradient to be any color I pass through the arg
                    color1,
                    color2
                )
            )
        )
    )
}

@ExperimentalComposeUiApi
@Composable
fun DynamicGradiantBackground(color1: Color) {
    //moved the tracking to the blob
    var mousePos by remember {mutableStateOf(Offset(0f, 0f))}

    val delayedPos by animateOffsetAsState(
        targetValue = mousePos,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f)
    )

    Box(
        modifier = Modifier.fillMaxSize()            //listens for mouse movements
            .onPointerEvent(PointerEventType.Move) { event ->   //s/o google
                mousePos = event.changes.first().position //s/o google
            }
            // makes it looks nice im ngl
            .blur(radius = 75.dp).background(
            brush = Brush.radialGradient( // set the blob to a circle
                colors = listOf(color1, Color.Transparent),
                center = delayedPos, // TODO: see if I should have the mouse tracking in this function specifically so I can reuse this effect
                radius = 200f
            )
        )
    )
}

@Composable
fun fadeInText(texts: String, textColor: Color, drop: Int, time: Int, size: Int, weight: FontWeight) {
    var isTextVisible by remember {mutableStateOf(false)}

    // still don't know what alpha means, but the
    val animText by animateFloatAsState(
        targetValue = if (isTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = time)
    )

    //Makes the Text start animation the second app is opened
    LaunchedEffect(Unit) {
        isTextVisible = true
    }

    //text Config
    Text(
        text = texts,
        color = textColor.copy(alpha = animText),
        fontSize = size.sp,
        fontWeight = weight,
        modifier = Modifier.offset(y = (drop).dp)

    )
}

@Composable
fun resizeTitleText(text: String, color: Color, alignment: TextAlign, fontWeight: FontWeight) {
BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val responsiveTextSize = 20.sp // Makes the size responsive to the screen size, and not a set size
        Text(
            text = text,
            color = color,
            fontSize = responsiveTextSize,
            textAlign = alignment,
            fontWeight = fontWeight,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(task: Task, onDelete: () -> Unit, reqTimeEst: suspend (Task) -> Int, onStatusChange: (TaskStatus) -> Unit) {
    //this allows for asynchronous tasks to be preformed
    val scope = rememberCoroutineScope()
    //allows these var to not be ran once and not repeated over and over again
    var time by remember { mutableStateOf(task.time) }
    var loading by remember { mutableStateOf(false) }

    //Just Card Design, very rough v1
    Card(
        modifier = Modifier.padding(5.dp).fillMaxWidth(),
        elevation = 0.2.dp
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                if (task.tag.isNotEmpty()){
                    Text(text = task.tag.joinToString { it.name }, fontSize = 14.sp, color = Color.Red)
                }
                Text(text = task.className, fontSize = 14.sp, color = Color.Black)
                Text(text = task.title, fontSize = 20.sp, color = Color.Black)

                //displays what tag the task has

                //displays time if available
                if (time != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Est. $time min", fontSize = 14.sp, color = Color(0xFF6200EE), fontWeight = FontWeight.Medium)
                }

                //rows of button
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp) )

                {
                    //move task status left
                    if(task.status != TaskStatus.TODO) {
                        IconButton(
                            onClick = {
                                val prevStatus = if (task.status == TaskStatus.DONE) {
                                    TaskStatus.DOING
                                } else {
                                    TaskStatus.TODO
                                }
                                onStatusChange(prevStatus)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, "previousStatus")
                        }
                    }

                    //move task status right
                    if(task.status != TaskStatus.DONE) {
                        IconButton(
                            onClick = {
                                val prevStatus = if (task.status == TaskStatus.TODO) {
                                    TaskStatus.DOING
                                } else {
                                    TaskStatus.DONE
                                }
                                onStatusChange(prevStatus)
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, "nextStatus")
                        }
                    }

                    //ai time button
                    IconButton( onClick = {
                        scope.launch {
                            loading = true
                            time = reqTimeEst(task)
                            loading = false
                        }
                    },
                        modifier = Modifier.size(20.dp)
                    ) {
                        if (loading) {
                            Text("...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.PlayArrow, "estimate")
                        }
                    }

                    // link to copy button
                    IconButton( onClick = {
                        if (task.assignmentURL != null) {
                            val link = StringSelection(task.assignmentURL)
                            Toolkit.getDefaultToolkit().systemClipboard.setContents(link,link)
                        }
                    }, modifier = Modifier.size(20.dp)
                    ) {
                       Icon(Icons.Default.Share, "Copy Link")
                    }
                    //Delete the task
                    IconButton (onClick = onDelete, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }
        }
    }
}


@Composable
fun StatusColumn(title: String,
                 tasks: List<Task>,
                 onStatusChange: (Task, TaskStatus) -> Unit,
                 onDelete: (Task) -> Unit,
                 columnColor: Color,
                 reqTimeEst: suspend (Task) -> Int,
                 modifier: Modifier = Modifier) {
    Column(
        //aesthetic settings
        modifier = modifier.fillMaxHeight().background(columnColor, RoundedCornerShape(10.dp))
        .padding(8.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        LazyColumn (
            //since the columns are next to each other they need to by spaced and padding
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom= 20.dp)
        ) {
            items(tasks) { task ->
                //just task card stuff
                TaskCard(
                task = task,
                onDelete = {
                    onDelete(task)
                },
                reqTimeEst = reqTimeEst,
                    onStatusChange = { newStatus ->
                        onStatusChange(task, newStatus)
                    })
            }
        }
    }
}

