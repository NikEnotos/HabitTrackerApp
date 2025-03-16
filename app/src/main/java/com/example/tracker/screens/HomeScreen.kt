package com.example.tracker.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.example.tracker.AuthViewModel
import com.example.tracker.pages.AddNewHabitPage
import com.example.tracker.pages.HomePage
import com.example.tracker.pages.UserProfilePage

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Add", Icons.Default.Add),
        NavItem("Profile", Icons.Default.Person),
    )

    var selectedIndex by remember {
        mutableStateOf(0)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.DarkGray) {
                navItemList.forEachIndexed {index, navItem ->
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                        },
                        label = {
                            Text(text = navItem.label)
                        },
                        colors = NavigationBarItemColors(
                            selectedIndicatorColor = Color.Green,
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            disabledIconColor = Color.Yellow,
                            disabledTextColor = Color.Yellow,
                        )
                    )
                }
            }
        }
    ) {
        ContentScreen(modifier = modifier.padding(it), navController, authViewModel, selectedIndex)
    }

}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, selectedIndex: Int){

    when(selectedIndex){
        0 -> HomePage(modifier)
        1 -> AddNewHabitPage(modifier)
        2 -> UserProfilePage(modifier = modifier, navController, authViewModel)
    }

}

data class NavItem(
    val label: String,
    val icon: ImageVector
)