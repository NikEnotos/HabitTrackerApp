package com.example.tracker.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tracker.AuthViewModel
import com.example.tracker.ui.theme.DangerousButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.tracker.utils.NotificationUtils
import com.example.tracker.utils.NotificationUtils.scheduleHabitReminders
import com.example.tracker.utils.NotificationUtils.setNotificationsTime

@Composable
fun UserProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {

    val user = Firebase.auth.currentUser
    val context = LocalContext.current

    var userEmailAddress by remember {
        mutableStateOf("Can't load your email address")
    }
    var formattedDate by remember {
        mutableStateOf("can't load this info")
    }
    var notificationsEnabled by remember {
        mutableStateOf(NotificationUtils.areNotificationsEnabled(context))
    }

    val preferredTime = NotificationUtils.getNotificationsTime(context)

    var selectedHour by remember { mutableStateOf(preferredTime.first) }
    var selectedMinute by remember { mutableStateOf(preferredTime.second) }

    LaunchedEffect(user) {
        if (user != null) {

            userEmailAddress = Firebase.auth.currentUser!!.email.toString()
            val userMetadata = Firebase.auth.currentUser!!.metadata
            val creationTimestamp = userMetadata!!.creationTimestamp

            val creationDate = Date(creationTimestamp)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Format the date
            formattedDate = dateFormat.format(creationDate)

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "User Profile",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = userEmailAddress,
            enabled = false,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = "Email address",
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "Account created: $formattedDate",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 10.dp)
        )

        Spacer(modifier = Modifier.height(25.dp))

        Row (modifier = Modifier.align(Alignment.Start)){
            Text(
                text = "Notifications ",
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { isChecked ->
                    notificationsEnabled = isChecked
                    NotificationUtils.setNotificationsEnabled(context, isChecked)
                    if (isChecked) {
                        setNotificationsTime(context, selectedHour, selectedMinute)
                        NotificationUtils.cancelScheduledReminders(context)
                        NotificationUtils.cancelAllNotifications(context)
                        scheduleHabitReminders(context, selectedHour, selectedMinute)
                    } else {
                        NotificationUtils.cancelScheduledReminders(context)
                        NotificationUtils.cancelAllNotifications(context)
                    }

                },
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }



        Spacer(modifier = Modifier.height(15.dp))

        if (!notificationsEnabled) {
            Text(
                text = "Select a time to receive daily reminders:",
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                TimeSelector(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    onTimeChanged = { hour, minute ->
                        selectedHour = hour
                        selectedMinute = minute
                    }
                )
            }
        }

        if (notificationsEnabled) {
            Text(
                text = "Notifications are set for: ${"%02d".format(selectedHour)}:${
                    "%02d".format(
                        selectedMinute
                    )
                }",
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }



        Spacer(modifier = Modifier.weight(0.5f))

        TextButton(
            onClick = {
                authViewModel.logout()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Logout",
                fontSize = 20.sp,
                color = DangerousButton
            )
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

}

@Composable
fun TimeSelector(
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onTimeChanged: (hour: Int, minute: Int) -> Unit = { _, _ -> }
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeChanged(selectedHour, selectedMinute)
    }

    Box(
        modifier = Modifier
            .width(300.dp)
            .height(200.dp)
            .padding(16.dp)
    ) {
        // Selection highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.Center)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
        )

        // Time wheels container
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours wheel
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                NumberWheel(
                    items = hours,
                    initialValue = initialHour,
                    onValueChange = { selectedHour = it },
                    formatNumber = { "%02d".format(it) }
                )
            }

            Text(
                text = ":",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Minutes wheel
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                NumberWheel(
                    items = minutes,
                    initialValue = initialMinute,
                    onValueChange = { selectedMinute = it },
                    formatNumber = { "%02d".format(it) }
                )
            }
        }
    }
}

@Composable
fun NumberWheel(
    items: List<Int>,
    initialValue: Int,
    onValueChange: (Int) -> Unit,
    formatNumber: (Int) -> String = { it.toString() }
) {
    val initialIndex = items.indexOf(initialValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Update the selected value when scrolling stops
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerItemIndex = listState.firstVisibleItemIndex
            if (centerItemIndex >= 0 && centerItemIndex < items.size) {
                val centerItem = items[centerItemIndex]
                onValueChange(centerItem)
            }
        }
    }

    Box(
        modifier = Modifier
            .height(200.dp)
            .width(80.dp)
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 80.dp),
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
        ) {
            items(items.size) { index ->
                val item = items[index]
                val isSelected = listState.firstVisibleItemIndex == index

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = formatNumber(item),
                        fontSize = if (isSelected) 20.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                        modifier = Modifier
                            .alpha(if (isSelected) 1f else 0.6f)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}