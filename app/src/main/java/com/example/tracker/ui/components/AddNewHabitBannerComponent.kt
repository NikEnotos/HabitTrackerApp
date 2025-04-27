package com.example.tracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.R

@Composable
fun AddNewHabitBanner(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        Image(
            painter = painterResource(id = R.drawable.empty),
            contentDescription = "Empty State Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "It's a bit empty here...", fontSize = 20.sp)

        Spacer(modifier = Modifier.weight(0.5f))

        Text(text = "Let's add a new habit!", fontSize = 25.sp, fontWeight = FontWeight.Bold)

        Text(text = "↓", fontSize = 30.sp)
    }
}