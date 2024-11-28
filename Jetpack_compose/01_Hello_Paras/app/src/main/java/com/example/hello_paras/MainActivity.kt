package com.example.hello_paras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.hello_paras.ui.theme.Hello_ParasTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

                    Greeting()


        }
    }
}

@Composable
fun Greeting() {
    Text(
        text = "Hello Paras",
        color = Color.Green,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black,
        /*In Jetpack Compose, a Modifier is an object used to decorate or configure UI elements (composables) in a flexible and declarative way. It allows you to modify the behavior, appearance, or layout of a composable.*/
        modifier = Modifier.background(Color.White)
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(start = 20.dp, top = 20.dp,end=20.dp,bottom=20.dp)
        //.fillMaxSize()  instead of above two
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Hello_ParasTheme {
        Greeting()
    }
}