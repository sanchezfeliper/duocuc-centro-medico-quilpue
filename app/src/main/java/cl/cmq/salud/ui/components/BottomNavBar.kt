package cl.cmq.salud.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.cmq.salud.ui.theme.Green
import cl.cmq.salud.ui.theme.Grey

data class NavItem(val icon: String, val label: String)

@Composable
fun BottomNavBar(
    items: List<NavItem>,
    activeIndex: Int = 0,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .padding(top = 2.dp)
            ) {
                val color = if (index == activeIndex) Green else Grey
                Text(item.icon, color = color, fontSize = 16.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    item.label,
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = if (index == activeIndex) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}
